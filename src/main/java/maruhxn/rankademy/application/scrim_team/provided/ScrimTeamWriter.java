package maruhxn.rankademy.application.scrim_team.provided;

import maruhxn.rankademy.domain.scrim_team.ScrimTeam;
import maruhxn.rankademy.domain.scrim_team.dto.ScrimTeamCreateRequest;
import maruhxn.rankademy.domain.scrim_team.dto.ScrimTeamUpdateRequest;

public interface ScrimTeamWriter {
    ScrimTeam create(Long userId, ScrimTeamCreateRequest createRequest);

    ScrimTeam update(Long scrimTeamId, ScrimTeamUpdateRequest updateRequest);

    void delete(Long scrimTeamId);
}
