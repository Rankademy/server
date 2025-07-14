package maruhxn.rankademy.adapter.integration;

import maruhxn.rankademy.application.member.required.RiotApiProvider;
import maruhxn.rankademy.domain.member.dto.RiotAuthRequest;
import maruhxn.rankademy.domain.member.dto.RiotLeagueEntryResponse;
import maruhxn.rankademy.domain.member.dto.RiotSummonerResponse;
import org.springframework.stereotype.Component;

@Component
public class DummyRiotApiProvider implements RiotApiProvider {

    @Override
    public String getPuuid(RiotAuthRequest riotAuthRequest) {
        return "";
    }

    @Override
    public RiotSummonerResponse getSummonerInfoByPuuid(String puuid) {
        return null;
    }

    @Override
    public RiotLeagueEntryResponse getSoloRankInfoByPuuid(String puuid) {
        return null;
    }
}
