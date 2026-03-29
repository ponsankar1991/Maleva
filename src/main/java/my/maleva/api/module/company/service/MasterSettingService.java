package my.maleva.api.module.company.service;

import my.maleva.api.module.company.dto.MasterSettingDto;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.module.company.mapper.MasterSettingMapper;
import my.maleva.api.module.company.entity.MasterSetting;
import my.maleva.api.module.company.repository.MasterSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MasterSettingService {

    private final MasterSettingRepository repository;
    private final MasterSettingMapper mapper;

    public MasterSettingService(MasterSettingRepository repository, MasterSettingMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<MasterSettingDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public MasterSettingDto getById(Integer id) {
        MasterSetting ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("MasterSetting not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public MasterSettingDto create(MasterSettingDto dto) {
        LocalDateTime now = LocalDateTime.now();
        MasterSetting ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        MasterSetting saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public MasterSettingDto update(Integer id, MasterSettingDto dto) {
        MasterSetting ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("MasterSetting not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        MasterSetting saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        MasterSetting ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("MasterSetting not found: " + id));
        repository.delete(ent);
    }
}
