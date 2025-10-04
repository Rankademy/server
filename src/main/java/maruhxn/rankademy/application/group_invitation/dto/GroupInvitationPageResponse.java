package maruhxn.rankademy.application.group_invitation.dto;

import java.time.LocalDateTime;
import java.util.List;

public record GroupInvitationPageResponse(
        Long totalCount,
        List<GroupInvitationResponse> competitionRequests
) {

    public record GroupInvitationResponse(
            Long invitationId,
            Long groupId,
            String groupName,
            Long userId,
            LocalDateTime invitedAt) {
    }
}
