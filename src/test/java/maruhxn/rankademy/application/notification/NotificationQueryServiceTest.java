package maruhxn.rankademy.application.notification;

import maruhxn.rankademy.application.notification.provided.dto.NotificationPageResponse;
import maruhxn.rankademy.application.notification.required.NotificationRepository;
import maruhxn.rankademy.domain.notification.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("애플리케이션 - 알림 조회 서비스 (통합 테스트)")
class NotificationQueryServiceTest {

    @Autowired
    private NotificationQueryService notificationQueryService;

    @Autowired
    private NotificationRepository notificationRepository;

    @BeforeEach
    void setUp() {
        for (int i = 1; i <= 40; i++) {
            notificationRepository.save(Notification.create(1L, "Message " + i, LocalDateTime.now().minusMinutes(i)));
        }
        notificationRepository.save(Notification.create(2L, "Other user message", LocalDateTime.now()));
    }

    @Test
    @DisplayName("사용자의 알림 목록 첫 페이지를 조회한다.")
    void getNotifications_firstPage() {
        // given
        Long userId = 1L;
        int page = 0; // Spring data page is 0-indexed

        // when
        NotificationPageResponse result = notificationQueryService.getNotifications(userId, page);

        // then
        assertThat(result.totalCount()).isEqualTo(40);
        assertThat(result.notifications()).hasSize(30)
                .extracting(NotificationPageResponse.NotificationResponse::isConfirmed).doesNotContain(true);
        assertThat(result.notifications().getFirst().message()).isEqualTo("Message 40");
    }

    @Test
    @DisplayName("사용자의 알림 목록 두 번째 페이지를 조회한다.")
    void getNotifications_secondPage() {
        // given
        Long userId = 1L;
        int page = 1;

        // when
        NotificationPageResponse result = notificationQueryService.getNotifications(userId, page);

        // then
        assertThat(result.totalCount()).isEqualTo(40);
        assertThat(result.notifications()).hasSize(10)
                .extracting(NotificationPageResponse.NotificationResponse::isConfirmed).doesNotContain(true);
        assertThat(result.notifications().getLast().message()).isEqualTo("Message 1");
    }

    @Test
    @DisplayName("알림이 없는 사용자를 조회하면 빈 페이지를 반환한다.")
    void getNotifications_noNotifications() {
        // given
        Long userId = 99L;
        int page = 0;

        // when
        NotificationPageResponse result = notificationQueryService.getNotifications(userId, page);

        // then
        assertThat(result.totalCount()).isEqualTo(0);
        assertThat(result.notifications()).isEmpty();
    }
}