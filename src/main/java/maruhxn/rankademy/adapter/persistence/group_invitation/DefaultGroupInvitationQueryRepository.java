package maruhxn.rankademy.adapter.persistence.group_invitation;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.group_invitation.dto.GroupInvitationPageResponse;
import maruhxn.rankademy.application.group_invitation.required.GroupInvitationQueryRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import static maruhxn.rankademy.domain.group.QGroup.group;
import static maruhxn.rankademy.domain.group_invitation.QGroupInvitation.groupInvitation;

@Repository
@RequiredArgsConstructor
public class DefaultGroupInvitationQueryRepository implements GroupInvitationQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public GroupInvitationPageResponse getInvitations(Long userId, int page) {
        Long totalCount = queryFactory.select(groupInvitation.count())
                .from(groupInvitation)
                .where(groupInvitation.userId.eq(userId))
                .fetchOne();

        List<GroupInvitationPageResponse.GroupInvitationResponse> result = queryFactory
                .select(
                        Projections.constructor(
                                GroupInvitationPageResponse.GroupInvitationResponse.class,
                                groupInvitation.id,
                                groupInvitation.groupId,
                                group.name,
                                groupInvitation.userId,
                                groupInvitation.invitedAt
                        )
                )
                .from(groupInvitation)
                .join(group).on(groupInvitation.groupId.eq(group.id))
                .where(groupInvitation.userId.eq(userId))
                .fetch();

        return new GroupInvitationPageResponse(totalCount, result);
    }
}
