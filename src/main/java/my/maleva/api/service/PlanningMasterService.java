package my.maleva.api.service;

import my.maleva.api.dto.PlanningDetailsDto;
import my.maleva.api.dto.PlanningMasterDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.exception.InvalidRequestException;
import my.maleva.api.mapper.PlanningMasterMapper;
import my.maleva.api.model.PlanningDetails;
import my.maleva.api.model.PlanningMaster;
import my.maleva.api.repo.PlanningDetailsRepository;
import my.maleva.api.repo.PlanningMasterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlanningMasterService {

    private final PlanningMasterRepository planningMasterRepository;
    private final PlanningDetailsRepository planningDetailsRepository;
    private final PlanningMasterMapper planningMasterMapper;

    public PlanningMasterService(
            PlanningMasterRepository planningMasterRepository,
            PlanningDetailsRepository planningDetailsRepository,
            PlanningMasterMapper planningMasterMapper) {
        this.planningMasterRepository = planningMasterRepository;
        this.planningDetailsRepository = planningDetailsRepository;
        this.planningMasterMapper = planningMasterMapper;
    }

    /**
     * Get all planning records by company (non-deleted)
     */
    public List<PlanningMasterDto> listAll() {
        return planningMasterRepository.findByCompanyRefIdAndActiveNot(0, 2)
                .stream()
                .map(planningMasterMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get planning record by ID
     */
    public PlanningMasterDto getById(Integer id) {
        PlanningMaster planning = planningMasterRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Planning Master not found: " + id));

        PlanningMasterDto dto = planningMasterMapper.toDto(planning);

        // Load related details
        List<PlanningDetails> details = planningDetailsRepository.findByPlanningMasterRefIdSorted(id);
        dto.setPlanningDetails(details.stream()
                .map(d -> PlanningDetailsDto.builder()
                        .id(d.getId())
                        .planningMasterRefId(d.getPlanningMasterRefId())
                        .saleOrderMasterRefId(d.getSaleOrderMasterRefId())
                        .truckRefId(d.getTruckRefId())
                        .remarks(d.getRemarks())
                        .createdDate(d.getCreatedDate())
                        .modifiedDate(d.getModifiedDate())
                        .originD(d.getOriginD())
                        .destinationD(d.getDestinationD())
                        .pickupDateD(d.getPickupDateD())
                        .deliveryDateD(d.getDeliveryDateD())
                        .sortBy(d.getSortBy())
                        .truckNameD(d.getTruckNameD())
                        .driverNameD(d.getDriverNameD())
                        .pickupTimeList(d.getPickupTimeList())
                        .pickupQuantityList(d.getPickupQuantityList())
                        .deliveryQuantityList(d.getDeliveryQuantityList())
                        .deliveryTimeList(d.getDeliveryTimeList())
                        .build())
                .collect(Collectors.toList()));

        return dto;
    }

    /**
     * Create new planning record with details (Implements SP_PLANINGMaster logic)
     */
    @Transactional
    public PlanningMasterDto create(PlanningMasterDto dto) {
        if (dto.getCompanyRefId() == null) {
            throw new InvalidRequestException("Company reference ID is required");
        }

        LocalDateTime now = LocalDateTime.now();

        // Create master record
        PlanningMaster planning = PlanningMaster.builder()
                .companyRefId(dto.getCompanyRefId())
                .userRefId(dto.getUserRefId())
                .employeeRefId(dto.getEmployeeRefId())
                .lastEmployeeRefId(dto.getLastEmployeeRefId())
                .saleDate(dto.getSaleDate() != null ? dto.getSaleDate() : now)
                .fDate(dto.getFDate() != null ? dto.getFDate() : now)
                .tDate(dto.getTDate() != null ? dto.getTDate() : now)
                .cNumberDisplay(dto.getCNumberDisplay())
                .cNumber(dto.getCNumber() != null ? dto.getCNumber() : 0)
                .remarks(dto.getRemarks())
                .search(dto.getSearch())
                .active(1) // Active by default
                .createdDate(now)
                .createdBy(dto.getCreatedBy() != null ? dto.getCreatedBy() : "SYSTEM")
                .modifiedDate(now)
                .modifiedBy(dto.getModifiedBy() != null ? dto.getModifiedBy() : "SYSTEM")
                .build();

        PlanningMaster savedMaster = planningMasterRepository.save(planning);

        // Create detail records if provided
        if (dto.getPlanningDetails() != null && !dto.getPlanningDetails().isEmpty()) {
            List<PlanningDetails> details = new ArrayList<>();
            for (PlanningDetailsDto detailDto : dto.getPlanningDetails()) {
                PlanningDetails detail = PlanningDetails.builder()
                        .planningMasterRefId(savedMaster.getId())
                        .saleOrderMasterRefId(detailDto.getSaleOrderMasterRefId())
                        .truckRefId(detailDto.getTruckRefId())
                        .remarks(detailDto.getRemarks())
                        .originD(detailDto.getOriginD())
                        .destinationD(detailDto.getDestinationD())
                        .pickupDateD(detailDto.getPickupDateD())
                        .deliveryDateD(detailDto.getDeliveryDateD())
                        .sortBy(detailDto.getSortBy() != null ? detailDto.getSortBy() : 0)
                        .truckNameD(detailDto.getTruckNameD())
                        .driverNameD(detailDto.getDriverNameD())
                        .pickupTimeList(detailDto.getPickupTimeList())
                        .pickupQuantityList(detailDto.getPickupQuantityList())
                        .deliveryQuantityList(detailDto.getDeliveryQuantityList())
                        .deliveryTimeList(detailDto.getDeliveryTimeList())
                        .createdDate(now)
                        .modifiedDate(now)
                        .build();
                details.add(detail);
            }
            planningDetailsRepository.saveAll(details);
        }

        return getById(savedMaster.getId());
    }

    /**
     * Update planning record with details (Implements SP_PLANINGMaster logic)
     */
    @Transactional
    public PlanningMasterDto update(Integer id, PlanningMasterDto dto) {
        PlanningMaster planning = planningMasterRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Planning Master not found: " + id));

        LocalDateTime now = LocalDateTime.now();

        // Update master record
        planning.setLastEmployeeRefId(dto.getLastEmployeeRefId() != null ? dto.getLastEmployeeRefId() : planning.getLastEmployeeRefId());
        planning.setUserRefId(dto.getUserRefId() != null ? dto.getUserRefId() : planning.getUserRefId());
        planning.setEmployeeRefId(dto.getEmployeeRefId() != null ? dto.getEmployeeRefId() : planning.getEmployeeRefId());
        planning.setFDate(dto.getFDate() != null ? dto.getFDate() : planning.getFDate());
        planning.setTDate(dto.getTDate() != null ? dto.getTDate() : planning.getTDate());
        planning.setSaleDate(dto.getSaleDate() != null ? dto.getSaleDate() : planning.getSaleDate());
        planning.setSearch(dto.getSearch() != null ? dto.getSearch() : planning.getSearch());
        planning.setRemarks(dto.getRemarks() != null ? dto.getRemarks() : planning.getRemarks());
        planning.setModifiedDate(now);
        planning.setModifiedBy(dto.getModifiedBy() != null ? dto.getModifiedBy() : "SYSTEM");

        planningMasterRepository.save(planning);

        // Delete and recreate details if provided
        if (dto.getPlanningDetails() != null) {
            planningDetailsRepository.deleteByPlanningMasterRefId(id);

            List<PlanningDetails> details = new ArrayList<>();
            for (PlanningDetailsDto detailDto : dto.getPlanningDetails()) {
                PlanningDetails detail = PlanningDetails.builder()
                        .planningMasterRefId(id)
                        .saleOrderMasterRefId(detailDto.getSaleOrderMasterRefId())
                        .truckRefId(detailDto.getTruckRefId())
                        .remarks(detailDto.getRemarks())
                        .originD(detailDto.getOriginD())
                        .destinationD(detailDto.getDestinationD())
                        .pickupDateD(detailDto.getPickupDateD())
                        .deliveryDateD(detailDto.getDeliveryDateD())
                        .sortBy(detailDto.getSortBy() != null ? detailDto.getSortBy() : 0)
                        .truckNameD(detailDto.getTruckNameD())
                        .driverNameD(detailDto.getDriverNameD())
                        .pickupTimeList(detailDto.getPickupTimeList())
                        .pickupQuantityList(detailDto.getPickupQuantityList())
                        .deliveryQuantityList(detailDto.getDeliveryQuantityList())
                        .deliveryTimeList(detailDto.getDeliveryTimeList())
                        .createdDate(now)
                        .modifiedDate(now)
                        .build();
                details.add(detail);
            }
            planningDetailsRepository.saveAll(details);
        }

        return getById(id);
    }

    /**
     * Delete planning record
     */
    @Transactional
    public void delete(Integer id) {
        PlanningMaster planning = planningMasterRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Planning Master not found: " + id));

        // Delete related details first
        planningDetailsRepository.deleteByPlanningMasterRefId(id);

        // Delete master record
        planningMasterRepository.delete(planning);
    }

    /**
     * Get planning records by company and date range
     */
    public List<PlanningMasterDto> getByCompanyAndDateRange(Integer companyId, LocalDateTime fromDate, LocalDateTime toDate) {
        return planningMasterRepository.findByCompanyAndDateRange(companyId, fromDate, toDate)
                .stream()
                .map(planningMasterMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Search planning records
     */
    public List<PlanningMasterDto> search(Integer companyId, String keyword) {
        return planningMasterRepository.searchByCompanyAndKeyword(companyId, keyword)
                .stream()
                .map(planningMasterMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get planning records by employee
     */
    public List<PlanningMasterDto> getByCompanyAndEmployee(Integer companyId, Integer employeeId) {
        return planningMasterRepository.findByCompanyRefIdAndEmployeeRefIdAndActiveNot(companyId, employeeId, 2)
                .stream()
                .map(planningMasterMapper::toDto)
                .collect(Collectors.toList());
    }
}



