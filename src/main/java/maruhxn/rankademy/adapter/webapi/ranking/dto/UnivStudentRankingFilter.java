package maruhxn.rankademy.adapter.webapi.ranking.dto;

import maruhxn.rankademy.domain.user.LolPosition;

public record UnivStudentRankingFilter(
        String major,
        Integer admissionYear,
        LolPosition mainPosition,
        String userNameKey
) {
}
