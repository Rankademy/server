package maruhxn.rankademy.application.group.provided.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "최근 대항전 요약")
public record RecentCompetitionResponse(
        @Schema(description = "대항전 상대 그룹 ID", example = "1") Long groupId,
        @Schema(description = "대항전 상대 그룹 이름") String groupName,
        @Schema(description = "승리 여부") boolean isWin
) {
}
