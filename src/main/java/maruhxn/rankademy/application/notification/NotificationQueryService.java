package maruhxn.rankademy.application.notification;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.notification.provided.NotificationReader;
import maruhxn.rankademy.application.notification.provided.dto.NotificationPageResponse;
import maruhxn.rankademy.application.notification.required.NotificationQueryRepository;
import maruhxn.rankademy.application.notification.required.NotificationRepository;
import maruhxn.rankademy.domain.notification.Notification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationQueryService implements NotificationReader {

    private final NotificationRepository notificationRepository;
    private final NotificationQueryRepository notificationQueryRepository;

    @Override
    public Notification get(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NoSuchElementException("알림 정보를 찾을 수 없습니다. notificationId: " + notificationId));
    }

    @Override
    public NotificationPageResponse getNotifications(Long userId, int page) {
        return notificationQueryRepository.getNotifications(userId, page);
    }
}
