package maruhxn.rankademy.domain.competition;

import maruhxn.rankademy.domain.team.Team;
import maruhxn.rankademy.domain.team.TeamMember;
import maruhxn.rankademy.domain.user.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CompetitionFixture {

    private static Team createTeamWithMembers(Long teamId, Long leaderId, String leaderName) {
        User leader = UserFixture.createUser(leaderName + "@test.com", leaderName);
        ReflectionTestUtils.setField(leader, "id", leaderId);
        leader.enrollUnivInfo(UserFixture.createEnrollUnivRequest());
        leader.completeUnivAuthentication();
        leader.connectSummonerInfo(
                UserFixture.createSummonerInfoConnector(leaderName + "-puuid", new TierInfo(Tier.DIAMOND, Rank.I, 50)),
                UserFixture.createRiotAuthRequest(leaderName, "KR" + leaderId)
        );

        Set<TeamMember> members = IntStream.range(0, 4)
                .mapToObj(i -> {
                    User user = UserFixture.createUser("member" + i + "@test.com", "member" + i);
                    ReflectionTestUtils.setField(user, "id", (long) (leaderId * 10 + i));
                    user.enrollUnivInfo(UserFixture.createEnrollUnivRequest());
                    user.completeUnivAuthentication();
                    user.connectSummonerInfo(
                            UserFixture.createSummonerInfoConnector("member" + i + "-puuid", new TierInfo(Tier.GOLD, Rank.IV, 0)),
                            UserFixture.createRiotAuthRequest("member" + i, "KR_M" + i)
                    );
                    return new TeamMember(user, LolPosition.values()[i]);
                })
                .collect(Collectors.toSet());

        members.add(new TeamMember(leader, LolPosition.values()[4]));

        Team team = Team.create(teamId, "Team " + teamId, "Intro " + teamId, leaderId, members);
        ReflectionTestUtils.setField(team, "id", teamId);
        return team;
    }

    public static Competition createCompetition() {
        Team team1 = createTeamWithMembers(1L, 1L, "leader1");
        Team team2 = createTeamWithMembers(2L, 2L, "leader2");
        return Competition.createAfterAccept(team1, team2);
    }
}
