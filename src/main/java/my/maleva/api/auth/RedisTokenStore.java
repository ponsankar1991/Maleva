package my.maleva.api.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RedisTokenStore implements TokenStore {

    private StringRedisTemplate redisTemplate;

    // in-memory fallback store: token -> expiryEpochMillis
    private final Map<String, Long> fallbackStore = new ConcurrentHashMap<>();
    // in-memory revoked tokens: token -> expiryEpochMillis
    private final Map<String, Long> fallbackRevoked = new ConcurrentHashMap<>();

    public RedisTokenStore() {
        // no-arg constructor
    }

    @Autowired(required = false)
    public void setRedisTemplate(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void storeToken(String token, long ttlSeconds) {
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(token, "1", Duration.ofSeconds(ttlSeconds));
                fallbackStore.remove(token);
                return;
            } catch (RuntimeException ex) {
                // fall through to fallback
            }
        }
        long expiry = Instant.now().plusSeconds(ttlSeconds).toEpochMilli();
        fallbackStore.put(token, expiry);
    }

    @Override
    public boolean exists(String token) {
        // existence means token present (not revoked)
        if (isRevoked(token)) {
            return false;
        }

        if (redisTemplate != null) {
            try {
                Boolean has = redisTemplate.hasKey(token);
                if (Boolean.TRUE.equals(has)) {
                    return true;
                }
            } catch (RuntimeException ex) {
                // ignore and check fallback
            }
        }

        Long expiry = fallbackStore.get(token);
        if (expiry == null) return false;
        if (Instant.now().toEpochMilli() > expiry) {
            fallbackStore.remove(token);
            return false;
        }
        return true;
    }

    @Override
    public void revoke(String token) {
        if (redisTemplate != null) {
            try {
                redisTemplate.delete(token);
                // also remove any revoked marker
                redisTemplate.delete(revokedKey(token));
                return;
            } catch (RuntimeException ex) {
                // ignore and remove from fallback
                fallbackStore.remove(token);
                fallbackRevoked.remove(token);
                return;
            }
        }
        fallbackStore.remove(token);
        fallbackRevoked.remove(token);
    }

    @Override
    public void revokeToken(String token, long ttlSeconds) {
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(revokedKey(token), "1", Duration.ofSeconds(ttlSeconds));
                return;
            } catch (RuntimeException ex) {
                // fall through to fallback
            }
        }
        long expiry = Instant.now().plusSeconds(ttlSeconds).toEpochMilli();
        fallbackRevoked.put(token, expiry);
    }

    private String revokedKey(String token) {
        return "revoked:" + token;
    }

    private boolean isRevoked(String token) {
        if (redisTemplate != null) {
            try {
                Boolean has = redisTemplate.hasKey(revokedKey(token));
                if (Boolean.TRUE.equals(has)) {
                    return true;
                }
            } catch (RuntimeException ex) {
                // ignore and check fallback
            }
        }
        Long expiry = fallbackRevoked.get(token);
        if (expiry == null) return false;
        if (Instant.now().toEpochMilli() > expiry) {
            fallbackRevoked.remove(token);
            return false;
        }
        return true;
    }
}
