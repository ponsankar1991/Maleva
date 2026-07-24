package my.maleva.api.module.employee.service.impl;

import lombok.RequiredArgsConstructor;
import my.maleva.api.module.employee.dto.CapabilityDto;
import my.maleva.api.module.employee.entity.Capability;
import my.maleva.api.module.employee.mapper.CapabilityMapper;
import my.maleva.api.module.employee.repository.CapabilityRepository;
import my.maleva.api.module.employee.service.CapabilityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CapabilityServiceImpl implements CapabilityService {

    private final CapabilityRepository repository;
    private final CapabilityMapper mapper;

    @Override
    public List<CapabilityDto> getActiveCapabilities() {
        List<Capability> capabilities = repository.findByIsActiveTrue();
        return mapper.toDtoList(capabilities);
    }
}
