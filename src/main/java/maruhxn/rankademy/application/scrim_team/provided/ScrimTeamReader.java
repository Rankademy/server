package maruhxn.rankademy.application.scrim_team.provided;

import maruhxn.rankademy.application.scrim_team.provided.dto.ScrimTeamDetailResponse;
import maruhxn.rankademy.application.scrim_team.provided.dto.ScrimTeamPageResponse;
import maruhxn.rankademy.domain.scrim_team.ScrimTeam;

public interface ScrimTeamReader {

    ScrimTeam get(Long scrimTeamId);

    ScrimTeamPageResponse getScrimTeamList(int page);

    ScrimTeamDetailResponse getDetails(Long scrimTeamId);
}
