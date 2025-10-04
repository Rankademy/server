package maruhxn.rankademy.adapter.webapi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import maruhxn.rankademy.RankademyTestConfiguration;
import maruhxn.rankademy.adapter.security.model.RankademyUser;
import maruhxn.rankademy.adapter.security.model.UserInfo;
import maruhxn.rankademy.application.group.provided.dto.*;
import maruhxn.rankademy.application.group.required.GroupRepository;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.group.Group;
import maruhxn.rankademy.domain.group.GroupFixture;
import maruhxn.rankademy.domain.group.GroupRole;
import maruhxn.rankademy.domain.group.dto.GroupUpdateRequest;
import maruhxn.rankademy.domain.user.User;
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
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(RankademyTestConfiguration.class)
class GroupApiTest {

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

    @Test
    @DisplayName("내 그룹 목록 조회 - 성공")
    void getMyGroups() throws Exception {
        User member = GroupFixture.createMember();
        userRepository.save(member);

        User leader = GroupFixture.createLeader();
        userRepository.save(leader);

        for (int i = 0; i < 5; i++) {
            Group group = GroupFixture.createGroup(leader, "group" + i);
            group.addMember(member, GroupRole.MEMBER);
            groupRepository.save(group);
        }

        MvcTestResult result = mvcTester.get().uri("/api/v1/groups/my")
                .with(user(RankademyUser.from(UserInfo.from(member))))
                .exchange();
        assertThat(result).hasStatusOk();

        List<MyGroupResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
        });
        assertThat(response).hasSize(5);
    }


    @Test
    @WithAnonymousUser
    @DisplayName("내 그룹 목록 조회 - 인증되지 않은 사용자")
    void getMyGroups_withAnonymousUser() throws Exception {
        User member = GroupFixture.createMember();
        member.removeUnivInfo();
        userRepository.save(member);

        User leader = GroupFixture.createLeader();
        userRepository.save(leader);

        for (int i = 0; i < 5; i++) {
            Group group = GroupFixture.createGroup(leader, "group" + i);
            groupRepository.save(group);
        }

        MvcTestResult result = mvcTester.get().uri("/api/v1/groups/my")
                .with(user(RankademyUser.from(UserInfo.from(member))))
                .exchange();
        assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("그룹 생성 - 성공")
    void createGroup() throws Exception {
        User leader = GroupFixture.createLeader();
        userRepository.save(leader);

        MvcTestResult result = mvcTester.post().uri(BASE_URL)
                .with(user(RankademyUser.from(UserInfo.from(leader))))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(GroupFixture.createGroupCreateRequest()))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("그룹 생성 - 인증은 되었으나 인가는 되지 않은 사용자")
    void createGroup_withNotAuthorizedUser() throws Exception {
        User leader = GroupFixture.createLeader();
        leader.removeUnivInfo();
        userRepository.save(leader);

        MvcTestResult result = mvcTester.post().uri(BASE_URL)
                .with(user(RankademyUser.from(UserInfo.from(leader))))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(GroupFixture.createGroupCreateRequest()))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("그룹 생성 - 인증되지 않은 사용자")
    void createGroup_withAnonymousUser() throws Exception {
        User leader = GroupFixture.createLeader();
        userRepository.save(leader);

        MvcTestResult result = mvcTester.post().uri(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(GroupFixture.createGroupCreateRequest()))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("그룹 상세 조회")
    void getGroupDetail() throws Exception {
        Group group = generateGroup();

        em.flush();
        em.clear();

        MvcTestResult result = mvcTester.get().uri(BASE_URL + "/" + group.getId())
                .exchange();

        assertThat(result).hasStatusOk();

        GroupDetailResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), GroupDetailResponse.class);
        assertThat(response.groupId()).isEqualTo(group.getId());

    }


    @Test
    @WithAnonymousUser
    @DisplayName("그룹 최근 전적 조회")
    void getRecentCompetitions() throws Exception {
        Group group = generateGroup();

        MvcTestResult result = mvcTester.get().uri(BASE_URL + String.format("/%d/recent-competitions", group.getId()))
                .exchange();

        assertThat(result).hasStatusOk();
    }

    @Test
    @DisplayName("그룹 멤버 조회")
    void getGroupMembers() throws Exception {
        Group group = generateGroup();

        for (int i = 0; i < 10; i++) {
            User member = GroupFixture.createMember("member" + i + "@test.com", "member" + i);
            userRepository.save(member);
            group.addMember(member, GroupRole.MEMBER);
        }
        groupRepository.save(group);

        MvcTestResult result = mvcTester.get().uri(BASE_URL + String.format("/%d/members", group.getId()))
                .exchange();

        assertThat(result).hasStatusOk();
        List<GroupMemberResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
        });

        assertThat(response).hasSize(7);
    }

    @Test
    @DisplayName("그룹 정보 수정")
    void updateGroup() throws Exception {
        User leader = GroupFixture.createLeader();
        userRepository.save(leader);

        Group group = GroupFixture.createGroup(leader);
        groupRepository.save(group);

        GroupUpdateRequest request = new GroupUpdateRequest(
                "updated",
                "updated",
                "updated.jpg"
        );

        MvcTestResult result = mvcTester.put().uri(BASE_URL + "/" + group.getId())
                .with(user(RankademyUser.from(UserInfo.from(leader))))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();
        assertThat(result).hasStatus(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("그룹 정보 수정 - 리더가 아닌 경우")
    void updateGroup_withNotLeader() throws Exception {
        User member = GroupFixture.createMember();
        userRepository.save(member);
        Group group = generateGroup();

        GroupUpdateRequest request = new GroupUpdateRequest(
                "updated",
                "updated",
                "updated.jpg"
        );

        MvcTestResult result = mvcTester.put().uri(BASE_URL + "/" + group.getId())
                .with(user(RankademyUser.from(UserInfo.from(member))))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();
        assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("그룹 삭제")
    void deleteGroup() throws Exception {
        User leader = GroupFixture.createLeader();
        userRepository.save(leader);

        Group group = GroupFixture.createGroup(leader);
        groupRepository.save(group);

        MvcTestResult result = mvcTester.delete().uri(BASE_URL + String.format("/%d", group.getId()))
                .with(user(RankademyUser.from(UserInfo.from(leader))))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("그룹 삭제 - 리더가 아닌 경우")
    void deleteGroup_withNotLeader() throws Exception {
        User member = GroupFixture.createMember();
        userRepository.save(member);
        Group group = generateGroup();

        MvcTestResult result = mvcTester.delete().uri(BASE_URL + String.format("/%d", group.getId()))
                .with(user(RankademyUser.from(UserInfo.from(member))))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
    }

    private Group generateGroup() {
        User leader = GroupFixture.createLeader();
        userRepository.save(leader);

        Group group = GroupFixture.createGroup(leader);
        groupRepository.save(group);

        em.flush();
        em.clear();

        return group;
    }
}
