package maruhxn.rankademy.application.user.dto;

import maruhxn.rankademy.domain.user.LolPosition;
import maruhxn.rankademy.domain.user.TierInfo;

import java.util.List;

public record MyProfileResponse(
        Long id,
        String username,
        SummonerInfoResponse summonerInfo,
        UnivInfoResponse univInfo,
        String description,
        List<String> mostChampionIds,
        LolPosition mainPosition,
        LolPosition subPosition
) {

    public record UnivInfoResponse(
            String univName,
            String univMail,
            boolean univVerified,
            String major,
            int admissionYear
    ) {
    }

    public record SummonerInfoResponse(
            String puuid,
            String summonerName,
            String summonerTag,
            int summonerIconNum,
            TierInfo tierInfo,
            int winCount,
            int lossCount,
            double winRate
    ) {
        public SummonerInfoResponse(String puuid, String summonerName, String summonerTag, int summonerIconNum, TierInfo tierInfo, int winCount, int lossCount) {
            this(
                puuid,
                summonerName,
                summonerTag,
                summonerIconNum,
                tierInfo,
                winCount,
                lossCount,
                (double) winCount / Math.max(1, winCount + lossCount) * 100.0
            );
        }
    }
}
