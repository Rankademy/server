package maruhxn.rankademy.adapter.persistence;

import jakarta.persistence.EntityManager;
import maruhxn.rankademy.adapter.webapi.dto.UnivRankingResponse;
import maruhxn.rankademy.adapter.webapi.dto.UnivStudentRankingResponse;
import maruhxn.rankademy.domain.match.service.MostChampionCalculator;
import maruhxn.rankademy.domain.user.ChampionPlayRecord;
import maruhxn.rankademy.domain.user.TierInfo;
import maruhxn.rankademy.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static maruhxn.rankademy.domain.user.Rank.I;
import static maruhxn.rankademy.domain.user.Rank.II;
import static maruhxn.rankademy.domain.user.Tier.*;
import static maruhxn.rankademy.domain.user.UserFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@DataJpaTest
@Import(UnivRankingRepository.class)
class UnivRankingRepositoryTest {

    @Autowired
    UnivRankingRepository univRankingRepository;

    @Autowired
    EntityManager em;

    @MockitoBean
    MostChampionCalculator mostChampionCalculator;

    @BeforeEach
    void setUp() {
        // given
        User user1 = createUser("user1@test.com", "user1");
        user1.enrollUnivInfo(createEnrollUnivRequest("서울과학기술대학교", "user1@seoultech.ac.kr"));
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
        user2.enrollUnivInfo(createEnrollUnivRequest("서울과학기술대학교", "user2@seoultech.ac.kr"));
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
        user3.enrollUnivInfo(createEnrollUnivRequest("고려대학교", "user3@korea.ac.kr"));
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
    @DisplayName("getUnivRanking 메서드는 대학교 랭킹 목록을 올바르게 반환한다")
    void getUnivRanking() {
        // when
        List<UnivRankingResponse> univRanking = univRankingRepository.getUnivRanking(0);

        // then
        UnivRankingResponse seoultech = univRanking.stream().filter(u -> u.univName().equals("서울과학기술대학교")).findFirst().get();
        assertThat(seoultech.totalUserCnt()).isEqualTo(2);
        assertThat(seoultech.winCount()).isEqualTo(200);
        assertThat(seoultech.rankerDto().username()).isEqualTo("user2");

        UnivRankingResponse korea = univRanking.stream().filter(u -> u.univName().equals("고려대학교")).findFirst().get();
        assertThat(korea.totalUserCnt()).isEqualTo(1);
        assertThat(korea.winCount()).isEqualTo(100);
        assertThat(korea.rankerDto().username()).isEqualTo("user3");
    }

    @Test
    @DisplayName("getUnivStudentRanking 메서드는 특정 대학교의 학생 랭킹을 올바르게 반환한다")
    void getUnivStudentRanking() {
        // when
        List<UnivStudentRankingResponse> snutUnivStudentRanking = univRankingRepository.getUnivStudentRanking("서울과학기술대학교", 0);

        // then
        assertThat(snutUnivStudentRanking).hasSize(2);
        assertThat(snutUnivStudentRanking.get(0).summonerName()).isEqualTo("summoner2");
        assertThat(snutUnivStudentRanking.get(0).tierInfo().getTier()).isEqualTo(EMERALD);
        assertThat(snutUnivStudentRanking.get(0).topMosts()).containsExactly("champ4", "champ5", "champ6");
        assertThat(snutUnivStudentRanking.get(1).summonerName()).isEqualTo("summoner1");
        assertThat(snutUnivStudentRanking.get(1).tierInfo().getTier()).isEqualTo(GOLD);
        assertThat(snutUnivStudentRanking.get(1).topMosts()).containsExactly("champ1", "champ2", "champ3");

        List<UnivStudentRankingResponse> koreaUnivStudentRanking = univRankingRepository.getUnivStudentRanking("고려대학교", 0);
        assertThat(koreaUnivStudentRanking).hasSize(1);
        assertThat(koreaUnivStudentRanking.get(0).summonerName()).isEqualTo("summoner3");
        assertThat(koreaUnivStudentRanking.get(0).tierInfo().getTier()).isEqualTo(BRONZE);
        assertThat(koreaUnivStudentRanking.get(0).topMosts()).containsExactly("champ7", "champ8", "champ9");

    }
}
