package maruhxn.rankademy.application.scrim_team.required;

import maruhxn.rankademy.domain.scrim_team.ScrimTeam;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface ScrimTeamRepository extends Repository<ScrimTeam, Long> {
    ScrimTeam save(ScrimTeam scrimTeam);

    Optional<ScrimTeam> findById(Long id);

    void deleteById(Long id);
}
