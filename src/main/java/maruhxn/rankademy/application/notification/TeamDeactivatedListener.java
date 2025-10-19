package maruhxn.rankademy.application.notification;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.notification.required.NotificationRepository;
import maruhxn.rankademy.application.team.required.TeamRepository;
import maruhxn.rankademy.domain.notification.Notification;
import maruhxn.rankademy.domain.shared.event.TeamDeactivatedEvent;
import maruhxn.rankademy.domain.team.Team;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Component
@RequiredArgsConstructor
public class TeamDeactivatedListener {

    private final NotificationRepository notificationRepository;
    private final TeamRepository teamRepository;

    /**
     * 팀 비활성화 이벤트 발생 시 남은 멤버들에게 알림 생성
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @EventListener
    public void on(TeamDeactivatedEvent event) {
        Team team = teamRepository.findByIdWithTeamMember(event.getTeamId())
                .orElseThrow(() -> new NoSuchElementException("팀 정보를 찾을 수 없습니다. teamId: " + event.getTeamId()));

        // 탈퇴자는 이미 팀 멤버에서 제외되었으므로 남은 멤버 모두에게 전송
        team.getTeamMembers().forEach(member -> {
            Notification notification = Notification.create(
                    member.getUser().getId(),
                    "%s 팀 해제".formatted(team.getName()),
                    event.getOccurredAt()
            );
            notificationRepository.save(notification);
        });
    }
}
