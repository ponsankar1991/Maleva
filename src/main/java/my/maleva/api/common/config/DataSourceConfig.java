package my.maleva.api.common.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * The application's own database — declared explicitly rather than left to
 * Spring Boot's auto-configuration.
 *
 * <p>It has to be explicit because this application has a second datasource:
 * {@code qneDataSource} (QNE's own SQL Server, used by the chart-of-accounts
 * import). Spring Boot's {@code DataSourceAutoConfiguration} is annotated
 * {@code @ConditionalOnMissingBean(DataSource.class)}, so the mere existence
 * of that second bean makes it back off and create <em>nothing</em>. JPA then
 * binds to the only datasource in the context — QNE's — and every query fails
 * with "Invalid object name", because the application's tables do not exist
 * over there. Marking this one {@code @Primary} is what keeps that from
 * happening.
 *
 * <p>Binding {@code spring.datasource.hikari} here also restores
 * {@code auto-commit: false}, which the hand-built QNE pool does not set;
 * without it Hibernate's {@code provider_disables_autocommit} promise is
 * broken and rollbacks throw, masking the real exception behind
 * "Unable to rollback against JDBC Connection".
 */
@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.hikari")
    public HikariDataSource dataSource(DataSourceProperties dataSourceProperties) {
        return dataSourceProperties
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    /**
     * The application's JdbcTemplate, on its own database.
     *
     * <p>Explicit for the same reason the datasource is: {@code qneJdbcTemplate}
     * exists, so Boot's JdbcTemplate auto-configuration
     * ({@code @ConditionalOnMissingBean(JdbcOperations.class)}) backs off and
     * every plain {@code JdbcTemplate} injection point — the dashboards, the
     * planning search, every hand-written SQL repository — would receive the
     * QNE template instead and fail with "Invalid object name".
     */
    @Bean
    @Primary
    public JdbcTemplate jdbcTemplate(HikariDataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /** Same reasoning as {@link #jdbcTemplate} for the named-parameter flavour. */
    @Bean
    @Primary
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(JdbcTemplate jdbcTemplate) {
        return new NamedParameterJdbcTemplate(jdbcTemplate);
    }
}
