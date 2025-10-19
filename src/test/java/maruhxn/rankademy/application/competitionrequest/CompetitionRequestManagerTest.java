package maruhxn.rankademy.application.competitionrequest;

import jakarta.persistence.EntityManager;
import maruhxn.rankademy.application.competitionrequest.provided.CompetitionRequestManager;
import maruhxn.rankademy.application.competitionrequest.required.CompetitionRequestRepository;
import maruhxn.rankademy.application.group.required.GroupRepository;
import maruhxn.rankademy.application.team.required.TeamRepository;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.competitionrequest.CompetitionRequest;
import maruhxn.rankademy.domain.competitionrequest.CompetitionRequestStatus;
import maruhxn.rankademy.domain.group.Group;
import maruhxn.rankademy.domain.group.GroupFixture;
import maruhxn.rankademy.domain.team.Team;
import maruhxn.rankademy.domain.team.TeamFixture;
import maruhxn.rankademy.domain.team.TeamMember;
import maruhxn.rankademy.domain.team.dto.TeamCreateRequest;
import maruhxn.rankademy.domain.user.LolPosition;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CompetitionRequestManager 테스트")
class CompetitionRequestManagerTest extends IntegrationTestSupport {

    @Autowired
    private CompetitionRequestManager competitionRequestManager;

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

    private User user1, user2;
    private Group group1, group2;
    private Team team1, team2;

    @BeforeEach
    void setUp() {
        user1 = GroupFixture.createLeader("user1");
        userRepository.save(user1);
        group1 = GroupFixture.createGroup(user1);
        groupRepository.save(group1);
        team1 = createTeam("team1", group1.getId(), user1);
        teamRepository.save(team1);

        user2 = GroupFixture.createLeader("user2");
        userRepository.save(user2);
        group2 = GroupFixture.createGroup(user2, "group2");
        groupRepository.save(group2);
        team2 = createTeam("team2", group2.getId(), user2);
        teamRepository.save(team2);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("대항전 요청 - 성공")
    void sendRequest() {
        // when
        CompetitionRequest competitionRequest = competitionRequestManager.sendRequest(user1.getId(), team1.getId(), team2.getId());

        // then
        assertThat(competitionRequest).isNotNull();
        assertThat(competitionRequest.getFromTeamId()).isEqualTo(team1.getId());
        assertThat(competitionRequest.getToTeamId()).isEqualTo(team2.getId());
        assertThat(competitionRequest.getStatus()).isEqualTo(CompetitionRequestStatus.PENDING);
    }

    @Test
    @DisplayName("대항전 요청 실패 - From 팀이 존재하지 않음")
    void sendRequest_fail_fromTeamNotFound() {
        // when & then
        assertThatThrownBy(() -> competitionRequestManager.sendRequest(user1.getId(), 999L, team2.getId()))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("대항전 요청 실패 - To 팀이 존재하지 않음")
    void sendRequest_fail_toTeamNotFound() {
        // when & then
        assertThatThrownBy(() -> competitionRequestManager.sendRequest(user1.getId(), team1.getId(), 999L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("대항전 요청 수락 - 성공")
    void acceptRequest() {
        // given
        CompetitionRequest request = new CompetitionRequest(team1.getId(), team2.getId(), LocalDateTime.now());
        competitionRequestRepository.save(request);
        em.flush();
        em.clear();

        // when
        competitionRequestManager.acceptRequest(user2.getId(), request.getId());
        em.flush();
        em.clear();

        // then
        CompetitionRequest findRequest = competitionRequestRepository.findById(request.getId()).get();
        assertThat(findRequest.getStatus()).isEqualTo(CompetitionRequestStatus.ACCEPTED);
    }

    @Test
    @DisplayName("대항전 요청 수락 실패 - 요청이 존재하지 않음")
    void acceptRequest_fail_requestNotFound() {
        // when & then
        assertThatThrownBy(() -> competitionRequestManager.acceptRequest(user2.getId(), 999L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("대항전 요청 거절 - 성공")
    void rejectRequest() {
        // given
        CompetitionRequest request = new CompetitionRequest(team1.getId(), team2.getId(), LocalDateTime.now());
        competitionRequestRepository.save(request);
        em.flush();
        em.clear();

        // when
        competitionRequestManager.rejectRequest(user2.getId(), request.getId());
        em.flush();
        em.clear();

        // then
        CompetitionRequest findRequest = competitionRequestRepository.findById(request.getId()).get();
        assertThat(findRequest.getStatus()).isEqualTo(CompetitionRequestStatus.REJECTED);
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
                TeamFixture.toSlots(members)
        );

        return Team.create(request, members);
    }
}
