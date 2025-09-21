package maruhxn.rankademy.application.notification.provided;

import maruhxn.rankademy.application.notification.provided.dto.NotificationPageResponse;
import maruhxn.rankademy.domain.notification.Notification;

public interface NotificationReader {

    Notification get(Long notificationId);

    NotificationPageResponse getNotifications(Long userId, int page);

}
