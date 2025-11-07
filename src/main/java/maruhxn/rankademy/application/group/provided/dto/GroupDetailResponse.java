package maruhxn.rankademy.application.group.provided.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import maruhxn.rankademy.domain.user.TierInfo;
import maruhxn.rankademy.domain.user.service.TierMapper;

import java.time.LocalDateTime;

@Schema(description = "그룹 상세 응답")
public record GroupDetailResponse(
        @Schema(description = "그룹 ID", example = "1") Long groupId,
        @Schema(description = "그룹 이름", example = "Rankademy 그룹") String name,
        @Schema(description = "그룹 소개", example = "Rankademy 공식 그룹") String about,
        @Schema(description = "그룹 로고 URL") String logoImageUrl,
        @Schema(description = "그룹 평균 티어") TierInfo avgTierInfo,
        @Schema(description = "최근 대항전 기록") RecordInfoDto competitionInfo,
        @Schema(description = "정원", example = "50") Long capacity,
        @Schema(description = "현재 멤버 수", example = "10") Long memberCnt,
        @Schema(description = "그룹 리더 정보") LeaderDto leader,
        @Schema(description = "생성일") LocalDateTime createdAt,
        @Schema(description = "요청자가 그룹에 가입되어 있는지 여부") boolean isJoined,
        @Schema(description = "요청자가 그룹 리더인지 여부") boolean isLeader,
        @Schema(description = "그룹 모집 중 여부") boolean isRecruiting,
        @Schema(description = "학교 이름") String univName
) {
    public GroupDetailResponse(Long groupId, String name, String about, String logoImageUrl, Double mappedTier, RecordInfoDto competitionInfo, Long capacity, Long memberCnt, LeaderDto leader, LocalDateTime createdAt, boolean isJoined, boolean isLeader, boolean isRecruiting, String univName) {
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
                isJoined,
                isLeader,
                isRecruiting,
                univName
        );
    }
}
