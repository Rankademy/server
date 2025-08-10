package maruhxn.rankademy.adapter.webapi;

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
public class GroupJoinRequestApi {

    private final GroupReader groupReader;
    private final GroupJoinRequestManager groupJoinRequestManager;

    @GetMapping
    @PreAuthorize("@groupLeaderChecker.isGroupLeader(principal.userInfo(), #groupId)")
    public List<JoinRequestResponse> getJoinRequests(
            @PathVariable Long groupId,
            @RequestParam(value = "page", defaultValue = "0") int page
    ) {
        return groupReader.getJoinRequests(groupId, page);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("principal.userInfo().authorized")
    public void sendJoinRequest(
            @AuthenticationPrincipal RankademyUser rankademyUser,
            @PathVariable Long groupId
    ) {
        groupJoinRequestManager.sendJoinRequest(rankademyUser.getId(), groupId);
    }

    @PatchMapping("/{requestorId}/accept")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@groupLeaderChecker.isGroupLeader(principal.userInfo(), #groupId)")
    public void acceptJoinRequest(
            @PathVariable Long groupId,
            @PathVariable Long requestorId
    ) {
        groupJoinRequestManager.acceptJoinRequest(requestorId, groupId);
    }

    @PatchMapping("/{requestorId}/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@groupLeaderChecker.isGroupLeader(principal.userInfo(), #groupId)")
    public void rejectJoinRequest(
            @PathVariable Long groupId,
            @PathVariable Long requestorId
    ) {
        groupJoinRequestManager.rejectJoinRequest(requestorId, groupId);
    }
}
