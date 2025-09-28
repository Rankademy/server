package maruhxn.rankademy.adapter.webapi.dto;

public record UnivRankingResponse(
        String univName,
        Long totalUserCnt,
        Long competitionTotalCnt,
        Long competitionWinCnt,
        RankerDto rankerDto
) {
}
