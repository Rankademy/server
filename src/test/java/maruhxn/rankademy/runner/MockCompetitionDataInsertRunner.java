package maruhxn.rankademy.runner;

import maruhxn.rankademy.domain.competition.CompetitionStatus;
import maruhxn.rankademy.support.jdbc.JdbcBatchInsert;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class MockCompetitionDataInsertRunner {

    private static final int TARGET_COMPETITION_COUNT = 150;
    private static final int BATCH_SIZE = 200;

    private final JdbcTemplate jdbcTemplate;

    public MockCompetitionDataInsertRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void run() {
        long existing = existingCompetitionCount();
        if (existing >= TARGET_COMPETITION_COUNT) {
            return;
        }

        List<TeamInfo> teams = loadTeamInfos();
        if (teams.size() < 2) {
            return;
        }

        int remainingQuota = (int) Math.max(0, TARGET_COMPETITION_COUNT - existing);
        if (remainingQuota <= 0) {
            return;
        }

        long nextCompetitionId = nextId("competitions");
        Random random = new Random(2025_11_08);
        List<MockCompetitionRow> rows = generateCompetitions(teams, remainingQuota, nextCompetitionId, random);
        if (rows.isEmpty()) {
            return;
        }

        insertCompetitions(rows);
        insertSetResults(rows);
    }

    private void insertCompetitions(List<MockCompetitionRow> rows) {
        String sql = """
                INSERT INTO competitions
                (id, team1id, team2id, status, memo, total_sets, final_winner_team_id, final_winner_group_id,
                 final_loser_group_id, scheduled_at, submitted_at, expired_at, opposed_reason)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        JdbcBatchInsert<MockCompetitionRow> batchInsert = JdbcBatchInsert.of(jdbcTemplate, sql, BATCH_SIZE);
        batchInsert.execute(rows, (PreparedStatement ps, MockCompetitionRow row) -> {
            ps.setLong(1, row.id());
            ps.setLong(2, row.team1Id());
            ps.setLong(3, row.team2Id());
            ps.setString(4, row.status().name());
            if (row.memo() == null) {
                ps.setNull(5, Types.VARCHAR);
            } else {
                ps.setString(5, row.memo());
            }
            ps.setInt(6, row.totalSets());
            setLong(ps, 7, row.finalWinnerTeamId());
            setLong(ps, 8, row.finalWinnerGroupId());
            setLong(ps, 9, row.finalLoserGroupId());
            setTimestamp(ps, 10, row.scheduledAt());
            setTimestamp(ps, 11, row.submittedAt());
            setTimestamp(ps, 12, row.expiredAt());
            if (row.opposedReason() == null) {
                ps.setNull(13, Types.VARCHAR);
            } else {
                ps.setString(13, row.opposedReason());
            }
        });
    }

    private void insertSetResults(List<MockCompetitionRow> rows) {
        String sql = """
                INSERT INTO set_result
                (competition_id, set_number, winner_team_id, result_image_key)
                VALUES (?, ?, ?, ?)
                """;

        List<SetResultRow> setRows = rows.stream()
                .map(MockCompetitionRow::setResults)
                .flatMap(Collection::stream)
                .toList();

        if (setRows.isEmpty()) {
            return;
        }

        JdbcBatchInsert<SetResultRow> batchInsert = JdbcBatchInsert.of(jdbcTemplate, sql, BATCH_SIZE);
        batchInsert.execute(setRows, (PreparedStatement ps, SetResultRow row) -> {
            ps.setLong(1, row.competitionId());
            ps.setInt(2, row.setNumber());
            ps.setLong(3, row.winnerTeamId());
            ps.setString(4, row.resultImageKey());
        });
    }

    private List<MockCompetitionRow> generateCompetitions(List<TeamInfo> teams, int toCreate, long nextId, Random random) {
        List<MockCompetitionRow> rows = new ArrayList<>(toCreate);
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < toCreate; i++) {
            TeamInfo team1 = teams.get(random.nextInt(teams.size()));
            TeamInfo team2 = team1;
            while (team2.id() == team1.id()) {
                team2 = teams.get(random.nextInt(teams.size()));
            }

            double roll = random.nextDouble();
            long competitionId = nextId + i;
            if (roll < 0.6) {
                rows.add(buildCompletedCompetition(competitionId, team1, team2, now, random, CompetitionStatus.COMPLETED));
            } else if (roll < 0.9) {
                rows.add(buildScheduledCompetition(competitionId, team1, team2, now, random));
            } else {
                rows.add(buildCompletedCompetition(competitionId, team1, team2, now, random, CompetitionStatus.OPPOSED));
            }
        }
        return rows;
    }

    private MockCompetitionRow buildScheduledCompetition(long competitionId, TeamInfo team1, TeamInfo team2, LocalDateTime now, Random random) {
        LocalDateTime scheduledAt = now.plusDays(random.nextInt(15) + 1).withHour(20);
        LocalDateTime expiredAt = scheduledAt.plusDays(7);
        String memo = "예정된 친선전 - 맵 풀 점검";

        return new MockCompetitionRow(
                competitionId,
                team1.id(),
                team2.id(),
                CompetitionStatus.SCHEDULED,
                memo,
                0,
                null,
                null,
                null,
                scheduledAt,
                null,
                expiredAt,
                null,
                List.of()
        );
    }

    private MockCompetitionRow buildCompletedCompetition(long competitionId, TeamInfo team1, TeamInfo team2, LocalDateTime now, Random random, CompetitionStatus status) {
        LocalDateTime scheduledAt = now.minusDays(random.nextInt(40) + 5).withHour(19);
        LocalDateTime submittedAt = scheduledAt.plusHours(5 + random.nextInt(24));
        LocalDateTime expiredAt = scheduledAt.plusDays(7);

        int totalSets = 3 + random.nextInt(3); // 3 to 5
        boolean team1Wins = random.nextBoolean();
        TeamInfo winner = team1Wins ? team1 : team2;
        TeamInfo loser = team1Wins ? team2 : team1;

        int winnerWins = totalSets / 2 + 1;
        int loserWins = totalSets - winnerWins;
        List<Long> winners = new ArrayList<>(totalSets);
        for (int i = 0; i < winnerWins; i++) {
            winners.add(winner.id());
        }
        for (int i = 0; i < loserWins; i++) {
            winners.add(loser.id());
        }
        Collections.shuffle(winners, random);

        List<SetResultRow> setRows = new ArrayList<>(totalSets);
        for (int setNumber = 1; setNumber <= totalSets; setNumber++) {
            Long setWinnerId = winners.get(setNumber - 1);
            String imageKey = "mock/competitions/%04d/set-%d.png".formatted(competitionId, setNumber);
            setRows.add(new SetResultRow(competitionId, setNumber, setWinnerId, imageKey));
        }

        String memo = "BO" + totalSets + " 경기. 주요 전략 검증 완료.";
        String opposedReason = status == CompetitionStatus.OPPOSED
                ? "세트 스코어 입력 오류에 대한 이의 제기"
                : null;

        return new MockCompetitionRow(
                competitionId,
                team1.id(),
                team2.id(),
                status,
                memo,
                totalSets,
                winner.id(),
                winner.groupId(),
                loser.groupId(),
                scheduledAt,
                submittedAt,
                expiredAt,
                opposedReason,
                setRows
        );
    }

    private List<TeamInfo> loadTeamInfos() {
        String sql = """
                SELECT id, group_id
                FROM teams
                WHERE group_id IS NOT NULL
                ORDER BY id
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new TeamInfo(
                rs.getLong("id"),
                rs.getLong("group_id")
        ));
    }

    private long existingCompetitionCount() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM competitions", Long.class);
        return count == null ? 0L : count;
    }

    private long nextId(String table) {
        Long max = jdbcTemplate.queryForObject("SELECT COALESCE(MAX(id), 0) FROM " + table, Long.class);
        return (max == null ? 0L : max) + 1;
    }

    private static void setTimestamp(PreparedStatement ps, int index, LocalDateTime value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(index, Timestamp.valueOf(value));
        }
    }

    private static void setLong(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.BIGINT);
        } else {
            ps.setLong(index, value);
        }
    }

    private record TeamInfo(long id, long groupId) {
    }

    private record SetResultRow(long competitionId, int setNumber, long winnerTeamId, String resultImageKey) {
    }

    private record MockCompetitionRow(
            long id,
            long team1Id,
            long team2Id,
            CompetitionStatus status,
            String memo,
            int totalSets,
            Long finalWinnerTeamId,
            Long finalWinnerGroupId,
            Long finalLoserGroupId,
            LocalDateTime scheduledAt,
            LocalDateTime submittedAt,
            LocalDateTime expiredAt,
            String opposedReason,
            List<SetResultRow> setResults
    ) {
    }
}
