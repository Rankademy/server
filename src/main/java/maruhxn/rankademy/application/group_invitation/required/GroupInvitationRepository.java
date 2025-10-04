package maruhxn.rankademy.application.group_invitation.required;

import maruhxn.rankademy.domain.group_invitation.GroupInvitation;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface GroupInvitationRepository extends Repository<GroupInvitation, Long> {

    GroupInvitation save(GroupInvitation invitation);

    Optional<GroupInvitation> findById(Long id);
}
