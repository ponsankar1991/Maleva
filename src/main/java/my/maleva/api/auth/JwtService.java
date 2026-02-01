package my.maleva.api.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final Algorithm algorithm;
    private final long expirationSeconds;

    public JwtService(@Value("${jwt.secret:changeit}") String secret,
                      @Value("${jwt.expiration:3600}") long expirationSeconds) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.expirationSeconds = expirationSeconds;
    }

    // Backwards-compatible single-arg token generator (no role)
    public String generateToken(String subject) {
        return generateToken(subject, null);
    }

    // New generator includes roleId as a numeric claim when provided
    public String generateToken(String subject, Integer roleId) {
        Instant now = Instant.now();
        Date issuedAt = Date.from(now);
        Date expiresAt = Date.from(now.plusSeconds(expirationSeconds));

        com.auth0.jwt.JWTCreator.Builder builder = JWT.create()
                .withSubject(subject)
                .withIssuedAt(issuedAt)
                .withExpiresAt(expiresAt);

        if (roleId != null) {
            builder.withClaim("roleId", roleId);
        }

        return builder.sign(algorithm);
    }

    public boolean validateToken(String token) {
        try {
            JWT.require(algorithm).build().verify(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getSubject(String token) {
        DecodedJWT jwt = JWT.decode(token);
        return jwt.getSubject();
    }

    public Integer getRoleId(String token) {
        DecodedJWT jwt = JWT.decode(token);
        if (jwt.getClaim("roleId").isNull()) return null;
        try {
            return jwt.getClaim("roleId").asInt();
        } catch (Exception e) {
            return null;
        }
    }

    public long getExpiresAtMillis(String token) {
        DecodedJWT jwt = JWT.decode(token);
        Date expires = jwt.getExpiresAt();
        return expires != null ? expires.getTime() : -1L;
    }

    // expose configured expiration (seconds) for callers that need to store TTLs
    public long getExpirationSeconds() {
        return expirationSeconds;
    }
}
