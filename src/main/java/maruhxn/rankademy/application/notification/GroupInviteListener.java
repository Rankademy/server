package maruhxn.rankademy.application.notification;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.group.required.GroupRepository;
import maruhxn.rankademy.application.notification.required.NotificationRepository;
import maruhxn.rankademy.domain.group.Group;
import maruhxn.rankademy.domain.notification.Notification;
import maruhxn.rankademy.domain.shared.event.GroupInviteEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Component
@RequiredArgsConstructor
public class GroupInviteListener {

    private final NotificationRepository notificationRepository;
    private final GroupRepository groupRepository;

    /**
     * 그룹 초대 요청 시, 상대팀에게 알림 전송
     *
     * @param event
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(GroupInviteEvent event) {
        Group group = groupRepository.findById(event.getGroupId())
                .orElseThrow(() -> new NoSuchElementException("그룹 정보를 찾을 수 없습니다. groupId: " + event.getGroupId()));
        LocalDateTime now = LocalDateTime.now();

        Notification notification = Notification.create(
                event.getInvitedUserId(),
                "%s 그룹의 초대".formatted(group.getName()),
                now
        );
        notificationRepository.save(notification);
    }
}
