package maruhxn.rankademy.application.group_invitation.required;

import maruhxn.rankademy.application.group_invitation.dto.GroupInvitationPageResponse;

public interface GroupInvitationQueryRepository {

    GroupInvitationPageResponse getInvitations(Long userId, int page);
}
