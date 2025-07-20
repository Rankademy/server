package maruhxn.rankademy.domain.user.service;

import maruhxn.rankademy.domain.user.SummonerInfo;
import maruhxn.rankademy.domain.user.dto.RiotAuthRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static maruhxn.rankademy.domain.user.UserFixture.createSummonerInfoConnector;
import static org.assertj.core.api.Assertions.assertThat;

class SummonerInfoConnectorTest {

    SummonerInfoConnector summonerInfoConnector;

    @BeforeEach
    void setUp() {
        summonerInfoConnector = createSummonerInfoConnector();
    }

    @Test
    void connect() {
        RiotAuthRequest request = new RiotAuthRequest("maruhxn", "KOR");
        SummonerInfo summonerInfo = summonerInfoConnector.connect(request);

        assertThat(summonerInfo.getSummonerName()).isEqualTo(request.summonerName());
        assertThat(summonerInfo.getSummonerTag()).isEqualTo(request.summonerTag());
    }
}