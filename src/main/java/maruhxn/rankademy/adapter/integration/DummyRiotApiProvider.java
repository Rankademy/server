package maruhxn.rankademy.adapter.integration;

import maruhxn.rankademy.application.member.required.RiotApiProvider;
import maruhxn.rankademy.domain.match.MatchData;
import maruhxn.rankademy.domain.member.dto.RiotAuthRequest;
import maruhxn.rankademy.domain.member.dto.RiotLeagueEntryResponse;
import maruhxn.rankademy.domain.member.dto.RiotSummonerResponse;
import org.springframework.context.annotation.Fallback;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Fallback
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

    @Override
    public List<String> getMatchIds(String puuid, Long startTime, int start, int chunkSize) {
        return List.of();
    }

    @Override
    public MatchData getMatchInfo(String matchId, Long memberId) {
        return new MatchData(
                UUID.randomUUID().toString(),
                memberId,
                "anyJsonData"
        );
    }
}
