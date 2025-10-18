package maruhxn.rankademy.application.team.provided.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import maruhxn.rankademy.domain.user.LolPosition;
import maruhxn.rankademy.domain.user.TierInfo;
import maruhxn.rankademy.domain.user.service.TierMapper;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "팀 상세 응답")
public record TeamDetailResponse(
        @Schema(description = "팀 ID", example = "1") Long teamId,
        @Schema(description = "팀 이름", example = "Rankademy") String teamName,
        @Schema(description = "팀이 속한 대학교", example = "서울과학기술대학교") String univName,
        @Schema(description = "소속 그룹 이름", example = "Rankademy 그룹") String groupName,
        @Schema(description = "그룹 로고 이미지 URL") String groupLogo,
        @Schema(description = "팀 소개", example = "랭크 전문 팀") String intro,
        @Schema(description = "팀 생성일", example = "2025-01-01T12:00:00") LocalDateTime createdAt,
        @Schema(description = "팀 활성 여부") boolean isActive,
        @Schema(description = "팀 평균 티어 정보") TierInfo avgTierInfo,
        @Schema(description = "팀 멤버 목록") List<TeamMemberResponse> teamMembers,
        @Schema(description = "요청자가 팀 리더인지 여부") boolean isTeamLeader,
        @Schema(description = "요청자가 팀 구성원인지 여부") boolean isMyTeam
) {

    public TeamDetailResponse(Long teamId, String teamName, String univName, String groupName, String groupLogo, String intro, LocalDateTime createdAt, boolean isActive, Double avgMappedTier, List<TeamMemberResponse> teamMembers, boolean isTeamLeader, boolean isMyTeam) {
        this(
                teamId,
                teamName,
                univName,
                groupName,
                groupLogo,
                intro,
                createdAt,
                isActive,
                TierMapper.scoreToTier(avgMappedTier.intValue()),
                teamMembers,
                isTeamLeader,
                isMyTeam
        );
    }

    @Schema(description = "팀 멤버 요약 정보")
    public record TeamMemberResponse(
            @Schema(description = "팀 멤버 ID", example = "10") Long memberId,
            @Schema(description = "포지션", implementation = LolPosition.class) LolPosition position,
            @Schema(description = "소환사 이름", example = "Ranker") String summonerName,
            @Schema(description = "소환사 태그", example = "KR1") String summonerTag,
            @Schema(description = "소환사 아이콘 번호", example = "1234") int summonerIcon,
            @Schema(description = "소속 대학교", example = "서울과학기술대학교") String univName,
            @Schema(description = "학과", example = "컴퓨터공학과") String major,
            @Schema(description = "학번", example = "21") int admissionYear,
            @Schema(description = "멤버 티어 정보") TierInfo tierInfo
    ) {
    }
}
