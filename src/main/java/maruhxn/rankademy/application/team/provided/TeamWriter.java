package maruhxn.rankademy.application.team.provided;

import maruhxn.rankademy.domain.team.Team;
import maruhxn.rankademy.domain.team.dto.TeamCreateRequest;

public interface TeamWriter {

    Team create(TeamCreateRequest request);
}
