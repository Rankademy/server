package maruhxn.rankademy.application.competition;

import jakarta.persistence.EntityManager;
import maruhxn.rankademy.application.competition.provided.CompetitionReader;
import maruhxn.rankademy.application.competition.provided.dto.CompetitionDetailResponse;
import maruhxn.rankademy.application.competition.provided.dto.CompetitionPageResponse;
import maruhxn.rankademy.application.competition.provided.dto.CompetitionResultResponse;
import maruhxn.rankademy.application.competition.required.CompetitionRepository;
import maruhxn.rankademy.application.group.required.GroupRepository;
import maruhxn.rankademy.application.team.required.TeamRepository;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.competition.Competition;
import maruhxn.rankademy.domain.competition.CompetitionStatus;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@DisplayName("CompetitionReader 테스트")
class CompetitionReaderTest {

    @Autowired
    CompetitionReader reader;

    @Autowired
    UserRepository userRepository;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    CompetitionRepository competitionRepository;

    @Autowired
    EntityManager em;

    private User groupLeader1;
    private Group group1;

    private User groupLeader2;
    private Group group2;

    @BeforeEach
    void setUp() {
        groupLeader1 = GroupFixture.createLeader("group-leader-1");
        userRepository.save(groupLeader1);

        group1 = GroupFixture.createGroup(groupLeader1, "group1");
        groupRepository.save(group1);

        groupLeader2 = GroupFixture.createLeader("group-leader-2");
        userRepository.save(groupLeader2);

        group2 = GroupFixture.createGroup(groupLeader2, "group2");
        groupRepository.save(group2);
    }

    @Test
    @DisplayName("대항전 상세 정보 조회 - 성공")
    void getDetail() {
        // given
        Team team1 = createTeam("leader1", group1.getId());
        Team team2 = createTeam("leader2", group2.getId());
        Competition competition = Competition.createAfterAccept(team1.getId(), team2.getId());
        competitionRepository.save(competition);

        em.flush();
        em.clear();

        // when
        CompetitionDetailResponse result = reader.getDetail(competition.getId());

        // then
        assertThat(result.competitionId()).isEqualTo(competition.getId());
        assertThat(result.status()).isEqualTo(CompetitionStatus.SCHEDULED);
        assertThat(result.team1().teamId()).isEqualTo(team1.getId());
        assertThat(result.team1().teamName()).isEqualTo(team1.getName());
        assertThat(result.team1().teamMembers()).hasSize(5);
        assertThat(result.team2().teamId()).isEqualTo(team2.getId());
        assertThat(result.team2().teamName()).isEqualTo(team2.getName());
        assertThat(result.team2().teamMembers()).hasSize(5);
    }

    private Team createTeam(String leaderName, Long groupId) {
        User representative = GroupFixture.createLeader(leaderName);
        userRepository.save(representative);

        Set<TeamMember> members = new HashSet<>();
        members.add(new TeamMember(representative, LolPosition.TOP));

        for (int j = 0; j < 4; j++) {
            User memberUser = GroupFixture.createMember(leaderName + groupId + "-" + j + "@test.com", leaderName + groupId + "-" + j);
            userRepository.save(memberUser);
            members.add(new TeamMember(memberUser, LolPosition.values()[j + 1]));
        }

        Team team = Team.create(TeamFixture.createTeamCreateRequest(representative.getId(), members, groupId));
        return teamRepository.save(team);
    }

    @Test
    @DisplayName("대항전 상세 정보 조회 - 대항전 정보 없음")
    void getDetail_competitionNotFound() {
        // given
        Long competitionId = 1L;

        // when & then
        assertThatThrownBy(() -> reader.getDetail(competitionId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("대항전 정보를 찾을 수 없습니다. competitionId: " + competitionId);
    }

    @Test
    @DisplayName("대항전 상세 정보 조회 - 팀1 정보 없음")
    void getDetail_team1NotFound() {
        // given
        Competition competition = Competition.createAfterAccept(1L, 2L);
        competitionRepository.save(competition);

        // when & then
        assertThatThrownBy(() -> reader.getDetail(competition.getId()))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("팀 정보를 찾을 수 없습니다. team1Id: " + 1L);
    }

    @Test
    @DisplayName("대항전 상세 정보 조회 - 팀2 정보 없음")
    void getDetail_team2NotFound() {
        // given
        Team team1 = createTeam("leader1", group1.getId());
        Competition competition = Competition.createAfterAccept(team1.getId(), 2L);
        competitionRepository.save(competition);

        // when & then
        assertThatThrownBy(() -> reader.getDetail(competition.getId()))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("팀 정보를 찾을 수 없습니다. team2Id: " + 2L);
    }

    @Test
    @DisplayName("대항전 결과 조회 - 성공")
    void getResult() {
        // given
        Team team1 = createTeam("leader1", group1.getId());
        Team team2 = createTeam("leader2", group2.getId());
        Competition competition = Competition.createAfterAccept(team1.getId(), team2.getId());

        SubmitCompetitionResultRequest request = new SubmitCompetitionResultRequest(
                team1.getId(),
                team2.getId(),
                3,
                List.of(
                        new SubmitCompetitionResultRequest.SetResultDto(1, team1.getId(), "image1"),
                        new SubmitCompetitionResultRequest.SetResultDto(2, team2.getId(), "image2"),
                        new SubmitCompetitionResultRequest.SetResultDto(3, team1.getId(), "image3")
                ),
                "Team 1 won",
                team1.getId()
        );
        competitionRepository.save(competition);
        competition.submitSetResult(request);
        competitionRepository.save(competition);
        em.flush();
        em.clear();

        // when
        CompetitionResultResponse result = reader.getResult(competition.getId());

        // then
        assertThat(result.competitionId()).isEqualTo(competition.getId());
        assertThat(result.team1().teamId()).isEqualTo(team1.getId());
        assertThat(result.team2().teamId()).isEqualTo(team2.getId());
        assertThat(result.setResults()).hasSize(3);
        assertThat(result.finalWinnerTeamId()).isEqualTo(team1.getId());
    }

    @Test
    @DisplayName("내 대항전 내역 조회 - 성공")
    void getMyCompetitionHistory() {
        // given
        Team team1 = createTeam("leader1", group1.getId());
        Team team2 = createTeam("leader2", group2.getId());
        Team team3 = createTeam("leader3", group1.getId());
        Team team4 = createTeam("leader4", group2.getId());

        User testUser = team1.getTeamMembers().stream()
                .map(TeamMember::getUser)
                .findFirst()
                .orElseThrow();

        Competition competition1 = Competition.createAfterAccept(team1.getId(), team2.getId());
        SubmitCompetitionResultRequest request = new SubmitCompetitionResultRequest(
                team1.getId(),
                team2.getId(),
                3,
                List.of(
                        new SubmitCompetitionResultRequest.SetResultDto(1, team1.getId(), "image1"),
                        new SubmitCompetitionResultRequest.SetResultDto(2, team2.getId(), "image2"),
                        new SubmitCompetitionResultRequest.SetResultDto(3, team1.getId(), "image3")
                ),
                "Team 1 won",
                team1.getId()
        );
        competition1.submitSetResult(request);
        competitionRepository.save(competition1);

        Competition competition2 = Competition.createAfterAccept(team3.getId(), team4.getId());
        competitionRepository.save(competition2);

        Competition competition3 = Competition.createAfterAccept(team1.getId(), team3.getId());
        competitionRepository.save(competition3);

        em.flush();
        em.clear();

        // when
        var result1 = reader.getMyCompetitionHistory(testUser.getId(), 0);
        var result2 = reader.getMyCompetitionHistory(testUser.getId(), 1);

        // then
        assertThat(result1.totalCount()).isEqualTo(2);
        assertThat(result1.competitions()).hasSize(2);
        assertThat(result1.competitions().getLast().competitionId()).isEqualTo(competition1.getId());
        assertThat(result1.competitions().getLast().status()).isEqualTo(CompetitionStatus.COMPLETED);
        List<Long> competitionIds = result1.competitions().stream()
                .map(CompetitionPageResponse.CompetitionListItemResponse::competitionId)
                .toList();
        assertThat(competitionIds).containsExactlyInAnyOrder(competition1.getId(), competition3.getId());

        assertThat(result2.totalCount()).isEqualTo(2);
        assertThat(result2.competitions()).hasSize(0);
    }

    @Test
    @DisplayName("대항전 내역이 없다면 빈 리스트를 반환한다")
    void getMyCompetitionHistory_emptyList() {
        // given
        Team team1 = createTeam("leader1", group1.getId());
        Team team2 = createTeam("leader2", group2.getId());
        Team team3 = createTeam("leader3", group1.getId());
        Team team4 = createTeam("leader4", group2.getId());

        User testUser = team1.getTeamMembers().stream()
                .map(TeamMember::getUser)
                .findFirst()
                .orElseThrow();

        em.flush();
        em.clear();

        // when
        var result = reader.getMyCompetitionHistory(testUser.getId(), 0);

        // then
        assertThat(result.totalCount()).isEqualTo(0);
        assertThat(result.competitions()).hasSize(0);
    }
}
