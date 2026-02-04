package my.maleva.api.service;

import my.maleva.api.dto.EnquiryMasterDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.EnquiryMasterMapper;
import my.maleva.api.model.EnquiryMaster;
import my.maleva.api.repo.EnquiryMasterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnquiryMasterService {

    private final EnquiryMasterRepository repository;
    private final EnquiryMasterMapper mapper;

    public EnquiryMasterService(EnquiryMasterRepository repository, EnquiryMasterMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<EnquiryMasterDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public EnquiryMasterDto getById(Integer id) {
        EnquiryMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("EnquiryMaster not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public EnquiryMasterDto create(EnquiryMasterDto dto) {
        LocalDateTime now = LocalDateTime.now();
        EnquiryMaster ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        EnquiryMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public EnquiryMasterDto update(Integer id, EnquiryMasterDto dto) {
        EnquiryMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("EnquiryMaster not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        EnquiryMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        EnquiryMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("EnquiryMaster not found: " + id));
        repository.delete(ent);
    }
}
