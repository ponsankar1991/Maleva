package my.maleva.api.auth;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenStore tokenStore;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, TokenStore tokenStore) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.tokenStore = tokenStore;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Validated @RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String subject = auth.getName();
        if (subject == null) subject = request.getUsername();

        String token = jwtService.generateToken(subject);
        tokenStore.storeToken(token, 3600);

        return ResponseEntity.ok(Map.of(
                "token", token,
                "expiresIn", 3600
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Authorization header missing or invalid"));
        }

        String token = authorization.substring("Bearer ".length()).trim();
        if (!jwtService.validateToken(token)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid token"));
        }

        long expiresAt = jwtService.getExpiresAtMillis(token);
        long now = System.currentTimeMillis();
        long ttlMillis = Math.max(0L, expiresAt - now);
        long ttlSeconds = Math.max(1L, ttlMillis / 1000L);

        tokenStore.revokeToken(token, ttlSeconds);

        // Also remove any active session cookie or server-side session if used (we disabled Redis-backed session storage)
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }
}
