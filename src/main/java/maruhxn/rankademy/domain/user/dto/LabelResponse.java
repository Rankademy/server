package maruhxn.rankademy.domain.user.dto;

import java.util.List;

public record LabelResponse(
        String puuid,
        String platform,
        List<LabelDto> labels,
        Integer matchCountRequested,
        Integer matchCountCollected
) {
    public record LabelDto(
            String name,
            Integer priority
    ) {
    }
}
