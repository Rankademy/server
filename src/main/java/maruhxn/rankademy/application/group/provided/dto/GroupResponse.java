package maruhxn.rankademy.application.group.provided.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import maruhxn.rankademy.domain.user.TierInfo;
import maruhxn.rankademy.domain.user.service.TierMapper;

@Schema(description = "그룹 요약 응답")
public record GroupResponse(
        @Schema(description = "그룹 ID", example = "1") Long groupId,
        @Schema(description = "그룹 이름") String name,
        @Schema(description = "로고 이미지 URL") String logoImageUrl,
        @Schema(description = "정원", example = "50") int capacity,
        @Schema(description = "현재 멤버 수", example = "10") int memberCnt,
        @Schema(description = "대항전 전체 횟수", example = "15") int competitionTotalCnt,
        @Schema(description = "대항전 승리 횟수", example = "9") int competitionWinCnt,
        @Schema(description = "평균 티어") TierInfo avgTierInfo,
        @Schema(description = "리더 정보") LeaderDto leader
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
