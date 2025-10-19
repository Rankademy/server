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

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

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
        for (int i = 0; i < 5; i++) {
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
        TeamPageResponse firstPage = teamReader.getTeamList(0);
        TeamPageResponse secondPage = teamReader.getTeamList(1);

        // then
        assertThat(firstPage.totalCount()).isEqualTo(5);
        assertThat(firstPage.teams()).hasSize(3);

        assertThat(secondPage.totalCount()).isEqualTo(5);
        assertThat(secondPage.teams()).hasSize(2);
    }

    @Test
    @DisplayName("팀 목록 조회 - 팀이 없을 경우")
    void getTeamListWhenNoTeams() {
        // when
        TeamPageResponse result = teamReader.getTeamList(0);

        // then
        assertThat(result.totalCount()).isEqualTo(0);
        assertThat(result.teams()).isEmpty();
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
}
