package maruhxn.rankademy.adapter.webapi;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.security.model.RankademyUser;
import maruhxn.rankademy.application.team.provided.TeamReader;
import maruhxn.rankademy.application.team.provided.TeamWriter;
import maruhxn.rankademy.application.team.provided.dto.TeamDetailResponse;
import maruhxn.rankademy.application.team.provided.dto.TeamPageResponse;
import maruhxn.rankademy.domain.team.dto.TeamCreateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamApi {

    private final TeamReader teamReader;
    private final TeamWriter teamWriter;

    @GetMapping
    public TeamPageResponse getTeamList(@RequestParam("page") int page) {
        return teamReader.getTeamList(page);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createTeam(@RequestBody TeamCreateRequest request) {
        teamWriter.create(request);
    }

    @GetMapping("/{teamId}")
    public TeamDetailResponse getTeamDetail(
            @AuthenticationPrincipal RankademyUser user,
            @PathVariable("teamId") Long teamId
    ) {
        return teamReader.getTeamDetails(user.getId(), teamId);
    }

    @DeleteMapping("/{teamId}/withdraw")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdrawTeam(
            @AuthenticationPrincipal RankademyUser user,
            @PathVariable("teamId") Long teamId
    ) {
        teamWriter.withdraw(user.getId(), teamId);
    }
}
