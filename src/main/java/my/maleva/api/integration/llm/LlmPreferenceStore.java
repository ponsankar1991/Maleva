package my.maleva.api.integration.llm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.module.company.entity.MasterSetting;
import my.maleva.api.module.company.repository.MasterSettingRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Per-company provider choice, kept in the legacy {@code MasterSetting}
 * key/value table so no new schema is needed. {@code LLM_PROVIDER} holds the
 * company default; {@code LLM_PROVIDER:<task>} holds a task override.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LlmPreferenceStore {

    static final String VARIABLE = "LLM_PROVIDER";

    private final MasterSettingRepository repository;

    static String variableName(String task) {
        return task == null || task.isBlank() ? VARIABLE : VARIABLE + ":" + task.trim();
    }

    /** Stored provider key for the task (null task = company default), if any. */
    @Transactional(readOnly = true)
    public Optional<String> get(Integer companyRefId, String task) {
        if (companyRefId == null) {
            return Optional.empty();
        }
        return repository.findFirstByCompanyRefIdAndVariableName(companyRefId, variableName(task))
                .map(MasterSetting::getSValue)
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim);
    }

    /** Blank / null provider key removes the stored preference. */
    @Transactional
    public void put(Integer companyRefId, String task, String providerKey) {
        String name = variableName(task);
        Optional<MasterSetting> existing = repository.findFirstByCompanyRefIdAndVariableName(companyRefId, name);
        if (providerKey == null || providerKey.isBlank()) {
            existing.ifPresent(repository::delete);
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        MasterSetting setting = existing.orElseGet(() -> MasterSetting.builder()
                .companyRefId(companyRefId)
                .variableName(name)
                .status(1)
                .createdDate(now)
                .build());
        setting.setSValue(providerKey.trim());
        setting.setStatus(1);
        setting.setModifiedBy(currentUser());
        setting.setModifiedDate(now);
        repository.save(setting);
        log.info("LLM preference {} for company {} set to {}", name, companyRefId, providerKey);
    }

    private static String currentUser() {
        Authentication auth = SecurityContextHolder.getContext() == null ? null
                : SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            return "SYSTEM";
        }
        String name = auth.getName();
        return name.length() > 50 ? name.substring(0, 50) : name;
    }
}
