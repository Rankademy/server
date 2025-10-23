package maruhxn.rankademy.adapter.webapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import maruhxn.rankademy.application.group.provided.dto.GroupSortKey;
import maruhxn.rankademy.application.group.required.GroupRepository;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.group.Group;
import maruhxn.rankademy.domain.group.GroupFixture;
import maruhxn.rankademy.domain.match.service.MostChampionCalculator;
import maruhxn.rankademy.domain.user.ChampionPlayRecord;
import maruhxn.rankademy.domain.user.TierInfo;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static maruhxn.rankademy.domain.user.Rank.I;
import static maruhxn.rankademy.domain.user.Rank.II;
import static maruhxn.rankademy.domain.user.Tier.*;
import static maruhxn.rankademy.domain.user.UserFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@AutoConfigureMockMvc
class OnCampusRankingApiTest extends IntegrationTestSupport {

    static final String BASE_URL = "/api/v1/rankings/univ";

    @Autowired
    MockMvcTester mvcTester;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    GroupRepository groupRepository;

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
    @DisplayName("교내 그룹 랭킹 조회")
    void getGroupRankingList() throws Exception {
        for (int i = 0; i < 30; i++) {
            User leader = GroupFixture.createLeader("leader" + i);
            userRepository.save(leader);

            Group group = GroupFixture.createGroup(leader, "group" + i);
            groupRepository.save(group);
        }

        MvcTestResult result = mvcTester.get().uri(BASE_URL + "/서울과학기술대학교/groups")
                .param("page", "1")
                .param("keyword", "")
                .param("sortKey", GroupSortKey.TIER.name())
                .exchange();

        assertThat(result).hasStatusOk();

//        List<GroupResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
//        });
//        assertThat(response).hasSize(10);
    }

    @Test
    @DisplayName("교내 유저 랭킹 조회")
    void getUnivStudentRanking() throws UnsupportedEncodingException, JsonProcessingException {
        MvcTestResult result = mvcTester.get().uri(BASE_URL + "/서울과학기술대학교")
                .exchange();
        assertThat(result).hasStatusOk();

//        List<UnivStudentRankingResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
//        });
//
//        assertThat(response).hasSize(2);
//        assertThat(response.get(0).summonerName()).isEqualTo("summoner2");
//        assertThat(response.get(0).tierInfo().getTier()).isEqualTo(EMERALD);
//        assertThat(response.get(1).summonerName()).isEqualTo("summoner1");
//        assertThat(response.get(1).tierInfo().getTier()).isEqualTo(GOLD);
    }
}