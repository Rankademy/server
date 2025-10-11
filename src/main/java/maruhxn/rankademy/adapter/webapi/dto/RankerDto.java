package maruhxn.rankademy.adapter.webapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "대표 랭커 정보")
public record RankerDto(
        @Schema(description = "유저 ID", example = "10") Long id,
        @Schema(description = "소환사 이름", example = "TopRanker") String summonerName,
        @Schema(description = "소환사 아이콘 번호", example = "1234") int summonerIcon
) {
}
