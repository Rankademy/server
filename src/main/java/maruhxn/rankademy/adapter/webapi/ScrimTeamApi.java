package maruhxn.rankademy.adapter.webapi;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.scrim_team.provided.ScrimTeamReader;
import maruhxn.rankademy.application.scrim_team.provided.ScrimTeamWriter;
import maruhxn.rankademy.application.scrim_team.provided.dto.ScrimTeamDetailResponse;
import maruhxn.rankademy.application.scrim_team.provided.dto.ScrimTeamPageResponse;
import maruhxn.rankademy.domain.scrim_team.dto.ScrimTeamCreateRequest;
import maruhxn.rankademy.domain.scrim_team.dto.ScrimTeamUpdateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/scrim-teams")
@RequiredArgsConstructor
public class ScrimTeamApi {

    private final ScrimTeamReader scrimTeamReader;
    private final ScrimTeamWriter scrimTeamWriter;

    @GetMapping
    public ScrimTeamPageResponse getScrimTeamList(@RequestParam("page") int page) {
        return scrimTeamReader.getScrimTeamList(page);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createScrimTeam(@RequestBody ScrimTeamCreateRequest request) {
        scrimTeamWriter.create(request);
    }

    @GetMapping("/{scrimTeamId}")
    public ScrimTeamDetailResponse getDetails(@PathVariable("scrimTeamId") Long scrimTeamId) {
        return scrimTeamReader.getDetails(scrimTeamId);
    }

    @PatchMapping("/{scrimTeamId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@scrimTeamChecker.isScrimTeamLeader(principal.userInfo(), #scrimTeamId)")
    public void updateScrimTeam(
            @PathVariable("scrimTeamId") Long scrimTeamId,
            @RequestBody ScrimTeamUpdateRequest request
    ) {
        scrimTeamWriter.update(scrimTeamId, request);
    }

    @DeleteMapping("/{scrimTeamId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@scrimTeamChecker.isScrimTeamLeader(principal.userInfo(), #scrimTeamId)")
    public void deleteScrimTeam(@PathVariable("scrimTeamId") Long scrimTeamId) {
        scrimTeamWriter.delete(scrimTeamId);
    }
}
