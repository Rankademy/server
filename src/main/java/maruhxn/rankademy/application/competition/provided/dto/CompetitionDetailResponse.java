package maruhxn.rankademy.application.competition.provided.dto;

import maruhxn.rankademy.application.team.provided.dto.TeamDetailResponse;
import maruhxn.rankademy.domain.competition.CompetitionStatus;

public record CompetitionDetailResponse(
        Long competitionId,
        CompetitionStatus status,
        TeamDetailResponse team1,
        TeamDetailResponse team2
) {

}
