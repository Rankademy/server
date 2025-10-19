package maruhxn.rankademy.application.notification;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.group.required.GroupRepository;
import maruhxn.rankademy.application.notification.required.NotificationRepository;
import maruhxn.rankademy.domain.group.Group;
import maruhxn.rankademy.domain.notification.Notification;
import maruhxn.rankademy.domain.shared.event.SendGroupJoinRequestEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.NoSuchElementException;

@Component
@RequiredArgsConstructor
public class GroupJoinRequestSendListener {

    private final NotificationRepository notificationRepository;
    private final GroupRepository groupRepository;

    /**
     * 그룹 가입 요청 전송 시, 해당 그룹의 그룹장에게 알림 전송
     * @param event
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(SendGroupJoinRequestEvent event) {
        Group group = groupRepository.findByIdWithLeader(event.getGroupId())
                .orElseThrow(() -> new NoSuchElementException("그룹 정보를 찾을 수 없습니다. groupId: " + event.getGroupId()));

        Notification notification = Notification.create(
                group.getLeader().getId(),
                String.format("%s 님의 그룹 가입요청", event.getFullSummonerName()),
                event.getOccurredAt()
        );

        notificationRepository.save(notification);
    }
}
