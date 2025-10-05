package maruhxn.rankademy.application.team.required;

import maruhxn.rankademy.application.team.provided.dto.TeamDetailResponse;
import maruhxn.rankademy.application.team.provided.dto.TeamPageResponse;

import java.util.Optional;

public interface TeamQueryRepository {

    TeamPageResponse findAll(int page);

    Optional<TeamDetailResponse> getDetailById(Long userId, Long teamId);
}
