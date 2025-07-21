package maruhxn.rankademy.domain.user.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import maruhxn.rankademy.domain.user.LolPosition;

public record ProfileUpdateRequest(
        @Size(min = 2, max = 20) String username,

        @Size(max = 255)
        String description,

        @NotNull
        LolPosition mainPosition,

        @NotNull
        LolPosition subPosition
) {
}
