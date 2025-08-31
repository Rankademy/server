package maruhxn.rankademy.application.competition;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.competition.required.CompetitionRepository;
import maruhxn.rankademy.domain.competition.Competition;
import maruhxn.rankademy.domain.shared.event.CompetitionAcceptEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class CompetitionRequestAcceptListener {

    private final CompetitionRepository competitionRepository;

    /**
     * 대항전 요청 수락 시, 대항전 생성
     *
     * @param event
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void createCompetition(CompetitionAcceptEvent event) {
        competitionRepository.save(Competition.createAfterAccept(event.getFromTeamId(), event.getToTeamId()));
    }
}
