package maruhxn.rankademy.application.competition;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.competition.provided.CompetitionResultManager;
import maruhxn.rankademy.application.competition.required.CompetitionRepository;
import maruhxn.rankademy.domain.competition.Competition;
import maruhxn.rankademy.domain.competition.dto.OpposeResultRequest;
import maruhxn.rankademy.domain.competition.dto.SubmitCompetitionResultRequest;
import maruhxn.rankademy.domain.shared.DomainEventPublisher;
import maruhxn.rankademy.domain.shared.event.CompetitionResultSubmitEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
@Transactional
@RequiredArgsConstructor
public class CompetitionResultService implements CompetitionResultManager {

    private final CompetitionRepository competitionRepository;
    private final DomainEventPublisher publisher;

    @Override
    public void submitResult(Long actingUserId, Long competitionId, SubmitCompetitionResultRequest request) {
        Competition competition = getCompetition(competitionId);
        competition.submitSetResult(request);
        publisher.publish(new CompetitionResultSubmitEvent(competitionId, actingUserId));
    }

    @Override
    public void opposeResult(Long competitionId, OpposeResultRequest request) {
        Competition competition = getCompetition(competitionId);
        LocalDateTime now = LocalDateTime.now();
        competition.oppose(request, now);
    }

    private Competition getCompetition(Long competitionId) {
        return competitionRepository.findById(competitionId)
                .orElseThrow(() -> new NoSuchElementException("대항전 정보를 찾을 수 없습니다. competitionId: " + competitionId));
    }
}
