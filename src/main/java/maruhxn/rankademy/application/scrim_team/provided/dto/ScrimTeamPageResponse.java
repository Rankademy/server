package maruhxn.rankademy.application.scrim_team.provided.dto;

import maruhxn.rankademy.domain.user.TierInfo;
import maruhxn.rankademy.domain.user.service.TierMapper;

import java.time.LocalDateTime;
import java.util.List;

public record ScrimTeamPageResponse(
        Long totalCount,
        List<ScrimTeamResponse> teams
) {

    public record ScrimTeamResponse(
            Long scrimTeamId,
            String scrimTeamName,
            String intro,
            LocalDateTime createdAt,
            TierInfo avgTierInfo,
            boolean isRecommended
    ) {
        public ScrimTeamResponse(Long scrimTeamId, String scrimTeamName, String intro, LocalDateTime createdAt, Double avgMappedTier, boolean isRecommended) {
            this(
                    scrimTeamId,
                    scrimTeamName,
                    intro,
                    createdAt,
                    TierMapper.scoreToTier(avgMappedTier.intValue()),
                    isRecommended
            );
        }
    }
}
