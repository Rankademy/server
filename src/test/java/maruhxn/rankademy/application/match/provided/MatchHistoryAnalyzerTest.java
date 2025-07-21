package maruhxn.rankademy.application.match.provided;

import jakarta.persistence.EntityManager;
import maruhxn.rankademy.RankademyTestConfiguration;
import maruhxn.rankademy.application.match.required.MatchDataRepository;
import maruhxn.rankademy.application.match.required.MatchHistoryCollector;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.match.MatchData;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.domain.user.UserFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
@Import(RankademyTestConfiguration.class)
class MatchHistoryAnalyzerTest {

    @Autowired
    MatchHistoryAnalyzer matchHistoryAnalyzer;

    @Autowired
    UserRepository userRepository;

    @Autowired
    MatchDataRepository matchDataRepository;

    @Autowired
    EntityManager em;

    @MockitoBean
    MatchHistoryCollector matchHistoryCollector;

    @AfterEach
    void tearDown() {
        matchDataRepository.deleteAll();
    }

    @Test
    void fetchAndAnalyzeMatches() throws IOException {
        User user = UserFixture.createUser();
        user.enrollUnivInfo(UserFixture.createEnrollUnivRequest());
        user.completeUnivAuthentication();
        user.connectSummonerInfo(UserFixture.createSummonerInfoConnector(), UserFixture.createRiotAuthRequest());
        userRepository.save(user);
        em.flush();
        em.clear();

        Long userId = user.getId();

        String jsonData = getMatchJsonData();

        when(matchHistoryCollector.collectAllMatches(user))
                .thenReturn(Arrays.asList(
                        MatchData.builder()
                                .matchId(UUID.randomUUID().toString())
                                .userId(userId)
                                .jsonData(jsonData)
                                .build()));

        matchHistoryAnalyzer.fetchAndAnalyzeMatches(userId);
        em.flush();
        em.clear();

        user = userRepository.findById(userId).get();
        assertThat(matchDataRepository.findAll()).isNotEmpty();
        assertThat(user.getSummonerInfo().getMostChampions()).hasSize(1);
    }

    private static String getMatchJsonData() throws IOException {
        ClassPathResource resource = new ClassPathResource("match-data.json");
        String jsonData;
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            jsonData = FileCopyUtils.copyToString(reader);
        }
        return jsonData;
    }
}
