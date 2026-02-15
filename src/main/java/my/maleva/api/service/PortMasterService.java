package my.maleva.api.service;

import my.maleva.api.dto.PortMasterDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.exception.InvalidRequestException;
import my.maleva.api.mapper.PortMasterMapper;
import my.maleva.api.model.PortMaster;
import my.maleva.api.repo.PortMasterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PortMasterService {

    private final PortMasterRepository portMasterRepository;
    private final PortMasterMapper portMasterMapper;

    public PortMasterService(
            PortMasterRepository portMasterRepository,
            PortMasterMapper portMasterMapper) {
        this.portMasterRepository = portMasterRepository;
        this.portMasterMapper = portMasterMapper;
    }

    /**
     * Get all active port records by company
     */
    public List<PortMasterDto> listAll() {
        return portMasterRepository.findByCompanyRefIdAndActiveNot(0, 2)
                .stream()
                .map(portMasterMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get port record by ID
     */
    public PortMasterDto getById(Integer id) {
        PortMaster port = portMasterRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Port Master not found: " + id));
        return portMasterMapper.toDto(port);
    }

    /**
     * Get all ports by company
     */
    public List<PortMasterDto> getByCompany(Integer companyId) {
        return portMasterRepository.findByCompanyRefId(companyId)
                .stream()
                .map(portMasterMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get active ports by company
     */
    public List<PortMasterDto> getActiveByCompany(Integer companyId) {
        return portMasterRepository.findByCompanyRefIdAndActive(companyId, 1)
                .stream()
                .map(portMasterMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Create new port record (Implements SP_PortMaster create logic)
     */
    @Transactional
    public PortMasterDto create(PortMasterDto dto) {
        if (dto.getCompanyRefId() == null) {
            throw new InvalidRequestException("Company reference ID is required");
        }

        if (dto.getPortName() == null || dto.getPortName().trim().isEmpty()) {
            throw new InvalidRequestException("Port name is required");
        }

        // Check for duplicate port name in same company
        if (portMasterRepository.existsByCompanyRefIdAndPortName(dto.getCompanyRefId(), dto.getPortName())) {
            throw new InvalidRequestException("Port name '" + dto.getPortName() + "' already exists for this company");
        }

        LocalDateTime now = LocalDateTime.now();

        // Create port record (SP_PortMaster insert logic)
        PortMaster port = PortMaster.builder()
                .companyRefId(dto.getCompanyRefId())
                .portName(dto.getPortName())
                .active(dto.getActive() != null ? dto.getActive() : 1)
                .createdDate(now)
                .modifiedDate(now)
                .modifiedBy(dto.getModifiedBy() != null ? dto.getModifiedBy() : "SYSTEM")
                .build();

        PortMaster saved = portMasterRepository.save(port);
        return portMasterMapper.toDto(saved);
    }

    /**
     * Create multiple port records in batch (Implements SP_PortMaster bulk logic)
     */
    @Transactional
    public List<PortMasterDto> createBatch(Integer companyId, List<PortMasterDto> dtos) {
        if (companyId == null) {
            throw new InvalidRequestException("Company reference ID is required");
        }

        if (dtos == null || dtos.isEmpty()) {
            throw new InvalidRequestException("Port details list cannot be empty");
        }

        LocalDateTime now = LocalDateTime.now();
        List<PortMaster> ports = new java.util.ArrayList<>();

        for (PortMasterDto dto : dtos) {
            if (dto.getPortName() == null || dto.getPortName().trim().isEmpty()) {
                throw new InvalidRequestException("Port name is required for all records");
            }

            // Check for duplicate in batch
            if (portMasterRepository.existsByCompanyRefIdAndPortName(companyId, dto.getPortName())) {
                throw new InvalidRequestException("Port name '" + dto.getPortName() + "' already exists for this company");
            }

            PortMaster port;

            // SP_PortMaster logic: if Id is 0 or null, create new; otherwise update
            if (dto.getId() == null || dto.getId() == 0) {
                port = PortMaster.builder()
                        .companyRefId(companyId)
                        .portName(dto.getPortName())
                        .active(dto.getActive() != null ? dto.getActive() : 1)
                        .createdDate(now)
                        .modifiedDate(now)
                        .modifiedBy(dto.getModifiedBy() != null ? dto.getModifiedBy() : "SYSTEM")
                        .build();
            } else {
                port = portMasterRepository.findById(dto.getId())
                        .orElseThrow(() -> new EntityNotFoundException("Port Master not found: " + dto.getId()));
                port.setPortName(dto.getPortName());
                port.setActive(dto.getActive() != null ? dto.getActive() : port.getActive());
                port.setModifiedDate(now);
                port.setModifiedBy(dto.getModifiedBy() != null ? dto.getModifiedBy() : "SYSTEM");
            }

            ports.add(port);
        }

        // Save all in transaction
        List<PortMaster> saved = portMasterRepository.saveAll(ports);
        return saved.stream()
                .map(portMasterMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Update port record (Implements SP_PortMaster update logic)
     */
    @Transactional
    public PortMasterDto update(Integer id, PortMasterDto dto) {
        PortMaster port = portMasterRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Port Master not found: " + id));

        // Validate port name is not empty
        if (dto.getPortName() != null && !dto.getPortName().trim().isEmpty()) {
            // Check for duplicate (excluding current record)
            portMasterRepository.findByCompanyRefIdAndPortName(port.getCompanyRefId(), dto.getPortName())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(id)) {
                            throw new InvalidRequestException("Port name '" + dto.getPortName() + "' already exists");
                        }
                    });

            port.setPortName(dto.getPortName());
        }

        // Update other fields
        if (dto.getActive() != null) {
            port.setActive(dto.getActive());
        }

        LocalDateTime now = LocalDateTime.now();
        port.setModifiedDate(now);
        port.setModifiedBy(dto.getModifiedBy() != null ? dto.getModifiedBy() : "SYSTEM");

        PortMaster saved = portMasterRepository.save(port);
        return portMasterMapper.toDto(saved);
    }

    /**
     * Delete port record
     */
    @Transactional
    public void delete(Integer id) {
        PortMaster port = portMasterRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Port Master not found: " + id));
        portMasterRepository.delete(port);
    }

    /**
     * Search ports by name within company
     */
    public List<PortMasterDto> search(Integer companyId, String portName) {
        if (companyId == null) {
            throw new InvalidRequestException("Company reference ID is required");
        }

        if (portName == null || portName.trim().isEmpty()) {
            return getActiveByCompany(companyId);
        }

        return portMasterRepository.searchByCompanyAndPortName(companyId, portName)
                .stream()
                .map(portMasterMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Soft delete by setting active to 2
     */
    @Transactional
    public void softDelete(Integer id) {
        PortMaster port = portMasterRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Port Master not found: " + id));

        port.setActive(2);
        port.setModifiedDate(LocalDateTime.now());
        port.setModifiedBy("SYSTEM");

        portMasterRepository.save(port);
    }

    /**
     * Activate port record
     */
    @Transactional
    public PortMasterDto activate(Integer id) {
        PortMaster port = portMasterRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Port Master not found: " + id));

        port.setActive(1);
        port.setModifiedDate(LocalDateTime.now());
        port.setModifiedBy("SYSTEM");

        PortMaster saved = portMasterRepository.save(port);
        return portMasterMapper.toDto(saved);
    }

    /**
     * Deactivate port record
     */
    @Transactional
    public PortMasterDto deactivate(Integer id) {
        PortMaster port = portMasterRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Port Master not found: " + id));

        port.setActive(0);
        port.setModifiedDate(LocalDateTime.now());
        port.setModifiedBy("SYSTEM");

        PortMaster saved = portMasterRepository.save(port);
        return portMasterMapper.toDto(saved);
    }
}

