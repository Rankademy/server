package maruhxn.rankademy.domain.competitionrequest.service;

import maruhxn.rankademy.domain.competitionrequest.CompetitionRequest;
import maruhxn.rankademy.domain.shared.event.SendCompetitionRequestEvent;
import maruhxn.rankademy.domain.team.Team;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompetitionRequestSender 테스트")
class CompetitionRequestSenderTest {

    @InjectMocks
    private CompetitionRequestSender competitionRequestSender;

    @Mock
    private WeeklyLimitPolicy policy;

    @Mock
    private ParticipationCounter counter;

    @Mock
    private ApplicationEventPublisher publisher;

    @Mock
    private Team fromTeam;

    @Mock
    private Team toTeam;

    @Test
    @DisplayName("대항전 요청 성공")
    void sendRequest_Success() {
        // given
        Long actingUserId = 1L;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = now.minusDays(1);

        given(fromTeam.isRepresentative(actingUserId)).willReturn(true);
        given(fromTeam.getGroupId()).willReturn(10L);
        given(toTeam.getGroupId()).willReturn(20L);
        given(policy.currentWindowStart(now)).willReturn(windowStart);
        given(policy.getMaxPerWeek()).willReturn(3);
        given(counter.countUserParticipation(actingUserId, windowStart, now)).willReturn(1);
        given(fromTeam.getId()).willReturn(100L);
        given(toTeam.getId()).willReturn(200L);

        // when
        CompetitionRequest competitionRequest = competitionRequestSender.sendRequest(fromTeam, toTeam, actingUserId, now);

        // then
        assertThat(competitionRequest).isNotNull();
        assertThat(competitionRequest.getFromTeamId()).isEqualTo(100L);
        assertThat(competitionRequest.getToTeamId()).isEqualTo(200L);
        verify(publisher).publishEvent(any(SendCompetitionRequestEvent.class));
    }

    @Test
    @DisplayName("대항전 요청 실패 - 대표자가 아님")
    void sendRequest_Fail_NotRepresentative() {
        // given
        Long actingUserId = 1L;
        LocalDateTime now = LocalDateTime.now();
        given(fromTeam.isRepresentative(actingUserId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> competitionRequestSender.sendRequest(fromTeam, toTeam, actingUserId, now))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("대표자만 대항전을 요청할 수 있습니다.");
    }

    @Test
    @DisplayName("대항전 요청 실패 - 같은 그룹")
    void sendRequest_Fail_SameGroup() {
        // given
        Long actingUserId = 1L;
        LocalDateTime now = LocalDateTime.now();
        given(fromTeam.isRepresentative(actingUserId)).willReturn(true);
        given(fromTeam.getGroupId()).willReturn(10L);
        given(toTeam.getGroupId()).willReturn(10L);

        // when & then
        assertThatThrownBy(() -> competitionRequestSender.sendRequest(fromTeam, toTeam, actingUserId, now))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("같은 그룹끼리는 대항전 요청을 보낼 수 없습니다.");
    }

    @Test
    @DisplayName("대항전 요청 실패 - 주간 참여 횟수 초과")
    void sendRequest_Fail_WeeklyLimitExceeded() {
        // given
        Long actingUserId = 1L;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = now.minusDays(1);

        given(fromTeam.isRepresentative(actingUserId)).willReturn(true);
        given(fromTeam.getGroupId()).willReturn(10L);
        given(toTeam.getGroupId()).willReturn(20L);
        given(policy.currentWindowStart(now)).willReturn(windowStart);
        given(policy.getMaxPerWeek()).willReturn(3);
        given(counter.countUserParticipation(actingUserId, windowStart, now)).willReturn(4); // Exceeded

        // when & then
        assertThatThrownBy(() -> competitionRequestSender.sendRequest(fromTeam, toTeam, actingUserId, now))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("주간 참여 가능 횟수를 초과했습니다.");
    }
}
