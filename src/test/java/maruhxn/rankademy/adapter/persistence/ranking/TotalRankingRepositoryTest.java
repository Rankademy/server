package maruhxn.rankademy.adapter.persistence.ranking;

import jakarta.persistence.EntityManager;
import maruhxn.rankademy.adapter.webapi.dto.UnivRankingResponse;
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
class TotalRankingRepositoryTest {

    @Autowired
    TotalRankingRepository totalRankingRepository;

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
    @DisplayName("getUnivRanking 메서드는 대학교 랭킹 목록을 올바르게 반환한다")
    void getUnivRanking() {
        // when
        List<UnivRankingResponse> univRanking = totalRankingRepository.getUnivRanking(0);

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
}