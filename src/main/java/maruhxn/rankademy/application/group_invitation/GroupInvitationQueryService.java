package maruhxn.rankademy.application.group_invitation;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.group_invitation.dto.GroupInvitationPageResponse;
import maruhxn.rankademy.application.group_invitation.provided.GroupInvitationReader;
import maruhxn.rankademy.application.group_invitation.required.GroupInvitationQueryRepository;
import maruhxn.rankademy.application.group_invitation.required.GroupInvitationRepository;
import maruhxn.rankademy.domain.group_invitation.GroupInvitation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GroupInvitationQueryService implements GroupInvitationReader {

    private final GroupInvitationRepository groupInvitationRepository;
    private final GroupInvitationQueryRepository groupInvitationQueryRepository;

    @Override
    public GroupInvitation get(Long invitationId) {
        return groupInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new NoSuchElementException("그룹 초대 정보를 찾을 수 없습니다. invitationId: " + invitationId));
    }

    @Override
    public GroupInvitationPageResponse getInvitations(Long userId, int page) {
        return groupInvitationQueryRepository.getInvitations(userId, page);
    }
}
