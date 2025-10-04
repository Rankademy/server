package maruhxn.rankademy.application.group_invitation.provided;

import maruhxn.rankademy.application.group_invitation.dto.GroupInvitationPageResponse;
import maruhxn.rankademy.domain.group_invitation.GroupInvitation;

public interface GroupInvitationReader {

    GroupInvitation get(Long invitationId);
    GroupInvitationPageResponse getInvitations(Long userId, int page);
}
