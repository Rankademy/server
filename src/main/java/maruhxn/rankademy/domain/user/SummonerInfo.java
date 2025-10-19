package maruhxn.rankademy.domain.user;

import jakarta.persistence.*;
import lombok.*;
import maruhxn.rankademy.domain.match.MatchData;
import maruhxn.rankademy.domain.match.service.MostChampionCalculator;
import maruhxn.rankademy.domain.shared.AbstractEntity;
import maruhxn.rankademy.domain.user.dto.RiotAuthRequest;
import maruhxn.rankademy.domain.user.dto.RiotLeagueEntryResponse;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Entity
@Getter
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@NaturalIdCache
public class SummonerInfo extends AbstractEntity {

    @NaturalId
    @Column(nullable = false)
    private String puuid;

    @Column(nullable = false, length = 100)
    private String summonerName;

    @Column(nullable = false, length = 10)
    private String summonerTag;

    @Column(nullable = false)
    private int summonerIconNum;

    @Embedded
    private TierInfo tierInfo;

    private int winCount;

    private int lossCount;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "summoner_most_champion",
            joinColumns = @JoinColumn(name = "summoner_info_id"))
    @OrderColumn(name = "champion_order")
    private List<ChampionPlayRecord> mostChampions = new ArrayList<>();

    private LocalDateTime enrolledAt;

    @Column(name = "last_synced_match_id", length = 100)
    private String lastSyncedMatchId;

    private LocalDateTime matchSyncedAt;

    @Builder
    public SummonerInfo(String puuid, String summonerName, String summonerTag, int summonerIconNum, TierInfo tierInfo, int winCount, int lossCount, LocalDateTime enrolledAt, String lastSyncedMatchId, LocalDateTime matchSyncedAt) {
        this.puuid = puuid;
        this.summonerName = summonerName;
        this.summonerTag = summonerTag;
        this.summonerIconNum = summonerIconNum;
        this.tierInfo = tierInfo;
        this.winCount = winCount;
        this.lossCount = lossCount;
        this.enrolledAt = enrolledAt;
        this.lastSyncedMatchId = lastSyncedMatchId;
        this.matchSyncedAt = matchSyncedAt;
    }

    public static SummonerInfo of(String puuid, RiotAuthRequest riotAuthRequest, int summonerIconId, RiotLeagueEntryResponse soloRankEntry) {
        return SummonerInfo.builder()
                .puuid(requireNonNull(puuid))
                .summonerName(riotAuthRequest.summonerName())
                .summonerTag(riotAuthRequest.summonerTag())
                .summonerIconNum(summonerIconId)
                .tierInfo(TierInfo.from(soloRankEntry))
                .winCount(soloRankEntry.wins())
                .lossCount(soloRankEntry.losses())
                .enrolledAt(LocalDateTime.now())
                .lastSyncedMatchId(null)
                .matchSyncedAt(null)
                .build();
    }

    public void update(String summonerName, String summonerTag, int summonerIconNum, TierInfo tierInfo, int winCount, int lossCount) {
        this.summonerName = summonerName;
        this.summonerTag = summonerTag;
        this.summonerIconNum = summonerIconNum;
        this.tierInfo = tierInfo;
        this.winCount = winCount;
        this.lossCount = lossCount;
    }

    public void updateMostChampions(MostChampionCalculator mostChampionCalculator, List<MatchData> newMatches) {
        this.mostChampions.clear();
        List<ChampionPlayRecord> mostChampions =
                mostChampionCalculator.calculateMostChampionsTop3(newMatches, puuid);
        this.mostChampions.addAll(mostChampions);
    }

    public int getTotalMatchCnt() {
        return winCount + lossCount;
    }

    public double getWinRate() {
        return (double) winCount / getTotalMatchCnt() * 100;
    }

    public void updateMatchSyncStatus(String lastSyncedMatchId) {
        this.lastSyncedMatchId = lastSyncedMatchId;
        this.matchSyncedAt = LocalDateTime.now();
    }

    public void touchMatchSync() {
        this.matchSyncedAt = LocalDateTime.now();
    }
}
