package maruhxn.rankademy.application.member.required;

import maruhxn.rankademy.domain.member.dto.RiotAuthRequest;
import maruhxn.rankademy.domain.member.dto.RiotLeagueEntryResponse;
import maruhxn.rankademy.domain.member.dto.RiotSummonerResponse;

public interface RiotApiProvider {

    String getPuuid(RiotAuthRequest riotAuthRequest);

    RiotSummonerResponse getSummonerInfoByPuuid(String puuid);

    RiotLeagueEntryResponse getSoloRankInfoByPuuid(String puuid);
}
