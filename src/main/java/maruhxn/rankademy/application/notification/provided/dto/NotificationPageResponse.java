package maruhxn.rankademy.application.notification.provided.dto;

import java.time.LocalDateTime;
import java.util.List;

public record NotificationPageResponse(
        Long totalCount,
        List<NotificationResponse> notifications
) {

    public record NotificationResponse(
            Long notificationId,
            String message,
            boolean isConfirmed,
            LocalDateTime deliveredAt
    ) {
    }
}
