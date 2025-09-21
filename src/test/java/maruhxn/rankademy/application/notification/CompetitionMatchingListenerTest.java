package maruhxn.rankademy.application.notification;

import maruhxn.rankademy.application.notification.required.NotificationRepository;
import maruhxn.rankademy.application.team.required.TeamRepository;
import maruhxn.rankademy.domain.notification.Notification;
import maruhxn.rankademy.domain.shared.event.CompetitionAcceptEvent;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("애플리케이션 - 대항전 매칭 이벤트 리스너")
class CompetitionMatchingListenerTest {

    @InjectMocks
    private CompetitionMatchingListener competitionMatchingListener;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private TeamRepository teamRepository;

    @Test
    @DisplayName("대항전 매칭 이벤트 발생 시 양 팀 모든 팀원에게 알림을 전송한다.")
    void onCompetitionAccept() {
        // given
        // Team 1
        User user1 = mock(User.class);
        given(user1.getId()).willReturn(1L);
        TeamMember member1 = mock(TeamMember.class);
        given(member1.getUser()).willReturn(user1);
        Team team1 = mock(Team.class);
        given(team1.getName()).willReturn("TeamA");
        given(team1.getTeamMembers()).willReturn(Set.of(member1));

        // Team 2
        User user2 = mock(User.class);
        given(user2.getId()).willReturn(2L);
        TeamMember member2 = mock(TeamMember.class);
        given(member2.getUser()).willReturn(user2);
        Team team2 = mock(Team.class);
        given(team2.getName()).willReturn("TeamB");
        given(team2.getTeamMembers()).willReturn(Set.of(member2));

        CompetitionAcceptEvent event = new CompetitionAcceptEvent(1L, 2L, LocalDateTime.now());

        given(teamRepository.findByIdWithTeamMember(1L)).willReturn(Optional.of(team1));
        given(teamRepository.findByIdWithTeamMember(2L)).willReturn(Optional.of(team2));

        // when
        competitionMatchingListener.on(event);

        // then
        verify(teamRepository, times(1)).findByIdWithTeamMember(1L);
        verify(teamRepository, times(1)).findByIdWithTeamMember(2L);

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(2)).save(notificationCaptor.capture());

        List<Notification> notifications = notificationCaptor.getAllValues();
        assertThat(notifications).hasSize(2);

        // Notification for Team 1 member
        Notification notification1 = notifications.stream().filter(n -> n.getUserId().equals(1L)).findFirst().orElseThrow();
        assertThat(notification1.getMessage()).isEqualTo("TeamB와(과)의 대항전이 성사되었습니다!");

        // Notification for Team 2 member
        Notification notification2 = notifications.stream().filter(n -> n.getUserId().equals(2L)).findFirst().orElseThrow();
        assertThat(notification2.getMessage()).isEqualTo("TeamA와(과)의 대항전이 성사되었습니다!");
    }

    @Test
    @DisplayName("대항전 매칭 이벤트 발생 시 첫 번째 팀을 찾을 수 없으면 예외가 발생한다.")
    void onCompetitionAccept_whenFromTeamNotFound() {
        // given
        CompetitionAcceptEvent event = new CompetitionAcceptEvent(1L, 2L, LocalDateTime.now());
        given(teamRepository.findByIdWithTeamMember(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> competitionMatchingListener.on(event))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("팀 정보를 찾을 수 없습니다. teamId: 1");

        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    @DisplayName("대항전 매칭 이벤트 발생 시 두 번째 팀을 찾을 수 없으면 예외가 발생한다.")
    void onCompetitionAccept_whenToTeamNotFound() {
        // given
        Team team1 = mock(Team.class);
        CompetitionAcceptEvent event = new CompetitionAcceptEvent(1L, 2L, LocalDateTime.now());
        given(teamRepository.findByIdWithTeamMember(1L)).willReturn(Optional.of(team1));
        given(teamRepository.findByIdWithTeamMember(2L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> competitionMatchingListener.on(event))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("팀 정보를 찾을 수 없습니다. teamId: 2");

        verify(notificationRepository, never()).save(any(Notification.class));
    }
}
