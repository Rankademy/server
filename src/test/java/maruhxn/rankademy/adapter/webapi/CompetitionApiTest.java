package maruhxn.rankademy.adapter.webapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import maruhxn.rankademy.RankademyTestConfiguration;
import maruhxn.rankademy.adapter.security.model.RankademyUser;
import maruhxn.rankademy.adapter.security.model.UserInfo;
import maruhxn.rankademy.application.competition.provided.dto.CompetitionDetailResponse;
import maruhxn.rankademy.application.competition.provided.dto.CompetitionPageResponse;
import maruhxn.rankademy.application.competition.provided.dto.CompetitionResultResponse;
import maruhxn.rankademy.application.competition.required.CompetitionRepository;
import maruhxn.rankademy.application.group.required.GroupRepository;
import maruhxn.rankademy.application.team.required.TeamRepository;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.competition.Competition;
import maruhxn.rankademy.domain.competition.CompetitionStatus;
import maruhxn.rankademy.domain.competition.dto.OpposeResultRequest;
import maruhxn.rankademy.domain.competition.dto.SubmitCompetitionResultRequest;
import maruhxn.rankademy.domain.group.Group;
import maruhxn.rankademy.domain.group.GroupFixture;
import maruhxn.rankademy.domain.shared.TimeProvider;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static maruhxn.rankademy.domain.group.GroupFixture.createGroup;
import static maruhxn.rankademy.domain.group.GroupFixture.createMember;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(RankademyTestConfiguration.class)
@DisplayName("Competition API 테스트")
class CompetitionApiTest {

    static final String BASE_URL = "/api/v1/competitions";

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
    CompetitionRepository competitionRepository;

    @Autowired
    TimeProvider timeProvider;

    private User myTeamLeader, otherTeamLeader, otherUser;
    private Group group1, group2;
    private Team myTeam, otherTeam;
    private Competition competition;

    @BeforeEach
    void setUp() {
        myTeamLeader = GroupFixture.createMember("myTeamLeader@test.com", "myTeamLeader");
        userRepository.save(myTeamLeader);
        otherTeamLeader = GroupFixture.createMember("otherTeamLeader@test.com", "otherTeamLeader");
        userRepository.save(otherTeamLeader);
        otherUser = GroupFixture.createMember("otherUser@test.com", "otherUser");
        userRepository.save(otherUser);

        group1 = createGroup(myTeamLeader, "test group1");
        groupRepository.save(group1);
        group2 = createGroup(otherTeamLeader, "test group2");
        groupRepository.save(group2);

        myTeam = makeTeam("myTeam", myTeamLeader, group1.getId());
        teamRepository.save(myTeam);
        otherTeam = makeTeam("otherTeam", otherTeamLeader, group2.getId());
        teamRepository.save(otherTeam);
        competition = Competition.createAfterAccept(myTeam.getId(), otherTeam.getId());
        competitionRepository.save(competition);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("내 대결 목록 조회 - 성공")
    void getMyCompetitions_success() throws Exception {
        // when
        var result = mvcTester.get().uri(BASE_URL + "/my?page=0")
                .with(user(RankademyUser.from(UserInfo.from(myTeamLeader))))
                .exchange();

        // then
        assertThat(result).hasStatusOk();
        CompetitionPageResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), CompetitionPageResponse.class);
        assertThat(response.totalCount()).isEqualTo(1);
        assertThat(response.competitions()).hasSize(1);
        assertThat(response.competitions().get(0).competitionId()).isEqualTo(competition.getId());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("내 대결 목록 조회 - 미인증 사용자")
    void getMyCompetitions_unauthorized() {
        // when
        var result = mvcTester.get().uri(BASE_URL + "/my?page=0")
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("그룹 대결 목록 조회 - 성공")
    void getGroupCompetitions_success() throws Exception {
        // when
        var result = mvcTester.get().uri(BASE_URL + "/groups/" + group1.getId() + "?page=0")
                .with(user(RankademyUser.from(UserInfo.from(myTeamLeader))))
                .exchange();

        // then
        assertThat(result).hasStatusOk();
        CompetitionPageResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), CompetitionPageResponse.class);
        assertThat(response.totalCount()).isEqualTo(1);
        assertThat(response.competitions()).hasSize(1);
        assertThat(response.competitions().get(0).competitionId()).isEqualTo(competition.getId());
    }

    @Test
    @DisplayName("대결 상세 조회 - 성공")
    void getCompetitionDetail_success() throws Exception {
        // when
        var result = mvcTester.get().uri(BASE_URL + "/" + competition.getId())
                .with(user(RankademyUser.from(UserInfo.from(myTeamLeader))))
                .exchange();

        // then
        assertThat(result).hasStatusOk();
        CompetitionDetailResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), CompetitionDetailResponse.class);
        assertThat(response.competitionId()).isEqualTo(competition.getId());
        assertThat(response.team1().teamId()).isEqualTo(myTeam.getId());
        assertThat(response.team2().teamId()).isEqualTo(otherTeam.getId());
    }

    @Test
    @DisplayName("대결 결과 조회 - 성공")
    void getCompetitionResult_success() throws Exception {
        // given
        SubmitCompetitionResultRequest request = new SubmitCompetitionResultRequest(
                myTeam.getId(),
                otherTeam.getId(),
                1,
                List.of(new SubmitCompetitionResultRequest.SetResultDto(1, myTeam.getId(), "k")),
                "m",
                myTeam.getId(),
                myTeam.getGroupId(),
                otherTeam.getGroupId()
        );
        competition.submitSetResult(request, timeProvider.getCurrentTime());
        competitionRepository.save(competition);
        em.flush();
        em.clear();

        // when
        var result = mvcTester.get().uri(BASE_URL + "/" + competition.getId() + "/result")
                .with(user(RankademyUser.from(UserInfo.from(myTeamLeader))))
                .exchange();

        // then
        assertThat(result).hasStatusOk();
        CompetitionResultResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), CompetitionResultResponse.class);
        assertThat(response.competitionId()).isEqualTo(competition.getId());
        assertThat(response.finalWinnerTeamId()).isEqualTo(myTeam.getId());
        assertThat(response.setResults()).hasSize(1);
        assertThat(response.setResults().get(0).setNumber()).isEqualTo(1);
    }

    @Test
    @DisplayName("대결 결과 제출 - 성공")
    void submitCompetitionResult_success() throws Exception {
        // given
        List<SubmitCompetitionResultRequest.SetResultDto> setResults = List.of(
                new SubmitCompetitionResultRequest.SetResultDto(1, myTeam.getId(), "image-key-1.jpg")
        );

        SubmitCompetitionResultRequest request = new SubmitCompetitionResultRequest(
                myTeam.getId(),
                otherTeam.getId(),
                1, // totalSets
                setResults,
                "GG",
                myTeam.getId(),
                myTeam.getGroupId(),
                otherTeam.getGroupId()
        );

        // when
        var result = mvcTester.post().uri(BASE_URL + "/" + competition.getId() + "/results")
                .with(user(RankademyUser.from(UserInfo.from(myTeamLeader))))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.CREATED);
        Competition updatedCompetition = competitionRepository.findById(competition.getId()).orElseThrow();
        assertThat(updatedCompetition.getStatus()).isEqualTo(CompetitionStatus.COMPLETED);
        assertThat(updatedCompetition.getSetResults()).hasSize(1);
        assertThat(updatedCompetition.getSetResults().get(0).getSetNumber()).isEqualTo(1);
        assertThat(updatedCompetition.getSetResults().get(0).getWinnerTeamId()).isEqualTo(myTeam.getId());
        assertThat(updatedCompetition.getFinalWinnerTeamId()).isEqualTo(myTeam.getId());
    }

    @Test
    @DisplayName("대결 결과 제출 - 관련 없는 사용자")
    void submitCompetitionResult_forbidden() throws Exception {
        // given
        SubmitCompetitionResultRequest request = new SubmitCompetitionResultRequest(
                myTeam.getId(),
                otherTeam.getId(),
                1,
                List.of(new SubmitCompetitionResultRequest.SetResultDto(1, myTeam.getId(), "k")),
                "m",
                myTeam.getId(),
                myTeam.getGroupId(),
                otherTeam.getGroupId()
        );

        // when
        var result = mvcTester.post().uri(BASE_URL + "/" + competition.getId() + "/results")
                .with(user(RankademyUser.from(UserInfo.from(otherUser))))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("대결 결과 이의 제기 - 성공")
    void opposeCompetitionResult_success() throws Exception {
        // given
        // First, submit a result
        competition.submitSetResult(
                new SubmitCompetitionResultRequest(
                        myTeam.getId(),
                        otherTeam.getId(),
                        1,
                        List.of(new SubmitCompetitionResultRequest.SetResultDto(1, myTeam.getId(), "k")),
                        "m",
                        myTeam.getId(),
                        myTeam.getGroupId(),
                        otherTeam.getGroupId()
                ),
                timeProvider.getCurrentTime()
        );
        competitionRepository.save(competition);
        em.flush();
        em.clear();

        OpposeResultRequest request = new OpposeResultRequest("Invalid result");

        // when
        var result = mvcTester.post().uri(BASE_URL + "/" + competition.getId() + "/oppose")
                .with(user(RankademyUser.from(UserInfo.from(otherTeamLeader))))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.CREATED);
        Competition updatedCompetition = competitionRepository.findById(competition.getId()).orElseThrow();
        assertThat(updatedCompetition.getStatus()).isEqualTo(CompetitionStatus.OPPOSED);
        assertThat(updatedCompetition.getOpposedReason()).isEqualTo("Invalid result");
    }

    @Test
    @DisplayName("대결 결과 이의 제기 - 관련 없는 사용자")
    void opposeCompetitionResult_forbidden() throws Exception {
        // given
        OpposeResultRequest request = new OpposeResultRequest("Invalid result");

        // when
        var result = mvcTester.post().uri(BASE_URL + "/" + competition.getId() + "/oppose")
                .with(user(RankademyUser.from(UserInfo.from(otherUser))))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
    }


    private Team makeTeam(String name, User leader, Long groupId) {
        Set<TeamMember> members = new HashSet<>();
        members.add(new TeamMember(leader, LolPosition.TOP));
        for (int i = 0; i < 4; i++) {
            User member = createMember(name + "member" + i + "@test.com", name + "member" + i);
            userRepository.save(member);
            members.add(new TeamMember(member, LolPosition.values()[(i + 1) % 5]));
        }
        TeamCreateRequest request = new TeamCreateRequest(
                groupId,
                name,
                "intro of " + name,
                leader.getId(),
                members
        );
        return Team.create(request);
    }
}
