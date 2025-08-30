package maruhxn.rankademy.domain.competitionrequest;

import maruhxn.rankademy.domain.team.Team;
import maruhxn.rankademy.domain.team.TeamFixture;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.domain.user.UserFixture;
import org.springframework.test.util.ReflectionTestUtils;

public class CompetitionRequestFixture {

    public static CompetitionRequest createCompetitionRequest(Team fromTeam, Team toTeam) {
        return new CompetitionRequest(
                fromTeam.getId(),
                toTeam.getId()
        );
    }

    public static Team createTeam(Long representativeId, Long groupId) {
        User representative = UserFixture.createUser("tester@test.com", "fromRepName");
        ReflectionTestUtils.setField(representative, "id", representativeId);
        Team team = TeamFixture.createTeamWithReflection(representative, groupId);
        ReflectionTestUtils.setField(team, "id", representativeId);
        return team;
    }
}
