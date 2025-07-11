package maruhxn.rankademy.domain.member;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import lombok.*;
import maruhxn.rankademy.domain.shared.AbstractEntity;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;

import java.time.LocalDateTime;

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

    private double winRate;

    private LocalDateTime enrolledAt;

    @Builder
    public SummonerInfo(String puuid, String summonerName, String summonerTag, int summonerIconNum, TierInfo tierInfo, double winRate, LocalDateTime enrolledAt) {
        this.puuid = puuid;
        this.summonerName = summonerName;
        this.summonerTag = summonerTag;
        this.summonerIconNum = summonerIconNum;
        this.tierInfo = tierInfo;
        this.winRate = winRate;
        this.enrolledAt = enrolledAt;
    }

    public static SummonerInfo create(String puuid, String summonerName, String summonerTag, int summonerIconNum, TierInfo tierInfo, double winRate) {
        return SummonerInfo.builder()
                .puuid(requireNonNull(puuid))
                .summonerName(requireNonNull(summonerName))
                .summonerTag(requireNonNull(summonerTag))
                .summonerIconNum(summonerIconNum)
                .tierInfo(new TierInfo(
                        tierInfo.tier(),
                        tierInfo.rank(),
                        tierInfo.lp()
                ))
                .winRate(winRate)
                .enrolledAt(LocalDateTime.now())
                .build();
    }
}
