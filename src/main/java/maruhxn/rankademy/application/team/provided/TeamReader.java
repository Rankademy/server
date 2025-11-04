package maruhxn.rankademy.application.team.provided;

import maruhxn.rankademy.application.team.provided.dto.MyTeamPageResponse;
import maruhxn.rankademy.application.team.provided.dto.TeamDetailResponse;
import maruhxn.rankademy.application.team.provided.dto.TeamPageResponse;
import maruhxn.rankademy.domain.team.Team;
import org.springframework.data.web.PagedModel;

public interface TeamReader {

    Team get(Long teamId);

    TeamPageResponse getTeamList(int page);

    PagedModel<MyTeamPageResponse> getMyTeamList(Long userId, int page);

    TeamDetailResponse getTeamDetails(Long userId, Long teamId);
}
