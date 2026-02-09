package my.maleva.api.service;

import my.maleva.api.dto.ItemMasterCStockDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.ItemMasterCStockMapper;
import my.maleva.api.model.ItemMasterCStock;
import my.maleva.api.repo.ItemMasterCStockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemMasterCStockService {

    private final ItemMasterCStockRepository repository;
    private final ItemMasterCStockMapper mapper;

    public ItemMasterCStockService(ItemMasterCStockRepository repository, ItemMasterCStockMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<ItemMasterCStockDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public ItemMasterCStockDto getById(Integer id) {
        ItemMasterCStock ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("ItemMasterCStock not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public ItemMasterCStockDto create(ItemMasterCStockDto dto) {
        LocalDateTime now = LocalDateTime.now();
        ItemMasterCStock ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        ItemMasterCStock saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public ItemMasterCStockDto update(Integer id, ItemMasterCStockDto dto) {
        ItemMasterCStock ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("ItemMasterCStock not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        ItemMasterCStock saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        ItemMasterCStock ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("ItemMasterCStock not found: " + id));
        repository.delete(ent);
    }
}
