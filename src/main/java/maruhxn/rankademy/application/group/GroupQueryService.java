package maruhxn.rankademy.application.group;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.group.provided.GroupReader;
import maruhxn.rankademy.application.group.provided.dto.*;
import maruhxn.rankademy.application.group.required.GroupQueryRepository;
import maruhxn.rankademy.application.group.required.GroupRepository;
import maruhxn.rankademy.domain.group.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class GroupQueryService implements GroupReader {

    private final GroupRepository groupRepository;
    private final GroupQueryRepository groupQueryRepository;

    @Override
    public Group get(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("그룹 정보가 존재하지 않습니다. id: " + groupId));
    }

    @Override
    public List<MyGroupResponse> getMyGroupList(Long userId) {
        return groupQueryRepository.getMyGroupList(userId);
    }

    @Override
    public List<MyGroupSummaryResponse> getMyGroupSummaryList(Long userId) {
        return groupQueryRepository.getMyGroupSummaries(userId);
    }

    @Override
    public GroupDetailResponse getDetail(Long userId, Long groupId) {
        return groupQueryRepository.getGroupDetails(userId, groupId)
                .orElseThrow(() -> new NoSuchElementException("그룹 정보가 존재하지 않습니다. id: " + groupId));
    }

    @Override
    public PagedModel<RecruitmentPostResponse> getRecruitmentPostList(int page) {
        Page<RecruitmentPostResponse> result = groupQueryRepository.getRecruitmentPostList(page);
        return new PagedModel(result);
    }

    @Override
    public RecruitmentPostDetailResponse getRecruitmentPostDetail(Long userId, Long groupId) {
        return groupQueryRepository.getRecruitmentPostDetail(userId, groupId)
                .orElseThrow(() -> new NoSuchElementException("그룹 모집 공고 정보가 존재하지 않습니다. id: " + groupId));
    }

    @Override
    public PagedModel<GroupMemberResponse> getGroupMembers(Long groupId, int page) {
        Page<GroupMemberResponse> result = groupQueryRepository.getGroupMembers(groupId, page);
        return new PagedModel<>(result);
    }

    @Override
    public PagedModel<GroupMemberResponse> getGroupMembersWithoutLeader(Long groupId, int page) {
        Page<GroupMemberResponse> result = groupQueryRepository.getGroupMembersWithoutLeader(groupId, page);
        return new PagedModel<>(result);
    }

    @Override
    public List<SearchGroupMemberResponse> searchGroupMembers(Long groupId, String memberNameKey) {
        return groupQueryRepository.searchGroupMembers(groupId, memberNameKey);
    }

    @Override
    public PagedModel<JoinRequestResponse> getJoinRequests(Long groupId, int page) {
        Page<JoinRequestResponse> result = groupQueryRepository.getJoinRequestList(groupId, page);
        return new PagedModel(result);
    }

    @Override
    public List<RecentCompetitionResponse> getRecentCompetitions(Long groupId) {
        return groupQueryRepository.getRecentCompetitions(groupId);
    }
}
