package my.maleva.api.module.master.service;

import my.maleva.api.module.master.dto.BankMasterDto;
import my.maleva.api.common.dto.ComboListModel;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.module.master.mapper.BankMasterMapper;
import my.maleva.api.module.master.entity.BankMaster;
import my.maleva.api.module.master.repository.BankMasterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BankMasterService {

    private static final Logger logger = LoggerFactory.getLogger(BankMasterService.class);
    private final BankMasterRepository repository;
    private final BankMasterMapper mapper;

    public BankMasterService(BankMasterRepository repository, BankMasterMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<BankMasterDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public BankMasterDto getById(Integer id) {
        BankMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("BankMaster not found: " + id));
        return mapper.toDto(ent);
    }

    /**
     * Get active banks for a company as combo list
     * Equivalent to .NET: GetBank(int Comid)
     * Query: SELECT Id, AccountName FROM BankMaster
     *        WHERE CompanyRefId = Comid AND Active = 1
     *
     * @param companyRefId Company ID
     * @return List of ComboListModel with Id and AccountName
     */
    @Transactional(readOnly = true)
    public List<ComboListModel> getBank(Integer companyRefId) {
        logger.info("Fetching active banks for company: {}", companyRefId);
        try {
            List<ComboListModel> banks = repository.findActiveBanksByCompany(companyRefId);
            logger.info("Found {} banks for company: {}", banks.size(), companyRefId);
            return banks;
        } catch (Exception ex) {
            logger.error("Error fetching banks for company: {}", companyRefId, ex);
            throw new RuntimeException("Error fetching banks: " + ex.getMessage(), ex);
        }
    }

    // ...existing code...

    @Transactional
    public BankMasterDto create(BankMasterDto dto) {
        LocalDateTime now = LocalDateTime.now();
        BankMaster ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        BankMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public BankMasterDto update(Integer id, BankMasterDto dto) {
        BankMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("BankMaster not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        BankMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        BankMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("BankMaster not found: " + id));
        repository.delete(ent);
    }
}
