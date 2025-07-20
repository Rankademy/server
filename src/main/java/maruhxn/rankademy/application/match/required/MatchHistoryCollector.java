package maruhxn.rankademy.application.match.required;

import maruhxn.rankademy.domain.match.MatchData;
import maruhxn.rankademy.domain.user.User;

import java.util.List;

public interface MatchHistoryCollector {
    List<MatchData> collectAllMatches(User user);
}
