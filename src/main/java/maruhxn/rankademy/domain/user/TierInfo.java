package maruhxn.rankademy.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import maruhxn.rankademy.domain.user.dto.RiotLeagueEntryResponse;
import maruhxn.rankademy.domain.user.service.TierMapper;

@Embeddable
public record TierInfo(
        @Column(nullable = false)
        @Enumerated(EnumType.STRING)
        Tier tier,

        @Column(name = "tier_rank")
        @Enumerated(EnumType.STRING)
        Rank rank,

        @Column(nullable = false)
        int lp,

        @Column(nullable = false)
        int mappedTier
) {

    public TierInfo(Tier tier, Rank rank, int lp) {
        this(tier, rank, lp, TierMapper.tierToScore(tier, rank, lp));
    }

    static TierInfo from(RiotLeagueEntryResponse soloRankEntry) {
        Tier tier = soloRankEntry.tier().isEmpty() ? Tier.UNRANKED : Tier.valueOf(soloRankEntry.tier());
        Rank rank = soloRankEntry.rank().isEmpty() ? Rank.EMPTY : Rank.valueOf(soloRankEntry.rank());
        int lp = soloRankEntry.leaguePoints();
        return new TierInfo(tier, rank, lp);
    }
}
