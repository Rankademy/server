package maruhxn.rankademy.adapter.webapi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.security.model.RankademyUser;
import maruhxn.rankademy.application.group.provided.GroupReader;
import maruhxn.rankademy.application.group.provided.GroupWriter;
import maruhxn.rankademy.application.group.provided.dto.*;
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
public class GroupApi {

    private final GroupReader groupReader;
    private final GroupWriter groupWriter;

    @GetMapping
    public List<GroupResponse> getGroupRankingList(
            @RequestParam("page") int page,
            @RequestParam("keyword") String keyword,
            @RequestParam("sortKey") GroupSortKey sortKey
    ) {
        return groupReader.getRankingList(page, keyword, sortKey);
    }

    @GetMapping("/my")
    @PreAuthorize("principal.userInfo().authorized")
    public List<MyGroupResponse> getMyGroups(
            @AuthenticationPrincipal RankademyUser rankademyUser
    ) {
        return groupReader.getMyGroupList(rankademyUser.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("principal.userInfo().authorized")
    public Long createGroup(
            @AuthenticationPrincipal RankademyUser rankademyUser,
            @RequestBody @Valid GroupCreateRequest request // TODO: 이미지
    ) {
        Group group = groupWriter.create(rankademyUser.getId(), request);
        return group.getId();
    }

    @GetMapping("/{groupId}")
    public GroupDetailResponse getGroupDetail(
            @AuthenticationPrincipal RankademyUser rankademyUser,
            @PathVariable("groupId") Long groupId
    ) {
        return groupReader.getDetail(rankademyUser != null ? rankademyUser.getId() : null, groupId);
    }

    @GetMapping("/{groupId}/recent-competitions")
    public List<RecentCompetitionResponse> getRecentCompetitions(
            @PathVariable Long groupId
    ) {
        return groupReader.getRecentCompetitions(groupId);
    }

    @GetMapping("/{groupId}/members")
    public List<GroupMemberResponse> getGroupMembers(
            @PathVariable Long groupId,
            @RequestParam(value = "page", defaultValue = "0") int page
    ) {
        return groupReader.getGroupMembers(groupId, page);
    }

    @PutMapping("/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@groupLeaderChecker.isGroupLeader(#rankademyUser.userInfo(), #groupId)")
    public void updateGroup(
            @AuthenticationPrincipal RankademyUser rankademyUser,
            @PathVariable("groupId") Long groupId,
            @RequestBody @Valid GroupUpdateRequest request // TODO: 이미지
    ) {
        groupWriter.update(groupId, request);
    }

    @PostMapping("/{groupId}/recruitment")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@groupLeaderChecker.isGroupLeader(#rankademyUser.userInfo(), #groupId)")
    public void startRecruitment(
            @AuthenticationPrincipal RankademyUser rankademyUser,
            @PathVariable("groupId") Long groupId
    ) {
        groupWriter.startRecruitment(groupId);
    }

    @DeleteMapping("/{groupId}/recruitment")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@groupLeaderChecker.isGroupLeader(#rankademyUser.userInfo(), #groupId)")
    public void closeRecruitment(
            @AuthenticationPrincipal RankademyUser rankademyUser,
            @PathVariable("groupId") Long groupId
    ) {
        groupWriter.closeRecruitment(groupId);
    }

    @DeleteMapping("/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@groupLeaderChecker.isGroupLeader(#rankademyUser.userInfo(), #groupId)")
    public void deleteGroup(
            @AuthenticationPrincipal RankademyUser rankademyUser,
            @PathVariable("groupId") Long groupId
    ) {
        groupWriter.delete(groupId);
    }
}
