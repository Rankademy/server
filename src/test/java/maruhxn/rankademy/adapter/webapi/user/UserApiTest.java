package maruhxn.rankademy.adapter.webapi.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import maruhxn.rankademy.adapter.security.model.RankademyUser;
import maruhxn.rankademy.adapter.security.model.UserInfo;
import maruhxn.rankademy.application.user.dto.ProfileResponse;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.user.LolPosition;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.domain.user.UserFixture;
import maruhxn.rankademy.domain.user.dto.ProfileUpdateRequest;
import maruhxn.rankademy.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@AutoConfigureMockMvc
class UserApiTest extends IntegrationTestSupport {

    private static final String BASE_URL = "/api/v1/users";

    @Autowired
    MockMvcTester mvcTester;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("유저 프로필 조회 - 성공")
    void getProfile_success() throws Exception {
        User viewer = userRepository.save(UserFixture.createAuthorizedMember("viewer@test.com", "viewer"));
        User target = UserFixture.createAuthorizedMember("target@test.com", "target");
        target.updateProfile(new ProfileUpdateRequest("target", "test description", LolPosition.TOP, LolPosition.JG));
        userRepository.save(target);

        Long viewerId = viewer.getId();
        Long targetId = target.getId();

        em.flush();
        em.clear();

        User persistedViewer = userRepository.findById(viewerId).orElseThrow();
        User persistedTarget = userRepository.findById(targetId).orElseThrow();

        MvcTestResult result = mvcTester.get().uri(BASE_URL + "/" + targetId)
                .with(user(RankademyUser.from(UserInfo.from(persistedViewer))))
                .exchange();

        assertThat(result).hasStatusOk();

        ProfileResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), ProfileResponse.class);

        assertThat(response.id()).isEqualTo(persistedTarget.getId());
        assertThat(response.description()).isEqualTo(persistedTarget.getDescription());
        assertThat(response.univInfo().univName()).isEqualTo(persistedTarget.getUnivInfo().getUnivName());
        assertThat(response.univInfo().univVerified()).isTrue();
        assertThat(response.summonerInfo().summonerName()).isEqualTo(persistedTarget.getSummonerInfo().getSummonerName());
        assertThat(response.summonerInfo().summonerTag()).isEqualTo(persistedTarget.getSummonerInfo().getSummonerTag());
        assertThat(response.mainPosition()).isEqualTo(persistedTarget.getMainPosition());
        assertThat(response.subPosition()).isEqualTo(persistedTarget.getSubPosition());
        assertThat(response.mostChampionIds()).isEmpty();
    }

    @Test
    @DisplayName("유저 프로필 조회 - 유저 정보 없음")
    void getProfile_notFoundWhenMissing() {
        User viewer = userRepository.save(UserFixture.createAuthorizedMember("viewer@test.com", "viewer"));
        Long viewerId = viewer.getId();

        em.flush();
        em.clear();

        User persistedViewer = userRepository.findById(viewerId).orElseThrow();

        MvcTestResult result = mvcTester.get().uri(BASE_URL + "/999999")
                .with(user(RankademyUser.from(UserInfo.from(persistedViewer))))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("유저 프로필 조회 - 인증되지 않은 사용자")
    void getProfile_notFoundWhenTargetUnauthorized() {
        User viewer = userRepository.save(UserFixture.createAuthorizedMember("viewer@test.com", "viewer"));
        User unauthorized = userRepository.save(UserFixture.createUser("unauth@test.com", "unauth"));

        Long viewerId = viewer.getId();
        Long targetId = unauthorized.getId();

        em.flush();
        em.clear();

        User persistedViewer = userRepository.findById(viewerId).orElseThrow();

        MvcTestResult result = mvcTester.get().uri(BASE_URL + "/" + targetId)
                .with(user(RankademyUser.from(UserInfo.from(persistedViewer))))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    }
}