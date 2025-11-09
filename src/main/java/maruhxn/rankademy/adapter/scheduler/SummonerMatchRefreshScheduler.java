package maruhxn.rankademy.adapter.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maruhxn.rankademy.application.match.provided.MatchHistoryAnalyzer;
import maruhxn.rankademy.application.user.provided.UserReader;
import maruhxn.rankademy.domain.shared.TimeProvider;
import maruhxn.rankademy.domain.user.User;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class SummonerMatchRefreshScheduler {

    private final MatchHistoryAnalyzer matchHistoryAnalyzer;
    private final UserReader userReader;
    private final TimeProvider timeProvider;
    private final AtomicBoolean running = new AtomicBoolean(false);

    // 동시에 갱신 중인 유저를 추적
    private final Set<Long> refreshingUsers = ConcurrentHashMap.newKeySet();

    // 배치 처리 동시성 제한 (Rate Limit 고려)
    // 각 유저가 여러 API 호출을 하므로 동시 처리 유저 수를 최소화
    private static final int BATCH_CONCURRENCY = 1;

    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.MINUTES)
    public void refreshMatchHistory() {
        if (!running.compareAndSet(false, true)) {
            log.warn("[전적 자동 갱신 배치] - 이전 실행이 종료되지 않아 이번 라운드를 건너뜁니다.");
            return;
        }

        try {
            executeRefresh();
        } finally {
            running.set(false);
        }
    }

    private void executeRefresh() {
        log.info("[전적 자동 갱신 배치] - 시작");
        long startTime = System.currentTimeMillis();

        // 최근 활동 기록이 있는 유저 리스트 조회 (트랜잭션 분리)
        List<User> activeUsers = userReader.findActiveUsers(timeProvider.getCurrentTime());
        log.info("[전적 자동 갱신 배치] - 활성 유저 수: {}", activeUsers.size());

        if (activeUsers.isEmpty()) {
            log.info("[전적 자동 갱신 배치] - 갱신할 유저가 없습니다.");
            return;
        }

        // 통계 추적
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicInteger skippedCount = new AtomicInteger(0);

        // 병렬 처리하되 동시성 제한 (Rate Limit 고려)
        try {
            Flux.fromIterable(activeUsers)
                    .flatMap(user -> processUserRefresh(user, successCount, failureCount, skippedCount),
                            BATCH_CONCURRENCY)
                    .collectList()
                    .block();
        } catch (Exception e) {
            log.error("[전적 자동 갱신 배치] - 예상치 못한 오류 발생", e);
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("[전적 자동 갱신 배치 종료] - 성공: {}, 실패: {}, 스킵: {}, 소요시간: {}ms",
                successCount.get(), failureCount.get(), skippedCount.get(), duration);
    }

    private Mono<Void> processUserRefresh(
            User user,
            AtomicInteger successCount,
            AtomicInteger failureCount,
            AtomicInteger skippedCount
    ) {
        return Mono.fromRunnable(() -> {
                    Long userId = user.getId();

                    // 이미 갱신 중인 유저는 스킵 (수동 갱신과의 충돌 방지)
                    if (!refreshingUsers.add(userId)) {
                        log.debug("[전적 자동 갱신] - userId: {} 이미 갱신 중, 스킵", userId);
                        skippedCount.incrementAndGet();
                        return;
                    }

                    try {
                        matchHistoryAnalyzer.refreshMatches(userId);
                        successCount.incrementAndGet();
                        log.debug("[전적 자동 갱신] - userId: {} 갱신 완료", userId);
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                        log.error("[전적 자동 갱신] - userId: {} 갱신 실패", userId, e);
                    } finally {
                        refreshingUsers.remove(userId);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    /**
     * 수동 갱신 요청 시 사용할 메서드
     * 배치와 동시 실행 방지
     */
    public boolean tryAcquireRefreshLock(Long userId) {
        return refreshingUsers.add(userId);
    }

    /**
     * 수동 갱신 완료 후 락 해제
     */
    public void releaseRefreshLock(Long userId) {
        refreshingUsers.remove(userId);
    }
}