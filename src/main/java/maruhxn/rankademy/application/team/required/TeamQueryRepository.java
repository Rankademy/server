package maruhxn.rankademy.application.team.required;

import maruhxn.rankademy.application.team.provided.dto.MyTeamPageResponse;
import maruhxn.rankademy.application.team.provided.dto.TeamDetailResponse;
import maruhxn.rankademy.application.team.provided.dto.TeamPageResponse;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface TeamQueryRepository {

    TeamPageResponse findAll(int page);

    Optional<TeamDetailResponse> getDetailById(Long userId, Long teamId);

    Page<MyTeamPageResponse> findMyTeamList(Long userId, int page);
}
