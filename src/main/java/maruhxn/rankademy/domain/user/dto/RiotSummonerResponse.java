package maruhxn.rankademy.domain.user.dto;

public record RiotSummonerResponse(
        String id, // summonerId
        String accountId,
        String puuid,
        int profileIconId,
        Long revisionDate,
        Long summonerLevel
) {
}
