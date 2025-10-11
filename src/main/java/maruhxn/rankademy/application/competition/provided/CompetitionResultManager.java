package maruhxn.rankademy.application.competition.provided;

import maruhxn.rankademy.domain.competition.dto.OpposeResultRequest;
import maruhxn.rankademy.domain.competition.dto.SubmitCompetitionResultRequest;

public interface CompetitionResultManager {

    void submitResult(Long actingUserId, Long competitionId, SubmitCompetitionResultRequest request);

    void opposeResult(Long competitionId, OpposeResultRequest request);

}
