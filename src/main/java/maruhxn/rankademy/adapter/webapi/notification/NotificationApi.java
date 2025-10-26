package maruhxn.rankademy.adapter.webapi.notification;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.security.model.RankademyUser;
import maruhxn.rankademy.application.notification.provided.NotificationModifier;
import maruhxn.rankademy.application.notification.provided.NotificationReader;
import maruhxn.rankademy.application.notification.provided.dto.NotificationPageResponse;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "알림 조회 및 상태 변경 API")
public class NotificationApi {

    private final NotificationReader notificationReader;
    private final NotificationModifier notificationModifier;

    @GetMapping
    @Operation(
            summary = "알림 목록 조회",
            description = "사용자의 알림 목록을 페이지 단위로 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "알림 조회 성공")
    public PagedModel<NotificationPageResponse.NotificationResponse> getNotifications(
            @AuthenticationPrincipal RankademyUser user,
            @Parameter(description = "0부터 시작하는 페이지 번호", example = "0")
            @RequestParam("page") int page
    ) {
        NotificationPageResponse response = notificationReader.getNotifications(user.getId(), page);
        long totalCount = response.totalCount() == null ? 0L : response.totalCount();
        Pageable pageable = PageRequest.of(page, 30);
        return new PagedModel<>(new PageImpl<>(response.notifications(), pageable, totalCount));
    }

    @PatchMapping("/{notificationId}/confirm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@notificationOwnerChecker.isNotificationOwner(principal.userInfo(), #notificationId)")
    @Operation(
            summary = "알림 확인",
            description = "알림을 확인 처리하여 더 이상 미확인으로 표시되지 않도록 합니다."
    )
    @ApiResponse(responseCode = "204", description = "알림 확인 성공")
    public void confirmNotification(
            @Parameter(description = "확인할 알림 ID", example = "1")
            @PathVariable Long notificationId
    ) {
        notificationModifier.confirm(notificationId);
    }
}
