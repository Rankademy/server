package maruhxn.rankademy.application.user.dto;

import lombok.Builder;
import maruhxn.rankademy.domain.user.LolPosition;
import maruhxn.rankademy.domain.user.TierInfo;

import java.util.List;

@Builder
public record ProfileResponse(
        Long id,
        SummonerInfoResponse summonerInfo,
        UnivInfoResponse univInfo,
        String description,
        List<String> mostChampionIds,
        LolPosition mainPosition,
        LolPosition subPosition
) {
    public record UnivInfoResponse(String univName, boolean univVerified, String major, int admissionYear) {
    }

    public record SummonerInfoResponse(
            String summonerName,
            String summonerTag,
            int summonerIcon,
            TierInfo tierInfo,
            int winCount,
            int lossCount,
            double winRate
    ) {
        public SummonerInfoResponse(String summonerName, String summonerTag, int summonerIcon, TierInfo tierInfo, int winCount, int lossCount) {
            this(
                    summonerName,
                    summonerTag,
                    summonerIcon,
                    tierInfo,
                    winCount,
                    lossCount,
                    (double) winCount / Math.max(1, winCount + lossCount) * 100.0
            );
        }
    }
}
