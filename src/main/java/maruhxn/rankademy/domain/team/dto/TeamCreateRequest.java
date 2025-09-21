package maruhxn.rankademy.domain.team.dto;

import maruhxn.rankademy.domain.team.TeamMember;

import java.util.Set;

public record TeamCreateRequest(
        Long groupId,
        String name,
        String intro,
        Long representativeId,
        Set<TeamMember> members
) {
}
