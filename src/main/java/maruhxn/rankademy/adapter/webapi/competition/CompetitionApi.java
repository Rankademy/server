package maruhxn.rankademy.adapter.webapi.competition;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.security.model.RankademyUser;
import maruhxn.rankademy.application.competition.provided.CompetitionReader;
import maruhxn.rankademy.application.competition.provided.CompetitionResultManager;
import maruhxn.rankademy.application.competition.provided.dto.CompetitionDetailResponse;
import maruhxn.rankademy.application.competition.provided.dto.CompetitionPageResponse;
import maruhxn.rankademy.application.competition.provided.dto.CompetitionResultResponse;
import maruhxn.rankademy.domain.competition.dto.OpposeResultRequest;
import maruhxn.rankademy.domain.competition.dto.SubmitCompetitionResultRequest;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/competitions")
@RequiredArgsConstructor
@Tag(name = "Competitions", description = "대항전 조회 및 결과 관리 API")
public class CompetitionApi {

    private final CompetitionReader competitionReader;
    private final CompetitionResultManager competitionResultManager;

    @GetMapping("/my")
    @Operation(
            summary = "내 대항전 목록 조회",
            description = "사용자가 참여한 대항전 목록을 페이지 단위로 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "대항전 목록 조회 성공")
    public PagedModel<CompetitionPageResponse> getMyCompetitions(
            @AuthenticationPrincipal RankademyUser user,
            @Parameter(description = "0부터 시작하는 페이지 번호", example = "0")
            @RequestParam("page") int page
    ) {
        return competitionReader.getMyCompetitionHistory(user.getId(), page);
    }

    @GetMapping("/groups/{groupId}")
    @Operation(
            summary = "그룹 대항전 목록 조회",
            description = "그룹이 참여한 대항전 이력을 페이지 단위로 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "그룹 대항전 조회 성공")
    public PagedModel<CompetitionPageResponse> getGroupCompetitions(
            @Parameter(description = "그룹 ID", example = "1")
            @PathVariable Long groupId,
            @Parameter(description = "0부터 시작하는 페이지 번호", example = "0")
            @RequestParam("page") int page
    ) {
        return competitionReader.getGroupCompetitionHistory(groupId, page);
    }

    @GetMapping("/{competitionId}")
    @Operation(
            summary = "대항전 상세 조회",
            description = "대항전 ID를 이용해 상세 정보를 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "대항전 상세 조회 성공")
    public CompetitionDetailResponse getCompetition(
            @Parameter(description = "대항전 ID", example = "1")
            @PathVariable Long competitionId
    ) {
        return competitionReader.getDetail(competitionId);
    }

    @GetMapping("/{competitionId}/result")
    @Operation(
            summary = "대항전 결과 조회",
            description = "대항전 결과를 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "대항전 결과 조회 성공")
    public CompetitionResultResponse getCompetitionResult(
            @Parameter(description = "대항전 ID", example = "1")
            @PathVariable Long competitionId
    ) {
        return competitionReader.getResult(competitionId);
    }

    @PostMapping("/{competitionId}/results")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@competitionChecker.isMyCompetition(principal.userInfo(), #competitionId)")
    @Operation(
            summary = "대항전 결과 등록",
            description = "대항전 세트 결과를 등록합니다."
    )
    @ApiResponse(responseCode = "201", description = "대항전 결과 등록 성공")
    public void submitCompetitionResult(
            @AuthenticationPrincipal RankademyUser user,
            @Parameter(description = "대항전 ID", example = "1")
            @PathVariable Long competitionId,
            @RequestBody SubmitCompetitionResultRequest request
    ) {
        competitionResultManager.submitResult(user.getId(), competitionId, request);
    }

    @PostMapping("/{competitionId}/oppose")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@competitionChecker.isMyCompetition(principal.userInfo(), #competitionId)")
    @Operation(
            summary = "대항전 결과 이의 제기",
            description = "등록된 결과에 대해 이의 신청을 생성합니다."
    )
    @ApiResponse(responseCode = "201", description = "이의 제기 등록 성공")
    public void opposeCompetitionResult(
            @Parameter(description = "대항전 ID", example = "1")
            @PathVariable Long competitionId,
            @RequestBody OpposeResultRequest request
    ) {
        competitionResultManager.opposeResult(competitionId, request);
    }

}
