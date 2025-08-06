package maruhxn.rankademy.application.group;

import jakarta.persistence.EntityManager;
import maruhxn.rankademy.application.group.provided.GroupWriter;
import maruhxn.rankademy.application.group.required.GroupRepository;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.group.Group;
import maruhxn.rankademy.domain.group.dto.GroupCreateRequest;
import maruhxn.rankademy.domain.group.dto.GroupUpdateRequest;
import maruhxn.rankademy.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static maruhxn.rankademy.domain.group.GroupFixture.createGroup;
import static maruhxn.rankademy.domain.group.GroupFixture.createLeader;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("GroupWriter 테스트")
class GroupWriterTest {

    @Autowired
    GroupWriter groupWriter;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager em;

    User leader;

    @BeforeEach
    void setUp() {
        leader = createLeader();
        userRepository.save(leader);
    }

    @Test
    @DisplayName("그룹 생성")
    void createGroupTest() {
        // given
        GroupCreateRequest request = new GroupCreateRequest("새로운 그룹", "새로운 그룹입니다.", "new_logo.jpg");

        // when
        Group newGroup = groupWriter.create(leader.getId(), request);
        em.flush();
        em.clear();

        // then
        assertThat(newGroup).isNotNull();
        assertThat(newGroup.getName()).isEqualTo("새로운 그룹");
        assertThat(newGroup.getAbout()).isEqualTo("새로운 그룹입니다.");
        assertThat(newGroup.getLogoImage()).isEqualTo("new_logo.jpg");
        assertThat(newGroup.getMembers()).hasSize(1);
        assertThat(newGroup.getMembers().stream().findFirst().get().getUser()).isEqualTo(leader);
    }

    @Test
    @DisplayName("그룹 정보 수정")
    void updateGroupTest() {
        // given
        Group group = generateGroup();
        GroupUpdateRequest request = new GroupUpdateRequest("수정된 그룹", "수정된 그룹입니다.", "updated_logo.jpg");

        // when
        Group updatedGroup = groupWriter.update(group.getId(), request);
        em.flush();
        em.clear();

        // then
        assertThat(updatedGroup).isNotNull();
        assertThat(updatedGroup.getName()).isEqualTo("수정된 그룹");
        assertThat(updatedGroup.getAbout()).isEqualTo("수정된 그룹입니다.");
        assertThat(updatedGroup.getLogoImage()).isEqualTo("updated_logo.jpg");
    }

    @Test
    void startAndCloseRecruitment() {
        Group group = generateGroup();
        group.closeRecruitment();

        assertThat(group.isRecruiting()).isFalse();

        group = groupWriter.startRecruitment(group.getId());
        em.flush();
        em.clear();

        assertThat(group.isRecruiting()).isTrue();

        group = groupWriter.closeRecruitment(group.getId());
        em.flush();
        em.clear();

        assertThat(group.isRecruiting()).isFalse();
    }

    private Group generateGroup() {
        Group group = createGroup(leader);
        groupRepository.save(group);
        em.flush();
        em.clear();
        return group;
    }

    @Test
    void delete() {
        Group group = generateGroup();

        groupWriter.delete(group.getId());
        em.flush();
        em.clear();

        assertThat(groupRepository.findById(group.getId())).isEmpty();
    }
}
