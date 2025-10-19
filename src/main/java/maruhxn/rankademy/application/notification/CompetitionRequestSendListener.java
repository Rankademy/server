package maruhxn.rankademy.application.notification;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.notification.required.NotificationRepository;
import maruhxn.rankademy.application.team.required.TeamRepository;
import maruhxn.rankademy.domain.notification.Notification;
import maruhxn.rankademy.domain.shared.event.SendCompetitionRequestEvent;
import maruhxn.rankademy.domain.team.Team;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.NoSuchElementException;

@Component
@RequiredArgsConstructor
public class CompetitionRequestSendListener {

    private final NotificationRepository notificationRepository;
    private final TeamRepository teamRepository;

    /**
     * 대항전 요청 전송 시, 상대팀에게 알림 전송
     *
     * @param event
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(SendCompetitionRequestEvent event) {
        Team fromTeam = getTeam(event.getFromTeamId());
        Team toTeam = getTeam(event.getToTeamId());

        toTeam.getTeamMembers().forEach(member -> {
            Notification notification = Notification.create(
                    member.getUser().getId(),
                    "%s 팀의 대항전 요청".formatted(fromTeam.getName()),
                    event.getRequestedAt()
            );
            notificationRepository.save(notification);
        });
    }

    private Team getTeam(Long teamId) {
        return teamRepository.findByIdWithTeamMember(teamId)
                .orElseThrow(() -> new NoSuchElementException("팀 정보를 찾을 수 없습니다. teamId: " + teamId));
    }

}
