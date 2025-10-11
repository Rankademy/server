package maruhxn.rankademy.domain.competition;

import maruhxn.rankademy.domain.competition.dto.SubmitCompetitionResultRequest;

import java.util.ArrayList;
import java.util.List;

public class CompetitionFixture {

    public static Competition createCompetition() {
        return Competition.createAfterAccept(1L, 2L);
    }

    public static SubmitCompetitionResultRequest createSubmitCompetitionResultRequest(
            Long team1Id,
            Long team2Id,
            int totalSets,
            Long finalWinnerId,
            Long finalWinnerGroupId,
            Long finalLoserGroupId
    ) {
        Long loserId = finalWinnerId == team1Id ? team2Id : team1Id;

        List<SubmitCompetitionResultRequest.SetResultDto> setResults = new ArrayList<>();
        for (int i = 1; i <= totalSets; i++) {
            Long winTeamId = i % 2 == 0 ? loserId : finalWinnerId;
            setResults.add(new SubmitCompetitionResultRequest.SetResultDto(i, winTeamId, "image" + i));
        }

        return new SubmitCompetitionResultRequest(
                team1Id,
                team2Id,
                totalSets,
                setResults,
                String.format("Win Team Id: %d", finalWinnerId),
                team1Id,
                finalWinnerGroupId,
                finalLoserGroupId
        );
    }
}
