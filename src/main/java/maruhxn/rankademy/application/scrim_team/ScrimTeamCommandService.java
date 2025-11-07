package maruhxn.rankademy.application.scrim_team;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.scrim_team.provided.ScrimTeamReader;
import maruhxn.rankademy.application.scrim_team.provided.ScrimTeamWriter;
import maruhxn.rankademy.application.scrim_team.required.ScrimTeamRepository;
import maruhxn.rankademy.domain.scrim_team.ScrimTeam;
import maruhxn.rankademy.domain.scrim_team.dto.ScrimTeamCreateRequest;
import maruhxn.rankademy.domain.scrim_team.dto.ScrimTeamUpdateRequest;
import maruhxn.rankademy.domain.scrim_team.service.ScrimTeamLeaderLimitValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ScrimTeamCommandService implements ScrimTeamWriter {

    private final ScrimTeamReader scrimTeamReader;
    private final ScrimTeamRepository scrimTeamRepository;
    private final ScrimTeamLeaderLimitValidator scrimTeamLeaderLimitValidator;

    @Override
    public ScrimTeam create(Long userId , ScrimTeamCreateRequest createRequest) {
        scrimTeamLeaderLimitValidator.validateScrimTeamLeaderLimitExceeded(userId);

        ScrimTeam scrimTeam = ScrimTeam.create(createRequest);

        return scrimTeamRepository.save(scrimTeam);
    }

    @Override
    public ScrimTeam update(Long scrimTeamId, ScrimTeamUpdateRequest updateRequest) {
        ScrimTeam scrimTeam = scrimTeamReader.get(scrimTeamId);
        scrimTeam.update(updateRequest);

        return scrimTeamRepository.save(scrimTeam);
    }

    @Override
    public void delete(Long scrimTeamId) {
        ScrimTeam scrimTeam = scrimTeamReader.get(scrimTeamId);

        scrimTeamRepository.deleteById(scrimTeamId);
    }
}
