package maruhxn.rankademy.application.group.required;

import maruhxn.rankademy.domain.group.Group;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface GroupRepository extends Repository<Group, Long> {

    Group save(Group group);

    Optional<Group> findById(Long id);

    void delete(Group group);
}
