package maruhxn.rankademy.application.notification;

import maruhxn.rankademy.application.competition.required.CompetitionRepository;
import maruhxn.rankademy.application.notification.required.NotificationRepository;
import maruhxn.rankademy.application.team.required.TeamRepository;
import maruhxn.rankademy.domain.competition.Competition;
import maruhxn.rankademy.domain.notification.Notification;
import maruhxn.rankademy.domain.shared.event.CompetitionResultSubmitEvent;
import maruhxn.rankademy.domain.team.Team;
import maruhxn.rankademy.domain.team.TeamMember;
import maruhxn.rankademy.domain.user.SummonerInfo;
import maruhxn.rankademy.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
@DisplayName("애플리케이션 - 대항전 결과 제출 이벤트 리스너")
class CompetitionResultSubmitListenerTest {

    @InjectMocks
    private CompetitionResultSubmitListener competitionResultSubmitListener;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private CompetitionRepository competitionRepository;

    @Mock
    private TeamRepository teamRepository;

    @Test
    @DisplayName("대항전 결과 제출 이벤트 발생 시 양 팀 모든 팀원에게 알림을 전송한다.")
    void onResultSubmit() {
        // given
        Long actingUserId = 1L;
        Long competitionId = 10L;
        Long team1Id = 1L;
        Long team2Id = 2L;

        CompetitionResultSubmitEvent event = new CompetitionResultSubmitEvent(competitionId, actingUserId);

        Competition competition = mock(Competition.class);
        given(competition.getTeam1Id()).willReturn(team1Id);
        given(competition.getTeam2Id()).willReturn(team2Id);

        SummonerInfo summonerInfo = mock(SummonerInfo.class);
        given(summonerInfo.getSummonerName()).willReturn("actingUser");
        given(summonerInfo.getSummonerTag()).willReturn("KR1");

        User user1 = mock(User.class);
        given(user1.getId()).willReturn(actingUserId);
        given(user1.getSummonerInfo()).willReturn(summonerInfo);
        TeamMember member1 = mock(TeamMember.class);
        given(member1.getUser()).willReturn(user1);
        Team team1 = mock(Team.class);
        given(team1.getName()).willReturn("team1");
        given(team1.getTeamMembers()).willReturn(Set.of(member1));

        User user2 = mock(User.class);
        given(user2.getId()).willReturn(2L);
        TeamMember member2 = mock(TeamMember.class);
        given(member2.getUser()).willReturn(user2);
        Team team2 = mock(Team.class);
        given(team2.getName()).willReturn("team2");
        given(team2.getTeamMembers()).willReturn(Set.of(member2));

        given(competitionRepository.findById(competitionId)).willReturn(Optional.of(competition));
        given(teamRepository.findByIdWithTeamMember(team1Id)).willReturn(Optional.of(team1));
        given(teamRepository.findByIdWithTeamMember(team2Id)).willReturn(Optional.of(team2));

        // when
        competitionResultSubmitListener.on(event);

        // then
        verify(team1, times(1)).deactivate();
        verify(team2, times(1)).deactivate();

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(2)).save(captor.capture());

        List<Notification> notifications = captor.getAllValues();
        assertThat(notifications).hasSize(2);
        assertThat(notifications.get(0).getMessage()).isEqualTo("team1(등록 유저: actingUser#KR1) 팀의 대항전 결과 등록");
        assertThat(notifications.get(1).getMessage()).isEqualTo("team2(등록 유저: actingUser#KR1) 팀의 대항전 결과 등록");
    }

    @Test
    @DisplayName("대항전을 찾을 수 없으면 예외가 발생한다.")
    void onResultSubmit_whenCompetitionNotFound() {
        // given
        CompetitionResultSubmitEvent event = new CompetitionResultSubmitEvent(10L, 1L);
        given(competitionRepository.findById(10L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> competitionResultSubmitListener.on(event))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("대항전 정보를 찾을 수 없습니다. compeitionId: 10");

        verify(teamRepository, never()).findByIdWithTeamMember(any());
        verify(notificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("팀을 찾을 수 없으면 예외가 발생한다.")
    void onResultSubmit_whenTeamNotFound() {
        // given
        Competition competition = mock(Competition.class);
        given(competition.getTeam1Id()).willReturn(1L);

        CompetitionResultSubmitEvent event = new CompetitionResultSubmitEvent(10L, 1L);
        given(competitionRepository.findById(10L)).willReturn(Optional.of(competition));
        given(teamRepository.findByIdWithTeamMember(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> competitionResultSubmitListener.on(event))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("팀 정보를 찾을 수 없습니다. teamId: 1");

        verify(notificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("결과를 제출한 유저를 팀에서 찾을 수 없으면 예외가 발생한다.")
    void onResultSubmit_whenActingUserNotFound() {
        // given
        Long actingUserId = 99L; // Non-existent user
        Long competitionId = 10L;
        Long team1Id = 1L;
        Long team2Id = 2L;

        CompetitionResultSubmitEvent event = new CompetitionResultSubmitEvent(competitionId, actingUserId);

        Competition competition = mock(Competition.class);
        given(competition.getTeam1Id()).willReturn(team1Id);
        given(competition.getTeam2Id()).willReturn(team2Id);

        User user1 = mock(User.class);
        given(user1.getId()).willReturn(1L); // Different ID
        TeamMember member1 = mock(TeamMember.class);
        given(member1.getUser()).willReturn(user1);
        Team team1 = mock(Team.class);
        given(team1.getTeamMembers()).willReturn(Set.of(member1));

        Team team2 = mock(Team.class);
        given(team2.getTeamMembers()).willReturn(Set.of());

        given(competitionRepository.findById(competitionId)).willReturn(Optional.of(competition));
        given(teamRepository.findByIdWithTeamMember(team1Id)).willReturn(Optional.of(team1));
        given(teamRepository.findByIdWithTeamMember(team2Id)).willReturn(Optional.of(team2));

        // when & then
        assertThatThrownBy(() -> competitionResultSubmitListener.on(event))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("참여 멤버 중 다음의 유저를 찾을 수 없습니다. userId: 99");

        verify(notificationRepository, never()).save(any());
    }
}
