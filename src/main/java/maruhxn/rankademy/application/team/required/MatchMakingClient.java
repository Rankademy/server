package maruhxn.rankademy.application.team.required;

import maruhxn.rankademy.application.team.dto.request.AdjustMmrRequest;
import maruhxn.rankademy.application.team.dto.request.GetTeamMmrRequest;
import maruhxn.rankademy.application.team.dto.response.AdjustedMmrResponse;
import maruhxn.rankademy.application.team.dto.response.TeamMmrResponse;

public interface MatchMakingClient {
    /**
     * 팀 평균 무력 조회
     */
    TeamMmrResponse getTeamMmr(GetTeamMmrRequest request);

    /**
     * 경기 결과 반영
     */
    AdjustedMmrResponse adjustUsersMmr(AdjustMmrRequest request);
}
