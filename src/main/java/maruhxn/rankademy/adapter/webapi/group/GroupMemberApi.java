package maruhxn.rankademy.adapter.webapi.group;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.group.provided.GroupMemberManager;
import maruhxn.rankademy.application.group.provided.GroupReader;
import maruhxn.rankademy.application.group.provided.dto.GroupMemberResponse;
import maruhxn.rankademy.application.group.provided.dto.SearchGroupMemberResponse;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/members")
@RequiredArgsConstructor
@Tag(name = "Group Member APIs", description = "그룹 멤버 관련 API")
public class GroupMemberApi {

    private final GroupReader groupReader;
    private final GroupMemberManager groupMemberManager;

    @GetMapping
    @Operation(
            summary = "그룹 멤버 목록 조회",
            description = "그룹 멤버 목록을 페이지 단위로 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "그룹 멤버 조회 성공")
    public PagedModel<GroupMemberResponse> getGroupMembers(
            @Parameter(description = "대상 그룹 ID", example = "1")
            @PathVariable Long groupId,
            @Parameter(description = "0부터 시작하는 페이지 번호", example = "0")
            @RequestParam(value = "page", defaultValue = "0") int page
    ) {
        return groupReader.getGroupMembers(groupId, page);
    }

    @GetMapping("/search")
    @Operation(
            summary = "그룹 멤버 검색",
            description = "그룹 멤버를 memberKey로 검색합니다. (4개씩 반환)"
    )
    @ApiResponse(responseCode = "200", description = "그룹 멤버 검색 성공")
    public List<SearchGroupMemberResponse> searchGroupMembers(
            @Parameter(description = "대상 그룹 ID", example = "1")
            @PathVariable Long groupId,
            @Parameter(description = "그룹 멤버 소환사명 키", example = "니카")
            @RequestParam(value = "memberNameKey") String memberNameKey
    ) {
        return groupReader.searchGroupMembers(groupId, memberNameKey);
    }

    @GetMapping("/manage")
    @PreAuthorize("@groupLeaderChecker.isGroupLeader(principal.userInfo(), #groupId)")
    @Operation(
            summary = "그룹 멤버 목록(그룹장 제외) 조회",
            description = "그룹 멤버 목록(그룹장 제외)을 페이지 단위로 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "그룹 멤버(그룹장 제외) 조회 성공")
    public void getGroupMembersWithoutLeader(
            @Parameter(description = "대상 그룹 ID", example = "1")
            @PathVariable Long groupId,
            @Parameter(description = "0부터 시작하는 페이지 번호", example = "0")
            @RequestParam(value = "page", defaultValue = "0") int page
    ) {
        groupReader.getGroupMembersWithoutLeader(groupId, page);
    }

    @DeleteMapping("/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(
            @PathVariable("groupId") Long groupId,
            @PathVariable("memberId") Long memberId
    ) {
        groupMemberManager.removeMember(groupId, memberId);
    }
}
