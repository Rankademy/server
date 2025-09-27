package maruhxn.rankademy.adapter.webapi.ranking.dto;

import maruhxn.rankademy.domain.user.LolPosition;

public record GroupRankingFilter(
        String groupNameKey,
        String major,
        Integer admissionYear,
        LolPosition mainPosition
) {
}