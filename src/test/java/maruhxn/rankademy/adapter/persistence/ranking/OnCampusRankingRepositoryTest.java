package maruhxn.rankademy.adapter.persistence.ranking;

import jakarta.persistence.EntityManager;
import maruhxn.rankademy.adapter.webapi.dto.UnivStudentRankingResponse;
import maruhxn.rankademy.adapter.webapi.ranking.dto.UnivStudentRankingFilter;
import maruhxn.rankademy.application.competition.required.CompetitionRepository;
import maruhxn.rankademy.application.group.provided.dto.GroupResponse;
import maruhxn.rankademy.application.group.required.GroupRepository;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.competition.Competition;
import maruhxn.rankademy.domain.competition.dto.SubmitCompetitionResultRequest;
import maruhxn.rankademy.domain.group.Group;
import maruhxn.rankademy.domain.match.service.MostChampionCalculator;
import maruhxn.rankademy.domain.user.ChampionPlayRecord;
import maruhxn.rankademy.domain.user.LolPosition;
import maruhxn.rankademy.domain.user.TierInfo;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.domain.user.dto.EnrollUnivRequest;
import maruhxn.rankademy.domain.user.dto.ProfileUpdateRequest;
import maruhxn.rankademy.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.PagedModel;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static maruhxn.rankademy.domain.group.GroupFixture.createGroup;
import static maruhxn.rankademy.domain.group.GroupFixture.createLeader;
import static maruhxn.rankademy.domain.user.Rank.I;
import static maruhxn.rankademy.domain.user.Rank.II;
import static maruhxn.rankademy.domain.user.Tier.*;
import static maruhxn.rankademy.domain.user.UserFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class OnCampusRankingRepositoryTest extends IntegrationTestSupport {

    @Autowired
    OnCampusRankingRepository onCampusRankingRepository;

    @Autowired
    EntityManager em;

    @Autowired
    UserRepository userRepository;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    CompetitionRepository competitionRepository;

    @MockitoBean
    MostChampionCalculator mostChampionCalculator;

    @BeforeEach
    void setUp() {
        // given
        User user1 = createUser("user1@test.com", "user1");
        user1.enrollUnivInfo(new EnrollUnivRequest("서울과학기술대학교", "user1@seoultech.ac.kr", true, 2021, "컴퓨터공학과"));
        user1.updateProfile(new ProfileUpdateRequest("user1", null, LolPosition.TOP, LolPosition.JUNGLE));
        user1.connectSummonerInfo(createSummonerInfoConnector(new TierInfo(GOLD, II, 50)), createRiotAuthRequest("summoner1", "KR1"));
        when(mostChampionCalculator.calculateMostChampionsTop3(anyList(), anyString())).thenReturn(List.of(
                new ChampionPlayRecord("champ1", 15L),
                new ChampionPlayRecord("champ2", 10L),
                new ChampionPlayRecord("champ3", 5L)
        ));
        user1.getSummonerInfo().updateMostChampions(mostChampionCalculator, List.of());
        user1.completeUnivAuthentication();
        em.persist(user1);

        User user2 = createUser("user2@test.com", "user2");
        user2.enrollUnivInfo(new EnrollUnivRequest("서울과학기술대학교", "user2@seoultech.ac.kr", true, 2020, "전기정보공학과"));
        user2.updateProfile(new ProfileUpdateRequest("user2", null, LolPosition.MIDDLE, LolPosition.UTILITY));
        user2.connectSummonerInfo(createSummonerInfoConnector(new TierInfo(EMERALD, I, 20)), createRiotAuthRequest("summoner2", "KR1"));
        when(mostChampionCalculator.calculateMostChampionsTop3(anyList(), anyString())).thenReturn(List.of(
                new ChampionPlayRecord("champ4", 15L),
                new ChampionPlayRecord("champ5", 10L),
                new ChampionPlayRecord("champ6", 5L)
        ));
        user2.getSummonerInfo().updateMostChampions(mostChampionCalculator, List.of());
        user2.completeUnivAuthentication();
        em.persist(user2);

        User user3 = createUser("user3@test.com", "user3");
        user3.enrollUnivInfo(new EnrollUnivRequest("고려대학교", "user3@korea.ac.kr", true, 2022, "경영학과"));
        user3.updateProfile(new ProfileUpdateRequest("user3", null, LolPosition.JUNGLE, LolPosition.MIDDLE));
        user3.connectSummonerInfo(createSummonerInfoConnector(new TierInfo(BRONZE, I, 50)), createRiotAuthRequest("summoner3", "KR1"));
        when(mostChampionCalculator.calculateMostChampionsTop3(anyList(), anyString())).thenReturn(List.of(
                new ChampionPlayRecord("champ7", 15L),
                new ChampionPlayRecord("champ8", 10L),
                new ChampionPlayRecord("champ9", 5L)
        ));
        user3.getSummonerInfo().updateMostChampions(mostChampionCalculator, List.of());
        user3.completeUnivAuthentication();
        em.persist(user3);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("getUnivStudentRanking 메서드는 특정 대학교의 학생 랭킹을 올바르게 반환한다")
    void getUnivStudentRanking() {
        // given
        UnivStudentRankingFilter univStudentRankingFilter = new UnivStudentRankingFilter(null, null, null);

        // when
        PagedModel<UnivStudentRankingResponse> snutUnivStudentRanking = onCampusRankingRepository.getUnivStudentRanking("서울과학기술대학교", 0, univStudentRankingFilter);

        // then
        assertThat(snutUnivStudentRanking.getContent()).hasSize(2);
        assertThat(snutUnivStudentRanking.getContent().get(0).summonerName()).isEqualTo("summoner2");
        assertThat(snutUnivStudentRanking.getContent().get(0).tierInfo().getTier()).isEqualTo(EMERALD);
        assertThat(snutUnivStudentRanking.getContent().get(1).summonerName()).isEqualTo("summoner1");
        assertThat(snutUnivStudentRanking.getContent().get(1).tierInfo().getTier()).isEqualTo(GOLD);

        PagedModel<UnivStudentRankingResponse> koreaUnivStudentRanking = onCampusRankingRepository.getUnivStudentRanking("고려대학교", 0, univStudentRankingFilter);
        assertThat(koreaUnivStudentRanking.getContent()).hasSize(1);
        assertThat(koreaUnivStudentRanking.getContent().get(0).summonerName()).isEqualTo("summoner3");
        assertThat(koreaUnivStudentRanking.getContent().get(0).tierInfo().getTier()).isEqualTo(BRONZE);

    }

    @Test
    @DisplayName("getUnivStudentRanking 메서드는 major 필터를 사용하여 특정 대학교의 학생   랭킹을 올바르게 반환한다")
    void getUnivStudentRankingWithMajorFilter() {
        // given
        UnivStudentRankingFilter univStudentRankingFilter = new UnivStudentRankingFilter("컴퓨터공학과", null, null);

        // when
        PagedModel<UnivStudentRankingResponse> snutUnivStudentRanking = onCampusRankingRepository.getUnivStudentRanking("서울과학기술대학교", 0, univStudentRankingFilter);

        // then
        assertThat(snutUnivStudentRanking.getContent()).hasSize(1);
        assertThat(snutUnivStudentRanking.getContent().get(0).summonerName()).isEqualTo("summoner1");
    }

    @Test
    @DisplayName("getUnivStudentRanking 메서드는 admissionYear 필터를 사용하여 특정 대학교의 학생 랭킹을 올바르게 반환한다")
    void getUnivStudentRankingWithAdmissionYearFilter() {
        // given
        UnivStudentRankingFilter univStudentRankingFilter = new UnivStudentRankingFilter(null, 2020, null);

        // when
        PagedModel<UnivStudentRankingResponse> snutUnivStudentRanking = onCampusRankingRepository.getUnivStudentRanking("서울과학기술대학교", 0, univStudentRankingFilter);

        // then
        assertThat(snutUnivStudentRanking.getContent()).hasSize(1);
        assertThat(snutUnivStudentRanking.getContent().get(0).summonerName()).isEqualTo("summoner2");
    }

    @Test
    @DisplayName("getUnivStudentRanking 메서드는 mainPosition 필터를 사용하여 특정 대학교의 학생 랭킹을 올바르게 반환한다")
    void getUnivStudentRankingWithMainPositionFilter() {
        // given
        UnivStudentRankingFilter univStudentRankingFilter = new UnivStudentRankingFilter(null, null, LolPosition.TOP);

        // when
        PagedModel<UnivStudentRankingResponse> snutUnivStudentRanking = onCampusRankingRepository.getUnivStudentRanking("서울과학기술대학교", 0, univStudentRankingFilter);

        // then
        assertThat(snutUnivStudentRanking.getContent()).hasSize(1);
        assertThat(snutUnivStudentRanking.getContent().get(0).summonerName()).isEqualTo("summoner1");
    }

    @Test
    @DisplayName("getUnivStudentRanking 메서드는 여러 필터를 사용하여 특정 대학교의 학생 랭킹을 올바르게 반환한다")
    void getUnivStudentRankingWithMultipleFilters() {
        // given
        UnivStudentRankingFilter univStudentRankingFilter = new UnivStudentRankingFilter("전기정보공학과", 2020, LolPosition.MIDDLE);

        // when
        PagedModel<UnivStudentRankingResponse> snutUnivStudentRanking = onCampusRankingRepository.getUnivStudentRanking("서울과학기술대학교", 0, univStudentRankingFilter);

        // then
        assertThat(snutUnivStudentRanking.getContent()).hasSize(1);
        assertThat(snutUnivStudentRanking.getContent().get(0).summonerName()).isEqualTo("summoner2");
    }

    @Test
    @DisplayName("getUnivStudentRanking 메서드는 필터에 해당하는 결과가 없으면 빈 리스트를 반환한다")
    void getUnivStudentRankingWithNoResults() {
        // given
        UnivStudentRankingFilter univStudentRankingFilter = new UnivStudentRankingFilter("기계공학과", null, null);

        // when
        PagedModel<UnivStudentRankingResponse> snutUnivStudentRanking = onCampusRankingRepository.getUnivStudentRanking("서울과학기술대학교", 0, univStudentRankingFilter);

        // then
        assertThat(snutUnivStudentRanking.getContent()).isEmpty();
    }

//    @Test
//    @DisplayName("그룹 랭킹 목록 조회 - 티어 순으로 정렬")
//    void getGroupRankingOrderByTier() {
//        // given
//        User leader = userRepository.save(createLeader());
//        Group group = groupRepository.save(createGroup(leader));
//
//        User leader2 = userRepository.save(createLeader("leader2"));
//        SummonerInfo summonerInfo2 = leader2.getSummonerInfo();
//        leader2.getSummonerInfo().update(
//                summonerInfo2.getSummonerName(),
//                summonerInfo2.getSummonerTag(),
//                summonerInfo2.getsummonerIcon(),
//                new TierInfo(Tier.DIAMOND, Rank.II, 0),
//                50,
//                50
//        );
//        Group group2 = groupRepository.save(createGroup(leader2, "group2"));
//        User member2 = userRepository.save(createMember("member2@test.com", "member2"));
//        SummonerInfo member2SummonerInfo = member2.getSummonerInfo();
//        member2SummonerInfo.update(
//                member2SummonerInfo.getSummonerName(),
//                member2SummonerInfo.getSummonerTag(),
//                member2SummonerInfo.getsummonerIcon(),
//                new TierInfo(Tier.PLATINUM, Rank.IV, 0),
//                50,
//                50
//        );
//        group2.addMember(member2, GroupRole.MEMBER);
//
//        User leader3 = userRepository.save(createLeader("leader3"));
//        SummonerInfo summonerInfo3 = leader3.getSummonerInfo();
//        summonerInfo3.update(
//                summonerInfo3.getSummonerName(),
//                summonerInfo3.getSummonerTag(),
//                summonerInfo3.getsummonerIcon(),
//                new TierInfo(Tier.PLATINUM, Rank.I, 0),
//                40,
//                60
//        );
//        Group group3 = groupRepository.save(createGroup(leader3, "group3"));
//        em.flush();
//        em.clear();
//
//        GroupRankingFilter filter = new GroupRankingFilter(null, null, null, null);
//
//        // when
//        PagedModel<GroupResponse> rankingList = onCampusRankingRepository.getGroupRanking("서울과학기술대학교", 0, filter);
//
//        // then
//        assertThat(rankingList.getContent()).hasSize(3)
//                .extracting(GroupResponse::groupId)
//                .containsExactly(group2.getId(), group3.getId(), group.getId());
//    }

    @Test
    @DisplayName("그룹 랭킹 목록 조회 - 페이징")
    void getGroupRankingWithPaging() {
        // given
        for (int i = 0; i < 21; i++) {
            User newLeader = userRepository.save(createLeader("leader" + i));
            groupRepository.save(createGroup(newLeader, "group" + i));
        }

        // when
        PagedModel<GroupResponse> rankingList = onCampusRankingRepository.getGroupRanking("서울과학기술대학교", 1, null);

        // then
        assertThat(rankingList.getContent()).hasSize(1)
                .extracting(GroupResponse::name)
                .containsExactly("group20");
    }

    @Test
    @DisplayName("그룹 랭킹 목록 조회 - 검색")
    void getGroupRankingSearchWithKeyword() {
        // given
        for (int i = 0; i < 20; i++) {
            User newLeader = userRepository.save(createLeader("leader" + i));
            groupRepository.save(createGroup(newLeader, "group" + i));
        }

        // when
        PagedModel<GroupResponse> rankingList = onCampusRankingRepository.getGroupRanking("서울과학기술대학교", 0, "5");

        // then
        assertThat(rankingList.getContent()).hasSize(2)
                .extracting(GroupResponse::name)
                .containsExactly("group5", "group15");
    }

    @Test
    @DisplayName("그룹 랭킹 목록 조회 - 대항전 승리 순으로 정렬")
    void getGroupRankingOrderByWinCount() {
        // given: 그룹 2개 더 생성
        User leader = userRepository.save(createLeader());
        Group group = groupRepository.save(createGroup(leader));

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

        // when
        PagedModel<GroupResponse> rankingList = onCampusRankingRepository.getGroupRanking(
                "서울과학기술대학교", 0, null
        );

        // then: 승리 수 내림차순으로 group2(2승), group3(1승), group(0승)
        assertThat(rankingList.getContent()).hasSize(3)
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