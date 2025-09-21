package maruhxn.rankademy.application.competition.provided.dto;

import maruhxn.rankademy.domain.competition.CompetitionStatus;

import java.time.LocalDateTime;
import java.util.List;

public record CompetitionPageResponse(
        Long totalCount,
        List<CompetitionListItemResponse> competitions
) {
    public record CompetitionListItemResponse(
            Long competitionId,
            String otherTeamUnivName,
            CompetitionStatus status,
            TeamInfoResponse myTeam,
            TeamInfoResponse otherTeam,
            LocalDateTime submittedAt,
            boolean isWin,
            List<SetResultResponse> setResults
    ) {
    }
}
