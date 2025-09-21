package maruhxn.rankademy.adapter.webapi;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.security.model.RankademyUser;
import maruhxn.rankademy.application.competitionrequest.provided.CompetitionRequestManager;
import maruhxn.rankademy.application.competitionrequest.provided.CompetitionRequestReader;
import maruhxn.rankademy.application.competitionrequest.required.dto.CompetitionRequestPageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/competition-requests/{teamId}")
@RequiredArgsConstructor
public class CompetitionRequestApi {

    private final CompetitionRequestReader competitionRequestReader;
    private final CompetitionRequestManager competitionRequestManager;

    @PostMapping("/send")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@teamChecker.isTeamLeader(principal.userInfo(), #myTeamId)")
    public void sendCompetitionRequest(
            @PathVariable("teamId") Long myTeamId,
            @AuthenticationPrincipal RankademyUser user,
            @RequestParam("otherTeamId") Long otherTeamId
    ) {
        competitionRequestManager.sendRequest(user.getId(), myTeamId, otherTeamId);
    }

    @GetMapping
    @PreAuthorize("@teamChecker.isTeamLeader(principal.userInfo(), #teamId)")
    public CompetitionRequestPageResponse getCompetitionRequests(
            @PathVariable Long teamId,
            @RequestParam("page") int page
    ) {
        return competitionRequestReader.getRequests(teamId, page);
    }

    @PatchMapping("/accept/{requestId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@teamChecker.isTeamLeader(principal.userInfo(), #teamId)")
    public void acceptCompetitionRequest(
            @PathVariable Long teamId,
            @AuthenticationPrincipal RankademyUser user,
            @PathVariable Long requestId
    ) {
        competitionRequestManager.acceptRequest(user.getId(), requestId);
    }

    @PatchMapping("/reject/{requestId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@teamChecker.isTeamLeader(principal.userInfo(), #teamId)")
    public void rejectCompetitionRequest(
            @PathVariable Long teamId,
            @AuthenticationPrincipal RankademyUser user,
            @PathVariable Long requestId
    ) {
        competitionRequestManager.rejectRequest(user.getId(), requestId);
    }
}
