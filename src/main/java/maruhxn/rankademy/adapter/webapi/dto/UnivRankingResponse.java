package maruhxn.rankademy.adapter.webapi.dto;

import maruhxn.rankademy.domain.user.TierInfo;
import maruhxn.rankademy.domain.user.service.TierMapper;

public record UnivRankingResponse(
        String univName,
        TierInfo tierInfo,
        Long winCount,
        Long totalUserCnt,
        RankerDto rankerDto
) {
    public UnivRankingResponse(String univName, Double mappedTier, Long winCount, Long totalUserCnt, RankerDto rankerDto) {
        this(
                univName,
                TierMapper.scoreToTier(mappedTier != null ? mappedTier.intValue() : 0),
                winCount,
                totalUserCnt,
                rankerDto
        );
    }
}
