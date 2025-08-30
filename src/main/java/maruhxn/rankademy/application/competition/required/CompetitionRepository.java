package maruhxn.rankademy.application.competition.required;

import maruhxn.rankademy.domain.competition.Competition;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface CompetitionRepository extends Repository<Competition, Long> {

    Competition save(Competition competition);

    Optional<Competition> findById(Long id);
}
