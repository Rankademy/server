package maruhxn.rankademy.adapter.integration.riot;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.integration.riot.dto.RiotAccountDto;
import maruhxn.rankademy.adapter.integration.riot.dto.RiotSummonerDto;
import maruhxn.rankademy.adapter.util.RiotApiUrlBuilder;
import maruhxn.rankademy.application.user.required.RiotApiProvider;
import maruhxn.rankademy.domain.match.MatchData;
import maruhxn.rankademy.domain.user.dto.RiotAuthRequest;
import maruhxn.rankademy.domain.user.dto.RiotLeagueEntryResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RiotApiProviderImpl implements RiotApiProvider {

    private final WebClient riotClient;

    @Override
    public String getPuuid(RiotAuthRequest riotAuthRequest) {
        String url = RiotApiUrlBuilder.getAccountInfoUrl(riotAuthRequest.summonerName(), riotAuthRequest.summonerTag());
        RiotAccountDto riotAccountDto = fetchOne(url, RiotAccountDto.class);
        return riotAccountDto.puuid();
    }

    @Override
    public int getSummonerIconId(String puuid) {
        String url = RiotApiUrlBuilder.getSummonerInfoUrl(puuid);
        RiotSummonerDto riotSummonerDto = fetchOne(url, RiotSummonerDto.class);
        return riotSummonerDto.profileIconId();
    }

    @Override
    public RiotLeagueEntryResponse getSoloRankInfoByPuuid(String puuid) {
        String url = RiotApiUrlBuilder.getLeagueInfoUrl(puuid);
        List<RiotLeagueEntryResponse> leagueInfos = fetchList(url, new ParameterizedTypeReference<>() {
        });

        return leagueInfos.stream()
                .filter(li -> li.queueType().contains("RANKED_SOLO"))
                .findFirst().get();
    }

    @Override
    public List<String> getMatchIds(String puuid, Long startTime, int start, int chunkSize) {
        String url = RiotApiUrlBuilder.getMatchIdsUrl(puuid, startTime, start, chunkSize);
        return fetchList(url, new ParameterizedTypeReference<>() {
        });
    }

    @Override
    public Mono<MatchData> getMatchInfo(String matchId, String puuid, Long userId) {
        String url = RiotApiUrlBuilder.getMatchInfoUrl(matchId);

        return riotClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .map(json -> new MatchData(
                        matchId,
                        userId,
                        puuid,
                        json
                ));
    }

    private <T> T fetchOne(String url, Class<T> clazz) {
        try {
            return riotClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(clazz)
                    .blockOptional()
                    .orElseThrow(() -> new RuntimeException("Empty response for " + clazz.getSimpleName()));
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Failed to fetch " + clazz.getSimpleName() + ": " + e.getStatusCode(), e);
        }
    }

    private <T> List<T> fetchList(String url, ParameterizedTypeReference<List<T>> typeRef) {
        try {
            return riotClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(typeRef)
                    .blockOptional()
                    .orElseThrow(() -> new RuntimeException("Empty response for list at " + url));
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Failed to fetch list at " + url + ": " + e.getStatusCode(), e);
        }
    }
}
