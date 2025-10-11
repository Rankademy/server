package maruhxn.rankademy.domain.shared.event;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CompetitionAcceptEvent extends DomainEvent {

    private final Long fromTeamId;
    private final Long toTeamId;
    private final LocalDateTime acceptedAt;

    public CompetitionAcceptEvent(Long fromTeamId, Long toTeamId, LocalDateTime acceptedAt) {
        this.fromTeamId = fromTeamId;
        this.toTeamId = toTeamId;
        this.acceptedAt = acceptedAt;
    }
}
