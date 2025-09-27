package maruhxn.rankademy.adapter.persistence.ranking;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.webapi.dto.RankerDto;
import maruhxn.rankademy.adapter.webapi.dto.UnivRankingResponse;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TotalRankingRepository {

    private final Long PAGE_SIZE = 20L;

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

    public List<UnivRankingResponse> getTotalUserRanking(int page) {
        return null;
    }
}
