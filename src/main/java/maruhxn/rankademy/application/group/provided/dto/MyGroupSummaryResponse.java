package maruhxn.rankademy.application.group.provided.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 그룹 요약 정보")
public record MyGroupSummaryResponse(
        @Schema(description = "그룹 ID", example = "1") Long groupId,
        @Schema(description = "그룹 이름", example = "Rankademy 그룹") String groupName
) {
}
