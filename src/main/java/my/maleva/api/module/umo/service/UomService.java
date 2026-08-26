package my.maleva.api.module.umo.service;

import my.maleva.api.module.umo.dto.UomDto;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.module.umo.mapper.UomMapper;
import my.maleva.api.module.umo.entity.Uom;
import my.maleva.api.module.umo.repository.UomRepository;
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
        // Soft delete: items may reference this UOM, so the row is deactivated
        // rather than removed — same rule as the other masters.
        Uom uom = uomRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("UOM not found: " + id));
        uom.setActive(0);
        uom.setModifiedDate(LocalDateTime.now());
        uomRepository.save(uom);
    }

    /**
     * Process UOM using SP_UOM stored procedure logic
     * Incorporates batch processing with check flag
     *
     * @param dto - UomDto with UOM data
     * @param companyId - Company ID for the UOM
     * @param checkFlag - If 1, checks if UOM already exists before insert
     * @return Processed UomDto
     */
    @Transactional
    public UomDto processUom(UomDto dto, Integer companyId, Integer checkFlag) {
        // Set company ID
        dto.setCompanyRefId(companyId);

        // SP_UOM logic: If Check flag = 1, check if UOM exists by description
        if (checkFlag != null && checkFlag == 1) {
            // Query: SELECT Id from UOM WHERE CompanyRefId=companyId AND Description=description AND Active=1
            Uom existing = uomRepository.findByDescriptionAndCompanyRefIdAndActive(
                    dto.getDescription(), companyId, 1);

            if (existing != null) {
                // Found active record - UPDATE
                return update(existing.getId(), dto);
            }
        }

        // Standard insert/update logic
        if (dto.getId() == null || dto.getId() == 0) {
            // New record - INSERT
            return create(dto);
        } else {
            // Existing record - UPDATE
            return update(dto.getId(), dto);
        }
    }
}
