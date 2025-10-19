package maruhxn.rankademy.application.notification;

import jakarta.persistence.EntityManager;
import maruhxn.rankademy.application.notification.provided.NotificationModifier;
import maruhxn.rankademy.application.notification.required.NotificationRepository;
import maruhxn.rankademy.domain.notification.Notification;
import maruhxn.rankademy.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("NotificationModifier 테스트")
class NotificationModifierTest extends IntegrationTestSupport {

    @Autowired
    NotificationModifier modifier;

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("알림을 확인 처리한다.")
    void confirm() {
        // given
        Notification notification = notificationRepository.save(
                Notification.create(1L, "Test Message", LocalDateTime.now()));
        assertThat(notification.isConfirmed()).isFalse();
        assertThat(notification.getConfirmedAt()).isNull();

        // when
        modifier.confirm(notification.getId());
        em.flush();
        em.clear();

        // then
        Notification confirmedNotification = notificationRepository.findById(notification.getId()).orElseThrow();
        assertThat(confirmedNotification.isConfirmed()).isTrue();
        assertThat(confirmedNotification.getConfirmedAt()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 알림을 확인하려고 하면 예외가 발생한다.")
    void confirm_whenNotificationNotFound() {
        // given
        Long nonExistentNotificationId = 999L;

        // when & then
        assertThatThrownBy(() -> modifier.confirm(nonExistentNotificationId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("알림 정보를 찾을 수 없습니다. notificationId = " + nonExistentNotificationId);
    }
}
