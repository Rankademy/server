package maruhxn.rankademy.domain.shared.event;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class GroupInviteEvent extends DomainEvent {
    private final Long groupId;
    private final Long invitedUserId;
    private final LocalDateTime invitedAt;

    public GroupInviteEvent(Long groupId, Long invitedUserId, LocalDateTime invitedAt) {
        this.groupId = groupId;
        this.invitedUserId = invitedUserId;
        this.invitedAt = invitedAt;
    }
}
