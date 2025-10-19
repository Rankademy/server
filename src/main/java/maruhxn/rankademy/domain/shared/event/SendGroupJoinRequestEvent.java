package maruhxn.rankademy.domain.shared.event;

import lombok.Getter;

@Getter
public class SendGroupJoinRequestEvent extends DomainEvent {
    private final Long groupId;
    private final String fullSummonerName;

    public SendGroupJoinRequestEvent(Long groupId, String fullSummonerName) {
        this.groupId = groupId;
        this.fullSummonerName = fullSummonerName;
    }
}
