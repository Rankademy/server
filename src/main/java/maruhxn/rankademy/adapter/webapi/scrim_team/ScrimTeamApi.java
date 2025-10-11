package maruhxn.rankademy.adapter.webapi.scrim_team;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Scrim Teams", description = "스크림 팀 관리 API")
public class ScrimTeamApi {

    private final ScrimTeamReader scrimTeamReader;
    private final ScrimTeamWriter scrimTeamWriter;

    @GetMapping
    @Operation(
            summary = "스크림 팀 목록 조회",
            description = "스크림 팀 목록을 페이지 단위로 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "스크림 팀 조회 성공")
    public ScrimTeamPageResponse getScrimTeamList(
            @Parameter(description = "0부터 시작하는 페이지 번호", example = "0")
            @RequestParam("page") int page
    ) {
        return scrimTeamReader.getScrimTeamList(page);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "스크림 팀 생성",
            description = "스크림 팀 정보를 등록하여 팀을 생성합니다."
    )
    @ApiResponse(responseCode = "201", description = "스크림 팀 생성 성공")
    public void createScrimTeam(@RequestBody ScrimTeamCreateRequest request) {
        scrimTeamWriter.create(request);
    }

    @GetMapping("/{scrimTeamId}")
    @Operation(
            summary = "스크림 팀 상세 조회",
            description = "스크림 팀 ID를 통해 상세 정보를 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "스크림 팀 상세 조회 성공")
    public ScrimTeamDetailResponse getDetails(
            @Parameter(description = "스크림 팀 ID", example = "1")
            @PathVariable("scrimTeamId") Long scrimTeamId
    ) {
        return scrimTeamReader.getDetails(scrimTeamId);
    }

    @PatchMapping("/{scrimTeamId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@scrimTeamChecker.isScrimTeamLeader(principal.userInfo(), #scrimTeamId)")
    @Operation(
            summary = "스크림 팀 수정",
            description = "스크림 팀 정보를 수정합니다."
    )
    @ApiResponse(responseCode = "204", description = "스크림 팀 수정 성공")
    public void updateScrimTeam(
            @Parameter(description = "스크림 팀 ID", example = "1")
            @PathVariable("scrimTeamId") Long scrimTeamId,
            @RequestBody ScrimTeamUpdateRequest request
    ) {
        scrimTeamWriter.update(scrimTeamId, request);
    }

    @DeleteMapping("/{scrimTeamId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@scrimTeamChecker.isScrimTeamLeader(principal.userInfo(), #scrimTeamId)")
    @Operation(
            summary = "스크림 팀 삭제",
            description = "스크림 팀을 삭제합니다."
    )
    @ApiResponse(responseCode = "204", description = "스크림 팀 삭제 성공")
    public void deleteScrimTeam(
            @Parameter(description = "스크림 팀 ID", example = "1")
            @PathVariable("scrimTeamId") Long scrimTeamId
    ) {
        scrimTeamWriter.delete(scrimTeamId);
    }
}
