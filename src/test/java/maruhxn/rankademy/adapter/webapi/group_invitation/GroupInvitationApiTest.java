package maruhxn.rankademy.adapter.webapi.group_invitation;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import maruhxn.rankademy.RankademyTestConfiguration;
import maruhxn.rankademy.adapter.security.model.RankademyUser;
import maruhxn.rankademy.adapter.security.model.UserInfo;
import maruhxn.rankademy.application.group.required.GroupRepository;
import maruhxn.rankademy.application.group_invitation.dto.GroupInvitationPageResponse;
import maruhxn.rankademy.application.group_invitation.required.GroupInvitationQueryRepository;
import maruhxn.rankademy.application.group_invitation.required.GroupInvitationRepository;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.group.Group;
import maruhxn.rankademy.domain.group.GroupFixture;
import maruhxn.rankademy.domain.group_invitation.GroupInvitation;
import maruhxn.rankademy.domain.group_invitation.GroupInvitationStatus;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(RankademyTestConfiguration.class)
@DisplayName("GroupInvitation API")
class GroupInvitationApiTest {

    private static final String BASE_URL = "/api/v1/groups";

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
    GroupInvitationRepository groupInvitationRepository;

    @Autowired
    GroupInvitationQueryRepository groupInvitationQueryRepository;

    private User leader;
    private Group group;

    @BeforeEach
    void setUp() {
        leader = userRepository.save(GroupFixture.createLeader());
        group = groupRepository.save(GroupFixture.createGroup(leader));
    }

    @Test
    @DisplayName("내게 온 그룹 초대 목록 조회 - 성공")
    void getInvitations_success() throws Exception {
        // given
        User invitee = userRepository.save(GroupFixture.createMember("invitee@test.com", "invitee"));
        Long inviteeId = invitee.getId();
        groupInvitationRepository.save(new GroupInvitation(group.getId(), inviteeId));

        em.flush();
        em.clear();

        // when
        MvcTestResult result = mvcTester.get()
                .uri(BASE_URL + "/" + group.getId() + "/invitation")
                .with(user(RankademyUser.from(UserInfo.from(invitee))))
                .param("page", "0")
                .exchange();

        // then
        assertThat(result).hasStatusOk();
        GroupInvitationPageResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                GroupInvitationPageResponse.class
        );

        assertThat(response.totalCount()).isEqualTo(1L);
        assertThat(response.competitionRequests()).hasSize(1);
        GroupInvitationPageResponse.GroupInvitationResponse invitation = response.competitionRequests().get(0);
        assertThat(invitation.groupId()).isEqualTo(group.getId());
        assertThat(invitation.userId()).isEqualTo(inviteeId);
    }

    @Test
    @DisplayName("그룹 초대 전송 - 성공")
    void sendInvitation_success() {
        // given
        User invitee = userRepository.save(GroupFixture.createMember("invitee2@test.com", "invitee2"));
        Long inviteeId = invitee.getId();

        // when
        MvcTestResult result = mvcTester.post()
                .uri(BASE_URL + "/" + group.getId() + "/invitation/send")
                .param("userId", inviteeId.toString())
                .with(user(principalOf(leader.getId())))
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.CREATED);

        em.flush();
        em.clear();

        GroupInvitationPageResponse page = groupInvitationQueryRepository.getInvitations(inviteeId, 0);
        assertThat(page.totalCount()).isEqualTo(1L);
        assertThat(page.competitionRequests()).hasSize(1);
        assertThat(page.competitionRequests().get(0).groupId()).isEqualTo(group.getId());
    }

    @Test
    @DisplayName("그룹 초대 수락 - 성공")
    void acceptInvitation_success() {
        // given
        User invitee = userRepository.save(GroupFixture.createMember("accept@test.com", "accept"));
        Long inviteeId = invitee.getId();
        Long invitationId = groupInvitationRepository.save(new GroupInvitation(group.getId(), inviteeId)).getId();

        em.flush();
        em.clear();

        // when
        MvcTestResult result = mvcTester.patch()
                .uri(BASE_URL + "/" + group.getId() + "/invitation/accept/" + invitationId)
                .with(user(principalOf(inviteeId)))
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.NO_CONTENT);

        em.flush();
        em.clear();

        GroupInvitation updated = groupInvitationRepository.findById(invitationId).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(GroupInvitationStatus.ACCEPTED);
        Group updatedGroup = groupRepository.findById(group.getId()).orElseThrow();
        assertThat(updatedGroup.getMembers())
                .anyMatch(member -> member.getUser().getId().equals(inviteeId));
    }

    @Test
    @DisplayName("그룹 초대 거절 - 성공")
    void rejectInvitation_success() {
        // given
        User invitee = userRepository.save(GroupFixture.createMember("reject@test.com", "reject"));
        Long inviteeId = invitee.getId();
        Long invitationId = groupInvitationRepository.save(new GroupInvitation(group.getId(), inviteeId)).getId();

        em.flush();
        em.clear();

        // when
        MvcTestResult result = mvcTester.patch()
                .uri(BASE_URL + "/" + group.getId() + "/invitation/reject/" + invitationId)
                .with(user(principalOf(inviteeId)))
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.NO_CONTENT);

        em.flush();
        em.clear();

        GroupInvitation updated = groupInvitationRepository.findById(invitationId).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(GroupInvitationStatus.REJECTED);
    }

    private RankademyUser principalOf(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return RankademyUser.from(UserInfo.from(user));
    }
}
