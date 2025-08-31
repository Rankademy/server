package maruhxn.rankademy.domain.shared.event;

import lombok.Getter;

@Getter
public class CompetitionResultSubmitEvent extends DomainEvent {

    private final Long competitionId;

    private final Long actingUserId;

    public CompetitionResultSubmitEvent(Long competitionId, Long actingUserId) {
        this.competitionId = competitionId;
        this.actingUserId = actingUserId;
    }
}
