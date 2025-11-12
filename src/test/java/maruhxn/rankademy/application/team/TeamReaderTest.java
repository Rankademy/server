package maruhxn.rankademy.application.team;

import jakarta.persistence.EntityManager;
import maruhxn.rankademy.application.group.required.GroupRepository;
import maruhxn.rankademy.application.team.provided.TeamReader;
import maruhxn.rankademy.application.team.provided.dto.TeamDetailResponse;
import maruhxn.rankademy.application.team.provided.dto.TeamPageResponse;
import maruhxn.rankademy.application.team.required.TeamRepository;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.group.Group;
import maruhxn.rankademy.domain.group.GroupFixture;
import maruhxn.rankademy.domain.team.Team;
import maruhxn.rankademy.domain.team.TeamFixture;
import maruhxn.rankademy.domain.team.TeamMember;
import maruhxn.rankademy.domain.user.LolPosition;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@DisplayName("TeamReader 테스트")
class TeamReaderTest extends IntegrationTestSupport {

    @Autowired
    TeamReader teamReader;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    EntityManager em;

    private User user;
    private Group group;

    @BeforeEach
    void setUp() {
        user = GroupFixture.createLeader("main-leader");
        userRepository.save(user);

        group = GroupFixture.createGroup(user);
        groupRepository.save(group);
    }

    @Test
    @DisplayName("팀 목록 조회 - 페이징")
    void getTeamList() {
        // given
        for (int i = 0; i < 15; i++) {
            User representative = GroupFixture.createLeader("re" + i);
            userRepository.save(representative);

            Set<TeamMember> members = new HashSet<>();
            members.add(new TeamMember(representative, LolPosition.TOP));

            for (int j = 0; j < 4; j++) {
                User memberUser = GroupFixture.createMember("member" + i + "-" + j + "@test.com", "member" + i + "-" + j);
                userRepository.save(memberUser);
                members.add(new TeamMember(memberUser, LolPosition.values()[j + 1]));
            }

            Team team = Team.create(TeamFixture.createTeamCreateRequest(representative.getId(), TeamFixture.toSlots(members), group.getId()), members);
            teamRepository.save(team);
        }

        em.flush();
        em.clear();

        // when
        TeamPageResponse firstPage = teamReader.getTeamList(user.getId(), 0);
        TeamPageResponse secondPage = teamReader.getTeamList(user.getId(), 1);

        // then
        assertThat(firstPage.totalCount()).isEqualTo(15);
        assertThat(firstPage.teams()).hasSize(10);

        assertThat(secondPage.totalCount()).isEqualTo(15);
        assertThat(secondPage.teams()).hasSize(5);
    }

    @Test
    @DisplayName("팀 목록 조회 - 팀이 없을 경우")
    void getTeamListWhenNoTeams() {
        // when
        TeamPageResponse result = teamReader.getTeamList(user.getId(), 0);

        // then
        assertThat(result.totalCount()).isEqualTo(0);
        assertThat(result.teams()).isEmpty();
    }

    @Test
    @DisplayName("리더 팀을 보유한 사용자는 첫 페이지 상단에 추천 팀 3개가 노출된다")
    void getTeamListWithLeaderRecommendations() {
        // given
        Team leaderTeam = createTeamForRepresentative(user, "leader-main", 1000.0);

        Team nearFirst = createOpponentTeam("near-first", 1001.0);
        Team nearSecond = createOpponentTeam("near-second", 1003.0);
        Team nearThird = createOpponentTeam("near-third", 1006.0);

        List<Team> fillers = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            fillers.add(createOpponentTeam("filler-" + i, 1100.0 + i * 25));
        }
        int totalTeams = 1 + 3 + fillers.size();

        em.flush();
        em.clear();

        // when
        TeamPageResponse result = teamReader.getTeamList(user.getId(), 0);

        // then
        assertThat(result.totalCount()).isEqualTo(totalTeams);
        assertThat(result.teams()).hasSize(10);

        List<TeamPageResponse.TeamResponse> recommended = result.teams().subList(0, 3);
        assertThat(recommended)
                .extracting(TeamPageResponse.TeamResponse::teamId)
                .containsExactly(nearFirst.getId(), nearSecond.getId(), nearThird.getId());
        assertThat(recommended)
                .allMatch(TeamPageResponse.TeamResponse::isRecommended);
        assertThat(recommended)
                .noneMatch(team -> team.teamId().equals(leaderTeam.getId()));

        boolean baseTeamsAreNotRecommended = result.teams().stream()
                .skip(3)
                .allMatch(team -> !team.isRecommended());
        assertThat(baseTeamsAreNotRecommended).isTrue();
    }

    @Test
    @DisplayName("팀 상세 조회 - 성공")
    void getTeamDetails_withSuccess() {
        // given
        User representative = GroupFixture.createLeader("re");
        userRepository.save(representative);

        Set<TeamMember> members = new HashSet<>();
        members.add(new TeamMember(representative, LolPosition.TOP));

        for (int j = 0; j < 4; j++) {
            User memberUser = GroupFixture.createMember("member" + j + "@test.com", "member" + j);
            userRepository.save(memberUser);
            members.add(new TeamMember(memberUser, LolPosition.values()[j + 1]));
        }

        Team team = Team.create(TeamFixture.createTeamCreateRequest(representative.getId(), TeamFixture.toSlots(members), group.getId()), members);
        teamRepository.save(team);

        em.flush();
        em.clear();

        // when
        TeamDetailResponse teamDetails = teamReader.getTeamDetails(user.getId(), team.getId());

        // then
        assertThat(teamDetails).isNotNull();
        assertThat(teamDetails.teamId()).isEqualTo(team.getId());
        assertThat(teamDetails.groupId()).isEqualTo(group.getId());
        assertThat(teamDetails.groupName()).isEqualTo(group.getName());
        assertThat(teamDetails.isActive()).isTrue();
        assertThat(teamDetails.teamMembers()).hasSize(5);
        assertThat(teamDetails.teamMembers().get(0).summonerName()).isNotNull();
    }

    @Test
    @DisplayName("팀 상세 조회 - 존재하지 않는 ID")
    void getTeamDetails_withNonExistentId() {
        // when / then
        assertThatThrownBy(() -> teamReader.getTeamDetails(user.getId(), 999L))
                .isInstanceOf(NoSuchElementException.class);
    }

    private Team createTeamForRepresentative(User representative, String prefix, double avgMmr) {
        Set<TeamMember> members = createMembers(representative, prefix);
        Team team = Team.create(
                TeamFixture.createTeamCreateRequest(representative.getId(), TeamFixture.toSlots(members), group.getId()),
                members
        );
        team.updateTeamMmr(avgMmr);
        return teamRepository.save(team);
    }

    private Team createOpponentTeam(String prefix, double avgMmr) {
        User representative = GroupFixture.createLeader(prefix);
        userRepository.save(representative);
        return createTeamForRepresentative(representative, prefix, avgMmr);
    }

    private Set<TeamMember> createMembers(User representative, String prefix) {
        Set<TeamMember> members = new HashSet<>();
        members.add(new TeamMember(representative, LolPosition.TOP));

        for (int j = 0; j < 4; j++) {
            User memberUser = GroupFixture.createMember(prefix + "-member" + j + "@rankademy.app", prefix + "-member" + j);
            userRepository.save(memberUser);
            members.add(new TeamMember(memberUser, LolPosition.values()[j + 1]));
        }

        return members;
    }
}
