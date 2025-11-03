package maruhxn.rankademy.adapter.persistence.ranking;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.adapter.webapi.dto.UnivStudentRankingResponse;
import maruhxn.rankademy.adapter.webapi.ranking.dto.UnivStudentRankingFilter;
import maruhxn.rankademy.application.group.provided.dto.GroupResponse;
import maruhxn.rankademy.application.group.provided.dto.LeaderDto;
import maruhxn.rankademy.domain.competition.CompetitionStatus;
import maruhxn.rankademy.domain.group.GroupRole;
import maruhxn.rankademy.domain.group.QGroupMember;
import maruhxn.rankademy.domain.user.QUser;
import maruhxn.rankademy.domain.user.SummonerInfo;
import maruhxn.rankademy.domain.user.User;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
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

    private final int PAGE_SIZE = 20;

    private final JPAQueryFactory queryFactory;

    public PagedModel<UnivStudentRankingResponse> getUnivStudentRanking(String univName, int page, UnivStudentRankingFilter univStudentRankingFilter) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);

        Long total = queryFactory.select(user.count())
                .from(user)
                .join(summonerInfo).on(user.summonerInfo.id.eq(summonerInfo.id))
                .where(
                        user.univInfo.univName.eq(univName),
                        filteredByMajor(univStudentRankingFilter.major()),
                        filteredByAdmissionYear(univStudentRankingFilter.admissionYear()),
                        filteredByMainPosition(univStudentRankingFilter.mainPosition())
                )
                .fetchOne();

        if(total == null || total <= 0) return new PagedModel<>(new PageImpl<>(List.of(), pageable, 0));

        List<User> users = queryFactory.selectFrom(user)
                .join(summonerInfo).on(user.summonerInfo.id.eq(summonerInfo.id))
                .where(
                        user.univInfo.univName.eq(univName),
                        filteredByMajor(univStudentRankingFilter.major()),
                        filteredByAdmissionYear(univStudentRankingFilter.admissionYear()),
                        filteredByMainPosition(univStudentRankingFilter.mainPosition()),
                        filteredByUserName(univStudentRankingFilter.userNameKey())
                )
                .orderBy(summonerInfo.tierInfo.mappedTier.desc(), summonerInfo.winCount.desc())
                .offset((long) page * PAGE_SIZE)
                .limit(PAGE_SIZE)
                .fetch();

        List<UnivStudentRankingResponse> content = users.stream()
                .map(u -> {
                    SummonerInfo s = u.getSummonerInfo();

                    return new UnivStudentRankingResponse(
                            u.getId(),
                            s.getPuuid(),
                            s.getSummonerName(),
                            s.getSummonerTag(),
                            s.getSummonerIcon(),
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

        return new PagedModel<>(new PageImpl<>(content, pageable, 0));
    }

    public PagedModel<GroupResponse> getGroupRanking(String univName, int page, String groupNameKey) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);

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

        Long total = queryFactory
                .select(group.count())
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
                        filteredByGroupNameKey(groupNameKey)
                )
                .fetchOne();

        if(total == null || total <= 0) return new PagedModel<>(new PageImpl<>(List.of(), pageable, 0));

        List<GroupResponse> content = queryFactory
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
                                        leaderUser.summonerInfo.summonerIcon
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
                        filteredByGroupNameKey(groupNameKey)
                )
                .groupBy(
                        group.id, group.name, group.logoImage, group.capacity,
                        leaderUser.id, leaderUser.summonerInfo.summonerIcon
                )
                .orderBy(winCount.desc(), totalCount.desc(), group.createdAt.asc())
                .offset((long) page * PAGE_SIZE)
                .limit(PAGE_SIZE)
                .fetch();

        return new PagedModel<>(new PageImpl<>(content, pageable, total));
    }


    private static Predicate filteredByGroupNameKey(String groupNameKey) {
        return groupNameKey != null ? group.name.like("%" + groupNameKey + "%") : null;
    }
}
