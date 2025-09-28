package maruhxn.rankademy.adapter.persistence.ranking;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.webapi.dto.RankerDto;
import maruhxn.rankademy.adapter.webapi.dto.TotalUserRankingResponse;
import maruhxn.rankademy.adapter.webapi.dto.UnivRankingResponse;
import maruhxn.rankademy.domain.user.UserAuthStatus;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Repository;

import java.util.List;

import static maruhxn.rankademy.domain.user.QSummonerInfo.summonerInfo;
import static maruhxn.rankademy.domain.user.QUser.user;

@Repository
@RequiredArgsConstructor
public class TotalRankingRepository {

    private final int PAGE_SIZE = 20;

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public List<UnivRankingResponse> getUnivRanking(int page, String univNameKey) {
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

    public PagedModel<TotalUserRankingResponse> getTotalUserRanking(int page, String userNameKey) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);

        Long totalCount = queryFactory
                .select(user.count())
                .from(user)
                .join(user.summonerInfo, summonerInfo)
                .where(
                        user.authStatus.eq(UserAuthStatus.AUTHORIZED),
                        filteredBySummonerNameKey(userNameKey)
                )
                .fetchOne();

        long total = (totalCount == null ? 0L : totalCount);
        if (total <= 0) {
            return new PagedModel<>(new PageImpl<>(List.of(), pageable, 0L));
        }

        List<TotalUserRankingResponse> result = queryFactory
                .select(Projections.constructor(
                        TotalUserRankingResponse.class,
                        user.id,
                        summonerInfo.puuid,
                        summonerInfo.summonerName,
                        summonerInfo.summonerTag,
                        summonerInfo.summonerIconNum,
                        summonerInfo.tierInfo,
                        summonerInfo.winCount,
                        summonerInfo.lossCount,
                        user.mainPosition,
                        user.subPosition
                ))
                .from(user)
                .join(user.summonerInfo, summonerInfo)
                .where(
                        user.authStatus.eq(UserAuthStatus.AUTHORIZED),
                        filteredBySummonerNameKey(userNameKey)
                )
                .orderBy(
                        summonerInfo.tierInfo.mappedTier.desc(), // 티어 내림차순
                        user.id.desc()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PagedModel<>(new PageImpl<>(result, pageable, total));
    }

    private static BooleanExpression filteredBySummonerNameKey(String key) {
        if (key == null) return null;
        String trimmed = key.trim();
        if (trimmed.isEmpty()) return null;
        // 대소문자 무시 부분검색
        return user.summonerInfo.summonerName.containsIgnoreCase(trimmed);
    }
}
