package my.maleva.api.module.gps.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Turns Spring scheduling on, but only when the GPS sync is enabled.
 *
 * Scheduling is not enabled anywhere else in the application, so switching it on
 * globally would activate any future @Scheduled bean as a side effect. Gating it
 * on the same flag as the scheduler keeps the blast radius to this module.
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "wialon.sync", name = "enabled", havingValue = "true")
public class GpsSchedulingConfig {
}
