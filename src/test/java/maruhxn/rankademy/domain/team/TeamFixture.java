package maruhxn.rankademy.domain.team;

import maruhxn.rankademy.domain.team.dto.TeamCreateRequest;
import maruhxn.rankademy.domain.user.LolPosition;
import maruhxn.rankademy.domain.user.TierInfo;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.domain.user.UserFixture;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static maruhxn.rankademy.domain.user.UserFixture.*;

public class TeamFixture {

    public static TeamMember createTeamMember(Long id, TierInfo tierInfo, LolPosition position) {
        User user = UserFixture.createUser("test" + id + "@test.com", "tester" + id);
        ReflectionTestUtils.setField(user, "id", id);
        user.completeUnivAuthentication(createEnrollUnivRequest());
        user.connectSummonerInfo(
                createSummonerInfoConnector("member" + id + "-puuid", tierInfo),
                createRiotAuthRequest("member" + id, "KR" + id)
        );
        return new TeamMember(user, position);
    }

    public static Set<TeamMember> createTeamMembersWithReflection(User representative) {
        Set<User> users = IntStream.range(0, 4)
                .mapToObj(i ->
                        {
                            User user = UserFixture.createUser("test" + i + "@test.com", "tester" + i);
                            ReflectionTestUtils.setField(user, "id", (long) i);
                            return user;
                        }
                )
                .collect(Collectors.toSet());
        users.add(representative);

        Set<TeamMember> members = new HashSet<>();
        int i = 0;
        for (User user : users) {
            members.add(new TeamMember(user, LolPosition.values()[i++]));
        }
        return members;
    }

    public static TeamCreateRequest createTeamCreateRequest(Long representativeId, Set<TeamCreateRequest.TeamMemberSlot> memberSlots) {
        return new TeamCreateRequest(1L, "test team", "test intro", representativeId, memberSlots);
    }

    public static TeamCreateRequest createTeamCreateRequest(Long representativeId, Set<TeamCreateRequest.TeamMemberSlot> memberSlots, Long groupId) {
        return new TeamCreateRequest(
                groupId,
                UUID.randomUUID().toString(),
                "test intro",
                representativeId,
                memberSlots
        );
    }

    public static Team createTeamWithReflection(User representative) {
        Set<TeamMember> members = createTeamMembersWithReflection(representative);
        return Team.create(createTeamCreateRequest(representative.getId(), toSlots(members)), members);
    }

    public static Team createTeamWithReflection(User representative, Long groupId) {
        Set<TeamMember> members = createTeamMembersWithReflection(representative);
        return Team.create(createTeamCreateRequest(representative.getId(), toSlots(members), groupId), members);
    }

    public static Set<TeamCreateRequest.TeamMemberSlot> toSlots(Set<TeamMember> members) {
        return members.stream()
                .map(member -> new TeamCreateRequest.TeamMemberSlot(
                        member.getUser().getId(),
                        member.getPosition()
                ))
                .collect(Collectors.toSet());
    }
}
