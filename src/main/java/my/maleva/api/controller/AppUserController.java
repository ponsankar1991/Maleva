package my.maleva.api.controller;

import my.maleva.api.dto.AppUserDto;
import my.maleva.api.model.AppUser;
import my.maleva.api.repo.AppUserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.net.URI;

@RestController
@RequestMapping("/api/users")
public class AppUserController {

    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public AppUserController(AppUserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AppUserDto dto) {
        if (dto.getUserId() == null || dto.getPassword() == null) {
            return ResponseEntity.badRequest().body("userId and password required");
        }

        if (repository.findByUserId(dto.getUserId()).isPresent()) {
            return ResponseEntity.status(409).body("userId already exists");
        }

        AppUser u = AppUser.builder()
                .companyRefId(dto.getCompanyRefId())
                .userId(dto.getUserId())
                .password(passwordEncoder.encode(dto.getPassword()))
                .priv(dto.getPriv())
                .createdDate(LocalDateTime.now())
                .modifiedDate(LocalDateTime.now())
                .modifiedBy(dto.getModifiedBy())
                .active(dto.getActive() == null ? 1 : dto.getActive())
                .build();

        AppUser saved = repository.save(u);

        AppUserDto resp = AppUserDto.builder()
                .id(saved.getId())
                .companyRefId(saved.getCompanyRefId())
                .userId(saved.getUserId())
                .priv(saved.getPriv())
                .createdDate(saved.getCreatedDate())
                .modifiedDate(saved.getModifiedDate())
                .modifiedBy(saved.getModifiedBy())
                .active(saved.getActive())
                .build();

        return ResponseEntity.created(URI.create("/api/users/" + saved.getId())).body(resp);
    }
}
