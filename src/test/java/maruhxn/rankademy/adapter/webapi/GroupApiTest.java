package maruhxn.rankademy.adapter.webapi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import maruhxn.rankademy.adapter.security.model.RankademyUser;
import maruhxn.rankademy.adapter.security.model.UserInfo;
import maruhxn.rankademy.application.competition.required.CompetitionRepository;
import maruhxn.rankademy.application.group.provided.dto.GroupDetailResponse;
import maruhxn.rankademy.application.group.provided.dto.MyGroupResponse;
import maruhxn.rankademy.application.group.provided.dto.RecentCompetitionResponse;
import maruhxn.rankademy.application.group.required.GroupRepository;
import maruhxn.rankademy.application.team.required.TeamRepository;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.competition.Competition;
import maruhxn.rankademy.domain.competition.dto.SubmitCompetitionResultRequest;
import maruhxn.rankademy.domain.group.Group;
import maruhxn.rankademy.domain.group.GroupFixture;
import maruhxn.rankademy.domain.group.GroupRole;
import maruhxn.rankademy.domain.group.dto.GroupUpdateRequest;
import maruhxn.rankademy.domain.team.Team;
import maruhxn.rankademy.domain.team.TeamFixture;
import maruhxn.rankademy.domain.team.TeamMember;
import maruhxn.rankademy.domain.team.dto.TeamCreateRequest;
import maruhxn.rankademy.domain.user.LolPosition;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@AutoConfigureMockMvc
class GroupApiTest extends IntegrationTestSupport {

    static final String BASE_URL = "/api/v1/groups";

    @Autowired
    MockMvcTester mvcTester;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    CompetitionRepository competitionRepository;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("내 그룹 목록 조회 - 성공")
    void getMyGroups() throws Exception {
        User member = GroupFixture.createMember();
        userRepository.save(member);

        User leader = GroupFixture.createLeader();
        userRepository.save(leader);

        for (int i = 0; i < 5; i++) {
            Group group = GroupFixture.createGroup(leader, "group" + i);
            group.addMember(member, GroupRole.MEMBER);
            groupRepository.save(group);
        }

        MvcTestResult result = mvcTester.get().uri("/api/v1/groups/my")
                .with(user(RankademyUser.from(UserInfo.from(member))))
                .exchange();
        assertThat(result).hasStatusOk();

        List<MyGroupResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
        });
        assertThat(response).hasSize(5);
    }


    @Test
    @WithAnonymousUser
    @DisplayName("내 그룹 목록 조회 - 인증되지 않은 사용자")
    void getMyGroups_withAnonymousUser() throws Exception {
        User member = GroupFixture.createMember();
        member.removeUnivInfo();
        userRepository.save(member);

        User leader = GroupFixture.createLeader();
        userRepository.save(leader);

        for (int i = 0; i < 5; i++) {
            Group group = GroupFixture.createGroup(leader, "group" + i);
            groupRepository.save(group);
        }

        MvcTestResult result = mvcTester.get().uri("/api/v1/groups/my")
                .with(user(RankademyUser.from(UserInfo.from(member))))
                .exchange();
        assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("그룹 생성 - 성공")
    void createGroup() throws Exception {
        User leader = GroupFixture.createLeader();
        userRepository.save(leader);

        MvcTestResult result = mvcTester.post().uri(BASE_URL)
                .with(user(RankademyUser.from(UserInfo.from(leader))))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(GroupFixture.createGroupCreateRequest()))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("그룹 생성 - 인증은 되었으나 인가는 되지 않은 사용자")
    void createGroup_withNotAuthorizedUser() throws Exception {
        User leader = GroupFixture.createLeader();
        leader.removeUnivInfo();
        userRepository.save(leader);

        MvcTestResult result = mvcTester.post().uri(BASE_URL)
                .with(user(RankademyUser.from(UserInfo.from(leader))))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(GroupFixture.createGroupCreateRequest()))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("그룹 생성 - 인증되지 않은 사용자")
    void createGroup_withAnonymousUser() throws Exception {
        User leader = GroupFixture.createLeader();
        userRepository.save(leader);

        MvcTestResult result = mvcTester.post().uri(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(GroupFixture.createGroupCreateRequest()))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("그룹 상세 조회")
    void getGroupDetail() throws Exception {
        Group group = generateGroup();

        em.flush();
        em.clear();

        MvcTestResult result = mvcTester.get().uri(BASE_URL + "/" + group.getId())
                .exchange();

        assertThat(result).hasStatusOk();

        GroupDetailResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), GroupDetailResponse.class);
        assertThat(response.groupId()).isEqualTo(group.getId());
        assertThat(response.isJoined()).isFalse();
        assertThat(response.isLeader()).isFalse();

    }


    @Test
    @WithAnonymousUser
    @DisplayName("그룹 최근 대항전 조회 - 성공")
    void getRecentCompetitions_success() throws Exception {
        // Given
        User leaderA = GroupFixture.createLeader("leaderA");
        userRepository.save(leaderA);
        Group groupA = GroupFixture.createGroup(leaderA, "groupA");
        groupRepository.save(groupA);
        Team teamA = createTeam("teamA", groupA);
        teamRepository.save(teamA);

        User leaderB = GroupFixture.createLeader("leaderB");
        userRepository.save(leaderB);
        Group groupB = GroupFixture.createGroup(leaderB, "groupB");
        groupRepository.save(groupB);
        Team teamB = createTeam("teamB", groupB);
        teamRepository.save(teamB);

        // Create 7 competitions, only 5 most recent should be returned
        for (int i = 0; i < 5; i++) {
            Competition competition = Competition.createAfterAccept(teamA.getId(), teamB.getId());
            if (i % 2 == 0) { // groupA wins
                setWinStatus(teamA, teamB, competition);
            } else { // groupB wins
                setWinStatus(teamB, teamA, competition);
            }
            competitionRepository.save(competition);
        }

        em.flush();
        em.clear();

        // When
        MvcTestResult result = mvcTester.get().uri(BASE_URL + String.format("/%d/recent-competitions", groupA.getId()))
                .exchange();

        // Then
        assertThat(result).hasStatusOk();
        List<RecentCompetitionResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
        });

        assertThat(response).hasSize(3);

        assertThat(response)
                .hasSize(3)
                .extracting("groupId", "isWin")
                .containsOnly(
                        tuple(teamB.getGroupId(), true),
                        tuple(teamB.getGroupId(), false),
                        tuple(teamB.getGroupId(), true)
                );
    }

    private static void setWinStatus(Team winnerTeam, Team loserTeam, Competition competition) {
        SubmitCompetitionResultRequest request = new SubmitCompetitionResultRequest(
                winnerTeam.getId(),
                loserTeam.getId(),
                1,
                List.of(new SubmitCompetitionResultRequest.SetResultDto(1, winnerTeam.getId(), "k")),
                "m",
                winnerTeam.getId(),
                winnerTeam.getGroupId(),
                loserTeam.getGroupId()
        );
        competition.submitSetResult(request, LocalDateTime.now());
    }

    @Test
    @DisplayName("그룹 멤버 조회")
    void getGroupMembers() throws Exception {
        Group group = generateGroup();

        for (int i = 0; i < 10; i++) {
            User member = GroupFixture.createMember("member" + i + "@test.com", "member" + i);
            userRepository.save(member);
            group.addMember(member, GroupRole.MEMBER);
        }
        groupRepository.save(group);

        MvcTestResult result = mvcTester.get().uri(BASE_URL + String.format("/%d/members", group.getId()))
                .exchange();

        assertThat(result).hasStatusOk();
//        List<GroupMemberResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
//        });

//        assertThat(response).hasSize(7);
    }

    @Test
    @DisplayName("그룹 정보 수정")
    void updateGroup() throws Exception {
        User leader = GroupFixture.createLeader();
        userRepository.save(leader);

        Group group = GroupFixture.createGroup(leader);
        groupRepository.save(group);

        GroupUpdateRequest request = new GroupUpdateRequest(
                "updated",
                "updated",
                "updated.jpg"
        );

        MvcTestResult result = mvcTester.put().uri(BASE_URL + "/" + group.getId())
                .with(user(RankademyUser.from(UserInfo.from(leader))))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();
        assertThat(result).hasStatus(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("그룹 정보 수정 - 리더가 아닌 경우")
    void updateGroup_withNotLeader() throws Exception {
        User member = GroupFixture.createMember();
        userRepository.save(member);
        Group group = generateGroup();

        GroupUpdateRequest request = new GroupUpdateRequest(
                "updated",
                "updated",
                "updated.jpg"
        );

        MvcTestResult result = mvcTester.put().uri(BASE_URL + "/" + group.getId())
                .with(user(RankademyUser.from(UserInfo.from(member))))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();
        assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("그룹 삭제")
    void deleteGroup() throws Exception {
        User leader = GroupFixture.createLeader();
        userRepository.save(leader);

        Group group = GroupFixture.createGroup(leader);
        groupRepository.save(group);

        MvcTestResult result = mvcTester.delete().uri(BASE_URL + String.format("/%d", group.getId()))
                .with(user(RankademyUser.from(UserInfo.from(leader))))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("그룹 삭제 - 리더가 아닌 경우")
    void deleteGroup_withNotLeader() throws Exception {
        User member = GroupFixture.createMember();
        userRepository.save(member);
        Group group = generateGroup();

        MvcTestResult result = mvcTester.delete().uri(BASE_URL + String.format("/%d", group.getId()))
                .with(user(RankademyUser.from(UserInfo.from(member))))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
    }

    private Group generateGroup() {
        User leader = GroupFixture.createLeader();
        userRepository.save(leader);

        Group group = GroupFixture.createGroup(leader);
        groupRepository.save(group);

        em.flush();
        em.clear();

        return group;
    }

    private Team createTeam(String name, Group group) {
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
                TeamFixture.toSlots(teamMembers)
        );

        Team team = Team.create(request, teamMembers);
        return teamRepository.save(team);
    }
}
