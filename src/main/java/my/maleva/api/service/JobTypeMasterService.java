package my.maleva.api.service;

import my.maleva.api.common.ApiResponse;
import my.maleva.api.dto.JobTypeMasterDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.JobTypeMasterMapper;
import my.maleva.api.model.JobTypeMaster;
import my.maleva.api.repo.JobTypeMasterRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobTypeMasterService {

    private final JobTypeMasterRepository repository;
    private final JobTypeMasterMapper mapper;

    public JobTypeMasterService(JobTypeMasterRepository repository, JobTypeMasterMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /**
     * Get all job types
     */
    public List<JobTypeMasterDto> listAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get active job types by company ID
     */
    public ResponseEntity<ApiResponse<List<JobTypeMasterDto>>> getJobTypes(Integer companyId) {
        if (companyId == null || companyId <= 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure(HttpStatus.BAD_REQUEST, "Invalid company ID"));
        }

        List<JobTypeMasterDto> dtoList = repository.findByCompanyRefIdAndActiveNot(companyId, 2)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());

        if (dtoList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(HttpStatus.NOT_FOUND, "No Job Types Found for company ID: " + companyId));
        }

        return ResponseEntity.ok(ApiResponse.success("Job Types Retrieved Successfully", dtoList));
    }

    /**
     * Get job type by ID
     */
    public JobTypeMasterDto getById(Integer id) {
        return repository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("JobTypeMaster not found with ID: " + id));
    }

    /**
     * Create new job type
     */
    @Transactional
    public JobTypeMasterDto create(JobTypeMasterDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("JobTypeMasterDto cannot be null");
        }

        LocalDateTime now = LocalDateTime.now();
        JobTypeMaster entity = mapper.toEntity(dto);
        entity.setCreatedDate(now);
        entity.setModifiedDate(now);

        JobTypeMaster saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    /**
     * Update existing job type
     */
    @Transactional
    public JobTypeMasterDto update(Integer id, JobTypeMasterDto dto) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid ID provided");
        }

        if (dto == null) {
            throw new IllegalArgumentException("JobTypeMasterDto cannot be null");
        }

        JobTypeMaster entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("JobTypeMaster not found with ID: " + id));

        mapper.updateFromDto(dto, entity);
        entity.setModifiedDate(LocalDateTime.now());

        JobTypeMaster saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    /**
     * Delete job type by ID
     */
    @Transactional
    public void delete(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid ID provided");
        }

        JobTypeMaster entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("JobTypeMaster not found with ID: " + id));

        repository.delete(entity);
    }
}
