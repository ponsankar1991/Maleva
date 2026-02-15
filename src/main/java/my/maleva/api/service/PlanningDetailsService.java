package my.maleva.api.service;

import my.maleva.api.dto.PlanningDetailsDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.PlanningDetailsMapper;
import my.maleva.api.model.PlanningDetails;
import my.maleva.api.repo.PlanningDetailsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlanningDetailsService {

    private final PlanningDetailsRepository planningDetailsRepository;
    private final PlanningDetailsMapper planningDetailsMapper;

    public PlanningDetailsService(
            PlanningDetailsRepository planningDetailsRepository,
            PlanningDetailsMapper planningDetailsMapper) {
        this.planningDetailsRepository = planningDetailsRepository;
        this.planningDetailsMapper = planningDetailsMapper;
    }

    /**
     * Get all planning details
     */
    public List<PlanningDetailsDto> listAll() {
        return planningDetailsRepository.findAll()
                .stream()
                .map(planningDetailsMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get planning details by ID
     */
    public PlanningDetailsDto getById(Integer id) {
        PlanningDetails detail = planningDetailsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Planning Details not found: " + id));
        return planningDetailsMapper.toDto(detail);
    }

    /**
     * Create new planning details
     */
    @Transactional
    public PlanningDetailsDto create(PlanningDetailsDto dto) {
        LocalDateTime now = LocalDateTime.now();

        PlanningDetails detail = PlanningDetails.builder()
                .planningMasterRefId(dto.getPlanningMasterRefId())
                .saleOrderMasterRefId(dto.getSaleOrderMasterRefId())
                .truckRefId(dto.getTruckRefId())
                .remarks(dto.getRemarks())
                .originD(dto.getOriginD())
                .destinationD(dto.getDestinationD())
                .pickupDateD(dto.getPickupDateD())
                .deliveryDateD(dto.getDeliveryDateD())
                .sortBy(dto.getSortBy() != null ? dto.getSortBy() : 0)
                .truckNameD(dto.getTruckNameD())
                .driverNameD(dto.getDriverNameD())
                .pickupTimeList(dto.getPickupTimeList())
                .pickupQuantityList(dto.getPickupQuantityList())
                .deliveryQuantityList(dto.getDeliveryQuantityList())
                .deliveryTimeList(dto.getDeliveryTimeList())
                .createdDate(now)
                .modifiedDate(now)
                .build();

        PlanningDetails saved = planningDetailsRepository.save(detail);
        return planningDetailsMapper.toDto(saved);
    }

    /**
     * Update planning details
     */
    @Transactional
    public PlanningDetailsDto update(Integer id, PlanningDetailsDto dto) {
        PlanningDetails detail = planningDetailsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Planning Details not found: " + id));

        planningDetailsMapper.updateFromDto(dto, detail);
        detail.setModifiedDate(LocalDateTime.now());

        PlanningDetails saved = planningDetailsRepository.save(detail);
        return planningDetailsMapper.toDto(saved);
    }

    /**
     * Delete planning details
     */
    @Transactional
    public void delete(Integer id) {
        PlanningDetails detail = planningDetailsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Planning Details not found: " + id));
        planningDetailsRepository.delete(detail);
    }

    /**
     * Get all details by planning master reference ID
     */
    public List<PlanningDetailsDto> getByPlanningMasterId(Integer masterRefId) {
        return planningDetailsRepository.findByPlanningMasterRefIdSorted(masterRefId)
                .stream()
                .map(planningDetailsMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all details by sale order master reference ID
     */
    public List<PlanningDetailsDto> getBySaleOrderMasterId(Integer saleOrderMasterId) {
        return planningDetailsRepository.findBySaleOrderMasterRefId(saleOrderMasterId)
                .stream()
                .map(planningDetailsMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all details by truck reference ID
     */
    public List<PlanningDetailsDto> getByTruckId(Integer truckRefId) {
        return planningDetailsRepository.findByTruckRefId(truckRefId)
                .stream()
                .map(planningDetailsMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Delete all details by planning master reference ID
     */
    @Transactional
    public void deleteByPlanningMasterId(Integer masterRefId) {
        planningDetailsRepository.deleteByPlanningMasterRefId(masterRefId);
    }
}

