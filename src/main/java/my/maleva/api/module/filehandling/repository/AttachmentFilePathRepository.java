package my.maleva.api.module.filehandling.repository;

import my.maleva.api.common.exception.InvalidRequestException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

/**
 * Writes the comma-joined attachment paths back to a record's {@code FilePath}
 * column.
 *
 * Dozens of legacy tables carry that column, so the table name has to be
 * dynamic and cannot be a bind parameter. The legacy service concatenated the
 * caller-supplied name - which was the {@code FolderName} request header -
 * straight into the statement, so any header value became executable SQL. The
 * name is checked against an identifier pattern here before it is interpolated,
 * and the values themselves are always bound.
 */
@Repository
public class AttachmentFilePathRepository {

    private static final Pattern SAFE_TABLE_NAME = Pattern.compile("[A-Za-z_][A-Za-z0-9_]{0,127}");

    private final JdbcTemplate jdbcTemplate;

    public AttachmentFilePathRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Runs in its own transaction: the pool hands out connections with
     * autocommit off, so an uncommitted JdbcTemplate write is rolled back when
     * the connection is returned and the update silently does nothing.
     *
     * @return rows updated
     */
    @Transactional
    public int updateFilePath(String tableName, int recordId, int companyRefId, String joinedPaths) {
        if (tableName == null || !SAFE_TABLE_NAME.matcher(tableName).matches()) {
            throw new InvalidRequestException("Invalid FilePath table name: " + tableName);
        }

        String sql = "UPDATE [" + tableName + "] SET FilePath = ? WHERE Id = ? AND CompanyRefId = ?";
        return jdbcTemplate.update(sql, joinedPaths, recordId, companyRefId);
    }
}
