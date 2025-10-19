package maruhxn.rankademy.application.notification;

import maruhxn.rankademy.application.notification.required.NotificationRepository;
import maruhxn.rankademy.application.team.required.TeamRepository;
import maruhxn.rankademy.domain.notification.Notification;
import maruhxn.rankademy.domain.shared.event.SendCompetitionRequestEvent;
import maruhxn.rankademy.domain.team.Team;
import maruhxn.rankademy.domain.team.TeamMember;
import maruhxn.rankademy.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("애플리케이션 - 대항전 요청 이벤트 리스너")
class CompetitionRequestSendListenerTest {

    @InjectMocks
    private CompetitionRequestSendListener competitionRequestSendListener;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private TeamRepository teamRepository;

    @Test
    @DisplayName("대항전 요청 이벤트가 발생하면, 해당 팀의 모든 팀원에게 알림을 전송한다.")
    void onCompetitionRequestSend() {
        // given
        User user1 = mock(User.class);
        given(user1.getId()).willReturn(1L);

        User user2 = mock(User.class);
        given(user2.getId()).willReturn(2L);

        TeamMember member1 = mock(TeamMember.class);
        given(member1.getUser()).willReturn(user1);

        TeamMember member2 = mock(TeamMember.class);
        given(member2.getUser()).willReturn(user2);

        Team fromTeam = mock(Team.class);
        given(fromTeam.getName()).willReturn("TeamA");
        Team toTeam = mock(Team.class);
        given(toTeam.getTeamMembers()).willReturn(Set.of(member1, member2));

        Long fromTeamId = 1L;
        Long toTeamId = 2L;
        SendCompetitionRequestEvent event =
                new SendCompetitionRequestEvent(fromTeamId, toTeamId, 1L, LocalDateTime.now());

        given(teamRepository.findByIdWithTeamMember(fromTeamId)).willReturn(Optional.of(fromTeam));
        given(teamRepository.findByIdWithTeamMember(toTeamId)).willReturn(Optional.of(toTeam));

        // when
        competitionRequestSendListener.on(event);

        // then
        verify(teamRepository, times(1)).findByIdWithTeamMember(fromTeamId);
        verify(teamRepository, times(1)).findByIdWithTeamMember(toTeamId);

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(2)).save(notificationCaptor.capture());

        List<Notification> capturedNotifications = notificationCaptor.getAllValues();
        assertThat(capturedNotifications)
                .hasSize(2)
                .extracting("message")
                .containsExactly("TeamA 팀의 대항전 요청", "TeamA 팀의 대항전 요청");
    }

    @Test
    @DisplayName("대항전 요청 시 팀이 존재하지 않으면 예외가 발생한다.")
    void onCompetitionRequestSend_whenTeamNotFound() {
        // given
        Long fromTeamId = 1L;
        Long toTeamId = 2L;
        SendCompetitionRequestEvent event = new SendCompetitionRequestEvent(fromTeamId, toTeamId, 1L, LocalDateTime.now());
        given(teamRepository.findByIdWithTeamMember(fromTeamId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> competitionRequestSendListener.on(event))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("팀 정보를 찾을 수 없습니다. teamId: " + event.getFromTeamId());
    }
}
