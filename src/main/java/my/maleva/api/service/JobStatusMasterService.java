package my.maleva.api.service;

import my.maleva.api.dto.JobStatusMasterDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.JobStatusMasterMapper;
import my.maleva.api.model.JobStatusMaster;
import my.maleva.api.repo.JobStatusMasterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobStatusMasterService {

    private final JobStatusMasterRepository repository;
    private final JobStatusMasterMapper mapper;

    public JobStatusMasterService(JobStatusMasterRepository repository, JobStatusMasterMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<JobStatusMasterDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public JobStatusMasterDto getById(Integer id) {
        JobStatusMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("JobStatusMaster not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public JobStatusMasterDto create(JobStatusMasterDto dto) {
        LocalDateTime now = LocalDateTime.now();
        JobStatusMaster ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        JobStatusMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public JobStatusMasterDto update(Integer id, JobStatusMasterDto dto) {
        JobStatusMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("JobStatusMaster not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        JobStatusMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        JobStatusMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("JobStatusMaster not found: " + id));
        repository.delete(ent);
    }
}
