package maruhxn.rankademy.adapter.webapi;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.security.model.RankademyUser;
import maruhxn.rankademy.application.notification.provided.NotificationModifier;
import maruhxn.rankademy.application.notification.provided.NotificationReader;
import maruhxn.rankademy.application.notification.provided.dto.NotificationPageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationApi {

    private final NotificationReader notificationReader;
    private final NotificationModifier notificationModifier;

    @GetMapping
    public NotificationPageResponse getNotifications(
            @AuthenticationPrincipal RankademyUser user,
            @RequestParam("page") int page
    ) {
        return notificationReader.getNotifications(user.getId(), page);
    }

    @PatchMapping("/{notificationId}/confirm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@notificationOwnerChecker.isNotificationOwner(principal.userInfo(), #notificationId)")
    public void confirmNotification(
            @PathVariable Long notificationId
    ) {
        notificationModifier.confirm(notificationId);
    }
}
