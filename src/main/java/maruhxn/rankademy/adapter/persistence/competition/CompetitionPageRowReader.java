package maruhxn.rankademy.adapter.persistence.competition;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.domain.competition.CompetitionStatus;
import maruhxn.rankademy.domain.competition.QCompetition;
import maruhxn.rankademy.domain.group.QGroup;
import maruhxn.rankademy.domain.team.QTeam;
import maruhxn.rankademy.domain.team.QTeamMember;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CompetitionPageRowReader {

    private final JPAQueryFactory queryFactory;

    public record Row(Long cid, Long myTid, Long otherTid, String otherUniv,
                      LocalDateTime submittedAt, boolean isWin, CompetitionStatus status) {
    }

    public List<Row> fetchUserPageRows(Long userId, int page, int size) {
        QCompetition c = QCompetition.competition;
        QTeamMember tm1 = new QTeamMember("tm1");
        QTeamMember tm2 = new QTeamMember("tm2");
        QTeam t1 = new QTeam("t1");
        QTeam t2 = new QTeam("t2");
        QGroup g1 = new QGroup("g1");
        QGroup g2 = new QGroup("g2");

        var cb = new CaseBuilder();
        NumberExpression<Long> myTid = cb.when(tm1.id.isNotNull()).then(c.team1Id).otherwise(c.team2Id);
        NumberExpression<Long> otherTid = cb.when(tm1.id.isNotNull()).then(c.team2Id).otherwise(c.team1Id);
        StringExpression otherUniv = cb.when(tm1.id.isNotNull()).then(g2.name).otherwise(g1.name);
        DateTimeExpression<LocalDateTime> submittedAt = c.submittedAt;
        BooleanExpression isWin = c.finalWinnerTeamId.isNotNull().and(c.finalWinnerTeamId.eq(myTid));

        return queryFactory.select(Projections.constructor(Row.class,
                        c.id, myTid, otherTid, otherUniv, submittedAt, isWin, c.status))
                .from(c)
                .leftJoin(tm1).on(tm1.team.id.eq(c.team1Id).and(tm1.user.id.eq(userId)))
                .leftJoin(tm2).on(tm2.team.id.eq(c.team2Id).and(tm2.user.id.eq(userId)))
                .leftJoin(t1).on(t1.id.eq(c.team1Id)).leftJoin(g1).on(t1.groupId.eq(g1.id))
                .leftJoin(t2).on(t2.id.eq(c.team2Id)).leftJoin(g2).on(t2.groupId.eq(g2.id))
                .where(tm1.id.isNotNull().or(tm2.id.isNotNull()))
                .orderBy(submittedAt.desc().nullsFirst(), c.id.desc())
                .offset((long) page * size).limit(size)
                .fetch();
    }

    public List<Row> fetchGroupPageRows(Long groupId, int page, int size) {
        QCompetition c = QCompetition.competition;
        QTeam t1 = new QTeam("t1");
        QTeam t2 = new QTeam("t2");
        QGroup g1 = new QGroup("g1");
        QGroup g2 = new QGroup("g2");

        // "내 팀이 team1인가?"를 팀의 groupId로 판정
        BooleanExpression mineIsTeam1 = t1.groupId.eq(groupId);

        var cb = new CaseBuilder();
        NumberExpression<Long> myTid = cb.when(mineIsTeam1).then(c.team1Id).otherwise(c.team2Id);
        NumberExpression<Long> otherTid = cb.when(mineIsTeam1).then(c.team2Id).otherwise(c.team1Id);
        StringExpression otherUniv = cb.when(mineIsTeam1).then(g2.name).otherwise(g1.name);

        // submittedAt 컬럼이 없다면: COALESCE(c.submittedAt, (select max(sr.played_at) ...))로 교체
        DateTimeExpression<LocalDateTime> submittedAt = c.submittedAt;

        BooleanExpression isWin = c.finalWinnerTeamId.isNotNull().and(c.finalWinnerTeamId.eq(myTid));

        return queryFactory
                .select(Projections.constructor(Row.class,
                        c.id, myTid, otherTid, otherUniv, submittedAt, isWin, c.status))
                .from(c)
                .leftJoin(t1).on(t1.id.eq(c.team1Id))
                .leftJoin(t2).on(t2.id.eq(c.team2Id))
                .leftJoin(g1).on(g1.id.eq(t1.groupId))
                .leftJoin(g2).on(g2.id.eq(t2.groupId))
                .where(t1.groupId.eq(groupId).or(t2.groupId.eq(groupId)))
                .orderBy(submittedAt.desc().nullsFirst(), c.id.desc())
                .offset((long) page * size)
                .limit(size)
                .fetch();
    }
}
