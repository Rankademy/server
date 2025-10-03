package maruhxn.rankademy.adapter.webapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import maruhxn.rankademy.RankademyTestConfiguration;
import maruhxn.rankademy.adapter.security.model.RankademyUser;
import maruhxn.rankademy.adapter.security.model.UserInfo;
import maruhxn.rankademy.application.user.dto.ProfileResponse;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.user.LolPosition;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.domain.user.dto.ProfileUpdateRequest;
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

import static maruhxn.rankademy.domain.user.UserFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(RankademyTestConfiguration.class)
class ProfileApiTest {

    static final String BASE_URL = "/api/v1/me";

    @Autowired
    MockMvcTester mvcTester;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager em;

    @Test
    void getProfile() throws UnsupportedEncodingException, JsonProcessingException {
        User user = createUser();
        user.enrollUnivInfo(createEnrollUnivRequest());
        user.completeUnivAuthentication();
        user.connectSummonerInfo(createSummonerInfoConnector(), createRiotAuthRequest());
        userRepository.save(user);

        em.flush();
        em.clear();

        RankademyUser mockUser = RankademyUser.from(UserInfo.from(user));

        // 학교 인증 및 라이엇 인증 완료 후
        MvcTestResult result = mvcTester.get().uri(BASE_URL)
                .with(user(mockUser))
                .exchange();

        assertThat(result).hasStatusOk();

        var response = objectMapper.readValue(result.getResponse().getContentAsString(), ProfileResponse.class);

        User target = userRepository.findById(user.getId()).orElseThrow();
        assertAll(
                () -> assertThat(response.id()).isEqualTo(target.getId()),
                () -> assertThat(response.summonerInfo().summonerName()).isEqualTo(target.getSummonerInfo().getSummonerName()),
                () -> assertThat(response.univInfo().univName()).isEqualTo(target.getUnivInfo().getUnivName()),
                () -> assertThat(response.univInfo().univVerified()).isEqualTo(true),
                () -> assertThat(response.univInfo().admissionYear()).isEqualTo(target.getUnivInfo().getAdmissionYear()),
                () -> assertThat(response.univInfo().major()).isEqualTo(target.getUnivInfo().getMajor()),
                () -> assertThat(response.mostChampionIds()).isEmpty(),
                () -> assertThat(response.winRate()).isEqualTo(target.getSummonerInfo().getWinRate())
        );
    }

    @Test
    void updateProfile() throws JsonProcessingException {
        User user = registerUser();
        RankademyUser mockUser = RankademyUser.from(UserInfo.from(user));

        var request = new ProfileUpdateRequest(
                "new-summonerName",
                "it's about",
                LolPosition.TOP,
                LolPosition.JG
        );
        String requestJson = objectMapper.writeValueAsString(request);

        MvcTestResult result = mvcTester.patch().uri(BASE_URL)
                .with(user(mockUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .exchange();
        assertThat(result).hasStatus(HttpStatus.NO_CONTENT);

        User target = userRepository.findById(user.getId()).orElseThrow();

        assertAll(
                () -> assertThat(target.getUsername()).isEqualTo(request.username()),
                () -> assertThat(target.getDescription()).isEqualTo(request.description()),
                () -> assertThat(target.getMainPosition()).isEqualTo(request.mainPosition()),
                () -> assertThat(target.getSubPosition()).isEqualTo(request.subPosition())
        );
    }

    @Test
    void withdraw() {
        User user = registerUser();
        RankademyUser mockUser = RankademyUser.from(UserInfo.from(user));

        MvcTestResult result = mvcTester.delete().uri(BASE_URL)
                .with(user(mockUser))
                .exchange();
        assertThat(result).hasStatus(HttpStatus.NO_CONTENT);

        assertThat(userRepository.findById(user.getId())).isEmpty();
    }

    @Test
    void sendCertifyUnivMail_FAIL() {
        User user = registerUser();
        RankademyUser mockUser = RankademyUser.from(UserInfo.from(user));

        MvcTestResult result = mvcTester.post().uri(BASE_URL + "/univ-email/send", user.getId())
                .with(user(mockUser))
                .exchange();
        assertThat(result)
                .hasStatus(HttpStatus.CONFLICT);
    }

    @Test
    void sendCertifyUnivMail() {
        User user = registerUser();
        user.enrollUnivInfo(createEnrollUnivRequest());
        userRepository.save(user);
        RankademyUser mockUser = RankademyUser.from(UserInfo.from(user));

        MvcTestResult result = mvcTester.post().uri(BASE_URL + "/univ-email/send", user.getId())
                .with(user(mockUser))
                .exchange();

        assertThat(result).hasStatusOk();
    }

    @Test
    void certifyUnivMail() {
        User user = registerUser();
        user.enrollUnivInfo(createEnrollUnivRequest());
        userRepository.save(user);
        RankademyUser mockUser = RankademyUser.from(UserInfo.from(user));


        MvcTestResult result = mvcTester.post().uri(BASE_URL + "/univ-email/certify", user.getId())
                .with(user(mockUser))
                .param("code", "1234")
                .exchange();

        assertThat(result).hasStatusOk();

        User target = userRepository.findById(user.getId()).orElseThrow();
        assertThat(target.getUnivInfo().isUnivVerified()).isTrue();
    }

    @Test
    void certifyUnivMail_FAIL_WITHOUT_CODE() {
        User user = registerUser();
        user.enrollUnivInfo(createEnrollUnivRequest());
        userRepository.save(user);
        RankademyUser mockUser = RankademyUser.from(UserInfo.from(user));


        MvcTestResult result = mvcTester.post().uri(BASE_URL + "/univ-email/certify", user.getId())
                .with(user(mockUser))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void rso() throws JsonProcessingException {
        User user = registerUser();
        RankademyUser mockUser = RankademyUser.from(UserInfo.from(user));

        var request = createRiotAuthRequest();

        MvcTestResult result = mvcTester.post().uri(BASE_URL + "/rso", user.getId())
                .with(user(mockUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();
        assertThat(result).hasStatusOk();

        User target = userRepository.findById(user.getId()).orElseThrow();
        assertThat(target.getSummonerInfo().getSummonerName()).isEqualTo(request.summonerName());
        assertThat(target.getSummonerInfo().getSummonerTag()).isEqualTo(request.summonerTag());
    }

    private User registerUser() {
        User user = userRepository.save(createUser());
        em.flush();
        em.clear();
        return user;
    }

}