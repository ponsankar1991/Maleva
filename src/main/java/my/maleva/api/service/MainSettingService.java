package my.maleva.api.service;

import my.maleva.api.dto.MainSettingDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.MainSettingMapper;
import my.maleva.api.model.MainSetting;
import my.maleva.api.repo.MainSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MainSettingService {

    private final MainSettingRepository repository;
    private final MainSettingMapper mapper;

    public MainSettingService(MainSettingRepository repository, MainSettingMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<MainSettingDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public MainSettingDto getById(Integer id) {
        MainSetting ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("MainSetting not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public MainSettingDto create(MainSettingDto dto) {
        LocalDateTime now = LocalDateTime.now();
        MainSetting ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        MainSetting saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public MainSettingDto update(Integer id, MainSettingDto dto) {
        MainSetting ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("MainSetting not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        MainSetting saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        MainSetting ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("MainSetting not found: " + id));
        repository.delete(ent);
    }
}
