package my.maleva.api.module.jobs.service;

import my.maleva.api.module.jobs.dto.ItemMasterJobDetailsDto;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.module.jobs.mapper.ItemMasterJobDetailsMapper;
import my.maleva.api.module.jobs.entity.ItemMasterJobDetails;
import my.maleva.api.module.jobs.repository.ItemMasterJobDetailsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemMasterJobDetailsService {

    private final ItemMasterJobDetailsRepository repository;
    private final ItemMasterJobDetailsMapper mapper;

    public ItemMasterJobDetailsService(ItemMasterJobDetailsRepository repository, ItemMasterJobDetailsMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<ItemMasterJobDetailsDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public ItemMasterJobDetailsDto getById(Integer id) {
        ItemMasterJobDetails ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("ItemMasterJobDetails not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public ItemMasterJobDetailsDto create(ItemMasterJobDetailsDto dto) {
        ItemMasterJobDetails ent = mapper.toEntity(dto);
        ItemMasterJobDetails saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public ItemMasterJobDetailsDto update(Integer id, ItemMasterJobDetailsDto dto) {
        ItemMasterJobDetails ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("ItemMasterJobDetails not found: " + id));
        mapper.updateFromDto(dto, ent);
        ItemMasterJobDetails saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        ItemMasterJobDetails ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("ItemMasterJobDetails not found: " + id));
        repository.delete(ent);
    }
}
