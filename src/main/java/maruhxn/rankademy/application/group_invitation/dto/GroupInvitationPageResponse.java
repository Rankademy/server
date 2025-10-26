package maruhxn.rankademy.application.group_invitation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "그룹 초대 페이지 응답")
public record GroupInvitationPageResponse(
        @Schema(description = "총 초대 수", example = "3") Long totalCount,
        @Schema(description = "초대 목록") List<GroupInvitationResponse> groupInvitations
) {

    @Schema(description = "그룹 초대 정보")
    public record GroupInvitationResponse(
            @Schema(description = "초대 ID", example = "5") Long invitationId,
            @Schema(description = "그룹 ID", example = "1") Long groupId,
            @Schema(description = "그룹 이름") String groupName,
            @Schema(description = "초대 대상 사용자 ID", example = "100") Long userId,
            @Schema(description = "초대한 시간") LocalDateTime invitedAt) {
    }
}
