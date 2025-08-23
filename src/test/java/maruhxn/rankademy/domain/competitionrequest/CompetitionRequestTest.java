package maruhxn.rankademy.domain.competitionrequest;

import maruhxn.rankademy.domain.team.Team;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.domain.user.UserFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

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
        competitionRequest = CompetitionRequestFixture.createCompetitionRequest(fromTeam, toTeam);
    }

    @Test
    @DisplayName("대항전 요청 생성에 성공한다")
    void createCompetitionRequest() {
        // given

        // when
        CompetitionRequest newRequest = new CompetitionRequest(fromTeam.getId(), toTeam.getId());

        // then
        assertThat(newRequest).isNotNull();
        assertThat(newRequest.getFromTeam()).isEqualTo(fromTeam.getId());
        assertThat(newRequest.getToTeam()).isEqualTo(toTeam.getId());
        assertThat(newRequest.getStatus()).isEqualTo(CompetitionRequestStatus.PENDING);
        assertThat(newRequest.getRequestedAt()).isNotNull();
    }

    @Test
    @DisplayName("대항전 요청 수락에 성공한다")
    void acceptRequest() {
        // given

        // when
        competitionRequest.accept(fromTeam, toTeam);

        // then
        assertThat(competitionRequest.getStatus()).isEqualTo(CompetitionRequestStatus.ACCEPTED);
    }

    @Test
    @DisplayName("이미 처리된 요청은 수락할 수 없다")
    void acceptRequest_whenAlreadyAccepted() {
        // given
        competitionRequest.accept(fromTeam, toTeam);

        // when & then
        assertThatThrownBy(() -> competitionRequest.accept(fromTeam, toTeam))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("대기 중인 요청이 아닙니다.");
    }

    @Test
    @DisplayName("같은 그룹의 팀끼리는 대항전을 수락할 수 없다")
    void acceptRequest_withSameGroup() {
        // given
        User representative = UserFixture.createUser("sameGroupRep@test.com", "sameGroupRepName");
        ReflectionTestUtils.setField(representative, "id", 997L);
        Team sameGroupTeam = CompetitionRequestFixture.createTeam(representative.getId(), fromTeam.getGroupId());

        CompetitionRequest sameGroupRequest = CompetitionRequestFixture.createCompetitionRequest(fromTeam, sameGroupTeam);

        // when & then
        assertThatThrownBy(() -> sameGroupRequest.accept(fromTeam, sameGroupTeam))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("같은 그룹끼리는 대항전 요청을 보낼 수 없습니다.");
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
