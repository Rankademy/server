package maruhxn.rankademy.adapter.persistence;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.team.provided.dto.TeamDetailResponse;
import maruhxn.rankademy.application.team.provided.dto.TeamPageResponse;
import maruhxn.rankademy.application.team.required.TeamQueryRepository;
import maruhxn.rankademy.domain.group.QGroup;
import maruhxn.rankademy.domain.team.QTeam;
import maruhxn.rankademy.domain.team.QTeamMember;
import maruhxn.rankademy.domain.user.QSummonerInfo;
import maruhxn.rankademy.domain.user.QUser;
import maruhxn.rankademy.domain.user.TierInfo;
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
    public TeamPageResponse findAll(int page) {
        List<TeamPageResponse.TeamResponse> result = queryFactory
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
                .where(team.isActive.eq(true))
                .groupBy(team.id)
                .orderBy(team.id.desc())
                .offset(page * 10L)
                .limit(10)
                .fetch();


        Long count = queryFactory
                .select(team.id.count())
                .from(team)
                .where(team.isActive.eq(true))
                .fetchOne();

        return new TeamPageResponse(count, result);
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
                        si.summonerIconNum,
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
}
