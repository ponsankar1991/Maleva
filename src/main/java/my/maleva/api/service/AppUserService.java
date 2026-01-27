package my.maleva.api.service;

import my.maleva.api.dto.AppUserDto;
import my.maleva.api.model.AppUser;
import my.maleva.api.repo.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class AppUserService {

    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(AppUserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public AppUserDto register(AppUserDto dto) {
        // Check existing userId
        repository.findByUserId(dto.getUserId()).ifPresent(u -> {
            throw new RuntimeException("UserId already exists: " + dto.getUserId());
        });

        AppUser user = AppUser.builder()
                .companyRefId(dto.getCompanyRefId())
                .userId(dto.getUserId())
                .password(passwordEncoder.encode(dto.getPassword()))
                .priv(dto.getPriv())
                .createdDate(LocalDateTime.now())
                .modifiedDate(LocalDateTime.now())
                .modifiedBy(dto.getModifiedBy())
                .active(dto.getActive() == null ? 1 : dto.getActive())
                .build();

        AppUser saved = repository.save(user);
        return AppUserDto.builder()
                .id(saved.getId())
                .companyRefId(saved.getCompanyRefId())
                .userId(saved.getUserId())
                .priv(saved.getPriv())
                .createdDate(saved.getCreatedDate())
                .modifiedDate(saved.getModifiedDate())
                .modifiedBy(saved.getModifiedBy())
                .active(saved.getActive())
                .build();
    }

    @Transactional(readOnly = true)
    public AppUserDto findByUserId(String userId) {
        return repository.findByUserId(userId)
                .map(u -> AppUserDto.builder()
                        .id(u.getId())
                        .companyRefId(u.getCompanyRefId())
                        .userId(u.getUserId())
                        .priv(u.getPriv())
                        .createdDate(u.getCreatedDate())
                        .modifiedDate(u.getModifiedDate())
                        .modifiedBy(u.getModifiedBy())
                        .active(u.getActive())
                        .build())
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public boolean verifyCredentials(String userId, String rawPassword) {
        return repository.findByUserId(userId)
                .map(u -> passwordEncoder.matches(rawPassword, u.getPassword()))
                .orElse(false);
    }
}
