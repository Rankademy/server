package maruhxn.rankademy.application.team;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.team.provided.TeamWriter;
import maruhxn.rankademy.application.team.required.TeamRepository;
import maruhxn.rankademy.domain.team.Team;
import maruhxn.rankademy.domain.team.dto.TeamCreateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TeamCommandService implements TeamWriter {

    private final TeamRepository teamRepository;

    @Override
    public Team create(TeamCreateRequest request) {
        return teamRepository.save(Team.create(request));
    }
}
