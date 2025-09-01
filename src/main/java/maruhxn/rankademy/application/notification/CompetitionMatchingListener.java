package maruhxn.rankademy.application.notification;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.notification.required.NotificationRepository;
import maruhxn.rankademy.application.team.required.TeamRepository;
import maruhxn.rankademy.domain.notification.Notification;
import maruhxn.rankademy.domain.shared.event.CompetitionAcceptEvent;
import maruhxn.rankademy.domain.team.Team;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Component
@RequiredArgsConstructor
public class CompetitionMatchingListener {

    private final NotificationRepository notificationRepository;
    private final TeamRepository teamRepository;

    /**
     * 대항전 수락 시, 매칭 성사 알림을 양팀 모든 멤버에게 생성
     *
     * @param event
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(CompetitionAcceptEvent event) {
        Team team1 = getTeam(event.getFromTeamId());
        Team team2 = getTeam(event.getToTeamId());
        LocalDateTime now = LocalDateTime.now();

        createNotificationForAllMembers(team1, team2, now);
        createNotificationForAllMembers(team2, team1, now);
    }

    private void createNotificationForAllMembers(Team fromTeam, Team toTeam, LocalDateTime now) {
        fromTeam.getTeamMembers().forEach(member -> {
            Notification notification = Notification.create(
                    member.getUser().getId(),
                    "%s와(과)의 대항전이 성사되었습니다!".formatted(toTeam.getName()),
                    now
            );
            notificationRepository.save(notification);
        });
    }

    private Team getTeam(Long teamId) {
        return teamRepository.findByIdWithTeamMember(teamId)
                .orElseThrow(() -> new NoSuchElementException("팀 정보를 찾을 수 없습니다. teamId: " + teamId));
    }

}
