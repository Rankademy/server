package maruhxn.rankademy.runner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import maruhxn.rankademy.domain.user.*;
import maruhxn.rankademy.domain.user.service.TierMapper;
import maruhxn.rankademy.support.jdbc.JdbcBatchInsert;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class MockUserDataInsertRunner  {

    private static final int TARGET_USER_COUNT = 2_000;
    private static final int BATCH_SIZE = 250;
    private static final List<String> MAJORS = List.of(
            "컴퓨터공학과", "경영학과", "물리학과", "경제학과",
            "통계학과", "기계공학과", "전기공학과",
            "화학공학과", "시각디자인과", "체육학과", "교육학과",
            "건축학과", "신소재공학과", "미디어학과", "안경광학과"
    );
    private static final List<String> CHAMPION_IDS = List.of(
            "Aatrox", "Ahri", "Akali", "Ashe", "Caitlyn", "Darius",
            "Ezreal", "Garen", "Jinx", "Lux", "Morgana", "Riven", "Yasuo", "Zed"
    );

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public MockUserDataInsertRunner(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Value("classpath:univ-mail.json")
    private Resource univMailResource;

    @Transactional
    public void run() throws Exception {
        long existingMockUsers = existingMockUserCount();
        if (existingMockUsers >= TARGET_USER_COUNT) {
            return;
        }

        List<UnivMail> univMails = loadUnivMailData();
        if (univMails.isEmpty()) {
            throw new IllegalStateException("No university data available. Please populate the univ table first.");
        }

        int toCreate = (int) (TARGET_USER_COUNT - existingMockUsers);
        long nextSummonerId = nextId("summoner_info");
        long nextUserId = nextId("users");

        List<MockUserRow> rows = generateRows(univMails, toCreate, nextSummonerId, nextUserId);

        insertSummonerInfos(rows);
        insertSummonerMostChampions(rows);
        insertUsers(rows);
    }

    private void insertSummonerInfos(List<MockUserRow> rows) {
        String sql = """
                INSERT INTO summoner_info
                (id, puuid, summoner_name, summoner_tag, summoner_icon, tier, tier_rank, lp, mapped_tier, win_count, loss_count, enrolled_at, last_synced_match_id, match_synced_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        JdbcBatchInsert<MockUserRow> batchInsert = JdbcBatchInsert.of(jdbcTemplate, sql, BATCH_SIZE);
        batchInsert.execute(rows, (PreparedStatement ps, MockUserRow row) -> {
            ps.setLong(1, row.summonerInfoId());
            ps.setString(2, row.puuid());
            ps.setString(3, row.summonerName());
            ps.setString(4, row.summonerTag());
            ps.setInt(5, row.summonerIcon());
            ps.setString(6, row.tier().name());
            ps.setString(7, row.rank().name());
            ps.setInt(8, row.lp());
            ps.setInt(9, row.mappedTier());
            ps.setInt(10, row.winCount());
            ps.setInt(11, row.lossCount());
            setTimestamp(ps, 12, row.enrolledAt());
            ps.setNull(13, Types.VARCHAR);
            ps.setNull(14, Types.TIMESTAMP);
        });
    }

    private void insertUsers(List<MockUserRow> rows) {
        String sql = """
                INSERT INTO users
                (id, username, email, auth_status, description, joined_at,
                 main_position, sub_position, role, univ_name, univ_mail,
                 admission_year, major, summoner_info_id, last_login_at,
                 last_label_updated_at, mu, sigma)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        JdbcBatchInsert<MockUserRow> batchInsert = JdbcBatchInsert.of(jdbcTemplate, sql, BATCH_SIZE);
        batchInsert.execute(rows, (PreparedStatement ps, MockUserRow row) -> {
            ps.setLong(1, row.userId());
            ps.setString(2, row.username());
            ps.setString(3, row.email());
            ps.setString(4, row.authStatus().name());
            ps.setString(5, row.description());
            setTimestamp(ps, 6, row.joinedAt());
            ps.setString(7, row.mainPosition().name());
            ps.setString(8, row.subPosition().name());
            ps.setString(9, row.role().name());
            ps.setString(10, row.univName());
            ps.setString(11, row.univMail());
            ps.setInt(12, row.admissionYear());
            ps.setString(13, row.major());
            ps.setLong(14, row.summonerInfoId());
            setTimestamp(ps, 15, row.lastLoginAt());
            setTimestamp(ps, 16, row.lastLabelUpdatedAt());
            ps.setDouble(17, row.mu());
            ps.setDouble(18, row.sigma());
        });
    }

    private void insertSummonerMostChampions(List<MockUserRow> rows) {
        String sql = """
                INSERT INTO summoner_most_champion
                (summoner_info_id, champion_id, play_count, champion_order)
                VALUES (?, ?, ?, ?)
                """;

        List<SummonerMostChampionRow> championRows = new ArrayList<>();
        for (MockUserRow row : rows) {
            List<ChampionPlayRecord> champions = row.mostChampions();
            for (int i = 0; i < champions.size(); i++) {
                championRows.add(new SummonerMostChampionRow(row.summonerInfoId(), champions.get(i), i));
            }
        }

        if (championRows.isEmpty()) {
            return;
        }

        JdbcBatchInsert<SummonerMostChampionRow> batchInsert = JdbcBatchInsert.of(jdbcTemplate, sql, BATCH_SIZE);
        batchInsert.execute(championRows, (PreparedStatement ps, SummonerMostChampionRow row) -> {
            ps.setLong(1, row.summonerInfoId());
            ps.setString(2, row.champion().championId());
            ps.setLong(3, row.champion().playCount());
            ps.setInt(4, row.championOrder());
        });
    }

    private List<MockUserRow> generateRows(List<UnivMail> univMails, int count, long nextSummonerId, long nextUserId) {
        Random random = new Random(2025_11_08);
        List<LolPosition> positions = Arrays.stream(LolPosition.values())
                .filter(position -> position != LolPosition.ANY)
                .toList();
        Tier[] tiers = {Tier.IRON, Tier.BRONZE, Tier.SILVER, Tier.GOLD, Tier.PLATINUM, Tier.EMERALD, Tier.DIAMOND, Tier.MASTER};
        Rank[] ranks = {Rank.I, Rank.II, Rank.III, Rank.IV};

        LocalDateTime now = LocalDateTime.now();
        List<MockUserRow> rows = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            long userId = nextUserId + i;
            long summonerId = nextSummonerId + i;

            UnivMail univMail = univMails.get(i % univMails.size());

            String username = "user%04d".formatted(userId);
            String mainEmail = username + "@rankademy.app";
            String univEmail = username + "@" + univMail.domain();

            LocalDateTime joinedAt = now.minusDays(random.nextInt(365)).minusHours(random.nextInt(24));
            LocalDateTime lastLoginAt = joinedAt.plusDays(random.nextInt(60));
            LocalDateTime lastLabelUpdatedAt = lastLoginAt;

            LolPosition mainPosition = positions.get(random.nextInt(positions.size()));
            LolPosition subPosition = pickDifferentPosition(positions, mainPosition, random);

            Tier tier = tiers[random.nextInt(tiers.length)];
            Rank rank = ranks[random.nextInt(ranks.length)];
            int lp = random.nextInt(100);
            int mappedTier = TierMapper.tierToScore(tier, rank, lp);

            int winCount = 50 + random.nextInt(200);
            int lossCount = 50 + random.nextInt(200);
            List<ChampionPlayRecord> mostChampions = generateMostChampionRecords(random);

            rows.add(new MockUserRow(
                    userId,
                    summonerId,
                    username,
                    mainEmail,
                    "설명 %d".formatted(userId),
                    joinedAt,
                    lastLoginAt,
                    lastLabelUpdatedAt,
                    mainPosition,
                    subPosition,
                    UserAuthStatus.AUTHORIZED,
                    Role.ROLE_USER,
                    univMail.name(),
                    univEmail,
                    2012 + random.nextInt(13),
                    MAJORS.get(random.nextInt(MAJORS.size())),
                    EffectiveStrength.DEFAULT_MU,
                    EffectiveStrength.DEFAULT_SIGMA,
                    UUID.randomUUID().toString(),
                    "소환사이름%04d".formatted(userId),
                    "KR1",
                    10 + random.nextInt(500),
                    tier,
                    rank,
                    lp,
                    mappedTier,
                    winCount,
                    lossCount,
                    joinedAt,
                    mostChampions
            ));
        }

        return rows;
    }

    private List<ChampionPlayRecord> generateMostChampionRecords(Random random) {
        List<String> pool = new ArrayList<>(CHAMPION_IDS);
        Collections.shuffle(pool, random);
        int championCount = Math.min(3, pool.size());

        List<ChampionPlayRecord> champions = new ArrayList<>(championCount);
        for (int i = 0; i < championCount; i++) {
            long playCount = 5L + random.nextInt(61);
            champions.add(new ChampionPlayRecord(pool.get(i), playCount));
        }

        return List.copyOf(champions);
    }

    private List<UnivMail> loadUnivMailData() throws IOException {
        List<UnivMail> existing = jdbcTemplate.query(
                "SELECT univ_name, univ_mail_postfix FROM univ",
                (rs, rowNum) -> new UnivMail(rs.getString("univ_name"), rs.getString("univ_mail_postfix"))
        );

        if (!existing.isEmpty()) {
            return existing;
        }

        List<UnivMail> fromJson = readUnivMailJson();
        insertUnivData(fromJson);
        return fromJson;
    }

    private void insertUnivData(List<UnivMail> univMails) {
        String sql = "INSERT INTO univ (univ_name, univ_mail_postfix) VALUES (?, ?)";
        JdbcBatchInsert<UnivMail> batchInsert = JdbcBatchInsert.of(jdbcTemplate, sql, BATCH_SIZE);
        batchInsert.execute(univMails, (PreparedStatement ps, UnivMail mail) -> {
            ps.setString(1, mail.name());
            ps.setString(2, mail.domain());
        });
    }

    private List<UnivMail> readUnivMailJson() throws IOException {
        try (InputStream inputStream = univMailResource.getInputStream()) {
            TypeReference<LinkedHashMap<String, String>> type = new TypeReference<>() {};
            Map<String, String> map = objectMapper.readValue(inputStream, type);
            return map.entrySet().stream()
                    .map(entry -> new UnivMail(entry.getKey(), entry.getValue()))
                    .toList();
        }
    }

    private LolPosition pickDifferentPosition(List<LolPosition> positions, LolPosition main, Random random) {
        LolPosition sub = main;
        while (sub == main) {
            sub = positions.get(random.nextInt(positions.size()));
        }
        return sub;
    }

    private long nextId(String tableName) {
        Long max = jdbcTemplate.queryForObject("SELECT COALESCE(MAX(id), 0) FROM " + tableName, Long.class);
        return (max == null ? 0L : max) + 1;
    }

    private long existingMockUserCount() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE username LIKE 'mockuser%'", Long.class);
        return count == null ? 0L : count;
    }

    private static void setTimestamp(PreparedStatement ps, int index, LocalDateTime value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(index, Timestamp.valueOf(value));
        }
    }

    private record UnivMail(String name, String domain) {
    }

    private record MockUserRow(
            long userId,
            long summonerInfoId,
            String username,
            String email,
            String description,
            LocalDateTime joinedAt,
            LocalDateTime lastLoginAt,
            LocalDateTime lastLabelUpdatedAt,
            LolPosition mainPosition,
            LolPosition subPosition,
            UserAuthStatus authStatus,
            Role role,
            String univName,
            String univMail,
            Integer admissionYear,
            String major,
            double mu,
            double sigma,
            String puuid,
            String summonerName,
            String summonerTag,
            int summonerIcon,
            Tier tier,
            Rank rank,
            int lp,
            int mappedTier,
            int winCount,
            int lossCount,
            LocalDateTime enrolledAt,
            List<ChampionPlayRecord> mostChampions
    ) {
    }

    private record SummonerMostChampionRow(
            long summonerInfoId,
            ChampionPlayRecord champion,
            int championOrder
    ) {
    }
}
