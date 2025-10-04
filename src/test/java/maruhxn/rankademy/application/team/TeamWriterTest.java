package maruhxn.rankademy.application.team;

import jakarta.persistence.EntityManager;
import maruhxn.rankademy.application.notification.NotificationQueryService;
import maruhxn.rankademy.application.team.provided.TeamWriter;
import maruhxn.rankademy.application.team.required.TeamRepository;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.group.GroupFixture;
import maruhxn.rankademy.domain.team.Team;
import maruhxn.rankademy.domain.team.TeamFixture;
import maruhxn.rankademy.domain.team.TeamMember;
import maruhxn.rankademy.domain.team.dto.TeamCreateRequest;
import maruhxn.rankademy.domain.user.LolPosition;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.domain.user.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("TeamWriter 테스트")
class TeamWriterTest {

    @Autowired
    TeamWriter teamWriter;

    @Autowired
    EntityManager em;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    NotificationQueryService notificationQueryService;

    @Test
    @DisplayName("팀 생성 성공")
    void create() {
        // given
        User representative = UserFixture.createUser();
        userRepository.save(representative);

        Set<TeamMember> members = new HashSet<>();
        members.add(new TeamMember(representative, LolPosition.TOP));

        for (int i = 0; i < 4; i++) {
            User memberUser = GroupFixture.createMember("member" + i + "@test.com", "member" + i);
            userRepository.save(memberUser);
            members.add(new TeamMember(memberUser, LolPosition.values()[i + 1]));
        }
        TeamCreateRequest teamCreateRequest = TeamFixture.createTeamCreateRequest(representative.getId(), members);

        // when
        Team team = teamWriter.create(teamCreateRequest);
        em.flush();
        em.clear();

        // then
        assertThat(team).isNotNull();
        assertThat(team.getName()).isNotNull();
        assertThat(team.getIntro()).isNotNull();
        assertThat(team.getRepresentativeId()).isEqualTo(representative.getId());
        assertThat(team.getTeamMembers()).hasSize(5);
    }

    @Test
    @DisplayName("팀 탈퇴 시 멤버 제외, 팀 비활성화, 알림 생성")
    void withdraw() {
        // given: 대표자 + 4명으로 팀 생성
        User representative = GroupFixture.createLeader("leader-withdraw");
        userRepository.save(representative);

        Set<TeamMember> members = new HashSet<>();
        members.add(new TeamMember(representative, LolPosition.TOP));

        User leavingUser = null;
        for (int i = 0; i < 4; i++) {
            User memberUser = GroupFixture.createMember("member-wd" + i + "@test.com", "member-wd" + i);
            userRepository.save(memberUser);
            if (i == 0) leavingUser = memberUser; // 탈퇴할 대상 지정
            members.add(new TeamMember(memberUser, LolPosition.values()[i + 1]));
        }

        TeamCreateRequest baseRequest = TeamFixture.createTeamCreateRequest(representative.getId(), members);
        TeamCreateRequest request = new TeamCreateRequest(
                baseRequest.groupId(),
                "team-withdraw-" + UUID.randomUUID(),
                baseRequest.intro(),
                baseRequest.representativeId(),
                baseRequest.members()
        );
        Team team = teamWriter.create(request);
        String creationMessage = "팀 %s이 생성되었습니다.".formatted(team.getName());
        String deactivationMessage = "팀 %s이 비활성화되었습니다.".formatted(team.getName());
        Long teamId = team.getId();

        // 팀 생성 트랜잭션 커밋 후 이벤트 발행
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // 다음 시나리오를 위한 새 트랜잭션 시작
        TestTransaction.start();

        // when: 멤버 1명이 탈퇴 실행 (서비스 트랜잭션 커밋 후 이벤트 처리)
        teamWriter.withdraw(leavingUser.getId(), teamId);

        // 탈퇴 처리 트랜잭션 커밋하여 비활성화 이벤트 발행
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // 검증용 새 트랜잭션 시작
        TestTransaction.start();

        // then: 팀은 비활성화되고, 멤버 수는 4명으로 감소하며, 탈퇴자는 제외된다
        Team found = teamRepository.findByIdWithTeamMember(teamId).orElseThrow();
        assertThat(found.isActive()).isFalse();
        assertThat(found.getTeamMembers()).hasSize(4)
                .extracting(tm -> tm.getUser().getId()).doesNotContain(leavingUser.getId());

        // 남아있는 멤버 전원에게 알림 2건씩 생성되었는지 확인 (생성 및 비활성화)
        found.getTeamMembers().forEach(tm -> {
            var page = notificationQueryService.getNotifications(tm.getUser().getId(), 0);
            assertThat(page.totalCount()).isEqualTo(2);
            assertThat(page.notifications()).hasSize(2);
            assertThat(page.notifications().getFirst().message()).isEqualTo(deactivationMessage);
            assertThat(page.notifications().getLast().message()).isEqualTo(creationMessage);
        });

        // 탈퇴자는 비활성화 알림을 받지 않지만 생성 알림은 유지된다
        var leavingPage = notificationQueryService.getNotifications(leavingUser.getId(), 0);
        assertThat(leavingPage.totalCount()).isEqualTo(1);
        assertThat(leavingPage.notifications()).hasSize(1);
        assertThat(leavingPage.notifications().getFirst().message()).isEqualTo(creationMessage);

        TestTransaction.flagForRollback();
        TestTransaction.end();

        cleanupPersistedState(teamId, members, Set.of(creationMessage, deactivationMessage));
    }

    private void cleanupPersistedState(Long teamId, Set<TeamMember> members, Set<String> notificationMessages) {
        TestTransaction.start();

        notificationMessages.forEach(message ->
                em.createQuery("delete from Notification n where n.message = :message")
                        .setParameter("message", message)
                        .executeUpdate()
        );

        Team persistedTeam = em.find(Team.class, teamId);
        if (persistedTeam != null) {
            em.remove(persistedTeam);
        }

        Set<Long> userIds = members.stream()
                .map(member -> member.getUser().getId())
                .collect(Collectors.toSet());

        userIds.forEach(userId -> {
            User persistedUser = em.find(User.class, userId);
            if (persistedUser != null) {
                em.remove(persistedUser);
            }
        });

        TestTransaction.flagForCommit();
        TestTransaction.end();
    }
}
