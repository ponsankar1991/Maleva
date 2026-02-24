package my.maleva.api.service;

import my.maleva.api.dto.EmployeeMasterDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.model.EmployeeMaster;
import my.maleva.api.repo.EmployeeMasterRepository;
import my.maleva.api.mapper.EmployeeMasterMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeMasterService {

    private final EmployeeMasterRepository repository;
    private final EmployeeMasterMapper mapper;
    private final PasswordEncoder passwordEncoder;

    public EmployeeMasterService(EmployeeMasterRepository repository, EmployeeMasterMapper mapper, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
    }

    public EmployeeMasterDto create(EmployeeMasterDto dto) {
        EmployeeMaster entity = mapper.toEntity(dto);
        // If neither roleId nor role provided in DTO, apply DB default in entity to avoid null insertion
        if (entity.getRoleId() == null) {
            entity.setRoleId(100);
        }
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedDate(now);
        entity.setModifiedDate(now);
        EmployeeMaster saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    public EmployeeMasterDto update(Integer id, EmployeeMasterDto dto) {
        EmployeeMaster existing = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Employee not found: " + id));
        mapper.updateFromDto(dto, existing);
        existing.setModifiedDate(LocalDateTime.now());
        EmployeeMaster saved = repository.save(existing);
        return mapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public EmployeeMasterDto getById(Integer id) {
        EmployeeMaster e = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Employee not found: " + id));
        return mapper.toDto(e);
    }

    @Transactional(readOnly = true)
    public List<EmployeeMasterDto> findAll(String name) {
        List<EmployeeMaster> list;
        if (name == null || name.isBlank()) {
            list = repository.findAll();
        } else {
            list = repository.findByEmployeeNameContainingIgnoreCase(name, Pageable.unpaged()).getContent();
        }
        return list.stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) throw new EntityNotFoundException("Employee not found: " + id);
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean verifyCredentials(String userName, String rawPassword) {
        Optional<EmployeeMaster> maybe = repository.findByUserNameAndActive(userName,1);
        return maybe.map(u -> {
            String stored = u.getPassword();
            if (stored == null) stored = u.getAppPassword();
            if (stored == null) return false;
            // If stored password appears to be BCrypt (starts with $2a$ or $2b$) use encoder, otherwise compare raw
            if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) {
                return passwordEncoder.matches(rawPassword, stored);
            } else {
                return stored.equals(rawPassword);
            }
        }).orElse(false);
    }

    @Transactional(readOnly = true)
    public EmployeeMasterDto findByUserName(String userName) {
        return repository.findByUserNameAndActive(userName,1).map(mapper::toDto).orElse(null);
    }

    /**
     * Get active employees for a company filtered by role IDs.
     * This is a combo list API endpoint that returns employee details filtered by company and role IDs.
     *
     * @param companyRefId The company ID to filter by
     * @param roleId First role ID to filter (optional)
     * @param roleId1 Second role ID to filter (optional)
     * @return List of employees matching the filter criteria with only Active=1 status
     */
    @Transactional(readOnly = true)
    public List<EmployeeMasterDto> getEmployeesByCompanyAndRoles(Integer companyRefId, Integer roleId, Integer roleId1) {
        List<Integer> roleIdList = new java.util.ArrayList<>();

        // Add roleId if provided
        if (roleId != null && roleId > 0) {
            roleIdList.add(roleId);
        }

        // Add roleId1 if provided
        if (roleId1 != null && roleId1 > 0) {
            roleIdList.add(roleId1);
        }

        List<EmployeeMaster> employees;

        // Query employees based on filters
        if (roleIdList.isEmpty()) {
            // If no role filters provided, get all active employees for the company
            employees = repository.findByCompanyRefIdAndActive(companyRefId, 1);
        } else {
            // Get employees with specific role IDs
            employees = repository.findByCompanyAndRoleIds(companyRefId, roleIdList);
        }

        // Convert to DTO and return
        return employees.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
