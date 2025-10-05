package maruhxn.rankademy.application.scrim_team.provided.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import maruhxn.rankademy.domain.user.TierInfo;
import maruhxn.rankademy.domain.user.service.TierMapper;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "스크림 팀 페이지 응답")
public record ScrimTeamPageResponse(
        @Schema(description = "총 스크림 팀 수", example = "5") Long totalCount,
        @Schema(description = "스크림 팀 목록") List<ScrimTeamResponse> teams
) {

    @Schema(description = "스크림 팀 요약")
    public record ScrimTeamResponse(
            @Schema(description = "스크림 팀 ID", example = "1") Long scrimTeamId,
            @Schema(description = "스크림 팀 이름") String scrimTeamName,
            @Schema(description = "팀 소개") String intro,
            @Schema(description = "생성일") LocalDateTime createdAt,
            @Schema(description = "평균 티어") TierInfo avgTierInfo,
            @Schema(description = "추천 여부") boolean isRecommended
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
