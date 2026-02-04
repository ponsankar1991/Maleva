package my.maleva.api.service;

import my.maleva.api.dto.ItemMasterDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.ItemMasterMapper;
import my.maleva.api.model.ItemMaster;
import my.maleva.api.repo.ItemMasterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemMasterService {

    private final ItemMasterRepository repository;
    private final ItemMasterMapper mapper;

    public ItemMasterService(ItemMasterRepository repository, ItemMasterMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<ItemMasterDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public ItemMasterDto getById(Integer id) {
        ItemMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("ItemMaster not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public ItemMasterDto create(ItemMasterDto dto) {
        LocalDateTime now = LocalDateTime.now();
        ItemMaster ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        ItemMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public ItemMasterDto update(Integer id, ItemMasterDto dto) {
        ItemMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("ItemMaster not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        ItemMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        ItemMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("ItemMaster not found: " + id));
        repository.delete(ent);
    }
}
