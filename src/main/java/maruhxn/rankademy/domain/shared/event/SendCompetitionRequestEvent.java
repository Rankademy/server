package maruhxn.rankademy.domain.shared.event;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SendCompetitionRequestEvent extends DomainEvent {

    private final Long fromTeamId;
    private final Long toTeamId;
    private final Long actingUserId;
    private final LocalDateTime requestedAt;

    public SendCompetitionRequestEvent(Long fromTeamId, Long toTeamId, Long actingUserId, LocalDateTime requestedAt) {
        this.fromTeamId = fromTeamId;
        this.toTeamId = toTeamId;
        this.actingUserId = actingUserId;
        this.requestedAt = requestedAt;
    }
}
