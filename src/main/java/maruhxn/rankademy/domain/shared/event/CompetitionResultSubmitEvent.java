package maruhxn.rankademy.domain.shared.event;

import java.time.LocalDateTime;

public record CompetitionResultSubmitEvent(
        Long competitionId,
        Long actingUserId,
        LocalDateTime submittedAt
) {
}
