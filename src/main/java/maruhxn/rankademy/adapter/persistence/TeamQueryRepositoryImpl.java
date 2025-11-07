package maruhxn.rankademy.adapter.persistence;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.team.provided.dto.MyTeamPageResponse;
import maruhxn.rankademy.application.team.provided.dto.TeamDetailResponse;
import maruhxn.rankademy.application.team.provided.dto.TeamPageResponse;
import maruhxn.rankademy.application.team.required.TeamQueryRepository;
import maruhxn.rankademy.domain.group.QGroup;
import maruhxn.rankademy.domain.team.QTeam;
import maruhxn.rankademy.domain.team.QTeamMember;
import maruhxn.rankademy.domain.team.Team;
import maruhxn.rankademy.domain.user.QSummonerInfo;
import maruhxn.rankademy.domain.user.QUser;
import maruhxn.rankademy.domain.user.TierInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static maruhxn.rankademy.domain.group.QGroup.group;
import static maruhxn.rankademy.domain.team.QTeam.team;
import static maruhxn.rankademy.domain.team.QTeamMember.teamMember;
import static maruhxn.rankademy.domain.user.QSummonerInfo.summonerInfo;
import static maruhxn.rankademy.domain.user.QUser.user;

@Repository
@RequiredArgsConstructor
public class TeamQueryRepositoryImpl implements TeamQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public TeamPageResponse findAll(int page, List<Long> excludedTeamIds, int excludedCount) {
        final int pageSize = 10;
        long baseOffset = (long) page * pageSize;
        long baseLimit = pageSize;

        if (page == 0) {
            baseOffset = 0L;
            baseLimit = Math.max(pageSize - excludedCount, 0);
        } else if (excludedCount > 0) {
            baseOffset = Math.max(baseOffset - excludedCount, 0);
        }

        BooleanBuilder baseCondition = new BooleanBuilder(team.isActive.eq(true));
        if (excludedTeamIds != null && !excludedTeamIds.isEmpty()) {
            baseCondition.and(team.id.notIn(excludedTeamIds));
        }

        List<TeamPageResponse.TeamResponse> baseResults = baseLimit <= 0
                ? List.of()
                : queryFactory
                .select(
                        Projections.constructor(
                                TeamPageResponse.TeamResponse.class,
                                team.id,
                                team.name,
                                group.univName,
                                group.name,
                                team.intro,
                                team.createdAt,
                                summonerInfo.tierInfo.mappedTier.avg(),
                                Expressions.constant(false)
                        )
                )
                .from(team)
                .join(group).on(team.groupId.eq(group.id))
                .join(teamMember).on(team.id.eq(teamMember.team.id))
                .join(user).on(user.id.eq(teamMember.user.id))
                .join(summonerInfo).on(user.summonerInfo.id.eq(summonerInfo.id))
                .where(baseCondition)
                .groupBy(team.id)
                .orderBy(team.id.desc())
                .offset(baseOffset)
                .limit(baseLimit)
                .fetch();


        Long count = queryFactory
                .select(team.id.count())
                .from(team)
                .where(team.isActive.eq(true))
                .fetchOne();

        return new TeamPageResponse(count, baseResults);
    }

    @Override
    public List<TeamPageResponse.TeamResponse> findRecommendedTeams(Long leaderTeamId, int limit) {
        if (leaderTeamId == null || limit <= 0) {
            return List.of();
        }

        Double leaderAvgMmr = queryFactory
                .select(team.avgMmr)
                .from(team)
                .where(team.id.eq(leaderTeamId))
                .fetchOne();

        NumberExpression<Double> similarityScore = Expressions.numberTemplate(
                Double.class,
                "abs(coalesce({0}, 0) - {1})",
                team.avgMmr,
                leaderAvgMmr
        );

        return queryFactory
                .select(
                        Projections.constructor(
                                TeamPageResponse.TeamResponse.class,
                                team.id,
                                team.name,
                                group.univName,
                                group.name,
                                team.intro,
                                team.createdAt,
                                summonerInfo.tierInfo.mappedTier.avg(),
                                Expressions.constant(true)
                        )
                )
                .from(team)
                .join(group).on(team.groupId.eq(group.id))
                .join(teamMember).on(team.id.eq(teamMember.team.id))
                .join(user).on(user.id.eq(teamMember.user.id))
                .join(summonerInfo).on(user.summonerInfo.id.eq(summonerInfo.id))
                .where(team.isActive.eq(true).and(team.id.ne(leaderTeamId)))
                .groupBy(team.id)
                .orderBy(
                        new CaseBuilder().when(team.id.eq(leaderTeamId)).then(0).otherwise(1).asc(),
                        similarityScore.asc(),
                        team.id.desc()
                )
                .limit(limit)
                .fetch();
    }

    @Override
    public Optional<TeamDetailResponse> getDetailById(Long userId, Long teamId) {
        QTeamMember tm = QTeamMember.teamMember;
        QUser u = QUser.user;
        QSummonerInfo si = QSummonerInfo.summonerInfo;
        QGroup g = QGroup.group;
        QTeam t = QTeam.team;

        Tuple head = queryFactory
                .select(
                        t.id,
                        t.name,
                        g.univName,
                        g.name,
                        g.logoImage,
                        t.intro,
                        t.createdAt,
                        t.isActive,
                        t.representativeId
                )
                .from(t)
                .join(g).on(t.groupId.eq(g.id))
                .where(t.id.eq(teamId))
                .fetchOne();

        if (head == null) return Optional.empty();

        // 2) 멤버 목록
        List<TeamDetailResponse.TeamMemberResponse> members = queryFactory
                .select(Projections.constructor(
                        TeamDetailResponse.TeamMemberResponse.class,
                        u.id,
                        tm.position,
                        si.summonerName,
                        si.summonerTag,
                        si.summonerIcon,
                        u.univInfo.univName,
                        u.univInfo.major,
                        u.univInfo.admissionYear,
                        si.tierInfo
                ))
                .from(tm)
                .join(tm.user, u)
                .leftJoin(u.summonerInfo, si)
                .where(tm.team.id.eq(teamId))
                .orderBy(tm.position.asc(), u.id.asc())
                .fetch();

        double avgMappedTier = members.stream()
                .map(TeamDetailResponse.TeamMemberResponse::tierInfo)
                .filter(Objects::nonNull)
                .mapToInt(TierInfo::getMappedTier)
                .average()
                .orElse(0.0);

        Long representativeId = head.get(t.representativeId);
        boolean isTeamLeader = representativeId != null && representativeId.equals(userId);
        boolean isMyTeam = isTeamLeader || members.stream()
                .anyMatch(member -> Objects.equals(member.memberId(), userId));

        return Optional.of(new TeamDetailResponse(
                head.get(t.id),
                head.get(t.name),
                head.get(g.univName),
                head.get(g.name),
                head.get(g.logoImage),
                head.get(t.intro),
                head.get(t.createdAt),
                Boolean.TRUE.equals(head.get(t.isActive)),
                avgMappedTier,
                members,
                isTeamLeader,
                isMyTeam
        ));
    }

    @Override
    public Page<MyTeamPageResponse> findMyTeamList(Long userId, int page) {
        Pageable pageable = PageRequest.of(page, 3);

        Long total = queryFactory
                .select(team.count())
                .from(team)
                .join(group).on(team.groupId.eq(group.id))
                .where(team.isActive.eq(true))
                .fetchOne();

        if(total == null || total <= 0L) return new PageImpl<>(List.of(),  pageable, 0L);

        List<MyTeamPageResponse> result = queryFactory
                .select(
                        Projections.constructor(
                                MyTeamPageResponse.class,
                                team.id,
                                team.name,
                                group.id,
                                group.name
                        )
                )
                .from(team)
                .join(group).on(team.groupId.eq(group.id))
                .where(team.isActive.eq(true))
                .groupBy(team.id)
                .orderBy(team.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(result, pageable, total);
    }

    @Override
    public Boolean existsTeamLeaderByUserId(Long userId) {
        return queryFactory
                .selectOne()
                .from(team)
                .where(team.representativeId.eq(userId))
                .fetchFirst() != null;
    }

    @Override
    public Optional<Team> findMyLeaderTeamByUserId(Long userId) {
         return Optional.ofNullable(queryFactory
                 .selectFrom(team)
                 .where(team.representativeId.eq(userId))
                 .fetchOne());
    }
}
