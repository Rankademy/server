package maruhxn.rankademy.domain.shared.event;

import java.time.LocalDateTime;

public record SendCompetitionRequestEvent(
        Long fromTeamId,
        Long toTeamId,
        Long actingUserId,
        LocalDateTime requestedAt
) {
}
