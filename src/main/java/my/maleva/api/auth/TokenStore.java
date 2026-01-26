package my.maleva.api.auth;

public interface TokenStore {
    void storeToken(String token, long ttlSeconds);
    boolean exists(String token);
    void revoke(String token);
    // Mark a token as revoked for the given ttl (seconds) so it can be checked against a revocation list
    void revokeToken(String token, long ttlSeconds);
}
