package maruhxn.rankademy.adapter.config;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.integration.riot.RiotApiRateLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Configuration
@RequiredArgsConstructor
public class RiotClientConfig {

    private static final String HEADER_RIOT_TOKEN = "X-Riot-Token";
    private static final Duration MIN_BACKOFF = Duration.ofMillis(10);
    private static final Duration MAX_ADDITIONAL_JITTER = Duration.ofMillis(25);

    private final WebClient.Builder webClientBuilder;
    private final RiotApiRateLimiter limiter;

    @Value("${riot.api-key}")
    private String API_KEY;

    @Bean
    public WebClient riotClient() {
        return webClientBuilder.clone()
                .defaultHeader(HEADER_RIOT_TOKEN, API_KEY)
                .filter(rateLimitFilter())
                .build();
    }

    private ExchangeFilterFunction rateLimitFilter() {
        return (request, next) -> awaitPermit().then(next.exchange(request));
    }

    private Mono<Void> awaitPermit() {
        return Mono.defer(() -> {
            Duration wait = limiter.waitDurationIfNeeded();
            if (wait.isZero() || wait.isNegative()) return Mono.empty();
            var eff = wait.compareTo(MIN_BACKOFF) < 0 ? MIN_BACKOFF : wait;
            eff = eff.plusMillis(ThreadLocalRandom.current().nextLong(MAX_ADDITIONAL_JITTER.toMillis() + 1));
            return Mono.delay(eff).then(); // 1회 대기
        }).repeat(() -> { // 조건이 true면 반복
            var w = limiter.waitDurationIfNeeded();
            return !w.isZero() && !w.isNegative();
        }).then();
    }
}
