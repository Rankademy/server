package maruhxn.rankademy.adapter.persistence.ranking;

import jakarta.persistence.EntityManager;
import maruhxn.rankademy.adapter.webapi.ranking.dto.GroupRankingFilter;
import maruhxn.rankademy.application.competition.required.CompetitionRepository;
import maruhxn.rankademy.application.group.provided.dto.GroupResponse;
import maruhxn.rankademy.application.group.provided.dto.GroupSortKey;
import maruhxn.rankademy.application.group.required.GroupRepository;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.competition.Competition;
import maruhxn.rankademy.domain.competition.dto.SubmitCompetitionResultRequest;
import maruhxn.rankademy.domain.group.Group;
import maruhxn.rankademy.domain.group.GroupRole;
import maruhxn.rankademy.domain.user.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static maruhxn.rankademy.domain.group.GroupFixture.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("GroupRankingRepository 테스트")
class GroupRankingRepositoryTest {

    @Autowired
    GroupRankingRepository groupRankingRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    CompetitionRepository competitionRepository;

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
    @DisplayName("그룹 랭킹 목록 조회 - 티어 순으로 정렬")
    void getGroupRankingOrderByTier() {
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

        GroupRankingFilter filter = new GroupRankingFilter(null, null, null, null);

        // when
        List<GroupResponse> rankingList = groupRankingRepository.getGroupRanking("서울과학기술대학교", 0, GroupSortKey.TIER, filter);

        // then
        assertThat(rankingList).hasSize(3)
                .extracting(GroupResponse::groupId)
                .containsExactly(group2.getId(), group3.getId(), group.getId());
    }

    @Test
    @DisplayName("그룹 랭킹 목록 조회 - 페이징")
    void getGroupRankingWithPaging() {
        // given
        for (int i = 0; i < 20; i++) {
            User newLeader = userRepository.save(createLeader("leader" + i));
            groupRepository.save(createGroup(newLeader, "group" + i));
        }
        GroupRankingFilter filter = new GroupRankingFilter(null, null, null, null);

        // when
        List<GroupResponse> rankingList = groupRankingRepository.getGroupRanking("서울과학기술대학교", 1, GroupSortKey.TIER, filter);

        // then
        assertThat(rankingList).hasSize(1)
                .extracting(GroupResponse::name)
                .containsExactly("group19");
    }

    @Test
    @DisplayName("그룹 랭킹 목록 조회 - 검색")
    void getGroupRankingSearchWithKeyword() {
        // given
        for (int i = 0; i < 20; i++) {
            User newLeader = userRepository.save(createLeader("leader" + i));
            groupRepository.save(createGroup(newLeader, "group" + i));
        }
        GroupRankingFilter filter = new GroupRankingFilter("5", null, null, null);

        // when
        List<GroupResponse> rankingList = groupRankingRepository.getGroupRanking("서울과학기술대학교", 0, GroupSortKey.TIER, filter);

        // then
        assertThat(rankingList).hasSize(2)
                .extracting(GroupResponse::name)
                .containsExactly("group5", "group15");
    }

    @Test
    @DisplayName("그룹 랭킹 목록 조회 - 대항전 승리 순으로 정렬")
    void getGroupRankingOrderByWinCount() {
        // given: 그룹 2개 더 생성
        User leader2 = userRepository.save(createLeader("leader2"));
        Group group2 = groupRepository.save(createGroup(leader2, "group2"));

        User leader3 = userRepository.save(createLeader("leader3"));
        Group group3 = groupRepository.save(createGroup(leader3, "group3"));

        // 완료된 대항전 세팅
        // group2: 2승 (vs group, vs group3)
        createCompletedCompetition(group2.getId(), group.getId());
        createCompletedCompetition(group2.getId(), group3.getId());

        // group3: 1승 (vs group)
        createCompletedCompetition(group3.getId(), group.getId());

        // (옵션) 패배 경기도 참여수 카운트를 위해 한두 개 더 넣고 싶다면 아래처럼 추가 가능
        createCompletedCompetition(group2.getId(), group3.getId()); // 이미 위에 1번 있음
        createCompletedCompetition(group3.getId(), group2.getId()); // 승수 동률 방지하려면 생략

        em.flush();
        em.clear();

        GroupRankingFilter filter = new GroupRankingFilter(null, null, null, null);

        // when
        List<GroupResponse> rankingList = groupRankingRepository.getGroupRanking(
                "서울과학기술대학교", 0, GroupSortKey.WIN_COUNT, filter
        );

        // then: 승리 수 내림차순으로 group2(2승), group3(1승), group(0승)
        assertThat(rankingList).hasSize(3)
                .extracting(GroupResponse::groupId)
                .containsExactly(group2.getId(), group3.getId(), group.getId());
    }

    /**
     * 완료된 대항전 엔티티 생성 헬퍼.
     * 프로젝트 엔티티 정의에 맞게 생성자/세터/빌더는 필요시 수정해 사용하세요.
     */
    private Competition createCompletedCompetition(Long winnerGroupId, Long loserGroupId) {
        Competition competition = Competition.createAfterAccept(1L, 2L);
        SubmitCompetitionResultRequest request = new SubmitCompetitionResultRequest(
                1L,
                2L,
                3,
                List.of(
                        new SubmitCompetitionResultRequest.SetResultDto(1, 1L, "image1"),
                        new SubmitCompetitionResultRequest.SetResultDto(2, 2L, "image2"),
                        new SubmitCompetitionResultRequest.SetResultDto(3, winnerGroupId, "image3")
                ),
                "Team 1 won",
                1L,
                winnerGroupId,
                loserGroupId
        );
        competition.submitSetResult(request, LocalDateTime.now());
        return competitionRepository.save(competition);
    }
}