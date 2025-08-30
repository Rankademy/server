package maruhxn.rankademy.domain.shared.event;

import java.time.LocalDateTime;

public record CompetitionAcceptEvent(
        Long fromTeamId,
        Long toTeamId,
        LocalDateTime acceptedAt
) {
}
