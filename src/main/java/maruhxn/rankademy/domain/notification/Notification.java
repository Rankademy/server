package maruhxn.rankademy.domain.notification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import maruhxn.rankademy.domain.shared.AbstractEntity;

import java.time.LocalDateTime;

import static java.util.Objects.requireNonNull;

@Table(name = "notifications")
@Entity
@Getter
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends AbstractEntity {

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private boolean isConfirmed; // 확인 여부

    @Column(nullable = false)
    private LocalDateTime deliveredAt;

    private LocalDateTime confirmedAt;

    public Notification(Long userId, String message, LocalDateTime deliveredAt) {
        this.userId = userId;
        this.message = message;
        this.deliveredAt = deliveredAt;
        this.isConfirmed = false;
    }

    public static Notification create(Long userId, String message, LocalDateTime deliveredAt) {
        return new Notification(
                requireNonNull(userId),
                requireNonNull(message),
                requireNonNull(deliveredAt)
        );
    }

    public void confirm(LocalDateTime now) {
        this.isConfirmed = true;
        this.confirmedAt = now;
    }
}
