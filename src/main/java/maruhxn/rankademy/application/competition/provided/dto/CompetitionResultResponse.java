package maruhxn.rankademy.application.competition.provided.dto;

import maruhxn.rankademy.domain.user.LolPosition;

import java.util.List;

public record CompetitionResultResponse(
        Long competitionId,
        TeamInfoResponse team1,
        TeamInfoResponse team2,
        List<SetResultResponse> setResults,
        Long finalWinnerTeamId
) {
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

    public record SetResultResponse(
            int setNumber,
            Long winnerTeamId
    ) {
    }
}
