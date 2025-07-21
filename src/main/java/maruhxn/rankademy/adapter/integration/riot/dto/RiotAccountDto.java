package maruhxn.rankademy.adapter.integration.riot.dto;

/**
 * /riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}
 * <p>
 * 소환사명과 태그를 통해 puuid를 가져올 때 사용(계정 연동 시 처음에만 사용)
 */
public record RiotAccountDto(
        String puuid,
        String gameName,
        String tagLine
) {
}
