package my.maleva.api.service;

import my.maleva.api.dto.CardMasterDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.CardMasterMapper;
import my.maleva.api.model.CardMaster;
import my.maleva.api.repo.CardMasterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CardMasterService {

    private final CardMasterRepository repository;
    private final CardMasterMapper mapper;

    public CardMasterService(CardMasterRepository repository, CardMasterMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<CardMasterDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public CardMasterDto getById(Integer id) {
        CardMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("CardMaster not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public CardMasterDto create(CardMasterDto dto) {
        LocalDateTime now = LocalDateTime.now();
        CardMaster ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        CardMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public CardMasterDto update(Integer id, CardMasterDto dto) {
        CardMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("CardMaster not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        CardMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        CardMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("CardMaster not found: " + id));
        repository.delete(ent);
    }
}
