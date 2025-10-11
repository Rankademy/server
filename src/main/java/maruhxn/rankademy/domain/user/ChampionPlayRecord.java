package maruhxn.rankademy.domain.user;

import jakarta.persistence.Embeddable;

@Embeddable
public record ChampionPlayRecord(
        String championId,
        Long playCount
) {
}
