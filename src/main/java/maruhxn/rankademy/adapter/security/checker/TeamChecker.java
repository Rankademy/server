package maruhxn.rankademy.adapter.security.checker;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.security.model.UserInfo;
import maruhxn.rankademy.application.team.provided.TeamReader;
import maruhxn.rankademy.domain.team.Team;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TeamChecker {

    private final TeamReader teamReader;

    public boolean isTeamLeader(UserInfo user, Long teamId) {
        if(!user.isAuthorized()) return false;

        Team team = teamReader.get(teamId);

        return team.isRepresentative(user.id());
    }
}
