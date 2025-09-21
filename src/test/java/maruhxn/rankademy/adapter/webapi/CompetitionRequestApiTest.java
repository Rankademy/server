package maruhxn.rankademy.adapter.webapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import maruhxn.rankademy.RankademyTestConfiguration;
import maruhxn.rankademy.adapter.security.model.RankademyUser;
import maruhxn.rankademy.adapter.security.model.UserInfo;
import maruhxn.rankademy.application.competition.required.CompetitionRepository;
import maruhxn.rankademy.application.competitionrequest.required.CompetitionRequestQueryRepository;
import maruhxn.rankademy.application.competitionrequest.required.CompetitionRequestRepository;
import maruhxn.rankademy.application.competitionrequest.required.dto.CompetitionRequestPageResponse;
import maruhxn.rankademy.application.group.required.GroupRepository;
import maruhxn.rankademy.application.team.required.TeamRepository;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.competitionrequest.CompetitionRequest;
import maruhxn.rankademy.domain.competitionrequest.CompetitionRequestStatus;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static maruhxn.rankademy.domain.group.GroupFixture.createGroup;
import static maruhxn.rankademy.domain.user.UserFixture.createUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(RankademyTestConfiguration.class)
@DisplayName("CompetitionRequest API 테스트")
class CompetitionRequestApiTest {

    static final String BASE_URL = "/api/v1/competition-requests";

    @Autowired
    MockMvcTester mvcTester;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    EntityManager em;

    @Autowired
    UserRepository userRepository;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    CompetitionRequestRepository competitionRequestRepository;

    @Autowired
    CompetitionRequestQueryRepository competitionRequestQueryRepository;

    @Autowired
    CompetitionRepository competitionRepository;

    private User myTeamLeader, otherTeamLeader, otherUser;
    private Group myGroup, otherGroup;
    private Team myTeam, otherTeam;

    @BeforeEach
    void setUp() {
        myTeamLeader = GroupFixture.createMember("myTeamLeader@test.com", "myTeamLeader");
        userRepository.save(myTeamLeader);
        otherTeamLeader = GroupFixture.createMember("otherTeamLeader@test.com", "otherTeamLeader");
        userRepository.save(otherTeamLeader);
        otherUser = GroupFixture.createMember("otherUser@test.com", "otherUser");
        userRepository.save(otherUser);

        myGroup = createGroup(myTeamLeader, "my group");
        otherGroup = createGroup(otherTeamLeader, "other group");
        groupRepository.save(myGroup);
        groupRepository.save(otherGroup);

        myTeam = makeTeam("myTeam", myTeamLeader, myGroup);
        teamRepository.save(myTeam);
        otherTeam = makeTeam("otherTeam", otherTeamLeader, otherGroup);
        teamRepository.save(otherTeam);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("대결 요청 보내기 - 성공")
    void sendCompetitionRequest_success() {
        // when
        var result = mvcTester.post().uri(BASE_URL + "/" + myTeam.getId() + "/send?otherTeamId=" + otherTeam.getId())
                .with(user(RankademyUser.from(UserInfo.from(myTeamLeader))))
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.CREATED);
        CompetitionRequestPageResponse request = competitionRequestQueryRepository.findAll(otherTeam.getId(), 0);
        assertThat(request.totalCount()).isEqualTo(1);
        CompetitionRequestPageResponse.CompetitionRequestResponse response = request.competitionRequests().get(0);
        assertThat(response.fromTeamId()).isEqualTo(myTeam.getId());
    }

    @Test
    @DisplayName("대결 요청 보내기 - 팀 리더가 아닌 경우")
    void sendCompetitionRequest_forbidden() {
        // when
        var result = mvcTester.post().uri(BASE_URL + "/" + myTeam.getId() + "/send?otherTeamId=" + otherTeam.getId())
                .with(user(RankademyUser.from(UserInfo.from(otherUser))))
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    @WithAnonymousUser
    @DisplayName("대결 요청 보내기 - 인증되지 않은 사용자")
    void sendCompetitionRequest_unauthorized() {
        // when
        var result = mvcTester.post().uri(BASE_URL + "/" + myTeam.getId() + "/send?otherTeamId=" + otherTeam.getId())
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("대결 요청 목록 조회 - 성공")
    void getCompetitionRequests_success() throws Exception {
        // given
        CompetitionRequest newRequest = new CompetitionRequest(myTeam.getId(), otherTeam.getId(), LocalDateTime.now());
        competitionRequestRepository.save(newRequest);
        em.flush();
        em.clear();

        // when
        var result = mvcTester.get().uri(BASE_URL + "/" + otherTeam.getId() + "?page=0")
                .with(user(RankademyUser.from(UserInfo.from(otherTeamLeader))))
                .exchange();

        // then
        assertThat(result).hasStatusOk();
        CompetitionRequestPageResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), CompetitionRequestPageResponse.class);
        assertThat(response.competitionRequests()).hasSize(1);
        assertThat(response.competitionRequests().get(0).requestId()).isEqualTo(newRequest.getId());
    }

    @Test
    @DisplayName("대결 요청 목록 조회 - 팀 리더가 아닌 경우")
    void getCompetitionRequests_forbidden() {
        // when
        var result = mvcTester.get().uri(BASE_URL + "/" + myTeam.getId() + "?page=0")
                .with(user(RankademyUser.from(UserInfo.from(otherTeamLeader))))
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("대결 요청 수락 - 성공")
    void acceptCompetitionRequest_success() {
        // given
        CompetitionRequest newRequest = new CompetitionRequest(myTeam.getId(), otherTeam.getId(), LocalDateTime.now());
        competitionRequestRepository.save(newRequest);
        em.flush();
        em.clear();

        // when
        var result = mvcTester.patch().uri(BASE_URL + "/" + otherTeam.getId() + "/accept/" + newRequest.getId())
                .with(user(RankademyUser.from(UserInfo.from(otherTeamLeader))))
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.NO_CONTENT);
        CompetitionRequest acceptedRequest = competitionRequestRepository.findById(newRequest.getId()).orElseThrow();
        assertThat(acceptedRequest.getStatus()).isEqualTo(CompetitionRequestStatus.ACCEPTED);
    }

    @Test
    @DisplayName("대결 요청 거절 - 성공")
    void rejectCompetitionRequest_success() {
        // given
        CompetitionRequest newRequest = new CompetitionRequest(myTeam.getId(), otherTeam.getId(), LocalDateTime.now());
        competitionRequestRepository.save(newRequest);
        em.flush();
        em.clear();

        // when
        var result = mvcTester.patch().uri(BASE_URL + "/" + otherTeam.getId() + "/reject/" + newRequest.getId())
                .with(user(RankademyUser.from(UserInfo.from(otherTeamLeader))))
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.NO_CONTENT);
        CompetitionRequest rejectedRequest = competitionRequestRepository.findById(newRequest.getId()).orElseThrow();
        assertThat(rejectedRequest.getStatus()).isEqualTo(CompetitionRequestStatus.REJECTED);
    }

    @Test
    @DisplayName("대결 요청 처리 - 요청받은 팀의 리더가 아닌 경우")
    void handleCompetitionRequest_forbidden() {
        // given
        CompetitionRequest newRequest = new CompetitionRequest(otherTeam.getId(), myTeam.getId(), LocalDateTime.now());
        competitionRequestRepository.save(newRequest);
        em.flush();
        em.clear();

        // when
        var acceptResult = mvcTester.patch().uri(BASE_URL + "/" + myTeam.getId() + "/accept/" + newRequest.getId())
                .with(user(RankademyUser.from(UserInfo.from(otherTeamLeader))))
                .exchange();
        var rejectResult = mvcTester.patch().uri(BASE_URL + "/" + myTeam.getId() + "/reject/" + newRequest.getId())
                .with(user(RankademyUser.from(UserInfo.from(otherTeamLeader))))
                .exchange();

        // then
        assertThat(acceptResult).hasStatus(HttpStatus.FORBIDDEN);
        assertThat(rejectResult).hasStatus(HttpStatus.FORBIDDEN);
    }


    private Team makeTeam(String name, User leader, Group group) {
        Set<TeamMember> members = new HashSet<>();
        members.add(new TeamMember(leader, LolPosition.TOP));
        for (int i = 0; i < 4; i++) {
            User member = createUser(name + "member" + i + "@test.com", name + "member" + i);
            userRepository.save(member);
            members.add(new TeamMember(member, LolPosition.values()[(i + 1) % 5]));
        }
        TeamCreateRequest request = new TeamCreateRequest(
                group.getId(),
                name,
                "intro of " + name,
                leader.getId(),
                members
        );
        return Team.create(request);
    }
}
