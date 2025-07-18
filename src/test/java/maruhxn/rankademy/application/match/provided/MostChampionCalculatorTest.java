package maruhxn.rankademy.application.match.provided;

import jakarta.persistence.EntityManager;
import maruhxn.rankademy.RankademyTestConfiguration;
import maruhxn.rankademy.domain.match.ChampionPlayRecord;
import maruhxn.rankademy.domain.match.MatchData;
import maruhxn.rankademy.domain.match.service.MostChampionCalculator;
import maruhxn.rankademy.domain.match.service.MyChampionIdParser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
@Import(RankademyTestConfiguration.class)
class MostChampionCalculatorTest {

    @Autowired
    MostChampionCalculator calculator;

    @Autowired
    EntityManager em;

    @MockitoBean
    MyChampionIdParser myChampionIdParser;

    @Test
    void calculateMostChampion() {
        Long memberId = 1L;
        String puuid = UUID.randomUUID().toString();
        List<MatchData> matches = new ArrayList<>();

        when(myChampionIdParser.parse(any(), any()))
                .thenReturn("Riven", "Yasuo", "Irelia", "Riven", "Riven", "Yasuo", "Teemo", "Riven", "Yasuo", "Irelia");

        for (int i = 0; i < 10; i++) {
            MatchData matchData = MatchData.builder()
                    .memberId(memberId)
                    .matchId(UUID.randomUUID().toString())
                    .jsonData("jsonData")
                    .build();
            matches.add(matchData);
        }


        List<ChampionPlayRecord> most3Champions = calculator.calculateMostChampionsTop3(matches, puuid);
        assertThat(most3Champions).hasSize(3);
        assertThat(most3Champions.get(0).championId()).isEqualTo("Riven");
        assertThat(most3Champions.get(1).championId()).isEqualTo("Yasuo");
        assertThat(most3Champions.get(2).championId()).isEqualTo("Irelia");
    }
}