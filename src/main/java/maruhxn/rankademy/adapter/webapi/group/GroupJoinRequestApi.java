package maruhxn.rankademy.adapter.webapi.group;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.security.model.RankademyUser;
import maruhxn.rankademy.application.group.provided.GroupJoinRequestManager;
import maruhxn.rankademy.application.group.provided.GroupReader;
import maruhxn.rankademy.application.group.provided.dto.JoinRequestResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/join-requests")
@RequiredArgsConstructor
@Tag(name = "Group Join Requests", description = "그룹 가입 요청 관리 API")
public class GroupJoinRequestApi {

    private final GroupReader groupReader;
    private final GroupJoinRequestManager groupJoinRequestManager;

    @GetMapping
    @PreAuthorize("@groupLeaderChecker.isGroupLeader(principal.userInfo(), #groupId)")
    @Operation(
            summary = "그룹 가입 요청 목록 조회",
            description = "그룹 리더가 받은 가입 요청 목록을 페이지 단위로 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "가입 요청 조회 성공")
    public List<JoinRequestResponse> getJoinRequests(
            @Parameter(description = "그룹 ID", example = "1")
            @PathVariable Long groupId,
            @Parameter(description = "0부터 시작하는 페이지 번호", example = "0")
            @RequestParam(value = "page", defaultValue = "0") int page
    ) {
        return groupReader.getJoinRequests(groupId, page);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("principal.userInfo().authorized")
    @Operation(
            summary = "그룹 가입 요청 생성",
            description = "해당 그룹에 가입을 요청합니다."
    )
    @ApiResponse(responseCode = "201", description = "가입 요청 생성 성공")
    public void sendJoinRequest(
            @AuthenticationPrincipal RankademyUser rankademyUser,
            @Parameter(description = "가입을 요청할 그룹 ID", example = "1")
            @PathVariable Long groupId
    ) {
        groupJoinRequestManager.sendJoinRequest(rankademyUser.getId(), groupId);
    }

    @PatchMapping("/{requestorId}/accept")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@groupLeaderChecker.isGroupLeader(principal.userInfo(), #groupId)")
    @Operation(
            summary = "가입 요청 수락",
            description = "그룹 리더가 가입 요청을 수락합니다."
    )
    @ApiResponse(responseCode = "204", description = "가입 요청 수락 성공")
    public void acceptJoinRequest(
            @Parameter(description = "그룹 ID", example = "1")
            @PathVariable Long groupId,
            @Parameter(description = "가입을 요청한 사용자 ID", example = "10")
            @PathVariable Long requestorId
    ) {
        groupJoinRequestManager.acceptJoinRequest(requestorId, groupId);
    }

    @PatchMapping("/{requestorId}/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@groupLeaderChecker.isGroupLeader(principal.userInfo(), #groupId)")
    @Operation(
            summary = "가입 요청 거절",
            description = "그룹 리더가 가입 요청을 거절합니다."
    )
    @ApiResponse(responseCode = "204", description = "가입 요청 거절 성공")
    public void rejectJoinRequest(
            @Parameter(description = "그룹 ID", example = "1")
            @PathVariable Long groupId,
            @Parameter(description = "가입 요청자 ID", example = "10")
            @PathVariable Long requestorId
    ) {
        groupJoinRequestManager.rejectJoinRequest(requestorId, groupId);
    }
}
