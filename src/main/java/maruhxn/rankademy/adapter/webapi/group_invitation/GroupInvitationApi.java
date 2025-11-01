package maruhxn.rankademy.adapter.webapi.group_invitation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.security.model.RankademyUser;
import maruhxn.rankademy.application.group_invitation.dto.GroupInvitationPageResponse;
import maruhxn.rankademy.application.group_invitation.provided.GroupInvitationManager;
import maruhxn.rankademy.application.group_invitation.provided.GroupInvitationReader;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Group Invitations", description = "그룹 초대 관리 API")
public class GroupInvitationApi {
    private final GroupInvitationManager groupInvitationManager;
    private final GroupInvitationReader groupInvitationReader;

    @GetMapping("/api/v1/groups/invitation")
    @Operation(
            summary = "내 그룹 초대 목록 조회",
            description = "사용자에게 도착한 그룹 초대 목록을 페이지 단위로 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "그룹 초대 조회 성공")
    public PagedModel<GroupInvitationPageResponse.GroupInvitationResponse> getInvitations(
            @AuthenticationPrincipal RankademyUser user,
            @Parameter(description = "0부터 시작하는 페이지 번호", example = "0")
            @RequestParam("page") int page
    ) {
        GroupInvitationPageResponse response = groupInvitationReader.getInvitations(user.getId(), page);
        long totalCount = response.totalCount() == null ? 0L : response.totalCount();
        Pageable pageable = PageRequest.of(page, 20);
        return new PagedModel<>(new PageImpl<>(response.groupInvitations(), pageable, totalCount));
    }

    @PostMapping("/api/v1/groups/{groupId}/invitation/send")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@groupLeaderChecker.isGroupLeader(principal.userInfo(), #groupId)")
    @Operation(
            summary = "그룹 초대 발송",
            description = "그룹 리더가 특정 사용자에게 그룹 초대를 발송합니다."
    )
    @ApiResponse(responseCode = "201", description = "그룹 초대 발송 성공")
    public void sendCompetitionRequest(
            @Parameter(description = "그룹 ID", example = "1")
            @PathVariable("groupId") Long groupId,
            @Parameter(description = "초대할 사용자 ID", example = "10")
            @RequestParam("userId") Long invitedUserId
    ) {
        groupInvitationManager.invite(groupId, invitedUserId);
    }

    @PatchMapping("/api/v1/groups/{groupId}/invitation/accept/{invitationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@groupInviteeChecker.isInvitee(principal.userInfo(), #invitationId)")
    @Operation(
            summary = "그룹 초대 수락",
            description = "사용자가 도착한 그룹 초대를 수락합니다."
    )
    @ApiResponse(responseCode = "204", description = "그룹 초대 수락 성공")
    public void acceptInvitation(
            @AuthenticationPrincipal RankademyUser user,
            @Parameter(description = "초대 ID", example = "5")
            @PathVariable Long invitationId
    ) {
        groupInvitationManager.acceptInvitation(user.getId(), invitationId);
    }

    @PatchMapping("/api/v1/groups/{groupId}/invitation/reject/{invitationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@groupInviteeChecker.isInvitee(principal.userInfo(), #invitationId)")
    @Operation(
            summary = "그룹 초대 거절",
            description = "사용자가 그룹 초대를 거절합니다."
    )
    @ApiResponse(responseCode = "204", description = "그룹 초대 거절 성공")
    public void rejectInvitation(
            @AuthenticationPrincipal RankademyUser user,
            @Parameter(description = "초대 ID", example = "5")
            @PathVariable Long invitationId
    ) {
        groupInvitationManager.rejectInvitation(user.getId(), invitationId);
    }
}
