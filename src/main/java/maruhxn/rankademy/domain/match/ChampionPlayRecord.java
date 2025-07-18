package maruhxn.rankademy.domain.match;

import jakarta.persistence.Embeddable;

@Embeddable
public record ChampionPlayRecord(
        String championId,
        Long playCount
) {
}
