package my.maleva.api.module.employee.service.impl;

import my.maleva.api.module.employee.dto.EmployeePortMasterDto;
import my.maleva.api.module.employee.entity.EmployeePortMaster;
import my.maleva.api.module.employee.mapper.EmployeePortMasterMapper;
import my.maleva.api.module.employee.repository.EmployeePortMasterRepository;
import my.maleva.api.module.employee.service.EmployeePortMasterService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeePortMasterServiceImpl implements EmployeePortMasterService {

    private final EmployeePortMasterRepository repository;
    private final EmployeePortMasterMapper mapper;

    public EmployeePortMasterServiceImpl(EmployeePortMasterRepository repository, EmployeePortMasterMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public EmployeePortMasterDto create(EmployeePortMasterDto dto) {
        EmployeePortMaster entity = mapper.toEntity(dto);
        
        entity.setId(null);
        
        if (entity.getActive() == null) {
            entity.setActive(1);
        }
        
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedDate(now);
        entity.setModifiedDate(now);
        
        if (entity.getModifiedBy() == null || entity.getModifiedBy().isEmpty()) {
            entity.setModifiedBy("SA");
        }

        EmployeePortMaster saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public List<EmployeePortMasterDto> bulkCreate(List<EmployeePortMasterDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return List.of();
        }

        Integer companyRefId = dtos.get(0).getCompanyRefId();
        Integer employeeRefId = dtos.get(0).getEmployeeRefId();

        // 1. CLEAR EXISTING: Prevent duplicates by fully wiping the old ports for this employee
        repository.deleteByCompanyRefIdAndEmployeeRefId(companyRefId, employeeRefId);

        // 2. FILTER DUPLICATES IN REQUEST: Ensure the frontend didn't send the same port twice in the array
        List<EmployeePortMasterDto> uniqueDtos = dtos.stream()
                .collect(Collectors.toMap(
                        dto -> dto.getPortRefId(), 
                        dto -> dto, 
                        (existing, replacement) -> existing)) // Keep first if duplicate
                .values().stream()
                .collect(Collectors.toList());

        // 3. INSERT NEW: Save the clean mapping
        List<EmployeePortMaster> entities = uniqueDtos.stream().map(dto -> {
            EmployeePortMaster entity = mapper.toEntity(dto);
            entity.setId(null);
            
            if (entity.getActive() == null) {
                entity.setActive(1);
            }
            
            LocalDateTime now = LocalDateTime.now();
            entity.setCreatedDate(now);
            entity.setModifiedDate(now);
            
            if (entity.getModifiedBy() == null || entity.getModifiedBy().isEmpty()) {
                entity.setModifiedBy("SA");
            }
            return entity;
        }).collect(Collectors.toList());

        List<EmployeePortMaster> saved = repository.saveAll(entities);
        return saved.stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeePortMasterDto> getByEmployeeRefId(Integer companyRefId, Integer employeeRefId) {
        return repository.findByCompanyRefIdAndEmployeeRefIdAndActive(companyRefId, employeeRefId, 1)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
