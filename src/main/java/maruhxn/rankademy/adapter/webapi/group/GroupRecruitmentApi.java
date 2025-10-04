package maruhxn.rankademy.adapter.webapi.group;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.group.provided.GroupRecruitmentManager;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupRecruitmentApi {

    private final GroupRecruitmentManager groupRecruitmentManager;

    @PostMapping("/{groupId}/recruitment")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@groupLeaderChecker.isGroupLeader(principal.userInfo(), #groupId)")
    public void startRecruitment(
            @PathVariable("groupId") Long groupId
    ) {
        groupRecruitmentManager.startRecruitment(groupId);
    }

    @DeleteMapping("/{groupId}/recruitment")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@groupLeaderChecker.isGroupLeader(principal.userInfo(), #groupId)")
    public void closeRecruitment(
            @PathVariable("groupId") Long groupId
    ) {
        groupRecruitmentManager.closeRecruitment(groupId);
    }
}
