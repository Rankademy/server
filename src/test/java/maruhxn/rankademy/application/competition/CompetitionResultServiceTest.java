package maruhxn.rankademy.application.competition;

import jakarta.persistence.EntityManager;
import maruhxn.rankademy.application.competition.required.CompetitionRepository;
import maruhxn.rankademy.application.group.required.GroupRepository;
import maruhxn.rankademy.application.team.required.TeamRepository;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.competition.Competition;
import maruhxn.rankademy.domain.competition.CompetitionStatus;
import maruhxn.rankademy.domain.competition.dto.OpposeResultRequest;
import maruhxn.rankademy.domain.competition.dto.SubmitCompetitionResultRequest;
import maruhxn.rankademy.domain.group.Group;
import maruhxn.rankademy.domain.group.GroupFixture;
import maruhxn.rankademy.domain.team.Team;
import maruhxn.rankademy.domain.team.TeamFixture;
import maruhxn.rankademy.domain.team.TeamMember;
import maruhxn.rankademy.domain.user.LolPosition;
import maruhxn.rankademy.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static maruhxn.rankademy.domain.competition.CompetitionFixture.createSubmitCompetitionResultRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@DisplayName("CompetitionResultService 테스트")
class CompetitionResultServiceTest {

    @Autowired
    private CompetitionResultService competitionResultService;

    @Autowired
    private CompetitionRepository competitionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private EntityManager em;

    private Competition competition;
    private Team team1;
    private Team team2;
    private User groupLeader1;

    @BeforeEach
    void setUp() {
        groupLeader1 = GroupFixture.createLeader("group-leader-1");
        userRepository.save(groupLeader1);
        Group group1 = GroupFixture.createGroup(groupLeader1, "group1");
        groupRepository.save(group1);

        User groupLeader2 = GroupFixture.createLeader("group-leader-2");
        userRepository.save(groupLeader2);
        Group group2 = GroupFixture.createGroup(groupLeader2, "group2");
        groupRepository.save(group2);

        team1 = createTeam("leader1", group1.getId());
        team2 = createTeam("leader2", group2.getId());
        teamRepository.save(team1);
        teamRepository.save(team2);

        competition = Competition.createAfterAccept(team1.getId(), team2.getId());
        competitionRepository.save(competition);

        em.flush();
        em.clear();
    }

    private Team createTeam(String leaderName, Long groupId) {
        User representative = GroupFixture.createLeader(leaderName);
        userRepository.save(representative);

        Set<TeamMember> members = new HashSet<>();
        members.add(new TeamMember(representative, LolPosition.TOP));

        for (int j = 0; j < 4; j++) {
            User memberUser = GroupFixture.createMember("member" + groupId + "-" + j + "@test.com", "member" + groupId + "-" + j);
            userRepository.save(memberUser);
            members.add(new TeamMember(memberUser, LolPosition.values()[j + 1]));
        }

        Team team = Team.create(TeamFixture.createTeamCreateRequest(representative.getId(), members, groupId));
        return teamRepository.save(team);
    }

    @Test
    @DisplayName("결과 제출 - 성공")
    void submitResult() {
        // given
        SubmitCompetitionResultRequest request = createSubmitCompetitionResultRequest(
                team1.getId(),
                team2.getId(),
                3,
                team1.getId()
        );

        // when
        competitionResultService.submitResult(competition.getId(), request);
        em.flush();
        em.clear();

        // then
        Competition foundCompetition = competitionRepository.findById(competition.getId()).get();
        assertThat(foundCompetition.getStatus()).isEqualTo(CompetitionStatus.COMPLETED);
        assertThat(foundCompetition.getFinalWinnerTeamId()).isEqualTo(team1.getId());
        assertThat(foundCompetition.getSetResults()).hasSize(3);
    }

    @Test
    @DisplayName("결과 제출 - 대항전 정보 없음")
    void submitResult_competitionNotFound() {
        // given
        Long invalidCompetitionId = -1L;
        SubmitCompetitionResultRequest request = createSubmitCompetitionResultRequest(
                team1.getId(),
                team2.getId(),
                1,
                team1.getId()
        );

        // when & then
        assertThatThrownBy(() -> competitionResultService.submitResult(invalidCompetitionId, request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("대항전 정보를 찾을 수 없습니다. competitionId: " + invalidCompetitionId);
    }

    @Test
    @DisplayName("결과 이의 제기 - 성공")
    void opposeResult() {
        // given
        // 먼저 결과를 제출
        SubmitCompetitionResultRequest submitRequest =createSubmitCompetitionResultRequest(
                team1.getId(),
                team2.getId(),
                3,
                team1.getId()
        );
        competitionResultService.submitResult(competition.getId(), submitRequest);
        em.flush();
        em.clear();

        OpposeResultRequest opposeRequest = new OpposeResultRequest("이의가 있습니다.");

        // when
        competitionResultService.opposeResult(competition.getId(), opposeRequest);
        em.flush();
        em.clear();

        // then
        Competition foundCompetition = competitionRepository.findById(competition.getId()).get();
        assertThat(foundCompetition.getStatus()).isEqualTo(CompetitionStatus.OPPOSED);
    }

    @Test
    @DisplayName("결과 이의 제기 - 대항전 정보 없음")
    void opposeResult_competitionNotFound() {
        // given
        Long invalidCompetitionId = -1L;
        OpposeResultRequest opposeRequest = new OpposeResultRequest("이의가 있습니다.");

        // when & then
        assertThatThrownBy(() -> competitionResultService.opposeResult(invalidCompetitionId, opposeRequest))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("대항전 정보를 찾을 수 없습니다. competitionId: " + invalidCompetitionId);
    }
}
