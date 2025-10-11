package maruhxn.rankademy.application.team.provided.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import maruhxn.rankademy.domain.user.TierInfo;
import maruhxn.rankademy.domain.user.service.TierMapper;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "팀 목록 페이지 응답")
public record TeamPageResponse(
        @Schema(description = "총 팀 수", example = "15") Long totalCount,
        @Schema(description = "팀 목록") List<TeamResponse> teams
) {

    @Schema(description = "팀 요약 정보")
    public record TeamResponse(
            @Schema(description = "팀 ID", example = "1") Long teamId,
            @Schema(description = "팀 이름", example = "Rankademy") String teamName,
            @Schema(description = "팀 소속 대학교") String univName,
            @Schema(description = "팀 소속 그룹명") String groupName,
            @Schema(description = "팀 소개") String intro,
            @Schema(description = "팀 생성일") LocalDateTime createdAt,
            @Schema(description = "팀 평균 티어") TierInfo avgTierInfo,
            @Schema(description = "추천 여부") boolean isRecommended
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
