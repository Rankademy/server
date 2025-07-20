package maruhxn.rankademy.adapter.webapi.dto;

import maruhxn.rankademy.domain.user.TierInfo;

public record SearchedUserResponse(
        String username,
        int summonerIcon,
        String univName,
        String major,
        String admissionYear,
        TierInfo tierInfo
) {
}
