package maruhxn.rankademy.application.competitionrequest;

import jakarta.persistence.EntityManager;
import maruhxn.rankademy.application.competitionrequest.provided.CompetitionRequestReader;
import maruhxn.rankademy.application.competitionrequest.required.CompetitionRequestRepository;
import maruhxn.rankademy.application.competitionrequest.required.dto.CompetitionRequestPageResponse;
import maruhxn.rankademy.application.group.required.GroupRepository;
import maruhxn.rankademy.application.team.required.TeamRepository;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.competitionrequest.CompetitionRequest;
import maruhxn.rankademy.domain.group.Group;
import maruhxn.rankademy.domain.group.GroupFixture;
import maruhxn.rankademy.domain.team.Team;
import maruhxn.rankademy.domain.team.TeamMember;
import maruhxn.rankademy.domain.team.dto.TeamCreateRequest;
import maruhxn.rankademy.domain.user.LolPosition;
import maruhxn.rankademy.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("CompetitionRequestQueryService 테스트")
class CompetitionRequestQueryServiceTest {

    @Autowired
    private CompetitionRequestReader competitionRequestReader;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompetitionRequestRepository competitionRequestRepository;

    @Autowired
    private EntityManager em;

    private User user;
    private Group group;
    private Team myTeam;

    @BeforeEach
    void setUp() {
        user = GroupFixture.createLeader("user");
        userRepository.save(user);
        group = GroupFixture.createGroup(user, "group");
        groupRepository.save(group);
        myTeam = createTeam("myTeam", group.getId(), user);
        teamRepository.save(myTeam);
    }

    @Test
    @DisplayName("대항전 요청 목록 조회 - 페이징")
    void getRequests_withPaging() {
        // given

        // Create 25 requests
        for (int i = 0; i < 25; i++) {
            User u = GroupFixture.createLeader("user_" + i);
            userRepository.save(u);
            Group g = GroupFixture.createGroup(u, "group_" + i);
            groupRepository.save(g);
            Team team = createTeam("team" + i, g.getId(), u);
            teamRepository.save(team);

            CompetitionRequest request = new CompetitionRequest(team.getId(), myTeam.getId(), LocalDateTime.now());
            competitionRequestRepository.save(request);
        }

        em.flush();
        em.clear();


        // when
        CompetitionRequestPageResponse firstPage = competitionRequestReader.getRequests(myTeam.getId(), 0);
        CompetitionRequestPageResponse secondPage = competitionRequestReader.getRequests(myTeam.getId(), 1);

        // then
        assertThat(firstPage.totalCount()).isEqualTo(25);
        assertThat(firstPage.teams()).hasSize(20);

        assertThat(secondPage.totalCount()).isEqualTo(25);
        assertThat(secondPage.teams()).hasSize(5);
    }

    @Test
    @DisplayName("대항전 요청 목록 조회 - 요청이 없음")
    void getRequests_withNoRequests() {
        // when
        CompetitionRequestPageResponse result = competitionRequestReader.getRequests(myTeam.getId(), 0);

        // then
        assertThat(result.totalCount()).isEqualTo(0);
        assertThat(result.teams()).isEmpty();
    }

    private Team createTeam(String name, Long groupId, User representative) {
        Set<TeamMember> members = new HashSet<>();
        members.add(new TeamMember(representative, LolPosition.TOP));

        for (int j = 0; j < 4; j++) {
            User memberUser = GroupFixture.createMember(name + "-member" + j + "@test.com", name + "-member" + j);
            userRepository.save(memberUser);
            members.add(new TeamMember(memberUser, LolPosition.values()[j + 1]));
        }

        TeamCreateRequest request = new TeamCreateRequest(
                groupId,
                name,
                "test intro for " + name,
                representative.getId(),
                members
        );

        return Team.create(request);
    }
}
