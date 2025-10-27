package maruhxn.rankademy.adapter.persistence;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.scrim_team.provided.dto.ScrimTeamDetailResponse;
import maruhxn.rankademy.application.scrim_team.provided.dto.ScrimTeamPageResponse;
import maruhxn.rankademy.application.scrim_team.required.ScrimTeamQueryRepository;
import maruhxn.rankademy.domain.scrim_team.QScrimTeam;
import maruhxn.rankademy.domain.scrim_team.QScrimTeamMember;
import maruhxn.rankademy.domain.user.QSummonerInfo;
import maruhxn.rankademy.domain.user.QUser;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static maruhxn.rankademy.domain.scrim_team.QScrimTeam.scrimTeam;
import static maruhxn.rankademy.domain.scrim_team.QScrimTeamMember.scrimTeamMember;
import static maruhxn.rankademy.domain.user.QSummonerInfo.summonerInfo;
import static maruhxn.rankademy.domain.user.QUser.user;

@Repository
@RequiredArgsConstructor
public class ScrimTeamQueryRepositoryImpl implements ScrimTeamQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public ScrimTeamPageResponse findAll(int page) {
        Long count = queryFactory.select(scrimTeam.id.count())
                .from(scrimTeam)
                .where(scrimTeam.isActive.eq(true))
                .fetchOne();

        if (count == null || count == 0L) return new ScrimTeamPageResponse(0L, List.of());

        List<ScrimTeamPageResponse.ScrimTeamResponse> result = queryFactory
                .select(
                        Projections.constructor(
                                ScrimTeamPageResponse.ScrimTeamResponse.class,
                                scrimTeam.id,
                                scrimTeam.name,
                                scrimTeam.intro,
                                scrimTeam.createdAt,
                                summonerInfo.tierInfo.mappedTier.avg(),
                                Expressions.constant(false)
                        )
                )
                .from(scrimTeam)
                .join(scrimTeamMember).on(scrimTeamMember.scrimTeam.id.eq(scrimTeam.id))
                .join(user).on(user.id.eq(scrimTeamMember.user.id))
                .join(summonerInfo).on(summonerInfo.id.eq(user.summonerInfo.id))
                .where(scrimTeam.isActive.eq(true))
                .groupBy(scrimTeam.id)
                .orderBy(scrimTeam.id.desc())
                .offset(page * 10L)
                .limit(10L)
                .fetch();

        return new ScrimTeamPageResponse(count, result);
    }

    @Override
    public Optional<ScrimTeamDetailResponse> getDetailById(Long scrimTeamId) {
        // 별칭 준비
        QScrimTeamMember stm = QScrimTeamMember.scrimTeamMember;
        QUser u = QUser.user;
        QSummonerInfo si = QSummonerInfo.summonerInfo;
        QScrimTeam st = QScrimTeam.scrimTeam;

        // 1) 팀 헤더 + 평균티어(수치) 서브쿼리
        SubQueryExpression<Double> avgMappedTierSubq =
                JPAExpressions
                        .select(si.tierInfo.mappedTier.avg().coalesce(0.0))  // 멤버 0명일 때 가드
                        .from(stm)
                        .join(stm.user, u)
                        .leftJoin(u.summonerInfo, si)
                        .where(stm.scrimTeam.id.eq(st.id));

        ScrimTeamDetailResponse head = queryFactory
                .select(Projections.constructor(
                        ScrimTeamDetailResponse.class,
                        st.id,
                        st.name,
                        st.intro,
                        st.createdAt,
                        st.isActive,
                        avgMappedTierSubq,
                        Expressions.constant(List.of())
                ))
                .from(st)
                .where(st.id.eq(scrimTeamId))
                .fetchOne();

        if (head == null) return Optional.empty();

        // 2) 멤버 목록
        List<ScrimTeamDetailResponse.ScrimTeamMemberResponse> members = queryFactory
                .select(Projections.constructor(
                        ScrimTeamDetailResponse.ScrimTeamMemberResponse.class,
                        u.id,
                        stm.position,
                        si.summonerName,
                        si.summonerTag,
                        si.summonerIcon,
                        u.univInfo.univName,
                        si.tierInfo
                ))
                .from(stm)
                .join(stm.user, u)
                .leftJoin(u.summonerInfo, si)
                .where(stm.scrimTeam.id.eq(scrimTeamId))
                .orderBy(stm.position.asc(), u.id.asc())
                .fetch();

        // 3) 최종 합성
        return Optional.of(new ScrimTeamDetailResponse(
                head.scrimTeamId(),
                head.scrimTeamName(),
                head.intro(),
                head.createdAt(),
                head.isActive(),
                head.avgTierInfo(),
                members
        ));
    }
}
