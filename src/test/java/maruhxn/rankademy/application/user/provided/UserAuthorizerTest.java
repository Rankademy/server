package maruhxn.rankademy.application.user.provided;

import jakarta.persistence.EntityManager;
import maruhxn.rankademy.RankademyTestConfiguration;
import maruhxn.rankademy.application.user.required.UnivMailCertifier;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.user.PasswordEncoder;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.domain.user.dto.RiotAuthRequest;
import maruhxn.rankademy.domain.user.service.SummonerInfoConnector;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static maruhxn.rankademy.domain.user.UserFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
@Import(RankademyTestConfiguration.class)
class UserAuthorizerTest {

    @Autowired
    UserAuthorizer userAuthorizer;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager em;

    @MockitoBean
    UnivMailCertifier univMailCertifier;

    @MockitoBean
    SummonerInfoConnector summonerInfoConnector;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void sendUnivCertifyMail() {
        User user = registerUser();
        enrollUnivInfo(user);

        userAuthorizer.sendUnivCertifyMail(user.getId());

        verify(univMailCertifier, times(1))
                .sendCertifyMail(anyString(), anyString(), anyBoolean());
    }

    @Test
    void completeUnivAuthentication() {
        User user = registerUser();
        enrollUnivInfo(user);

        user = userAuthorizer.completeUnivAuthentication(user.getId(), 1234);
        em.flush();
        em.clear();

        assertThat(user.getUnivInfo().univVerified()).isTrue();
    }

    @Test
    void completeUnivAuthentication_Fail() {
        User user = registerUser();
        enrollUnivInfo(user);
        doThrow(new IllegalArgumentException())
                .when(univMailCertifier)
                .certifyCode(anyString(), anyString(), anyInt());

        assertThatThrownBy(() -> userAuthorizer.completeUnivAuthentication(user.getId(), 0000))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void completeRiotAuthentication() {
        User user = registerUser();
        RiotAuthRequest request = createRiotAuthRequest();
        when(summonerInfoConnector.connect(request))
                .thenReturn(createSummonerInfo(request));

        user = userAuthorizer.completeRiotAuthentication(user.getId(), request);
        em.flush();
        em.clear();

        assertThat(user.getSummonerInfo()).isNotNull();
        assertThat(user.getSummonerInfo().getSummonerName()).isEqualTo(request.summonerName());
        assertThat(user.getSummonerInfo().getSummonerTag()).isEqualTo(request.summonerTag());

        assertThat(user.isAuthorized()).isFalse();

        // 학교 인증까지 성공 시, isAuthorized는 true
        enrollUnivInfo(user);
        user.completeUnivAuthentication();

        assertThat(user.isAuthorized()).isTrue();
    }

    @Test
    void removeRiotAuthentication() {
        User user = registerUser();
        RiotAuthRequest request = createRiotAuthRequest();
        when(summonerInfoConnector.connect(request))
                .thenReturn(createSummonerInfo(request));
        user.connectSummonerInfo(summonerInfoConnector, createRiotAuthRequest());

        assertThat(user.getSummonerInfo()).isNotNull();

        user = userAuthorizer.removeRiotAuthentication(user.getId());
        em.flush();
        em.clear();

        assertThat(user.getSummonerInfo()).isNull();
    }

    private User registerUser() {
        var initialRequest = createUserRegisterRequest();
        User user = userRepository.save(User.register(initialRequest, passwordEncoder));
        em.flush();
        em.clear();
        return user;
    }

    private void enrollUnivInfo(User user) {
        user.enrollUnivInfo(createEnrollUnivRequest());
        userRepository.save(user);
        em.flush();
        em.clear();
    }

}