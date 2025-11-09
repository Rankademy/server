package maruhxn.rankademy.adapter.integration.riot;

import io.github.bucket4j.Bucket;
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
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RiotApiProviderImpl implements RiotApiProvider {

    private final WebClient riotClient;
    private final Bucket rateLimitBucket;

    /**
     * Rate Limit을 고려하여 API 호출 전 토큰 획득
     * 토큰을 얻을 때까지 블로킹
     */
    private void acquireToken() {
        try {
            rateLimitBucket.asBlocking().consume(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Rate limit token acquisition interrupted", e);
        }
    }

    /**
     * Rate Limit을 고려하여 비동기 API 호출 전 토큰 획득
     * Mono로 래핑하여 리액티브 체인에 통합
     */
    private Mono<Void> acquireTokenAsync() {
        return Mono.<Void>fromCallable(() -> {
            rateLimitBucket.asBlocking().consume(1);
            return null;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public String getPuuid(RiotAuthRequest riotAuthRequest) {
        acquireToken();
        String url = RiotApiUrlBuilder.getAccountInfoUrl(
                riotAuthRequest.summonerName(),
                riotAuthRequest.summonerTag()
        );
        RiotAccountDto riotAccountDto = fetchOne(url, RiotAccountDto.class);
        return riotAccountDto.puuid();
    }

    @Override
    public int getSummonerIconId(String puuid) {
        acquireToken();
        String url = RiotApiUrlBuilder.getSummonerInfoUrl(puuid);
        RiotSummonerDto riotSummonerDto = fetchOne(url, RiotSummonerDto.class);
        return riotSummonerDto.profileIconId();
    }

    @Override
    public RiotLeagueEntryResponse getSoloRankInfoByPuuid(String puuid) {
        acquireToken();
        String url = RiotApiUrlBuilder.getLeagueInfoUrl(puuid);
        List<RiotLeagueEntryResponse> leagueInfos = fetchList(
                url,
                new ParameterizedTypeReference<>() {}
        );

        return leagueInfos.stream()
                .filter(li -> li.queueType().contains("RANKED_SOLO"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No RANKED_SOLO data found"));
    }

    @Override
    public List<String> getMatchIds(String puuid, Long startTime, int start, int chunkSize) {
        acquireToken();
        String url = RiotApiUrlBuilder.getMatchIdsUrl(puuid, startTime, start, chunkSize);
        return fetchList(url, new ParameterizedTypeReference<>() {});
    }

    @Override
    public Mono<MatchData> getMatchInfo(String matchId, String puuid, Long userId) {
        String url = RiotApiUrlBuilder.getMatchInfoUrl(matchId);

        return acquireTokenAsync()
                .then(riotClient.get()
                        .uri(url)
                        .retrieve()
                        .bodyToMono(String.class)
                        .map(json -> new MatchData(
                                matchId,
                                userId,
                                puuid,
                                json
                        ))
                );
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
            throw new RuntimeException(
                    "Failed to fetch " + clazz.getSimpleName() + ": " + e.getStatusCode(),
                    e
            );
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
            throw new RuntimeException(
                    "Failed to fetch list at " + url + ": " + e.getStatusCode(),
                    e
            );
        }
    }
}
