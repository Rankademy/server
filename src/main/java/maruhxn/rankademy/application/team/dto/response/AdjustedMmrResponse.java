package maruhxn.rankademy.application.team.dto.response;

import java.util.Collections;
import java.util.List;

public record AdjustedMmrResponse(
        List<PlayerMmr> players
) {

    public static AdjustedMmrResponse empty() {
        return new AdjustedMmrResponse(Collections.emptyList());
    }

    public record PlayerMmr(
            String puuid,
            Double mu,
            Double sigma
    ) {
    }
}
