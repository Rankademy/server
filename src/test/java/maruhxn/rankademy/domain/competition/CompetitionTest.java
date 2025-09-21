package maruhxn.rankademy.domain.competition;

import maruhxn.rankademy.domain.competition.dto.OpposeResultRequest;
import maruhxn.rankademy.domain.competition.dto.SubmitCompetitionResultRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static maruhxn.rankademy.domain.competition.CompetitionFixture.createSubmitCompetitionResultRequest;
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
        assertThat(competition.getTeam1Id()).isNotNull();
        assertThat(competition.getTeam2Id()).isNotNull();
        assertThat(competition.getTeam1Id()).isNotEqualTo(competition.getTeam2Id());
        assertThat(competition.getScheduledAt()).isNotNull();
    }

    @Test
    @DisplayName("대항전 결과 제출 시, 상태가 RESULT_SUBMITTED로 변경되고 결과가 정상적으로 반영된다")
    void submitSetResult_Success() {
        // given
        Competition competition = CompetitionFixture.createCompetition();
        Long team1Id = competition.getTeam1Id();
        Long team2Id = competition.getTeam2Id();

        List<SubmitCompetitionResultRequest.SetResultDto> setResults = List.of(
                new SubmitCompetitionResultRequest.SetResultDto(1, team1Id, "key1"),
                new SubmitCompetitionResultRequest.SetResultDto(2, team2Id, "key2"),
                new SubmitCompetitionResultRequest.SetResultDto(3, team1Id, "key3")
        );

        SubmitCompetitionResultRequest request = new SubmitCompetitionResultRequest(
                team1Id,
                team2Id,
                3,
                setResults,
                "Team 1 won",
                team1Id
        );

        // when
        competition.submitSetResult(request);

        // then
        assertThat(competition.getStatus()).isEqualTo(CompetitionStatus.COMPLETED);
        assertThat(competition.getTotalSets()).isEqualTo(3);
        assertThat(competition.getFinalWinnerTeamId()).isEqualTo(team1Id);
        assertThat(competition.getMemo()).isEqualTo("Team 1 won");
        assertThat(competition.getSetResults()).hasSize(3);
        assertThat(competition.getFinalWinnerId()).isEqualTo(team1Id);
    }

    @Test
    @DisplayName("잘못된 팀 구성으로 결과 제출 시, IllegalArgumentException이 발생한다")
    void submitSetResult_Fail_With_InvalidTeams() {
        // given
        Competition competition = CompetitionFixture.createCompetition();
        Long team1Id = competition.getTeam1Id();

        SubmitCompetitionResultRequest request = createSubmitCompetitionResultRequest(
                team1Id,
                999L,
                1,
                team1Id
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
        Long team1Id = competition.getTeam1Id();
        Long team2Id = competition.getTeam2Id();

        SubmitCompetitionResultRequest request = createSubmitCompetitionResultRequest(
                team1Id,
                team2Id,
                1,
                team1Id
        );

        competition.submitSetResult(request); // First submission

        // when & then
        assertThatThrownBy(() -> competition.submitSetResult(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 진행된 대항전입니다.");
    }

    @Test
    @DisplayName("최종 승리팀을 올바르게 반환한다")
    void getFinalWinnerId() {
        // given
        Competition competition = CompetitionFixture.createCompetition();
        Long team1Id = competition.getTeam1Id();
        Long team2Id = competition.getTeam2Id();

        List<SubmitCompetitionResultRequest.SetResultDto> setResults = List.of(
                new SubmitCompetitionResultRequest.SetResultDto(1, team2Id, "key1")
        );

        SubmitCompetitionResultRequest request = new SubmitCompetitionResultRequest(
                team1Id,
                team2Id,
                1,
                setResults,
                "Team 2 won",
                team2Id
        );

        competition.submitSetResult(request);

        // when
        Long finalWinnerId = competition.getFinalWinnerId();

        // then
        assertThat(finalWinnerId).isEqualTo(team2Id);
    }

    @Test
    @DisplayName("경기 결과에 이의를 제기한다")
    void oppose() {
        Competition competition = CompetitionFixture.createCompetition();
        Long team1Id = competition.getTeam1Id();
        Long team2Id = competition.getTeam2Id();

        List<SubmitCompetitionResultRequest.SetResultDto> setResults = List.of(
                new SubmitCompetitionResultRequest.SetResultDto(1, team2Id, "key1")
        );

        SubmitCompetitionResultRequest request = new SubmitCompetitionResultRequest(
                team1Id,
                team2Id,
                1,
                setResults,
                "Team 2 won",
                team2Id
        );

        competition.submitSetResult(request);

        OpposeResultRequest opposeResultRequest = new OpposeResultRequest("OCR 결과 잘못됨");

        // when
        competition.oppose(opposeResultRequest, competition.getSubmittedAt());

        // then
        assertThat(competition.getStatus()).isEqualTo(CompetitionStatus.OPPOSED);
        assertThat(competition.getOpposedReason()).isEqualTo(opposeResultRequest.reason());
    }

    @Test
    @DisplayName("등록되지 않은 경기 결과에 대해서는 이의 신청할 수 없다")
    void opposeFail() {
        Competition competition = CompetitionFixture.createCompetition();

        OpposeResultRequest opposeResultRequest = new OpposeResultRequest("OCR 결과 잘못됨");

        // when
        assertThatThrownBy(() -> competition.oppose(opposeResultRequest, LocalDateTime.now()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("등록된지 7일이 지난 경기 결과에 대해서는 이의 신청할 수 없다")
    void opposeFail_over7days() {
        Competition competition = CompetitionFixture.createCompetition();
        Long team1Id = competition.getTeam1Id();
        Long team2Id = competition.getTeam2Id();

        List<SubmitCompetitionResultRequest.SetResultDto> setResults = List.of(
                new SubmitCompetitionResultRequest.SetResultDto(1, team2Id, "key1")
        );

        SubmitCompetitionResultRequest request = new SubmitCompetitionResultRequest(
                team1Id,
                team2Id,
                1,
                setResults,
                "Team 2 won",
                team2Id
        );

        competition.submitSetResult(request);

        OpposeResultRequest opposeResultRequest = new OpposeResultRequest("OCR 결과 잘못됨");

        // when
        assertThatThrownBy(() -> competition.oppose(opposeResultRequest, competition.getSubmittedAt().plusDays(7)))
                .isInstanceOf(IllegalStateException.class);
    }
}