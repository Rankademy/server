package maruhxn.rankademy.application.match;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.match.provided.MatchHistoryAnalyzer;
import maruhxn.rankademy.application.match.required.MatchDataRepository;
import maruhxn.rankademy.application.match.required.MatchHistoryCollector;
import maruhxn.rankademy.application.user.provided.UserReader;
import maruhxn.rankademy.domain.match.MatchData;
import maruhxn.rankademy.domain.match.service.MostChampionCalculator;
import maruhxn.rankademy.domain.user.SummonerInfo;
import maruhxn.rankademy.domain.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchHistoryService implements MatchHistoryAnalyzer {

    private final UserReader userReader;
    private final MatchDataRepository matchDataRepository;
    private final MatchHistoryCollector matchHistoryCollector;
    private final MostChampionCalculator mostChampionCalculator;

    @Override
    @Transactional
    public void refreshMatches(Long userId) {
        User user = userReader.getWithSummonerInfo(userId);
        SummonerInfo summonerInfo = user.getSummonerInfo();

        List<MatchData> newMatches = matchHistoryCollector.collectMatchesWithLastMatchId(
                user,
                summonerInfo.getLastSyncedMatchId()
        );

        int saved = persistMatches(user, newMatches);
        if (saved == 0) {
            summonerInfo.touchMatchSync();
        }
    }

    private int persistMatches(User user, List<MatchData> newMatches) {
        if (newMatches.isEmpty()) {
            return 0;
        }

        matchDataRepository.saveAll(newMatches);

        List<MatchData> allMatches = matchDataRepository.findAllByUserId(user.getId());

        SummonerInfo summonerInfo = user.getSummonerInfo();
        summonerInfo.updateMostChampions(mostChampionCalculator, allMatches);
        summonerInfo.updateMatchSyncStatus(newMatches.get(0).getMatchId());

        return newMatches.size();
    }
}
