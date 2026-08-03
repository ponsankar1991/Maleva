package my.maleva.api.module.zbentry.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.module.zbentry.dto.ZbEntryBulkSaveRequest;
import my.maleva.api.module.zbentry.dto.ZbEntryResponse;
import my.maleva.api.module.zbentry.dto.ZbEntrySaveRequest;
import my.maleva.api.module.zbentry.dto.ZbEntrySearchRequest;
import my.maleva.api.module.zbentry.entity.ZbEntry;
import my.maleva.api.module.zbentry.mapper.ZbEntryMapper;
import my.maleva.api.module.zbentry.repository.ZbEntryRepository;
import my.maleva.api.module.zbentry.service.ZbEntryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ZbEntryServiceImpl implements ZbEntryService {

    private final ZbEntryRepository zbEntryRepository;
    private final ZbEntryMapper zbEntryMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ZbEntryResponse> searchZbEntries(ZbEntrySearchRequest request, Pageable pageable) {
        if (request.getFromDate().isAfter(request.getToDate())) {
            log.warn("Validation failed: fromDate {} is after toDate {}", request.getFromDate(), request.getToDate());
            throw new my.maleva.api.common.exception.InvalidDateRangeException("FromDate must be less than or equal to ToDate");
        }

        log.info("Searching ZbEntry with companyRefId={}, chargeType={}, keyword={}, fromDate={}, toDate={}, page={}, size={}",
                request.getCompanyRefId(), request.getChargeType(), request.getKeyword(),
                request.getFromDate(), request.getToDate(), pageable.getPageNumber(), pageable.getPageSize());

        Page<ZbEntry> entityPage = zbEntryRepository.searchZbEntries(
                request.getCompanyRefId(),
                request.getChargeType(),
                request.getKeyword(),
                request.getFromDate(),
                request.getToDate(),
                pageable
        );

        log.info("Found {} records for ZbEntry search.", entityPage.getTotalElements());

        return entityPage.map(zbEntryMapper::toDto);
    }

    @Override
    @Transactional
    public void bulkSaveZbEntries(ZbEntryBulkSaveRequest request) {
        log.info("Starting bulk save for ZbEntry, companyRefId={}, count={}", 
                 request.getCompanyRefId(), request.getDetails().size());

        for (ZbEntrySaveRequest detail : request.getDetails()) {
            if (detail.getId() == null || detail.getId() == 0) {
                // Insert
                ZbEntry newEntity = zbEntryMapper.toEntity(detail);
                newEntity.setCompanyRefId(request.getCompanyRefId());
                zbEntryRepository.save(newEntity);
            } else {
                // Update
                ZbEntry existingEntity = zbEntryRepository.findByIdAndCompanyRefId(detail.getId(), request.getCompanyRefId())
                        .orElseThrow(() -> new my.maleva.api.common.exception.EntityNotFoundException(
                                "ZbEntry not found with ID: " + detail.getId() + " for Company ID: " + request.getCompanyRefId()));
                
                zbEntryMapper.updateEntity(existingEntity, detail);
                zbEntryRepository.save(existingEntity);
            }
        }
        
        log.info("Successfully processed bulk save for {} records.", request.getDetails().size());
    }
}
