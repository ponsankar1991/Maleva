package my.maleva.api.module.supplier.service.impl;

import my.maleva.api.module.supplier.dto.SupplierDto;
import my.maleva.api.module.supplier.dto.SupplierSearchResponse;
import my.maleva.api.module.supplier.dto.SupplierComboList;
import my.maleva.api.module.supplier.dto.SupplierExtendedResponse;
import my.maleva.api.module.supplier.mapper.SupplierMapper;
import my.maleva.api.module.supplier.entity.Supplier;
import my.maleva.api.module.supplier.repository.SupplierRepository;
import my.maleva.api.module.supplier.service.SupplierQneService;
import my.maleva.api.module.supplier.service.SupplierService;
import my.maleva.api.common.dto.ResponseViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * SupplierServiceImpl - Implementation for Supplier service
 * Handles comprehensive supplier/vendor management with audit trail
 */
@Service
public class SupplierServiceImpl implements SupplierService {

    private static final Logger logger = LoggerFactory.getLogger(SupplierServiceImpl.class);

    @Autowired
    private SupplierRepository repository;

    @Autowired
    private SupplierMapper mapper;

    @Autowired
    private SupplierQneService qneService;

    @Override
    public List<SupplierDto> getByCompanyRefId(Integer companyRefId) {
        logger.info("Fetching Supplier for company: {}", companyRefId);
        return repository.findByCompanyRefId(companyRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SupplierDto> getBySupplierName(String supplierName) {
        logger.info("Fetching Supplier by name: {}", supplierName);
        return repository.findBySupplierName(supplierName).map(mapper::toDto);
    }

    @Override
    public Optional<SupplierDto> getByCNumber(Integer cNumber, Integer companyRefId) {
        logger.info("Fetching Supplier by C Number: {} for company: {}", cNumber, companyRefId);
        return repository.findByCNumberAndCompanyRefId(cNumber, companyRefId).map(mapper::toDto);
    }

    @Override
    public List<SupplierDto> getActiveByCompany(Integer companyRefId) {
        logger.info("Fetching active Supplier for company: {}", companyRefId);
        return repository.findByCompanyRefIdAndActive(companyRefId, 1)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SupplierDto> getBySupplierType(String supplierType) {
        logger.info("Fetching Supplier for type: {}", supplierType);
        return repository.findBySupplierType(supplierType)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SupplierDto> getByCountry(String country) {
        logger.info("Fetching Supplier for country: {}", country);
        return repository.findByCountry(country)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SupplierDto> getByCity(String city) {
        logger.info("Fetching Supplier for city: {}", city);
        return repository.findByCity(city)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SupplierDto> getByEmail(String email) {
        logger.info("Fetching Supplier by email: {}", email);
        return repository.findByEmail(email).map(mapper::toDto);
    }

    @Override
    public Optional<SupplierDto> getByGstNo(String gstNo) {
        logger.info("Fetching Supplier by GST No: {}", gstNo);
        return repository.findByGstNo(gstNo).map(mapper::toDto);
    }

    @Override
    public Optional<SupplierDto> getById(Integer id) {
        logger.info("Fetching Supplier by ID: {}", id);
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional
    public SupplierDto create(SupplierDto dto) {
        logger.info("Creating new Supplier");
        validateSupplierData(dto);
        Supplier entity = mapper.toEntity(dto);

        // Set default values as per table schema
        LocalDateTime now = LocalDateTime.now();
        if (entity.getCreatedDate() == null) {
            entity.setCreatedDate(now);
        }
        if (entity.getModifiedDate() == null) {
            entity.setModifiedDate(now);
        }
        if (entity.getModifiedBy() == null) {
            entity.setModifiedBy("SYSTEM");
        }
        if (entity.getActive() == null) {
            entity.setActive(1);
        }
        if (entity.getCNumber() == null) {
            entity.setCNumber(1);
        }
        if (entity.getOpeningBalance() == null) {
            entity.setOpeningBalance(BigDecimal.ZERO);
        }
        if (entity.getSupplierType() == null) {
            entity.setSupplierType("VENDOR");
        }
        if (entity.getAccountRefid() == null) {
            entity.setAccountRefid(1);
        }

        Supplier saved = repository.save(entity);
        logger.info("Supplier created with ID: {}", saved.getId());
        qneService.pushCreatedAfterCommit(saved);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public SupplierDto update(Integer id, SupplierDto dto) {
        logger.info("Updating Supplier with ID: {}", id);
        validateSupplierData(dto);

        Supplier entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found: " + id));

        // Preserve created date and update modified date
        LocalDateTime now = LocalDateTime.now();
        entity.setModifiedDate(now);
        entity.setModifiedBy("SYSTEM");

        mapper.updateEntityFromDto(dto, entity);
        Supplier updated = repository.save(entity);
        logger.info("Supplier updated with ID: {}", updated.getId());
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        logger.info("Deleting Supplier with ID: {}", id);
        if (repository.existsById(id)) {
            repository.deleteById(id);
            logger.info("Supplier deleted with ID: {}", id);
            return true;
        }
        return false;
    }

    @Override
    public long countByCompanyRefId(Integer companyRefId) {
        logger.info("Counting Supplier for company: {}", companyRefId);
        return repository.countByCompanyRefId(companyRefId);
    }

    @Override
    public long countActiveByCompany(Integer companyRefId) {
        logger.info("Counting active Supplier for company: {}", companyRefId);
        return repository.countByCompanyRefIdAndActive(companyRefId, 1);
    }

    @Override
    public void validateSupplierData(SupplierDto dto) {
        if (dto.getCompanyRefId() == null) {
            throw new RuntimeException("Company Reference ID is required");
        }
        if (dto.getSupplierName() == null || dto.getSupplierName().trim().isEmpty()) {
            throw new RuntimeException("Supplier Name is required");
        }
        if (dto.getCNumberDisplay() == null || dto.getCNumberDisplay().trim().isEmpty()) {
            throw new RuntimeException("C Number Display is required");
        }
        if (dto.getCNumber() == null) {
            throw new RuntimeException("C Number is required");
        }
        if (dto.getSymbolRefid() == null) {
            throw new RuntimeException("Symbol Reference ID is required");
        }
        if (dto.getPaymentTermsRefid() == null) {
            throw new RuntimeException("Payment Terms Reference ID is required");
        }
        if (dto.getSupplierType() == null || dto.getSupplierType().trim().isEmpty()) {
            throw new RuntimeException("Supplier Type is required");
        }
        if (dto.getAccountRefid() == null) {
            throw new RuntimeException("Account Reference ID is required");
        }
    }

    @Override
    @Transactional
    public SupplierDto activateSupplier(Integer id) {
        logger.info("Activating Supplier with ID: {}", id);
        Supplier entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found: " + id));

        entity.setActive(1);
        entity.setModifiedDate(LocalDateTime.now());
        entity.setModifiedBy("SYSTEM");
        Supplier updated = repository.save(entity);

        logger.info("Supplier activated with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public SupplierDto deactivateSupplier(Integer id) {
        logger.info("Deactivating Supplier with ID: {}", id);
        Supplier entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found: " + id));

        entity.setActive(0);
        entity.setModifiedDate(LocalDateTime.now());
        entity.setModifiedBy("SYSTEM");
        Supplier updated = repository.save(entity);

        logger.info("Supplier deactivated with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    public boolean existsBySupplierName(String supplierName) {
        logger.info("Checking if Supplier exists with name: {}", supplierName);
        return repository.existsBySupplierName(supplierName);
    }

    /**
     * Process Supplier using SP_Supplier logic
     * Supports both INSERT (id = 0) and UPDATE (id > 0)
     */
    @Transactional
    public SupplierDto processSupplierBatch(SupplierDto dto) {
        logger.info("Processing Supplier with SP_Supplier logic");

        if (dto.getId() == null || dto.getId() == 0) {
            // New record - INSERT with default values
            logger.info("Processing INSERT operation");
            return create(dto);
        } else {
            // Existing record - UPDATE with modified date
            logger.info("Processing UPDATE operation for ID: {}", dto.getId());
            return update(dto.getId(), dto);
        }
    }

    /**
     * Select Supplier with pagination and search filters
     * Equivalent to .NET SelectSupplier method
     *
     * @param comid Company ID
     * @param startindex Starting index for pagination (use -1 for last page)
     * @param pageCount Records per page
     * @param keyword Search keyword
     * @param column Search column (SupplierName, MobileNo, Id, All)
     * @param type Supplier type filter (empty/ALL/null means no filter)
     * @return SupplierSearchResponse with paginated results
     */
    @Override
    public SupplierSearchResponse selectSupplier(Integer comid, Integer startindex, Integer pageCount,String keyword, String column, String type) {
        try {
            logger.info("Selecting Suppliers - comid: {}, startindex: {}, pageCount: {}, keyword: {}, column: {}, type: {}", comid, startindex, pageCount, keyword, column, type);

            // Normalize inputs
            comid = comid != null ? comid : 6;
            startindex = startindex != null ? startindex : 0;
            pageCount = pageCount != null ? pageCount : 10;
            keyword = keyword != null ? keyword : "";
            column = column != null ? column : "";
            type = (type != null && !type.isEmpty() && !"ALL".equalsIgnoreCase(type)) ? type : "";

            List<Supplier> resultList;
            Integer totalCount = 0;

            // Step 1: Determine if we're using type filter
            boolean hasTypeFilter = !type.isEmpty();

            // Step 2: Execute search based on keyword and column
            if (keyword.isEmpty()) {
                // No keyword - get all records
                if (hasTypeFilter) {
                    resultList = repository.findAllByCompanyIdWithType(comid, type);
                    totalCount = repository.countByCompanyWithType(comid, type);
                } else {
                    resultList = repository.findAllByCompanyId(comid);
                    totalCount = repository.countByCompany(comid);
                }
            } else {
                // With keyword - search based on column
                if ("SupplierName".equalsIgnoreCase(column)) {
                    if (hasTypeFilter) {
                        resultList = repository.searchBySupplierNameWithType(comid, keyword, type);
                    } else {
                        resultList = repository.searchBySupplierName(comid, keyword);
                    }
                } else if ("MobileNo".equalsIgnoreCase(column)) {
                    if (hasTypeFilter) {
                        resultList = repository.searchByMobileNoWithType(comid, keyword, type);
                    } else {
                        resultList = repository.searchByMobileNo(comid, keyword);
                    }
                } else if ("Id".equalsIgnoreCase(column)) {
                    try {
                        Integer keywordId = Integer.parseInt(keyword.trim());
                        if (hasTypeFilter) {
                            resultList = repository.searchByIdWithType(comid, keywordId, type);
                        } else {
                            resultList = repository.searchById(comid, keywordId);
                        }
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid ID format for search: {}", keyword);
                        resultList = new ArrayList<>();
                    }
                } else if ("All".equalsIgnoreCase(column)) {
                    // Search in all fields (supplier name, mobile, etc.)
                    if (hasTypeFilter) {
                        resultList = repository.findAllByCompanyIdWithType(comid, type);
                        totalCount = repository.countByCompanyWithType(comid, type);
                    } else {
                        resultList = repository.findAllByCompanyId(comid);
                        totalCount = repository.countByCompany(comid);
                    }
                } else {
                    logger.warn("Invalid column specified: {}", column);
                    return SupplierSearchResponse.builder()
                            .ok(false)
                            .message("Invalid column specified")
                            .data(new ArrayList<>())
                            .count(0)
                            .build();
                }
                totalCount = resultList.size();
            }

            // Step 3: Handle pagination
            // In .NET, pagination is ONLY applied when keyword is empty
            List<Supplier> paginatedList;
            if (keyword.isEmpty()) {
                // If startindex is -1, calculate last page
                if (startindex == -1) {
                    if (totalCount > pageCount && pageCount > 0) {
                        double div = (double) totalCount / pageCount;
                        div = Math.floor(div);
                        startindex = (int) (div * pageCount);
                    } else {
                        startindex = 0;
                    }
                }

                // Apply pagination
                if (pageCount > 0) {
                    int endIndex = Math.min(startindex + pageCount, resultList.size());
                    paginatedList = resultList.subList(startindex, endIndex);
                } else {
                    paginatedList = new ArrayList<>();
                }
            } else {
                paginatedList = resultList; // No pagination when searching by keyword
            }

            // Step 4: Convert to DTOs and sort by created date
            List<SupplierDto> supplierDtos = paginatedList.stream()
                    .map(mapper::toDto)
                    .sorted((a, b) -> {
                        if (a.getCreatedDate() == null || b.getCreatedDate() == null) {
                            return 0;
                        }
                        return b.getCreatedDate().compareTo(a.getCreatedDate()); // Descending order (newest first)
                    })
                    .collect(Collectors.toList());

            // Step 5: Calculate pagination info
            Integer totalPages = (int) Math.ceil((double) totalCount / pageCount);
            Integer currentPage = (int) Math.ceil((double) (startindex + 1) / pageCount);

            logger.info("Successfully fetched {} suppliers out of {} total", paginatedList.size(), totalCount);

            return SupplierSearchResponse.builder()
                    .ok(true)
                    .message("Success")
                    .data(supplierDtos)
                    .count(totalCount)
                    .totalPages(totalPages)
                    .currentPage(currentPage)
                    .build();

        } catch (Exception ex) {
            logger.error("Error in selectSupplier", ex);
            return SupplierSearchResponse.builder()
                    .ok(false)
                    .message(ex.getMessage())
                    .data(new ArrayList<>())
                    .count(0)
                    .build();
        }
    }

    /**
     * Get Supplier combo list for dropdowns/comboboxes
     * Equivalent to .NET GetSupplier method
     *
     * SQL Query Equivalent:
     * SELECT S.Id, (S.SupplierName + '-' + S.MobileNo) as AccountName
     * FROM Supplier S
     * WHERE S.CompanyRefId = :comid AND S.Active = 1
     * AND (IF :type is not null/empty/ALL: S.SupplierType IN (:type, 'ALL'))
     *
     * @param comid Company ID
     * @param type Supplier Type filter (null/""/ALL for no type filter)
     * @return ResponseViewModel with List<SupplierComboList>
     */
    // Deliberately NOT @Transactional: this method catches and wraps its own
    // errors, and inside a transaction a repository failure marks it
    // rollback-only — the caught error is then replaced at commit by an opaque
    // "Transaction silently rolled back" 500. The read rides the repository's
    // own transaction.
    @Override
    public ResponseViewModel getSupplier(Integer comid, String type) {
        logger.info("Fetching Supplier dropdown list for company: {} with type filter: {}", comid, type);

        try {
            // Validate input
            if (comid == null || comid <= 0) {
                logger.warn("Invalid comid: {}", comid);
                return ResponseViewModel.error("Invalid Company ID provided", 400);
            }

            // Fetch supplier combo list based on type filter
            List<SupplierComboList> result;

            // Check if type filter should be applied
            if (type != null && !type.trim().isEmpty() && !type.equalsIgnoreCase("ALL")) {
                logger.debug("Fetching suppliers with type filter: {}", type);
                result = repository.getSupplierComboListWithType(comid, type);
            } else {
                logger.debug("Fetching suppliers without type filter");
                result = repository.getSupplierComboList(comid);
            }

            logger.info("Successfully retrieved {} supplier records for company: {}", result.size(), comid);

            // Return success response
            return ResponseViewModel.success(
                    result,
                    "Success",
                    200
            );

        } catch (Exception ex) {
            logger.error("Error fetching supplier combo list for company: " + comid, ex);
            return ResponseViewModel.error(
                    "Error retrieving supplier list: " + ex.getMessage(),
                    500
            );
        }
    }

    /**
     * Select All Suppliers with joined master data
     * Equivalent to .NET SelectSupplierAll method
     *
     * Fetches all suppliers for a company with joined data from:
     * - SymbolMaster (SName)
     * - PaymentTermsMaster (TermsName)
     * - AccountsGroupMaster (AccountCode)
     *
     * Filters:
     * - CompanyRefId = comid
     * - Active != 2
     *
     * Sorted by SupplierName
     *
     * @param comid Company Reference ID
     * @return List of SupplierExtendedResponse with all supplier details and joined master data
     */
    // NOT @Transactional for the same reason as getSupplier: the catch wraps
    // the error, which a transaction would replace with an opaque rollback 500.
    @Override
    public List<SupplierExtendedResponse> selectSupplierAll(Integer comid) {
        logger.info("Fetching all suppliers with master data for company: {}", comid);

        try {
            // Validate input
            if (comid == null || comid <= 0) {
                logger.warn("Invalid comid: {}", comid);
                return new ArrayList<>();
            }

            // Fetch suppliers with joined master data
            List<SupplierExtendedResponse> result = repository.findAllSupplierWithMasterData(comid);

            logger.info("Successfully retrieved {} supplier records for company: {}", result.size(), comid);
            return result;

        } catch (Exception ex) {
            logger.error("Error fetching suppliers with master data for company: " + comid, ex);
            return new ArrayList<>();
        }
    }

}
