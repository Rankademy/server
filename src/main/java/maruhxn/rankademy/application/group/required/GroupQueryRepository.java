package maruhxn.rankademy.application.group.required;

import maruhxn.rankademy.application.group.provided.dto.*;

import java.util.List;
import java.util.Optional;

public interface GroupQueryRepository {

    List<GroupResponse> getRankingList(int page, String keyword, GroupSortKey sortKey);

    Optional<GroupDetailResponse> getGroupDetails(Long userId, Long groupId);

    List<RecruitmentPostResponse> getRecruitmentPostList(int page);

    Optional<RecruitmentPostDetailResponse> getRecruitmentPostDetail(Long userId, Long groupId);

    List<GroupMemberResponse> getGroupMembers(Long groupId, int page);

    List<JoinRequestResponse> getJoinRequestList(Long groupId, int page);

    List<MyGroupResponse> getMyGroupList(Long userId);
}
