package maruhxn.rankademy.domain.member.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import maruhxn.rankademy.domain.member.LolPosition;

public record MemberProfileUpdateRequest(
        @Size(min = 5, max = 20) String username,

        @Size(max = 255)
        String description,

        @NotNull
        LolPosition mainPosition,

        @NotNull
        LolPosition subPosition

) {
}
