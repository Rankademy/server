package maruhxn.rankademy.application.competition.provided.dto;

import maruhxn.rankademy.domain.user.LolPosition;

import java.util.List;

public record TeamInfoResponse(
        Long teamId,
        String teamName,
        String groupName,
        List<TeamMemberResponse> teamMembers
) {

    public record TeamMemberResponse(
            Long memberId,
            LolPosition position,
            String summonerName,
            String summonerTag
    ) {
    }
}
