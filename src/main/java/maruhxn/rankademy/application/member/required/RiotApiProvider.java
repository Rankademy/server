package maruhxn.rankademy.application.member.required;

import maruhxn.rankademy.domain.match.MatchData;
import maruhxn.rankademy.domain.member.dto.RiotAuthRequest;
import maruhxn.rankademy.domain.member.dto.RiotLeagueEntryResponse;
import maruhxn.rankademy.domain.member.dto.RiotSummonerResponse;

import java.util.List;

public interface RiotApiProvider {

    // https://asia.api.riotgames.com/riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}
    String getPuuid(RiotAuthRequest riotAuthRequest);

    // https://kr.api.riotgames.com/lol/summoner/v4/summoners/by-puuid/{puuid}
    RiotSummonerResponse getSummonerInfoByPuuid(String puuid);

    // https://kr.api.riotgames.com/lol/league/v4/entries/by-puuid{puuid}
    RiotLeagueEntryResponse getSoloRankInfoByPuuid(String puuid);

    // queue: 420(솔랭), 440(자랭)
    // https://asia.api.riotgames.com/lol/match/v5/matches/by-puuid/{puuid}/ids?startTime={startTime}&queue=420&type=ranked&start={start}&count={chunkSize}
    List<String> getMatchIds(String puuid, Long startTime, int start, int chunkSize);

    // https://asia.api.riotgames.com/lol/match/v5/matches/{matchId}
    MatchData getMatchInfo(String matchId, Long memberId);
}
