package maruhxn.rankademy.domain.group;

import maruhxn.rankademy.domain.group.dto.GroupUpdateRequest;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.domain.user.UserFixture;
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
        assertThat(group.isRecruiting()).isTrue();
        assertThat(group.getUnivName()).isEqualTo(leader.getUnivInfo().getUnivName());
        assertThat(group.getRecruitmentPost()).isNull();
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
    void addMember_And_CloseRecruitment() {
        // 48명 추가
        for (int i = 1; i < 49; i++) {
            group.addMember(createMember(String.format("test%d@test.com", i), String.format("tester%d", i)), GroupRole.MEMBER);
        }

        assertThat(group.isRecruiting()).isTrue();
        assertThat(group.isFull()).isFalse();

        group.addMember(createMember(), GroupRole.MEMBER);
        assertThat(group.isRecruiting()).isFalse();
        assertThat(group.isFull()).isTrue();
    }

    @Test
    @DisplayName("다른 학교 소속의 유저는 그룹에 가입할 수 없다")
    void addMemberWithDifferentUniv() {
        // given
        User otherUnivUser = createMember();
        otherUnivUser.enrollUnivInfo(UserFixture.createEnrollUnivRequest("다른대학교", "test@other.ac.kr"));
        otherUnivUser.completeUnivAuthentication();

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
    @DisplayName("모집 기간이 아닌 경우 가입 신청이 불가능하다")
    void joinRequest_Fail() {
        User requester = createMember(1L);

        group.closeRecruitment();
        assertThat(group.isRecruiting()).isFalse();

        assertThatThrownBy(() -> group.addJoinRequest(requester))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("이미 신청한 경우 가입 신청이 불가능하다")
    void joinRequest_Fail2() {
        User requester = createMember(1L);

        group.addJoinRequest(requester);

        assertThatThrownBy(() -> group.addJoinRequest(requester))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 대기 중인 요청이 있습니다.");
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
    @DisplayName("모집 종료 시, 모집 공고는 비활성화된다")
    void manageRecruitment() {
        // when: 모집 공고 등록
        group.upsertRecruitmentPost(createRecruitmentRequest());

        // then
        assertThat(group.getRecruitmentPost()).isNotNull();
        assertThat(group.getRecruitmentPost().isActive()).isTrue();

        // when: 모집 종료
        group.closeRecruitment();

        // then
        assertThat(group.getRecruitmentPost().isActive()).isFalse();
    }

    @Test
    @DisplayName("모집이 시작되면 기존 모집 공고가 활성화된다")
    void startRecruitment() {
        group.upsertRecruitmentPost(createRecruitmentRequest());
        group.getRecruitmentPost().deactivate();

        group.startRecruitment();

        assertThat(group.getRecruitmentPost().isActive()).isTrue();
    }
}
