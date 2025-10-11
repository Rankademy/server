package maruhxn.rankademy.domain.group.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record GroupCreateRequest(
        @NotEmpty
        @Size(max = 100)
        String name,

        @NotEmpty
        String about,

        @NotEmpty
        String logoImage
) {
}
