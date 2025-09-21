package maruhxn.rankademy.application.notification.required;

import maruhxn.rankademy.application.notification.provided.dto.NotificationPageResponse;

public interface NotificationQueryRepository {
    NotificationPageResponse getNotifications(Long userId, int page);
}
