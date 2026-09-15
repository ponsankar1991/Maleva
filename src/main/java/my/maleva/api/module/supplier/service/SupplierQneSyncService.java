package my.maleva.api.module.supplier.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.common.config.QneProperties;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.integration.qne.QneCall;
import my.maleva.api.integration.qne.QneGateway;
import my.maleva.api.integration.qne.QnePushLock;
import my.maleva.api.integration.qne.dto.QneSupplierResponse;
import my.maleva.api.module.supplier.dto.SupplierDto;
import my.maleva.api.module.supplier.dto.SupplierQneSyncResult;
import my.maleva.api.module.supplier.repository.SupplierQneSyncRepository;
import my.maleva.api.module.supplier.repository.SupplierQneSyncRepository.IdRepair;
import my.maleva.api.module.supplier.repository.SupplierQneSyncRepository.LinkedSupplier;
import my.maleva.api.module.supplier.repository.SupplierQneSyncRepository.NamedId;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * "Update from QNE" — the port of legacy {@code SupplierServices.UpdateSupplierId}.
 *
 * <p>What legacy did, and still happens here: read QNE's supplier list; for a
 * QNE supplier whose company code exists locally, store its QNE id; for one
 * that does not, create the supplier locally the way SP_Supplier would
 * (VENDOR, currency and term matched by name) and link it to its QNE code.
 *
 * <p>What legacy got wrong, and does not happen here:
 * <ul>
 *   <li><b>Only the first 1,000.</b> It asked for {@code ?top=1000} once. Every
 *       page is read now, ordered by company code.</li>
 *   <li><b>Deleted suppliers came back.</b> It matched against suppliers with
 *       {@code Active != 2} only, so each sync re-created every supplier
 *       deleted here. Deleted suppliers count as known.</li>
 *   <li><b>Case-sensitive matching.</b> {@code List.Contains} compared codes
 *       exactly, while SQL Server treats them case-insensitively; a code stored
 *       as {@code 400-p001} was created again as {@code 400-P001}.</li>
 *   <li><b>Blank codes.</b> A QNE supplier with no company code was created and
 *       then "linked" to an empty code — again on every sync. It is skipped and
 *       counted.</li>
 *   <li><b>One failure hid the rest.</b> A failed insert was swallowed without
 *       a word. Each supplier is created in its own transaction; failures are
 *       collected and reported.</li>
 *   <li><b>Two clicks, two syncs.</b> Nothing stopped a second run creating the
 *       same suppliers twice. A second sync for the company is refused while
 *       one is running.</li>
 * </ul>
 *
 * <p>Runs outside any transaction: reading QNE can take minutes, and holding a
 * database transaction open across it would block every supplier save.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierQneSyncService {

    /** Legacy's {@code top=1000}, now the page size. */
    static final int PAGE_SIZE = 1000;

    /** A safety stop, far above any real supplier list (200,000 suppliers). */
    static final int MAX_PAGES = 200;

    /** SP_Supplier's OPENJSON column widths — longer text was cut there, and is cut the same way here. */
    private static final int NAME_WIDTH = 500;
    private static final int ADDRESS_WIDTH = 300;
    private static final int CITY_WIDTH = 100;
    private static final int PHONE_WIDTH = 50;
    private static final int EMAIL_WIDTH = 100;

    private final QneGateway gateway;
    private final QneProperties properties;
    private final QnePushLock locks;
    private final SupplierQneSyncRepository repository;
    private final SupplierService suppliers;

    /** Package-private so tests can page through a handful of suppliers. */
    int pageSize = PAGE_SIZE;

    public SupplierQneSyncResult syncFromQne(Integer companyId) {
        if (companyId == null || companyId <= 0) {
            throw new InvalidRequestException("Company is required to update suppliers from QNE.");
        }
        if (!properties.isEnabled()) {
            return SupplierQneSyncResult.disabled();
        }

        String lockKey = "supplier-qne-sync:" + companyId;
        if (!locks.tryAcquire(lockKey)) {
            throw new InvalidRequestException(
                    "An update from QNE is already running for this company. Wait for it to finish.");
        }
        try {
            return sync(companyId);
        } finally {
            locks.release(lockKey);
        }
    }

    private SupplierQneSyncResult sync(int companyId) {
        List<QneSupplierResponse> fromQne = new ArrayList<>();
        String previousFirstCode = null;
        for (int page = 0; page < MAX_PAGES; page++) {
            QneCall<List<QneSupplierResponse>> call = gateway.listSuppliers(page * pageSize, pageSize);
            if (!call.success()) {
                // Legacy: ro.Message = ro1.Message. Nothing local has been touched yet.
                log.warn("UpdateSupplierId: QNE refused the supplier list for company {}: {}", companyId, call.message());
                return SupplierQneSyncResult.failed(call.message());
            }
            List<QneSupplierResponse> rows = call.data() == null ? List.of() : call.data();
            if (rows.isEmpty()) {
                break;
            }
            String firstCode = rows.get(0).getCompanyCode();
            if (page > 0 && Objects.equals(firstCode, previousFirstCode)) {
                // A QNE that ignores skip would hand back page one for ever.
                log.warn("UpdateSupplierId: QNE returned the same page again at skip {}; stopping", page * pageSize);
                break;
            }
            previousFirstCode = firstCode;
            fromQne.addAll(rows);
            if (rows.size() < pageSize) {
                break;
            }
        }

        Map<String, LinkedSupplier> known = new HashMap<>();
        for (LinkedSupplier link : repository.linkedSuppliers(companyId)) {
            known.putIfAbsent(codeKey(link.qneCode()), link);
        }
        Map<String, Integer> symbolIds = firstIdByName(repository.symbols(companyId));
        Map<String, Integer> termIds = firstIdByName(repository.paymentTerms(companyId));

        List<IdRepair> repairs = new ArrayList<>();
        List<String> failures = new ArrayList<>();
        int alreadyLinked = 0;
        int created = 0;
        int skippedWithoutCode = 0;

        for (QneSupplierResponse qne : fromQne) {
            String code = trimToNull(qne.getCompanyCode());
            if (code == null) {
                skippedWithoutCode++;
                continue;
            }

            LinkedSupplier local = known.get(codeKey(code));
            if (local != null) {
                alreadyLinked++;
                if (qne.getId() != null && !qne.getId().equals(local.qneId())) {
                    repairs.add(new IdRepair(local.qneCode(), qne.getId()));
                    known.put(codeKey(code), new LinkedSupplier(local.id(), local.qneCode(), qne.getId()));
                }
                continue;
            }

            try {
                int id = suppliers.createFromQne(toLocalSupplier(qne, companyId, symbolIds, termIds), qne.getId(), code);
                // Known from now on, so a duplicate entry in QNE's list is not created twice.
                known.put(codeKey(code), new LinkedSupplier(id, code, qne.getId()));
                created++;
            } catch (RuntimeException ex) {
                log.warn("UpdateSupplierId: could not create QNE supplier {} for company {}", code, companyId, ex);
                failures.add(code + " " + orEmpty(qne.getCompanyName()) + ": " + reason(ex));
            }
        }

        int idsRepaired = repository.repairQneIds(companyId, repairs);

        StringJoiner summary = new StringJoiner(", ");
        summary.add(fromQne.size() + " in QNE").add(created + " created").add(idsRepaired + " QNE ids repaired");
        if (!failures.isEmpty()) {
            summary.add(failures.size() + " failed");
        }
        if (skippedWithoutCode > 0) {
            summary.add(skippedWithoutCode + " without a QNE code skipped");
        }
        return new SupplierQneSyncResult(SupplierQneSyncResult.Status.COMPLETED, summary.toString(),
                fromQne.size(), alreadyLinked, idsRepaired, created, skippedWithoutCode, List.copyOf(failures));
    }

    /**
     * The local supplier legacy built from a QNE supplier, field for field:
     * CompanyName → SupplierName; Address1..4 → Address1; ContactPerson → City;
     * PhoneNo1 → MobileNo, OPhone and APhone; Email → Email, OEmail and AEmail;
     * SupplierType VENDOR; the symbol whose SName equals Currency and the term
     * whose TermsName equals Term, else 0. Text the form would send as "" is "",
     * and the Int32 fields SupplierModel defaulted are 0.
     *
     * <p>Two corrections: the address lines are joined without the empty lines
     * legacy padded in ({@code "LINE 1\n\n\n"}), and text longer than its column
     * is cut to the column's width, as SP_Supplier's OPENJSON did — a plain
     * insert would otherwise fail the whole supplier.
     */
    static SupplierDto toLocalSupplier(QneSupplierResponse qne, int companyId,
                                       Map<String, Integer> symbolIds, Map<String, Integer> termIds) {
        StringJoiner address = new StringJoiner("\n");
        for (String line : new String[]{qne.getAddress1(), qne.getAddress2(), qne.getAddress3(), qne.getAddress4()}) {
            if (line != null && !line.isBlank()) {
                address.add(line);
            }
        }
        String phone = cut(orEmpty(qne.getPhoneNo1()), PHONE_WIDTH);
        String email = cut(orEmpty(qne.getEmail()), EMAIL_WIDTH);

        SupplierDto dto = new SupplierDto();
        dto.setCompanyRefId(companyId);
        dto.setSupplierName(cut(orEmpty(qne.getCompanyName()), NAME_WIDTH));
        dto.setSupplierType("VENDOR");
        dto.setAddress1(cut(address.toString(), ADDRESS_WIDTH));
        dto.setAddress2("");
        dto.setAddress3("");
        dto.setPersonId("");
        dto.setCity(cut(orEmpty(qne.getContactPerson()), CITY_WIDTH));
        dto.setState("");
        dto.setZipcode("");
        dto.setCountry("");
        dto.setSymbolRefid(idByName(symbolIds, qne.getCurrency()));
        dto.setPaymentTermsRefid(idByName(termIds, qne.getTerm()));
        dto.setGstNo("");
        dto.setEmail(email);
        dto.setOEmail(email);
        dto.setAEmail(email);
        dto.setOEmail1("");
        dto.setAEmail1("");
        dto.setMobileNo(phone);
        dto.setOPhone(phone);
        dto.setAPhone(phone);
        dto.setOName("");
        dto.setAName("");
        dto.setUserName("");
        dto.setPassword("");
        dto.setTinNo("");
        dto.setSstNo("");
        dto.setMsicCode("");
        dto.setServiceTaxType("");
        dto.setBankName("");
        dto.setAccountNo("");
        dto.setTinType("");
        dto.setSupplierTin("");
        dto.setTaxExemptionNo("");
        dto.setTaxExemptionDetails("");
        dto.setRegistrationNo("");
        dto.setSupplierCity("");
        dto.setSelfBilled(0);
        dto.setMsicCodeRefId(0);
        dto.setActive(1);
        return dto;
    }

    /** LINQ {@code .Where(c => c.SName == value).Select(c => c.Id).FirstOrDefault()}: exact name, first row wins. */
    private static Map<String, Integer> firstIdByName(List<NamedId> rows) {
        Map<String, Integer> byName = new LinkedHashMap<>();
        for (NamedId row : rows) {
            if (row.name() != null) {
                byName.putIfAbsent(row.name(), row.id());
            }
        }
        return byName;
    }

    /**
     * The id for a QNE currency or term name, 0 when there is none. A QNE
     * supplier without a currency or term is ordinary, and {@code Map.get(null)}
     * throws on immutable maps — so a missing name is answered before any lookup.
     */
    private static int idByName(Map<String, Integer> idsByName, String name) {
        if (name == null) {
            return 0;
        }
        Integer id = idsByName.get(name);
        return id == null ? 0 : id;
    }

    private static String codeKey(String code) {
        return code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String cut(String value, int width) {
        return value.length() <= width ? value : value.substring(0, width);
    }

    private static String reason(RuntimeException ex) {
        return ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
    }
}
