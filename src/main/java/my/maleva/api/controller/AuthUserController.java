package my.maleva.api.controller;

import my.maleva.api.dto.AppUserDto;
import my.maleva.api.service.AppUserService;
import my.maleva.api.service.EmployeeMasterService;
import my.maleva.api.auth.JwtService;
import my.maleva.api.auth.TokenStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

        // fetch user to obtain roleId
        var dto = employeeMasterService.findByUserName(userName);
        Integer roleId = dto == null ? null : dto.getRoleId();

        String token = jwtService.generateToken(userName, roleId);
        tokenStore.storeToken(token, jwtService.getExpirationSeconds());

        return ResponseEntity.ok(Map.of("token", token, "roleId", roleId));
    }
}
