package maruhxn.rankademy.adapter.webapi.group;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.security.model.RankademyUser;
import maruhxn.rankademy.application.group.provided.GroupMemberManager;
import maruhxn.rankademy.application.group.provided.GroupReader;
import maruhxn.rankademy.application.group.provided.GroupWriter;
import maruhxn.rankademy.application.group.provided.dto.GroupDetailResponse;
import maruhxn.rankademy.application.group.provided.dto.MyGroupResponse;
import maruhxn.rankademy.application.group.provided.dto.MyGroupSummaryResponse;
import maruhxn.rankademy.application.group.provided.dto.RecentCompetitionResponse;
import maruhxn.rankademy.domain.group.Group;
import maruhxn.rankademy.domain.group.dto.GroupCreateRequest;
import maruhxn.rankademy.domain.group.dto.GroupUpdateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
@Tag(name = "Groups", description = "그룹 생성 및 조회 API")
public class GroupApi {

    private final GroupReader groupReader;
    private final GroupWriter groupWriter;
    private final GroupMemberManager groupMemberManager;

    @GetMapping("/my")
    @PreAuthorize("principal.userInfo().authorized")
    @Operation(
            summary = "내 그룹 목록 조회",
            description = "로그인한 사용자가 속한 그룹 목록을 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "그룹 목록 조회 성공")
    public List<MyGroupResponse> getMyGroups(
            @AuthenticationPrincipal RankademyUser rankademyUser
    ) {
        return groupReader.getMyGroupList(rankademyUser.getId());
    }

    @GetMapping("/my/summary")
    @PreAuthorize("principal.userInfo().authorized")
    @Operation(
            summary = "내 그룹 목록 조회 (요약)",
            description = "로그인한 사용자가 속한 그룹 목록을 조회합니다. (팀 생성 시 사용)"
    )
    @ApiResponse(responseCode = "200", description = "그룹 목록 조회 성공")
    public List<MyGroupSummaryResponse> getMyGroupSummaries(
            @AuthenticationPrincipal RankademyUser rankademyUser
    ) {
        return groupReader.getMyGroupSummaryList(rankademyUser.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("principal.userInfo().authorized")
    @Operation(
            summary = "그룹 생성",
            description = "사용자가 입력한 기본 정보를 바탕으로 신규 그룹을 생성합니다."
    )
    @ApiResponse(responseCode = "201", description = "그룹 생성 성공")
    public Long createGroup(
            @AuthenticationPrincipal RankademyUser rankademyUser,
            @RequestBody @Valid GroupCreateRequest request
    ) {
        Group group = groupWriter.create(rankademyUser.getId(), request);
        return group.getId();
    }

    @GetMapping("/{groupId}")
    @Operation(
            summary = "그룹 상세 조회",
            description = "그룹 ID를 이용해 그룹 상세 정보를 조회합니다. 로그인하지 않은 사용자는 null로 전달됩니다."
    )
    @ApiResponse(responseCode = "200", description = "그룹 상세 조회 성공")
    public GroupDetailResponse getGroupDetail(
            @AuthenticationPrincipal RankademyUser rankademyUser,
            @Parameter(description = "조회할 그룹 ID", example = "1")
            @PathVariable("groupId") Long groupId
    ) {
        return groupReader.getDetail(rankademyUser != null ? rankademyUser.getId() : null, groupId);
    }

    @GetMapping("/{groupId}/recent-competitions")
    @Operation(
            summary = "그룹 최근 대항전 조회",
            description = "그룹이 참여한 최근 대항전 기록을 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "최근 대항전 조회 성공")
    public List<RecentCompetitionResponse> getRecentCompetitions(
            @Parameter(description = "대상 그룹 ID", example = "1")
            @PathVariable Long groupId
    ) {
        return groupReader.getRecentCompetitions(groupId);
    }

    @PutMapping("/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@groupLeaderChecker.isGroupLeader(principal.userInfo(), #groupId)")
    @Operation(
        summary = "그룹 정보 수정",
        description = "그룹 리더가 그룹의 기본 정보를 수정합니다."
    )
    @ApiResponse(responseCode = "204", description = "그룹 수정 성공")
    public void updateGroup(
            @Parameter(description = "수정할 그룹 ID", example = "1")
            @PathVariable("groupId") Long groupId,
            @RequestBody @Valid GroupUpdateRequest request
    ) {
        groupWriter.update(groupId, request);
    }

    @DeleteMapping("/{groupId}/withdraw")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@groupMemberChecker.isGroupMember(principal.userInfo(), #groupId)")
    public void withdrawGroup(
            @Parameter(description = "탈퇴할 그룹 ID", example = "1")
            @PathVariable("groupId") Long groupId,

            @AuthenticationPrincipal RankademyUser rankademyUser
    ) {
        groupMemberManager.removeMember(groupId, rankademyUser.getId());
    }

    @DeleteMapping("/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@groupLeaderChecker.isGroupLeader(principal.userInfo(), #groupId)")
    @Operation(
            summary = "그룹 삭제",
            description = "그룹 리더가 그룹을 삭제합니다."
    )
    @ApiResponse(responseCode = "204", description = "그룹 삭제 성공")
    public void deleteGroup(
            @Parameter(description = "삭제할 그룹 ID", example = "1")
            @PathVariable("groupId") Long groupId
    ) {
        groupWriter.delete(groupId);
    }
}
