package maruhxn.rankademy.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import maruhxn.rankademy.domain.user.dto.RiotLeagueEntryResponse;

@Embeddable
public record TierInfo(
        @Column(nullable = false)
        String tier,

        @Column(name = "tier_rank")
        String rank,

        @Column(nullable = false)
        int lp
) {
    public TierInfo {
        if (tier == null) {
            tier = "UNRANKED";
            rank = null;
            lp = 0;
        }
    }

    static TierInfo from(RiotLeagueEntryResponse soloRankEntry) {
        return new TierInfo(soloRankEntry.tier(), soloRankEntry.rank(), soloRankEntry.leaguePoints());
    }
}
