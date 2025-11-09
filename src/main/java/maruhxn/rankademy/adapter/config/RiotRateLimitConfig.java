package maruhxn.rankademy.adapter.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RiotRateLimitConfig {

    private static final int TOKEN_PER_SECONDS = 18;
    private static final int TOKEN_TWO_MINUTES = 90;

    /**
     * Riot API Rate Limit에 맞는 Bucket 생성
     * - 20 requests per 1 second
     * - 100 requests per 2 minutes
     */
    @Bean
    public Bucket riotApiBucket() {
        Bandwidth perSecondLimit = Bandwidth.builder()
                .capacity(TOKEN_PER_SECONDS)
                .refillIntervally(TOKEN_PER_SECONDS, Duration.ofSeconds(1))
                .build();

        Bandwidth perTwoMinutesLimit = Bandwidth.builder()
                .capacity(TOKEN_TWO_MINUTES)
                .refillIntervally(TOKEN_TWO_MINUTES, Duration.ofMinutes(2))
                .build();

        return Bucket.builder()
                .addLimit(perSecondLimit)
                .addLimit(perTwoMinutesLimit)
                .build();
    }
}
