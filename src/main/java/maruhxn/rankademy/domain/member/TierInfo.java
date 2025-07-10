package maruhxn.rankademy.domain.member;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

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
}
