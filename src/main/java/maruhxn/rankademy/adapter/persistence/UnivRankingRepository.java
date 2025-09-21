package maruhxn.rankademy.adapter.persistence;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.webapi.dto.RankerDto;
import maruhxn.rankademy.adapter.webapi.dto.UnivRankingResponse;
import maruhxn.rankademy.adapter.webapi.dto.UnivStudentRankingResponse;
import maruhxn.rankademy.adapter.webapi.ranking.dto.UnivStudentRankingFilter;
import maruhxn.rankademy.domain.user.ChampionPlayRecord;
import maruhxn.rankademy.domain.user.LolPosition;
import maruhxn.rankademy.domain.user.SummonerInfo;
import maruhxn.rankademy.domain.user.User;
import org.springframework.stereotype.Repository;

import java.util.List;

import static maruhxn.rankademy.domain.user.QSummonerInfo.summonerInfo;
import static maruhxn.rankademy.domain.user.QUser.user;

@Repository
@RequiredArgsConstructor
public class UnivRankingRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public List<UnivRankingResponse> getUnivRanking(int page) {
        List<UnivRankingResponse> results = em.createQuery(
                        "SELECT NEW maruhxn.rankademy.adapter.webapi.dto.UnivRankingResponse(" +
                                "u.univInfo.univName, " +
                                "AVG(s.tierInfo.mappedTier), " +
                                "SUM(s.winCount), " +
                                "COUNT(u.id), " +
                                "null" +
                                ")" +
                                "FROM User u " +
                                "JOIN u.summonerInfo s " +
                                "WHERE u.univInfo.univVerified = true " +
                                "GROUP BY u.univInfo.univName " +
                                "ORDER BY SUM(s.winCount) DESC",
                        UnivRankingResponse.class)
                .setFirstResult(10 * page)
                .setMaxResults(10)
                .getResultList();

        return results.stream()
                .map(ur -> {
                    RankerDto rankerDto = em.createQuery(
                                    "SELECT NEW maruhxn.rankademy.adapter.webapi.dto.RankerDto(" +
                                            " u.id, " +
                                            " u.username, " +
                                            " s.summonerIconNum " +
                                            ")" +
                                            "FROM User  u " +
                                            "JOIN u.summonerInfo s " +
                                            "WHERE u.univInfo.univName = :univName " +
                                            "ORDER BY s.tierInfo.mappedTier DESC, s.winCount DESC",
                                    RankerDto.class
                            ).setParameter("univName", ur.univName())
                            .setMaxResults(1)
                            .getSingleResult();
                    return new UnivRankingResponse(
                            ur.univName(),
                            ur.tierInfo(),
                            ur.winCount(),
                            ur.totalUserCnt(),
                            rankerDto
                    );
                }).toList();
    }

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
                .offset(page * 20L)
                .limit(20L)
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

    private static Predicate filteredByMainPosition(LolPosition mainPosition) {
        return mainPosition == null ? null : user.mainPosition.eq(mainPosition);
    }

    private static Predicate filteredByAdmissionYear(Integer admissionYear) {
        return admissionYear == null ? null : user.univInfo.admissionYear.eq(admissionYear);
    }

    private static BooleanExpression filteredByMajor(String major) {
        return major == null ? null : user.univInfo.major.eq(major);
    }
}
