package my.maleva.api.module.joborder.service.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.module.employee.repository.EmployeeMasterRepository;
import my.maleva.api.module.fleet.repository.DriverMasterRepository;
import my.maleva.api.module.fleet.repository.TruckMasterRepository;
import my.maleva.api.module.joborder.dto.JobOrderFilterDto;
import my.maleva.api.module.joborder.dto.JobOrderLookupDto;
import my.maleva.api.module.joborder.dto.JobOrderRequestDto;
import my.maleva.api.module.joborder.dto.JobOrderResponseDto;
import my.maleva.api.module.joborder.entity.JobOrderMaster;
import my.maleva.api.module.joborder.entity.JobOrderPriorityMaster;
import my.maleva.api.module.joborder.entity.JobOrderStatusMaster;
import my.maleva.api.module.joborder.entity.JobOrderTypeMaster;
import my.maleva.api.module.joborder.mapper.JobOrderMapper;
import my.maleva.api.module.joborder.repository.JobOrderMasterRepository;
import my.maleva.api.module.joborder.repository.JobOrderPriorityMasterRepository;
import my.maleva.api.module.joborder.repository.JobOrderStatusMasterRepository;
import my.maleva.api.module.joborder.repository.JobOrderTypeMasterRepository;
import my.maleva.api.module.joborder.service.JobOrderService;
import my.maleva.api.module.master.entity.SequenceNoMaster;
import my.maleva.api.module.master.repository.SequenceNoMasterRepository;
import my.maleva.api.module.joborder.repository.JobOrderDetailRepository;
import my.maleva.api.module.joborder.mapper.JobOrderDetailMapper;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobOrderServiceImpl implements JobOrderService {

    private final JobOrderMasterRepository jobOrderMasterRepository;
    private final JobOrderStatusMasterRepository statusRepository;
    private final JobOrderTypeMasterRepository typeRepository;
    private final JobOrderPriorityMasterRepository priorityRepository;
    private final EmployeeMasterRepository employeeRepository;
    private final TruckMasterRepository truckRepository;
    private final DriverMasterRepository driverRepository;
    private final SequenceNoMasterRepository sequenceRepository;
    private final JobOrderMapper jobOrderMapper;
    private final JobOrderDetailRepository jobOrderDetailRepository;
    private final JobOrderDetailMapper jobOrderDetailMapper;


    @Override
    @Transactional(readOnly = true)
    public List<JobOrderResponseDto> getJobOrders(JobOrderFilterDto filterDto) {
        Specification<JobOrderMaster> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filterDto.getCompanyRefId() != null && filterDto.getCompanyRefId() != 0) {
                predicates.add(cb.equal(root.get("companyRefId"), filterDto.getCompanyRefId()));
            }
            if (filterDto.getStatusRefId() != null && filterDto.getStatusRefId() != 0) {
                predicates.add(cb.equal(root.get("status").get("id"), filterDto.getStatusRefId()));
            }
            if (filterDto.getJobTypeRefId() != null && filterDto.getJobTypeRefId() != 0) {
                predicates.add(cb.equal(root.get("jobType").get("id"), filterDto.getJobTypeRefId()));
            }
            if (filterDto.getPriorityRefId() != null && filterDto.getPriorityRefId() != 0) {
                predicates.add(cb.equal(root.get("priority").get("id"), filterDto.getPriorityRefId()));
            }
            if (filterDto.getTruckMasterRefId() != null && filterDto.getTruckMasterRefId() != 0) {
                predicates.add(cb.equal(root.get("truck").get("id"), filterDto.getTruckMasterRefId()));
            }
            if (filterDto.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("jobDate"), filterDto.getFromDate().atStartOfDay()));
            }
            if (filterDto.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("jobDate"), filterDto.getToDate().atTime(java.time.LocalTime.MAX)));
            }

            if (filterDto.getIsActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), filterDto.getIsActive()));
            } else {
                predicates.add(cb.isTrue(root.get("isActive")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<JobOrderMaster> list = jobOrderMasterRepository.findAll(spec);
        return list.stream().map(entity -> {
            JobOrderResponseDto dto = jobOrderMapper.toDto(entity);
            List<my.maleva.api.module.joborder.entity.JobOrderDetail> details = jobOrderDetailRepository.findByJobOrderMasterRefId(entity.getId());
            dto.setDetails(details.stream().map(jobOrderDetailMapper::toDto).collect(Collectors.toList()));
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public JobOrderResponseDto getJobOrderById(Integer id, Integer companyRefId) {
        JobOrderMaster entity = findByIdAndCompany(id, companyRefId);
        JobOrderResponseDto dto = jobOrderMapper.toDto(entity);
        List<my.maleva.api.module.joborder.entity.JobOrderDetail> details = jobOrderDetailRepository.findByJobOrderMasterRefId(id);
        dto.setDetails(details.stream().map(jobOrderDetailMapper::toDto).collect(Collectors.toList()));
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<my.maleva.api.module.joborder.dto.JobOrderDetailResponseDto> getJobOrderDetailsByMasterId(Integer masterId) {
        log.info("Fetching details for JobOrderMaster ID: {}", masterId);
        List<my.maleva.api.module.joborder.entity.JobOrderDetail> details = jobOrderDetailRepository.findByJobOrderMasterRefId(masterId);
        return details.stream().map(jobOrderDetailMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public JobOrderResponseDto createJobOrder(JobOrderRequestDto requestDto) {
        log.info("Creating new Job Order for company {}", requestDto.getCompanyRefId());
        JobOrderMaster entity = jobOrderMapper.toEntity(requestDto);

        Integer userId = null;

        // Set lookups
        hydrateLookups(entity, requestDto);

        // If status isn't provided, default to Open (assuming 1 is Open as per instructions)
        if (requestDto.getStatusRefId() == null) {
            JobOrderStatusMaster openStatus = statusRepository.findById(1)
                    .orElseThrow(() -> new EntityNotFoundException("Default Status Open (1) not found"));
            entity.setStatus(openStatus);
        }

        // Sequence logic
        Integer maxSeq = sequenceRepository.findMaxSequenceNoByCompanyAndSequenceName(requestDto.getCompanyRefId(), "JobOrderMaster");
        Integer newSeq = (maxSeq == null || maxSeq == 0) ? 1 : maxSeq + 1;
        
        entity.setCNumber(newSeq);
        String cNumberDisplay = String.format("JO%09d", newSeq);
        entity.setCNumberDisplay(cNumberDisplay);

        // Update SequenceNoMaster
        SequenceNoMaster seqMaster = sequenceRepository
                .findByCompanyRefIdAndSequenceName(requestDto.getCompanyRefId(), "JobOrderMaster")
                .orElseGet(() -> {
                    SequenceNoMaster newS = new SequenceNoMaster();
                    newS.setCompanyRefId(requestDto.getCompanyRefId());
                    newS.setSequenceName("JobOrderMaster");
                    return newS;
                });
        seqMaster.setSequenceNo(newSeq);
        sequenceRepository.save(seqMaster);

        entity.setCreatedBy(userId);
        entity.setCreatedDate(LocalDateTime.now());
        
        // Ensure default JobDate if null
        if (entity.getJobDate() == null) {
            entity.setJobDate(LocalDateTime.now());
        }

        sanitizeDecimals(entity);

        JobOrderMaster saved = jobOrderMasterRepository.save(entity);
        
        List<my.maleva.api.module.joborder.dto.JobOrderDetailResponseDto> savedDetailsDto = new java.util.ArrayList<>();
        if (requestDto.getDetails() != null && !requestDto.getDetails().isEmpty()) {
            for (my.maleva.api.module.joborder.dto.JobOrderDetailRequestDto detailDto : requestDto.getDetails()) {
                my.maleva.api.module.joborder.entity.JobOrderDetail detailEntity = jobOrderDetailMapper.toEntity(detailDto);
                if (detailEntity.getCost() != null) {
                    detailEntity.setCost(detailEntity.getCost().setScale(2, java.math.RoundingMode.HALF_UP));
                }
                detailEntity.setId(null);
                detailEntity.setJobOrderMasterRefId(saved.getId());
                detailEntity.setCreatedBy(userId);
                my.maleva.api.module.joborder.entity.JobOrderDetail savedDetail = jobOrderDetailRepository.save(detailEntity);
                savedDetailsDto.add(jobOrderDetailMapper.toDto(savedDetail));
            }
        }
        
        JobOrderResponseDto responseDto = jobOrderMapper.toDto(saved);
        responseDto.setDetails(savedDetailsDto);
        return responseDto;
    }

    @Override
    @Transactional
    public JobOrderResponseDto updateJobOrder(Integer id, JobOrderRequestDto requestDto) {
        log.info("Updating Job Order {} for company {}", id, requestDto.getCompanyRefId());
        JobOrderMaster entity = findByIdAndCompany(id, requestDto.getCompanyRefId());

        jobOrderMapper.updateEntity(entity, requestDto);
        hydrateLookups(entity, requestDto);

        Integer userId = null;
        
        // Auto set CompletedDate if status changed to Completed (Assuming 3 is Completed)
        if (requestDto.getStatusRefId() != null && requestDto.getStatusRefId() == 3 && entity.getCompletedDate() == null) {
            entity.setCompletedDate(LocalDateTime.now());
        }

        entity.setModifiedBy(userId);
        entity.setModifiedDate(LocalDateTime.now());

        sanitizeDecimals(entity);

        JobOrderMaster updated = jobOrderMasterRepository.save(entity);
        
        List<my.maleva.api.module.joborder.dto.JobOrderDetailResponseDto> savedDetailsDto = new java.util.ArrayList<>();
        if (requestDto.getDetails() != null) {
            List<my.maleva.api.module.joborder.entity.JobOrderDetail> existingDetails = jobOrderDetailRepository.findByJobOrderMasterRefId(updated.getId());
            java.util.Map<Integer, my.maleva.api.module.joborder.entity.JobOrderDetail> existingDetailsMap = existingDetails.stream()
                    .collect(java.util.stream.Collectors.toMap(my.maleva.api.module.joborder.entity.JobOrderDetail::getId, d -> d));

            for (my.maleva.api.module.joborder.dto.JobOrderDetailRequestDto detailDto : requestDto.getDetails()) {
                my.maleva.api.module.joborder.entity.JobOrderDetail detailEntity;
                if (detailDto.getId() != null && detailDto.getId() > 0 && existingDetailsMap.containsKey(detailDto.getId())) {
                    detailEntity = existingDetailsMap.get(detailDto.getId());
                    jobOrderDetailMapper.updateEntity(detailEntity, detailDto);
                    existingDetailsMap.remove(detailDto.getId());
                } else {
                    detailEntity = jobOrderDetailMapper.toEntity(detailDto);
                    detailEntity.setId(null);
                    detailEntity.setJobOrderMasterRefId(updated.getId());
                    detailEntity.setCreatedBy(userId);
                }

                if (detailEntity.getCost() != null) {
                    detailEntity.setCost(detailEntity.getCost().setScale(2, java.math.RoundingMode.HALF_UP));
                }
                
                my.maleva.api.module.joborder.entity.JobOrderDetail savedDetail = jobOrderDetailRepository.save(detailEntity);
                savedDetailsDto.add(jobOrderDetailMapper.toDto(savedDetail));
            }

            for (my.maleva.api.module.joborder.entity.JobOrderDetail detailToDelete : existingDetailsMap.values()) {
                jobOrderDetailRepository.delete(detailToDelete);
            }
        }

        JobOrderResponseDto responseDto = jobOrderMapper.toDto(updated);
        responseDto.setDetails(savedDetailsDto);
        return responseDto;
    }

    @Override
    @Transactional
    public void deleteJobOrder(Integer id, Integer companyRefId) {
        log.info("Deleting Job Order {} for company {}", id, companyRefId);
        JobOrderMaster entity = findByIdAndCompany(id, companyRefId);
        entity.setIsActive(false);
        entity.setModifiedDate(LocalDateTime.now());
        jobOrderMasterRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public JobOrderLookupDto getLookups() {
        List<JobOrderLookupDto.LookupItem> statuses = statusRepository.findByIsActiveTrue().stream()
                .map(s -> new JobOrderLookupDto.LookupItem(s.getId(), s.getStatusName()))
                .collect(Collectors.toList());

        List<JobOrderLookupDto.LookupItem> types = typeRepository.findByIsActiveTrue().stream()
                .map(t -> new JobOrderLookupDto.LookupItem(t.getId(), t.getJobTypeName()))
                .collect(Collectors.toList());

        List<JobOrderLookupDto.LookupItem> priorities = priorityRepository.findByIsActiveTrue().stream()
                .map(p -> new JobOrderLookupDto.LookupItem(p.getId(), p.getPriorityName()))
                .collect(Collectors.toList());

        return JobOrderLookupDto.builder()
                .statuses(statuses)
                .jobTypes(types)
                .priorities(priorities)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobOrderLookupDto.LookupItem> getStatuses() {
        return statusRepository.findByIsActiveTrue().stream()
                .map(s -> new JobOrderLookupDto.LookupItem(s.getId(), s.getStatusName()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobOrderLookupDto.LookupItem> getJobTypes() {
        return typeRepository.findByIsActiveTrue().stream()
                .map(t -> new JobOrderLookupDto.LookupItem(t.getId(), t.getJobTypeName()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobOrderLookupDto.LookupItem> getPriorities() {
        return priorityRepository.findByIsActiveTrue().stream()
                .map(p -> new JobOrderLookupDto.LookupItem(p.getId(), p.getPriorityName()))
                .collect(Collectors.toList());
    }

    private JobOrderMaster findByIdAndCompany(Integer id, Integer companyRefId) {
        return jobOrderMasterRepository.findByIdAndCompanyRefId(id, companyRefId)
                .orElseThrow(() -> new EntityNotFoundException("Job Order not found with ID " + id));
    }

    private void hydrateLookups(JobOrderMaster entity, JobOrderRequestDto request) {
        if (request.getJobTypeRefId() != null) {
            JobOrderTypeMaster type = typeRepository.findById(request.getJobTypeRefId())
                    .orElseThrow(() -> new EntityNotFoundException("Job Type not found"));
            entity.setJobType(type);
        }

        if (request.getStatusRefId() != null) {
            JobOrderStatusMaster status = statusRepository.findById(request.getStatusRefId())
                    .orElseThrow(() -> new EntityNotFoundException("Status not found"));
            entity.setStatus(status);
        }

        if (request.getPriorityRefId() != null) {
            JobOrderPriorityMaster priority = priorityRepository.findById(request.getPriorityRefId())
                    .orElseThrow(() -> new EntityNotFoundException("Priority not found"));
            entity.setPriority(priority);
        } else {
            entity.setPriority(null);
        }

        if (request.getEmployeeRefId() != null) {
            entity.setEmployee(employeeRepository.findById(request.getEmployeeRefId()).orElse(null));
        } else {
            entity.setEmployee(null);
        }

        if (request.getTruckMasterRefId() != null) {
            entity.setTruck(truckRepository.findById(request.getTruckMasterRefId()).orElse(null));
        } else {
            entity.setTruck(null);
        }

        if (request.getDriverMasterRefId() != null) {
            entity.setDriver(driverRepository.findById(request.getDriverMasterRefId()).orElse(null));
        } else {
            entity.setDriver(null);
        }
    }

    private void sanitizeDecimals(JobOrderMaster entity) {
        if (entity.getOdometerReading() != null) {
            entity.setOdometerReading(entity.getOdometerReading().setScale(2, java.math.RoundingMode.HALF_UP));
        }
        if (entity.getEstimatedCost() != null) {
            entity.setEstimatedCost(entity.getEstimatedCost().setScale(2, java.math.RoundingMode.HALF_UP));
        }
        if (entity.getActualCost() != null) {
            entity.setActualCost(entity.getActualCost().setScale(2, java.math.RoundingMode.HALF_UP));
        }
    }
}
