package maruhxn.rankademy.domain.user.dto;

import jakarta.validation.constraints.NotEmpty;

public record RiotAuthRequest(
        @NotEmpty
        String summonerName,

        @NotEmpty
        String summonerTag
) {

}
