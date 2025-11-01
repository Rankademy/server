package maruhxn.rankademy.adapter.scheduler;

import jakarta.persistence.EntityManager;
import maruhxn.rankademy.application.match.required.MatchDataRepository;
import maruhxn.rankademy.application.match.required.MatchHistoryCollector;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.match.MatchData;
import maruhxn.rankademy.domain.shared.TimeProvider;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.support.IntegrationTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static maruhxn.rankademy.domain.user.UserFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("스케줄러 - SummonerMatchRefreshScheduler 테스트")
class SummonerMatchRefreshSchedulerTest extends IntegrationTestSupport {

    @Autowired
    SummonerMatchRefreshScheduler scheduler;

    @Autowired
    UserRepository userRepository;

    @Autowired
    MatchDataRepository matchDataRepository;

    @Autowired
    EntityManager em;

    @MockitoBean
    MatchHistoryCollector matchHistoryCollector;

    @MockitoBean
    TimeProvider timeProvider;

    LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.of(2025, 10, 1, 12, 0);
        matchDataRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        matchDataRepository.deleteAll();
    }

    @Test
    void refreshMatchHistory() {
        User activeUser = createUser("active@rankademy.app", "active");
        activeUser.enrollUnivInfo(createEnrollUnivRequest());
        activeUser.completeUnivAuthentication();
        activeUser.connectSummonerInfo(createSummonerInfoConnector("active-puuid"), createRiotAuthRequest());
        ReflectionTestUtils.setField(activeUser, "lastLoginAt", now.minusDays(1));

        User inactiveUser = createUser("inactive@rankademy.app", "inactive");
        inactiveUser.enrollUnivInfo(createEnrollUnivRequest("대학교", "inactive@univ.app"));
        inactiveUser.completeUnivAuthentication();
        inactiveUser.connectSummonerInfo(createSummonerInfoConnector("inactive-puuid"), createRiotAuthRequest("inactive", "KR1"));
        ReflectionTestUtils.setField(inactiveUser, "lastLoginAt", now.minusDays(30));

        userRepository.save(activeUser);
        userRepository.save(inactiveUser);
        em.flush();
        Long activeUserId = activeUser.getId();
        Long inactiveUserId = inactiveUser.getId();
        em.clear();

        when(timeProvider.getCurrentTime()).thenReturn(now);
        when(matchHistoryCollector.collectMatchesWithLastMatchId(any(User.class), isNull()))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    if (user.getId().equals(activeUserId)) {
                        return List.of(
                                MatchData.builder().matchId("match-1").userId(activeUserId).jsonData(sampleMatchJson("active-puuid")).build(),
                                MatchData.builder().matchId("match-2").userId(activeUserId).jsonData(sampleMatchJson("active-puuid")).build()
                        );
                    }
                    return List.of();
                });

        // when
        scheduler.refreshMatchHistory();

        // then
        verify(matchHistoryCollector, times(1))
                .collectMatchesWithLastMatchId(argThat(user -> user.getId().equals(activeUserId)), isNull());

        List<MatchData> stored = matchDataRepository.findAllByUserId(activeUserId);
        assertThat(stored).hasSize(2);
        assertThat(matchDataRepository.findAllByUserId(inactiveUserId)).isEmpty();
    }

    @Test
    void skipIfPreviousRunInProgress() {
        ReflectionTestUtils.setField(scheduler, "running", new java.util.concurrent.atomic.AtomicBoolean(true));

        scheduler.refreshMatchHistory();

        verify(matchHistoryCollector, never()).collectMatchesWithLastMatchId(any(User.class), any());
    }

    private String sampleMatchJson(String puuid) {
        return """
                {
                  "info": {
                    "participants": [
                      {
                        "puuid": "%s",
                        "championName": "Ahri"
                      }
                    ]
                  }
                }
                """.formatted(puuid);
    }
}
