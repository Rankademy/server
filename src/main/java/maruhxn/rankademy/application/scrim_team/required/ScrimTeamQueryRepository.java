package maruhxn.rankademy.application.scrim_team.required;

import maruhxn.rankademy.application.scrim_team.provided.dto.ScrimTeamDetailResponse;
import maruhxn.rankademy.application.scrim_team.provided.dto.ScrimTeamPageResponse;

import java.util.Optional;

public interface ScrimTeamQueryRepository {

    ScrimTeamPageResponse findAll(int page);

    Optional<ScrimTeamDetailResponse> getDetailById(Long scrimTeamId);
}
