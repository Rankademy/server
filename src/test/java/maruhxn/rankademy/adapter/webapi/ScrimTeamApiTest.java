package maruhxn.rankademy.adapter.webapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import maruhxn.rankademy.adapter.security.model.RankademyUser;
import maruhxn.rankademy.adapter.security.model.UserInfo;
import maruhxn.rankademy.application.scrim_team.required.ScrimTeamRepository;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.scrim_team.ScrimTeam;
import maruhxn.rankademy.domain.scrim_team.ScrimTeamMember;
import maruhxn.rankademy.domain.scrim_team.dto.ScrimTeamCreateRequest;
import maruhxn.rankademy.domain.scrim_team.dto.ScrimTeamUpdateRequest;
import maruhxn.rankademy.domain.user.LolPosition;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.domain.user.UserFixture;
import maruhxn.rankademy.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@AutoConfigureMockMvc
@DisplayName("ScrimTeam API 테스트")
class ScrimTeamApiTest extends IntegrationTestSupport {

    static final String BASE_URL = "/api/v1/scrim-teams";

    @Autowired
    MockMvcTester mvcTester;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ScrimTeamRepository scrimTeamRepository;

    @Autowired
    EntityManager em;

    private User leader;
    private User other;

    @BeforeEach
    void setUp() {
        leader = UserFixture.createAuthorizedMember("leader@test.com", "leader");
        userRepository.save(leader);

        other = UserFixture.createAuthorizedMember("other@test.com", "other");
        userRepository.save(other);
    }

    @Test
    @DisplayName("내전 팀 목록 조회 - 성공")
    void getScrimTeamList() throws Exception {
        // given
        for (int i = 0; i < 5; i++) {
            User leader = UserFixture.createAuthorizedMember("leader" + i + "@test.com", "leader" + i);
            userRepository.save(leader);
            createScrimTeam("scrim" + i, leader);
        }
        em.flush();
        em.clear();

        // when
        MvcTestResult result = mvcTester.get().uri(BASE_URL + "?page=0")
                .with(user(RankademyUser.from(UserInfo.from(leader))))
                .exchange();

        // then
        assertThat(result).hasStatusOk();
    }

    @Test
    @WithAnonymousUser
    @DisplayName("내전 팀 목록 조회 - 인증되지 않은 사용자")
    void getScrimTeamList_withAnonymousUser() throws Exception {
        // when
        MvcTestResult result = mvcTester.get().uri(BASE_URL + "?page=0")
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("내전 팀 생성 - 성공")
    void createScrimTeam_success() throws Exception {
        // given
        ScrimTeamCreateRequest request = createScrimTeamCreateRequest(leader, "scrim");

        // when
        MvcTestResult result = mvcTester.post().uri(BASE_URL)
                .with(user(RankademyUser.from(UserInfo.from(leader))))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.CREATED);
    }

    @Test
    @WithAnonymousUser
    @DisplayName("내전 팀 생성 - 인증되지 않은 사용자")
    void createScrimTeam_withAnonymousUser() throws Exception {
        // given
        ScrimTeamCreateRequest request = createScrimTeamCreateRequest(leader, "scrim");

        // when
        MvcTestResult result = mvcTester.post().uri(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("내전 팀 상세 조회 - 성공")
    void getScrimTeamDetail() throws Exception {
        // given
        ScrimTeam scrimTeam = createScrimTeam("scrim", leader);
        em.flush();
        em.clear();

        // when
        MvcTestResult result = mvcTester.get().uri(BASE_URL + "/" + scrimTeam.getId())
                .with(user(RankademyUser.from(UserInfo.from(leader))))
                .exchange();

        // then
        assertThat(result).hasStatusOk();
    }

    @Test
    @DisplayName("내전 팀 상세 조회 - 팀 정보 없음")
    void getScrimTeamDetail_notFound() throws Exception {
        // when
        MvcTestResult result = mvcTester.get().uri(BASE_URL + "/999")
                .with(user(RankademyUser.from(UserInfo.from(leader))))
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @WithAnonymousUser
    @DisplayName("내전 팀 상세 조회 - 인증되지 않은 사용자")
    void getScrimTeamDetail_withAnonymousUser() throws Exception {
        // when
        MvcTestResult result = mvcTester.get().uri(BASE_URL + "/1")
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("내전 팀 정보 수정 - 성공")
    void updateScrimTeam() throws Exception {
        // given
        ScrimTeam scrimTeam = createScrimTeam("scrim", leader);
        ScrimTeamUpdateRequest request = new ScrimTeamUpdateRequest("updated name", "updated intro");
        em.flush();
        em.clear();

        // when
        MvcTestResult result = mvcTester.patch().uri(BASE_URL + "/" + scrimTeam.getId())
                .with(user(RankademyUser.from(UserInfo.from(leader))))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.NO_CONTENT);
        ScrimTeam findScrim = scrimTeamRepository.findById(scrimTeam.getId()).get();
        assertThat(findScrim.getName()).isEqualTo(request.name());
        assertThat(findScrim.getIntro()).isEqualTo(request.intro());
    }

    @Test
    @DisplayName("내전 팀 정보 수정 - 작성자가 아닌 경우")
    void updateScrimTeam_forbidden() throws Exception {
        // given
        ScrimTeam scrimTeam = createScrimTeam("scrim", leader);
        ScrimTeamUpdateRequest request = new ScrimTeamUpdateRequest("updated name", "updated intro");
        em.flush();
        em.clear();

        // when
        MvcTestResult result = mvcTester.patch().uri(BASE_URL + "/" + scrimTeam.getId())
                .with(user(RankademyUser.from(UserInfo.from(other))))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    @WithAnonymousUser
    @DisplayName("내전 팀 정보 수정 - 인증되지 않은 사용자")
    void updateScrimTeam_withAnonymousUser() throws Exception {
        // given
        ScrimTeamUpdateRequest request = new ScrimTeamUpdateRequest("updated name", "updated intro");

        // when
        MvcTestResult result = mvcTester.patch().uri(BASE_URL + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("내전 팀 삭제 - 성공")
    void deleteScrimTeam() throws Exception {
        // given
        ScrimTeam scrimTeam = createScrimTeam("scrim", leader);
        em.flush();
        em.clear();

        // when
        MvcTestResult result = mvcTester.delete().uri(BASE_URL + "/" + scrimTeam.getId())
                .with(user(RankademyUser.from(UserInfo.from(leader))))
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.NO_CONTENT);
        assertThat(scrimTeamRepository.findById(scrimTeam.getId())).isEmpty();
    }

    @Test
    @DisplayName("내전 팀 삭제 - 작성자가 아닌 경우")
    void deleteScrimTeam_forbidden() throws Exception {
        // given
        ScrimTeam scrimTeam = createScrimTeam("scrim", leader);
        em.flush();
        em.clear();

        // when
        MvcTestResult result = mvcTester.delete().uri(BASE_URL + "/" + scrimTeam.getId())
                .with(user(RankademyUser.from(UserInfo.from(other))))
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    @WithAnonymousUser
    @DisplayName("내전 팀 삭제 - 인증되지 않은 사용자")
    void deleteScrimTeam_withAnonymousUser() throws Exception {
        // when
        MvcTestResult result = mvcTester.delete().uri(BASE_URL + "/1")
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    private ScrimTeamCreateRequest createScrimTeamCreateRequest(User leader, String scrimTeamName) {
        Set<ScrimTeamMember> scrimTeamMembers = new HashSet<>();
        scrimTeamMembers.add(new ScrimTeamMember(leader, LolPosition.TOP));

        for (int i = 0; i < 4; i++) {
            User member = UserFixture.createAuthorizedMember(scrimTeamName + i + "@test.com", scrimTeamName + i);
            userRepository.save(member);
            scrimTeamMembers.add(new ScrimTeamMember(member, LolPosition.values()[(i + 1) % LolPosition.values().length]));
        }

        return new ScrimTeamCreateRequest(
                scrimTeamName,
                "intro",
                leader.getId(),
                scrimTeamMembers
        );
    }


    private ScrimTeam createScrimTeam(String name, User leader) {
        ScrimTeamCreateRequest request = createScrimTeamCreateRequest(leader, name);
        ScrimTeam scrimTeam = ScrimTeam.create(request);
        return scrimTeamRepository.save(scrimTeam);
    }
}
