package maruhxn.rankademy.application.team.provided.dto;

import maruhxn.rankademy.domain.user.LolPosition;
import maruhxn.rankademy.domain.user.TierInfo;
import maruhxn.rankademy.domain.user.service.TierMapper;

import java.time.LocalDateTime;
import java.util.List;

public record TeamDetailResponse(
        Long teamId,
        String teamName,
        String univName,
        String groupName,
        String groupLogo,
        String intro,
        LocalDateTime createdAt,
        boolean isActive,
        TierInfo avgTierInfo,
        List<TeamMemberResponse> teamMembers,
        boolean isTeamLeader,
        boolean isMyTeam
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

    public record TeamMemberResponse(
            Long memberId,
            LolPosition position,
            String summonerName,
            String summonerTag,
            int summonerIcon,
            String univName,
            int admissionYear,
            TierInfo tierInfo
    ) {
    }
}
