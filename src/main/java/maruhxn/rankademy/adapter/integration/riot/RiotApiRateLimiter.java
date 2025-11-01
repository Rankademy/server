package maruhxn.rankademy.adapter.integration.riot;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RiotApiRateLimiter {

    private static final int PER_SECOND_TOKEN = 20;
    private static final int TWO_MINUTES_TOKEN = 100;
    private final Bucket bucket;

    public RiotApiRateLimiter() {
        Bandwidth perSecond = Bandwidth.builder()
                .capacity(PER_SECOND_TOKEN)
                .refillGreedy(PER_SECOND_TOKEN, Duration.ofSeconds(1))
                .build();

        Bandwidth per2Minutes = Bandwidth.builder()
                .capacity(TWO_MINUTES_TOKEN)
                .refillGreedy(TWO_MINUTES_TOKEN, Duration.ofMinutes(2))
                .build();

        this.bucket = Bucket.builder()
                .addLimit(perSecond)
                .addLimit(per2Minutes)
                .build();
    }

    /**
     * 비동기: 남은 시간 계산해서 기다릴 Duration 반환
     */
    public Duration waitDurationIfNeeded() {
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            return Duration.ZERO;
        } else {
            return Duration.ofNanos(probe.getNanosToWaitForRefill());
        }
    }
}
