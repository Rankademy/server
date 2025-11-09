package maruhxn.rankademy.support.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;

import java.util.Collection;
import java.util.Objects;

/**
 * Lightweight helper around {@link JdbcTemplate#batchUpdate} to simplify batch insert usage in tests.
 */
public final class JdbcBatchInsert<T> {

    private final JdbcTemplate jdbcTemplate;
    private final String sql;
    private final int batchSize;

    private JdbcBatchInsert(JdbcTemplate jdbcTemplate, String sql, int batchSize) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate must not be null");
        this.sql = Objects.requireNonNull(sql, "sql must not be null");
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize must be greater than zero");
        }
        this.batchSize = batchSize;
    }

    public static <T> JdbcBatchInsert<T> of(JdbcTemplate jdbcTemplate, String sql) {
        return new JdbcBatchInsert<>(jdbcTemplate, sql, 500);
    }

    public static <T> JdbcBatchInsert<T> of(JdbcTemplate jdbcTemplate, String sql, int batchSize) {
        return new JdbcBatchInsert<>(jdbcTemplate, sql, batchSize);
    }

    public void execute(Collection<T> rows, ParameterizedPreparedStatementSetter<T> setter) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        jdbcTemplate.batchUpdate(sql, rows, batchSize, setter);
    }
}
