package maruhxn.rankademy.application.team.provided.dto;

import maruhxn.rankademy.domain.user.TierInfo;
import maruhxn.rankademy.domain.user.service.TierMapper;

import java.time.LocalDateTime;
import java.util.List;

public record TeamPageResponse(
        Long totalCount,
        List<TeamResponse> teams
) {

    public record TeamResponse(
            Long teamId,
            String teamName,
            String univName,
            String groupName,
            String intro,
            LocalDateTime createdAt,
            TierInfo avgTierInfo,
            boolean isRecommended
    ) {
        public TeamResponse(Long teamId, String teamName, String univName, String groupName, String intro, LocalDateTime createdAt, Double avgMappedTier, boolean isRecommended) {
            this(
                    teamId,
                    teamName,
                    univName,
                    groupName,
                    intro,
                    createdAt,
                    TierMapper.scoreToTier(avgMappedTier.intValue()),
                    isRecommended
            );
        }
    }
}
