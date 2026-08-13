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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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


    @Override
    @Transactional(readOnly = true)
    public Page<JobOrderResponseDto> getJobOrders(JobOrderFilterDto filterDto, Pageable pageable) {
        Specification<JobOrderMaster> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filterDto.getCompanyRefId() != null) {
                predicates.add(cb.equal(root.get("companyRefId"), filterDto.getCompanyRefId()));
            }
            if (filterDto.getStatusRefId() != null) {
                predicates.add(cb.equal(root.get("status").get("id"), filterDto.getStatusRefId()));
            }
            if (filterDto.getJobTypeRefId() != null) {
                predicates.add(cb.equal(root.get("jobType").get("id"), filterDto.getJobTypeRefId()));
            }
            if (filterDto.getPriorityRefId() != null) {
                predicates.add(cb.equal(root.get("priority").get("id"), filterDto.getPriorityRefId()));
            }
            if (filterDto.getTruckMasterRefId() != null) {
                predicates.add(cb.equal(root.get("truck").get("id"), filterDto.getTruckMasterRefId()));
            }
            if (filterDto.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("jobDate"), filterDto.getFromDate()));
            }
            if (filterDto.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("jobDate"), filterDto.getToDate()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<JobOrderMaster> page = jobOrderMasterRepository.findAll(spec, pageable);
        return page.map(jobOrderMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public JobOrderResponseDto getJobOrderById(Integer id, Integer companyRefId) {
        JobOrderMaster entity = findByIdAndCompany(id, companyRefId);
        return jobOrderMapper.toDto(entity);
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

        JobOrderMaster saved = jobOrderMasterRepository.save(entity);
        return jobOrderMapper.toDto(saved);
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

        JobOrderMaster updated = jobOrderMasterRepository.save(entity);
        return jobOrderMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void deleteJobOrder(Integer id, Integer companyRefId) {
        log.info("Deleting Job Order {} for company {}", id, companyRefId);
        JobOrderMaster entity = findByIdAndCompany(id, companyRefId);
        jobOrderMasterRepository.delete(entity);
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
}
