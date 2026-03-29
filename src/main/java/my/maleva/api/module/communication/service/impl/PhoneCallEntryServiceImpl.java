package my.maleva.api.module.communication.service.impl;

import my.maleva.api.module.communication.dto.PhoneCallEntryDto;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.module.communication.mapper.PhoneCallEntryMapper;
import my.maleva.api.module.communication.entity.PhoneCallEntry;
import my.maleva.api.module.communication.repository.PhoneCallEntryRepository;
import my.maleva.api.module.communication.service.PhoneCallEntryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PhoneCallEntryServiceImpl implements PhoneCallEntryService {

    private final PhoneCallEntryRepository repository;
    private final PhoneCallEntryMapper mapper;

    public PhoneCallEntryServiceImpl(PhoneCallEntryRepository repository, PhoneCallEntryMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public PhoneCallEntryDto create(PhoneCallEntryDto dto) {
        PhoneCallEntry ent = mapper.toEntity(dto);
        // keep created/modified handling simple; entity only has callDate/remarks
        PhoneCallEntry saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Override
    public PhoneCallEntryDto getById(Integer id) {
        PhoneCallEntry ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("PhoneCallEntry not found: " + id));
        return mapper.toDto(ent);
    }

    @Override
    public List<PhoneCallEntryDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PhoneCallEntryDto update(Integer id, PhoneCallEntryDto dto) {
        PhoneCallEntry ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("PhoneCallEntry not found: " + id));
        mapper.updateFromDto(dto, ent);
        PhoneCallEntry saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        PhoneCallEntry ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("PhoneCallEntry not found: " + id));
        repository.delete(ent);
    }
}
