package my.maleva.api.module.supplier.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.common.config.QneProperties;
import my.maleva.api.integration.qne.QneAfterCommit;
import my.maleva.api.integration.qne.QneCall;
import my.maleva.api.integration.qne.QneGateway;
import my.maleva.api.integration.qne.QnePayloads;
import my.maleva.api.integration.qne.QnePushResult;
import my.maleva.api.integration.qne.dto.QneSupplierRequest;
import my.maleva.api.integration.qne.dto.QneSupplierResponse;
import my.maleva.api.module.master.entity.SymbolMaster;
import my.maleva.api.module.master.repository.SymbolMasterRepository;
import my.maleva.api.module.supplier.entity.Supplier;
import my.maleva.api.module.supplier.repository.SupplierRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * QNE sync for suppliers — the Java port of the QNE side of legacy
 * {@code SupplierServices} (InsertSupplier's push and UpdateSupplierId1's
 * backfill). Mirror image of the customer sync, but on QNEId/QNECode.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierQneService {

    private final QneGateway gateway;
    private final QneProperties properties;
    private final SupplierRepository suppliers;
    private final SymbolMasterRepository symbols;

    /** Pushes a newly created supplier once its insert commits (see CustomerQneService). */
    public void pushCreatedAfterCommit(Supplier saved) {
        QneAfterCommit.run(() -> {
            QnePushResult result = pushCreated(saved);
            if (!result.success()) {
                log.warn("QNE push for new supplier {} did not complete: {}",
                        saved.getId(), result.message());
            }
        });
    }

    public QnePushResult pushCreated(Supplier supplier) {
        if (!QnePayloads.isBlank(supplier.getQneCode())) {
            return QnePushResult.alreadyPushed(supplier.getQneId(), supplier.getQneCode(),
                    "Supplier already exists in QNE as " + supplier.getQneCode());
        }

        QneSupplierRequest request = buildRequest(
                supplier, currencyName(supplier), properties.getControlCodes().getSupplier());
        QneCall<QneSupplierResponse> call = gateway.createSupplier(request);
        if (!call.success()) {
            return QnePushResult.rejected(call.message());
        }

        suppliers.claimQneIdentity(supplier.getId(), call.data().getId(), call.data().getCompanyCode());
        return QnePushResult.ok(call.data().getId(), call.data().getCompanyCode(), null,
                "Supplier pushed to QNE as " + call.data().getCompanyCode());
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

    private String currencyName(Supplier supplier) {
        if (supplier.getSymbolRefid() == null) {
            return "";
        }
        return symbols.findById(supplier.getSymbolRefid())
                .map(SymbolMaster::getSName)
                .orElse("");
    }

    /**
     * Field mapping pinned by legacy {@code SupplierServices.InsertSupplier}:
     * same shape as the customer push (City as contact person, OEmail/OPhone
     * as the contact points) with the four Is* flags hardcoded false.
     */
    static QneSupplierRequest buildRequest(Supplier supplier, String currency, String controlAccount) {
        String[] address = QnePayloads.addressChunks(supplier.getAddress1());
        return QneSupplierRequest.builder()
                .companyName(supplier.getSupplierName())
                .companyName2(supplier.getSupplierName())
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
                .contactPerson(supplier.getCity())
                .email(supplier.getOEmail())
                .phoneNo1(supplier.getOPhone())
                .build();
    }
}
