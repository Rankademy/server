package maruhxn.rankademy.application.group_invitation;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.group_invitation.dto.GroupInvitationPageResponse;
import maruhxn.rankademy.application.group_invitation.provided.GroupInvitationReader;
import maruhxn.rankademy.application.group_invitation.required.GroupInvitationQueryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GroupInvitationQueryService implements GroupInvitationReader {

    private final GroupInvitationQueryRepository groupInvitationQueryRepository;

    @Override
    public GroupInvitationPageResponse getInvitations(Long userId, int page) {
        return null;
    }
}
