package maruhxn.rankademy.domain.shared.event;

import lombok.Getter;

@Getter
public class RiotAuthEvent extends DomainEvent {
    private final Long userId;

    public RiotAuthEvent(Long userId) {
        this.userId = userId;
    }
}
