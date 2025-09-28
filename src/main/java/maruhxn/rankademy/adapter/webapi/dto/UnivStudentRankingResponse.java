package maruhxn.rankademy.adapter.webapi.dto;

import maruhxn.rankademy.domain.user.LolPosition;
import maruhxn.rankademy.domain.user.TierInfo;

public record UnivStudentRankingResponse(
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
        LolPosition subPosition,
        int admissionYear,
        String major
) {
}
