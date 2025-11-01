package maruhxn.rankademy.adapter.integration.riot;

import maruhxn.rankademy.adapter.integration.riot.dto.RiotAccountDto;
import maruhxn.rankademy.adapter.integration.riot.dto.RiotSummonerDto;
import maruhxn.rankademy.adapter.util.RiotApiUrlBuilder;
import maruhxn.rankademy.application.user.required.RiotApiProvider;
import maruhxn.rankademy.domain.match.MatchData;
import maruhxn.rankademy.domain.user.dto.RiotAuthRequest;
import maruhxn.rankademy.domain.user.dto.RiotLeagueEntryResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.util.List;

@Component
public class RiotApiProviderImpl implements RiotApiProvider {

    private static final String HEADER_RIOT_TOKEN = "X-Riot-Token";

    private final WebClient riotClient;

    public RiotApiProviderImpl(Environment environment) {
        String apiKey = environment.getProperty("riot.api-key");
        this.riotClient = WebClient.builder()
                .defaultHeader(HEADER_RIOT_TOKEN, apiKey)
                .build();
    }

    @Override
    public String getPuuid(RiotAuthRequest riotAuthRequest) {
        String url = RiotApiUrlBuilder.getAccountInfoUrl(riotAuthRequest.summonerName(), riotAuthRequest.summonerTag());
        try {
            RiotAccountDto riotAccountDto = fetchOne(url, RiotAccountDto.class);
            return riotAccountDto.puuid();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getSummonerIconId(String puuid) {
        String url = RiotApiUrlBuilder.getSummonerInfoUrl(puuid);
        try {
            RiotSummonerDto riotSummonerDto = fetchOne(url, RiotSummonerDto.class);
            return riotSummonerDto.profileIconId();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RiotLeagueEntryResponse getSoloRankInfoByPuuid(String puuid) {
        String url = RiotApiUrlBuilder.getLeagueInfoUrl(puuid);
        try {
            List<RiotLeagueEntryResponse> leagueInfos = fetchList(
                    url,
                    new ParameterizedTypeReference<>() {
                    }
            );

            return leagueInfos.stream()
                    .filter(li -> li.queueType().contains("RANKED_SOLO"))
                    .findFirst().get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getMatchIds(String puuid, Long startTime, int start, int chunkSize) {
        String url = RiotApiUrlBuilder.getMatchIdsUrl(puuid, startTime, start, chunkSize);
        try {
            return fetchList(url, new ParameterizedTypeReference<>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MatchData getMatchInfo(String matchId, String puuid, Long userId) {
        String url = RiotApiUrlBuilder.getMatchInfoUrl(matchId);

        String json = riotClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return new MatchData(
                matchId,
                userId,
                puuid,
                json
        );
    }

    private <T> T fetchOne(String url, Class<T> clazz) throws IOException {
        try {
            return riotClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(clazz)
                    .blockOptional()
                    .orElseThrow(() -> new IOException("Empty response for " + clazz.getSimpleName()));
        } catch (WebClientResponseException e) {
            throw new IOException("Failed to fetch " + clazz.getSimpleName() + ": " + e.getStatusCode(), e);
        }
    }

    private <T> List<T> fetchList(String url, ParameterizedTypeReference<List<T>> typeRef) throws IOException {
        try {
            return riotClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(typeRef)
                    .blockOptional()
                    .orElseThrow(() -> new IOException("Empty response for list at " + url));
        } catch (WebClientResponseException e) {
            throw new IOException("Failed to fetch list at " + url + ": " + e.getStatusCode(), e);
        }
    }
}
