package maruhxn.rankademy.application.competition;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.competition.required.CompetitionRepository;
import maruhxn.rankademy.application.team.required.TeamRepository;
import maruhxn.rankademy.domain.competition.Competition;
import maruhxn.rankademy.domain.shared.event.CompetitionAcceptEvent;
import maruhxn.rankademy.domain.team.Team;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.NoSuchElementException;

@Component
@RequiredArgsConstructor
public class CompetitionRequestAcceptListener {

    private final TeamRepository teamRepository;
    private final CompetitionRepository competitionRepository;

    /**
     * 대항전 요청 수락 시, 대항전 생성
     *
     * @param event
     */
    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void createCompetition(CompetitionAcceptEvent event) {
        Team team1 = teamRepository.findById(event.fromTeamId())
                .orElseThrow(() -> new NoSuchElementException("팀 정보를 찾을 수 없습니다. teamId: " + event.fromTeamId()));
        Team team2 = teamRepository.findById(event.toTeamId())
                .orElseThrow(() -> new NoSuchElementException("팀 정보를 찾을 수 없습니다. teamId: " + event.toTeamId()));

        competitionRepository.save(Competition.createAfterAccept(team1, team2));
    }
}
