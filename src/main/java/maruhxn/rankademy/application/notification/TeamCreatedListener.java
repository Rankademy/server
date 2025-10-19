package maruhxn.rankademy.application.notification;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.notification.required.NotificationRepository;
import maruhxn.rankademy.application.team.required.TeamRepository;
import maruhxn.rankademy.domain.notification.Notification;
import maruhxn.rankademy.domain.shared.event.TeamCreatedEvent;
import maruhxn.rankademy.domain.team.Team;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.NoSuchElementException;

@Component
@RequiredArgsConstructor
public class TeamCreatedListener {

    private final NotificationRepository notificationRepository;
    private final TeamRepository teamRepository;

    /**
     * 팀 생성 이벤트 발생 시 멤버들에게 알림 생성
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(TeamCreatedEvent event) {
        Team team = teamRepository.findByIdWithTeamMember(event.getTeamId())
                .orElseThrow(() -> new NoSuchElementException("팀 정보를 찾을 수 없습니다. teamId: " + event.getTeamId()));

        String message = "%s 팀 생성".formatted(team.getName());

        team.getTeamMembers().forEach(member -> {
            Notification notification = Notification.create(
                    member.getUser().getId(),
                    message,
                    event.getCreatedAt()
            );
            notificationRepository.save(notification);
        });
    }
}
