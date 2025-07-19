package maruhxn.rankademy.adapter.webapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import maruhxn.rankademy.RankademyTestConfiguration;
import maruhxn.rankademy.adapter.webapi.dto.ProfileResponse;
import maruhxn.rankademy.application.member.required.MemberRepository;
import maruhxn.rankademy.domain.member.LolPosition;
import maruhxn.rankademy.domain.member.Member;
import maruhxn.rankademy.domain.member.dto.MemberProfileUpdateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;

import static maruhxn.rankademy.domain.member.MemberFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(RankademyTestConfiguration.class)
class ProfileApiTest {

    static final String BASE_URL = "/api/v1/{memberId}";

    @Autowired
    MockMvcTester mvcTester;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    EntityManager em;

    @Test
    void getProfile() throws UnsupportedEncodingException, JsonProcessingException {
        Member member = registerMember();

        MvcTestResult result = mvcTester.get().uri(BASE_URL, member.getId())
                .exchange();

        assertThat(result).hasStatusOk();

        var response = objectMapper.readValue(result.getResponse().getContentAsString(), ProfileResponse.class);

        assertAll(
                () -> assertThat(response.id()).isEqualTo(member.getId()),
                () -> assertThat(response.username()).isEqualTo(member.getUsername()),
                () -> assertThat(response.univInfo()).isNull(),
                () -> assertThat(response.mainPosition()).isNull(),
                () -> assertThat(response.summonerInfo()).isNull()
        );
    }

    @Test
    void getProfile_VERIFIED() throws UnsupportedEncodingException, JsonProcessingException {
        Member member = createMember();
        member.enrollUnivInfo(createEnrollUnivRequest());
        member = memberRepository.save(member);
        em.flush();
        em.clear();

        MvcTestResult result = mvcTester.get().uri(BASE_URL, member.getId())
                .exchange();

        assertThat(result).hasStatusOk();

        var response = objectMapper.readValue(result.getResponse().getContentAsString(), ProfileResponse.class);

        Member target = memberRepository.findById(member.getId()).orElseThrow();
        assertAll(
                () -> assertThat(response.id()).isEqualTo(target.getId()),
                () -> assertThat(response.username()).isEqualTo(target.getUsername()),
                () -> assertThat(response.univInfo()).isNotNull(),
                () -> assertThat(response.univInfo().univName()).isEqualTo(target.getUnivInfo().univName()),
                () -> assertThat(response.univInfo().univMail()).isEqualTo(target.getUnivInfo().univMail().address()),
                () -> assertThat(response.univInfo().univVerified()).isEqualTo(false),
                () -> assertThat(response.univInfo().major()).isEqualTo(target.getUnivInfo().major()),
                () -> assertThat(response.mainPosition()).isNull(),
                () -> assertThat(response.summonerInfo()).isNull()
        );

        member.completeUnivAuthentication();
        member.connectSummonerInfo(createSummonerInfoConnector(), createRiotAuthRequest());
        memberRepository.save(member);
        em.flush();
        em.clear();

        // 학교 인증 및 라이엇 인증 완료 후
        MvcTestResult result2 = mvcTester.get().uri(BASE_URL, member.getId())
                .exchange();

        assertThat(result2).hasStatusOk();

        var response2 = objectMapper.readValue(result2.getResponse().getContentAsString(), ProfileResponse.class);

        Member target2 = memberRepository.findById(member.getId()).orElseThrow();
        assertAll(
                () -> assertThat(response2.id()).isEqualTo(target2.getId()),
                () -> assertThat(response2.username()).isEqualTo(target2.getUsername()),
                () -> assertThat(response2.univInfo()).isNotNull(),
                () -> assertThat(response2.univInfo().univName()).isEqualTo(target.getUnivInfo().univName()),
                () -> assertThat(response2.univInfo().univMail()).isEqualTo(target.getUnivInfo().univMail().address()),
                () -> assertThat(response2.univInfo().univVerified()).isEqualTo(true),
                () -> assertThat(response2.univInfo().major()).isEqualTo(target.getUnivInfo().major()),
                () -> assertThat(response2.summonerInfo()).isNotNull(),
                () -> assertThat(response2.summonerInfo().summonerName()).isEqualTo(target2.getSummonerInfo().getSummonerName()),
                () -> assertThat(response2.summonerInfo().summonerTag()).isEqualTo(target2.getSummonerInfo().getSummonerTag())
        );
    }

    @Test
    void updateProfile() throws JsonProcessingException {
        Member member = registerMember();

        var request = new MemberProfileUpdateRequest(
                "new-username",
                "it's description",
                LolPosition.TOP,
                LolPosition.JG
        );
        String requestJson = objectMapper.writeValueAsString(request);

        MvcTestResult result = mvcTester.patch().uri(BASE_URL, member.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .exchange();
        assertThat(result).hasStatus(HttpStatus.NO_CONTENT);

        Member target = memberRepository.findById(member.getId()).orElseThrow();

        assertAll(
                () -> assertThat(target.getUsername()).isEqualTo(request.username()),
                () -> assertThat(target.getDescription()).isEqualTo(request.description()),
                () -> assertThat(target.getMainPosition()).isEqualTo(request.mainPosition()),
                () -> assertThat(target.getSubPosition()).isEqualTo(request.subPosition())
        );
    }

    @Test
    void withdraw() {
        Member member = registerMember();

        MvcTestResult result = mvcTester.delete().uri(BASE_URL, member.getId())
                .exchange();
        assertThat(result).hasStatus(HttpStatus.NO_CONTENT);

        assertThat(memberRepository.findById(member.getId())).isEmpty();
    }

    @Test
    void sendCertifyUnivMail_FAIL() {
        Member member = registerMember();

        MvcTestResult result = mvcTester.post().uri(BASE_URL + "/univ-email/send", member.getId())
                .exchange();
        assertThat(result)
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void sendCertifyUnivMail() {
        Member member = registerMember();
        member.enrollUnivInfo(createEnrollUnivRequest());
        memberRepository.save(member);

        MvcTestResult result = mvcTester.post().uri(BASE_URL + "/univ-email/send", member.getId())
                .exchange();

        assertThat(result).hasStatusOk();
    }

    @Test
    void certifyUnivMail() {
        Member member = registerMember();
        member.enrollUnivInfo(createEnrollUnivRequest());
        memberRepository.save(member);


        MvcTestResult result = mvcTester.post().uri(BASE_URL + "/univ-email/certify", member.getId())
                .param("code", "1234")
                .exchange();

        assertThat(result).hasStatusOk();

        Member target = memberRepository.findById(member.getId()).orElseThrow();
        assertThat(target.getUnivInfo().univVerified()).isTrue();
    }

    @Test
    void certifyUnivMail_FAIL_WITHOUT_CODE() {
        Member member = registerMember();
        member.enrollUnivInfo(createEnrollUnivRequest());
        memberRepository.save(member);


        MvcTestResult result = mvcTester.post().uri(BASE_URL + "/univ-email/certify", member.getId())
                .exchange();

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void rso() throws JsonProcessingException {
        Member member = registerMember();

        var request = createRiotAuthRequest();

        MvcTestResult result = mvcTester.post().uri(BASE_URL + "/rso", member.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();
        assertThat(result).hasStatusOk();

        Member target = memberRepository.findById(member.getId()).orElseThrow();
        assertThat(target.getSummonerInfo().getSummonerName()).isEqualTo(request.summonerName());
        assertThat(target.getSummonerInfo().getSummonerTag()).isEqualTo(request.summonerTag());
    }

    private Member registerMember() {
        Member member = memberRepository.save(createMember());
        em.flush();
        em.clear();
        return member;
    }

}