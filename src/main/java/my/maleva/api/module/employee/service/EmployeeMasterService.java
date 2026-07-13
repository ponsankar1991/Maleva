package my.maleva.api.module.employee.service;

import my.maleva.api.module.employee.dto.EmployeeMasterDto;
import my.maleva.api.module.employee.dto.EmployeeAllDto;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.module.employee.entity.EmployeeMaster;
import my.maleva.api.module.employee.repository.EmployeeMasterRepository;
import my.maleva.api.module.fleet.mapper.EmployeeMasterMapper;
import my.maleva.api.module.employee.mapper.EmployeeAllMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeMasterService {

    private final EmployeeMasterRepository repository;
    private final EmployeeMasterMapper mapper;
    private final EmployeeAllMapper employeeAllMapper;
    private final PasswordEncoder passwordEncoder;

    public EmployeeMasterService(EmployeeMasterRepository repository, EmployeeMasterMapper mapper, EmployeeAllMapper employeeAllMapper, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.mapper = mapper;
        this.employeeAllMapper = employeeAllMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @CacheEvict(value = "employees", allEntries = true)
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

    @CacheEvict(value = "employees", allEntries = true)
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
    @Cacheable(value = "employees", key = "'name_' + (#name != null ? #name : 'ALL')")
    public List<EmployeeMasterDto> findAll(String name) {
        List<EmployeeMaster> list;
        if (name == null || name.isBlank()) {
            list = repository.findAll();
        } else {
            list = repository.findByEmployeeNameContainingIgnoreCase(name, Pageable.unpaged()).getContent();
        }
        return list.stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @CacheEvict(value = "employees", allEntries = true)
    public void delete(Integer id) {
        if (!repository.existsById(id)) throw new EntityNotFoundException("Employee not found: " + id);
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean verifyCredentials(String userName, String rawPassword) {
        Optional<EmployeeMaster> maybe = repository.findByUserNameAndActive(userName, 1);
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
        return repository.findByUserNameAndActive(userName, 1).map(mapper::toDto).orElse(null);
    }

    /**
     * Get active employees for a company filtered by role IDs.
     * This is a combo list API endpoint that returns employee details filtered by company and role IDs.
     *
     * @param companyRefId The company ID to filter by
     * @param roleId       First role ID to filter (optional)
     * @param roleId1      Second role ID to filter (optional)
     * @return List of employees matching the filter criteria with only Active=1 status
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "employees", key = "'roles_' + #companyRefId + '_' + (#roleId != null ? #roleId : 0) + '_' + (#roleId1 != null ? #roleId1 : 0)")
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

    /**
     * Get all active employees for a company with optional employee type filter.
     * This endpoint corresponds to the .NET SelectEmployeeAll method.
     * Returns employees ordered by name, excluding those with Active=2.
     *
     * @param companyRefId The company ID to filter by
     * @param type         The employee type to filter by (optional). If empty, "ALL", or null, returns all employees
     * @return List of employees ordered by employee name
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "employees", key = "'type_' + #companyRefId + '_' + (#type != null ? #type : 'ALL')")
    public List<EmployeeAllDto> selectEmployeeAll(Integer companyRefId, String type) {
        List<EmployeeMaster> employees;

        // Check if type filter should be applied
        if (type != null && !type.trim().isEmpty() && !type.equalsIgnoreCase("ALL")) {
            // Get employees filtered by company and employee type, excluding Active=2
            employees = repository.findAllActiveByCompanyRefIdAndEmployeeType(companyRefId, type);
        } else {
            // Get all employees for company, excluding Active=2
            employees = repository.findAllActiveByCompanyRefId(companyRefId);
        }

        // Convert to EmployeeAllDto and return (ordered by employee name from query)
        return employees.stream()
                .map(employeeAllMapper::toDto)
                .collect(Collectors.toList());
    }
    /**
     * Search employees with dynamic filtering matching legacy SelectEmployee endpoint.
     *
     * @param request Search parameters
     * @return Search response with data and total count (if applicable)
     */
    @Transactional(readOnly = true)
    public my.maleva.api.module.employee.dto.EmployeeSearchResponse searchEmployees(my.maleva.api.module.employee.dto.EmployeeSearchRequest request) {
        List<EmployeeAllDto> employees = repository.searchEmployees(request);
        Integer totalCount = 0;
        
        String keyword = request.getKeyword();
        if (keyword == null || keyword.isEmpty()) {
            totalCount = repository.countSearchEmployees(request);
        }
        
        return my.maleva.api.module.employee.dto.EmployeeSearchResponse.builder()
                .data1(employees)
                .data4(totalCount)
                .build();
    }
}
