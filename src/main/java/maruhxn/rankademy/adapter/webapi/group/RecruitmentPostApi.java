package maruhxn.rankademy.adapter.webapi.group;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.security.model.RankademyUser;
import maruhxn.rankademy.application.group.provided.GroupReader;
import maruhxn.rankademy.application.group.provided.RecruitPostWriter;
import maruhxn.rankademy.application.group.provided.dto.RecruitmentPostDetailResponse;
import maruhxn.rankademy.application.group.provided.dto.RecruitmentPostResponse;
import maruhxn.rankademy.domain.group.dto.CreateRecruitmentPostRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
@Tag(name = "Group Recruitment Posts", description = "그룹 모집글 관리 API")
public class RecruitmentPostApi {

    private final GroupReader groupReader;
    private final RecruitPostWriter recruitPostWriter;

    @GetMapping("/posts")
    @Operation(
            summary = "모집글 목록 조회",
            description = "모집 중인 그룹의 모집글을 페이지 단위로 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "모집글 조회 성공")
    public List<RecruitmentPostResponse> getRecruitmentPostList(
            @Parameter(description = "0부터 시작하는 페이지 번호", example = "0")
            @RequestParam("page") int page
    ) {
        return groupReader.getRecruitmentPostList(page);
    }

    @GetMapping("/{groupId}/post")
    @Operation(
            summary = "모집글 상세 조회",
            description = "특정 그룹의 모집글 상세 정보를 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "모집글 상세 조회 성공")
    public RecruitmentPostDetailResponse getRecruitmentPostDetails(
            @AuthenticationPrincipal RankademyUser rankademyUser,
            @Parameter(description = "그룹 ID", example = "1")
            @PathVariable Long groupId
    ) {
        return groupReader.getRecruitmentPostDetail(rankademyUser.getId(), groupId);
    }

    @PostMapping("/{groupId}/posts")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@groupLeaderChecker.isGroupLeader(principal.userInfo(), #groupId)")
    @Operation(
            summary = "모집글 등록/수정",
            description = "그룹 리더가 모집글을 등록하거나 수정합니다."
    )
    @ApiResponse(responseCode = "201", description = "모집글 저장 성공")
    public void upsertRecruitmentPost(
            @Parameter(description = "그룹 ID", example = "1")
            @PathVariable Long groupId,
            @RequestBody CreateRecruitmentPostRequest request
    ) {
        recruitPostWriter.upsertRecruitmentPost(groupId, request);
    }

    @PatchMapping("/{groupId}/posts/up")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@groupLeaderChecker.isGroupLeader(principal.userInfo(), #groupId)")
    @Operation(
            summary = "모집글 상단 노출",
            description = "모집글의 노출 순위를 올립니다."
    )
    @ApiResponse(responseCode = "204", description = "모집글 상단 노출 성공")
    public void upRecruitmentPost(
            @Parameter(description = "그룹 ID", example = "1")
            @PathVariable Long groupId
    ) {
        recruitPostWriter.up(groupId);
    }
}
