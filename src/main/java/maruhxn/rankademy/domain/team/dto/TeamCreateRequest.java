package maruhxn.rankademy.domain.team.dto;

import maruhxn.rankademy.domain.user.LolPosition;

import java.util.Set;

public record TeamCreateRequest(
        Long groupId,
        String name,
        String intro,
        Long representativeId,
        Set<TeamMemberSlot> members
) {
    public record TeamMemberSlot(
            Long userId,
            LolPosition position
    ) {
    }
}
