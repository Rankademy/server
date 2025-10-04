package maruhxn.rankademy.application.team;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.team.provided.TeamWriter;
import maruhxn.rankademy.application.team.required.TeamRepository;
import maruhxn.rankademy.domain.shared.DomainEventPublisher;
import maruhxn.rankademy.domain.shared.event.TeamDeactivatedEvent;
import maruhxn.rankademy.domain.team.Team;
import maruhxn.rankademy.domain.team.dto.TeamCreateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TeamCommandService implements TeamWriter {

    private final TeamRepository teamRepository;
    private final DomainEventPublisher publisher;

    @Override
    public Team create(TeamCreateRequest request) {
        return teamRepository.save(Team.create(request));
    }

    @Override
    public void withdraw(Long userId, Long teamId) {
        // 팀과 유저 로드
        Team team = teamRepository.findByIdWithTeamMember(teamId)
                .orElseThrow(() -> new java.util.NoSuchElementException("팀을 찾을 수 없습니다. id: " + teamId));

        team.withdraw(userId);

        publisher.publish(new TeamDeactivatedEvent(team.getId(), java.time.LocalDateTime.now()));
    }
}
