package maruhxn.rankademy.application.match.required;

import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.match.MatchData;
import maruhxn.rankademy.domain.user.User;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static maruhxn.rankademy.domain.user.UserFixture.*;
import static org.assertj.core.api.Assertions.assertThat;

@Disabled
@SpringBootTest(properties = "de.flapdoodle.mongodb.embedded.version=8.0.12")
class MatchHistoryCollectorTest {

    @Autowired
    private MatchHistoryCollector matchHistoryCollector;

    @Autowired
    private UserRepository userRepository;

    @Test
    void collectMatchesWithLastMatchId() {
        // given
        User user = createUser();
        user.enrollUnivInfo(createEnrollUnivRequest());
        user.completeUnivAuthentication();
        user.connectSummonerInfo(createSummonerInfoConnector("MfiVjqqTLQ_XhERTcyHydIdiFmlQhK9zNTfKSel_DECSZHGgTIITI7QmHGGaPDbpjlPVOqAahCtHzA"), createRiotAuthRequest());

        String lastMatchId = "KR_7690698299";
        user.getSummonerInfo().updateMatchSyncStatus(lastMatchId);

        userRepository.save(user);

        // when
        List<MatchData> matchData = matchHistoryCollector.collectMatchesWithLastMatchId(user, lastMatchId);

        //
        assertThat(matchData.size()).isEqualTo(10);
    }
}