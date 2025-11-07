package maruhxn.rankademy.application.team.required;

import maruhxn.rankademy.application.team.provided.dto.MyTeamPageResponse;
import maruhxn.rankademy.application.team.provided.dto.TeamDetailResponse;
import maruhxn.rankademy.application.team.provided.dto.TeamPageResponse;
import maruhxn.rankademy.domain.team.Team;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface TeamQueryRepository {

    TeamPageResponse findAll(int page, List<Long> excludedTeamIds, int excludedCount);

    List<TeamPageResponse.TeamResponse> findRecommendedTeams(Long leaderTeamId, int limit);

    Optional<TeamDetailResponse> getDetailById(Long userId, Long teamId);

    Page<MyTeamPageResponse> findMyTeamList(Long userId, int page);

    Boolean existsTeamLeaderByUserId(Long userId);

    Optional<Team> findMyLeaderTeamByUserId(Long userId);
}
