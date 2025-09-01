package maruhxn.rankademy.application.team.required;

import maruhxn.rankademy.domain.team.Team;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TeamRepository extends Repository<Team, Long> {

    Team save(Team team);

    Optional<Team> findById(Long id);

    @Query("select t from Team t join fetch t.teamMembers tm join fetch tm.user where t.id = :id")
    Optional<Team> findByIdWithTeamMember(@Param("id") Long id);

}
