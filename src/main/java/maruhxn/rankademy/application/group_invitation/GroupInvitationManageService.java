package maruhxn.rankademy.application.group_invitation;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.group_invitation.provided.GroupInvitationManager;
import maruhxn.rankademy.application.group_invitation.required.GroupInvitationRepository;
import maruhxn.rankademy.domain.group_invitation.GroupInvitation;
import maruhxn.rankademy.domain.shared.DomainEventPublisher;
import maruhxn.rankademy.domain.shared.event.GroupInviteEvent;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
@Transactional
@RequiredArgsConstructor
public class GroupInvitationManageService implements GroupInvitationManager {

    private final GroupInvitationRepository groupInvitationRepository;
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
        GroupInvitation invitation = groupInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new NoSuchElementException("그룹 초대 정보를 찾을 수 없습니다. invitationId: " + invitationId));

        if(!invitation.getUserId().equals(actingUserId)) {
            throw new IllegalStateException("초대 대상이 아닙니다. invitationId: " + invitationId);
        }

        invitation.accept();
    }

    @Override
    public void rejectInvitation(Long actingUserId, Long invitationId) {
        GroupInvitation invitation = groupInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new NoSuchElementException("그룹 초대 정보를 찾을 수 없습니다. invitationId: " + invitationId));

        if(!invitation.getUserId().equals(actingUserId)) {
            throw new IllegalStateException("초대 대상이 아닙니다. invitationId: " + invitationId);
        }

        invitation.reject();
    }
}
