package maruhxn.rankademy.application.group.provided.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "그룹 모집글 상세 응답")
public record RecruitmentPostDetailResponse(
        @Schema(description = "모집글 ID", example = "1") Long postId,
        @Schema(description = "그룹 ID", example = "1") Long groupId,
        @Schema(description = "그룹 이름") String groupName,
        @Schema(description = "모집글 제목") String title,
        @Schema(description = "모집글 내용") String content,
        @Schema(description = "요구 사항") String requirements,
        @Schema(description = "작성일") LocalDateTime createdAt,
        @Schema(description = "요청자가 그룹에 가입되어 있는지 여부") boolean isJoined
) {
}
