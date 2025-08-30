package maruhxn.rankademy.application.team.required;

import maruhxn.rankademy.domain.team.Team;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface TeamRepository extends Repository<Team, Long> {

    Team save(Team team);

    Optional<Team> findById(Long id);

}
