package maruhxn.rankademy.domain.shared.event;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TeamDeactivatedEvent extends DomainEvent {
    private final Long teamId;
    private final LocalDateTime deactivatedAt;

    public TeamDeactivatedEvent(Long teamId, LocalDateTime deactivatedAt) {
        this.teamId = teamId;
        this.deactivatedAt = deactivatedAt;
    }
}

