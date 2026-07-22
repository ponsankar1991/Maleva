package my.maleva.api.module.rti.service.impl;

import my.maleva.api.module.rti.dto.RTIJobLookupDto;
import my.maleva.api.module.rti.dto.RTIMasterDto;
import my.maleva.api.module.rti.entity.RTIMaster;
import my.maleva.api.module.rti.mapper.RTIMasterMapper;
import my.maleva.api.module.rti.repository.RTIMasterRepository;
import my.maleva.api.module.rti.service.RTIMasterService;
import my.maleva.api.module.saleorder.repository.SaleOrderMasterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import my.maleva.api.module.customer.repository.CustomerRepository;
import my.maleva.api.module.saleorder.entity.SaleOrderMaster;

/**
 * RTIMasterServiceImpl
 * Service implementation for RTIMaster.
 */
@Service
public class RTIMasterServiceImpl implements RTIMasterService {

    private static final Logger logger = LoggerFactory.getLogger(RTIMasterServiceImpl.class);

    @Autowired
    private RTIMasterRepository rtiMasterRepository;

    @Autowired
    private SaleOrderMasterRepository saleOrderMasterRepository;

    @Autowired
    private RTIMasterMapper mapper;

    @Autowired
    private my.maleva.api.module.rti.repository.RTIDetailsRepository rtiDetailsRepository;
    
    @Autowired
    private my.maleva.api.module.rti.mapper.RTIDetailsMapper rtiDetailsMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Override
    @Transactional(readOnly = true)
    public List<RTIMasterDto> getAllByCompanyId(Integer companyRefId) {
        logger.info("Fetching all RTIMaster records for company: {}", companyRefId);
        return rtiMasterRepository.findByCompanyRefId(companyRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RTIMasterDto> getActiveByCompanyId(Integer companyRefId) {
        logger.info("Fetching active RTIMaster records for company: {}", companyRefId);
        return rtiMasterRepository.findByCompanyRefIdAndActive(companyRefId, 1)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RTIMasterDto> getActiveByCompanyId(
            Integer companyRefId,
            String fromDate,
            String toDate,
            Integer driverId,
            Integer truckId,
            Integer employeeId,
            String search) {
        logger.info(
                "Fetching active RTIMaster records for company: {} fromDate={} toDate={} driverId={} truckId={} employeeId={} search={}",
                companyRefId,
                fromDate,
                toDate,
                driverId,
                truckId,
                employeeId,
                search
        );

        String normalizedSearch = search != null ? search.trim() : "";
        if (!normalizedSearch.isEmpty()) {
            return rtiMasterRepository.findActiveByCompanyAndRtiNo(companyRefId, normalizedSearch)
                    .stream()
                    .map(mapper::toDto)
                    .collect(Collectors.toList());
        }

        LocalDateTime start = parseDateParam(fromDate, false);
        LocalDateTime end = parseDateParam(toDate, true);

        return rtiMasterRepository.findActiveByCompanyWithFilters(
                        companyRefId,
                        start,
                        end,
                        driverId,
                        truckId,
                        employeeId
                )
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    private LocalDateTime parseDateParam(String value, boolean endOfDay) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String trimmed = value.trim();
        String normalizedDate = trimmed.replace('/', '-');
        if (normalizedDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            LocalDate date = LocalDate.parse(normalizedDate);
            return endOfDay ? date.atTime(23, 59, 59, 999_000_000) : date.atStartOfDay();
        }

        try {
            return OffsetDateTime.parse(trimmed)
                    .atZoneSameInstant(ZoneId.systemDefault())
                    .toLocalDateTime();
        } catch (DateTimeParseException ignored) {
            // Fall through to local date-time parsing for legacy callers.
        }

        return LocalDateTime.parse(trimmed, DateTimeFormatter.ISO_DATE_TIME);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RTIMasterDto> getById(Integer id) {
        logger.info("Fetching RTIMaster by ID: {}", id);
        return rtiMasterRepository.findById(id)
                .map(mapper::toDto);
    }

    @Override
    @Transactional
    public RTIMasterDto create(RTIMasterDto dto) {
        logger.info("Creating new RTIMaster for company: {}", dto.getCompanyRefId());

        RTIMaster entity = mapper.toEntity(dto);
        LocalDateTime now = LocalDateTime.now();

        // Force insert semantics even if the client accidentally posts id=0.
        entity.setId(null);
        entity.setCreatedDate(now);
        entity.setModifiedDate(now);

        if (entity.getCreatedBy() == null || entity.getCreatedBy().isBlank()) {
            entity.setCreatedBy("SYSTEM");
        }
        if (entity.getModifiedBy() == null || entity.getModifiedBy().isBlank()) {
            entity.setModifiedBy(entity.getCreatedBy());
        }
        if (entity.getActive() == null) {
            entity.setActive(1);
        }
        if (entity.getSleeping() == null) {
            entity.setSleeping(0);
        }
        if (entity.getPickup() == null) {
            entity.setPickup(0);
        }
        if (entity.getPickupCount() == null) {
            entity.setPickupCount(0);
        }
        if (entity.getDropCount() == null) {
            entity.setDropCount(0);
        }
        if (entity.getAddDrop() == null) {
            entity.setAddDrop(0);
        }
        if (entity.getExitYN() == null) {
            entity.setExitYN(0);
        }

        RTIMaster saved = rtiMasterRepository.save(entity);
        logger.info("RTIMaster created with ID: {}", saved.getId());

        List<my.maleva.api.module.rti.dto.RTIDetailsDto> savedDetailsDto = new java.util.ArrayList<>();
        if (dto.getRtiDetails() != null && !dto.getRtiDetails().isEmpty()) {
            for (my.maleva.api.module.rti.dto.RTIDetailsDto detailDto : dto.getRtiDetails()) {
                my.maleva.api.module.rti.entity.RTIDetails detailEntity = rtiDetailsMapper.toEntity(detailDto);
                detailEntity.setId(null);
                detailEntity.setRtiMasterRefId(saved.getId());
                my.maleva.api.module.rti.entity.RTIDetails savedDetail = rtiDetailsRepository.save(detailEntity);
                savedDetailsDto.add(rtiDetailsMapper.toDto(savedDetail));

                // Synchronize RTIPickup and RTIDelivery tables on creation matching legacy .NET behavior
                if (savedDetail.getSaleOrderMasterRefId() != null) {
                    int rtiMasterRefId = saved.getId();
                    int saleOrderMasterRefId = savedDetail.getSaleOrderMasterRefId();

                    // Pickup Insertion
                    jdbcTemplate.update("""
                            INSERT INTO RTIPickup (
                                RTIMasterRefId, SaleOrderMasterRefId,
                                PickupAddress, PickupTime,
                                PickupWeight, PickupQuantity, CreatedDate
                            )
                            SELECT
                                ?,
                                ?,
                                PickupAddress,
                                PickupTime,
                                PickupWeight,
                                PickupQuantity,
                                GETDATE()
                            FROM SaleOrderPickup WITH(NOLOCK)
                            WHERE SaleOrderMasterRefId = ?
                            """,
                            rtiMasterRefId, saleOrderMasterRefId, saleOrderMasterRefId);

                    // Delivery Insertion
                    jdbcTemplate.update("""
                            INSERT INTO RTIDelivery (
                                RTIMasterRefId, SaleOrderMasterRefId,
                                DeliveryAddress, DeliveryTime,
                                DeliveryWeight, DeliveryQuantity, CreatedDate
                            )
                            SELECT
                                ?,
                                ?,
                                DeliveryAddress,
                                DeliveryTime,
                                DeliveryWeight,
                                DeliveryQuantity,
                                GETDATE()
                            FROM SaleOrderDelivery WITH(NOLOCK)
                            WHERE SaleOrderMasterRefId = ?
                            """,
                            rtiMasterRefId, saleOrderMasterRefId, saleOrderMasterRefId);

                    // CONDITIONAL: Warehouse Data Update - Only if pwdType = 1 or 2
                    if (detailDto.getPwdType() != null && detailDto.getPwdType() > 0) {
                        if (detailDto.getPwdType() == 1 || detailDto.getPwdType() == 2) {
                            logger.info("RTI_WAREHOUSE: Executing warehouse update - RTIMasterRefId: {}, SaleOrderMasterRefId: {}, pwdType: {}",
                                    rtiMasterRefId, saleOrderMasterRefId, detailDto.getPwdType());

                            int rowsUpdated = jdbcTemplate.update("""
                                UPDATE RD
                                SET
                                    RD.WareHouseEnterDate = SOM.WareHouseEnterDate,
                                    RD.WareHouseExitDate = SOM.WareHouseExitDate,
                                    RD.WareHouseAddress = SOM.WareHouseAddress
                                FROM RTIDetails RD
                                INNER JOIN SaleOrderMaster SOM
                                    ON SOM.Id = ?
                                WHERE RD.RTIMasterRefId = ?
                                  AND RD.SaleOrderMasterRefId = ?
                                """,
                                saleOrderMasterRefId,
                                rtiMasterRefId,
                                saleOrderMasterRefId);

                            logger.info("RTI_WAREHOUSE: Completed - Rows: {}, RTIMasterRefId: {}, pwdType: {}",
                                    rowsUpdated, rtiMasterRefId, detailDto.getPwdType());
                        }
                    } else if (detailDto.getPwdType() == null || detailDto.getPwdType() == 0) {
                        logger.debug("RTI_WAREHOUSE: Skipped warehouse update - pwdType is 0 or null (disabled)");
                    }

                }
            }
        }

        RTIMasterDto result = mapper.toDto(saved);
        result.setRtiDetails(savedDetailsDto);
        return result;
    }

    @Override
    @Transactional
    public RTIMasterDto update(Integer id, RTIMasterDto dto) {
        logger.info("Updating RTIMaster with ID: {}", id);
        RTIMaster entity = rtiMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RTIMaster not found with ID: " + id));

        mapper.updateEntityFromDto(dto, entity);
        entity.setId(id);
        entity.setModifiedDate(LocalDateTime.now());

        if (entity.getModifiedBy() == null || entity.getModifiedBy().isBlank()) {
            entity.setModifiedBy(entity.getCreatedBy() != null ? entity.getCreatedBy() : "SYSTEM");
        }

        RTIMaster updated = rtiMasterRepository.save(entity);
        logger.info("RTIMaster updated with ID: {}", id);

        List<my.maleva.api.module.rti.dto.RTIDetailsDto> savedDetailsDto = new java.util.ArrayList<>();
        if (dto.getRtiDetails() != null) {
            rtiDetailsRepository.deleteByRtiMasterRefId(updated.getId());
            for (my.maleva.api.module.rti.dto.RTIDetailsDto detailDto : dto.getRtiDetails()) {
                my.maleva.api.module.rti.entity.RTIDetails detailEntity = rtiDetailsMapper.toEntity(detailDto);
                detailEntity.setId(null);
                detailEntity.setRtiMasterRefId(updated.getId());
                my.maleva.api.module.rti.entity.RTIDetails savedDetail = rtiDetailsRepository.save(detailEntity);
                savedDetailsDto.add(rtiDetailsMapper.toDto(savedDetail));

                // Synchronize RTIPickup and RTIDelivery tables on update matching legacy .NET behavior
                if (savedDetail.getSaleOrderMasterRefId() != null) {
                    int rtiMasterRefId = updated.getId();
                    int saleOrderMasterRefId = savedDetail.getSaleOrderMasterRefId();


                    // CONDITIONAL: Warehouse Data Update - Only if pwdType = 1 or 2
                    if (detailDto.getPwdType() != null && detailDto.getPwdType() > 0) {
                        if (detailDto.getPwdType() == 1 || detailDto.getPwdType() == 2) {
                            logger.info("RTI_WAREHOUSE: Executing warehouse update - RTIMasterRefId: {}, SaleOrderMasterRefId: {}, pwdType: {}",
                                    rtiMasterRefId, saleOrderMasterRefId, detailDto.getPwdType());

                            int rowsUpdated = jdbcTemplate.update("""
                                UPDATE RD
                                SET
                                    RD.WareHouseEnterDate = SOM.WareHouseEnterDate,
                                    RD.WareHouseExitDate = SOM.WareHouseExitDate,
                                    RD.WareHouseAddress = SOM.WareHouseAddress
                                FROM RTIDetails RD
                                INNER JOIN SaleOrderMaster SOM
                                    ON SOM.Id = ?
                                WHERE RD.RTIMasterRefId = ?
                                  AND RD.SaleOrderMasterRefId = ?
                                """,
                                saleOrderMasterRefId,
                                rtiMasterRefId,
                                saleOrderMasterRefId);

                            logger.info("RTI_WAREHOUSE: Completed - Rows: {}, RTIMasterRefId: {}, pwdType: {}",
                                    rowsUpdated, rtiMasterRefId, detailDto.getPwdType());
                        }
                    } else if (detailDto.getPwdType() == null || detailDto.getPwdType() == 0) {
                        logger.debug("RTI_WAREHOUSE: Skipped warehouse update - pwdType is 0 or null (disabled)");
                    }
                }
            }
        }

        RTIMasterDto result = mapper.toDto(updated);
        result.setRtiDetails(savedDetailsDto);
        return result;
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        logger.info("Deleting RTIMaster with ID: {}", id);
        if (rtiMasterRepository.existsById(id)) {
            rtiMasterRepository.deleteById(id);
            logger.info("RTIMaster deleted with ID: {}", id);
            return true;
        }
        logger.warn("RTIMaster not found with ID: {}", id);
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RTIMasterDto> getByCNumber(Integer companyRefId, Integer cNumber) {
        logger.info("Fetching RTIMaster by CNumber: {}", cNumber);
        return rtiMasterRepository.findByCompanyRefIdAndCNumber(companyRefId, cNumber)
                .map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RTIMasterDto> getByEmployee(Integer companyRefId, Integer employeeRefId) {
        logger.info("Fetching RTIMaster for employee: {}", employeeRefId);
        return rtiMasterRepository.findByCompanyRefIdAndEmployeeRefId(companyRefId, employeeRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RTIMasterDto> getByAgent(Integer companyRefId, Integer agentMasterRefId) {
        logger.info("Fetching RTIMaster for agent: {}", agentMasterRefId);
        return rtiMasterRepository.findByCompanyRefIdAndAgentMasterRefId(companyRefId, agentMasterRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RTIMasterDto> getByDateRange(Integer companyRefId, LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Fetching RTIMaster between dates: {} to {}", startDate, endDate);
        return rtiMasterRepository.findByCompanyRefIdAndSaleDateBetween(companyRefId, startDate, endDate)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RTIMasterDto> getByCNumberDisplay(String cNumberDisplay) {
        logger.info("Fetching RTIMaster by CNumberDisplay: {}", cNumberDisplay);
        return rtiMasterRepository.findByCNumberDisplay(cNumberDisplay)
                .map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RTIMasterDto> getSleepingRecords(Integer companyRefId) {
        logger.info("Fetching sleeping RTIMaster records for company: {}", companyRefId);
        return rtiMasterRepository.findByCompanyRefIdAndSleeping(companyRefId, 1)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RTIMasterDto> getByTruck(Integer companyRefId, Integer truckRefId) {
        logger.info("Fetching RTIMaster for truck: {}", truckRefId);
        return rtiMasterRepository.findByCompanyRefIdAndTruckRefId(companyRefId, truckRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RTIJobLookupDto> searchJobNo(Integer companyRefId, String jobNo) {
        logger.info("Searching sale order job number '{}' for RTI company: {}", jobNo, companyRefId);

        if (companyRefId == null || jobNo == null || jobNo.isBlank()) {
            return List.of();
        }

        return saleOrderMasterRepository.findRTIJobLookupByCompanyRefIdAndJobNo(
                companyRefId,
                jobNo.trim()
        );
    }

    @Override
    public boolean existsByCNumber(Integer companyRefId, Integer cNumber) {
        logger.info("Checking if CNumber exists: {}", cNumber);
        return rtiMasterRepository.existsByCompanyRefIdAndCNumber(companyRefId, cNumber);
    }

    @Override
    public long countByCompanyId(Integer companyRefId) {
        logger.info("Counting RTIMaster records for company: {}", companyRefId);
        return rtiMasterRepository.countByCompanyRefId(companyRefId);
    }

    @Override
    public long countActiveByCompanyId(Integer companyRefId) {
        logger.info("Counting active RTIMaster records for company: {}", companyRefId);
        return rtiMasterRepository.countByCompanyRefIdAndActive(companyRefId, 1);
    }

    @Override
    @Transactional
    public RTIMasterDto activate(Integer id) {
        logger.info("Activating RTIMaster with ID: {}", id);
        RTIMaster entity = rtiMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RTIMaster not found with ID: " + id));
        entity.setActive(1);
        entity.setModifiedDate(LocalDateTime.now());
        RTIMaster updated = rtiMasterRepository.save(entity);
        logger.info("RTIMaster activated with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public RTIMasterDto deactivate(Integer id) {
        logger.info("Deactivating RTIMaster with ID: {}", id);
        RTIMaster entity = rtiMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RTIMaster not found with ID: " + id));
        entity.setActive(0);
        entity.setModifiedDate(LocalDateTime.now());
        RTIMaster updated = rtiMasterRepository.save(entity);
        logger.info("RTIMaster deactivated with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    public String generateCNumberDisplay(Integer cNumber) {
        logger.info("Generating CNumberDisplay for CNumber: {}", cNumber);
        return String.format("RTI%09d", cNumber);
    }

    // NOTE: create/clone revise behavior removed to match legacy .NET ReviseRTI which is read-only.

    @Override
    @Transactional
    public RTIMasterDto getForRevise(Integer id, Integer sourceCNumber, Integer companyRefId) {
        logger.info("Loading RTI for revise UI. id={}, sourceCNumber={}, companyRefId={}", id, sourceCNumber, companyRefId);

        RTIMaster source;
        if (sourceCNumber != null && sourceCNumber != 0) {
            if (companyRefId == null) {
                throw new RuntimeException("companyRefId is required when sourceCNumber (RTINo) is provided");
            }
            source = rtiMasterRepository.findByCompanyRefIdAndCNumber(companyRefId, sourceCNumber)
                    .orElseThrow(() -> new RuntimeException("RTIMaster not found with CNumber: " + sourceCNumber + " for company: " + companyRefId));
        } else {
            source = rtiMasterRepository.findById(id)
                    .orElseGet(() -> {
                        // Fallback: If id wasn't found as PK, check if the frontend accidentally passed CNumber as id
                        if (companyRefId != null) {
                            return rtiMasterRepository.findByCompanyRefIdAndCNumber(companyRefId, id)
                                    .orElseThrow(() -> new RuntimeException("RTIMaster not found with ID or CNumber: " + id));
                        }
                        throw new RuntimeException("RTIMaster not found with ID: " + id);
                    });
        }
        
        logger.info("Successfully loaded source RTI id={}, companyRefId={}", source.getId(), source.getCompanyRefId());

        RTIMasterDto masterDto = mapper.toDto(source);

        List<Object[]> enrichedDetails = rtiDetailsRepository.findDetailsWithEnrichment(source.getId());
        if (enrichedDetails == null || enrichedDetails.isEmpty()) {
            masterDto.setRtiDetails(List.of());
            return masterDto;
        }

        List<my.maleva.api.module.rti.dto.RTIDetailsDto> detailDtos = enrichedDetails.stream().map(row -> {
            my.maleva.api.module.rti.entity.RTIDetails d = (my.maleva.api.module.rti.entity.RTIDetails) row[0];
            SaleOrderMaster som = (SaleOrderMaster) row[1];
            String customerName = (String) row[2];

            my.maleva.api.module.rti.dto.RTIDetailsDto dto = rtiDetailsMapper.toDto(d);

            // Enrich with SaleOrderMaster job fields
            if (som != null) {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                dto.setJobNo(som.getCNumberDisplay());
                dto.setJobDate(som.getSaleDate() != null ? som.getSaleDate().format(fmt) : null);
                
                // Overwrite the old RTI Detail properties with the fresh data from SaleOrderMaster
                dto.setPickupDateD(som.getPickupDate());
                dto.setDeliveryDateD(som.getDeliveryDate());
                dto.setOriginD(som.getOrigin());
                dto.setDestinationD(som.getDestination());
                dto.setCustomerName(customerName);
            }

            return dto;
        }).collect(Collectors.toList());

        // Synchronize RTIPickup and RTIDelivery tables matching legacy .NET Dapper behavior
        if (!detailDtos.isEmpty()) {
            for (my.maleva.api.module.rti.dto.RTIDetailsDto item : detailDtos) {
                if (item.getSaleOrderMasterRefId() != null) {
                    int rtiMasterRefId = source.getId();
                    int saleOrderMasterRefId = item.getSaleOrderMasterRefId();

                    // Pickup Sync
                    jdbcTemplate.update("DELETE FROM RTIPickup WHERE RTIMasterRefId = ? AND SaleOrderMasterRefId = ?",
                            rtiMasterRefId, saleOrderMasterRefId);

                    jdbcTemplate.update("""
                            INSERT INTO RTIPickup (
                                RTIMasterRefId, SaleOrderMasterRefId,
                                PickupAddress, PickupTime,
                                PickupWeight, PickupQuantity, CreatedDate
                            )
                            SELECT
                                ?,
                                ?,
                                PickupAddress,
                                PickupTime,
                                PickupWeight,
                                PickupQuantity,
                                GETDATE()
                            FROM SaleOrderPickup WITH(NOLOCK)
                            WHERE SaleOrderMasterRefId = ?
                            """,
                            rtiMasterRefId, saleOrderMasterRefId, saleOrderMasterRefId);

                    // Delivery Sync
                    jdbcTemplate.update("DELETE FROM RTIDelivery WHERE RTIMasterRefId = ? AND SaleOrderMasterRefId = ?",
                            rtiMasterRefId, saleOrderMasterRefId);

                    jdbcTemplate.update("""
                            INSERT INTO RTIDelivery (
                                RTIMasterRefId, SaleOrderMasterRefId,
                                DeliveryAddress, DeliveryTime,
                                DeliveryWeight, DeliveryQuantity, CreatedDate
                            )
                            SELECT
                                ?,
                                ?,
                                DeliveryAddress,
                                DeliveryTime,
                                DeliveryWeight,
                                DeliveryQuantity,
                                GETDATE()
                            FROM SaleOrderDelivery WITH(NOLOCK)
                            WHERE SaleOrderMasterRefId = ?
                            """,
                            rtiMasterRefId, saleOrderMasterRefId, saleOrderMasterRefId);

                    // CONDITIONAL: Warehouse Data Update - Only if pwdType = 1 or 2
                    if (item.getPwdType() != null && item.getPwdType() > 0) {
                        if (item.getPwdType() == 1 || item.getPwdType() == 2) {
                            logger.info("RTI_WAREHOUSE: Executing warehouse update in getForRevise - RTIMasterRefId: {}, SaleOrderMasterRefId: {}, pwdType: {}",
                                    rtiMasterRefId, saleOrderMasterRefId, item.getPwdType());

                            int rowsUpdated = jdbcTemplate.update("""
                                UPDATE RD
                                SET
                                    RD.WareHouseEnterDate = SOM.WareHouseEnterDate,
                                    RD.WareHouseExitDate = SOM.WareHouseExitDate,
                                    RD.WareHouseAddress = SOM.WareHouseAddress
                                FROM RTIDetails RD
                                INNER JOIN SaleOrderMaster SOM
                                    ON SOM.Id = ?
                                WHERE RD.RTIMasterRefId = ?
                                  AND RD.SaleOrderMasterRefId = ?
                                """,
                                saleOrderMasterRefId,
                                rtiMasterRefId,
                                saleOrderMasterRefId);

                            logger.info("RTI_WAREHOUSE: Completed in getForRevise - Rows: {}, RTIMasterRefId: {}, pwdType: {}",
                                    rowsUpdated, rtiMasterRefId, item.getPwdType());
                        }
                    } else if (item.getPwdType() == null || item.getPwdType() == 0) {
                        logger.debug("RTI_WAREHOUSE: Skipped warehouse update in getForRevise - pwdType is 0 or null (disabled)");
                    }




                }
            }
        }

        masterDto.setRtiDetails(detailDtos);
        return masterDto;
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<my.maleva.api.module.rti.dto.RTIViewDto> getRtiViewDetails(
            java.time.LocalDate fromDate, 
            java.time.LocalDate toDate, 
            Integer employeeId) {
        
        logger.info("Executing getRtiViewDetails for EmployeeId: {}, FromDate: {}, ToDate: {}", 
                    employeeId, fromDate, toDate);
                    
        return rtiMasterRepository.findRtiViewDetails(fromDate, toDate, employeeId);
    }
}
