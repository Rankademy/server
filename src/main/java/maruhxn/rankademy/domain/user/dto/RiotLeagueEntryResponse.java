package maruhxn.rankademy.domain.user.dto;

public record RiotLeagueEntryResponse(
        String leagueId,
        String queueType, // RANKED_SOLO_5x5, RANKED_FLEX_SR
        String tier,
        String rank,
        String summonerId,
        String puuid,
        int leaguePoints, // 포인트
        int wins, // 승리 수
        int losses, // 패배 수
        boolean veteran,
        boolean inactive,
        boolean freshBlood,
        boolean hotStreak
) {
}
