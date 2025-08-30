package maruhxn.rankademy.domain.competitionrequest;

import maruhxn.rankademy.domain.team.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("도메인 - CompetitionRequest")
class CompetitionRequestTest {

    private Team fromTeam;
    private Team toTeam;
    private CompetitionRequest competitionRequest;

    @BeforeEach
    void setUp() {
        fromTeam = CompetitionRequestFixture.createTeam(998L, 1L);
        toTeam = CompetitionRequestFixture.createTeam(999L, 2L);
        competitionRequest = CompetitionRequestFixture.createCompetitionRequest(fromTeam, toTeam, LocalDateTime.now());
    }

    @Test
    @DisplayName("대항전 요청 생성에 성공한다")
    void createCompetitionRequest() {
        // when
        CompetitionRequest newRequest = new CompetitionRequest(fromTeam.getId(), toTeam.getId(), LocalDateTime.now());

        // then
        assertThat(newRequest).isNotNull();
        assertThat(newRequest.getFromTeamId()).isEqualTo(fromTeam.getId());
        assertThat(newRequest.getToTeamId()).isEqualTo(toTeam.getId());
        assertThat(newRequest.getStatus()).isEqualTo(CompetitionRequestStatus.PENDING);
        assertThat(newRequest.getRequestedAt()).isNotNull();
    }

    @Test
    @DisplayName("대항전 요청 수락에 성공한다")
    void acceptRequest() {
        // given

        // when
        competitionRequest.accept();

        // then
        assertThat(competitionRequest.getStatus()).isEqualTo(CompetitionRequestStatus.ACCEPTED);
    }

    @Test
    @DisplayName("이미 처리된 요청은 수락할 수 없다")
    void acceptRequest_whenAlreadyAccepted() {
        // given
        competitionRequest.accept();

        // when & then
        assertThatThrownBy(() -> competitionRequest.accept())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("대기 중인 요청이 아닙니다.");
    }

    @Test
    @DisplayName("대항전 요청 거절에 성공한다")
    void rejectRequest() {
        // given

        // when
        competitionRequest.reject();

        // then
        assertThat(competitionRequest.getStatus()).isEqualTo(CompetitionRequestStatus.REJECTED);
    }

    @Test
    @DisplayName("이미 처리된 요청은 거절할 수 없다")
    void rejectRequest_whenAlreadyRejected() {
        // given
        competitionRequest.reject();

        // when & then
        assertThatThrownBy(() -> competitionRequest.reject())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("대기 중인 요청이 아닙니다.");
    }
}
