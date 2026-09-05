package my.maleva.api.module.zbentry.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.common.exception.InvalidDateRangeException;
import my.maleva.api.module.zbentry.dto.ZbEntryBulkSaveRequest;
import my.maleva.api.module.zbentry.dto.ZbEntryResponse;
import my.maleva.api.module.zbentry.dto.ZbEntrySaveRequest;
import my.maleva.api.module.zbentry.dto.ZbEntrySaveResult;
import my.maleva.api.module.zbentry.dto.ZbEntrySearchRequest;
import my.maleva.api.module.zbentry.entity.ZbEntry;
import my.maleva.api.module.zbentry.mapper.ZbEntryMapper;
import my.maleva.api.module.zbentry.repository.ZbEntryProcedureRepository;
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
    private final ZbEntryProcedureRepository zbEntryProcedureRepository;
    private final ZbEntryMapper zbEntryMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ZbEntryResponse> searchZbEntries(ZbEntrySearchRequest request, Pageable pageable) {
        if (request.getFromDate().isAfter(request.getToDate())) {
            log.warn("Validation failed: fromDate {} is after toDate {}", request.getFromDate(), request.getToDate());
            throw new InvalidDateRangeException("FromDate must be less than or equal to ToDate");
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
    @Transactional(readOnly = true)
    public ZbEntryResponse getZbEntry(Integer id, Integer companyRefId) {
        return zbEntryRepository.findByIdAndCompanyRefId(id, companyRefId)
                .map(zbEntryMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException(
                        "ZbEntry not found with ID: " + id + " for Company ID: " + companyRefId));
    }

    /**
     * Hands the batch to {@code SP_ZBEntryMaster}.
     *
     * <p>No {@code @Transactional} on purpose: the procedure opens and commits
     * its own transaction, and wrapping it in a second one only adds a Spring
     * transaction that has nothing to roll back — the procedure has already
     * committed or rolled itself back by the time this returns.
     */
    @Override
    public ZbEntrySaveResult bulkSaveZbEntries(ZbEntryBulkSaveRequest request) {
        log.info("Saving {} ZbEntry row(s) for companyRefId={}",
                request.getDetails().size(), request.getCompanyRefId());

        String detailsJson = toProcedureJson(request);

        ZbEntrySaveResult result = zbEntryProcedureRepository.save(detailsJson, request.getCompanyRefId());

        if (!result.isSuccess()) {
            log.error("SP_ZBEntryMaster failed for companyRefId={}: {}",
                    request.getCompanyRefId(), result.getMsg());
        } else {
            log.info("Saved ZbEntry for companyRefId={}, last id={}",
                    request.getCompanyRefId(), result.getId());
        }

        return result;
    }

    /**
     * Builds the JSON the procedure's {@code OPENJSON ... WITH} clause expects.
     *
     * <p>Written field by field rather than by serialising the DTO, for two
     * reasons. The procedure reads PascalCase paths (`$.ZBNumber`, `$.PTWNo`),
     * which no camelCase DTO produces; and a missing or JSON-null value would
     * land in the table as SQL NULL, where legacy always wrote an empty string —
     * legacy did that with a blind {@code Replace("null", "\"\"")} over the whole
     * document, which also corrupts any real value containing those four letters.
     * Emitting "" for absent text keeps the stored shape identical without that
     * side effect.
     */
    private String toProcedureJson(ZbEntryBulkSaveRequest request) {
        ArrayNode rows = objectMapper.createArrayNode();

        for (ZbEntrySaveRequest detail : request.getDetails()) {
            ObjectNode row = rows.addObject();
            // Id 0 tells the procedure to insert; anything else updates.
            row.put("Id", detail.getId() == null ? 0 : detail.getId());
            row.put("CompanyRefId", request.getCompanyRefId());
            row.put("EntryDate", text(detail.getEntryDate()));
            row.put("ChargeType", text(detail.getChargeType()));
            row.put("ZBType", text(detail.getZbType()));
            row.put("PortChart", text(detail.getPortChart()));
            row.put("ZBNumber", text(detail.getZbNumber()));
            row.put("VesselName", text(detail.getVesselName()));
            row.put("JobNumber", text(detail.getJobNumber()));
            row.put("PTWNo", text(detail.getPtwNo()));
            row.put("Amount", text(detail.getAmount()));
            // Active 1 = live, 2 = deleted; the list query filters out 2.
            row.put("Active", detail.getActive() == null ? 1 : detail.getActive());
        }

        try {
            return objectMapper.writeValueAsString(rows);
        } catch (JsonProcessingException e) {
            // Every value is a String or an int, so this cannot fail in practice.
            throw new IllegalStateException("Could not build the ZB entry payload", e);
        }
    }

    private String text(String value) {
        return value == null ? "" : value.trim();
    }
}
