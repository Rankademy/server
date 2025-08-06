package maruhxn.rankademy.application.group;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.group.provided.GroupReader;
import maruhxn.rankademy.application.group.provided.dto.*;
import maruhxn.rankademy.application.group.required.GroupQueryRepository;
import maruhxn.rankademy.application.group.required.GroupRepository;
import maruhxn.rankademy.domain.group.Group;
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
    public List<GroupResponse> getRankingList(int page, String keyword, GroupSortKey sortKey) {
        return groupQueryRepository.getRankingList(page, keyword, sortKey);
    }

    @Override
    public GroupDetailResponse getDetail(Long userId, Long groupId) {
        return groupQueryRepository.getGroupDetails(userId, groupId)
                .orElseThrow(() -> new NoSuchElementException("그룹 정보가 존재하지 않습니다. id: " + groupId));
    }

    @Override
    public List<RecruitmentPostResponse> getRecruitmentPostList(int page) {
        return groupQueryRepository.getRecruitmentPostList(page);
    }

    @Override
    public RecruitmentPostDetailResponse getRecruitmentPostDetail(Long userId, Long groupId) {
        return groupQueryRepository.getRecruitmentPostDetail(userId, groupId)
                .orElseThrow(() -> new NoSuchElementException("그룹 모집 공고 정보가 존재하지 않습니다. id: " + groupId));
    }

    @Override
    public List<GroupMemberResponse> getGroupMembers(Long groupId, int page) {
        return groupQueryRepository.getGroupMembers(groupId, page);
    }

    @Override
    public List<JoinRequestResponse> getJoinRequests(Long groupId, int page) {
        return groupQueryRepository.getJoinRequestList(groupId, page);
    }

    @Override
    public List<RecentCompetitionResponse> getRecentCompetitions(Long groupId) {
        return List.of(); // TODO: 대항전 추가
    }


}
