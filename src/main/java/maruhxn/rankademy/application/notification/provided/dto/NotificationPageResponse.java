package maruhxn.rankademy.application.notification.provided.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "알림 목록 응답")
public record NotificationPageResponse(
        @Schema(description = "총 알림 수", example = "10") Long totalCount,
        @Schema(description = "알림 목록") List<NotificationResponse> notifications
) {

    @Schema(description = "알림 정보")
    public record NotificationResponse(
            @Schema(description = "알림 ID", example = "15") Long notificationId,
            @Schema(description = "알림 메시지", example = "팀 Rankademy이 생성되었습니다.") String message,
            @Schema(description = "확인 여부") boolean isConfirmed,
            @Schema(description = "전달 시간") LocalDateTime deliveredAt
    ) {
    }
}
