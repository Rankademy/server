package maruhxn.rankademy.domain.competition;

import maruhxn.rankademy.domain.competition.dto.SubmitCompetitionResultRequest;
import maruhxn.rankademy.domain.team.Team;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("도메인 - Competition")
class CompetitionTest {

    @Test
    @DisplayName("대항전 생성 시, 상태가 SCHEDULED이고 팀 정보가 올바르게 등록된다")
    void createCompetition() {
        // given
        Competition competition = CompetitionFixture.createCompetition();

        // when & then
        assertThat(competition).isNotNull();
        assertThat(competition.getStatus()).isEqualTo(CompetitionStatus.SCHEDULED);
        assertThat(competition.getTeam1()).isNotNull();
        assertThat(competition.getTeam2()).isNotNull();
        assertThat(competition.getTeam1()).isNotEqualTo(competition.getTeam2());
        assertThat(competition.getScheduledAt()).isNotNull();
    }

    @Test
    @DisplayName("대항전 결과 제출 시, 상태가 RESULT_SUBMITTED로 변경되고 결과가 정상적으로 반영된다")
    void submitSetResult_Success() {
        // given
        Competition competition = CompetitionFixture.createCompetition();
        Team team1 = competition.getTeam1();
        Team team2 = competition.getTeam2();
        Long winnerTeamId = team1.getId();

        List<SubmitCompetitionResultRequest.SetResultDto> setResults = List.of(
                new SubmitCompetitionResultRequest.SetResultDto(1, team1.getId(), "key1"),
                new SubmitCompetitionResultRequest.SetResultDto(2, team2.getId(), "key2"),
                new SubmitCompetitionResultRequest.SetResultDto(3, team1.getId(), "key3")
        );

        SubmitCompetitionResultRequest request = new SubmitCompetitionResultRequest(
                team1.getId(),
                team2.getId(),
                3,
                setResults,
                "Team 1 won",
                winnerTeamId
        );

        // when
        competition.submitSetResult(request);

        // then
        assertThat(competition.getStatus()).isEqualTo(CompetitionStatus.RESULT_SUBMITTED);
        assertThat(competition.getTotalSets()).isEqualTo(3);
        assertThat(competition.getFinalWinnerTeamId()).isEqualTo(winnerTeamId);
        assertThat(competition.getMemo()).isEqualTo("Team 1 won");
        assertThat(competition.getSetResults()).hasSize(3);
        assertThat(competition.getFinalWinner()).isEqualTo(team1);
    }

    @Test
    @DisplayName("잘못된 팀 구성으로 결과 제출 시, IllegalArgumentException이 발생한다")
    void submitSetResult_Fail_With_InvalidTeams() {
        // given
        Competition competition = CompetitionFixture.createCompetition();
        Team team1 = competition.getTeam1();
        Long winnerTeamId = team1.getId();

        SubmitCompetitionResultRequest request = new SubmitCompetitionResultRequest(
                team1.getId(),
                999L, // Invalid team ID
                0,
                List.of(),
                "Invalid memo",
                winnerTeamId
        );

        // when & then
        assertThatThrownBy(() -> competition.submitSetResult(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("세트의 팀 구성이 대항전과 다릅니다.");
    }

    @Test
    @DisplayName("이미 결과가 제출된 대항전에 다시 결과 제출 시, IllegalStateException이 발생한다")
    void submitSetResult_Fail_With_AlreadySubmitted() {
        // given
        Competition competition = CompetitionFixture.createCompetition();
        Team team1 = competition.getTeam1();
        Team team2 = competition.getTeam2();
        Long winnerTeamId = team1.getId();

        SubmitCompetitionResultRequest request = new SubmitCompetitionResultRequest(
                team1.getId(),
                team2.getId(),
                0,
                List.of(),
                "Team 1 won",
                winnerTeamId
        );

        competition.submitSetResult(request); // First submission

        // when & then
        assertThatThrownBy(() -> competition.submitSetResult(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 진행된 대항전입니다.");
    }

    @Test
    @DisplayName("최종 승리팀을 올바르게 반환한다")
    void getFinalWinner() {
        // given
        Competition competition = CompetitionFixture.createCompetition();
        Team team1 = competition.getTeam1();
        Team team2 = competition.getTeam2();
        Long winnerTeamId = team2.getId();

        List<SubmitCompetitionResultRequest.SetResultDto> setResults = List.of(
                new SubmitCompetitionResultRequest.SetResultDto(1, team2.getId(), "key1")
        );

        SubmitCompetitionResultRequest request = new SubmitCompetitionResultRequest(
                team1.getId(),
                team2.getId(),
                1,
                setResults,
                "Team 2 won",
                winnerTeamId
        );

        competition.submitSetResult(request);

        // when
        Team finalWinner = competition.getFinalWinner();

        // then
        assertThat(finalWinner).isEqualTo(team2);
    }
}