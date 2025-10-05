package maruhxn.rankademy.application.team;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.team.provided.TeamWriter;
import maruhxn.rankademy.application.team.required.TeamRepository;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.shared.DomainEventPublisher;
import maruhxn.rankademy.domain.shared.event.TeamCreatedEvent;
import maruhxn.rankademy.domain.shared.event.TeamDeactivatedEvent;
import maruhxn.rankademy.domain.team.Team;
import maruhxn.rankademy.domain.team.TeamMember;
import maruhxn.rankademy.domain.team.dto.TeamCreateRequest;
import maruhxn.rankademy.domain.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TeamCommandService implements TeamWriter {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final DomainEventPublisher publisher;

    @Override
    public Team create(TeamCreateRequest request) {
        Team team = assembleTeam(request);
        Team saved = teamRepository.save(team);
        publisher.publish(new TeamCreatedEvent(team.getId(), LocalDateTime.now()));
        return saved;
    }

    @Override
    public void withdraw(Long userId, Long teamId) {
        // 팀과 유저 로드
        Team team = teamRepository.findByIdWithTeamMember(teamId)
                .orElseThrow(() -> new java.util.NoSuchElementException("팀을 찾을 수 없습니다. id: " + teamId));

        team.withdraw(userId);

        publisher.publish(new TeamDeactivatedEvent(team.getId(), LocalDateTime.now()));
    }

    private Team assembleTeam(TeamCreateRequest request) {
        Set<TeamMember> teamMembers = request.members().stream()
                .map(slot -> new TeamMember(resolveUser(slot.userId()), slot.position()))
                .collect(Collectors.toCollection(HashSet::new));

        return Team.create(request, teamMembers);
    }

    private User resolveUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new java.util.NoSuchElementException("사용자를 찾을 수 없습니다. id: " + userId));
    }
}
