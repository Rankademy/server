package maruhxn.rankademy.application.notification;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.notification.provided.NotificationReader;
import maruhxn.rankademy.application.notification.provided.dto.NotificationPageResponse;
import maruhxn.rankademy.application.notification.required.NotificationQueryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationQueryService implements NotificationReader {

    private final NotificationQueryRepository notificationQueryRepository;

    @Override
    public NotificationPageResponse getNotifications(Long userId, int page) {
        return notificationQueryRepository.getNotifications(userId, page);
    }
}
