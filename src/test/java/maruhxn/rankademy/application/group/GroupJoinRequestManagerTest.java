package maruhxn.rankademy.application.group;

import jakarta.persistence.EntityManager;
import maruhxn.rankademy.application.group.provided.GroupJoinRequestManager;
import maruhxn.rankademy.application.group.required.GroupRepository;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.group.Group;
import maruhxn.rankademy.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static maruhxn.rankademy.domain.group.GroupFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@DisplayName("GroupJoinRequestManager 테스트")
class GroupJoinRequestManagerTest {

    @Autowired
    GroupJoinRequestManager groupJoinRequestManager;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager em;

    User leader;
    User member;
    Group group;

    @BeforeEach
    void setUp() {
        leader = createLeader();
        userRepository.save(leader);

        member = createMember();
        userRepository.save(member);

        group = createGroup(leader);
        groupRepository.save(group);
    }

    @Test
    @DisplayName("그룹 가입 신청")
    void addJoinRequest() {
        // when
        groupJoinRequestManager.addJoinRequest(member.getId(), group.getId());

        // then
        assertThat(group.getJoinRequests()).hasSize(1);
    }

    @Test
    @DisplayName("그룹 가입 수락")
    void acceptJoinRequest() {
        // given
        group.addJoinRequest(member);

        // when
        groupJoinRequestManager.acceptJoinRequest(member.getId(), group.getId());

        // then
        assertThat(group.getJoinRequests()).isEmpty();
        assertThat(group.getMembers()).hasSize(2);
    }

    @Test
    @DisplayName("그룹 가입 거절")
    void rejectJoinRequest() {
        // given
        group.addJoinRequest(member);

        // when
        groupJoinRequestManager.rejectJoinRequest(member.getId(), group.getId());
        em.flush();
        em.clear();

        // then
        assertThat(group.getJoinRequests()).isEmpty();
        assertThat(group.getMembers()).hasSize(1);
    }

    @Test
    @DisplayName("이미 가입된 사용자가 가입 신청 시 예외 발생")
    void addJoinRequestFailWithAlreadyJoinedUser() {
        // given
        group.addMember(member, maruhxn.rankademy.domain.group.GroupRole.MEMBER);

        // when & then
        assertThatThrownBy(() -> groupJoinRequestManager.addJoinRequest(member.getId(), group.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 가입된 유저입니다.");
    }

    @Test
    @DisplayName("존재하지 않는 가입 요청 수락 시 예외 발생")
    void acceptJoinRequestFailWithNonExistRequest() {
        // when & then
        assertThatThrownBy(() -> groupJoinRequestManager.acceptJoinRequest(member.getId(), group.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("존재하지 않는 가입 요청입니다.");
    }
}
