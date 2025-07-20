package maruhxn.rankademy.adapter.integration;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.match.required.MatchHistoryCollector;
import maruhxn.rankademy.application.user.required.RiotApiProvider;
import maruhxn.rankademy.domain.match.MatchData;
import maruhxn.rankademy.domain.user.User;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RiotApiMatchHistoryCollector implements MatchHistoryCollector {

    private static final Long SEASON_START_TIME = LocalDate.of(2025, 1, 9)
            .atStartOfDay(ZoneOffset.UTC).toEpochSecond();

    private static final int CHUNK_SIZE = 100;

    private final RiotApiProvider riotApiProvider;

    @Override
    public List<MatchData> collectAllMatches(User user) {
        String puuid = user.getSummonerInfo().getPuuid();
        List<String> allMatchIds = this.getMatchIds(puuid, user.getSummonerInfo().getTotalMatchCnt());

        List<MatchData> matches = new ArrayList<>();

        allMatchIds.forEach(matchId -> {
            MatchData matchInfo = riotApiProvider.getMatchInfo(matchId, user.getId());
            matches.add(matchInfo);
        });

        return matches;
    }

    private List<String> getMatchIds(String puuid, int totalMatchCnt) {
        int r = totalMatchCnt / CHUNK_SIZE;
        int remain = totalMatchCnt % CHUNK_SIZE;

        List<String> allMatchIds = new ArrayList<>();
        for (int i = 0; i < r + 1; i++) {
            int start = i * CHUNK_SIZE;
            if (i != r) {
                allMatchIds.addAll(riotApiProvider.getMatchIds(puuid, SEASON_START_TIME, start, CHUNK_SIZE));
            } else {
                allMatchIds.addAll(riotApiProvider.getMatchIds(puuid, SEASON_START_TIME, start, remain));
            }
        }

        return allMatchIds;
    }
}
