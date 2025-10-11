package maruhxn.rankademy.application.notification.required;

import maruhxn.rankademy.domain.notification.Notification;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface NotificationRepository extends Repository<Notification, Long> {

    Notification save(Notification notification);

    Optional<Notification> findById(Long id);
}
