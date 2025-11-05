package maruhxn.rankademy.adapter.persistence.ranking;

import jakarta.persistence.EntityManager;
import maruhxn.rankademy.adapter.webapi.dto.TotalUserRankingResponse;
import maruhxn.rankademy.adapter.webapi.dto.UnivRankingResponse;
import maruhxn.rankademy.application.competition.required.CompetitionRepository;
import maruhxn.rankademy.application.group.required.GroupRepository;
import maruhxn.rankademy.domain.competition.Competition;
import maruhxn.rankademy.domain.competition.dto.SubmitCompetitionResultRequest;
import maruhxn.rankademy.domain.group.Group;
import maruhxn.rankademy.domain.match.service.MostChampionCalculator;
import maruhxn.rankademy.domain.user.*;
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
import static maruhxn.rankademy.domain.user.Rank.I;
import static maruhxn.rankademy.domain.user.Rank.II;
import static maruhxn.rankademy.domain.user.Tier.*;
import static maruhxn.rankademy.domain.user.UserFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class TotalRankingRepositoryTest extends IntegrationTestSupport {

    @Autowired
    TotalRankingRepository totalRankingRepository;

    @Autowired
    EntityManager em;

    @Autowired
    CompetitionRepository competitionRepository;

    @Autowired
    GroupRepository groupRepository;

    @MockitoBean
    MostChampionCalculator mostChampionCalculator;

    User user1, user2, user3;

    @BeforeEach
    void setUp() {
        // given
        user1 = createUser("user1@test.com", "user1");
        user1.completeUnivAuthentication(new EnrollUnivRequest("서울과학기술대학교", "user1@seoultech.ac.kr"));
        user1.updateProfile(new ProfileUpdateRequest("user1", null, LolPosition.TOP, LolPosition.JUNGLE, 2021, "컴퓨터공학과"));
        user1.connectSummonerInfo(createSummonerInfoConnector(new TierInfo(GOLD, II, 50)), createRiotAuthRequest("summoner1", "KR1"));
        when(mostChampionCalculator.calculateMostChampionsTop3(anyList(), anyString())).thenReturn(List.of(
                new ChampionPlayRecord("champ1", 15L),
                new ChampionPlayRecord("champ2", 10L),
                new ChampionPlayRecord("champ3", 5L)
        ));
        user1.getSummonerInfo().updateMostChampions(mostChampionCalculator, List.of());
        em.persist(user1);

        user2 = createUser("user2@test.com", "user2");
        user2.completeUnivAuthentication(new EnrollUnivRequest("서울과학기술대학교", "user2@seoultech.ac.kr"));
        user2.updateProfile(new ProfileUpdateRequest("user2", null, LolPosition.MIDDLE, LolPosition.UTILITY,2020, "전기정보공학과"));
        user2.connectSummonerInfo(createSummonerInfoConnector(new TierInfo(EMERALD, I, 20)), createRiotAuthRequest("summoner2", "KR1"));
        when(mostChampionCalculator.calculateMostChampionsTop3(anyList(), anyString())).thenReturn(List.of(
                new ChampionPlayRecord("champ4", 15L),
                new ChampionPlayRecord("champ5", 10L),
                new ChampionPlayRecord("champ6", 5L)
        ));
        user2.getSummonerInfo().updateMostChampions(mostChampionCalculator, List.of());
        em.persist(user2);

        user3 = createUser("user3@test.com", "user3");
        user3.completeUnivAuthentication(new EnrollUnivRequest("고려대학교", "user3@korea.ac.kr"));
        user3.updateProfile(new ProfileUpdateRequest("user3", null, LolPosition.JUNGLE, LolPosition.MIDDLE, 2022, "경영학과"));
        user3.connectSummonerInfo(createSummonerInfoConnector(new TierInfo(BRONZE, I, 50)), createRiotAuthRequest("summoner3", "KR1"));
        when(mostChampionCalculator.calculateMostChampionsTop3(anyList(), anyString())).thenReturn(List.of(
                new ChampionPlayRecord("champ7", 15L),
                new ChampionPlayRecord("champ8", 10L),
                new ChampionPlayRecord("champ9", 5L)
        ));
        user3.getSummonerInfo().updateMostChampions(mostChampionCalculator, List.of());
        em.persist(user3);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("getUnivRanking: 대항전 승리 수 기준 내림차순 정렬 및 DISTINCT 집계가 정확하다")
    void getUnivRanking_orderByWinCount_andDistinct() {
        // given
        // 같은 대학(서울과기대)에 그룹 2개, 고려대에 그룹 1개
        Group st1 = groupRepository.save(createGroup(user1, "st1"));
        Group st2 = groupRepository.save(createGroup(user2, "st2"));
        Group ku = groupRepository.save(createGroup(user3, "ku"));

        // 경기 구성
        createCompletedCompetition(st1.getId(), st2.getId());
        createCompletedCompetition(st1.getId(), ku.getId());
        createCompletedCompetition(ku.getId(), st2.getId());

        em.flush();
        em.clear();

        // when
        PagedModel<UnivRankingResponse> rows = totalRankingRepository.getUnivRanking(0, null);

        // then: 서울과기대가 승리 2로 1위, 고려대가 승리 1로 2위
        List<UnivRankingResponse> content = rows.getContent();
        assertThat(content.size()).isEqualTo(2);
        UnivRankingResponse seoultech = content.get(0);
        UnivRankingResponse korea = content.get(1);

        assertThat(seoultech.univName()).isEqualTo("서울과학기술대학교");
        assertThat(korea.univName()).isEqualTo("고려대학교");

        // DISTINCT 검증: intra-univ 경기(stA–stB)는 총 1건으로 집계되어야 함
        assertThat(seoultech.competitionTotalCnt()).isEqualTo(3L); // (1),(2),(3) 모두 서울과기대 소속 그룹이 참여
        assertThat(seoultech.competitionWinCnt()).isEqualTo(2L);   // stA가 2승

        assertThat(korea.competitionTotalCnt()).isEqualTo(2L);     // (2),(3)
        assertThat(korea.competitionWinCnt()).isEqualTo(1L);       // ku가 1승

        // 활성 유저 수 & 랭커 확인
        assertThat(seoultech.totalUserCnt()).isEqualTo(2L);        // user1, user2
        assertThat(seoultech.rankerDto().summonerName()).isEqualTo(user2.getSummonerInfo().getSummonerName()); // EMERALD > GOLD
        assertThat(korea.totalUserCnt()).isEqualTo(1L);            // user3
        assertThat(korea.rankerDto().summonerName()).isEqualTo(user3.getSummonerInfo().getSummonerName());
    }

    @Test
    @DisplayName("getUnivRanking: 대학명 키워드로 필터링된다")
    void getUnivRanking_filterByUnivNameKey() {
        // when
        PagedModel<UnivRankingResponse> rows = totalRankingRepository.getUnivRanking(0, "서울과");

        // then
        assertThat(rows.getContent()).hasSize(1);
        assertThat(rows.getContent().get(0).univName()).isEqualTo("서울과학기술대학교");
    }

    private Competition createCompletedCompetition(Long winnerGroupId, Long loserGroupId) {
        Competition competition = Competition.createAfterAccept(1L, 2L);
        SubmitCompetitionResultRequest request = new SubmitCompetitionResultRequest(
                1L,
                2L,
                1,
                List.of(new SubmitCompetitionResultRequest.SetResultDto(1, 1L, "image1")),
                "Team 1 won",
                1L,
                winnerGroupId,
                loserGroupId
        );
        competition.submitSetResult(request, LocalDateTime.now());
        return competitionRepository.save(competition);
    }

    @Test
    @DisplayName("getTotalUserRanking: 티어 내림차순 정렬 + 활성화 유저만 포함")
    void getTotalUserRanking_orderByTier_andActiveOnly() {
        // given: 미인증 유저(user4) 추가 (더 높은 티어지만 결과에 나오면 안 됨)
        User user4 = createUser("user4@test.com", "user4");
        user4.updateProfile(new ProfileUpdateRequest("user4", null, LolPosition.BOTTOM, LolPosition.UTILITY, 2023, "산업공학과"));
        // DIAMOND > EMERALD > GOLD > BRONZE (프로젝트의 mappedTier 기준 가정: 기존 테스트에서도 DIAMOND 사용)
        user4.connectSummonerInfo(createSummonerInfoConnector(new TierInfo(Tier.DIAMOND, Rank.I, 0)), createRiotAuthRequest("summoner4", "KR1"));
        when(mostChampionCalculator.calculateMostChampionsTop3(anyList(), anyString())).thenReturn(List.of(
                new ChampionPlayRecord("champ10", 9L),
                new ChampionPlayRecord("champ11", 6L),
                new ChampionPlayRecord("champ12", 3L)
        ));
        user4.getSummonerInfo().updateMostChampions(mostChampionCalculator, List.of());
        em.persist(user4);
        em.flush();
        em.clear();

        // when
        PagedModel<TotalUserRankingResponse> page = totalRankingRepository.getTotalUserRanking(0, null);

        // then: EMERALD(user2) > GOLD(user1) > BRONZE(user3) 순, user4(미인증)는 제외
        List<TotalUserRankingResponse> content = page.getContent();
        assertThat(content).hasSize(3)
                .extracting(TotalUserRankingResponse::summonerName)
                .containsExactly("summoner2", "summoner1", "summoner3");
    }

    @Test
    @DisplayName("getTotalUserRanking: 소환사이름 키워드 검색(부분 일치, 대소문자 무시)")
    void getTotalUserRanking_searchBySummonerNameKey() {
        // when
        PagedModel<TotalUserRankingResponse> page = totalRankingRepository.getTotalUserRanking(0, "mOnEr2"); // 'summoner2'의 부분 문자열

        // then
        List<TotalUserRankingResponse> content = page.getContent();
        assertThat(content).hasSize(1);
        assertThat(content.get(0).summonerName()).isEqualTo("summoner2");
    }

    @Test
    @DisplayName("getTotalUserRanking: 검색 결과가 없으면 빈 페이지를 반환한다")
    void getTotalUserRanking_searchNoResult() {
        // when
        PagedModel<TotalUserRankingResponse> page = totalRankingRepository.getTotalUserRanking(0, "no_such_name");

        // then
        assertThat(page.getMetadata().totalElements()).isEqualTo(0);
        assertThat(page.getContent()).isEmpty();
    }
}