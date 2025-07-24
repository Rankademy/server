package maruhxn.rankademy.domain.group;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record JoinRequest(
        @Column(name = "user_id", nullable = false)
        Long userId
) {
}