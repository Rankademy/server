package maruhxn.rankademy.application.match.required;

import maruhxn.rankademy.domain.match.MatchData;
import maruhxn.rankademy.domain.member.Member;

import java.util.List;

public interface MatchHistoryCollector {
    List<MatchData> collectAllMatches(Member member);
}
