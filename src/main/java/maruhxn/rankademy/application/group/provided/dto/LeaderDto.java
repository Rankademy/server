package maruhxn.rankademy.application.group.provided.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "그룹 리더 정보")
public record LeaderDto(
        @Schema(description = "리더 ID", example = "10") Long id,
        @Schema(description = "리더 소환사 이름", example = "RankLeader") String username,
        @Schema(description = "소환사 아이콘 번호", example = "1234") int icon
) {
}
