package my.maleva.api.service;

import my.maleva.api.dto.CountryMasterDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.CountryMasterMapper;
import my.maleva.api.model.CountryMaster;
import my.maleva.api.repo.CountryMasterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CountryMasterService {

    private final CountryMasterRepository repository;
    private final CountryMasterMapper mapper;

    public CountryMasterService(CountryMasterRepository repository, CountryMasterMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<CountryMasterDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public CountryMasterDto getById(Integer id) {
        CountryMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("CountryMaster not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public CountryMasterDto create(CountryMasterDto dto) {
        CountryMaster ent = mapper.toEntity(dto);
        CountryMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public CountryMasterDto update(Integer id, CountryMasterDto dto) {
        CountryMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("CountryMaster not found: " + id));
        mapper.updateFromDto(dto, ent);
        CountryMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        CountryMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("CountryMaster not found: " + id));
        repository.delete(ent);
    }
}
