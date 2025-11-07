package maruhxn.rankademy.adapter.integration.match_making;

import lombok.extern.slf4j.Slf4j;
import maruhxn.rankademy.application.team.dto.request.AdjustMmrRequest;
import maruhxn.rankademy.application.team.dto.request.GetTeamMmrRequest;
import maruhxn.rankademy.application.team.dto.response.AdjustedMmrResponse;
import maruhxn.rankademy.application.team.dto.response.TeamMmrResponse;
import maruhxn.rankademy.application.team.required.MatchMakingClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class DefaultMatchMakingClient implements MatchMakingClient {

    private WebClient matchMakingClient;

    public DefaultMatchMakingClient(
            @Value("${matchmaking.url}") String matchMakingBaseUrl,
            WebClient.Builder webClientBuilder
    ) {
        this.matchMakingClient = webClientBuilder.clone()
                .baseUrl(matchMakingBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public TeamMmrResponse getTeamMmr(GetTeamMmrRequest request) {
        try {
            TeamMmrResponse response = matchMakingClient.post()
                    .uri("/team-mmr")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(TeamMmrResponse.class)
                    .block();
            return response != null ? response : TeamMmrResponse.empty();
        } catch (Exception ex) {
            log.error("Failed to fetch team MMR from matchmaking service. request={}", request, ex);
            return TeamMmrResponse.empty();
        }
    }

    @Override
    public AdjustedMmrResponse adjustUsersMmr(AdjustMmrRequest request) {
        try {
            AdjustedMmrResponse response = matchMakingClient.post()
                    .uri("/match")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(AdjustedMmrResponse.class)
                    .block();
            return response != null ? response : AdjustedMmrResponse.empty();
        } catch (Exception ex) {
            log.error("Failed to adjust users' MMR via matchmaking service. request={}", request, ex);
            return AdjustedMmrResponse.empty();
        }
    }
}
