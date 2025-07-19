package maruhxn.rankademy.domain.member;

import jakarta.persistence.*;
import lombok.*;
import maruhxn.rankademy.domain.match.ChampionPlayRecord;
import maruhxn.rankademy.domain.match.MatchData;
import maruhxn.rankademy.domain.match.service.MostChampionCalculator;
import maruhxn.rankademy.domain.member.dto.RiotAuthRequest;
import maruhxn.rankademy.domain.member.dto.RiotLeagueEntryResponse;
import maruhxn.rankademy.domain.member.dto.RiotSummonerResponse;
import maruhxn.rankademy.domain.shared.AbstractEntity;
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

    private int totalMatchCnt;

    private double winRate;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "summoner_most_champion",
            joinColumns = @JoinColumn(name = "summoner_info_id"))
    @OrderColumn(name = "champion_order")
    private List<ChampionPlayRecord> mostChampions = new ArrayList<>();

    public List<ChampionPlayRecord> getMostChampions() {
        return java.util.Collections.unmodifiableList(mostChampions);
    }

    private LocalDateTime enrolledAt;

    @Builder
    public SummonerInfo(String puuid, String summonerName, String summonerTag, int summonerIconNum, TierInfo tierInfo, int totalMatchCnt, double winRate, LocalDateTime enrolledAt) {
        this.puuid = puuid;
        this.summonerName = summonerName;
        this.summonerTag = summonerTag;
        this.summonerIconNum = summonerIconNum;
        this.tierInfo = tierInfo;
        this.totalMatchCnt = totalMatchCnt;
        this.winRate = winRate;
        this.enrolledAt = enrolledAt;
    }

    public static SummonerInfo of(String puuid, RiotAuthRequest riotAuthRequest, RiotSummonerResponse riotSummonerResponse, RiotLeagueEntryResponse soloRankEntry) {
        int totalWins = soloRankEntry.wins();
        int totalMatches = soloRankEntry.wins() + soloRankEntry.losses();

        return SummonerInfo.builder()
                .puuid(requireNonNull(puuid))
                .summonerName(riotAuthRequest.summonerName())
                .summonerTag(riotAuthRequest.summonerTag())
                .summonerIconNum(riotSummonerResponse.profileIconId())
                .tierInfo(TierInfo.from(soloRankEntry))
                .winRate((double) totalWins / totalMatches * 100)
                .totalMatchCnt(totalMatches)
                .enrolledAt(LocalDateTime.now())
                .build();
    }

    public void updateMostChampions(MostChampionCalculator mostChampionCalculator, List<MatchData> newMatches) {
        this.mostChampions.clear();
        List<ChampionPlayRecord> mostChampions =
                mostChampionCalculator.calculateMostChampionsTop3(newMatches, puuid);
        this.mostChampions.addAll(mostChampions);
    }
}
