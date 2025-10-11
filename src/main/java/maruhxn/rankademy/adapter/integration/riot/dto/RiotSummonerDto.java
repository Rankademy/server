package maruhxn.rankademy.adapter.integration.riot.dto;

/**
 * /lol/summoner/v4/summoners/by-puuid/{encryptedPUUID}
 * <p>
 * summonerId와 profileIcon, summonerLevel을 가져올 때 사용
 */
public record RiotSummonerDto(
        String id, // summonerInfoId
        String accountId,
        String puuid,
        int profileIconId,
        Long revisionDate,
        Long summonerLevel
) {
}
