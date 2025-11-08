package maruhxn.rankademy.adapter.security.checker;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.security.model.UserInfo;
import maruhxn.rankademy.application.competition.provided.CompetitionReader;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompetitionChecker {

    private final CompetitionReader competitionReader;

    public boolean isMyCompetition(UserInfo user, Long competitionId) {
        if(!user.isAuthorized()) return false;

        return competitionReader.checkIsMyCompetition(user.id(), competitionId);
    }
}
