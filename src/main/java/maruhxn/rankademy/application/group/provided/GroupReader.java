package maruhxn.rankademy.application.group.provided;

import maruhxn.rankademy.application.group.provided.dto.*;
import maruhxn.rankademy.domain.group.Group;
import org.springframework.data.web.PagedModel;

import java.util.List;

public interface GroupReader {

    Group get(Long groupId);

    List<MyGroupResponse> getMyGroupList(Long userId);

    List<MyGroupSummaryResponse> getMyGroupSummaryList(Long userId);

    GroupDetailResponse getDetail(Long userId, Long groupId);

    PagedModel<RecruitmentPostResponse> getRecruitmentPostList(int page);

    RecruitmentPostDetailResponse getRecruitmentPostDetail(Long userId, Long groupId);

    PagedModel<GroupMemberResponse> getGroupMembers(Long groupId, int page);

    PagedModel<JoinRequestResponse> getJoinRequests(Long groupId, int page);

    List<RecentCompetitionResponse> getRecentCompetitions(Long groupId);

    PagedModel<GroupMemberResponse> getGroupMembersWithoutLeader(Long groupId, int page);
}
