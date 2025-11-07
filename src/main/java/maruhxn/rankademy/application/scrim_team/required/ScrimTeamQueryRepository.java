package maruhxn.rankademy.application.scrim_team.required;

import maruhxn.rankademy.application.scrim_team.provided.dto.ScrimTeamDetailResponse;
import maruhxn.rankademy.application.scrim_team.provided.dto.ScrimTeamPageResponse;
import maruhxn.rankademy.domain.scrim_team.ScrimTeam;

import java.util.List;
import java.util.Optional;

public interface ScrimTeamQueryRepository {

    ScrimTeamPageResponse findAll(int page, List<Long> excludedScrimTeamIds, int excludedCount);

    List<ScrimTeamPageResponse.ScrimTeamResponse> findRecommendedTeams(Long leaderScrimTeamId, int limit);

    Optional<ScrimTeamDetailResponse> getDetailById(Long scrimTeamId);

    Boolean existsTeamLeaderByUserId(Long userId);

    Optional<ScrimTeam> findMyLeaderTeamByUserId(Long userId);
}
