package maruhxn.rankademy.application.notification;

import jakarta.persistence.EntityManager;
import maruhxn.rankademy.application.notification.provided.dto.NotificationPageResponse;
import maruhxn.rankademy.application.team.provided.TeamWriter;
import maruhxn.rankademy.application.team.required.TeamRepository;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.shared.event.TeamCreatedEvent;
import maruhxn.rankademy.domain.team.Team;
import maruhxn.rankademy.domain.team.TeamMember;
import maruhxn.rankademy.domain.team.TeamFixture;
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

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@DisplayName("TeamCreatedListener 통합 테스트")
class TeamCreatedListenerTest {

    @Autowired
    TeamWriter teamWriter;

    @Autowired
    UserRepository userRepository;

    @Autowired
    NotificationQueryService notificationQueryService;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    TeamCreatedListener teamCreatedListener;

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("팀 생성 이벤트 발생 시 모든 멤버에게 알림을 생성한다")
    void on_whenTeamCreated_createsNotificationsForMembers() {
        // given
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        User representative = UserFixture.createAuthorizedMember("rep-" + suffix + "@test.com", "rep-" + suffix);
        userRepository.save(representative);

        Set<TeamMember> members = new LinkedHashSet<>();
        members.add(new TeamMember(representative, LolPosition.TOP));

        List<LolPosition> positions = List.of(LolPosition.JG, LolPosition.MID, LolPosition.ADC, LolPosition.SUP);
        for (int i = 0; i < positions.size(); i++) {
            String memberSuffix = suffix + "-m" + i;
            User member = UserFixture.createAuthorizedMember("member-" + memberSuffix + "@test.com", "member-" + memberSuffix);
            userRepository.save(member);
            members.add(new TeamMember(member, positions.get(i)));
        }

        TeamCreateRequest baseRequest = TeamFixture.createTeamCreateRequest(representative.getId(), TeamFixture.toSlots(members));
        TeamCreateRequest request = new TeamCreateRequest(
                baseRequest.groupId(),
                "team-" + suffix,
                baseRequest.intro(),
                baseRequest.representativeId(),
                baseRequest.members()
        );

        Team team = teamWriter.create(request);
        String expectedMessage = "팀 %s이 생성되었습니다.".formatted(team.getName());

        // when: commit current transaction to trigger afterCommit listener
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // start fresh transaction for verification
        TestTransaction.start();

        Team persistedTeam = teamRepository.findByIdWithTeamMember(team.getId()).orElseThrow();
        persistedTeam.getTeamMembers().forEach(member -> {
            NotificationPageResponse page = notificationQueryService.getNotifications(member.getUser().getId(), 0);
            assertThat(page.totalCount()).isEqualTo(1L);
            NotificationPageResponse.NotificationResponse notification = page.notifications().getFirst();
            assertThat(notification.message()).isEqualTo(expectedMessage);
            assertThat(notification.isConfirmed()).isFalse();
            assertThat(notification.deliveredAt()).isNotNull();
        });

        TestTransaction.flagForRollback();
        TestTransaction.end();

        cleanupPersistedState(team.getId(), members, Set.of(expectedMessage));
    }

    @Test
    @DisplayName("존재하지 않는 팀 ID로 이벤트를 처리하면 예외가 발생한다")
    void on_whenTeamNotFound_throwsException() {
        // given
        TeamCreatedEvent event = new TeamCreatedEvent(Long.MAX_VALUE, LocalDateTime.now());

        // when & then
        assertThatThrownBy(() -> teamCreatedListener.on(event))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("팀 정보를 찾을 수 없습니다. teamId: " + event.getTeamId());
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
