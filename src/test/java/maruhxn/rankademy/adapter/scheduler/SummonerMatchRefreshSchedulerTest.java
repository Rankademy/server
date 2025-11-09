package maruhxn.rankademy.adapter.scheduler;

import jakarta.persistence.EntityManager;
import maruhxn.rankademy.application.match.required.MatchDataRepository;
import maruhxn.rankademy.application.match.required.MatchHistoryCollector;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.match.MatchData;
import maruhxn.rankademy.domain.shared.TimeProvider;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.AfterEach;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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

    @Autowired
    PlatformTransactionManager transactionManager;

    @MockitoBean
    MatchHistoryCollector matchHistoryCollector;

    @MockitoBean
    TimeProvider timeProvider;

    LocalDateTime now;
    TransactionTemplate requiresNewTransaction;
    Long activeUserId;
    Long inactiveUserId;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.of(2025, 10, 1, 12, 0);
        matchDataRepository.deleteAll();
        requiresNewTransaction = new TransactionTemplate(transactionManager);
        requiresNewTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        activeUserId = null;
        inactiveUserId = null;
    }

    @AfterEach
    void tearDown() {
        matchDataRepository.deleteAll();
        if (requiresNewTransaction == null) {
            return;
        }
        requiresNewTransaction.executeWithoutResult(status -> {
            if (activeUserId != null) {
                userRepository.findById(activeUserId).ifPresent(userRepository::delete);
            }
            if (inactiveUserId != null) {
                userRepository.findById(inactiveUserId).ifPresent(userRepository::delete);
            }
            em.flush();
            em.clear();
        });
    }

    @Test
    void refreshMatchHistory() {
        ReflectionTestUtils.setField(scheduler, "running", new AtomicBoolean(false));

        prepareUsers();

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
        ReflectionTestUtils.setField(scheduler, "running", new AtomicBoolean(true));

        scheduler.refreshMatchHistory();

        verify(matchHistoryCollector, never()).collectMatchesWithLastMatchId(any(User.class), any());
    }

    private void prepareUsers() {
        requiresNewTransaction.executeWithoutResult(status -> {
            User activeUser = createUser("active@rankademy.app", "active");
            activeUser.completeUnivAuthentication(createEnrollUnivRequest());
            activeUser.connectSummonerInfo(createSummonerInfoConnector("active-puuid"), createRiotAuthRequest());
            ReflectionTestUtils.setField(activeUser, "lastLoginAt", now.minusDays(1));

            User inactiveUser = createUser("inactive@rankademy.app", "inactive");
            inactiveUser.completeUnivAuthentication(createEnrollUnivRequest());
            inactiveUser.connectSummonerInfo(createSummonerInfoConnector("inactive-puuid"), createRiotAuthRequest("inactive", "KR1"));
            ReflectionTestUtils.setField(inactiveUser, "lastLoginAt", now.minusDays(30));

            userRepository.save(activeUser);
            userRepository.save(inactiveUser);
            em.flush();
            em.clear();
            activeUserId = activeUser.getId();
            inactiveUserId = inactiveUser.getId();
        });
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
