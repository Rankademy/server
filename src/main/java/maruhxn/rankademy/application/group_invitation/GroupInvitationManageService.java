package maruhxn.rankademy.application.group_invitation;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.group.provided.GroupReader;
import maruhxn.rankademy.application.group_invitation.provided.GroupInvitationManager;
import maruhxn.rankademy.application.group_invitation.provided.GroupInvitationReader;
import maruhxn.rankademy.application.group_invitation.required.GroupInvitationRepository;
import maruhxn.rankademy.application.user.provided.UserReader;
import maruhxn.rankademy.domain.group.Group;
import maruhxn.rankademy.domain.group.GroupRole;
import maruhxn.rankademy.domain.group_invitation.GroupInvitation;
import maruhxn.rankademy.domain.shared.DomainEventPublisher;
import maruhxn.rankademy.domain.shared.event.GroupInviteEvent;
import maruhxn.rankademy.domain.user.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class GroupInvitationManageService implements GroupInvitationManager {

    private final GroupInvitationRepository groupInvitationRepository;
    private final GroupInvitationReader groupInvitationReader;
    private final UserReader userReader;
    private final GroupReader groupReader;
    private final DomainEventPublisher publisher;

    @Override
    public GroupInvitation invite(Long groupId, Long invitedUserId) {
        GroupInvitation groupInvitation = GroupInvitation.create(groupId, invitedUserId);

        LocalDateTime now = LocalDateTime.now();
        publisher.publish(new GroupInviteEvent(groupId, invitedUserId, now));

        return groupInvitationRepository.save(groupInvitation);
    }

    @Override
    public void acceptInvitation(Long actingUserId, Long invitationId) {
        GroupInvitation invitation = groupInvitationReader.get(invitationId);

        Group group = groupReader.get(invitation.getGroupId());
        User user = userReader.get(invitation.getUserId());

        group.addMember(user, GroupRole.MEMBER);

        invitation.accept();
    }

    @Override
    public void rejectInvitation(Long actingUserId, Long invitationId) {
        GroupInvitation invitation = groupInvitationReader.get(invitationId);

        invitation.reject();
    }
}
