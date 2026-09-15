package my.maleva.api.module.supplier.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.common.config.QneProperties;
import my.maleva.api.integration.qne.QneCall;
import my.maleva.api.integration.qne.QneGateway;
import my.maleva.api.integration.qne.QnePayloads;
import my.maleva.api.integration.qne.QnePushResult;
import my.maleva.api.integration.qne.dto.QneSupplierRequest;
import my.maleva.api.integration.qne.dto.QneSupplierResponse;
import my.maleva.api.module.master.entity.SymbolMaster;
import my.maleva.api.module.master.repository.SymbolMasterRepository;
import my.maleva.api.module.supplier.dto.SupplierDto;
import my.maleva.api.module.supplier.dto.SupplierQneOutcome;
import my.maleva.api.module.supplier.entity.Supplier;
import my.maleva.api.module.supplier.repository.SupplierRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * QNE sync for suppliers — the Java port of the QNE half of legacy
 * {@code SupplierServices.InsertSupplier}, and of {@code UpdateSupplierId1}'s backfill.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierQneService {

    private final QneGateway gateway;
    private final QneProperties properties;
    private final SupplierRepository suppliers;
    private final SymbolMasterRepository symbols;

    /**
     * The push {@code InsertSupplier} ran once {@code SP_Supplier} returned
     * Result = 1. Call it after the save has COMMITTED and outside any
     * transaction, as legacy did — the supplier stays saved whatever QNE says.
     *
     * <ol>
     *   <li>{@code if (qneapilist.qneapi == true)} — {@code qne.enabled} here.</li>
     *   <li>{@code if (Id == 0 || QNECode == "" || QNECode == null)} → create,
     *       judged on the row as just saved. The else branch built a
     *       {@code Type = 3} update payload that was never dispatched, so an
     *       existing QNE supplier gets nothing, exactly as before.</li>
     *   <li>Currency = {@code SymbolMaster.SName} for the company, {@code ?? ""}.</li>
     *   <li>Payload from the values AS TYPED ({@code objcustomer[0]}), not the
     *       upper-cased stored row — see {@link #buildRequest}.</li>
     *   <li>On success {@code update Supplier set QNEId, QNECode}; on a refusal,
     *       QNE's message back to the screen.</li>
     * </ol>
     *
     * <p>Takes the saved row from the caller rather than reading it again: the
     * save has just loaded it, and every read is a trip to a remote database.
     *
     * <p>One difference: legacy's catch around the push logged an exception and
     * still answered "Supplier created". That is reported as FAILED here.
     *
     * @param saved the supplier as stored by the save that just committed
     * @param typed the payload the operator submitted
     */
    public SupplierQneOutcome pushSaved(SupplierDto saved, SupplierDto typed) {
        if (!properties.isEnabled()) {
            return SupplierQneOutcome.disabled();
        }
        if (!QnePayloads.isBlank(saved.getQneCode())) {
            return SupplierQneOutcome.alreadyInQne(saved.getQneId(), saved.getQneCode());
        }

        try {
            QneSupplierRequest request = buildRequest(typed,
                    currencyName(typed.getSymbolRefid(), saved.getCompanyRefId()),
                    properties.getControlCodes().getSupplier());

            QneCall<QneSupplierResponse> call = gateway.createSupplier(request);
            if (!call.success()) {
                // _logErrors.WriteDirectLog(ro1.Message, "InsertSupplier-QNE")
                log.warn("InsertSupplier-QNE: QNE refused supplier {}: {}", saved.getId(), call.message());
                return SupplierQneOutcome.failed(call.message());
            }

            QneSupplierResponse created = call.data();
            // if (result1 != null) — only then is the identity written back.
            if (created == null) {
                return SupplierQneOutcome.pushed(null, null);
            }
            suppliers.claimQneIdentity(saved.getId(), created.getId(), created.getCompanyCode());
            return SupplierQneOutcome.pushed(created.getId(), created.getCompanyCode());
        } catch (RuntimeException ex) {
            log.error("InsertSupplier-QNE: push for supplier {} failed", saved.getId(), ex);
            return SupplierQneOutcome.failed("The QNE push could not be completed: " + ex.getMessage());
        }
    }

    /** Repairs suppliers whose QNE code is known but whose QNE GUID was never stored. */
    public QnePushResult backfill(Integer companyRefId) {
        List<Supplier> pending = suppliers.findQneBackfillCandidates(companyRefId);
        if (pending.isEmpty()) {
            return QnePushResult.ok(null, null, null, "No suppliers waiting for a QNE id");
        }

        int repaired = 0;
        List<String> codes = pending.stream().map(Supplier::getQneCode).toList();
        for (List<String> chunk : QnePayloads.chunks(codes, 100)) {
            QneCall<List<QneSupplierResponse>> call = gateway.findSuppliersByCompanyCodes(chunk);
            if (!call.success()) {
                return QnePushResult.rejected(call.message());
            }
            for (QneSupplierResponse match : call.data()) {
                if (!QnePayloads.isBlank(match.getCompanyCode())) {
                    repaired += suppliers.backfillQneId(companyRefId, match.getCompanyCode(), match.getId());
                }
            }
        }
        return QnePushResult.ok(null, null, null,
                "Backfilled QNE ids for " + repaired + " of " + pending.size() + " suppliers");
    }

    /**
     * {@code SELECT S.SName FROM SymbolMaster S WHERE S.CompanyRefId = Comid AND S.Id = SymbolRefid}, {@code ?? ""}.
     */
    private String currencyName(Integer symbolId, Integer companyId) {
        if (symbolId == null || symbolId == 0) {
            return "";
        }
        return symbols.findByIdAndCompanyRefId(symbolId, companyId)
                .map(SymbolMaster::getSName)
                .orElse("");
    }

    /**
     * {@code SupplierQNEInsertModel}, field for field. Built from the payload
     * as typed, as legacy built it from {@code objcustomer[0]}: SP_Supplier
     * upper-cases the stored copy, but QNE was always sent the operator's own
     * casing, and years of QNE suppliers look like that.
     *
     * <p>ContactPerson is {@code City}. On this screen City is the PIC Name box
     * (the legacy page posted its city box there by mistake — see
     * supplier.contract.ts), so QNE now receives the person in charge.
     */
    static QneSupplierRequest buildRequest(SupplierDto typed, String currency, String controlAccount) {
        String[] address = QnePayloads.addressChunks(typed.getAddress1());
        return QneSupplierRequest.builder()
                .companyName(typed.getSupplierName())
                .companyName2(typed.getSupplierName())
                .controlAccount(controlAccount)
                .currency(currency)
                .address1(address[0])
                .address2(address[1])
                .address3(address[2])
                .address4(address[3])
                .isProspect(false)
                .isSuspended(false)
                .isExceedCreditAllowed(false)
                .isTaxExempted(false)
                .contactPerson(typed.getCity())
                .email(typed.getOEmail())
                .phoneNo1(typed.getOPhone())
                .build();
    }
}
