package maruhxn.rankademy.adapter.webapi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import maruhxn.rankademy.RankademyTestConfiguration;
import maruhxn.rankademy.adapter.security.model.RankademyUser;
import maruhxn.rankademy.adapter.security.model.UserInfo;
import maruhxn.rankademy.application.group.provided.dto.JoinRequestResponse;
import maruhxn.rankademy.application.group.required.GroupRepository;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.group.Group;
import maruhxn.rankademy.domain.group.GroupFixture;
import maruhxn.rankademy.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(RankademyTestConfiguration.class)
class GroupJoinRequestApiTest {

    static final String BASE_URL = "/api/v1/groups";

    @Autowired
    MockMvcTester mvcTester;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    EntityManager em;

    private User leader;
    private Group group;

    @BeforeEach
    void setUp() {
        leader = userRepository.save(GroupFixture.createLeader());
        group = groupRepository.save(GroupFixture.createGroup(leader));
    }

    @Test
    @DisplayName("그룹 가입 요청 목록 조회 - 성공")
    void getJoinRequests() throws Exception {
        // given
        for (int i = 0; i < 5; i++) {
            User requester = userRepository.save(GroupFixture.createMember("requester" + i + "@test.com", "r" + i));
            group.addJoinRequest(requester);
        }
        groupRepository.save(group);

        em.flush();
        em.clear();

        // when
        MvcTestResult result = mvcTester.get().uri(BASE_URL + "/" + group.getId() + "/join-requests")
                .with(user(RankademyUser.from(UserInfo.from(leader))))
                .param("page", "0")
                .exchange();

        // then
        assertThat(result).hasStatusOk();
        List<JoinRequestResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
        });
        assertThat(response).hasSize(5);
    }

    @Test
    @DisplayName("그룹 가입 요청 목록 조회 - 리더가 아닌 경우")
    void getJoinRequests_withNotLeader() throws Exception {
        // given
        User member = userRepository.save(GroupFixture.createMember());

        // when
        MvcTestResult result = mvcTester.get().uri(BASE_URL + "/" + group.getId() + "/join-requests")
                .with(user(RankademyUser.from(UserInfo.from(member))))
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("그룹 가입 요청 보내기 - 성공")
    void sendJoinRequest() throws Exception {
        // given
        User requester = userRepository.save(GroupFixture.createMember());

        // when
        MvcTestResult result = mvcTester.post().uri(BASE_URL + "/" + group.getId() + "/join-requests")
                .with(user(RankademyUser.from(UserInfo.from(requester))))
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.CREATED);

        em.flush();
        em.clear();

        Group updatedGroup = groupRepository.findById(group.getId()).get();
        assertThat(updatedGroup.getJoinRequests()).anyMatch(req -> req.userId().equals(requester.getId()));
    }

    @Test
    @DisplayName("그룹 가입 요청 보내기 - 이미 가입된 멤버")
    void sendJoinRequest_alreadyMember() throws Exception {
        // when
        MvcTestResult result = mvcTester.post().uri(BASE_URL + "/" + group.getId() + "/join-requests")
                .with(user(RankademyUser.from(UserInfo.from(leader))))
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("그룹 가입 요청 수락 - 성공")
    void acceptJoinRequest() throws Exception {
        // given
        User requester = userRepository.save(GroupFixture.createMember());
        group.addJoinRequest(requester);
        groupRepository.save(group);

        em.flush();
        em.clear();

        // when
        MvcTestResult result = mvcTester.patch().uri(BASE_URL + "/" + group.getId() + "/join-requests/" + requester.getId() + "/accept")
                .with(user(RankademyUser.from(UserInfo.from(leader))))
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.NO_CONTENT);

        Group updatedGroup = groupRepository.findById(group.getId()).get();
        assertThat(updatedGroup.getMembers()).anyMatch(member -> member.getUser().getId().equals(requester.getId()));
        assertThat(updatedGroup.getJoinRequests()).noneMatch(req -> req.userId().equals(requester.getId()));
    }

    @Test
    @DisplayName("그룹 가입 요청 수락 - 리더가 아닌 경우")
    void acceptJoinRequest_withNotLeader() throws Exception {
        // given
        User member = userRepository.save(GroupFixture.createMember());
        User requester = userRepository.save(GroupFixture.createMember("requester@test.com", "requestor"));
        group.addJoinRequest(requester);
        groupRepository.save(group);

        em.flush();
        em.clear();

        // when
        MvcTestResult result = mvcTester.patch().uri(BASE_URL + "/" + group.getId() + "/join-requests/" + requester.getId() + "/accept")
                .with(user(RankademyUser.from(UserInfo.from(member))))
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("그룹 가입 요청 거절 - 성공")
    void rejectJoinRequest() throws Exception {
        // given
        User requester = userRepository.save(GroupFixture.createMember());
        group.addJoinRequest(requester);
        groupRepository.save(group);

        em.flush();
        em.clear();

        // when
        MvcTestResult result = mvcTester.patch().uri(BASE_URL + "/" + group.getId() + "/join-requests/" + requester.getId() + "/reject")
                .with(user(RankademyUser.from(UserInfo.from(leader))))
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.NO_CONTENT);

        Group updatedGroup = groupRepository.findById(group.getId()).get();
        assertThat(updatedGroup.getMembers()).noneMatch(member -> member.getUser().getId().equals(requester.getId()));
        assertThat(updatedGroup.getJoinRequests()).noneMatch(req -> req.userId().equals(requester.getId()));
    }

    @Test
    @DisplayName("그룹 가입 요청 거절 - 인증되지 않은 사용자")
    void rejectJoinRequest_withAnonymousUser() throws Exception {
        // given
        User requester = userRepository.save(GroupFixture.createMember());
        group.addJoinRequest(requester);
        groupRepository.save(group);

        em.flush();
        em.clear();

        // when
        MvcTestResult result = mvcTester.patch().uri(BASE_URL + "/" + group.getId() + "/join-requests/" + requester.getId() + "/reject")
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }
}
