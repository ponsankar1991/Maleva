package my.maleva.api.service;

import my.maleva.api.dto.EmailInboxDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.EmailInboxMapper;
import my.maleva.api.model.EmailInbox;
import my.maleva.api.repo.EmailInboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmailInboxService {

    private final EmailInboxRepository repository;
    private final EmailInboxMapper mapper;

    public EmailInboxService(EmailInboxRepository repository, EmailInboxMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<EmailInboxDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public EmailInboxDto getById(Integer id) {
        EmailInbox ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("EmailInbox not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public EmailInboxDto create(EmailInboxDto dto) {
        EmailInbox ent = mapper.toEntity(dto);
        EmailInbox saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public EmailInboxDto update(Integer id, EmailInboxDto dto) {
        EmailInbox ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("EmailInbox not found: " + id));
        mapper.updateFromDto(dto, ent);
        EmailInbox saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        EmailInbox ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("EmailInbox not found: " + id));
        repository.delete(ent);
    }
}
