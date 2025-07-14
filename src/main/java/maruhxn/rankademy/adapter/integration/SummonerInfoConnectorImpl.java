package maruhxn.rankademy.adapter.integration;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.member.required.RiotApiProvider;
import maruhxn.rankademy.domain.member.SummonerInfo;
import maruhxn.rankademy.domain.member.dto.RiotAuthRequest;
import maruhxn.rankademy.domain.member.dto.RiotLeagueEntryResponse;
import maruhxn.rankademy.domain.member.dto.RiotSummonerResponse;
import maruhxn.rankademy.domain.member.service.SummonerInfoConnector;
import org.springframework.context.annotation.Fallback;
import org.springframework.stereotype.Component;

@Component
@Fallback
@RequiredArgsConstructor
public class SummonerInfoConnectorImpl implements SummonerInfoConnector {

    private final RiotApiProvider riotApiProvider;

    public SummonerInfo connect(RiotAuthRequest riotAuthRequest) {
        String puuid = riotApiProvider.getPuuid(riotAuthRequest);
        RiotSummonerResponse riotSummonerResponse = riotApiProvider.getSummonerInfoByPuuid(puuid);
        RiotLeagueEntryResponse soloRankEntry = riotApiProvider.getSoloRankInfoByPuuid(puuid);

        return SummonerInfo.of(puuid, riotAuthRequest, riotSummonerResponse, soloRankEntry);
    }

}
