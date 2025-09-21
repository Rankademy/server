package maruhxn.rankademy.application.scrim_team.provided.dto;

import maruhxn.rankademy.domain.user.LolPosition;
import maruhxn.rankademy.domain.user.TierInfo;
import maruhxn.rankademy.domain.user.service.TierMapper;

import java.time.LocalDateTime;
import java.util.List;

public record ScrimTeamDetailResponse(
        Long scrimTeamId,
        String scrimTeamName,
        String intro,
        LocalDateTime createdAt,
        boolean isActive,
        TierInfo avgTierInfo,
        List<ScrimTeamMemberResponse> scrimTeamMembers
) {
    public ScrimTeamDetailResponse(Long scrimTeamId, String scrimTeamName, String intro, LocalDateTime createdAt, boolean isActive, Double avgMappedTier, List<ScrimTeamMemberResponse> members) {
        this(
                scrimTeamId,
                scrimTeamName,
                intro,
                createdAt,
                isActive,
                TierMapper.scoreToTier(avgMappedTier.intValue()),
                members
        );
    }

    public record ScrimTeamMemberResponse(
            Long memberId,
            LolPosition position,
            String summonerName,
            String summonerTag,
            int summonerIcon,
            String univName,
            TierInfo tierInfo
    ) {
    }
}
