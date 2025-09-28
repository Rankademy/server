package maruhxn.rankademy.adapter.webapi.dto;

import maruhxn.rankademy.domain.user.LolPosition;
import maruhxn.rankademy.domain.user.TierInfo;

public record TotalUserRankingResponse(
        Long userId,
        String puuid,
        String summonerName,
        String summonerTag,
        int summonerIcon,
        TierInfo tierInfo,
        double winRate,
        int winCount,
        int lossCount,
        LolPosition mainPosition,
        LolPosition subPosition
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
