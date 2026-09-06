package my.maleva.api.module.invoice.print;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

/**
 * Developer tool, not a test: runs one read-only SQL statement against the
 * configured database and prints the rows, for checking a stored procedure's
 * text or a column's real values while porting a legacy screen.
 *
 * <pre>
 * mvn -o -q test -Dtest=DbQueryTool -Dsurefire.failIfNoSpecifiedTests=false \
 *     -Ddb.sql="EXEC sp_helptext 'SP_DoMaster'"
 * </pre>
 *
 * Connection details come from {@code db.url}, {@code db.user} and
 * {@code db.password} system properties, defaulting to the application.yaml
 * demo database. Skipped unless {@code db.sql} is set.
 */
class DbQueryTool {

    @Test
    @EnabledIfSystemProperty(named = "db.sql", matches = ".+")
    void run() throws Exception {
        String url = System.getProperty("db.url",
                "jdbc:sqlserver://103.215.139.8:1433;databaseName=MalevanewDemo;encrypt=true;trustServerCertificate=true;loginTimeout=30");
        String user = System.getProperty("db.user", "sa");
        String password = System.getProperty("db.password", "Kassamy@123");
        String sql = System.getProperty("db.sql");

        try (Connection c = DriverManager.getConnection(url, user, password);
             Statement st = c.createStatement()) {
            boolean hasResult = st.execute(sql);
            int set = 0;
            while (true) {
                if (hasResult) {
                    try (ResultSet rs = st.getResultSet()) {
                        ResultSetMetaData md = rs.getMetaData();
                        System.out.println("=== result set " + (++set));
                        StringBuilder head = new StringBuilder();
                        for (int i = 1; i <= md.getColumnCount(); i++) {
                            head.append(md.getColumnLabel(i)).append(" | ");
                        }
                        System.out.println(head);
                        int rows = 0;
                        while (rs.next() && rows++ < 500) {
                            StringBuilder line = new StringBuilder();
                            for (int i = 1; i <= md.getColumnCount(); i++) {
                                line.append(rs.getString(i)).append(" | ");
                            }
                            System.out.println(line);
                        }
                    }
                } else if (st.getUpdateCount() == -1) {
                    break;
                }
                hasResult = st.getMoreResults();
            }
        }
    }
}
