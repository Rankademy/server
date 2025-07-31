package maruhxn.rankademy.adapter.persistence;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.group.required.GroupMemberQueryRepository;
import maruhxn.rankademy.domain.group.GroupMember;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class GroupMemberQueryRepositoryImpl implements GroupMemberQueryRepository {

    private final EntityManager em;

    @Override
    public List<GroupMember> getGroupMembers(Long groupId, int page) {
        return em.createQuery(
                        "SELECT gm FROM GroupMember gm " +
                                "WHERE gm.group.id = :groupId", GroupMember.class)
                .setParameter("groupId", groupId)
                .setFirstResult(10 * page)
                .setMaxResults(10)
                .getResultList();
    }
}
