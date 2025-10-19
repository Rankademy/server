package maruhxn.rankademy.adapter.persistence.ranking;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.webapi.dto.RankerDto;
import maruhxn.rankademy.adapter.webapi.dto.TotalUserRankingResponse;
import maruhxn.rankademy.adapter.webapi.dto.UnivRankingResponse;
import maruhxn.rankademy.domain.competition.CompetitionStatus;
import maruhxn.rankademy.domain.user.UserAuthStatus;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Repository;

import java.util.List;

import static maruhxn.rankademy.domain.competition.QCompetition.competition;
import static maruhxn.rankademy.domain.group.QGroup.group;
import static maruhxn.rankademy.domain.group.QGroupMember.groupMember;
import static maruhxn.rankademy.domain.user.QSummonerInfo.summonerInfo;
import static maruhxn.rankademy.domain.user.QUser.user;

@Repository
@RequiredArgsConstructor
public class TotalRankingRepository {

    private final int PAGE_SIZE = 20;

    private final JPAQueryFactory queryFactory;

    public List<UnivRankingResponse> getUnivRanking(int page, String univNameKey) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);

        NumberExpression<Long> competitionCnt =
                Expressions.numberTemplate(Long.class,
                        "coalesce(count(distinct {0}), 0)", competition.id);

        NumberExpression<Long> winCount =
                Expressions.numberTemplate(Long.class,
                        "coalesce(count(distinct case when {0} = {1} then {2} end), 0)",
                        competition.finalWinnerGroupId, group.id, competition.id);

        List<UnivRankingResponse> results = queryFactory
                .select(Projections.constructor(
                                UnivRankingResponse.class,
                                user.univInfo.univName,
                                user.id.countDistinct(),
                                competitionCnt,
                                winCount,
                                Expressions.nullExpression(RankerDto.class)
                        )
                )
                .from(user)
                .join(user.summonerInfo, summonerInfo)
                .leftJoin(groupMember).on(groupMember.user.id.eq(user.id))
                .leftJoin(group).on(groupMember.group.id.eq(group.id))
                .leftJoin(competition).on(
                        competition.status.eq(CompetitionStatus.COMPLETED)
                                .and(
                                        competition.finalWinnerGroupId.eq(group.id)
                                                .or(competition.finalLoserGroupId.eq(group.id))
                                )
                )
                .groupBy(user.univInfo.univName)
                .orderBy(winCount.desc(), competitionCnt.desc(), user.id.count().asc())
                .where(
                        isAuthorized(),
                        filteredByUnivNameKey(univNameKey)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return results.stream().map(ur -> {
            RankerDto ranker = queryFactory.select(
                            Projections.constructor(
                                    RankerDto.class,
                                    user.id,
                                    user.username,
                                    user.summonerInfo.summonerIconNum
                            )
                    )
                    .from(user)
                    .join(user.summonerInfo, summonerInfo)
                    .where(isAuthorized(), user.univInfo.univName.eq(ur.univName()))
                    .orderBy(user.summonerInfo.tierInfo.mappedTier.desc())
                    .limit(1L)
                    .fetchOne();

            return new UnivRankingResponse(
                    ur.univName(),
                    ur.totalUserCnt(),
                    ur.competitionTotalCnt(),
                    ur.competitionWinCnt(),
                    ranker
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
                        isAuthorized(),
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
                        user.univInfo.univName,
                        summonerInfo.tierInfo,
                        summonerInfo.winCount,
                        summonerInfo.lossCount,
                        user.mainPosition,
                        user.subPosition
                ))
                .from(user)
                .join(user.summonerInfo, summonerInfo)
                .where(
                        isAuthorized(),
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

    private static BooleanExpression isAuthorized() {
        return user.authStatus.eq(UserAuthStatus.AUTHORIZED);
    }

    private static BooleanExpression filteredByUnivNameKey(String univNameKey) {
        if (univNameKey == null) return null;
        String trimmed = univNameKey.trim();
        if (trimmed.isEmpty()) return null;
        // 대소문자 무시 부분검색
        return user.univInfo.univName.containsIgnoreCase(trimmed);
    }

    private static BooleanExpression filteredBySummonerNameKey(String key) {
        if (key == null) return null;
        String trimmed = key.trim();
        if (trimmed.isEmpty()) return null;
        // 대소문자 무시 부분검색
        return user.summonerInfo.summonerName.containsIgnoreCase(trimmed);
    }
}
