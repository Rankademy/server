package maruhxn.rankademy.application.group.provided.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "그룹 리더 정보")
public record LeaderDto(
        @Schema(description = "리더 ID", example = "10") Long id,
        @Schema(description = "소환사 이름", example = "소환사명") String summonerName,
        @Schema(description = "소환사 태그", example = "1234") String summonerTag,
        @Schema(description = "소환사 아이콘 번호", example = "123") int summonerIcon
) {
}
