package maruhxn.rankademy.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record RefreshToken(
        @Column(nullable = false, unique = true)
        String payload
) {
}
