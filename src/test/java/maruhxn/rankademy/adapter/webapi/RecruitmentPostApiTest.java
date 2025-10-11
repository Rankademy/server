package maruhxn.rankademy.adapter.webapi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import maruhxn.rankademy.RankademyTestConfiguration;
import maruhxn.rankademy.adapter.security.model.RankademyUser;
import maruhxn.rankademy.adapter.security.model.UserInfo;
import maruhxn.rankademy.application.group.provided.dto.RecruitmentPostDetailResponse;
import maruhxn.rankademy.application.group.provided.dto.RecruitmentPostResponse;
import maruhxn.rankademy.application.group.required.GroupRepository;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.group.Group;
import maruhxn.rankademy.domain.group.GroupFixture;
import maruhxn.rankademy.domain.group.dto.CreateRecruitmentPostRequest;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(RankademyTestConfiguration.class)
class RecruitmentPostApiTest {

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
    @DisplayName("그룹 모집 공고 목록 조회")
    void getRecruitmentPostList() throws Exception {
        // given
        for (int i = 0; i < 15; i++) {
            User leader = GroupFixture.createLeader("leader" + i);
            userRepository.save(leader);
            Group group = GroupFixture.createGroup(leader, "group" + i);
            groupRepository.save(group);
            group.upsertRecruitmentPost(GroupFixture.createRecruitmentRequest());
        }

        em.flush();
        em.clear();

        // when
        MvcTestResult result = mvcTester.get().uri(BASE_URL + "/posts")
                .param("page", "1")
                .exchange();

        // then
        assertThat(result).hasStatusOk();
        List<RecruitmentPostResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
        });
        assertThat(response).hasSize(5);
    }

    @Test
    @DisplayName("그룹 모집 공고 상세 조회 - 성공")
    void getRecruitmentPostDetails() throws Exception {
        // given
        User leader = GroupFixture.createLeader();
        userRepository.save(leader);
        Group group = GroupFixture.createGroup(leader);
        groupRepository.save(group);

        group.upsertRecruitmentPost(GroupFixture.createRecruitmentRequest());

        em.flush();
        em.clear();

        // when
        MvcTestResult result = mvcTester.get().uri(BASE_URL + "/" + group.getId() + "/post")
                .with(user(RankademyUser.from(UserInfo.from(leader))))
                .exchange();

        // then
        assertThat(result).hasStatusOk();
        RecruitmentPostDetailResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), RecruitmentPostDetailResponse.class);
        assertThat(response.postId()).isEqualTo(group.getRecruitmentPost().getId());
    }

    @Test
    @DisplayName("그룹 모집 공고 생성/수정 - 생성 성공")
    void upsertRecruitmentPost_createSuccess() throws Exception {
        // given
        User leader = GroupFixture.createLeader();
        userRepository.save(leader);
        Group group = GroupFixture.createGroup(leader);
        groupRepository.save(group);

        CreateRecruitmentPostRequest request = GroupFixture.createRecruitmentRequest();

        // when
        MvcTestResult result = mvcTester.post().uri(BASE_URL + "/" + group.getId() + "/posts")
                .with(user(RankademyUser.from(UserInfo.from(leader))))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.CREATED);
        Group findGroup = groupRepository.findById(group.getId()).get();
        assertThat(findGroup.getRecruitmentPost()).isNotNull();
        assertThat(findGroup.getRecruitmentPost().getTitle()).isEqualTo(request.title());
    }

    @Test
    @DisplayName("그룹 모집 공고 생성/수정 - 수정 성공")
    void upsertRecruitmentPost_updateSuccess() throws Exception {
        // given
        User leader = GroupFixture.createLeader();
        userRepository.save(leader);
        Group group = GroupFixture.createGroup(leader);
        groupRepository.save(group);

        em.flush();
        em.clear();

        CreateRecruitmentPostRequest request = new CreateRecruitmentPostRequest(
                "updated title",
                "updated content",
                "updated requirements"
        );

        // when
        MvcTestResult result = mvcTester.post().uri(BASE_URL + "/" + group.getId() + "/posts")
                .with(user(RankademyUser.from(UserInfo.from(leader))))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.CREATED);
        Group findGroup = groupRepository.findById(group.getId()).get();
        assertThat(findGroup.getRecruitmentPost()).isNotNull();
        assertThat(findGroup.getRecruitmentPost().getTitle()).isEqualTo(request.title());
        assertThat(findGroup.getRecruitmentPost().getContent()).isEqualTo(request.content());
    }

    @Test
    @DisplayName("그룹 모집 공고 생성/수정 - 리더가 아닌 경우")
    void upsertRecruitmentPost_withNotLeader() throws Exception {
        // given
        User leader = GroupFixture.createLeader();
        userRepository.save(leader);
        User member = GroupFixture.createMember();
        userRepository.save(member);
        Group group = GroupFixture.createGroup(leader);
        groupRepository.save(group);

        CreateRecruitmentPostRequest request = GroupFixture.createRecruitmentRequest();

        // when
        MvcTestResult result = mvcTester.post().uri(BASE_URL + "/" + group.getId() + "/posts")
                .with(user(RankademyUser.from(UserInfo.from(member))))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    @WithAnonymousUser
    @DisplayName("그룹 모집 공고 생성/수정 - 인증되지 않은 사용자")
    void upsertRecruitmentPost_withAnonymousUser() throws Exception {
        // given
        User leader = GroupFixture.createLeader();
        userRepository.save(leader);
        Group group = GroupFixture.createGroup(leader);
        groupRepository.save(group);

        CreateRecruitmentPostRequest request = GroupFixture.createRecruitmentRequest();

        // when
        MvcTestResult result = mvcTester.post().uri(BASE_URL + "/" + group.getId() + "/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("그룹 모집 공고 끌어올리기 - 성공")
    void upRecruitmentPost() throws Exception {
        // given
        User leader = GroupFixture.createLeader();
        userRepository.save(leader);
        Group group = GroupFixture.createGroup(leader);
        groupRepository.save(group);
        group.upsertRecruitmentPost(GroupFixture.createRecruitmentRequest());

        em.flush();
        em.clear();

        Group beforeGroup = groupRepository.findById(group.getId()).get();
        beforeGroup.getRecruitmentPost().up(LocalDateTime.now().minusDays(2));
        LocalDateTime beforeLastUppedAt = beforeGroup.getRecruitmentPost().getLastUppedAt();

        // when
        MvcTestResult result = mvcTester.patch().uri(BASE_URL + "/" + group.getId() + "/posts/up")
                .with(user(RankademyUser.from(UserInfo.from(leader))))
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.NO_CONTENT);
        Group afterGroup = groupRepository.findById(group.getId()).get();
        assertThat(afterGroup.getRecruitmentPost().getLastUppedAt()).isAfter(beforeLastUppedAt);
    }

    @Test
    @DisplayName("그룹 모집 공고 끌어올리기 - 리더가 아닌 경우")
    void upRecruitmentPost_withNotLeader() throws Exception {
        // given
        User leader = GroupFixture.createLeader();
        userRepository.save(leader);
        User member = GroupFixture.createMember();
        userRepository.save(member);
        Group group = GroupFixture.createGroup(leader);
        groupRepository.save(group);

        em.flush();
        em.clear();

        // when
        MvcTestResult result = mvcTester.patch().uri(BASE_URL + "/" + group.getId() + "/posts/up")
                .with(user(RankademyUser.from(UserInfo.from(member))))
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    @WithAnonymousUser
    @DisplayName("그룹 모집 공고 끌어올리기 - 인증되지 않은 사용자")
    void upRecruitmentPost_withAnonymousUser() throws Exception {
        // given
        User leader = GroupFixture.createLeader();
        userRepository.save(leader);
        Group group = GroupFixture.createGroup(leader);
        groupRepository.save(group);

        em.flush();
        em.clear();

        // when
        MvcTestResult result = mvcTester.patch().uri(BASE_URL + "/" + group.getId() + "/posts/up")
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }
}
