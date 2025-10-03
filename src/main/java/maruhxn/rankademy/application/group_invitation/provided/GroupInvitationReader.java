package maruhxn.rankademy.application.group_invitation.provided;

import maruhxn.rankademy.application.group_invitation.dto.GroupInvitationPageResponse;

public interface GroupInvitationReader {
    GroupInvitationPageResponse getInvitations(Long userId, int page);
}
