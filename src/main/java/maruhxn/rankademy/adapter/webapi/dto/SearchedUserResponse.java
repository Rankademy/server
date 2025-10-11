package maruhxn.rankademy.adapter.webapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "소환사 검색 응답")
public record SearchedUserResponse(
        @Schema(description = "소환사명") String summonerName,
        @Schema(description = "소환사 태그") String summonerTag,
        @Schema(description = "소환사 아이콘 번호") int summonerIcon
) {
}
