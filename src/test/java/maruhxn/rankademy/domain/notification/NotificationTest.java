package maruhxn.rankademy.domain.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("도메인 - 알림")
class NotificationTest {

    @Test
    @DisplayName("알림 생성 성공")
    void create() {
        // given
        Long userId = 1L;
        String message = "test message";
        LocalDateTime deliveredAt = LocalDateTime.now();

        // when
        Notification notification = Notification.create(userId, message, deliveredAt);

        // then
        assertThat(notification).isNotNull();
        assertThat(notification.getUserId()).isEqualTo(userId);
        assertThat(notification.getMessage()).isEqualTo(message);
        assertThat(notification.getDeliveredAt()).isEqualTo(deliveredAt);
        assertThat(notification.isConfirmed()).isFalse();
        assertThat(notification.getConfirmedAt()).isNull();
    }

    @Test
    @DisplayName("알림 생성 시 Null 값 전달 시 예외 발생")
    void createWithNull() {
        // given
        Long userId = 1L;
        String message = "test message";
        LocalDateTime deliveredAt = LocalDateTime.now();

        // when & then
        assertThatThrownBy(() -> Notification.create(null, message, deliveredAt))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> Notification.create(userId, null, deliveredAt))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> Notification.create(userId, message, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("알림 확인")
    void confirm() {
        // given
        Notification notification = Notification.create(1L, "message", LocalDateTime.now());
        LocalDateTime now = LocalDateTime.now();

        // when
        notification.confirm(now);

        // then
        assertThat(notification.isConfirmed()).isTrue();
        assertThat(notification.getConfirmedAt()).isEqualTo(now);
    }
}
