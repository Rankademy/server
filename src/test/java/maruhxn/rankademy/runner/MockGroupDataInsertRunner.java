package maruhxn.rankademy.runner;

import maruhxn.rankademy.domain.group.GroupRole;
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
public class MockGroupDataInsertRunner {

    private static final int TARGET_GROUP_COUNT = 200;
    private static final int MIN_MEMBERS_PER_GROUP = 6;
    private static final int MAX_MEMBERS_PER_GROUP = 12;
    private static final int BATCH_SIZE = 200;
    private static final String GROUP_TABLE = "`groups`";
    private static final String RECRUITMENT_POST_TABLE = "group_recruitment_post";
    private static final List<String> GROUP_THEMES = List.of(
            "Aggressive Macro", "Clash Prep", "Solo Queue Study", "Pick & Ban Lab",
            "Objective Control", "Vision Control", "Jungle Tracking", "Dive Practice"
    );
    private static final List<String> REQUIREMENTS = List.of(
            "Mic on Discord mandatory\nAvailable 3 evenings per week",
            "Comfortable sharing POV vods\nPositive attitude only",
            "Able to scrim after 9 PM (KST)\nShotcalling interest preferred",
            "Flexible role swaps welcome\nNeed consistent practice attendance"
    );

    private final JdbcTemplate jdbcTemplate;

    public MockGroupDataInsertRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void run() {
        long existingGroups = existingGroupCount();
        if (existingGroups >= TARGET_GROUP_COUNT) {
            return;
        }

        List<UserSnapshot> users = loadUsers();
        if (users.size() < MIN_MEMBERS_PER_GROUP) {
            throw new IllegalStateException("Not enough users between ID 1 and 2000 to generate groups.");
        }

        int maxPossibleGroups = users.size() / MIN_MEMBERS_PER_GROUP;
        int remainingQuota = (int) Math.max(0, TARGET_GROUP_COUNT - existingGroups);
        int toCreate = Math.min(remainingQuota, maxPossibleGroups);
        if (toCreate <= 0) {
            return;
        }

        long nextGroupId = nextId(GROUP_TABLE);
        long nextRecruitmentPostId = nextId(RECRUITMENT_POST_TABLE);

        List<MockGroupRow> rows = generateGroupRows(users, toCreate, nextGroupId, nextRecruitmentPostId);
        if (rows.isEmpty()) {
            return;
        }

        insertRecruitmentPosts(rows);
        insertGroups(rows);
        insertGroupMembers(rows);
    }

    private void insertRecruitmentPosts(List<MockGroupRow> rows) {
        String sql = """
                INSERT INTO group_recruitment_post
                (id, title, content, requirements, last_upped_at, created_at, is_active)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        List<GroupRecruitmentPostRow> posts = rows.stream()
                .map(MockGroupRow::recruitmentPost)
                .toList();

        JdbcBatchInsert<GroupRecruitmentPostRow> batchInsert = JdbcBatchInsert.of(jdbcTemplate, sql, BATCH_SIZE);
        batchInsert.execute(posts, (PreparedStatement ps, GroupRecruitmentPostRow row) -> {
            ps.setLong(1, row.id());
            ps.setString(2, row.title());
            ps.setString(3, row.content());
            ps.setString(4, row.requirements());
            setTimestamp(ps, 5, row.lastUppedAt());
            setTimestamp(ps, 6, row.createdAt());
            ps.setBoolean(7, row.active());
        });
    }

    private void insertGroups(List<MockGroupRow> rows) {
        String sql = """
                INSERT INTO `groups`
                (id, name, about, logo_image, univ_name, capacity, is_recruiting, leader_id, recruitment_post_id, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        JdbcBatchInsert<MockGroupRow> batchInsert = JdbcBatchInsert.of(jdbcTemplate, sql, BATCH_SIZE);
        batchInsert.execute(rows, (PreparedStatement ps, MockGroupRow row) -> {
            ps.setLong(1, row.groupId());
            ps.setString(2, row.name());
            ps.setString(3, row.about());
            if (row.logoImage() == null) {
                ps.setNull(4, Types.VARCHAR);
            } else {
                ps.setString(4, row.logoImage());
            }
            ps.setString(5, row.univName());
            ps.setInt(6, row.capacity());
            ps.setBoolean(7, row.recruiting());
            ps.setLong(8, row.leaderUserId());
            ps.setLong(9, row.recruitmentPost().id());
            setTimestamp(ps, 10, row.createdAt());
        });
    }

    private void insertGroupMembers(List<MockGroupRow> rows) {
        String sql = """
                INSERT INTO group_member
                (group_id, user_id, role)
                VALUES (?, ?, ?)
                """;

        List<GroupMemberRow> members = rows.stream()
                .map(MockGroupRow::members)
                .flatMap(Collection::stream)
                .toList();

        JdbcBatchInsert<GroupMemberRow> batchInsert = JdbcBatchInsert.of(jdbcTemplate, sql, BATCH_SIZE);
        batchInsert.execute(members, (PreparedStatement ps, GroupMemberRow row) -> {
            ps.setLong(1, row.groupId());
            ps.setLong(2, row.userId());
            ps.setString(3, row.role().name());
        });
    }

    private List<MockGroupRow> generateGroupRows(List<UserSnapshot> users, int toCreate, long nextGroupId, long nextRecruitmentPostId) {
        List<UserSnapshot> shuffled = new ArrayList<>(users);
        Collections.shuffle(shuffled, new Random(2025_11_08));
        List<MockGroupRow> rows = new ArrayList<>(toCreate);
        Random random = new Random(2025_11_08);
        int cursor = 0;
        long groupId = nextGroupId;
        long recruitmentPostId = nextRecruitmentPostId;

        while (cursor < shuffled.size() && rows.size() < toCreate) {
            int remaining = shuffled.size() - cursor;
            int desiredSize = random.nextInt(MAX_MEMBERS_PER_GROUP - MIN_MEMBERS_PER_GROUP + 1) + MIN_MEMBERS_PER_GROUP;
            int take = Math.min(desiredSize, remaining);
            List<UserSnapshot> chunk = new ArrayList<>(shuffled.subList(cursor, cursor + take));
            rows.add(buildGroupRow(chunk, groupId++, recruitmentPostId++, random));
            cursor += take;
        }
        return rows;
    }

    private MockGroupRow buildGroupRow(List<UserSnapshot> members, long groupId, long recruitmentPostId, Random random) {
        UserSnapshot leader = members.get(0);
        String name = "그룹 %03d".formatted(groupId);
        String about = "%s 집단 연습 그룹".formatted(leader.univName());
        String logoImage = random.nextDouble() < 0.75 ? "https://static.rankademy.app/mock/logo-%02d.png".formatted(groupId % 30) : null;
        boolean recruiting = random.nextDouble() < 0.8;
        int capacity = Math.max(members.size(), 20 + random.nextInt(16));
        LocalDateTime createdAt = LocalDateTime.now().minusDays(random.nextInt(120)).minusHours(random.nextInt(12));

        GroupRecruitmentPostRow post = buildRecruitmentPostRow(recruitmentPostId, name, recruiting, createdAt, random);
        List<GroupMemberRow> memberRows = buildMemberRows(groupId, members);

        return new MockGroupRow(
                groupId,
                name,
                about,
                logoImage,
                leader.univName(),
                recruiting,
                capacity,
                leader.id(),
                createdAt,
                post,
                memberRows
        );
    }

    private GroupRecruitmentPostRow buildRecruitmentPostRow(long postId, String groupName, boolean isRecruiting, LocalDateTime createdAt, Random random) {
        String title = "%s 모집".formatted(groupName);
        String content = """
                주 2-3회 스크림과 VOD 리뷰를 진행합니다.
                최근 사용자 데이터 기반으로 전략을 업데이트합니다.
                """;
        String requirements = REQUIREMENTS.get(random.nextInt(REQUIREMENTS.size()));
        LocalDateTime lastUppedAt = isRecruiting ? createdAt.plusHours(random.nextInt(48)) : null;

        return new GroupRecruitmentPostRow(
                postId,
                title,
                content,
                requirements,
                createdAt,
                lastUppedAt,
                isRecruiting
        );
    }

    private List<GroupMemberRow> buildMemberRows(long groupId, List<UserSnapshot> members) {
        List<GroupMemberRow> rows = new ArrayList<>(members.size());
        for (int i = 0; i < members.size(); i++) {
            UserSnapshot snapshot = members.get(i);
            GroupRole role = (i == 0) ? GroupRole.LEADER : GroupRole.MEMBER;
            rows.add(new GroupMemberRow(groupId, snapshot.id(), role));
        }
        return rows;
    }

    private List<UserSnapshot> loadUsers() {
        return jdbcTemplate.query(
                "SELECT id, username, univ_name FROM users WHERE id BETWEEN 1 AND 2000 ORDER BY id",
                (rs, rowNum) -> new UserSnapshot(
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("univ_name")
                )
        );
    }

    private long existingGroupCount() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + GROUP_TABLE, Long.class);
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

    private record UserSnapshot(long id, String username, String univName) {
    }

    private record GroupMemberRow(long groupId, long userId, GroupRole role) {
    }

    private record GroupRecruitmentPostRow(
            long id,
            String title,
            String content,
            String requirements,
            LocalDateTime createdAt,
            LocalDateTime lastUppedAt,
            boolean active
    ) {
    }

    private record MockGroupRow(
            long groupId,
            String name,
            String about,
            String logoImage,
            String univName,
            boolean recruiting,
            int capacity,
            long leaderUserId,
            LocalDateTime createdAt,
            GroupRecruitmentPostRow recruitmentPost,
            List<GroupMemberRow> members
    ) {
    }
}
