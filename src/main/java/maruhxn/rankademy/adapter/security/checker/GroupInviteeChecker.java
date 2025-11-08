package maruhxn.rankademy.adapter.security.checker;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.security.model.UserInfo;
import maruhxn.rankademy.application.group_invitation.provided.GroupInvitationReader;
import maruhxn.rankademy.domain.group_invitation.GroupInvitation;
import org.springframework.stereotype.Component;

@Component("groupInviteeChecker")
@RequiredArgsConstructor
public class GroupInviteeChecker {

    private final GroupInvitationReader groupInvitationReader;

    public boolean isInvitee(UserInfo user, Long invitationId) {
        if(!user.isAuthorized()) return false;

        GroupInvitation invitation = groupInvitationReader.get(invitationId);

        return invitation.getUserId().equals(user.id());
    }

}
