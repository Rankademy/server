package maruhxn.rankademy.application.scrim_team.provided.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import maruhxn.rankademy.domain.user.LolPosition;
import maruhxn.rankademy.domain.user.TierInfo;
import maruhxn.rankademy.domain.user.service.TierMapper;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "스크림 팀 상세 응답")
public record ScrimTeamDetailResponse(
        @Schema(description = "스크림 팀 ID", example = "1") Long scrimTeamId,
        @Schema(description = "스크림 팀 이름") String scrimTeamName,
        @Schema(description = "팀 소개") String intro,
        @Schema(description = "생성일") LocalDateTime createdAt,
        @Schema(description = "활성 여부") boolean isActive,
        @Schema(description = "평균 티어") TierInfo avgTierInfo,
        @Schema(description = "팀 멤버 목록") List<ScrimTeamMemberResponse> scrimTeamMembers
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

    @Schema(description = "스크림 팀 멤버 정보")
    public record ScrimTeamMemberResponse(
            @Schema(description = "멤버 ID", example = "10") Long memberId,
            @Schema(description = "포지션", implementation = LolPosition.class) LolPosition position,
            @Schema(description = "소환사 이름") String summonerName,
            @Schema(description = "소환사 태그") String summonerTag,
            @Schema(description = "소환사 아이콘 번호") int summonerIcon,
            @Schema(description = "대학교 이름") String univName,
            @Schema(description = "티어 정보") TierInfo tierInfo
    ) {
    }
}
