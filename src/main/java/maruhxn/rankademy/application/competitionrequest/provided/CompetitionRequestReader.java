package maruhxn.rankademy.application.competitionrequest.provided;

import maruhxn.rankademy.application.competitionrequest.required.dto.CompetitionRequestPageResponse;

public interface CompetitionRequestReader {

    CompetitionRequestPageResponse getRequests(Long teamId, int page);

}
