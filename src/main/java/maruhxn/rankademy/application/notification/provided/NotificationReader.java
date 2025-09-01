package maruhxn.rankademy.application.notification.provided;

import maruhxn.rankademy.application.notification.provided.dto.NotificationPageResponse;

public interface NotificationReader {

    NotificationPageResponse getNotifications(Long userId, int page);

}
