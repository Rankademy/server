package maruhxn.rankademy.domain.competitionrequest.service;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.domain.competitionrequest.CompetitionRequest;
import maruhxn.rankademy.domain.shared.DomainEventPublisher;
import maruhxn.rankademy.domain.shared.event.CompetitionAcceptEvent;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CompetitionRequestAcceptor {

    private final DomainEventPublisher publisher;

    public void accept(CompetitionRequest competitionRequest) {
        competitionRequest.accept();
        LocalDateTime now = LocalDateTime.now();
        publisher.publish(
                new CompetitionAcceptEvent(
                        competitionRequest.getFromTeamId(),
                        competitionRequest.getToTeamId(),
                        now
                )
        );
    }
}
