package maruhxn.rankademy.adapter.persistence.ranking;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.webapi.dto.RankerDto;
import maruhxn.rankademy.adapter.webapi.dto.UnivRankingResponse;
import maruhxn.rankademy.adapter.webapi.dto.UnivStudentRankingResponse;
import maruhxn.rankademy.adapter.webapi.ranking.dto.UnivStudentRankingFilter;
import maruhxn.rankademy.domain.user.ChampionPlayRecord;
import maruhxn.rankademy.domain.user.SummonerInfo;
import maruhxn.rankademy.domain.user.User;
import org.springframework.stereotype.Repository;

import java.util.List;

import static maruhxn.rankademy.adapter.persistence.ranking.WhereClauseHelper.*;
import static maruhxn.rankademy.domain.user.QSummonerInfo.summonerInfo;
import static maruhxn.rankademy.domain.user.QUser.user;

@Repository
@RequiredArgsConstructor
public class OnCampusRankingRepository {

    private final Long PAGE_SIZE = 20L;

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public List<UnivStudentRankingResponse> getUnivStudentRanking(String univName, int page, UnivStudentRankingFilter univStudentRankingFilter) {
        List<User> users = queryFactory.selectFrom(user)
                .join(summonerInfo).on(user.summonerInfo.id.eq(summonerInfo.id))
                .where(
                        user.univInfo.univName.eq(univName),
                        filteredByMajor(univStudentRankingFilter.major()),
                        filteredByAdmissionYear(univStudentRankingFilter.admissionYear()),
                        filteredByMainPosition(univStudentRankingFilter.mainPosition())
                )
                .orderBy(summonerInfo.tierInfo.mappedTier.desc(), summonerInfo.winCount.desc())
                .offset(page * PAGE_SIZE)
                .limit(PAGE_SIZE)
                .fetch();

        return users.stream()
                .map(u -> {
                    SummonerInfo s = u.getSummonerInfo();
                    List<String> mostChampionIds = s.getMostChampions().stream()
                            .map(ChampionPlayRecord::championId)
                            .toList();

                    double winRate = s.getWinCount() + s.getLossCount() > 0 ?
                            ((double) s.getWinCount()) / (s.getWinCount() + s.getLossCount()) * 100 : 0.0;

                    return new UnivStudentRankingResponse(
                            u.getId(),
                            s.getPuuid(),
                            s.getSummonerName(),
                            s.getSummonerTag(),
                            s.getSummonerIconNum(),
                            s.getTierInfo(),
                            winRate,
                            mostChampionIds,
                            u.getMainPosition(),
                            u.getSubPosition(),
                            u.getUnivInfo().getAdmissionYear(),
                            u.getUnivInfo().getMajor()
                    );
                })
                .toList();
    }
}
