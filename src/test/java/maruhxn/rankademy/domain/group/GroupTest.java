package maruhxn.rankademy.domain.group;

import maruhxn.rankademy.domain.group.dto.GroupUpdateRequest;
import maruhxn.rankademy.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static maruhxn.rankademy.domain.group.GroupFixture.createMember;
import static maruhxn.rankademy.domain.group.GroupFixture.createRecruitmentRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("도메인 - Group")
class GroupTest {

    private User leader;
    private Group group;

    @BeforeEach
    void setUp() {
        leader = GroupFixture.createLeader();
        group = GroupFixture.createGroup(leader);
    }

    @Test
    @DisplayName("그룹 생성 시 생성자는 자동으로 그룹의 리더가 된다")
    void createGroup() {
        // then
        assertThat(group.getMembers()).hasSize(1);

        GroupMember groupMember = group.getMembers().stream().findFirst().get();

        assertThat(groupMember.getUser()).isEqualTo(leader);
        assertThat(groupMember.getRole()).isEqualTo(GroupRole.LEADER);
        assertThat(group.getUnivName()).isEqualTo(leader.getUnivInfo().getUnivName());
    }

    @Test
    @DisplayName("그룹 정보 수정")
    void updateGroupInfo() {
        // when
        group.updateGroupInfo(new GroupUpdateRequest("수정된 그룹명", "수정된 소개", "new_logo.jpg"));

        // then
        assertThat(group.getName()).isEqualTo("수정된 그룹명");
        assertThat(group.getAbout()).isEqualTo("수정된 소개");
        assertThat(group.getLogoImage()).isEqualTo("new_logo.jpg");
    }

    @Test
    @DisplayName("동일한 학교 소속의 유저만 그룹에 가입할 수 있다")
    void addMember() {
        // given
        User member = createMember();

        // when
        group.addMember(member, GroupRole.MEMBER);

        // then
        assertThat(group.getMembers()).hasSize(2);
    }

    @Test
    @DisplayName("다른 학교 소속의 유저는 그룹에 가입할 수 없다")
    void addMemberWithDifferentUniv() {
        // given
        User otherUnivUser = createMember();
        otherUnivUser.enrollUnivInfo(maruhxn.rankademy.domain.user.UserFixture.createEnrollUnivRequest("다른대학교", "test@other.ac.kr"));

        // when & then
        assertThatThrownBy(() -> group.addMember(otherUnivUser, GroupRole.MEMBER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("동일한 학교 소속의 유저만 가입할 수 있습니다.");
    }

    @Test
    @DisplayName("그룹에서 멤버를 탈퇴시킬 수 있다")
    void removeMember() {
        // given
        User member = createMember();
        group.addMember(member, GroupRole.MEMBER);

        // when
        group.removeMember(member);

        // then
        assertThat(group.getMembers()).hasSize(1);
        assertThat(group.getMembers().stream().noneMatch(m -> m.getUser().equals(member))).isTrue();
    }

    @Test
    @DisplayName("그룹 가입 신청, 수락, 거절")
    void joinRequest() {
        // given
        User requester = createMember(1L);

        // when: 가입 신청
        group.addJoinRequest(requester);

        // then
        assertThat(group.getJoinRequests()).hasSize(1);
        assertThat(group.getJoinRequests().stream().findFirst().get().userId()).isEqualTo(requester.getId());

        // when: 가입 수락
        group.acceptJoinRequest(requester);

        // then
        assertThat(group.getJoinRequests()).isEmpty();
        assertThat(group.getMembers()).hasSize(2);
        assertThat(group.getMembers().stream().anyMatch(m -> m.getUser().equals(requester) && m.getRole() == GroupRole.MEMBER)).isTrue();

        // given
        User anotherApplicant = createMember(2L);
        group.addJoinRequest(anotherApplicant);

        // when: 가입 거절
        group.rejectJoinRequest(anotherApplicant.getId());

        // then
        assertThat(group.getJoinRequests()).isEmpty();
        assertThat(group.getMembers()).hasSize(2); // 멤버 수는 그대로
    }

    @Test
    @DisplayName("그룹원 모집 공고를 등록하고 종료할 수 있다")
    void manageRecruitment() {
        // when: 모집 공고 등록
        group.createGroupRecruitmentPost(createRecruitmentRequest());

        // then
        assertThat(group.getRecruitmentPost()).isNotNull();
        assertThat(group.getRecruitmentPost().getTitle()).isEqualTo("그룹원 모집합니다");

        // when: 모집 공고 종료
        group.closeRecruitment();

        // then
        assertThat(group.getRecruitmentPost()).isNull();
    }
}
