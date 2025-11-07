package maruhxn.rankademy.adapter.integration.labels;

import lombok.extern.slf4j.Slf4j;
import maruhxn.rankademy.domain.user.dto.GetLabelRequest;
import maruhxn.rankademy.domain.user.dto.LabelResponse;
import maruhxn.rankademy.domain.user.service.UserLabelProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Component
public class DefaultLabelProvider implements UserLabelProvider {

    private WebClient labelsClient;

    public DefaultLabelProvider(
            @Value("${labels.url}") String labelsBaseUrl,
            WebClient.Builder webClientBuilder
    ) {
        labelsClient = webClientBuilder.baseUrl(labelsBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public List<String> getLabels(String puuid) {
        GetLabelRequest request = new GetLabelRequest(puuid, 10, 3);

        try {
            LabelResponse response = labelsClient.post()
                    .uri("/labels")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(LabelResponse.class)
                    .block();

            if(response == null || response.labels().isEmpty()) {
                return List.of();
            }

            return response.labels().stream()
                    .map(l -> l.name()).toList();
        }  catch (Exception ex) {
            log.error("Failed to fetch user labels from labels service. request={}", request, ex);
            return List.of();
        }
    }
}
