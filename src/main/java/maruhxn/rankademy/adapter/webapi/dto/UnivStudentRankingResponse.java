package maruhxn.rankademy.adapter.webapi.dto;

import maruhxn.rankademy.domain.user.LolPosition;
import maruhxn.rankademy.domain.user.TierInfo;

import java.util.List;

public record UnivStudentRankingResponse(
        Long userId,
        String puuid,
        String summonerName,
        String summonerTag,
        int summonerIcon,
        TierInfo tierInfo,
        double winRate,
        List<String> topMosts,
        LolPosition mainPosition,
        LolPosition subPosition,
        int admissionYear,
        String major
) {
}
