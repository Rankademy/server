package maruhxn.rankademy.domain.group;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;

@Embeddable
public record JoinRequest(
        @Column(name = "user_id", nullable = false)
        Long userId,

        LocalDateTime requestedAt
) {
    public JoinRequest(Long userId) {
        this(userId, LocalDateTime.now());
    }
}