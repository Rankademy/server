package maruhxn.rankademy.domain.member.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CertifySummonerInfoRequest(
        @NotEmpty
        String puuid,

        @NotEmpty
        String summonerName,

        @NotEmpty
        String summonerTag,

        @NotNull
        int summonerIconNum,

        String tier,

        String rank,

        int lp,

        @NotNull
        double winRate
) {
}
