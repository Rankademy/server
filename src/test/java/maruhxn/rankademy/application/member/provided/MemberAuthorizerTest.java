package maruhxn.rankademy.application.member.provided;

import jakarta.persistence.EntityManager;
import maruhxn.rankademy.RankademyTestConfiguration;
import maruhxn.rankademy.application.member.required.MemberRepository;
import maruhxn.rankademy.application.member.required.UnivMailCertifier;
import maruhxn.rankademy.domain.member.Member;
import maruhxn.rankademy.domain.member.PasswordEncoder;
import maruhxn.rankademy.domain.member.dto.RiotAuthRequest;
import maruhxn.rankademy.domain.member.service.SummonerInfoConnector;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static maruhxn.rankademy.domain.member.MemberFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
@Import(RankademyTestConfiguration.class)
class MemberAuthorizerTest {

    @Autowired
    MemberAuthorizer memberAuthorizer;

    @Autowired
    MemberRepository memberRepository;

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
        Member member = registerMember();
        enrollUnivInfo(member);

        memberAuthorizer.sendUnivCertifyMail(member.getId());

        verify(univMailCertifier, times(1))
                .sendCertifyMail(anyString(), anyString(), anyBoolean());
    }

    @Test
    void completeUnivAuthentication() {
        Member member = registerMember();
        enrollUnivInfo(member);

        member = memberAuthorizer.completeUnivAuthentication(member.getId(), 1234);
        em.flush();
        em.clear();

        assertThat(member.getUnivInfo().univVerified()).isTrue();
    }

    @Test
    void completeUnivAuthentication_Fail() {
        Member member = registerMember();
        enrollUnivInfo(member);
        doThrow(new IllegalArgumentException())
                .when(univMailCertifier)
                .certifyCode(anyString(), anyString(), anyInt());

        assertThatThrownBy(() -> memberAuthorizer.completeUnivAuthentication(member.getId(), 0000))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void completeRiotAuthentication() {
        Member member = registerMember();
        RiotAuthRequest request = createRiotAuthRequest();
        when(summonerInfoConnector.connect(request))
                .thenReturn(createSummonerInfo(request));

        member = memberAuthorizer.completeRiotAuthentication(member.getId(), request);
        em.flush();
        em.clear();

        assertThat(member.getSummonerInfo()).isNotNull();
        assertThat(member.getSummonerInfo().getSummonerName()).isEqualTo(request.summonerName());
        assertThat(member.getSummonerInfo().getSummonerTag()).isEqualTo(request.summonerTag());

        assertThat(member.isAuthorized()).isFalse();

        // 학교 인증까지 성공 시, isAuthorized는 true
        enrollUnivInfo(member);
        member.completeUnivAuthentication();

        assertThat(member.isAuthorized()).isTrue();
    }

    @Test
    void removeRiotAuthentication() {
        Member member = registerMember();
        RiotAuthRequest request = createRiotAuthRequest();
        when(summonerInfoConnector.connect(request))
                .thenReturn(createSummonerInfo(request));
        member.connectSummonerInfo(summonerInfoConnector, createRiotAuthRequest());

        assertThat(member.getSummonerInfo()).isNotNull();

        member = memberAuthorizer.removeRiotAuthentication(member.getId());
        em.flush();
        em.clear();

        assertThat(member.getSummonerInfo()).isNull();
    }

    private Member registerMember() {
        var initialRequest = createMemberRegisterRequest();
        Member member = memberRepository.save(Member.register(initialRequest, passwordEncoder));
        em.flush();
        em.clear();
        return member;
    }

    private void enrollUnivInfo(Member member) {
        member.enrollUnivInfo(createEnrollUnivRequest());
        memberRepository.save(member);
        em.flush();
        em.clear();
    }

}