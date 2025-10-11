package maruhxn.rankademy.application.competitionrequest.required.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CompetitionRequestPageResponse(
        Long totalCount,
        List<CompetitionRequestResponse> competitionRequests
) {

    public record CompetitionRequestResponse(
            Long requestId,
            Long fromTeamId,
            String fromTeamName,
            LocalDateTime requestedAt
    ) {
    }
}
