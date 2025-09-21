package maruhxn.rankademy.domain.scrim_team.dto;

import maruhxn.rankademy.domain.scrim_team.ScrimTeamMember;

import java.util.Set;

public record ScrimTeamCreateRequest(
        String name,
        String intro,
        Long representativeId,
        Set<ScrimTeamMember> members
) {
}
