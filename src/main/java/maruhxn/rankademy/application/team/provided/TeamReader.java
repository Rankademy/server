package maruhxn.rankademy.application.team.provided;

import maruhxn.rankademy.application.team.provided.dto.TeamDetailResponse;
import maruhxn.rankademy.application.team.provided.dto.TeamPageResponse;
import maruhxn.rankademy.domain.team.Team;

public interface TeamReader {

    Team get(Long teamId);

    TeamPageResponse getTeamList(int page);

    TeamDetailResponse getTeamDetails(Long teamId);
}
