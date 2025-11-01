package maruhxn.rankademy.adapter.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maruhxn.rankademy.application.match.MatchHistoryService;
import maruhxn.rankademy.application.user.provided.UserReader;
import maruhxn.rankademy.domain.shared.TimeProvider;
import maruhxn.rankademy.domain.user.User;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class SummonerMatchRefreshScheduler {

    private final MatchHistoryService matchHistoryService;
    private final UserReader userReader;
    private final TimeProvider timeProvider;
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Transactional
    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.MINUTES)
    public void refreshMatchHistory() {
        if (!running.compareAndSet(false, true)) {
            log.warn("[전적 자동 갱신 배치] - 이전 실행이 종료되지 않아 이번 라운드를 건너뜁니다.");
            return;
        }

        log.info("[전적 자동 갱신 배치] - 시작");
        // 최근 활동 기록이 있는 유저 리스트 조회
        List<User> activeUsers = userReader.findActiveUsers(timeProvider.getCurrentTime());
        log.info("[전적 자동 갱신 배치] - 활성 유저 수: {}", activeUsers.size());

        // 유저 전적 갱신
        activeUsers.forEach(u -> matchHistoryService.refreshMatches(u.getId()));
        log.info("[전적 자동 갱신 배치 종료]");

        running.set(false);
    }
}
