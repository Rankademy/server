package maruhxn.rankademy.adapter.webapi.competition;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Competition Requests", description = "대항전 요청 관리 API")
public class CompetitionRequestApi {

    private final CompetitionRequestReader competitionRequestReader;
    private final CompetitionRequestManager competitionRequestManager;

    @PostMapping("/send")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@teamChecker.isTeamLeader(principal.userInfo(), #myTeamId)")
    @Operation(
            summary = "대항전 요청 발송",
            description = "내 팀이 다른 팀에게 대항전을 요청합니다."
    )
    @ApiResponse(responseCode = "201", description = "대항전 요청 발송 성공")
    public void sendCompetitionRequest(
            @Parameter(description = "요청을 보내는 팀 ID", example = "1")
            @PathVariable("teamId") Long myTeamId,
            @AuthenticationPrincipal RankademyUser user,
            @Parameter(description = "대항전을 요청할 상대 팀 ID", example = "2")
            @RequestParam("otherTeamId") Long otherTeamId
    ) {
        competitionRequestManager.sendRequest(user.getId(), myTeamId, otherTeamId);
    }

    @GetMapping
    @PreAuthorize("@teamChecker.isTeamLeader(principal.userInfo(), #teamId)")
    @Operation(
            summary = "대항전 요청 목록 조회",
            description = "팀에 도착한 대항전 요청을 페이지 단위로 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "대항전 요청 조회 성공")
    public CompetitionRequestPageResponse getCompetitionRequests(
            @Parameter(description = "팀 ID", example = "1")
            @PathVariable Long teamId,
            @Parameter(description = "0부터 시작하는 페이지 번호", example = "0")
            @RequestParam("page") int page
    ) {
        return competitionRequestReader.getRequests(teamId, page);
    }

    @PatchMapping("/accept/{requestId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@teamChecker.isTeamLeader(principal.userInfo(), #teamId)")
    @Operation(
            summary = "대항전 요청 수락",
            description = "대항전 요청을 수락하여 대항전을 생성합니다."
    )
    @ApiResponse(responseCode = "204", description = "대항전 요청 수락 성공")
    public void acceptCompetitionRequest(
            @Parameter(description = "내 팀 ID", example = "1")
            @PathVariable Long teamId,
            @AuthenticationPrincipal RankademyUser user,
            @Parameter(description = "요청 ID", example = "10")
            @PathVariable Long requestId
    ) {
        competitionRequestManager.acceptRequest(user.getId(), requestId);
    }

    @PatchMapping("/reject/{requestId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@teamChecker.isTeamLeader(principal.userInfo(), #teamId)")
    @Operation(
            summary = "대항전 요청 거절",
            description = "도착한 대항전 요청을 거절합니다."
    )
    @ApiResponse(responseCode = "204", description = "대항전 요청 거절 성공")
    public void rejectCompetitionRequest(
            @Parameter(description = "내 팀 ID", example = "1")
            @PathVariable Long teamId,
            @AuthenticationPrincipal RankademyUser user,
            @Parameter(description = "요청 ID", example = "10")
            @PathVariable Long requestId
    ) {
        competitionRequestManager.rejectRequest(user.getId(), requestId);
    }
}
