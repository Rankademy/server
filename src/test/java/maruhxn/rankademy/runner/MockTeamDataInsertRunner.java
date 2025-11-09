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
public class MockTeamDataInsertRunner {

    private static final int TARGET_TEAM_COUNT = 180;
    private static final int TEAM_SIZE = 5;
    private static final int BATCH_SIZE = 200;
    private static final List<LolPosition> REQUIRED_POSITIONS = List.of(
            LolPosition.TOP, LolPosition.JUNGLE, LolPosition.MIDDLE, LolPosition.BOTTOM, LolPosition.UTILITY
    );
    private static final String TEAM_TABLE = "teams";
    private static final String TEAM_MEMBER_TABLE = "team_member";

    private final JdbcTemplate jdbcTemplate;

    public MockTeamDataInsertRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void run() {
        long existingTeams = existingTeamCount();
        if (existingTeams >= TARGET_TEAM_COUNT) {
            return;
        }

        Map<Long, Integer> userTierScores = loadUserTierScores();
        List<GroupRoster> rosters = loadGroupRosters();
        if (rosters.isEmpty()) {
            return;
        }

        List<TeamSeed> seeds = buildTeamSeeds(rosters);
        if (seeds.isEmpty()) {
            return;
        }

        Collections.shuffle(seeds, new Random(2025_11_08));
        int remainingQuota = (int) Math.max(0, TARGET_TEAM_COUNT - existingTeams);
        int toCreate = Math.min(remainingQuota, seeds.size());
        if (toCreate <= 0) {
            return;
        }

        long nextTeamId = nextId(TEAM_TABLE);
        Random random = new Random(2025_11_08);
        List<MockTeamRow> mockTeams = new ArrayList<>(toCreate);
        for (int i = 0; i < toCreate; i++) {
            mockTeams.add(buildTeamRow(seeds.get(i), nextTeamId + i, random, userTierScores));
        }

        insertTeams(mockTeams);
        insertTeamMembers(mockTeams);
    }

    private void insertTeams(List<MockTeamRow> rows) {
        String sql = """
                INSERT INTO teams
                (id, group_id, name, intro, representative_id, created_at, is_active, avg_mmr)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        JdbcBatchInsert<MockTeamRow> batchInsert = JdbcBatchInsert.of(jdbcTemplate, sql, BATCH_SIZE);
        batchInsert.execute(rows, (PreparedStatement ps, MockTeamRow row) -> {
            ps.setLong(1, row.teamId());
            ps.setLong(2, row.groupId());
            ps.setString(3, row.name());
            ps.setString(4, row.intro());
            ps.setLong(5, row.representativeId());
            setTimestamp(ps, 6, row.createdAt());
            ps.setBoolean(7, row.active());
            ps.setDouble(8, row.avgMmr());
        });
    }

    private void insertTeamMembers(List<MockTeamRow> rows) {
        String sql = """
                INSERT INTO team_member
                (team_id, user_id, position)
                VALUES (?, ?, ?)
                """;

        List<TeamMemberRow> members = rows.stream()
                .map(MockTeamRow::members)
                .flatMap(Collection::stream)
                .toList();

        JdbcBatchInsert<TeamMemberRow> batchInsert = JdbcBatchInsert.of(jdbcTemplate, sql, BATCH_SIZE);
        batchInsert.execute(members, (PreparedStatement ps, TeamMemberRow row) -> {
            ps.setLong(1, row.teamId());
            ps.setLong(2, row.userId());
            ps.setString(3, row.position().name());
        });
    }

    private List<TeamSeed> buildTeamSeeds(List<GroupRoster> rosters) {
        List<TeamSeed> seeds = new ArrayList<>();
        Random random = new Random(2025_11_08);
        for (GroupRoster roster : rosters) {
            List<Long> shuffled = new ArrayList<>(roster.memberIds());
            Collections.shuffle(shuffled, random);
            for (int idx = 0; idx + TEAM_SIZE <= shuffled.size(); idx += TEAM_SIZE) {
                List<Long> slice = new ArrayList<>(shuffled.subList(idx, idx + TEAM_SIZE));
                seeds.add(new TeamSeed(roster, slice));
            }
        }
        return seeds;
    }

    private MockTeamRow buildTeamRow(TeamSeed seed, long teamId, Random random, Map<Long, Integer> userTierScores) {
        LocalDateTime createdAt = LocalDateTime.now()
                .minusDays(random.nextInt(120))
                .minusHours(random.nextInt(24));
        List<LolPosition> positions = rotatedPositions(random);

        List<TeamMemberRow> memberRows = new ArrayList<>(TEAM_SIZE);
        for (int i = 0; i < TEAM_SIZE; i++) {
            long userId = seed.members().get(i);
            LolPosition position = positions.get(i);
            memberRows.add(new TeamMemberRow(teamId, userId, position));
        }

        double avgMmr = seed.members().stream()
                .mapToDouble(id -> userTierScores.getOrDefault(id, 600))
                .average()
                .orElse(600.0);

        String label = switch (random.nextInt(4)) {
            case 0 -> "Squad";
            case 1 -> "Unit";
            case 2 -> "Five";
            default -> "Roster";
        };

        String teamName = "%s %s %03d".formatted(seed.roster().groupName().replaceAll("\\s+", ""), label, teamId);
        String intro = "%s 기반 팀. 목표: 교내 상위 %d%%.".
                formatted(seed.roster().univName(), 10 + random.nextInt(40));
        boolean isActive = random.nextDouble() > 0.1;

        return new MockTeamRow(
                teamId,
                seed.roster().groupId(),
                teamName,
                intro,
                seed.members().get(0),
                createdAt,
                isActive,
                avgMmr,
                memberRows
        );
    }

    private List<LolPosition> rotatedPositions(Random random) {
        List<LolPosition> positions = new ArrayList<>(REQUIRED_POSITIONS);
        Collections.rotate(positions, random.nextInt(positions.size()));
        return positions;
    }

    private Map<Long, Integer> loadUserTierScores() {
        String sql = """
                SELECT u.id AS user_id, si.mapped_tier
                FROM users u
                JOIN summoner_info si ON si.id = u.summoner_info_id
                WHERE u.id BETWEEN 1 AND 2000
                """;
        Map<Long, Integer> scores = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            scores.put(rs.getLong("user_id"), rs.getInt("mapped_tier"));
        });
        return scores;
    }

    private List<GroupRoster> loadGroupRosters() {
        String sql = """
                SELECT g.id          AS group_id,
                       g.name        AS group_name,
                       g.univ_name   AS univ_name,
                       gm.user_id    AS user_id
                FROM `groups` g
                JOIN group_member gm ON gm.group_id = g.id
                ORDER BY g.id, gm.id
                """;
        Map<Long, GroupRosterBuilder> builders = new LinkedHashMap<>();
        jdbcTemplate.query(sql, rs -> {
            long groupId = rs.getLong("group_id");
            GroupRosterBuilder builder = builders.computeIfAbsent(groupId, id -> {
                try {
                    return new GroupRosterBuilder(
                            groupId,
                            rs.getString("group_name"),
                            rs.getString("univ_name")
                    );
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            builder.memberIds.add(rs.getLong("user_id"));
        });
        return builders.values().stream()
                .map(builder -> new GroupRoster(
                        builder.groupId,
                        builder.groupName,
                        builder.univName,
                        List.copyOf(builder.memberIds)
                ))
                .toList();
    }

    private long nextId(String tableName) {
        Long max = jdbcTemplate.queryForObject("SELECT COALESCE(MAX(id), 0) FROM " + tableName, Long.class);
        return (max == null ? 0L : max) + 1;
    }

    private long existingTeamCount() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + TEAM_TABLE, Long.class);
        return count == null ? 0L : count;
    }

    private static void setTimestamp(PreparedStatement ps, int index, LocalDateTime value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(index, Timestamp.valueOf(value));
        }
    }

    private static final class GroupRosterBuilder {
        private final long groupId;
        private final String groupName;
        private final String univName;
        private final List<Long> memberIds = new ArrayList<>();

        private GroupRosterBuilder(long groupId, String groupName, String univName) {
            this.groupId = groupId;
            this.groupName = groupName;
            this.univName = univName;
        }
    }

    private record GroupRoster(long groupId, String groupName, String univName, List<Long> memberIds) {
    }

    private record TeamSeed(GroupRoster roster, List<Long> members) {
    }

    private record TeamMemberRow(long teamId, long userId, LolPosition position) {
    }

    private record MockTeamRow(
            long teamId,
            long groupId,
            String name,
            String intro,
            long representativeId,
            LocalDateTime createdAt,
            boolean active,
            double avgMmr,
            List<TeamMemberRow> members
    ) {
    }
}
