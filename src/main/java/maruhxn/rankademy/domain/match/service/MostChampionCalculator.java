package maruhxn.rankademy.domain.match.service;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.domain.match.ChampionPlayRecord;
import maruhxn.rankademy.domain.match.MatchData;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 매치 정보들로부터 모스트 챔피언 3개를 계산하는 도메인 서비스
 */
@Service
@RequiredArgsConstructor
public class MostChampionCalculator {

    private static final int MOST_CNT = 3;

    private final MyChampionIdParser myChampionIdParser;

    public List<ChampionPlayRecord> calculateMostChampionsTop3(List<MatchData> matches, String puuid) {
        Map<String, Long> championPlayCounts = matches.stream()
                .map(MatchData::getJsonData)
                .map(document -> myChampionIdParser.parse(document, puuid))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return championPlayCounts.entrySet().stream()
                .sorted(Map.Entry.<String,
                        Long>comparingByValue().reversed())
                .limit(MOST_CNT)
                .map(entry -> new ChampionPlayRecord(entry.getKey(),
                        entry.getValue()))
                .collect(Collectors.toList());
    }
}
