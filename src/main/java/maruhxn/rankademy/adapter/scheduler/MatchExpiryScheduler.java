package maruhxn.rankademy.adapter.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maruhxn.rankademy.application.competition.required.CompetitionRepository;
import maruhxn.rankademy.domain.competition.CompetitionStatus;
import maruhxn.rankademy.domain.shared.TimeProvider;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchExpiryScheduler {

    private final CompetitionRepository competitionRepository;
    private final TimeProvider timeProvider;

    /**
     * 추후 Spring Batch 도입하고, 만료 이벤트 -> Notification 전송 로직으로 변경 필요 (or 바로 notification 생성)
     */
    @Scheduled(cron = "0 0 * * * *") // 매 정시
    public void markExpiredMatches() {
        LocalDateTime now = timeProvider.getCurrentTime();
        int updated = competitionRepository.bulkUpdateStatusIfExpired(
                CompetitionStatus.SCHEDULED,
                CompetitionStatus.EXPIRED,
                now
        );

        if (updated > 0) {
            log.info("{}개의 대항전 매치가 만료되었습니다.", updated);
        }
    }
}
