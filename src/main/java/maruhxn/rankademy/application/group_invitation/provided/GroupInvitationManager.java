package maruhxn.rankademy.application.group_invitation.provided;

import maruhxn.rankademy.domain.group_invitation.GroupInvitation;

public interface GroupInvitationManager {

    GroupInvitation invite(Long groupId, Long invitedUserId);

    void acceptInvitation(Long actingUserId, Long invitationId);

    void rejectInvitation(Long actingUserId, Long invitationId);
}
