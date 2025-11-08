package maruhxn.rankademy.adapter.security.checker;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.security.model.UserInfo;
import maruhxn.rankademy.application.scrim_team.provided.ScrimTeamReader;
import maruhxn.rankademy.domain.scrim_team.ScrimTeam;
import org.springframework.stereotype.Component;

@Component("scrimTeamChecker")
@RequiredArgsConstructor
public class ScrimTeamChecker {

    private final ScrimTeamReader scrimTeamReader;

    public boolean isScrimTeamLeader(UserInfo user, Long scrimTeamId) {
        if(!user.isAuthorized()) return false;

        ScrimTeam scrimTeam = scrimTeamReader.get(scrimTeamId);

        return scrimTeam.isRepresentative(user.id());
    }
}
