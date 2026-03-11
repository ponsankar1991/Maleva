package my.maleva.api.service;

import my.maleva.api.dto.SequenceNoMasterDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.SequenceNoMasterMapper;
import my.maleva.api.model.SequenceNoMaster;
import my.maleva.api.repo.SequenceNoMasterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for SequenceNoMaster business logic
 * Handles sequence number generation and management for various document types
 */
@Service
public class SequenceNoMasterService {

    private final SequenceNoMasterRepository repository;
    private final SequenceNoMasterMapper mapper;

    public SequenceNoMasterService(SequenceNoMasterRepository repository, SequenceNoMasterMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /**
     * Get all sequence numbers
     *
     * @return list of all sequence DTOs
     */
    @Transactional(readOnly = true)
    public List<SequenceNoMasterDto> listAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get sequence by ID
     *
     * @param id the sequence ID
     * @return the sequence DTO
     * @throws EntityNotFoundException if sequence not found
     */
    @Transactional(readOnly = true)
    public SequenceNoMasterDto getById(Integer id) {
        SequenceNoMaster entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("SequenceNoMaster not found with id: " + id));
        return mapper.toDto(entity);
    }

    /**
     * Get all sequences for a specific company
     *
     * @param companyRefId the company ID
     * @return list of sequence DTOs for the company
     */
    @Transactional(readOnly = true)
    public List<SequenceNoMasterDto> getByCompanyId(Integer companyRefId) {
        return repository.findByCompanyRefId(companyRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Create a new sequence number record
     *
     * @param dto the sequence DTO
     * @return the created sequence DTO with ID
     */
    @Transactional
    public SequenceNoMasterDto create(SequenceNoMasterDto dto) {
        SequenceNoMaster entity = mapper.toEntity(dto);
        if (entity.getSequenceDate() == null) {
            entity.setSequenceDate(LocalDateTime.now());
        }
        if (entity.getSequenceYear() == null) {
            entity.setSequenceYear(LocalDateTime.now().getYear());
        }
        if (entity.getSequenceMonth() == null) {
            entity.setSequenceMonth(LocalDateTime.now().getMonthValue());
        }
        SequenceNoMaster saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    /**
     * Update an existing sequence number record
     *
     * @param id the sequence ID
     * @param dto the updated sequence DTO
     * @return the updated sequence DTO
     * @throws EntityNotFoundException if sequence not found
     */
    @Transactional
    public SequenceNoMasterDto update(Integer id, SequenceNoMasterDto dto) {
        SequenceNoMaster entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("SequenceNoMaster not found with id: " + id));
        mapper.updateFromDto(dto, entity);
        SequenceNoMaster updated = repository.save(entity);
        return mapper.toDto(updated);
    }

    /**
     * Delete a sequence number record
     *
     * @param id the sequence ID
     * @throws EntityNotFoundException if sequence not found
     */
    @Transactional
    public void delete(Integer id) {
        SequenceNoMaster entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("SequenceNoMaster not found with id: " + id));
        repository.delete(entity);
    }

    /**
     * Get the maximum sequence number for a company and bill type
     * Used to generate the next sequence number
     *
     * @param companyId the company ID
     * @param billType the bill type (sequence name)
     * @return the next sequence number as a padded string
     */
    @Transactional(readOnly = true)
    public String getMaxSaleOrderNo(Integer companyId, String billType) {
        // Uses repository method that concatenates 'SaleOrderMaster' + billType internally
        Integer maxSequenceNo = repository.findMaxSequenceNoByCompanyAndBillType(companyId, billType);
        if (maxSequenceNo == null) {
            maxSequenceNo = 0;
        }
        String sequenceNumber = String.format("%09d", maxSequenceNo + 1);
        return billType + sequenceNumber;
    }

    /**
     * Generate the next sequence number for a bill type
     * Equivalent to the .NET MaxSaleOrderNo method
     * Concatenates 'SaleOrderMaster' + billType automatically
     *
     * @param companyId the company ID
     * @param billType the bill type (e.g., "SO", "INV", etc.)
     * @return formatted sequence number with bill type prefix and 9-digit padded number
     */
    @Transactional
    public String generateNextSequenceNo(Integer companyId, String billType) {
        String sequenceName = "SaleOrderMaster" + billType;

        // Get current max sequence number using BillType (repository handles concatenation)
        Integer currentMax = repository.findMaxSequenceNoByCompanyAndBillType(companyId, billType);
        if (currentMax == null) {
            currentMax = 0;
        }

        int nextSequenceNo = currentMax + 1;

        // Create or update the sequence record
        var existingSequence = repository.findByCompanyRefIdAndSequenceName(companyId, sequenceName);

        SequenceNoMaster sequenceRecord;
        if (existingSequence.isPresent()) {
            sequenceRecord = existingSequence.get();
            sequenceRecord.setSequenceNo(nextSequenceNo);
            sequenceRecord.setSequenceDate(LocalDateTime.now());
            sequenceRecord.setSequenceYear(LocalDateTime.now().getYear());
            sequenceRecord.setSequenceMonth(LocalDateTime.now().getMonthValue());
        } else {
            sequenceRecord = SequenceNoMaster.builder()
                    .companyRefId(companyId)
                    .sequenceName(sequenceName)
                    .sequenceNo(nextSequenceNo)
                    .sequenceDate(LocalDateTime.now())
                    .sequenceYear(LocalDateTime.now().getYear())
                    .sequenceMonth(LocalDateTime.now().getMonthValue())
                    .build();
        }

        repository.save(sequenceRecord);

        // Return formatted sequence number: BillType + 9-digit padded number
        String paddedNumber = String.format("%09d", nextSequenceNo);
        return billType + paddedNumber;
    }

    /**
     * Get all sequences for a specific company and year
     *
     * @param companyRefId the company ID
     * @param sequenceYear the year
     * @return list of sequence DTOs for the year
     */
    @Transactional(readOnly = true)
    public List<SequenceNoMasterDto> getSequencesByCompanyAndYear(Integer companyRefId, Integer sequenceYear) {
        return repository.findByCompanyRefIdAndSequenceYear(companyRefId, sequenceYear)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Generate the next PLANNING sequence number
     * Equivalent to the .NET MaxPLANINGNo method
     * Returns formatted sequence number with 'PL' prefix and 9-digit padded number
     * Example: "PL000000001", "PL000000002", etc.
     *
     * @param companyId the company ID
     * @return formatted PLANNING sequence number (PL + 9-digit padded number)
     */
    @Transactional
    public String getMaxPlanningNo(Integer companyId) {
        final String PLANNING_PREFIX = "PL";
        final String SEQUENCE_NAME = "PLANINGMaster";

        // Get current max sequence number for PLANNING
        Integer currentMax = repository.findMaxPlanningSequenceNoByCompany(companyId);
        if (currentMax == null) {
            currentMax = 0;
        }

        int nextSequenceNo = currentMax + 1;

        // Create or update the sequence record
        var existingSequence = repository.findByCompanyRefIdAndSequenceName(companyId, SEQUENCE_NAME);

        SequenceNoMaster sequenceRecord;
        if (existingSequence.isPresent()) {
            sequenceRecord = existingSequence.get();
            sequenceRecord.setSequenceNo(nextSequenceNo);
            sequenceRecord.setSequenceDate(LocalDateTime.now());
            sequenceRecord.setSequenceYear(LocalDateTime.now().getYear());
            sequenceRecord.setSequenceMonth(LocalDateTime.now().getMonthValue());
        } else {
            sequenceRecord = SequenceNoMaster.builder()
                    .companyRefId(companyId)
                    .sequenceName(SEQUENCE_NAME)
                    .sequenceNo(nextSequenceNo)
                    .sequenceDate(LocalDateTime.now())
                    .sequenceYear(LocalDateTime.now().getYear())
                    .sequenceMonth(LocalDateTime.now().getMonthValue())
                    .build();
        }

        repository.save(sequenceRecord);

        // Return formatted sequence number: PL + 9-digit padded number
        String paddedNumber = String.format("%09d", nextSequenceNo);
        return PLANNING_PREFIX + paddedNumber;
    }
}




