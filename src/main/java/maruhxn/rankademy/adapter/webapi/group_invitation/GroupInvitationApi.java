package maruhxn.rankademy.adapter.webapi.group_invitation;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.security.model.RankademyUser;
import maruhxn.rankademy.application.group_invitation.dto.GroupInvitationPageResponse;
import maruhxn.rankademy.application.group_invitation.provided.GroupInvitationManager;
import maruhxn.rankademy.application.group_invitation.provided.GroupInvitationReader;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/invitation")
@RequiredArgsConstructor
public class GroupInvitationApi {
    private final GroupInvitationManager groupInvitationManager;
    private final GroupInvitationReader groupInvitationReader;

    @GetMapping
    public GroupInvitationPageResponse getInvitations(
            @AuthenticationPrincipal RankademyUser user,
            @RequestParam("page") int page
    ) {
        return groupInvitationReader.getInvitations(user.getId(), page);
    }

    @PostMapping("/send")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@groupLeaderChecker.isGroupLeader(principal.userInfo(), #groupId)")
    public void sendCompetitionRequest(
            @PathVariable("groupId") Long groupId,
            @RequestParam("userId") Long invitedUserId
    ) {
        groupInvitationManager.invite(groupId, invitedUserId);
    }

    @PatchMapping("/accept/{invitationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@groupInviteeChecker.isInvitee(principal.userInfo(), #invitationId)")
    public void acceptInvitation(
            @AuthenticationPrincipal RankademyUser user,
            @PathVariable Long invitationId
    ) {
        groupInvitationManager.acceptInvitation(user.getId(), invitationId);
    }

    @PatchMapping("/reject/{invitationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@groupInviteeChecker.isInvitee(principal.userInfo(), #invitationId)")
    public void rejectInvitation(
            @AuthenticationPrincipal RankademyUser user,
            @PathVariable Long invitationId
    ) {
        groupInvitationManager.rejectInvitation(user.getId(), invitationId);
    }
}
