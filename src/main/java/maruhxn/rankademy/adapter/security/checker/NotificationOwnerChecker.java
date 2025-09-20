package maruhxn.rankademy.adapter.security.checker;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.security.model.UserInfo;
import maruhxn.rankademy.application.notification.provided.NotificationReader;
import maruhxn.rankademy.domain.notification.Notification;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationOwnerChecker {

    private final NotificationReader notificationReader;

    public boolean isNotificationOwner(UserInfo userInfo, Long notificationId) {
        Notification notification = notificationReader.get(notificationId);

        return notification.getUserId().equals(userInfo.id());
    }
}
