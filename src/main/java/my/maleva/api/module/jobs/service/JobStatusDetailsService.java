package my.maleva.api.module.jobs.service;

import my.maleva.api.module.jobs.dto.JobStatusDetailsDto;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.module.jobs.mapper.JobStatusDetailsMapper;
import my.maleva.api.module.jobs.entity.JobStatusDetails;
import my.maleva.api.module.jobs.repository.JobStatusDetailsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobStatusDetailsService {

    private final JobStatusDetailsRepository repository;
    private final JobStatusDetailsMapper mapper;

    public JobStatusDetailsService(JobStatusDetailsRepository repository, JobStatusDetailsMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<JobStatusDetailsDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public JobStatusDetailsDto getById(Integer id) {
        JobStatusDetails ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("JobStatusDetails not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public JobStatusDetailsDto create(JobStatusDetailsDto dto) {
        JobStatusDetails ent = mapper.toEntity(dto);
        JobStatusDetails saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public JobStatusDetailsDto update(Integer id, JobStatusDetailsDto dto) {
        JobStatusDetails ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("JobStatusDetails not found: " + id));
        mapper.updateFromDto(dto, ent);
        JobStatusDetails saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        JobStatusDetails ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("JobStatusDetails not found: " + id));
        repository.delete(ent);
    }
}
