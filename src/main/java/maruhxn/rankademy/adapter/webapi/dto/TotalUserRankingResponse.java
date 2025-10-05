package maruhxn.rankademy.adapter.webapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import maruhxn.rankademy.domain.user.LolPosition;
import maruhxn.rankademy.domain.user.TierInfo;

@Schema(description = "전체 유저 랭킹 응답")
public record TotalUserRankingResponse(
        @Schema(description = "사용자 ID", example = "10") Long userId,
        @Schema(description = "소환사 PUUID") String puuid,
        @Schema(description = "소환사 이름") String summonerName,
        @Schema(description = "소환사 태그") String summonerTag,
        @Schema(description = "소환사 아이콘 번호") int summonerIcon,
        @Schema(description = "티어 정보") TierInfo tierInfo,
        @Schema(description = "승률(%)", example = "66.7") double winRate,
        @Schema(description = "승리 횟수", example = "20") int winCount,
        @Schema(description = "패배 횟수", example = "10") int lossCount,
        @Schema(description = "주 포지션", implementation = LolPosition.class) LolPosition mainPosition,
        @Schema(description = "부 포지션", implementation = LolPosition.class) LolPosition subPosition
) {
    public TotalUserRankingResponse(
            Long userId,
            String puuid,
            String summonerName,
            String summonerTag,
            int summonerIcon,
            TierInfo tierInfo,
            int winCount,
            int lossCount,
            LolPosition mainPosition,
            LolPosition subPosition
    ) {
        this(
                userId,
                puuid,
                summonerName,
                summonerTag,
                summonerIcon,
                tierInfo,
                (double) winCount / (winCount + lossCount) * 100,
                winCount,
                lossCount,
                mainPosition,
                subPosition
        );
    }
}
