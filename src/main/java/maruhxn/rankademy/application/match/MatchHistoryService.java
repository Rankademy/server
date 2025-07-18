package maruhxn.rankademy.application.match;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.match.provided.MatchHistoryAnalyzer;
import maruhxn.rankademy.application.match.required.MatchDataRepository;
import maruhxn.rankademy.application.match.required.MatchHistoryCollector;
import maruhxn.rankademy.application.member.provided.MemberReader;
import maruhxn.rankademy.domain.match.MatchData;
import maruhxn.rankademy.domain.match.service.MostChampionCalculator;
import maruhxn.rankademy.domain.member.Member;
import maruhxn.rankademy.domain.member.SummonerInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchHistoryService implements MatchHistoryAnalyzer {

    private final MemberReader memberReader;
    private final MatchDataRepository matchDataRepository;
    private final MatchHistoryCollector matchHistoryCollector;
    private final MostChampionCalculator mostChampionCalculator;

    @Transactional
    public void fetchAndAnalyzeMatches(Long memberId) {
        Member member = memberReader.find(memberId);

        if (!member.isAuthorized()) {
            throw new IllegalStateException("인증된 사용자가 아닙니다.");
        }

        // 외부에서 새로운 매치 기록을 가져옴
        List<MatchData> newMatches = matchHistoryCollector.collectAllMatches(member);

        if (newMatches.isEmpty()) return;

        matchDataRepository.saveAll(newMatches);

        // 모스트 챔피언 업데이트
        SummonerInfo summonerInfo = member.getSummonerInfo();
        summonerInfo.updateMostChampions(mostChampionCalculator, newMatches);
    }
}
