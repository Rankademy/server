package maruhxn.rankademy.adapter.webapi.group;

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
public class RecruitmentPostApi {

    private final GroupReader groupReader;
    private final RecruitPostWriter recruitPostWriter;

    @GetMapping("/posts")
    public List<RecruitmentPostResponse> getRecruitmentPostList(
            @RequestParam("page") int page
    ) {
        return groupReader.getRecruitmentPostList(page);
    }

    @GetMapping("/{groupId}/post")
    public RecruitmentPostDetailResponse getRecruitmentPostDetails(
            @AuthenticationPrincipal RankademyUser rankademyUser,
            @PathVariable Long groupId
    ) {
        return groupReader.getRecruitmentPostDetail(rankademyUser.getId(), groupId);
    }

    @PostMapping("/{groupId}/posts")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@groupLeaderChecker.isGroupLeader(principal.userInfo(), #groupId)")
    public void upsertRecruitmentPost(
            @PathVariable Long groupId,
            @RequestBody CreateRecruitmentPostRequest request
    ) {
        recruitPostWriter.upsertRecruitmentPost(groupId, request);
    }

    @PatchMapping("/{groupId}/posts/up")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@groupLeaderChecker.isGroupLeader(principal.userInfo(), #groupId)")
    public void upRecruitmentPost(
            @PathVariable Long groupId
    ) {
        recruitPostWriter.up(groupId);
    }
}
