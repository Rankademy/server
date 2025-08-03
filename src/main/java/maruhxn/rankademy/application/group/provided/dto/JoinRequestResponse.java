package maruhxn.rankademy.application.group.provided.dto;

import java.time.LocalDateTime;

public record JoinRequestResponse(
        Long userId,
        String summonerName,
        String summonerTag,
        LocalDateTime requestedAt
) {
}
