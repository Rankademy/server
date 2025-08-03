package maruhxn.rankademy.application.group.provided.dto;

import maruhxn.rankademy.domain.user.TierInfo;
import maruhxn.rankademy.domain.user.service.TierMapper;

import java.time.LocalDateTime;

public record GroupDetailResponse(
        Long groupId,
        String name,
        String about,
        String logoImageUrl,
        TierInfo avgTierInfo,
        RecordInfoDto competitionInfo,
        Long capacity,
        Long memberCnt,
        LeaderDto leader,
        LocalDateTime createdAt,
        boolean isJoined
) {
    public GroupDetailResponse(Long groupId, String name, String about, String logoImageUrl, Double mappedTier, RecordInfoDto competitionInfo, Long capacity, Long memberCnt, LeaderDto leader, LocalDateTime createdAt, boolean isJoined) {
        this(
                groupId,
                name,
                about,
                logoImageUrl,
                TierMapper.scoreToTier(mappedTier.intValue()),
                competitionInfo,
                capacity,
                memberCnt,
                leader,
                createdAt,
                isJoined)
        ;
    }
}
