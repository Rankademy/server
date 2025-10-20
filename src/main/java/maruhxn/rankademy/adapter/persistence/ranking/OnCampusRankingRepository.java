package maruhxn.rankademy.adapter.persistence.ranking;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.webapi.dto.UnivStudentRankingResponse;
import maruhxn.rankademy.adapter.webapi.ranking.dto.GroupRankingFilter;
import maruhxn.rankademy.adapter.webapi.ranking.dto.UnivStudentRankingFilter;
import maruhxn.rankademy.application.group.provided.dto.GroupResponse;
import maruhxn.rankademy.application.group.provided.dto.GroupSortKey;
import maruhxn.rankademy.application.group.provided.dto.LeaderDto;
import maruhxn.rankademy.domain.competition.CompetitionStatus;
import maruhxn.rankademy.domain.group.GroupRole;
import maruhxn.rankademy.domain.group.QGroupMember;
import maruhxn.rankademy.domain.user.QUser;
import maruhxn.rankademy.domain.user.SummonerInfo;
import maruhxn.rankademy.domain.user.User;
import org.springframework.stereotype.Repository;

import java.util.List;

import static maruhxn.rankademy.adapter.persistence.ranking.WhereClauseHelper.*;
import static maruhxn.rankademy.domain.competition.QCompetition.competition;
import static maruhxn.rankademy.domain.group.QGroup.group;
import static maruhxn.rankademy.domain.group.QGroupMember.groupMember;
import static maruhxn.rankademy.domain.user.QSummonerInfo.summonerInfo;
import static maruhxn.rankademy.domain.user.QUser.user;

@Repository
@RequiredArgsConstructor
public class OnCampusRankingRepository {

    private final Long PAGE_SIZE = 20L;

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

                    return new UnivStudentRankingResponse(
                            u.getId(),
                            s.getPuuid(),
                            s.getSummonerName(),
                            s.getSummonerTag(),
                            s.getSummonerIconNum(),
                            s.getTierInfo(),
                            s.getWinRate(),
                            s.getWinCount(),
                            s.getLossCount(),
                            u.getMainPosition(),
                            u.getSubPosition(),
                            u.getUnivInfo().getAdmissionYear(),
                            u.getUnivInfo().getMajor()
                    );
                })
                .toList();
    }

    public List<GroupResponse> getGroupRanking(String univName, int page, GroupSortKey sortKey, GroupRankingFilter filter) {
        QGroupMember leaderMember = new QGroupMember("leaderMember");
        QUser leaderUser = new QUser("leaderUser");

        // ✅ 승리 수: SUM(CASE ...)를 템플릿으로 고정 (Hibernate 6 타입추론 우회)
        NumberExpression<Long> winCount =
                Expressions.numberTemplate(
                        Long.class,
                        "coalesce(sum(case when {0} = {1} then 1 else 0 end), 0)",
                        competition.finalWinnerGroupId, group.id);

        // ✅ 총 대항전 수: LEFT JOIN에 이미 참가 조건이 걸려 있으므로 COUNT(id)면 충분
        NumberExpression<Long> totalCount =
                Expressions.numberTemplate(
                        Long.class,
                        "coalesce(count({0}), 0)",
                        competition.id);

        OrderSpecifier<?> sortSpecifier = switch (sortKey) {
            case TIER -> summonerInfo.tierInfo.mappedTier.avg().desc();
            case WIN_COUNT -> winCount.desc(); // “대항전 승리 순”
        };

        return queryFactory
                .select(
                        Projections.constructor(
                                GroupResponse.class,
                                group.id,
                                group.name,
                                group.logoImage,
                                group.capacity,
                                group.members.size(),
                                totalCount.intValue(),
                                winCount.intValue(),
                                summonerInfo.tierInfo.mappedTier.avg(),
                                Projections.constructor(
                                        LeaderDto.class,
                                        leaderUser.id,
                                        leaderUser.summonerInfo.summonerName,
                                        leaderUser.summonerInfo.summonerTag,
                                        leaderUser.summonerInfo.summonerIconNum
                                )
                        )
                )
                .from(group)
                // 대항전 참여 매칭 (확정된 경기만 포함)
                .leftJoin(competition)
                .on(
                        competition.status.eq(CompetitionStatus.COMPLETED)
                                .and(
                                        competition.finalWinnerGroupId.eq(group.id)
                                                .or(competition.finalLoserGroupId.eq(group.id))
                                )
                )
                .join(group.members, groupMember)
                .join(groupMember.user, user)
                .join(user.summonerInfo, summonerInfo)
                .join(leaderMember).on(
                        leaderMember.group.id.eq(group.id)
                                .and(leaderMember.role.eq(GroupRole.LEADER))
                )
                .join(leaderMember.user, leaderUser)
                .where(
                        group.univName.eq(univName),
                        filteredByGroupNameKey(filter.groupNameKey()),
                        filteredByMajor(filter.major()),
                        filteredByAdmissionYear(filter.admissionYear()),
                        filteredByMainPosition(filter.mainPosition())
                )
                .groupBy(
                        group.id, group.name, group.logoImage, group.capacity,
                        leaderUser.id, leaderUser.summonerInfo.summonerIconNum
                )
                .orderBy(sortSpecifier, totalCount.desc(), group.createdAt.asc())
                .offset(page * PAGE_SIZE)
                .limit(PAGE_SIZE)
                .fetch();
    }


    private static Predicate filteredByGroupNameKey(String groupNameKey) {
        return groupNameKey != null ? group.name.like("%" + groupNameKey + "%") : null;
    }
}
