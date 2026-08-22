package my.maleva.api.common.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * Startup guard for the one datasource setting that silently disables every
 * transaction in the application.
 *
 * hibernate.connection.provider_disables_autocommit=true is a promise to
 * Hibernate that the pool hands out connections with autocommit already off, so
 * Hibernate skips its own setAutoCommit(false) call to save a round trip. The
 * promise is only true when the pool is configured with auto-commit: false.
 *
 * When the two disagree, connections stay in autocommit mode: every statement
 * commits on its own, @Transactional stops being atomic, a failed request
 * leaves half-written rows behind, and the rollback that Spring attempts throws
 * "Unable to rollback against JDBC Connection" - which replaces the real
 * exception, so a plain validation error is reported as a 500 with a message
 * that describes none of it.
 *
 * That combination produces no warning from Spring, Hibernate or Hikari on its
 * own. This check states it plainly at startup instead.
 */
@Component
public class TransactionIntegrityChecker {

    private static final Logger logger = LoggerFactory.getLogger(TransactionIntegrityChecker.class);

    private final DataSource dataSource;

    @Value("${spring.jpa.properties.hibernate.connection.provider_disables_autocommit:false}")
    private boolean providerDisablesAutocommit;

    public TransactionIntegrityChecker(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void verify() {
        if (!(dataSource instanceof HikariDataSource hikari)) {
            logger.info("Transaction integrity check skipped: datasource is {}, not HikariDataSource",
                    dataSource.getClass().getName());
            return;
        }

        boolean poolAutoCommit = hikari.isAutoCommit();

        if (providerDisablesAutocommit && poolAutoCommit) {
            logger.error(
                    "\n" +
                    "===================================================================\n" +
                    " TRANSACTIONS ARE NOT ATOMIC - DATASOURCE MISCONFIGURED\n" +
                    "===================================================================\n" +
                    " spring.datasource.hikari.auto-commit ................. : true\n" +
                    " hibernate.connection.provider_disables_autocommit .... : true\n" +
                    "\n" +
                    " These contradict each other. Hibernate has been told the pool\n" +
                    " already disabled autocommit, so it never disables it itself.\n" +
                    " Every INSERT and UPDATE therefore commits immediately and\n" +
                    " @Transactional does nothing.\n" +
                    "\n" +
                    " Expect: partial writes surviving failed requests, and every\n" +
                    " error surfacing as 500 \"Unable to rollback against JDBC\n" +
                    " Connection\" instead of its real message.\n" +
                    "\n" +
                    " FIX: set spring.datasource.hikari.auto-commit: false\n" +
                    "===================================================================");
            return;
        }

        if (!providerDisablesAutocommit && !poolAutoCommit) {
            logger.warn("Pool autocommit is off but hibernate.connection.provider_disables_autocommit is not set."
                    + " Transactions are correct, but Hibernate issues a redundant setAutoCommit(false) per"
                    + " transaction. Set the property to true to drop that round trip.");
            return;
        }

        logger.info("Transaction integrity check passed: pool auto-commit={}, provider_disables_autocommit={}",
                poolAutoCommit, providerDisablesAutocommit);
    }
}
