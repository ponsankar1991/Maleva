package my.maleva.api.integration.qne;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.common.config.QneProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The second datasource: QNE's own SQL Server.
 *
 * <p>Exists because one legacy feature — the chart-of-accounts import — reads
 * QNE's {@code GLAccounts} table directly; the QNE REST API has no endpoint
 * for it. Everything else goes through {@link QneClient} over HTTP, and new
 * code should never reach for this connection when an API route exists.
 *
 * <p>This bean must never be {@code @Primary}: it is the second datasource,
 * and the application's own one is declared in {@code DataSourceConfig}.
 * Removing that explicit primary makes Spring Boot's datasource
 * auto-configuration back off and bind JPA to this connection instead, which
 * fails every query with "Invalid object name".
 *
 * <p>Deliberately defensive, because this is somebody else's database on the
 * far side of the internet:
 * <ul>
 *   <li>Lazy and non-validating at startup ({@code initializationFailTimeout}
 *       -1): QNE being down must not stop this application booting.</li>
 *   <li>Pool of 2: the one feature using it is an occasional admin action.</li>
 *   <li>Read-only: nothing here may ever write into QNE's database.</li>
 *   <li>Absent entirely when {@code qne.datasource.host} is blank, so an
 *       environment without QNE DB access degrades to a clear error message
 *       in the one feature, not a broken context.</li>
 * </ul>
 *
 * <p>Always connects to the live catalog ({@code qne.db.live}, the legacy
 * {@code QneCon}). The {@code qne.demo} flag deliberately does not apply here
 * — it only switches the HTTP tenant; the trial connection string
 * ({@code QneConDemo}) was dropped from this port.
 */
@Slf4j
@Configuration
@ConditionalOnExpression("!'${qne.datasource.host:}'.isEmpty()")
public class QneDbConfig {

    @Bean(name = "qneDataSource")
    @Lazy
    public HikariDataSource qneDataSource(QneProperties properties) {
        QneProperties.Datasource ds = properties.getDatasource();
        String dbName = properties.getDb().getLive();

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:sqlserver://" + ds.getHost() + ":" + ds.getPort()
                + ";databaseName=" + dbName
                + ";encrypt=true;trustServerCertificate=true;loginTimeout=30");
        dataSource.setUsername(ds.getUsername());
        dataSource.setPassword(ds.getPassword());
        dataSource.setMaximumPoolSize(2);
        dataSource.setMinimumIdle(0);
        dataSource.setReadOnly(true);
        dataSource.setInitializationFailTimeout(-1);
        dataSource.setPoolName("qne-db");

        log.info("QNE direct-DB datasource configured for {} ({})", ds.getHost(), dbName);
        return dataSource;
    }

    @Bean(name = "qneJdbcTemplate")
    @Lazy
    public JdbcTemplate qneJdbcTemplate(@Qualifier("qneDataSource") HikariDataSource qneDataSource) {
        JdbcTemplate template = new JdbcTemplate(qneDataSource);
        // A hung query against a remote third-party DB should fail, not wedge
        // a request thread for the transport's 30 minutes.
        template.setQueryTimeout(60);
        return template;
    }
}
