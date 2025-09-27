package maruhxn.rankademy.adapter.persistence;

import jakarta.persistence.EntityManager;
import maruhxn.rankademy.adapter.persistence.ranking.OnCampusRankingRepository;
import maruhxn.rankademy.adapter.webapi.dto.UnivStudentRankingResponse;
import maruhxn.rankademy.adapter.webapi.ranking.dto.UnivStudentRankingFilter;
import maruhxn.rankademy.domain.match.service.MostChampionCalculator;
import maruhxn.rankademy.domain.user.ChampionPlayRecord;
import maruhxn.rankademy.domain.user.LolPosition;
import maruhxn.rankademy.domain.user.TierInfo;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.domain.user.dto.EnrollUnivRequest;
import maruhxn.rankademy.domain.user.dto.ProfileUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static maruhxn.rankademy.domain.user.Rank.I;
import static maruhxn.rankademy.domain.user.Rank.II;
import static maruhxn.rankademy.domain.user.Tier.*;
import static maruhxn.rankademy.domain.user.UserFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class OnCampusRankingRepositoryTest {

    @Autowired
    OnCampusRankingRepository onCampusRankingRepository;

    @Autowired
    EntityManager em;

    @MockitoBean
    MostChampionCalculator mostChampionCalculator;

    @BeforeEach
    void setUp() {
        // given
        User user1 = createUser("user1@test.com", "user1");
        user1.enrollUnivInfo(new EnrollUnivRequest("서울과학기술대학교", "user1@seoultech.ac.kr", true, 2021, "컴퓨터공학과"));
        user1.updateProfile(new ProfileUpdateRequest("user1", null, LolPosition.TOP, LolPosition.JG));
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
        user2.updateProfile(new ProfileUpdateRequest("user2", null, LolPosition.MID, LolPosition.SUP));
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
        user3.updateProfile(new ProfileUpdateRequest("user3", null, LolPosition.JG, LolPosition.MID));
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
        List<UnivStudentRankingResponse> snutUnivStudentRanking = onCampusRankingRepository.getUnivStudentRanking("서울과학기술대학교", 0, univStudentRankingFilter);

        // then
        assertThat(snutUnivStudentRanking).hasSize(2);
        assertThat(snutUnivStudentRanking.get(0).summonerName()).isEqualTo("summoner2");
        assertThat(snutUnivStudentRanking.get(0).tierInfo().getTier()).isEqualTo(EMERALD);
        assertThat(snutUnivStudentRanking.get(0).topMosts()).containsExactly("champ4", "champ5", "champ6");
        assertThat(snutUnivStudentRanking.get(1).summonerName()).isEqualTo("summoner1");
        assertThat(snutUnivStudentRanking.get(1).tierInfo().getTier()).isEqualTo(GOLD);
        assertThat(snutUnivStudentRanking.get(1).topMosts()).containsExactly("champ1", "champ2", "champ3");

        List<UnivStudentRankingResponse> koreaUnivStudentRanking = onCampusRankingRepository.getUnivStudentRanking("고려대학교", 0, univStudentRankingFilter);
        assertThat(koreaUnivStudentRanking).hasSize(1);
        assertThat(koreaUnivStudentRanking.get(0).summonerName()).isEqualTo("summoner3");
        assertThat(koreaUnivStudentRanking.get(0).tierInfo().getTier()).isEqualTo(BRONZE);
        assertThat(koreaUnivStudentRanking.get(0).topMosts()).containsExactly("champ7", "champ8", "champ9");

    }

    @Test
    @DisplayName("getUnivStudentRanking 메서드는 major 필터를 사용하여 특정 대학교의 학생   랭킹을 올바르게 반환한다")
    void getUnivStudentRankingWithMajorFilter() {
        // given
        UnivStudentRankingFilter univStudentRankingFilter = new UnivStudentRankingFilter("컴퓨터공학과", null, null);

        // when
        List<UnivStudentRankingResponse> snutUnivStudentRanking = onCampusRankingRepository.getUnivStudentRanking("서울과학기술대학교", 0, univStudentRankingFilter);

        // then
        assertThat(snutUnivStudentRanking).hasSize(1);
        assertThat(snutUnivStudentRanking.get(0).summonerName()).isEqualTo("summoner1");
    }

    @Test
    @DisplayName("getUnivStudentRanking 메서드는 admissionYear 필터를 사용하여 특정 대학교의 학생 랭킹을 올바르게 반환한다")
    void getUnivStudentRankingWithAdmissionYearFilter() {
        // given
        UnivStudentRankingFilter univStudentRankingFilter = new UnivStudentRankingFilter(null, 2020, null);

        // when
        List<UnivStudentRankingResponse> snutUnivStudentRanking = onCampusRankingRepository.getUnivStudentRanking("서울과학기술대학교", 0, univStudentRankingFilter);

        // then
        assertThat(snutUnivStudentRanking).hasSize(1);
        assertThat(snutUnivStudentRanking.get(0).summonerName()).isEqualTo("summoner2");
    }

    @Test
    @DisplayName("getUnivStudentRanking 메서드는 mainPosition 필터를 사용하여 특정 대학교의 학생 랭킹을 올바르게 반환한다")
    void getUnivStudentRankingWithMainPositionFilter() {
        // given
        UnivStudentRankingFilter univStudentRankingFilter = new UnivStudentRankingFilter(null, null, LolPosition.TOP);

        // when
        List<UnivStudentRankingResponse> snutUnivStudentRanking = onCampusRankingRepository.getUnivStudentRanking("서울과학기술대학교", 0, univStudentRankingFilter);

        // then
        assertThat(snutUnivStudentRanking).hasSize(1);
        assertThat(snutUnivStudentRanking.get(0).summonerName()).isEqualTo("summoner1");
    }

    @Test
    @DisplayName("getUnivStudentRanking 메서드는 여러 필터를 사용하여 특정 대학교의 학생 랭킹을 올바르게 반환한다")
    void getUnivStudentRankingWithMultipleFilters() {
        // given
        UnivStudentRankingFilter univStudentRankingFilter = new UnivStudentRankingFilter("전기정보공학과", 2020, LolPosition.MID);

        // when
        List<UnivStudentRankingResponse> snutUnivStudentRanking = onCampusRankingRepository.getUnivStudentRanking("서울과학기술대학교", 0, univStudentRankingFilter);

        // then
        assertThat(snutUnivStudentRanking).hasSize(1);
        assertThat(snutUnivStudentRanking.get(0).summonerName()).isEqualTo("summoner2");
    }

    @Test
    @DisplayName("getUnivStudentRanking 메서드는 필터에 해당하는 결과가 없으면 빈 리스트를 반환한다")
    void getUnivStudentRankingWithNoResults() {
        // given
        UnivStudentRankingFilter univStudentRankingFilter = new UnivStudentRankingFilter("기계공학과", null, null);

        // when
        List<UnivStudentRankingResponse> snutUnivStudentRanking = onCampusRankingRepository.getUnivStudentRanking("서울과학기술대학교", 0, univStudentRankingFilter);

        // then
        assertThat(snutUnivStudentRanking).isEmpty();
    }
}