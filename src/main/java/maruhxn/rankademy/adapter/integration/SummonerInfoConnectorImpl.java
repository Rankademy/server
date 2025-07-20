package maruhxn.rankademy.adapter.integration;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.user.required.RiotApiProvider;
import maruhxn.rankademy.domain.user.SummonerInfo;
import maruhxn.rankademy.domain.user.dto.RiotAuthRequest;
import maruhxn.rankademy.domain.user.dto.RiotLeagueEntryResponse;
import maruhxn.rankademy.domain.user.dto.RiotSummonerResponse;
import maruhxn.rankademy.domain.user.service.SummonerInfoConnector;
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
