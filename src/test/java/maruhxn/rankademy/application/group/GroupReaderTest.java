package maruhxn.rankademy.application.group;

import jakarta.persistence.EntityManager;
import maruhxn.rankademy.application.group.provided.GroupReader;
import maruhxn.rankademy.application.group.provided.dto.*;
import maruhxn.rankademy.application.group.required.GroupRepository;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.group.Group;
import maruhxn.rankademy.domain.group.GroupRole;
import maruhxn.rankademy.domain.user.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

import static maruhxn.rankademy.domain.group.GroupFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@DisplayName("GroupReader 테스트")
class GroupReaderTest {

    @Autowired
    GroupReader groupReader;

    @Autowired
    UserRepository userRepository;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    EntityManager em;

    private User user;
    private User leader;
    private Group group;

    @BeforeEach
    void setUp() {
        user = userRepository.save(createMember());
        leader = userRepository.save(createLeader());
        group = groupRepository.save(createGroup(leader));
        group.upsertRecruitmentPost(createRecruitmentRequest());
    }


    @Test
    @DisplayName("ID로 그룹 조회")
    void getGroup() {
        // when
        Group findGroup = groupReader.get(group.getId());

        // then
        assertThat(findGroup).isNotNull();
        assertThat(findGroup.getName()).isEqualTo("테스트 그룹");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 그룹 조회 시 예외 발생")
    void getGroupWithNonExistId() {
        // when & then
        assertThatThrownBy(() -> groupReader.get(999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("그룹 정보가 존재하지 않습니다. id: 999");
    }

    @Test
    @DisplayName("내가 속한 그룹 목록 조회 - 여러 그룹에 속해있을 경우")
    void getMyGroupListWithMultipleGroups() {
        // given
        group.addMember(user, GroupRole.MEMBER);

        User leader2 = userRepository.save(createLeader("leader2"));
        Group group2 = groupRepository.save(createGroup(leader2, "테스트 그룹 2"));


        User leader3 = userRepository.save(createLeader("leader3"));
        Group group3 = groupRepository.save(createGroup(leader3, "테스트 그룹 3"));

        group2.addMember(user, GroupRole.MEMBER);


        // when
        List<MyGroupResponse> myGroupList = groupReader.getMyGroupList(user.getId());

        // then
        assertThat(myGroupList).hasSize(2);
        assertThat(myGroupList.stream().map(MyGroupResponse::groupName))
                .containsExactlyInAnyOrder("테스트 그룹", "테스트 그룹 2");
    }

    @Test
    @DisplayName("그룹 랭킹 목록 조회 - 티어 순으로 정렬")
    void getRankingListOrderByTier() {
        // given
        User leader2 = userRepository.save(createLeader("leader2"));
        SummonerInfo summonerInfo2 = leader2.getSummonerInfo();
        leader2.getSummonerInfo().update(
                summonerInfo2.getSummonerName(),
                summonerInfo2.getSummonerTag(),
                summonerInfo2.getSummonerIconNum(),
                new TierInfo(Tier.DIAMOND, Rank.II, 0),
                50,
                50
        );
        Group group2 = groupRepository.save(createGroup(leader2, "group2"));
        User member2 = userRepository.save(createMember("member2@test.com", "member2"));
        SummonerInfo member2SummonerInfo = member2.getSummonerInfo();
        member2SummonerInfo.update(
                member2SummonerInfo.getSummonerName(),
                member2SummonerInfo.getSummonerTag(),
                member2SummonerInfo.getSummonerIconNum(),
                new TierInfo(Tier.PLATINUM, Rank.IV, 0),
                50,
                50
        );
        group2.addMember(member2, GroupRole.MEMBER);

        User leader3 = userRepository.save(createLeader("leader3"));
        SummonerInfo summonerInfo3 = leader3.getSummonerInfo();
        summonerInfo3.update(
                summonerInfo3.getSummonerName(),
                summonerInfo3.getSummonerTag(),
                summonerInfo3.getSummonerIconNum(),
                new TierInfo(Tier.PLATINUM, Rank.I, 0),
                40,
                60
        );
        Group group3 = groupRepository.save(createGroup(leader3, "group3"));
        em.flush();
        em.clear();

        // when
        List<GroupResponse> rankingList = groupReader.getRankingList(0, null, GroupSortKey.TIER);

        // then
        assertThat(rankingList).hasSize(3)
                .extracting(GroupResponse::groupId)
                .containsExactly(group2.getId(), group3.getId(), group.getId());
    }

    @Test
    @DisplayName("그룹 랭킹 목록 조회 - 승리 순으로 정렬")
    void getRankingListOrderByWinCount() {
        // given
        User leader2 = userRepository.save(createLeader("leader2"));
        SummonerInfo summonerInfo2 = leader2.getSummonerInfo();
        summonerInfo2.update(
                summonerInfo2.getSummonerName(),
                summonerInfo2.getSummonerTag(),
                summonerInfo2.getSummonerIconNum(),
                new TierInfo(Tier.DIAMOND, Rank.II, 0),
                300,
                50
        );
        Group group2 = groupRepository.save(createGroup(leader2, "group2"));

        User leader3 = userRepository.save(createLeader("leader3"));
        SummonerInfo summonerInfo3 = leader3.getSummonerInfo();
        summonerInfo3.update(
                summonerInfo3.getSummonerName(),
                summonerInfo3.getSummonerTag(),
                summonerInfo3.getSummonerIconNum(),
                new TierInfo(Tier.PLATINUM, Rank.I, 0),
                50,
                60
        );
        Group group3 = groupRepository.save(createGroup(leader3, "group3"));
        em.flush();
        em.clear();


        // when
        List<GroupResponse> rankingList = groupReader.getRankingList(0, null, GroupSortKey.WIN_COUNT);

        // then
        assertThat(rankingList).hasSize(3)
                .extracting(GroupResponse::groupId)
                .containsExactly(group2.getId(), group.getId(), group3.getId());
    }

    @Test
    @DisplayName("그룹 랭킹 목록 조회 - 페이징")
    void getRankingListWithPaging() {
        // given
        for (int i = 0; i < 20; i++) {
            User newLeader = userRepository.save(createLeader("leader" + i));
            groupRepository.save(createGroup(newLeader, "group" + i));
        }

        // when
        List<GroupResponse> rankingList = groupReader.getRankingList(1, null, GroupSortKey.TIER);

        // then
        assertThat(rankingList).hasSize(1)
                .extracting(GroupResponse::name)
                .containsExactly("group19");
    }

    @Test
    @DisplayName("그룹 랭킹 목록 조회 - 검색")
    void getRankingListSearchWithKeyword() {
        // given
        for (int i = 0; i < 20; i++) {
            User newLeader = userRepository.save(createLeader("leader" + i));
            groupRepository.save(createGroup(newLeader, "group" + i));
        }

        // when
        List<GroupResponse> rankingList = groupReader.getRankingList(0, "5", GroupSortKey.TIER);

        // then
        assertThat(rankingList).hasSize(2)
                .extracting(GroupResponse::name)
                .containsExactly("group5", "group15");
    }

    @Test
    @DisplayName("그룹 상세 정보 조회 - 그룹 멤버")
    void getDetailWithMember() {
        // when
        GroupDetailResponse groupDetail = groupReader.getDetail(leader.getId(), group.getId());

        // then
        assertThat(groupDetail).isNotNull();
        assertThat(groupDetail.name()).isEqualTo("테스트 그룹");
        assertThat(groupDetail.about()).isEqualTo("테스트 그룹입니다.");
        assertThat(groupDetail.logoImageUrl()).isEqualTo("logo.jpg");
        assertThat(groupDetail.leader().username()).isEqualTo(leader.getSummonerInfo().getSummonerName());
        assertThat(groupDetail.isJoined()).isTrue();
    }

    @Test
    @DisplayName("그룹 상세 정보 조회 - 그룹 멤버가 아님")
    void getDetailWithNotMember() {
        // when
        GroupDetailResponse groupDetail = groupReader.getDetail(user.getId(), group.getId());

        // then
        assertThat(groupDetail).isNotNull();
        assertThat(groupDetail.name()).isEqualTo("테스트 그룹");
        assertThat(groupDetail.about()).isEqualTo("테스트 그룹입니다.");
        assertThat(groupDetail.logoImageUrl()).isEqualTo("logo.jpg");
        assertThat(groupDetail.leader().username()).isEqualTo(leader.getSummonerInfo().getSummonerName());
        assertThat(groupDetail.isJoined()).isFalse();
    }

    @Test
    @DisplayName("그룹 모집 공고 목록 조회 - 페이징")
    void getRecruitmentPostListWithPaging() {
        // given
        group.startRecruitment();
        for (int i = 0; i < 10; i++) {
            User newLeader = userRepository.save(createLeader("leader" + i));
            Group newGroup = groupRepository.save(createGroup(newLeader, "group" + i));
            newGroup.upsertRecruitmentPost(createRecruitmentRequest());
            newGroup.startRecruitment();
        }

        // when
        List<RecruitmentPostResponse> recruitmentPostList = groupReader.getRecruitmentPostList(1);

        // then
        assertThat(recruitmentPostList).hasSize(1)
                .extracting(RecruitmentPostResponse::groupName)
                .containsExactly(group.getName());
    }

    @Test
    @DisplayName("그룹 모집 공고 목록 조회 - 모집 중인 그룹만 조회")
    void getRecruitmentPostListWithOnlyRecruiting() {
        // given
        group.startRecruitment();

        User leader2 = userRepository.save(createLeader("leader2"));
        Group group2 = groupRepository.save(createGroup(leader2, "group2"));
        group2.upsertRecruitmentPost(createRecruitmentRequest());
        group2.closeRecruitment();

        // when
        List<RecruitmentPostResponse> recruitmentPostList = groupReader.getRecruitmentPostList(0);

        // then
        assertThat(recruitmentPostList).hasSize(1);
        assertThat(recruitmentPostList.get(0).title()).isEqualTo("그룹원 모집합니다");
    }

    @Test
    @DisplayName("그룹 모집 공고 목록 조회 - 활성화된 모집 공고만 조회")
    void getRecruitmentPostListWithOnlyActive() {
        // given
        group.startRecruitment();

        User leader2 = userRepository.save(createLeader("leader2"));
        Group group2 = groupRepository.save(createGroup(leader2, "group2"));
        group2.upsertRecruitmentPost(createRecruitmentRequest());
        group2.getRecruitmentPost().deactivate();

        // when
        List<RecruitmentPostResponse> recruitmentPostList = groupReader.getRecruitmentPostList(0);

        // then
        assertThat(recruitmentPostList).hasSize(1);
        assertThat(recruitmentPostList.get(0).title()).isEqualTo("그룹원 모집합니다");
    }

    @Test
    @DisplayName("그룹 모집 공고 상세 조회 - 그룹 멤버")
    void getRecruitmentPostDetailWithMember() {
        // when
        RecruitmentPostDetailResponse recruitmentPostDetail = groupReader.getRecruitmentPostDetail(leader.getId(), group.getId());

        // then
        assertThat(recruitmentPostDetail).isNotNull();
        assertThat(recruitmentPostDetail.title()).isEqualTo("그룹원 모집합니다");
        assertThat(recruitmentPostDetail.isJoined()).isTrue();
    }

    @Test
    @DisplayName("그룹 모집 공고 상세 조회 - 그룹 멤버가 아님")
    void getRecruitmentPostDetailWithNotMember() {
        // when
        RecruitmentPostDetailResponse recruitmentPostDetail = groupReader.getRecruitmentPostDetail(user.getId(), group.getId());

        // then
        assertThat(recruitmentPostDetail).isNotNull();
        assertThat(recruitmentPostDetail.title()).isEqualTo("그룹원 모집합니다");
        assertThat(recruitmentPostDetail.isJoined()).isFalse();
    }

    @Test
    @DisplayName("그룹 모집 공고 상세 조회 - 비활성화된 모집 공고")
    void getRecruitmentPostDetailWithInactivePost() {
        // given
        group.closeRecruitment();

        // when & then
        assertThatThrownBy(() -> groupReader.getRecruitmentPostDetail(leader.getId(), group.getId()))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("그룹 멤버 목록 조회 - 내용 검증")
    void getGroupMembersWithContent() {
        // given
        group.addMember(user, GroupRole.MEMBER);

        // when
        List<GroupMemberResponse> groupMembers = groupReader.getGroupMembers(group.getId(), 0);

        // then
        assertThat(groupMembers).hasSize(2); // leader + user
        assertThat(groupMembers.stream().map(GroupMemberResponse::summonerName))
                .containsExactlyInAnyOrder(leader.getSummonerInfo().getSummonerName(), user.getSummonerInfo().getSummonerName());
    }

    @Test
    @DisplayName("그룹 멤버 목록 조회 - 페이징")
    void getGroupMembersWithPaging() {
        // given
        // leader is already a member
        for (int i = 0; i < 7; i++) {
            User newMember = userRepository.save(createMember("member" + i + "@rankademy.app", "member" + i));
            group.addMember(newMember, GroupRole.MEMBER);
        }

        // when
        List<GroupMemberResponse> firstPage = groupReader.getGroupMembers(group.getId(), 0);
        List<GroupMemberResponse> secondPage = groupReader.getGroupMembers(group.getId(), 1);

        // then
        assertThat(firstPage).hasSize(7);
        assertThat(secondPage).hasSize(1);
    }

    @Test
    @DisplayName("그룹 가입 신청 목록 조회 - 내용 검증")
    void getJoinRequestsWithContent() {
        // given
        group.addJoinRequest(user);

        // when
        List<JoinRequestResponse> joinRequests = groupReader.getJoinRequests(group.getId(), 0);

        // then
        assertThat(joinRequests).hasSize(1);
        assertThat(joinRequests.get(0).summonerName()).isEqualTo(user.getSummonerInfo().getSummonerName());
    }

    @Test
    @DisplayName("그룹 가입 신청 목록 조회 - 신청이 없는 경우")
    void getJoinRequestsWithEmptyList() {
        // when
        List<JoinRequestResponse> joinRequests = groupReader.getJoinRequests(group.getId(), 0);

        // then
        assertThat(joinRequests).isEmpty();
    }

    @Test
    @DisplayName("그룹 가입 신청 목록 조회 - 페이징")
    void getJoinRequestsWithPaging() {
        // given
        for (int i = 0; i < 11; i++) {
            User newMember = userRepository.save(createMember("member" + i + "@rankademy.app", "member" + i));
            group.addJoinRequest(newMember);
        }

        // when
        List<JoinRequestResponse> firstPage = groupReader.getJoinRequests(group.getId(), 0);
        List<JoinRequestResponse> secondPage = groupReader.getJoinRequests(group.getId(), 1);

        // then
        assertThat(firstPage).hasSize(10);
        assertThat(secondPage).hasSize(1);
    }

    @Test
    @DisplayName("최근 경기 목록 조회 - 현재는 비어있음") // TODO: 대항전 정보 추가 필요
    void getRecentCompetitions() {
        // when
        List<RecentCompetitionResponse> recentCompetitions = groupReader.getRecentCompetitions(group.getId());

        // then
        assertThat(recentCompetitions).isEmpty();
    }
}
