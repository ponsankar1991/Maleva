package my.maleva.api.service;

import my.maleva.api.dto.JobDetailsDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.JobDetailsMapper;
import my.maleva.api.model.JobDetails;
import my.maleva.api.repo.JobDetailsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobDetailsService {

    private final JobDetailsRepository repository;
    private final JobDetailsMapper mapper;

    public JobDetailsService(JobDetailsRepository repository, JobDetailsMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<JobDetailsDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public JobDetailsDto getById(Integer id) {
        JobDetails ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("JobDetails not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public JobDetailsDto create(JobDetailsDto dto) {
        JobDetails ent = mapper.toEntity(dto);
        JobDetails saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public JobDetailsDto update(Integer id, JobDetailsDto dto) {
        JobDetails ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("JobDetails not found: " + id));
        mapper.updateFromDto(dto, ent);
        JobDetails saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        JobDetails ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("JobDetails not found: " + id));
        repository.delete(ent);
    }
}
