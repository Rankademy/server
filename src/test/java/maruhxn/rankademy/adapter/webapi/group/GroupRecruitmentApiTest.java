package maruhxn.rankademy.adapter.webapi.group;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import maruhxn.rankademy.adapter.security.model.RankademyUser;
import maruhxn.rankademy.adapter.security.model.UserInfo;
import maruhxn.rankademy.application.group.required.GroupRepository;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.group.Group;
import maruhxn.rankademy.domain.group.GroupFixture;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@AutoConfigureMockMvc
class GroupRecruitmentApiTest extends IntegrationTestSupport {

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
    @DisplayName("그룹 모집 시작")
    void startRecruitment() throws Exception {
        User leader = GroupFixture.createLeader();
        userRepository.save(leader);

        Group group = GroupFixture.createGroup(leader);
        groupRepository.save(group);

        MvcTestResult result = mvcTester.post().uri(BASE_URL + String.format("/%d/recruitment", group.getId()))
                .with(user(RankademyUser.from(UserInfo.from(leader))))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("그룹 모집 시작 - 리더가 아닌 경우")
    void startRecruitment_withNotLeader() throws Exception {
        User member = GroupFixture.createMember();
        userRepository.save(member);
        Group group = generateGroup();

        MvcTestResult result = mvcTester.post().uri(BASE_URL + String.format("/%d/recruitment", group.getId()))
                .with(user(RankademyUser.from(UserInfo.from(member))))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("그룹 모집 종료")
    void closeRecruitment() throws Exception {
        User leader = GroupFixture.createLeader();
        userRepository.save(leader);

        Group group = GroupFixture.createGroup(leader);
        groupRepository.save(group);

        MvcTestResult result = mvcTester.delete().uri(BASE_URL + String.format("/%d/recruitment", group.getId()))
                .with(user(RankademyUser.from(UserInfo.from(leader))))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.NO_CONTENT);
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