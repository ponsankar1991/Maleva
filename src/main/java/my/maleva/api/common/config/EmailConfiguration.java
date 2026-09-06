package my.maleva.api.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * EmailConfiguration - Configures email sender and REST client
 * Provides beans for EmailService and ExternalApiService
 *
 * Email configuration is optional and only enabled if mail.smtp.host is configured.
 * This allows the application to start even without email configuration.
 */
@Configuration
public class EmailConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(EmailConfiguration.class);
    private static final Integer DEFAULT_SMTP_PORT = 587;  // Default TLS port

    /**
     * Configure JavaMailSender with SMTP properties from MailProperties
     *
     * Only creates the bean if mail.smtp.host is configured.
     * Falls back to defaults if port or other properties are missing:
     * - Port: 587 (TLS) if not specified
     * - Auth: true (default)
     * - STARTTLS: enabled (default)
     */
    @Bean
    @ConditionalOnProperty(prefix = "mail.smtp", name = "host")
    public JavaMailSender javaMailSender(MailProperties properties) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        MailProperties.Smtp smtp = properties.getSmtp();

        // ✅ Validate and set host (required)
        String host = smtp.getHost();
        if (host == null || host.trim().isEmpty()) {
            throw new IllegalArgumentException("mail.smtp.host is required for email configuration");
        }
        mailSender.setHost(host);

        // ✅ Set port with fallback to default 587
        Integer port = smtp.getPort() != null ? smtp.getPort() : DEFAULT_SMTP_PORT;
        mailSender.setPort(port);
        logger.info("Email configuration: host={}, port={}", host, port);

        // ✅ Set credentials if available
        if (smtp.getUsername() != null) {
            mailSender.setUsername(smtp.getUsername());
        }
        if (smtp.getPassword() != null) {
            mailSender.setPassword(smtp.getPassword());
        }

        // ✅ Configure SMTP properties with null-safety
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", smtp.getAuth() != null ? smtp.getAuth() : true);
        // Jakarta Mail reads "mail.smtp.starttls.enable" (no "d"); the misspelt
        // key that was here was silently ignored, so STARTTLS was never on.
        boolean starttls = smtp.getStarttls() == null || smtp.getStarttls().getEnabled() == null
                || smtp.getStarttls().getEnabled();
        props.put("mail.smtp.starttls.enable", starttls);
        props.put("mail.smtp.starttls.required",
                smtp.getStarttls() != null && smtp.getStarttls().getRequired() != null
                    ? smtp.getStarttls().getRequired() : starttls);
        // Port 465 is implicit TLS (SSL from the first byte); 587/25 talk plain
        // text and upgrade with STARTTLS — the legacy MailKit code used
        // SecureSocketOptions.StartTls on 587. Forcing an SSL socket factory on
        // 587, as before, failed the handshake with "plaintext connection?".
        if (port == 465) {
            props.put("mail.smtp.ssl.enable", true);
        }
        props.put("mail.smtp.connectiontimeout", properties.getTimeout() != null ? properties.getTimeout() : 30000);
        props.put("mail.smtp.timeout", properties.getTimeout() != null ? properties.getTimeout() : 30000);
        props.put("mail.smtp.writetimeout", properties.getTimeout() != null ? properties.getTimeout() : 30000);

        return mailSender;
    }

    /**
     * Configure RestTemplate for HTTP API calls
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
