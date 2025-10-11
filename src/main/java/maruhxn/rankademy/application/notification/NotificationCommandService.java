package maruhxn.rankademy.application.notification;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.notification.provided.NotificationModifier;
import maruhxn.rankademy.application.notification.required.NotificationRepository;
import maruhxn.rankademy.domain.notification.Notification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationCommandService implements NotificationModifier {

    private final NotificationRepository notificationRepository;

    @Override
    public void confirm(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NoSuchElementException("알림 정보를 찾을 수 없습니다. notificationId = " + notificationId));

        notification.confirm(LocalDateTime.now());
    }
}
