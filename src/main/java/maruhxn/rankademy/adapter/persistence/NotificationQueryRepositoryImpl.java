package maruhxn.rankademy.adapter.persistence;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.notification.provided.dto.NotificationPageResponse;
import maruhxn.rankademy.application.notification.required.NotificationQueryRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import static maruhxn.rankademy.domain.notification.QNotification.notification;

@Repository
@RequiredArgsConstructor
public class NotificationQueryRepositoryImpl implements NotificationQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public NotificationPageResponse getNotifications(Long userId, int page) {
        Long totalCount = queryFactory
                .select(notification.count())
                .from(notification)
                .where(notification.userId.eq(userId))
                .fetchOne();

        if (totalCount == null || totalCount == 0) {
            return new NotificationPageResponse(0L, List.of());
        }

        List<NotificationPageResponse.NotificationResponse> result = queryFactory
                .select(Projections.constructor(
                        NotificationPageResponse.NotificationResponse.class,
                        notification.id,
                        notification.message,
                        notification.isConfirmed,
                        notification.deliveredAt
                ))
                .from(notification)
                .where(
                        notification.userId.eq(userId),
                        notification.isConfirmed.eq(false) // 신규 알림만 조회
                )
                .orderBy(notification.id.desc())
                .offset(page * 30L)
                .limit(30)
                .fetch();

        return new NotificationPageResponse(totalCount, result);
    }
}
