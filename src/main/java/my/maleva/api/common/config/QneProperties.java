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
}
