package maruhxn.rankademy.domain.shared.event;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TeamCreatedEvent extends DomainEvent {
    private final Long teamId;
    private final LocalDateTime createdAt;

    public TeamCreatedEvent(Long teamId, LocalDateTime createdAt) {
        this.teamId = teamId;
        this.createdAt = createdAt;
    }
}
