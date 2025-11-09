package maruhxn.rankademy.adapter.integration.riot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maruhxn.rankademy.application.match.required.MatchHistoryCollector;
import maruhxn.rankademy.application.user.required.RiotApiProvider;
import maruhxn.rankademy.domain.match.MatchData;
import maruhxn.rankademy.domain.user.User;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class RiotApiMatchHistoryCollector implements MatchHistoryCollector {

    private static final Long SEASON_START_TIME = LocalDate.of(2025, 1, 9)
            .atStartOfDay(ZoneOffset.UTC).toEpochSecond();

    private static final int CHUNK_SIZE = 10;
    private static final int MATCH_FETCH_CONCURRENCY = 3; // 5에서 3으로 감소

    private final RiotApiProvider riotApiProvider;

    @Override
    public List<MatchData> collectMatchesWithLastMatchId(User user, String lastMatchId) {
        String puuid = user.getSummonerInfo().getPuuid();

        List<String> matchIds;

        if (Objects.isNull(lastMatchId)) {
            matchIds = new ArrayList<>(getAllMatchIds(puuid, user.getSummonerInfo().getTotalMatchCnt()));
        } else {
            matchIds = new ArrayList<>(getIncrementalMatchIds(puuid, lastMatchId));
            if (matchIds.isEmpty()) {
                return List.of();
            }
        }

        log.info("Collected {} match IDs for user {}", matchIds.size(), user.getId());

        return fetchMatches(user.getId(), puuid, matchIds);
    }

    private List<String> getAllMatchIds(String puuid, int totalMatchCnt) {
        int r = totalMatchCnt / CHUNK_SIZE;
        int remain = totalMatchCnt % CHUNK_SIZE;

        List<String> allMatchIds = new ArrayList<>();

        log.debug("Fetching {} chunks of match IDs (total: {})", r + 1, totalMatchCnt);

        for (int i = 0; i < r + 1; i++) {
            int start = i * CHUNK_SIZE;
            if (i != r) {
                allMatchIds.addAll(riotApiProvider.getMatchIds(puuid, SEASON_START_TIME, start, CHUNK_SIZE));
            } else {
                int size = remain == 0 ? CHUNK_SIZE : remain;
                allMatchIds.addAll(riotApiProvider.getMatchIds(puuid, SEASON_START_TIME, start, size));
            }
        }

        return allMatchIds;
    }

    private List<String> getIncrementalMatchIds(String puuid, String lastMatchId) {
        List<String> incrementalMatchIds = new ArrayList<>();
        int start = 0;

        while (true) {
            List<String> chunk = riotApiProvider.getMatchIds(puuid, SEASON_START_TIME, start, CHUNK_SIZE);
            if (chunk.isEmpty()) {
                break;
            }

            boolean found = false;
            for (String matchId : chunk) {
                if (matchId.equals(lastMatchId)) {
                    found = true;
                    break;
                }
                incrementalMatchIds.add(matchId);
            }

            if (found || chunk.size() < CHUNK_SIZE) {
                break;
            }

            start += CHUNK_SIZE;
        }

        log.debug("Found {} new matches since last match ID", incrementalMatchIds.size());
        return incrementalMatchIds;
    }

    private List<MatchData> fetchMatches(Long userId, String puuid, List<String> matchIds) {
        if (matchIds.isEmpty()) {
            return List.of();
        }

        log.info("Fetching {} matches with concurrency={}", matchIds.size(), MATCH_FETCH_CONCURRENCY);

        long startTime = System.currentTimeMillis();

        // 최대 5개까지 병렬로 매치 상세 수집
        // Rate Limit은 RiotApiProvider 내부에서 자동으로 처리됨
        List<MatchData> matches = Flux.fromIterable(matchIds)
                .flatMapSequential(matchId ->
                                riotApiProvider.getMatchInfo(matchId, puuid, userId)
                                        .doOnSuccess(data -> log.trace("Fetched match: {}", matchId))
                                        .doOnError(error -> log.error("Failed to fetch match {}: {}", matchId, error.getMessage()))
                                        .onErrorResume(error -> {
                                            log.warn("Skipping match {} due to error", matchId);
                                            return Mono.empty();
                                        })
                                        .subscribeOn(Schedulers.boundedElastic()),
                        MATCH_FETCH_CONCURRENCY
                )
                .collectList()
                .block();

        long duration = System.currentTimeMillis() - startTime;
        log.info("Fetched {}/{} matches in {}ms (avg: {}ms per match)",
                matches != null ? matches.size() : 0,
                matchIds.size(),
                duration,
                matches != null && !matches.isEmpty() ? duration / matches.size() : 0);

        return matches != null ? matches : List.of();
    }
}
