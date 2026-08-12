package my.maleva.api.module.employee.service;

import my.maleva.api.module.employee.dto.EmployeeMasterDto;
import my.maleva.api.module.employee.dto.EmployeeAllDto;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.module.employee.entity.EmployeeMaster;
import my.maleva.api.module.employee.repository.EmployeeMasterRepository;
import my.maleva.api.module.fleet.mapper.EmployeeMasterMapper;
import my.maleva.api.module.employee.mapper.EmployeeAllMapper;
import my.maleva.api.module.accountsgroupmaster.repository.AccountsGroupMasterRepository;
import my.maleva.api.module.accountsgroupmaster.entity.AccountsGroupMaster;
import my.maleva.api.common.dto.ApiResponse;
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

import my.maleva.api.module.employee.repository.EmployeeCapabilityRepository;
import my.maleva.api.module.employee.entity.EmployeeCapability;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import my.maleva.api.module.leave.repository.LeaveRequestRepository;
import my.maleva.api.module.leave.mapper.LeaveRequestMapper;
import my.maleva.api.module.leave.dto.response.LeaveRequestResponseDto;

@Service
@Transactional
public class EmployeeMasterService {

    private final EmployeeMasterRepository repository;
    private final EmployeeMasterMapper mapper;
    private final EmployeeAllMapper employeeAllMapper;
    private final PasswordEncoder passwordEncoder;
    private final AccountsGroupMasterRepository accountsGroupMasterRepository;
    private final EmployeeCapabilityRepository employeeCapabilityRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveRequestMapper leaveRequestMapper;

    public EmployeeMasterService(EmployeeMasterRepository repository, EmployeeMasterMapper mapper, EmployeeAllMapper employeeAllMapper, PasswordEncoder passwordEncoder, AccountsGroupMasterRepository accountsGroupMasterRepository, EmployeeCapabilityRepository employeeCapabilityRepository, LeaveRequestRepository leaveRequestRepository, LeaveRequestMapper leaveRequestMapper) {
        this.repository = repository;
        this.mapper = mapper;
        this.employeeAllMapper = employeeAllMapper;
        this.passwordEncoder = passwordEncoder;
        this.accountsGroupMasterRepository = accountsGroupMasterRepository;
        this.employeeCapabilityRepository = employeeCapabilityRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.leaveRequestMapper = leaveRequestMapper;
    }

    @CacheEvict(value = "employees", allEntries = true)
    public EmployeeMasterDto create(EmployeeMasterDto dto) {
        // Proxy to the robust SP-equivalent logic
        bulkUpsertEmployees(List.of(dto), dto.getCompanyRefId());
        
        // Fetch the newly created entity by name to return it
        EmployeeMaster saved = repository.findFirstByCompanyRefIdAndEmployeeNameIgnoreCase(dto.getCompanyRefId(), dto.getEmployeeName())
                .orElseThrow(() -> new EntityNotFoundException("Employee creation failed"));
        
        return mapper.toDto(saved);
    }

    @CacheEvict(value = "employees", allEntries = true)
    public EmployeeMasterDto update(Integer id, EmployeeMasterDto dto) {
        dto.setId(id); // Ensure the ID is set for update logic
        // Proxy to the robust SP-equivalent logic
        bulkUpsertEmployees(List.of(dto), dto.getCompanyRefId());
        
        EmployeeMaster saved = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee update failed"));
                
        return mapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public EmployeeMasterDto getById(Integer id) {
        EmployeeMaster e = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Employee not found: " + id));
        EmployeeMasterDto dto = mapper.toDto(e);
        
        List<Integer> capIds = employeeCapabilityRepository.findByEmployeeIdAndIsActiveTrue(id)
                .stream()
                .map(my.maleva.api.module.employee.entity.EmployeeCapability::getCapabilityId)
                .collect(Collectors.toList());
        dto.setCapabilityIds(capIds);
        
        return dto;
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

    @Transactional
    @CacheEvict(value = "employees", allEntries = true)
    public void delete(Integer id) {
        EmployeeMaster employee = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found: " + id));
        
        // Soft delete: set Active = 2
        employee.setActive(2);
        employee.setModifiedDate(LocalDateTime.now());
        repository.save(employee);
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
     * Get all boarding officers for a company.
     * A boarding officer is defined as an employee with active status and either:
     * - roleId is 500 or 600
     * - has active capability ID 5 or 6
     *
     * @param companyRefId The company ID
     * @return List of boarding officers
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "employees", key = "'boarding_officers_' + #companyRefId")
    public List<EmployeeAllDto> getBoardingOfficers(Integer companyRefId) {
        List<EmployeeMaster> employees = repository.findBoardingOfficers(companyRefId);
        
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
        
        // Populate capabilityIds for each employee returned in the search
        for (EmployeeAllDto emp : employees) {
            List<Integer> capIds = employeeCapabilityRepository.findByEmployeeIdAndIsActiveTrue(emp.getId())
                    .stream()
                    .map(my.maleva.api.module.employee.entity.EmployeeCapability::getCapabilityId)
                    .collect(Collectors.toList());
            emp.setCapabilityIds(capIds);
        }
        
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

    /**
     * Bulk upsert employees matching the legacy SP_Employee stored procedure logic.
     * Manages AccountsGroupMaster creation and synchronization automatically.
     *
     * @param employees    List of employees to insert or update
     * @param companyRefId The company ID
     * @return ApiResponse containing the name and ID of the last processed employee (matching legacy .NET return format)
     */
    @Transactional
    @CacheEvict(value = "employees", allEntries = true)
    public ApiResponse<String> bulkUpsertEmployees(List<EmployeeMasterDto> employees, Integer companyRefId) {
        String lastProcessedName = "";
        Integer lastProcessedId = 0;

        // Fetch parent AccountsGroupMaster ID for EMPLOYEES
        AccountsGroupMaster parentGroup = accountsGroupMasterRepository
                .findFirstByAccountNameAndAccountCodeAndCompanyRefIdAndActive("EMPLOYEES", "EMP", companyRefId, 1)
                .orElseThrow(() -> new EntityNotFoundException("Parent AccountsGroupMaster EMPLOYEES not found for company " + companyRefId));

        Integer parentId = parentGroup.getId();

        // Get max CNumber to start incrementing for new inserts
        Integer currentMaxCNumber = repository.findMaxCNumberByCompanyRefId(companyRefId);
        if (currentMaxCNumber == null) currentMaxCNumber = 0;

        for (EmployeeMasterDto dto : employees) {
            String empName = dto.getEmployeeName() != null ? dto.getEmployeeName().toUpperCase() : "";
            Integer id = dto.getId() != null ? dto.getId() : 0;

            // PREVENT DOUBLE-CLICKS (DUPLICATES)
            // If the frontend sends id=0 but the exact same employee name already exists in this company,
            // we safely switch to UPDATE mode to prevent database duplication.
            if (id == 0 && !empName.isEmpty()) {
                Optional<EmployeeMaster> existingByName = repository.findFirstByCompanyRefIdAndEmployeeNameIgnoreCase(companyRefId, empName);
                if (existingByName.isPresent()) {
                    id = existingByName.get().getId();
                }
            }

            final Integer targetId = id;
            EmployeeMaster entity;

            if (targetId == 0) {
                // INSERT LOGIC
                currentMaxCNumber++;
                String paddedCode = String.format("E%09d", currentMaxCNumber);

                // Calculate RowNumber (AccountCode) for new account group
                int childCount = accountsGroupMasterRepository.countByParentIdAndCompanyRefId(parentId, companyRefId);
                String rowNumber = "EMP-" + (childCount + 1);

                // 1. Insert into AccountsGroupMaster
                AccountsGroupMaster newAccount = AccountsGroupMaster.builder()
                        .companyRefId(companyRefId)
                        .accountName(empName)
                        .parentId(parentId)
                        .editmode(1)
                        .noChild(1)
                        .createdDate(LocalDateTime.now())
                        .modifiedDate(LocalDateTime.now())
                        .modifiedBy("SA")
                        .active(1)
                        .accountCode(rowNumber)
                        .build();

                AccountsGroupMaster savedAccount = accountsGroupMasterRepository.save(newAccount);

                // 2. Map and Insert into EmployeeMaster
                entity = mapper.toEntity(dto);
                entity.setId(null); // IMPORTANT: Force ID to null so JPA knows to use IDENTITY generation
                entity.setCompanyRefId(companyRefId);
                entity.setAccountRefid(savedAccount.getId());
                entity.setCNumber(currentMaxCNumber);
                entity.setCNumberDisplay(paddedCode);
                entity.setCreatedDate(LocalDateTime.now());
                entity.setModifiedDate(LocalDateTime.now());
                entity.setModifiedBy("SA");
                
                // Fallback for role defaults if empty
                if (entity.getRoleId() == null) entity.setRoleId(100);
                if (entity.getPermisionId() == null) entity.setPermisionId(0);

            } else {
                // UPDATE LOGIC
                entity = repository.findById(targetId)
                        .orElseThrow(() -> new EntityNotFoundException("Employee not found: " + targetId));

                // Map updates from DTO
                mapper.updateFromDto(dto, entity);
                
                entity.setCompanyRefId(companyRefId); // Ensure company ID integrity
                entity.setModifiedDate(LocalDateTime.now());

                // Update associated AccountsGroupMaster
                Integer accountRefid = entity.getAccountRefid();
                if (accountRefid != null) {
                    accountsGroupMasterRepository.findByIdAndCompanyRefId(accountRefid, companyRefId)
                            .ifPresent(account -> {
                                account.setAccountName(empName);
                                accountsGroupMasterRepository.save(account);
                            });
                }
            }

            // Apply strict formatting / uppercasing consistent with legacy SP
            entity.setEmployeeName(empName);
            if (entity.getEmployeecurrency() != null) entity.setEmployeecurrency(entity.getEmployeecurrency().toUpperCase());
            if (entity.getUserName() != null) entity.setUserName(entity.getUserName().toUpperCase());
            if (entity.getEmployeeType() != null) entity.setEmployeeType(entity.getEmployeeType().toUpperCase());
            if (entity.getAddress1() != null) entity.setAddress1(entity.getAddress1().toUpperCase());
            if (entity.getAddress2() != null) entity.setAddress2(entity.getAddress2().toUpperCase());
            if (entity.getAddress3() != null) entity.setAddress3(entity.getAddress3().toUpperCase());
            if (entity.getCity() != null) entity.setCity(entity.getCity().toUpperCase());
            if (entity.getState() != null) entity.setState(entity.getState().toUpperCase());
            if (entity.getZipcode() != null) entity.setZipcode(entity.getZipcode().toUpperCase());
            if (entity.getCountry() != null) entity.setCountry(entity.getCountry().toUpperCase());
            if (entity.getGstNo() != null) entity.setGstNo(entity.getGstNo().toUpperCase());
            if (entity.getPersonId() != null) entity.setPersonId(entity.getPersonId().toUpperCase());
            
            // Ensure Active is set
            if (entity.getActive() == null) entity.setActive(1);

            EmployeeMaster savedEntity = repository.save(entity);
            lastProcessedId = savedEntity.getId();
            lastProcessedName = empName;

            // --- CAPABILITY UPSERT LOGIC ---
            upsertCapabilities(lastProcessedId, dto.getCapabilityIds());
            // --- END CAPABILITY UPSERT LOGIC ---
        }

        // Return format to match legacy C# response: Data1 = AccountName, Data2 = Id
        ApiResponse<String> response = ApiResponse.success(lastProcessedName, "Employee successfully created/updated.");
        response.setData2(lastProcessedId);
        return response;
    }

    private void upsertCapabilities(Integer employeeId, List<Integer> capabilityIds) {
        if (capabilityIds == null) {
            return; // Null means do not touch capabilities
        }

        // Fetch ALL existing capabilities (both active and inactive) for this employee
        List<EmployeeCapability> allCaps = employeeCapabilityRepository.findByEmployeeId(employeeId);
        
        Set<Integer> selectedCapIds = new HashSet<>(capabilityIds);

        for (EmployeeCapability ec : allCaps) {
            if (selectedCapIds.contains(ec.getCapabilityId())) {
                // The capability is selected. If it was inactive, reactivate it and update granted date
                if (!ec.getIsActive()) {
                    ec.setIsActive(true);
                    ec.setGrantedDate(LocalDateTime.now());
                    employeeCapabilityRepository.save(ec);
                }
                // Remove from selected set so we know it's already handled
                selectedCapIds.remove(ec.getCapabilityId());
            } else {
                // The capability is NOT selected. If it is currently active, soft-delete it
                if (ec.getIsActive()) {
                    ec.setIsActive(false);
                    employeeCapabilityRepository.save(ec);
                }
            }
        }

        // Any IDs remaining in selectedCapIds are completely new and need inserting
        for (Integer newCapId : selectedCapIds) {
            EmployeeCapability ec = EmployeeCapability.builder()
                    .employeeId(employeeId)
                    .capabilityId(newCapId)
                    .grantedDate(LocalDateTime.now())
                    .grantedBy("SA")
                    .isActive(true)
                    .build();
            employeeCapabilityRepository.save(ec);
        }
    }

    @Transactional(readOnly = true)
    public List<EmployeeMasterDto> getAllEmployeeDetails(Integer companyId) {
        if (companyId == null) {
            throw new IllegalArgumentException("Company ID is required");
        }

        List<EmployeeMaster> results = repository.findByCompanyRefIdAndActive(companyId, 1);
        List<EmployeeMasterDto> dtos = results.stream().map(mapper::toDto).collect(Collectors.toList());

        // Optimize N+1 Query Problem: Collect all AccountRefid and fetch at once
        List<Integer> accountRefIds = results.stream()
                .map(EmployeeMaster::getAccountRefid)
                .filter(id -> id != null && id > 0)
                .distinct()
                .collect(Collectors.toList());

        if (!accountRefIds.isEmpty()) {
            java.util.Map<Integer, String> accountMap = accountsGroupMasterRepository.findByIdInAndCompanyRefId(accountRefIds, companyId)
                    .stream()
                    .collect(Collectors.toMap(my.maleva.api.module.accountsgroupmaster.entity.AccountsGroupMaster::getId, my.maleva.api.module.accountsgroupmaster.entity.AccountsGroupMaster::getAccountCode));

            for (EmployeeMasterDto dto : dtos) {
                if (dto.getAccountRefid() != null && accountMap.containsKey(dto.getAccountRefid())) {
                    dto.setAccountCode(accountMap.get(dto.getAccountRefid()));
                }
            }
        }

        // Optimize N+1 Query Problem for Leaves (ApplicantType = 1 for Employee)
        List<Integer> employeeIds = dtos.stream().map(EmployeeMasterDto::getId).collect(Collectors.toList());
        if (!employeeIds.isEmpty()) {
            java.time.LocalDateTime startDate = java.time.LocalDateTime.now().with(java.time.LocalTime.MIN);
            java.time.LocalDateTime endDate = startDate.plusDays(3).with(java.time.LocalTime.MAX);

            List<my.maleva.api.module.leave.entity.LeaveRequestMaster> leaves = leaveRequestRepository.findOverlappingLeavesForApplicants(1, employeeIds, 1, startDate, endDate);
            if (!leaves.isEmpty()) {
                List<LeaveRequestResponseDto> leaveDtos = leaveRequestMapper.toResponseDtoList(leaves);
                java.util.Map<Integer, List<LeaveRequestResponseDto>> leavesByEmployee = leaveDtos.stream()
                        .collect(Collectors.groupingBy(LeaveRequestResponseDto::getApplicantRefId));
                
                for (EmployeeMasterDto dto : dtos) {
                    dto.setLeaves(leavesByEmployee.getOrDefault(dto.getId(), new ArrayList<>()));
                }
            } else {
                for (EmployeeMasterDto dto : dtos) {
                    dto.setLeaves(new ArrayList<>());
                }
            }
        }

        return dtos;
    }
}
