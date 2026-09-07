package my.maleva.api.common.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * Authenticated smoke-test endpoint.
 *
 * <p>Its only job is to prove, in one call, that the whole request path is
 * healthy: the app is up, CORS lets the browser through, the bearer token was
 * accepted by {@code JwtAuthenticationFilter}, and the subject was resolved.
 * A 200 here means "your token works"; a 401 means it does not. It holds no
 * business logic and must never grow any.
 *
 * <p>Secured by {@code anyRequest().authenticated()} in {@code SecurityConfig}.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Health", description = "Connectivity and authentication checks")
public class WelcomeController {

    private final String applicationName;

    public WelcomeController(@Value("${spring.application.name:Maleva API}") String applicationName) {
        this.applicationName = applicationName;
    }

    /** Response body of {@link #welcome(Authentication)}. */
    public record WelcomeResponse(String message, String user, Instant serverTime) {
    }

    @Operation(summary = "Confirm the caller's token is valid and the API is reachable")
    @GetMapping("/welcome")
    public WelcomeResponse welcome(Authentication authentication) {
        return new WelcomeResponse(
                "Welcome to " + applicationName,
                authentication.getName(),
                Instant.now());
    }
}
