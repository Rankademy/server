package maruhxn.rankademy.application.team.dto.request;

import maruhxn.rankademy.domain.user.SummonerInfo;
import maruhxn.rankademy.domain.user.TierInfo;
import maruhxn.rankademy.domain.user.User;

public record MemberDto(
            String puuid,
            String tier,
            Double mu,
            Double sigma
) {
    public static MemberDto from(User user) {
        SummonerInfo summonerInfo = user.getSummonerInfo();
        TierInfo tierInfo = summonerInfo.getTierInfo();
        return new MemberDto(
                summonerInfo.getPuuid(),
                tierInfo.getFlattenString(),
                user.getEffectiveStrength().getMu(),
                user.getEffectiveStrength().getSigma()
        );
    }
}