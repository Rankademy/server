package maruhxn.rankademy.adapter.webapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import maruhxn.rankademy.RankademyTestConfiguration;
import maruhxn.rankademy.adapter.security.model.RankademyUser;
import maruhxn.rankademy.adapter.security.model.UserInfo;
import maruhxn.rankademy.application.group.required.GroupRepository;
import maruhxn.rankademy.application.team.provided.dto.TeamDetailResponse;
import maruhxn.rankademy.application.team.provided.dto.TeamPageResponse;
import maruhxn.rankademy.application.team.required.TeamRepository;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.group.Group;
import maruhxn.rankademy.domain.group.GroupFixture;
import maruhxn.rankademy.domain.team.Team;
import maruhxn.rankademy.domain.team.TeamFixture;
import maruhxn.rankademy.domain.team.TeamMember;
import maruhxn.rankademy.domain.team.dto.TeamCreateRequest;
import maruhxn.rankademy.domain.user.LolPosition;
import maruhxn.rankademy.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(RankademyTestConfiguration.class)
@DisplayName("Team API 테스트")
class TeamApiTest {

    static final String BASE_URL = "/api/v1/teams";

    @Autowired
    MockMvcTester mvcTester;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    EntityManager em;

    private User groupLeader;
    private Group group;
    private Set<User> members = new HashSet<>();

    @BeforeEach
    void setUp() {
        groupLeader = GroupFixture.createLeader("group-leader");
        userRepository.save(groupLeader);

        group = GroupFixture.createGroup(groupLeader, "test-group");
        groupRepository.save(group);

        for (int i = 0; i < 5; i++) {
            User member = GroupFixture.createMember("member" + i + "@test.com", "member" + i);
            userRepository.save(member);
            members.add(member);
        }
    }

    @Test
    @DisplayName("팀 목록 조회 - 성공")
    void getTeamList() throws Exception {
        // given
        for (int i = 0; i < 20; i++) {
            createTeam("team" + i);
        }

        em.flush();
        em.clear();

        // when
        MvcTestResult result = mvcTester.get().uri(BASE_URL + "?page=0")
                .with(user(RankademyUser.from(UserInfo.from(groupLeader))))
                .exchange();

        // then
        assertThat(result).hasStatusOk();
        TeamPageResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), TeamPageResponse.class);
        assertThat(response.teams()).hasSize(3);
        assertThat(response.totalCount()).isEqualTo(20);
    }

    @Test
    @WithAnonymousUser
    @DisplayName("팀 목록 조회 - 인증되지 않은 사용자")
    void getTeamList_withAnonymousUser() throws Exception {
        // when
        MvcTestResult result = mvcTester.get().uri(BASE_URL + "?page=0")
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("팀 생성 - 성공")
    void createTeam_success() throws Exception {
        // given
        Set<TeamMember> teamMembers = new HashSet<>();
        List<User> memberList = members.stream().toList();
        for (int i = 0; i < members.size(); i++) {
            teamMembers.add(new TeamMember(memberList.get(i), LolPosition.values()[i]));
        }

        User leader = memberList.get(0);
        TeamCreateRequest request = TeamFixture.createTeamCreateRequest(leader.getId(), teamMembers, group.getId());

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
    @DisplayName("팀 생성 - 인증되지 않은 사용자")
    void createTeam_withAnonymousUser() throws Exception {
        // given
        Set<TeamMember> teamMembers = members.stream()
                .map(member -> new TeamMember(member, LolPosition.TOP))
                .collect(Collectors.toSet());

        TeamCreateRequest request = TeamFixture.createTeamCreateRequest(groupLeader.getId(), teamMembers, group.getId());

        // when
        MvcTestResult result = mvcTester.post().uri(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("팀 상세 조회 - 성공")
    void getTeamDetail() throws Exception {
        // given
        Team team = createTeam("test-team");
        em.flush();
        em.clear();

        // when
        MvcTestResult result = mvcTester.get().uri(BASE_URL + "/" + team.getId())
                .with(user(RankademyUser.from(UserInfo.from(groupLeader))))
                .exchange();

        // then
        assertThat(result).hasStatusOk();
        TeamDetailResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), TeamDetailResponse.class);
        assertThat(response.teamId()).isEqualTo(team.getId());
        assertThat(response.teamName()).isEqualTo("test-team");
    }

    @Test
    @DisplayName("팀 상세 조회 - 팀 정보 없음")
    void getTeamDetail_notFound() throws Exception {
        // when
        MvcTestResult result = mvcTester.get().uri(BASE_URL + "/999")
                .with(user(RankademyUser.from(UserInfo.from(groupLeader))))
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @WithAnonymousUser
    @DisplayName("팀 상세 조회 - 인증되지 않은 사용자")
    void getTeamDetail_withAnonymousUser() throws Exception {
        // when
        MvcTestResult result = mvcTester.get().uri(BASE_URL + "/1")
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }


    private Team createTeam(String name) {
        User leader = GroupFixture.createMember(name + "-leader@test.com", name + "-leader");
        userRepository.save(leader);

        Set<TeamMember> teamMembers = new HashSet<>();
        teamMembers.add(new TeamMember(leader, LolPosition.TOP));
        for (int i = 0; i < 4; i++) {
            User m = GroupFixture.createMember(name + "-m" + i + "@test.com", name + "-m" + i);
            userRepository.save(m);
            teamMembers.add(new TeamMember(m, LolPosition.values()[(i + 1) % LolPosition.values().length]));
        }

        TeamCreateRequest request = new TeamCreateRequest(
                group.getId(),
                name,
                "test intro",
                leader.getId(),
                teamMembers
        );

        Team team = Team.create(request);
        return teamRepository.save(team);
    }

}
