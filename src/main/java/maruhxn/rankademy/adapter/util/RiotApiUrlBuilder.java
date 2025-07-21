package maruhxn.rankademy.adapter.util;

public class RiotApiUrlBuilder {

    private static final String RIOT_API_BASE_URL = "api.riotgames.com";

    public static String getAccountInfoUrl(String gameName, String tagLine) {
        return String.format("https://asia.%s/riot/account/v1/accounts/by-riot-id/%s/%s", RIOT_API_BASE_URL, gameName, tagLine);
    }

    public static String getSummonerInfoUrl(String puuid) {
        return String.format("https://kr.%s/lol/summoner/v4/summoners/by-puuid/%s", RIOT_API_BASE_URL, puuid);
    }

    public static String getLeagueInfoUrl(String puuid) {
        return String.format("https://kr.%s/lol/league/v4/entries/by-puuid/%s", RIOT_API_BASE_URL, puuid);
    }

    public static String getMatchIdsUrl(String puuid, Long startTime, int start, int chunkSize) {
        return String.format("https://asia.%s/lol/match/v5/matches/by-puuid/%s/ids?startTime=%d&queue=420&type=ranked&start=%d&count=%d", RIOT_API_BASE_URL, puuid, startTime, start, chunkSize);
    }

    public static String getMatchInfoUrl(String matchId) {
        return String.format("https://asia.%s/lol/match/v5/matches/%s", RIOT_API_BASE_URL, matchId);
    }
}