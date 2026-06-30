package my.maleva.api.module.user.controller;

import my.maleva.api.module.user.dto.AppUserDto;
import my.maleva.api.module.employee.dto.EmployeeMasterDto;
import my.maleva.api.module.user.service.AppUserService;
import my.maleva.api.module.employee.service.EmployeeMasterService;
import my.maleva.api.security.controller.JwtService;
import my.maleva.api.security.controller.TokenStore;
import my.maleva.api.common.constant.UserRoles;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthUserController {

    private final EmployeeMasterService employeeMasterService;
    private final AppUserService appUserService; // keep for register endpoint compatibility
    private final JwtService jwtService;
    private final TokenStore tokenStore;

    public AuthUserController(AppUserService appUserService, EmployeeMasterService employeeMasterService, JwtService jwtService, TokenStore tokenStore) {
        this.appUserService = appUserService;
        this.employeeMasterService = employeeMasterService;
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
        // accept either userName or userId (backwards compatibility)
        String userName = body.getOrDefault("userName", body.get("userId"));
        String password = body.get("password");
        if (userName == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "userName and password required"));
        }

        boolean ok = employeeMasterService.verifyCredentials(userName, password);
        if (!ok) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        // fetch user to obtain roleId + permisionId
        var dto = employeeMasterService.findByUserName(userName);
        Integer roleId = dto == null ? null : dto.getRoleId();
        Integer permisionId = dto == null ? null : dto.getPermisionId();
        String token = jwtService.generateToken(userName, roleId);
        storeAccessToken(token);

        return ResponseEntity.ok(buildAuthResponse(token, dto, roleId, permisionId));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Authorization header missing or invalid"));
        }

        String currentToken = authorization.substring("Bearer ".length()).trim();
        if (!jwtService.validateToken(currentToken) || !tokenStore.exists(currentToken)) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid or expired token"));
        }

        long now = System.currentTimeMillis();
        long sessionExpiresAt = jwtService.getSessionExpiresAtMillis(currentToken);
        if (sessionExpiresAt > 0 && now >= sessionExpiresAt) {
            tokenStore.revoke(currentToken);
            return ResponseEntity.status(401).body(Map.of("error", "Session expired"));
        }

        String userName = jwtService.getSubject(currentToken);
        var dto = employeeMasterService.findByUserName(userName);
        Integer roleId = dto != null ? dto.getRoleId() : jwtService.getRoleId(currentToken);

        long effectiveSessionExpiry = sessionExpiresAt > 0
                ? sessionExpiresAt
                : now + (jwtService.getSessionMaxAgeSeconds() * 1000L);

        String refreshedToken = jwtService.generateToken(userName, roleId, effectiveSessionExpiry);

        tokenStore.revoke(currentToken);
        storeAccessToken(refreshedToken);

        Integer permisionId = dto == null ? null : dto.getPermisionId();
        return ResponseEntity.ok(buildAuthResponse(refreshedToken, dto, roleId, permisionId));
    }

    private void storeAccessToken(String token) {
        long expiresAt = jwtService.getExpiresAtMillis(token);
        long ttlMillis = Math.max(1000L, expiresAt - System.currentTimeMillis());
        long ttlSeconds = Math.max(1L, (ttlMillis + 999L) / 1000L);
        tokenStore.storeToken(token, ttlSeconds);
    }

    private Map<String, Object> buildAuthResponse(String token, EmployeeMasterDto dto, Integer roleId, Integer permisionId) {
        var response = new LinkedHashMap<String, Object>();
        String roleName = roleId != null
                ? UserRoles.fromId(roleId).map(Enum::name).orElse(null)
                : null;

        response.put("token", token);
        response.put("roleId", roleId);
        response.put("userName", dto != null ? dto.getEmployeeName() : null);
        response.put("UserId", dto != null ? dto.getId() : null);
        response.put("rolename", roleName);
        response.put("companyId", dto != null ? dto.getCompanyRefId() : null);
        response.put("permisionId", permisionId);
        response.put("expiresAt", jwtService.getExpiresAtMillis(token));
        response.put("sessionExpiresAt", jwtService.getSessionExpiresAtMillis(token));
        return response;
    }
}
