package maruhxn.rankademy.application.competitionrequest.required;

import maruhxn.rankademy.application.competitionrequest.required.dto.CompetitionRequestPageResponse;

public interface CompetitionRequestQueryRepository {

    CompetitionRequestPageResponse findAll(Long teamId, int page);

}
