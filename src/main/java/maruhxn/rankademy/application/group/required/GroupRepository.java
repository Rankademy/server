package maruhxn.rankademy.application.group.required;

import maruhxn.rankademy.domain.group.Group;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GroupRepository extends Repository<Group, Long> {

    Group save(Group group);

    Optional<Group> findById(Long id);

    void delete(Group group);

    @Query("select g from Group g join fetch g.leader where g.id = :id")
    Optional<Group> findByIdWithLeader(@Param("id") Long id);
}
