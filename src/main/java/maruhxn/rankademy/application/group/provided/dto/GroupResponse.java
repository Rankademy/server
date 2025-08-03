package maruhxn.rankademy.application.group.provided.dto;

import maruhxn.rankademy.domain.user.TierInfo;
import maruhxn.rankademy.domain.user.service.TierMapper;

public record GroupResponse(
        Long groupId,
        String name,
        String logoImageUrl,
        int capacity,
        int memberCnt,
        int competitionTotalCnt,
        int competitionWinCnt,
        TierInfo avgTierInfo,
        LeaderDto leader
) {
    public GroupResponse(Long groupId, String name, String logoImageUrl, int capacity, int memberCnt, int competitionTotalCnt, int competitionWinCnt, Double avgMappedTier, LeaderDto leader) {
        this(
                groupId,
                name,
                logoImageUrl,
                capacity,
                memberCnt,
                competitionTotalCnt,
                competitionWinCnt,
                TierMapper.scoreToTier(avgMappedTier.intValue()),
                leader
        );
    }
}
