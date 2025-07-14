package maruhxn.rankademy.domain.member.service;

import maruhxn.rankademy.domain.member.SummonerInfo;
import maruhxn.rankademy.domain.member.dto.RiotAuthRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static maruhxn.rankademy.domain.member.MemberFixture.createSummonerInfoConnector;
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