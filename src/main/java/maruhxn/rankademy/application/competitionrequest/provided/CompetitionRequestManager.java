package maruhxn.rankademy.application.competitionrequest.provided;

import maruhxn.rankademy.domain.competitionrequest.CompetitionRequest;

public interface CompetitionRequestManager {

    CompetitionRequest sendRequest(Long actingUserId, Long fromTeamId, Long toTeamId);

    void acceptRequest(Long actingUserId, Long requestId);

    void rejectRequest(Long actingUserId, Long requestId);
}
