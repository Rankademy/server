package maruhxn.rankademy.application.group;

import jakarta.persistence.EntityManager;
import maruhxn.rankademy.application.group.provided.GroupMemberManager;
import maruhxn.rankademy.application.group.required.GroupRepository;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.group.Group;
import maruhxn.rankademy.domain.group.GroupRole;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static maruhxn.rankademy.domain.group.GroupFixture.*;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GroupMemberManager 테스트")
class GroupMemberManagerTest extends IntegrationTestSupport {

    @Autowired
    private GroupMemberManager groupMemberManager;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    EntityManager em;

    private User leader;
    private User member;
    private Group group;

    @BeforeEach
    void setUp() {
        leader = createLeader();
        userRepository.save(leader);

        member = createMember();
        userRepository.save(member);

        group = createGroup(leader);
        group.addMember(member, GroupRole.MEMBER);
        groupRepository.save(group);
    }

    @Test
    @DisplayName("그룹 멤버 추방")
    void removeMember() {
        // when
        groupMemberManager.removeMember(group.getId(), member.getId());

        // then
        assertThat(group.getMembers()).hasSize(1);
    }
}
