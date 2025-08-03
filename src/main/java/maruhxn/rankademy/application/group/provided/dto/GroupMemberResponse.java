package maruhxn.rankademy.application.group.provided.dto;

import maruhxn.rankademy.domain.user.LolPosition;
import maruhxn.rankademy.domain.user.TierInfo;

public record GroupMemberResponse(
        String summonerName,
        String summonerTag,
        int summonerIconId,
        String major,
        int admissionYear,
        LolPosition mainPosition,
        LolPosition subPosition,
        TierInfo tierInfo,
        RecordInfoDto recordInfo
) {
}
