package maruhxn.rankademy.runner;

import maruhxn.rankademy.domain.user.LolPosition;
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
public class MockScrimTeamDataInsertRunner {

    private static final int TARGET_SCRIM_TEAM_COUNT = 180;
    private static final int BATCH_SIZE = 200;
    private static final List<LolPosition> REQUIRED_POSITIONS = List.of(
            LolPosition.TOP, LolPosition.JUNGLE, LolPosition.MIDDLE, LolPosition.BOTTOM, LolPosition.UTILITY
    );
    private static final List<String> THEMES = List.of(
            "Tempo Runners", "Vision Masters", "Macro Lab", "Late Game Scaling",
            "Dive Force", "Split Push Lab", "Objective Rush", "Control Squad"
    );
    private static final List<String> PRACTICE_NOTES = List.of(
            "주 3회 저녁 스크림, VOD 피드백 진행.",
            "실전 대비 2세트 + 리뷰 루틴 유지.",
            "포지션별 콜 체계 정립 목표.",
            "대회 대비 BO3 스크림 플로우 점검."
    );

    private final JdbcTemplate jdbcTemplate;

    public MockScrimTeamDataInsertRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void run() {
        long existing = existingScrimTeamCount();
        if (existing >= TARGET_SCRIM_TEAM_COUNT) {
            return;
        }

        Map<LolPosition, Deque<UserCandidate>> candidatePools = loadCandidatePools();
        int maxPossible = candidatePools.values().stream()
                .mapToInt(Deque::size)
                .min()
                .orElse(0);

        if (maxPossible == 0) {
            return;
        }

        int remainingQuota = (int) Math.max(0, TARGET_SCRIM_TEAM_COUNT - existing);
        int toCreate = Math.min(remainingQuota, maxPossible);
        if (toCreate <= 0) {
            return;
        }

        long nextScrimTeamId = nextId("scrim_teams");
        Random random = new Random(2025_11_08);
        List<MockScrimTeamRow> rows = new ArrayList<>(toCreate);

        for (int i = 0; i < toCreate; i++) {
            List<ScrimTeamMemberRow> members = new ArrayList<>(REQUIRED_POSITIONS.size());
            String representativeUniv = null;
            for (LolPosition position : REQUIRED_POSITIONS) {
                Deque<UserCandidate> pool = candidatePools.get(position);
                if (pool == null || pool.isEmpty()) {
                    return;
                }
                UserCandidate candidate = pool.pollFirst();
                if (candidate == null) {
                    return;
                }
                if (representativeUniv == null) {
                    representativeUniv = candidate.univName();
                }
                members.add(new ScrimTeamMemberRow(candidate.userId(), position));
            }
            long teamId = nextScrimTeamId + i;
            rows.add(buildScrimTeamRow(teamId, members, representativeUniv, random));
        }

        insertScrimTeams(rows);
        insertScrimTeamMembers(rows);
    }

    private void insertScrimTeams(List<MockScrimTeamRow> rows) {
        String sql = """
                INSERT INTO scrim_teams
                (id, name, intro, representative_id, created_at, is_active)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        JdbcBatchInsert<MockScrimTeamRow> batchInsert = JdbcBatchInsert.of(jdbcTemplate, sql, BATCH_SIZE);
        batchInsert.execute(rows, (PreparedStatement ps, MockScrimTeamRow row) -> {
            ps.setLong(1, row.id());
            ps.setString(2, row.name());
            ps.setString(3, row.intro());
            ps.setLong(4, row.representativeId());
            setTimestamp(ps, 5, row.createdAt());
            ps.setBoolean(6, row.active());
        });
    }

    private void insertScrimTeamMembers(List<MockScrimTeamRow> rows) {
        String sql = """
                INSERT INTO scrim_team_member
                (scrim_team_id, user_id, position)
                VALUES (?, ?, ?)
                """;

        List<ScrimTeamMemberInsertRow> memberRows = new ArrayList<>();
        for (MockScrimTeamRow row : rows) {
            for (ScrimTeamMemberRow member : row.members()) {
                memberRows.add(new ScrimTeamMemberInsertRow(row.id(), member.userId(), member.position()));
            }
        }

        JdbcBatchInsert<ScrimTeamMemberInsertRow> batchInsert = JdbcBatchInsert.of(jdbcTemplate, sql, BATCH_SIZE);
        batchInsert.execute(memberRows, (PreparedStatement ps, ScrimTeamMemberInsertRow row) -> {
            ps.setLong(1, row.scrimTeamId());
            ps.setLong(2, row.userId());
            ps.setString(3, row.position().name());
        });
    }

    private MockScrimTeamRow buildScrimTeamRow(long teamId, List<ScrimTeamMemberRow> members, String univ, Random random) {
        long representativeId = members.get(random.nextInt(members.size())).userId();
        String theme = THEMES.get(random.nextInt(THEMES.size()));
        String note = PRACTICE_NOTES.get(random.nextInt(PRACTICE_NOTES.size()));
        String name = "%s %s %03d".formatted(univ == null ? "Global" : univ.replaceAll("\\s+", ""), theme.replace(" ", ""), teamId);
        String intro = "%s\n대표: %d\n%s".formatted(theme, representativeId, note);
        LocalDateTime createdAt = LocalDateTime.now()
                .minusDays(random.nextInt(90))
                .minusHours(random.nextInt(24));
        boolean isActive = random.nextDouble() > 0.15;

        return new MockScrimTeamRow(
                teamId,
                name,
                intro,
                representativeId,
                createdAt,
                isActive,
                List.copyOf(members)
        );
    }

    private Map<LolPosition, Deque<UserCandidate>> loadCandidatePools() {
        String sql = """
                SELECT id, main_position, univ_name
                FROM users
                WHERE id BETWEEN 1 AND 2000
                  AND main_position IN ('TOP', 'JUNGLE', 'MIDDLE', 'BOTTOM', 'UTILITY')
                ORDER BY id
                """;
        Map<LolPosition, Deque<UserCandidate>> pools = new EnumMap<>(LolPosition.class);
        jdbcTemplate.query(sql, rs -> {
            LolPosition position = LolPosition.valueOf(rs.getString("main_position"));
            pools.computeIfAbsent(position, key -> new ArrayDeque<>())
                    .add(new UserCandidate(
                            rs.getLong("id"),
                            rs.getString("univ_name")
                    ));
        });

        Random random = new Random(2025_11_08);
        for (LolPosition position : REQUIRED_POSITIONS) {
            Deque<UserCandidate> deque = pools.get(position);
            if (deque == null) {
                pools.put(position, new ArrayDeque<>());
                continue;
            }
            List<UserCandidate> shuffled = new ArrayList<>(deque);
            Collections.shuffle(shuffled, random);
            pools.put(position, new ArrayDeque<>(shuffled));
        }
        return pools;
    }

    private long existingScrimTeamCount() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM scrim_teams", Long.class);
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

    private record UserCandidate(long userId, String univName) {
    }

    private record ScrimTeamMemberRow(long userId, LolPosition position) {
    }

    private record ScrimTeamMemberInsertRow(long scrimTeamId, long userId, LolPosition position) {
    }

    private record MockScrimTeamRow(
            long id,
            String name,
            String intro,
            long representativeId,
            LocalDateTime createdAt,
            boolean active,
            List<ScrimTeamMemberRow> members
    ) {
    }
}
