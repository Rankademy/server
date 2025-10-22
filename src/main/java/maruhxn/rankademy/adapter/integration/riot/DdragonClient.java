package maruhxn.rankademy.adapter.integration.riot;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.util.List;

@Component
public class DdragonClient {

    private final WebClient ddragonClient;

    private static final String VERSION_CHECK_URI = "/api/versions.json";

    public DdragonClient() {
        this.ddragonClient = WebClient.builder()
                .baseUrl("https://ddragon.leagueoflegends.com")
                .build();
    }

    public String getLatestVersion() throws IOException {
        try {
            List<String> versions = ddragonClient.get()
                    .uri(VERSION_CHECK_URI)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<String>>() {})
                    .block();

            if (versions != null && !versions.isEmpty()) {
                return versions.get(0);
            } else {
                throw new IOException("[ddragon 최신 버전 조회 실패] 버전 목록이 비어있습니다.");
            }
        } catch (WebClientResponseException e) {
            throw new IOException("[ddragon 최신 버전 조회 실패] statusCode: " + e.getStatusCode(), e);
        }
    }
}
