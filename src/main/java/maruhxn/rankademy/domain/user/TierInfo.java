package maruhxn.rankademy.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import maruhxn.rankademy.domain.user.dto.RiotLeagueEntryResponse;
import maruhxn.rankademy.domain.user.service.TierMapper;

@Embeddable
@Getter
@NoArgsConstructor
public class TierInfo {

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Tier tier;

    @Column(name = "tier_rank")
    @Enumerated(EnumType.STRING)
    private Rank rank;

    @Column(nullable = false)
    private int lp;

    @Column(nullable = false)
    private int mappedTier;

    public TierInfo(Tier tier, Rank rank, int lp) {
        this(tier, rank, lp, TierMapper.tierToScore(tier, rank, lp));
    }

    public TierInfo(Tier tier, Rank rank, int lp, int mappedTier) {
        this.tier = tier;
        this.rank = rank;
        this.lp = lp;
        this.mappedTier = mappedTier;
    }

    static TierInfo from(RiotLeagueEntryResponse soloRankEntry) {
        Tier tier = soloRankEntry.tier().isEmpty() ? Tier.UNRANKED : Tier.valueOf(soloRankEntry.tier());
        Rank rank = soloRankEntry.rank().isEmpty() ? Rank.EMPTY : Rank.valueOf(soloRankEntry.rank());
        int lp = soloRankEntry.leaguePoints();
        return new TierInfo(tier, rank, lp);
    }

    public String getFlattenString() {
        return tier.name().toLowerCase() + rank.getInteger();
    }
}