package maruhxn.rankademy.domain.member.dto;

public record RiotSummonerResponse(
        String id, // summonerId
        String accountId,
        String puuid,
        int profileIconId,
        Long revisionDate,
        Long summonerLevel
) {
}
