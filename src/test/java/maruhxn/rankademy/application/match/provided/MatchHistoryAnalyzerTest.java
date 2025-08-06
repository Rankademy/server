package maruhxn.rankademy.application.match.provided;

import jakarta.persistence.EntityManager;
import maruhxn.rankademy.RankademyTestConfiguration;
import maruhxn.rankademy.application.match.required.MatchDataRepository;
import maruhxn.rankademy.application.match.required.MatchHistoryCollector;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.match.MatchData;
import maruhxn.rankademy.domain.user.User;
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
import java.util.List;
import java.util.UUID;

import static maruhxn.rankademy.domain.user.UserFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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
    EntityManager em;

    @MockitoBean
    private MatchDataRepository matchDataRepository;

    @MockitoBean
    MatchHistoryCollector matchHistoryCollector;

    @AfterEach
    void tearDown() {
        matchDataRepository.deleteAll();
    }

    @Test
    void fetchAndAnalyzeMatches() throws IOException {
        User user = createUser();
        user.enrollUnivInfo(createEnrollUnivRequest());
        user.completeUnivAuthentication();
        user.connectSummonerInfo(createSummonerInfoConnector("MfiVjqqTLQ_XhERTcyHydIdiFmlQhK9zNTfKSel_DECSZHGgTIITI7QmHGGaPDbpjlPVOqAahCtHzA"), createRiotAuthRequest());
        userRepository.save(user);
        em.flush();
        em.clear();

        Long userId = user.getId();
        String jsonData = getMatchJsonData();

        List<MatchData> mockMatches = Arrays.asList(
                MatchData.builder()
                        .matchId(UUID.randomUUID().toString())
                        .userId(userId)
                        .jsonData(jsonData)
                        .build());

        when(matchHistoryCollector.collectAllMatches(user))
                .thenReturn(mockMatches);

        // matchDataRepository.saveAll() 호출을 모킹
        when(matchDataRepository.saveAll(any())).thenReturn(mockMatches);

        // matchDataRepository.findAll() 호출을 모킹
        when(matchDataRepository.findAll()).thenReturn(mockMatches);

        matchHistoryAnalyzer.fetchAndAnalyzeMatches(userId);
        em.flush();
        em.clear();

        user = userRepository.findById(userId).get();

        // MongoDB 저장 로직이 호출되었는지 검증
        verify(matchDataRepository).saveAll(mockMatches);

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
