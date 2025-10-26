package maruhxn.rankademy.application.group.required;

import maruhxn.rankademy.application.group.provided.dto.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface GroupQueryRepository {

    Optional<GroupDetailResponse> getGroupDetails(Long userId, Long groupId);

    Page<RecruitmentPostResponse> getRecruitmentPostList(int page);

    Optional<RecruitmentPostDetailResponse> getRecruitmentPostDetail(Long userId, Long groupId);

    Page<GroupMemberResponse> getGroupMembers(Long groupId, int page);

    Page<JoinRequestResponse> getJoinRequestList(Long groupId, int page);

    List<MyGroupResponse> getMyGroupList(Long userId);

    List<RecentCompetitionResponse> getRecentCompetitions(Long groupId);
}
