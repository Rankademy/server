package maruhxn.rankademy.application.user.provided;

import jakarta.persistence.EntityManager;
import maruhxn.rankademy.application.user.required.UnivMailCertifier;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.univ_certification_code.UnivCertificationCode;
import maruhxn.rankademy.domain.user.Email;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.domain.user.dto.RiotAuthRequest;
import maruhxn.rankademy.domain.user.service.SummonerInfoConnector;
import maruhxn.rankademy.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;

import static maruhxn.rankademy.domain.user.UserFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UserAuthorizerTest extends IntegrationTestSupport {

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

    @Test
    void sendUnivCertifyMail() {
        User user = registerUser();

        userAuthorizer.sendUnivCertifyMail(user.getId(), "서울과학기술대학교", "test@seoultech.ac.kr");

        verify(univMailCertifier, times(1))
                .sendCertifyMail(anyString(), anyInt());
    }

    @Test
    void completeUnivAuthentication() {
        User user = registerUser();

        String email = "test@seoultech.ac.kr";
        int code = 1234;
        BDDMockito.given(univMailCertifier.certifyCode(email, code))
                .willReturn(UnivCertificationCode.create(
                        code,
                        new Email(email),
                        "서울과학기술대학교",
                        user.getId(),
                        LocalDateTime.now().plusMinutes(5)
                ));

        user = userAuthorizer.completeUnivAuthentication(user.getId(), email, code);
        em.flush();
        em.clear();

        assertThat(user.getUnivInfo()).isNotNull();
    }

    @Test
    void completeUnivAuthentication_Fail() {
        User user = registerUser();
        completeUnivAuthentication(user);
        doThrow(new IllegalArgumentException())
                .when(univMailCertifier)
                .certifyCode(anyString(), anyInt());

        assertThatThrownBy(() -> userAuthorizer.completeUnivAuthentication(user.getId(), "test@seoultech.ac.kr", 0000))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void completeRiotAuthentication() {
        User user = registerUser();
        RiotAuthRequest request = createRiotAuthRequest();
        when(summonerInfoConnector.connect(request)).thenReturn(createSummonerInfo(request));

        user = userAuthorizer.completeRiotAuthentication(user.getId(), request);
        em.flush();
        em.clear();

        assertThat(user.getSummonerInfo()).isNotNull();
        assertThat(user.getSummonerInfo().getSummonerName()).isEqualTo(request.summonerName());
        assertThat(user.getSummonerInfo().getSummonerTag()).isEqualTo(request.summonerTag());

        assertThat(user.isAuthorized()).isFalse();

        // 학교 인증까지 성공 시, isAuthorized는 true
        completeUnivAuthentication(user);

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
        User user = userRepository.save(createUser());
        em.flush();
        em.clear();
        return user;
    }

    private void completeUnivAuthentication(User user) {
        user.completeUnivAuthentication(createEnrollUnivRequest());
        userRepository.save(user);
        em.flush();
        em.clear();
    }

}
