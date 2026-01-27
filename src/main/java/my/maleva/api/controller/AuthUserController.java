package my.maleva.api.controller;

import my.maleva.api.dto.AppUserDto;
import my.maleva.api.service.AppUserService;
import my.maleva.api.service.AppUserService;
import my.maleva.api.auth.JwtService;
import my.maleva.api.auth.TokenStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthUserController {

    private final AppUserService appUserService;
    private final JwtService jwtService;
    private final TokenStore tokenStore;

    public AuthUserController(AppUserService appUserService, JwtService jwtService, TokenStore tokenStore) {
        this.appUserService = appUserService;
        this.jwtService = jwtService;
        this.tokenStore = tokenStore;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AppUserDto dto) {
        AppUserDto created = appUserService.register(dto);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String userId = body.get("userId");
        String password = body.get("password");
        if (userId == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "userId and password required"));
        }

        boolean ok = appUserService.verifyCredentials(userId, password);
        if (!ok) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        String token = jwtService.generateToken(userId);
        tokenStore.storeToken(token, 3600);

        return ResponseEntity.ok(Map.of("token", token));
    }
}
