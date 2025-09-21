package maruhxn.rankademy.adapter.webapi;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.security.model.RankademyUser;
import maruhxn.rankademy.application.competition.provided.CompetitionReader;
import maruhxn.rankademy.application.competition.provided.CompetitionResultManager;
import maruhxn.rankademy.application.competition.provided.dto.CompetitionDetailResponse;
import maruhxn.rankademy.application.competition.provided.dto.CompetitionPageResponse;
import maruhxn.rankademy.application.competition.provided.dto.CompetitionResultResponse;
import maruhxn.rankademy.domain.competition.dto.OpposeResultRequest;
import maruhxn.rankademy.domain.competition.dto.SubmitCompetitionResultRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/competitions")
@RequiredArgsConstructor
public class CompetitionApi {

    private final CompetitionReader competitionReader;
    private final CompetitionResultManager competitionResultManager;

    @GetMapping("/my")
    public CompetitionPageResponse getMyCompetitions(
            @AuthenticationPrincipal RankademyUser user,
            @RequestParam("page") int page
    ) {
        return competitionReader.getMyCompetitionHistory(user.getId(), page);
    }

    @GetMapping("/groups/{groupId}")
    public CompetitionPageResponse getMyCompetitions(
            @PathVariable Long groupId,
            @RequestParam("page") int page
    ) {
        return competitionReader.getGroupCompetitionHistory(groupId, page);
    }

    @GetMapping("/{competitionId}")
    public CompetitionDetailResponse getCompetition(@PathVariable Long competitionId) {
        return competitionReader.getDetail(competitionId);
    }

    @GetMapping("/{competitionId}/result")
    public CompetitionResultResponse getCompetitionResult(@PathVariable Long competitionId) {
        return competitionReader.getResult(competitionId);
    }

    @PostMapping("/{competitionId}/results")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@competitionChecker.isMyCompetition(principal.userInfo(), #competitionId)")
    public void submitCompetitionResult(
            @AuthenticationPrincipal RankademyUser user,
            @PathVariable Long competitionId,
            @RequestBody SubmitCompetitionResultRequest request
    ) {
        competitionResultManager.submitResult(user.getId(), competitionId, request);
    }

    @PostMapping("/{competitionId}/oppose")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@competitionChecker.isMyCompetition(principal.userInfo(), #competitionId)")
    public void opposeCompetitionResult(
            @PathVariable Long competitionId,
            @RequestBody OpposeResultRequest request
    ) {
        competitionResultManager.opposeResult(competitionId, request);
    }

}
