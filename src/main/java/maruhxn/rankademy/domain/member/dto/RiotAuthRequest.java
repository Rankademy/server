package maruhxn.rankademy.domain.member.dto;

import jakarta.validation.constraints.NotEmpty;

public record RiotAuthRequest(
        @NotEmpty
        String summonerName,

        @NotEmpty
        String summonerTag
) {

}
