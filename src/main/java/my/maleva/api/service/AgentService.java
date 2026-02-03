package my.maleva.api.service;

import my.maleva.api.dto.AgentDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.AgentMapper;
import my.maleva.api.model.Agent;
import my.maleva.api.repo.AgentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AgentService {

    private final AgentRepository repository;
    private final AgentMapper mapper;

    public AgentService(AgentRepository repository, AgentMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<AgentDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public AgentDto getById(Integer id) {
        Agent ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Agent not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public AgentDto create(AgentDto dto) {
        LocalDateTime now = LocalDateTime.now();
        Agent ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        Agent saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public AgentDto update(Integer id, AgentDto dto) {
        Agent ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Agent not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        Agent saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        Agent ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Agent not found: " + id));
        repository.delete(ent);
    }
}
