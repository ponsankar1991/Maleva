package my.maleva.api.service;

import my.maleva.api.dto.UomDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.UomMapper;
import my.maleva.api.model.Uom;
import my.maleva.api.repo.UomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UomService {

    private final UomRepository uomRepository;
    private final UomMapper uomMapper;

    public UomService(UomRepository uomRepository, UomMapper uomMapper) {
        this.uomRepository = uomRepository;
        this.uomMapper = uomMapper;
    }

    public List<UomDto> listAll() {
        return uomRepository.findAll().stream().map(uomMapper::toDto).collect(Collectors.toList());
    }

    public UomDto getById(Integer id) {
        Uom uom = uomRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("UOM not found: " + id));
        return uomMapper.toDto(uom);
    }

    @Transactional
    public UomDto create(UomDto dto) {
        LocalDateTime now = LocalDateTime.now();
        Uom uom = uomMapper.toEntity(dto);
        uom.setCreatedDate(now);
        uom.setModifiedDate(now);
        Uom saved = uomRepository.save(uom);
        return uomMapper.toDto(saved);
    }

    @Transactional
    public UomDto update(Integer id, UomDto dto) {
        Uom uom = uomRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("UOM not found: " + id));
        uomMapper.updateFromDto(dto, uom);
        uom.setModifiedDate(LocalDateTime.now());
        Uom saved = uomRepository.save(uom);
        return uomMapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        Uom uom = uomRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("UOM not found: " + id));
        uomRepository.delete(uom);
    }
}
