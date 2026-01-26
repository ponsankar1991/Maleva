package my.maleva.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisStartupChecker implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(RedisStartupChecker.class);

    private final StringRedisTemplate redisTemplate;

    @Value("${spring.redis.host:redis}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Autowired(required = false)
    public RedisStartupChecker(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("Resolved Redis host: {} port: {}", redisHost, redisPort);

        if (redisTemplate == null) {
            log.info("StringRedisTemplate bean not present - Redis not configured (or intentionally disabled)");
            return;
        }

        try {
            // Retry ping for up to 30 seconds to allow Redis to become available
            int retries = 30;
            int attempt = 0;
            while (attempt < retries) {
                try {
                    String pong = redisTemplate.getConnectionFactory().getConnection().ping();
                    log.info("Redis ping response: {}", pong);
                    break;
                } catch (Exception inner) {
                    attempt++;
                    Thread.sleep(1000);
                }
            }
            if (attempt >= retries) {
                log.warn("Redis did not respond to PING within {} seconds", retries);
            }
        } catch (Exception e) {
            log.error("Failed to ping Redis at startup: {}", e.toString());
        }
    }
}
