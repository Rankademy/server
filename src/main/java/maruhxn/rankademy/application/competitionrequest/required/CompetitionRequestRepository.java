package maruhxn.rankademy.application.competitionrequest.required;

import maruhxn.rankademy.domain.competitionrequest.CompetitionRequest;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface CompetitionRequestRepository extends Repository<CompetitionRequest, Long> {

    CompetitionRequest save(CompetitionRequest competitionRequest);

    Optional<CompetitionRequest> findById(Long id);
}
