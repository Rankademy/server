package maruhxn.rankademy.application.match.required;

import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.match.MatchData;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.support.IntegrationTestSupport;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static maruhxn.rankademy.domain.user.UserFixture.*;
import static org.assertj.core.api.Assertions.assertThat;

@Disabled
class MatchHistoryCollectorTest extends IntegrationTestSupport {

    @Autowired
    private MatchHistoryCollector matchHistoryCollector;

    @Autowired
    private UserRepository userRepository;

    @Test
    void collectMatchesWithLastMatchId() {
        // given
        User user = createUser();
        user.completeUnivAuthentication(createEnrollUnivRequest());
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