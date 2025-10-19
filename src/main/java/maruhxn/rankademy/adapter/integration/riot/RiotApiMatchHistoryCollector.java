package maruhxn.rankademy.adapter.integration.riot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maruhxn.rankademy.application.match.required.MatchHistoryCollector;
import maruhxn.rankademy.application.user.required.RiotApiProvider;
import maruhxn.rankademy.domain.match.MatchData;
import maruhxn.rankademy.domain.user.User;
import org.springframework.stereotype.Component;

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

    private static final int CHUNK_SIZE = 100;

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

        log.info("Match Ids: {}", matchIds);

        return fetchMatches(user.getId(), matchIds);
    }

    private List<String> getAllMatchIds(String puuid, int totalMatchCnt) {
        int r = totalMatchCnt / CHUNK_SIZE;
        int remain = totalMatchCnt % CHUNK_SIZE;

        List<String> allMatchIds = new ArrayList<>();
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

        return incrementalMatchIds;
    }

    private List<MatchData> fetchMatches(Long userId, List<String> matchIds) {
        List<MatchData> matches = new ArrayList<>(matchIds.size());

        matchIds.forEach(matchId -> {
            MatchData matchInfo = riotApiProvider.getMatchInfo(matchId, userId);
            matches.add(matchInfo);
        });

        return matches;
    }
}
