package maruhxn.rankademy.adapter.persistence;

import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maruhxn.rankademy.adapter.persistence.competition.CompetitionAssembler;
import maruhxn.rankademy.adapter.persistence.competition.CompetitionCountReader;
import maruhxn.rankademy.adapter.persistence.competition.CompetitionPageRowReader;
import maruhxn.rankademy.adapter.persistence.competition.TeamAndSetLoader;
import maruhxn.rankademy.application.competition.provided.dto.CompetitionPageResponse;
import maruhxn.rankademy.application.competition.provided.dto.CompetitionResultResponse;
import maruhxn.rankademy.application.competition.required.CompetitionQueryRepository;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static maruhxn.rankademy.domain.competition.QCompetition.competition;
import static maruhxn.rankademy.domain.competition.QSetResult.setResult;
import static maruhxn.rankademy.domain.group.QGroup.group;
import static maruhxn.rankademy.domain.team.QTeam.team;
import static maruhxn.rankademy.domain.team.QTeamMember.teamMember;
import static maruhxn.rankademy.domain.user.QSummonerInfo.summonerInfo;
import static maruhxn.rankademy.domain.user.QUser.user;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CompetitionQueryRepositoryImpl implements CompetitionQueryRepository {

    private final JPAQueryFactory queryFactory;

    private final CompetitionCountReader countReader;
    private final CompetitionPageRowReader rowReader;
    private final TeamAndSetLoader loader;
    private final CompetitionAssembler assembler;

    @Override
    public CompetitionResultResponse getResult(Long competitionId) {
        // Q1: 헤더(팀 ID 두 개 + 기타 필요시)
        var head = getHead(competitionId);

        // Q2: 두 팀 정보 + 멤버 리스트를 한 번에 로드 → GroupBy 변환
        List<Long> teamIds = List.of(head.team1Id, head.team2Id);

        Map<Long, CompetitionResultResponse.TeamInfoResponse> teamInfoMap = getTeamInfoMap(teamIds);
        var team1Info = teamInfoMap.get(head.team1Id);
        var team2Info = teamInfoMap.get(head.team2Id);

        if (team1Info == null)
            throw new NoSuchElementException("팀 정보를 조회할 수 없습니다. teamInfo: " + head.team1Id);
        if (team2Info == null)
            throw new NoSuchElementException("팀 정보를 조회할 수 없습니다. teamInfo: " + head.team2Id);

        // Q3: 세트 결과(정렬 포함)
        List<CompetitionResultResponse.SetResultResponse> setResults = getSetResults(competitionId);

        return new CompetitionResultResponse(
                head.competitionId,
                team1Info,
                team2Info,
                setResults,
                head.finalWinnerTeamId
        );
    }

    private List<CompetitionResultResponse.SetResultResponse> getSetResults(Long competitionId) {
        return queryFactory
                .select(Projections.constructor(
                        CompetitionResultResponse.SetResultResponse.class,
                        setResult.setNumber,
                        setResult.winnerTeamId
                ))
                .from(competition)
                .join(competition.setResults, setResult)
                .where(competition.id.eq(competitionId))
                .orderBy(setResult.setNumber.asc())
                .fetch();
    }

    private Map<Long, CompetitionResultResponse.TeamInfoResponse> getTeamInfoMap(List<Long> teamIds) {
        return queryFactory
                .from(team)
                .join(group).on(team.groupId.eq(group.id))
                .join(teamMember).on(teamMember.team.id.eq(team.id))
                .join(user).on(teamMember.user.id.eq(user.id))
                .join(summonerInfo).on(summonerInfo.id.eq(user.summonerInfo.id))
                .where(team.id.in(teamIds))
                .transform(
                        GroupBy.groupBy(team.id).as(
                                Projections.constructor(
                                        CompetitionResultResponse.TeamInfoResponse.class,
                                        team.id,
                                        team.name,
                                        group.name,
                                        GroupBy.list(
                                                Projections.constructor(
                                                        CompetitionResultResponse.TeamInfoResponse.TeamMemberResponse.class,
                                                        user.id,
                                                        teamMember.position,
                                                        summonerInfo.summonerName,
                                                        summonerInfo.summonerTag
                                                )
                                        )
                                )
                        )
                );
    }

    private Head getHead(Long competitionId) {
        var head = queryFactory
                .select(Projections.constructor(
                        Head.class,
                        competition.id,
                        competition.team1Id,
                        competition.team2Id,
                        competition.finalWinnerTeamId
                ))
                .from(competition)
                .where(competition.id.eq(competitionId))
                .fetchOne();
        if (head == null)
            throw new NoSuchElementException("대항전 정보를 찾을 수 없습니다. competitionId: " + competitionId);
        return head;
    }

    // 내부 전용 DTO
    public record Head(Long competitionId, Long team1Id, Long team2Id, Long finalWinnerTeamId) {
    }

    @Override
    public PagedModel<CompetitionPageResponse> getMyCompetitionHistory(Long userId, int page) {
        Pageable pageable = PageRequest.of(page, 10);

        long total = countReader.countForUser(userId);
        if (total == 0) return new PagedModel<>(new PageImpl<>(List.of(), pageable, total));

        var rows = rowReader.fetchUserPageRows(userId, page, 10);
        if (rows.isEmpty()) return new PagedModel<>(new PageImpl<>(List.of(), pageable, total));

        return getCompetitionPageResponsePagedModel(pageable, total, rows);
    }

    @Override
    public PagedModel<CompetitionPageResponse> getGroupCompetitionHistory(Long groupId, int page) {
        Pageable pageable = PageRequest.of(page, 10);

        long total = countReader.countForGroup(groupId);
        if (total == 0) return new PagedModel<>(new PageImpl<>(List.of(), pageable, total));

        var rows = rowReader.fetchGroupPageRows(groupId, page, 10);
        if (rows.isEmpty()) new PagedModel<>(new PageImpl<>(List.of(), pageable, total));

        return getCompetitionPageResponsePagedModel(pageable, total, rows);
    }

    private PagedModel<CompetitionPageResponse> getCompetitionPageResponsePagedModel(Pageable pageable, long total, List<CompetitionPageRowReader.Row> rows) {
        var compIds = rows.stream().map(CompetitionPageRowReader.Row::cid).toList();
        var teamIds = rows.stream()
                .flatMap(r -> Stream.of(r.myTid(), r.otherTid()))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        var teamInfo = loader.loadTeams(teamIds);
        var setMap = loader.loadSetResults(compIds);

        List<CompetitionPageResponse> content = assembler.assemble(rows, teamInfo, setMap);

        return new PagedModel<>(new PageImpl<>(content, pageable, total));
    }
}
