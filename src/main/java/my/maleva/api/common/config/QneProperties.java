package my.maleva.api.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "qne")
public class QneProperties {

    private boolean enabled;
    private boolean demo;
    private boolean view;
    private boolean reportView;

    private String baseUrl;
    private Db db;
    private ControlCodes controlCodes;
    private Datasource datasource = new Datasource();

    @Getter @Setter
    public static class Db {
        private String trial;
        private String live;
    }

    @Getter @Setter
    public static class ControlCodes {
        private String customer;
        private String supplier;
    }

    /**
     * Direct SQL access to QNE's own database.
     *
     * <p>One legacy feature reads QNE's {@code GLAccounts} table straight off
     * their SQL Server because the REST API has no chart-of-accounts endpoint
     * (legacy {@code QneCon}/{@code QneConDemo} connection strings). The
     * database name is not configured here: it is the same value as the
     * DbCode, resolved by the {@code demo} flag.
     */
    @Getter @Setter
    public static class Datasource {
        /** Blank disables the direct-DB features rather than failing startup. */
        private String host = "";
        private int port = 1433;
        private String username;
        private String password;
    }
}
