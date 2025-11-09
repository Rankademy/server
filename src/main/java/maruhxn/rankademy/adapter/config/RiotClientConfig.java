package maruhxn.rankademy.adapter.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RiotClientConfig {

    private static final String HEADER_RIOT_TOKEN = "X-Riot-Token";

    private final WebClient.Builder webClientBuilder;

    @Value("${riot.api-key}")
    private String API_KEY;

    @Bean
    public WebClient riotClient() {
        return webClientBuilder.clone()
                .defaultHeader(HEADER_RIOT_TOKEN, API_KEY)
                .build();
    }
}
