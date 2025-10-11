package maruhxn.rankademy.domain.user.service;


import maruhxn.rankademy.domain.user.Rank;
import maruhxn.rankademy.domain.user.Tier;
import maruhxn.rankademy.domain.user.TierInfo;

import java.util.Arrays;
import java.util.List;

import static maruhxn.rankademy.domain.user.Rank.IV;
import static maruhxn.rankademy.domain.user.Tier.*;

public class TierMapper {
    /**
     * @param tier: String ex) "GOLD"
     * @param rank: String ex) "II"
     * @param lp:   int ex) 75
     * @return int score
     */
    public static int tierToScore(Tier tier, Rank rank, int lp) {
        int baseScore = tier.getScore();
        int offset = rank.getScore();

        return baseScore + offset + lp;
    }

    public static TierInfo scoreToTier(int mappedTier) {
        // 1) 점수 정수화
        int score = (int) Math.floor(mappedTier);

        // 2) MASTER 이상은 모두 MASTER
        if (score >= MASTER.getScore()) {
            return new TierInfo(MASTER, null, score - MASTER.getScore(), mappedTier);
        }

        // 3) 나머지 티어 중에서 가장 높은 baseScore 이하인 티어 선택
        Tier selectedTier = Arrays.stream(Tier.values())
                // MASTER 계열 제외
                .filter(t -> !List.of(MASTER, GRANDMASTER, CHALLENGER).contains(t))
                // baseScore 내림차순
                .sorted((e1, e2) -> e2.getScore() - e1.getScore())
                // score >= baseScore 인 티어만 남김
                .filter(t -> score >= t.getScore())
                .findFirst()
                .orElse(IRON);

        int base = selectedTier.getScore();
        int remain = score - base;

        // 4) 해당 티어 내에서 offset(IV→100, III→200, II→300, I→400) 중 가장 큰 값을 골라 랭크·LP 계산
        Rank selectedRank = Arrays.stream(Rank.values())
                // offset 내림차순
                .sorted((e1, e2) -> e2.getScore() - e1.getScore())
                .filter(r -> remain >= r.getScore())
                .findFirst()
                .orElse(IV);

        int offset = selectedRank.getScore();
        int lp = remain - offset;

        return new TierInfo(selectedTier, selectedRank, lp);
    }
}
