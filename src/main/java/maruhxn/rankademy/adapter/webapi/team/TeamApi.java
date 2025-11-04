package maruhxn.rankademy.adapter.webapi.team;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.security.model.RankademyUser;
import maruhxn.rankademy.application.team.provided.TeamReader;
import maruhxn.rankademy.application.team.provided.TeamWriter;
import maruhxn.rankademy.application.team.provided.dto.MyTeamPageResponse;
import maruhxn.rankademy.application.team.provided.dto.TeamDetailResponse;
import maruhxn.rankademy.application.team.provided.dto.TeamPageResponse;
import maruhxn.rankademy.domain.team.dto.TeamCreateRequest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
@Tag(name = "Teams", description = "팀 생성 및 관리를 위한 API")
public class TeamApi {

    private final TeamReader teamReader;
    private final TeamWriter teamWriter;

    @GetMapping
    @Operation(
            summary = "팀 목록 조회",
            description = "페이지 번호에 따라 팀 목록을 페이지 단위로 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "팀 목록 조회 성공")
    public PagedModel<TeamPageResponse.TeamResponse> getTeamList(
            @Parameter(description = "0부터 시작하는 페이지 번호", example = "0")
            @RequestParam("page") int page
    ) {
        TeamPageResponse response = teamReader.getTeamList(page);
        long totalCount = response.totalCount() == null ? 0L : response.totalCount();
        Pageable pageable = PageRequest.of(page, 10);
        return new PagedModel<>(new PageImpl<>(response.teams(), pageable, totalCount));
    }

    @GetMapping("/my")
    @Operation(
            summary = "나의 팀 목록 조회",
            description = "페이지 번호에 따라 나의 팀 목록을 페이지 단위로 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "나의 팀 목록 조회 성공")
    public PagedModel<MyTeamPageResponse> getMyTeamList(
            @AuthenticationPrincipal RankademyUser user,
            @Parameter(description = "0부터 시작하는 페이지 번호", example = "0")
            @RequestParam(value = "page", defaultValue = "0") int page
    ) {
        return teamReader.getMyTeamList(user.getId(), page);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "팀 생성",
            description = "대표자와 팀원 정보를 기반으로 신규 팀을 생성합니다."
    )
    @ApiResponse(responseCode = "201", description = "팀 생성 성공")
    public void createTeam(@RequestBody TeamCreateRequest request) {
        teamWriter.create(request);
    }

    @GetMapping("/{teamId}")
    @Operation(
            summary = "팀 상세 조회",
            description = "팀 식별자를 통해 팀 상세 정보를 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "팀 상세 조회 성공")
    public TeamDetailResponse getTeamDetail(
            @AuthenticationPrincipal RankademyUser user,
            @Parameter(description = "조회할 팀 ID", example = "1")
            @PathVariable("teamId") Long teamId
    ) {
        return teamReader.getTeamDetails(user.getId(), teamId);
    }

    @DeleteMapping("/{teamId}/withdraw")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "팀 탈퇴",
            description = "요청 유저를 팀에서 탈퇴시키고 팀 상태를 비활성화합니다."
    )
    @ApiResponse(responseCode = "204", description = "팀 탈퇴 성공")
    public void withdrawTeam(
            @AuthenticationPrincipal RankademyUser user,
            @Parameter(description = "탈퇴할 팀 ID", example = "1")
            @PathVariable("teamId") Long teamId
    ) {
        teamWriter.withdraw(user.getId(), teamId);
    }
}
