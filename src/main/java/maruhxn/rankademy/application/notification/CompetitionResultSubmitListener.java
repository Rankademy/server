package maruhxn.rankademy.application.notification;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.competition.required.CompetitionRepository;
import maruhxn.rankademy.application.notification.required.NotificationRepository;
import maruhxn.rankademy.application.team.required.TeamRepository;
import maruhxn.rankademy.domain.competition.Competition;
import maruhxn.rankademy.domain.notification.Notification;
import maruhxn.rankademy.domain.shared.event.CompetitionResultSubmitEvent;
import maruhxn.rankademy.domain.team.Team;
import maruhxn.rankademy.domain.user.SummonerInfo;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class CompetitionResultSubmitListener {

    private final NotificationRepository notificationRepository;
    private final CompetitionRepository competitionRepository;
    private final TeamRepository teamRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(CompetitionResultSubmitEvent event) {
        Competition competition = competitionRepository.findById(event.getCompetitionId())
                .orElseThrow(() -> new NoSuchElementException("대항전 정보를 찾을 수 없습니다. compeitionId: " + event.getCompetitionId()));

        Team team1 = getTeam(competition.getTeam1Id());
        Team team2 = getTeam(competition.getTeam2Id());

        // 대항전 완료 이후 비활성화
        team1.deactivate();
        team2.deactivate();

        LocalDateTime now = LocalDateTime.now();

        String actingUserName = Stream.of(team1.getTeamMembers(), team2.getTeamMembers())
                .flatMap(Collection::stream)
                .filter(tm -> tm.getUser().getId().equals(event.getActingUserId()))
                .findAny()
                .map(tm -> {
                    SummonerInfo summonerInfo = tm.getUser().getSummonerInfo();
                    String summonerName = summonerInfo.getSummonerName();
                    String summonerTag = summonerInfo.getSummonerTag();
                    return "%s#%s".formatted(summonerName, summonerTag);
                })
                .orElseThrow(() ->
                        new IllegalStateException("참여 멤버 중 다음의 유저를 찾을 수 없습니다. userId: " + event.getActingUserId())
                );

        createNotificationForAllMembers(team1, actingUserName, now);
        createNotificationForAllMembers(team2, actingUserName, now);
    }

    private void createNotificationForAllMembers(Team team, String actingUserName, LocalDateTime now) {
        team.getTeamMembers().forEach(member -> {
            Notification notification = Notification.create(
                    member.getUser().getId(),
                    "%s님이 대항전 결과를 등록했습니다.".formatted(actingUserName),
                    now
            );
            notificationRepository.save(notification);
        });
    }

    private Team getTeam(Long teamId) {
        return teamRepository.findByIdWithTeamMember(teamId)
                .orElseThrow(() -> new NoSuchElementException("팀 정보를 찾을 수 없습니다. teamId: " + teamId));
    }
}
