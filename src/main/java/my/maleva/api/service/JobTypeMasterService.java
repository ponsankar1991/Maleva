package my.maleva.api.service;

import my.maleva.api.dto.JobTypeMasterDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.JobTypeMasterMapper;
import my.maleva.api.model.JobTypeMaster;
import my.maleva.api.repo.JobTypeMasterRepository;
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

    public List<JobTypeMasterDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public JobTypeMasterDto getById(Integer id) {
        JobTypeMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("JobTypeMaster not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public JobTypeMasterDto create(JobTypeMasterDto dto) {
        LocalDateTime now = LocalDateTime.now();
        JobTypeMaster ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        JobTypeMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public JobTypeMasterDto update(Integer id, JobTypeMasterDto dto) {
        JobTypeMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("JobTypeMaster not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        JobTypeMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        JobTypeMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("JobTypeMaster not found: " + id));
        repository.delete(ent);
    }
}
