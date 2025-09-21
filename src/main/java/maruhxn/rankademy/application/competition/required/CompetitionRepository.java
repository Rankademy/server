package maruhxn.rankademy.application.competition.required;

import maruhxn.rankademy.domain.competition.Competition;
import maruhxn.rankademy.domain.competition.CompetitionStatus;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CompetitionRepository extends Repository<Competition, Long> {

    Competition save(Competition competition);

    Optional<Competition> findById(Long id);

    @Modifying
    @Query("UPDATE Competition c SET c.status = :newStatus WHERE c.status = :oldStatus AND c.expiredAt < :now")
    int bulkUpdateStatusIfExpired(
            @Param("oldStatus") CompetitionStatus oldStatus,
            @Param("newStatus") CompetitionStatus newStatus,
            @Param("now") LocalDateTime now
    );
}
