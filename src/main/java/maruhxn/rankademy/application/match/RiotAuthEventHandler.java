package maruhxn.rankademy.application.match;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.match.provided.MatchHistoryAnalyzer;
import maruhxn.rankademy.domain.shared.event.RiotAuthEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class RiotAuthEventHandler {

    private final MatchHistoryAnalyzer matchHistoryAnalyzer;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(RiotAuthEvent event) {
        matchHistoryAnalyzer.refreshMatches(event.getUserId());
    }
}
