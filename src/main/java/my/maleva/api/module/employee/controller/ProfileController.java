package my.maleva.api.module.employee.controller;

import my.maleva.api.module.employee.dto.ProfileDto;
import my.maleva.api.module.employee.entity.EmployeeMaster;
import my.maleva.api.module.employee.mapper.ProfileMapper;
import my.maleva.api.module.employee.repository.EmployeeMasterRepository;
import my.maleva.api.module.employee.service.EmployeeMasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Profile controller for the currently authenticated employee.
 * Uses Spring Security context to identify the logged-in user.
 */
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final EmployeeMasterRepository employeeRepository;
    private final EmployeeMasterService employeeService;
    private final ProfileMapper profileMapper;
    private final PasswordEncoder passwordEncoder;

    public ProfileController(
            EmployeeMasterRepository employeeRepository,
            EmployeeMasterService employeeService,
            ProfileMapper profileMapper,
            PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.employeeService = employeeService;
        this.profileMapper = profileMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Resolves the currently authenticated EmployeeMaster entity.
     * Falls back to username lookup if employeeRefId claim is not available in context.
     */
    private EmployeeMaster resolveCurrentEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new RuntimeException("Not authenticated");
        }
        String username = auth.getName();

        // Try finding by username first (most reliable from JWT subject)
        return employeeRepository.findByUserNameAndActive(username, 1)
                .orElseThrow(() -> new RuntimeException("Employee not found for user: " + username));
    }

    /**
     * GET /api/profile/me
     * Returns the full profile of the currently authenticated employee.
     */
    @GetMapping("/me")
    public ResponseEntity<ProfileDto> getMyProfile() {
        EmployeeMaster emp = resolveCurrentEmployee();
        return ResponseEntity.ok(profileMapper.toProfileDto(emp));
    }

    /**
     * PUT /api/profile/me
     * Updates editable profile fields for the currently authenticated employee.
     */
    @PutMapping("/me")
    public ResponseEntity<ProfileDto> updateMyProfile(@RequestBody ProfileDto dto) {
        EmployeeMaster emp = resolveCurrentEmployee();
        profileMapper.updateFromProfileDto(dto, emp);
        emp.setModifiedDate(LocalDateTime.now());
        EmployeeMaster saved = employeeRepository.save(emp);
        return ResponseEntity.ok(profileMapper.toProfileDto(saved));
    }

    /**
     * PUT /api/profile/me/password
     * Changes the current employee's password.
     * Body: { "oldPassword": "...", "newPassword": "..." }
     */
    @PutMapping("/me/password")
    public ResponseEntity<?> changeMyPassword(@RequestBody Map<String, String> body) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");

        if (oldPassword == null || newPassword == null || oldPassword.isBlank() || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "oldPassword and newPassword are required"));
        }
        if (newPassword.length() < 4) {
            return ResponseEntity.badRequest().body(Map.of("error", "newPassword must be at least 4 characters"));
        }

        EmployeeMaster emp = resolveCurrentEmployee();

        // Verify old password
        String stored = emp.getPassword();
        if (stored == null) stored = emp.getAppPassword();
        boolean matches;
        if (stored != null && (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$"))) {
            matches = passwordEncoder.matches(oldPassword, stored);
        } else {
            matches = stored != null && stored.equals(oldPassword);
        }
        if (!matches) {
            return ResponseEntity.badRequest().body(Map.of("error", "Current password is incorrect"));
        }

        // Encode and save new password
        emp.setPassword(passwordEncoder.encode(newPassword));
        emp.setModifiedDate(LocalDateTime.now());
        employeeRepository.save(emp);

        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }
}
