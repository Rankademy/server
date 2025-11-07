package maruhxn.rankademy.application.match;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maruhxn.rankademy.application.match.provided.MatchHistoryAnalyzer;
import maruhxn.rankademy.application.match.required.MatchDataRepository;
import maruhxn.rankademy.application.match.required.MatchHistoryCollector;
import maruhxn.rankademy.application.user.provided.UserReader;
import maruhxn.rankademy.domain.match.MatchData;
import maruhxn.rankademy.domain.match.service.MostChampionCalculator;
import maruhxn.rankademy.domain.user.SummonerInfo;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.domain.user.service.UserLabelProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchHistoryService implements MatchHistoryAnalyzer {

    private final UserReader userReader;
    private final MatchDataRepository matchDataRepository;
    private final MatchHistoryCollector matchHistoryCollector;
    private final MostChampionCalculator mostChampionCalculator;
    private final UserLabelProvider userLabelProvider;

    @Override
    @Transactional
    public void refreshMatches(Long userId) {
        log.info("[MatchHistoryService] - 유저 전적 갱신 시작, userId: {}", userId);
        User user = userReader.getWithSummonerInfo(userId);
        SummonerInfo summonerInfo = user.getSummonerInfo();

        List<MatchData> newMatches = matchHistoryCollector.collectMatchesWithLastMatchId(
                user,
                summonerInfo.getLastSyncedMatchId()
        );

        int saved = persistMatches(user, newMatches);
        log.info("[MatchHistoryService] - userId: {}, 추가된 매치 개수: {}", userId, saved);
        if (saved != 0) {
            user.updateLabels(userLabelProvider);
        }
        summonerInfo.touchMatchSync();
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
