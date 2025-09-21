package maruhxn.rankademy.adapter.security.checker;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.security.model.UserInfo;
import maruhxn.rankademy.application.competition.provided.CompetitionReader;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompetitionChecker {

    private final CompetitionReader competitionReader;

    public boolean isMyCompetition(UserInfo userInfo, Long competitionId) {
        return competitionReader.checkIsMyCompetition(userInfo.id(), competitionId);
    }
}
