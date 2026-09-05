package my.maleva.api.module.invoice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.invoice.dto.SaleInvoiceDetailRequestDTO;
import my.maleva.api.module.invoice.dto.SaleInvoiceRequestDTO;
import my.maleva.api.module.invoice.dto.SaleInvoiceSaveResult;
import my.maleva.api.module.master.entity.SequenceNoMaster;
import my.maleva.api.module.master.repository.SequenceNoMasterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Saves a sale invoice through {@code SP_SaleMaster}.
 *
 * <p><b>Why the procedure is still called.</b> One save writes SaleMaster,
 * deletes and re-inserts SaleDetails, deletes and re-inserts
 * SaleMasterReference, clears and re-stamps {@code SaleOrderMaster.InvoiceNo}
 * for every job it touches, and allocates the invoice number from
 * SequenceNoMaster - all in one transaction. That is the procedure's contract
 * and it is shared with the .NET screens still in production, so both callers
 * must go through it or the two will allocate the same number and disagree on
 * which jobs are invoiced. Re-implementing it in JPA is a separate migration,
 * not a side effect of this endpoint.
 *
 * <p><b>What is different from the legacy caller.</b>
 * <ul>
 *   <li>The payload is bound as a parameter. Legacy pasted the JSON into
 *       {@code "Exec [SP_SaleMaster] '" + details + "'," + Comid} after running
 *       {@code Replace("'", "")} over it, so every apostrophe in a remark or an
 *       address was deleted before it reached the database - and anything the
 *       replace missed could rewrite the statement.</li>
 *   <li>Jackson writes the JSON. Legacy also ran
 *       {@code Replace("null", "\"\"")} over the serialized string, which
 *       replaced the four characters {@code null} <i>anywhere</i> they appeared,
 *       including inside legitimate text.</li>
 *   <li>Foreign keys are validated here, before the call. The procedure's own
 *       checks build their message with {@code 'text' + @intVariable}, which
 *       raises a conversion error instead of returning the message - so the
 *       operator saw a type error rather than "Employee Not Found". Validating
 *       first also keeps the procedure off its {@code ROLLBACK TRAN} path, which
 *       would otherwise unwind the transaction Spring opened and surface as a
 *       transaction-count mismatch on commit.</li>
 *   <li>The SequenceNoMaster row is created if it is missing. The procedure's
 *       bootstrap branch runs {@code UPDATE SequenceNoMaster ...} when no row
 *       exists yet, which matches nothing, so the sequence never advanced and
 *       every invoice for that company was numbered 1.</li>
 *   <li>Number allocation is serialised per company with an application lock.
 *       The procedure reads {@code MAX(SequenceNo)} and updates it as two
 *       statements with nothing in between; its own lock call is commented
 *       out.</li>
 * </ul>
 *
 * <p><b>Legacy behaviour deliberately preserved.</b> {@code SaleType} is forced
 * to {@code 'CREDIT'} by the procedure whatever is sent; {@code BillType} and
 * {@code DOCNo} are written on insert only; {@code CNumber} and
 * {@code CNumberDisplay} are never re-written on an edit; and an edit updates
 * {@code LastEmployeeRefId} while leaving the original {@code EmployeeRefId}
 * alone. None of that is changed here.
 */
@Service
public class SaleInvoiceTransactionService {

    private static final Logger logger = LoggerFactory.getLogger(SaleInvoiceTransactionService.class);

    /** SequenceNoMaster.SequenceName for invoice numbers. */
    private static final String SEQUENCE_NAME = "SaleMaster";

    private static final String NUMBER_PREFIX = "INV";
    private static final int NUMBER_DIGITS = 9;

    /** Rows the procedure treats as live. */
    private static final int ACTIVE = 1;

    private final NamedParameterJdbcTemplate jdbc;
    private final SequenceNoMasterRepository sequences;
    private final ObjectMapper objectMapper;

    public SaleInvoiceTransactionService(NamedParameterJdbcTemplate jdbc,
                                         SequenceNoMasterRepository sequences,
                                         ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.sequences = sequences;
        this.objectMapper = objectMapper;
    }

    // ─────────────────────────────────────────────────────────────── save ──

    @Transactional(rollbackFor = Exception.class)
    public SaleInvoiceSaveResult save(SaleInvoiceRequestDTO request) {
        Integer companyId = request.getCompanyRefId();
        if (companyId == null || companyId <= 0) {
            throw new InvalidRequestException("companyRefId is required");
        }
        if (request.getCustomerRefId() == null || request.getCustomerRefId() <= 0) {
            throw new InvalidRequestException("Select a customer before saving");
        }
        if (request.getJobMasterRefId() == null || request.getJobMasterRefId() <= 0) {
            throw new InvalidRequestException("Select a job type before saving");
        }
        if (request.getSaleDate() == null) {
            throw new InvalidRequestException("Invoice date is required");
        }

        List<SaleInvoiceDetailRequestDTO> lines = request.getDetails() == null
                ? List.of()
                : request.getDetails().stream().filter(Objects::nonNull).toList();
        if (lines.isEmpty()) {
            // The procedure deletes the existing SaleDetails rows before
            // inserting these, so an empty list on an edit empties the invoice.
            throw new InvalidRequestException("An invoice needs at least one line");
        }
        for (SaleInvoiceDetailRequestDTO line : lines) {
            if (line.getItemMasterRefId() == null || line.getItemMasterRefId() <= 0) {
                throw new InvalidRequestException("Every line needs a product");
            }
        }

        boolean creating = request.getId() == null || request.getId() == 0;

        requireLookupsExist(request, companyId);
        ensureSequenceRowExists(companyId);
        lockNumberAllocation(companyId);

        String payload = writePayload(request, lines);

        Map<String, Object> row = jdbc.queryForMap(
                "EXEC [SP_SaleMaster] :master, :comid",
                new MapSqlParameterSource()
                        .addValue("master", payload)
                        .addValue("comid", companyId));

        Integer resultCode = asInteger(row.get("Result"));
        if (resultCode == null || resultCode != 1) {
            String message = trimToNull(String.valueOf(row.getOrDefault("msg", "")));
            logger.warn("SP_SaleMaster rejected the invoice for company {}: {}", companyId, message);
            throw new InvalidRequestException(message == null ? "The invoice was not saved" : message);
        }

        Integer savedId = asInteger(row.get("id"));
        if (savedId == null || savedId == 0) {
            savedId = request.getId();
        }

        // The procedure only fills @SaleNoDisplay on its insert branch, so an
        // edit hands back a null BillNo. Read what was actually stored instead.
        String billNo = trimToNull(String.valueOf(row.getOrDefault("BillNo", "")));
        Integer billNumber = null;
        if (billNo == null && savedId != null) {
            Map<String, Object> stored = jdbc.queryForMap(
                    "SELECT CNumber, CNumberDisplay FROM SaleMaster WITH (NOLOCK) WHERE Id = :id",
                    new MapSqlParameterSource("id", savedId));
            billNo = trimToNull(String.valueOf(stored.getOrDefault("CNumberDisplay", "")));
            billNumber = asInteger(stored.get("CNumber"));
        }

        logger.info("Invoice {} {} for company {}", billNo, creating ? "created" : "updated", companyId);

        return SaleInvoiceSaveResult.builder()
                .id(savedId)
                .billNo(billNo)
                .billNumber(billNumber)
                .saleTime(asDateTime(row.get("SaleTime")))
                .created(creating)
                .build();
    }

    // ───────────────────────────────────────────────────────── validation ──

    /**
     * Repeats the procedure's own existence checks, in the same tables and with
     * the same company and Active scoping - {@code AgentMasterRefId} really is
     * checked against {@code Agent} and not {@code AgentMaster}.
     *
     * <p>The procedure skips a check when the id is 0, so 0 and null both mean
     * "not supplied" here too.
     */
    private void requireLookupsExist(SaleInvoiceRequestDTO r, Integer companyId) {
        requireExists("Login user", "AppUser", r.getUserRefId(), companyId);
        requireExists("Employee", "EmployeeMaster", r.getEmployeeRefId(), companyId);
        requireExists("Agent company", "AgentCompanyMaster", r.getAgentCompanyRefId(), companyId);
        requireExists("Agent", "Agent", r.getAgentMasterRefId(), companyId);
        requireExists("Off agent company", "AgentCompanyMaster", r.getOAgentCompanyRefId(), companyId);
        requireExists("Off agent", "Agent", r.getOAgentMasterRefId(), companyId);
        requireExists("Truck", "TruckMaster", r.getTruckRefId(), companyId);
        requireExists("Driver", "DriverMaster", r.getDriverRefId(), companyId);
        requireExists("Forklift operator", "EmployeeMaster", r.getForkliftByRefId(), companyId);
        requireExists("Seal by", "EmployeeMaster", r.getSealByRefId(), companyId);
        requireExists("Break seal by", "EmployeeMaster", r.getSealBreakByRefId(), companyId);
        requireExists("Seal by 2", "EmployeeMaster", r.getSealByRefId2(), companyId);
        requireExists("Break seal by 2", "EmployeeMaster", r.getSealBreakByRefId2(), companyId);
        requireExists("Seal by 3", "EmployeeMaster", r.getSealByRefId3(), companyId);
        requireExists("Break seal by 3", "EmployeeMaster", r.getSealBreakByRefId3(), companyId);
        requireExists("Boarding officer", "EmployeeMaster", r.getBoardingOfficerRefId(), companyId);
        requireExists("Boarding officer 2", "EmployeeMaster", r.getBoardingOfficer1RefId(), companyId);
    }

    private void requireExists(String label, String table, Integer id, Integer companyId) {
        if (id == null || id == 0) {
            return;
        }
        Integer found = jdbc.queryForObject(
                "SELECT COUNT(*) FROM [" + table + "] WITH (NOLOCK) "
                        + "WHERE Id = :id AND CompanyRefId = :comid AND Active = :active",
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("comid", companyId)
                        .addValue("active", ACTIVE),
                Integer.class);
        if (found == null || found == 0) {
            throw new InvalidRequestException(label + " " + id + " was not found for this company");
        }
    }

    // ─────────────────────────────────────────────────────────── numbering ──

    /**
     * Makes sure the company has a SequenceNoMaster row before the procedure
     * looks for one, seeded from the highest CNumber already issued so an
     * existing company does not restart at 1.
     */
    private void ensureSequenceRowExists(Integer companyId) {
        if (sequences.findByCompanyRefIdAndSequenceName(companyId, SEQUENCE_NAME).isPresent()) {
            return;
        }
        Integer highestIssued = jdbc.queryForObject(
                "SELECT ISNULL(MAX(CNumber), 0) FROM SaleMaster WITH (NOLOCK) WHERE CompanyRefId = :comid",
                new MapSqlParameterSource("comid", companyId),
                Integer.class);

        SequenceNoMaster seed = new SequenceNoMaster();
        seed.setCompanyRefId(companyId);
        seed.setSequenceName(SEQUENCE_NAME);
        seed.setSequenceNo(highestIssued == null ? 0 : highestIssued);
        seed.setSequenceDate(LocalDateTime.now());
        sequences.save(seed);

        logger.info("Created the {} sequence for company {} at {}", SEQUENCE_NAME, companyId, seed.getSequenceNo());
    }

    /**
     * Serialises invoice numbering for one company until this transaction ends.
     * The procedure reads the sequence and updates it as separate statements,
     * so two concurrent saves would otherwise take the same number.
     */
    private void lockNumberAllocation(Integer companyId) {
        try {
            Integer status = jdbc.queryForObject(
                    "DECLARE @status int; "
                            + "EXEC @status = sp_getapplock @Resource = :key, "
                            + "@LockMode = 'Exclusive', @LockOwner = 'Transaction', "
                            + "@LockTimeout = 15000; SELECT @status",
                    new MapSqlParameterSource("key", "SaleMasterNumber:" + companyId),
                    Integer.class);
            if (status != null && status < 0) {
                logger.warn("Invoice-number lock for company {} timed out (status {}); saving unguarded",
                        companyId, status);
            }
        } catch (Exception ex) {
            // Never let the guard itself block a save.
            logger.warn("Could not take the invoice-number lock ({}); saving unguarded", ex.getMessage());
        }
    }

    // ───────────────────────────────────────────────────────────── payload ──

    /**
     * Builds the JSON array the procedure reads with OPENJSON.
     *
     * <p>The null conventions are the procedure's, not ours:
     * <ul>
     *   <li>text fields are written as {@code ""} rather than JSON null, because
     *       the invoice list filters on {@code Remarks = ''} and
     *       {@code Remarks <> ''} and a NULL satisfies neither;</li>
     *   <li>reference ids are written as {@code 0}. The procedure only validates
     *       an id when it is {@code <> 0}, and its {@code IF @x = ''} block then
     *       turns the 0 into a NULL before the insert - in T-SQL {@code 0 = ''}
     *       is true, because the empty string converts to int 0;</li>
     *   <li>dates are written as JSON null, which OPENJSON reads as NULL. Sending
     *       {@code ""} instead would first convert to 1900-01-01 and rely on the
     *       same guard to undo it.</li>
     * </ul>
     */
    private String writePayload(SaleInvoiceRequestDTO r, List<SaleInvoiceDetailRequestDTO> lines) {
        ObjectNode master = objectMapper.createObjectNode();

        // The procedure's ROW_NUMBER() OVER(ORDER BY SNo) reads this.
        master.put("boundindex", 1);

        master.put("Id", zeroIfNull(r.getId()));
        master.put("CompanyRefId", r.getCompanyRefId());
        master.put("CustomerRefId", r.getCustomerRefId());
        master.put("JobMasterRefId", r.getJobMasterRefId());
        master.put("EmployeeRefId", zeroIfNull(r.getEmployeeRefId()));
        master.put("UserRefId", zeroIfNull(r.getUserRefId()));
        master.put("AgentCompanyRefId", zeroIfNull(r.getAgentCompanyRefId()));
        master.put("AgentMasterRefId", zeroIfNull(r.getAgentMasterRefId()));
        master.put("OAgentCompanyRefId", zeroIfNull(r.getOAgentCompanyRefId()));
        master.put("OAgentMasterRefId", zeroIfNull(r.getOAgentMasterRefId()));

        putDate(master, "SaleDate", r.getSaleDate());
        master.put("BillType", blank(r.getBillType()));
        // Written for completeness only; the procedure hard-codes 'CREDIT'.
        master.put("SaleType", "CREDIT");

        master.put("GrossAmount", zeroIfNull(r.getGrossAmount()));
        master.put("TaxAmount", zeroIfNull(r.getTaxAmount()));
        master.put("DiscountAmount", zeroIfNull(r.getDiscountAmount()));
        master.put("PlusAmount", zeroIfNull(r.getPlusAmount()));
        master.put("MinusAmount", zeroIfNull(r.getMinusAmount()));
        master.put("Coinage", zeroIfNull(r.getCoinage()));
        master.put("Amount", zeroIfNull(r.getAmount()));
        master.put("CurrencyValue", zeroIfNull(r.getCurrencyValue()));
        master.put("ActualNetAmount", zeroIfNull(r.getActualNetAmount()));
        master.put("SymbolRefId", zeroIfNull(r.getSymbolRefId()));

        master.put("Remarks", blank(r.getRemarks()));
        master.put("Remarks1", blank(r.getRemarks1()));
        master.put("DODescription", blank(r.getDoDescription()));
        master.put("Offvesselname", blank(r.getOffVesselName()));
        master.put("Loadingvesselname", blank(r.getLoadingVesselName()));
        master.put("TruckSize", blank(r.getTruckSize()));
        master.put("SPort", blank(r.getSPort()));
        master.put("OPort", blank(r.getOPort()));
        master.put("SCN", blank(r.getScn()));
        master.put("LSCN", blank(r.getLscn()));
        master.put("Vessel", blank(r.getVessel()));
        master.put("OVessel", blank(r.getOVessel()));
        master.put("Commodity", blank(r.getCommodity()));
        master.put("Cargo", blank(r.getCargo()));
        master.put("AWBNo", blank(r.getAwbNo()));
        master.put("BLCopy", blank(r.getBlCopy()));
        master.put("Quantity", blank(r.getQuantity()));
        master.put("TotalWeight", blank(r.getTotalWeight()));
        master.put("PTW", blank(r.getPtw()));
        master.put("Origin", blank(r.getOrigin()));
        master.put("Destination", blank(r.getDestination()));

        putDateTime(master, "ETA", r.getEta());
        putDateTime(master, "ETB", r.getEtb());
        putDateTime(master, "ETD", r.getEtd());
        putDateTime(master, "OETA", r.getOEta());
        putDateTime(master, "OETB", r.getOEtb());
        putDateTime(master, "OETD", r.getOEtd());
        putDateTime(master, "PickupDate", r.getPickupDate());
        putDateTime(master, "DeliveryDate", r.getDeliveryDate());
        putDateTime(master, "WareHouseEnterDate", r.getWareHouseEnterDate());
        putDateTime(master, "WareHouseExitDate", r.getWareHouseExitDate());

        master.put("PickupAddress", blank(r.getPickupAddress()));
        master.put("DeliveryAddress", blank(r.getDeliveryAddress()));
        master.put("WareHouseAddress", blank(r.getWareHouseAddress()));

        master.put("DOCNo", zeroIfNull(r.getDocNo()));
        master.put("SaleOrderMasterNo", zeroIfNull(r.getSaleOrderMasterNo()));
        master.put("TruckRefid", zeroIfNull(r.getTruckRefId()));
        master.put("DriverRefid", zeroIfNull(r.getDriverRefId()));
        master.put("JStatus", zeroIfNull(r.getJStatus()));
        master.put("OStatus", zeroIfNull(r.getOStatus()));

        master.put("ForkliftbyRefid", zeroIfNull(r.getForkliftByRefId()));
        master.put("SealbyRefid", zeroIfNull(r.getSealByRefId()));
        master.put("SealbreakbyRefid", zeroIfNull(r.getSealBreakByRefId()));
        master.put("SealbyRefid2", zeroIfNull(r.getSealByRefId2()));
        master.put("SealbreakbyRefid2", zeroIfNull(r.getSealBreakByRefId2()));
        master.put("SealbyRefid3", zeroIfNull(r.getSealByRefId3()));
        master.put("SealbreakbyRefid3", zeroIfNull(r.getSealBreakByRefId3()));
        master.put("BoardingOfficerRefid", zeroIfNull(r.getBoardingOfficerRefId()));
        master.put("BoardingOfficer1Refid", zeroIfNull(r.getBoardingOfficer1RefId()));
        master.put("BoardingAmount", zeroIfNull(r.getBoardingAmount()));
        master.put("BoardingAmount1", zeroIfNull(r.getBoardingAmount1()));

        master.put("Forwarding", blank(r.getForwarding()));
        master.put("Forwarding2", blank(r.getForwarding2()));
        master.put("Forwarding3", blank(r.getForwarding3()));
        master.put("ForwardingEnterRef", blank(r.getForwardingEnterRef()));
        master.put("ForwardingExitRef", blank(r.getForwardingExitRef()));
        master.put("ForwardingEnterRef2", blank(r.getForwardingEnterRef2()));
        master.put("ForwardingExitRef2", blank(r.getForwardingExitRef2()));
        master.put("ForwardingEnterRef3", blank(r.getForwardingEnterRef3()));
        master.put("ForwardingExitRef3", blank(r.getForwardingExitRef3()));
        master.put("ForwardingSMKNo", blank(r.getForwardingSmkNo()));
        master.put("ForwardingSMKNo2", blank(r.getForwardingSmkNo2()));
        master.put("ForwardingSMKNo3", blank(r.getForwardingSmkNo3()));

        master.put("PortChargesRef", blank(r.getPortChargesRef()));
        master.put("PortCharges", zeroIfNull(r.getPortCharges()));
        master.put("SealAmount", zeroIfNull(r.getSealAmount()));
        master.put("BreakSealAmount", zeroIfNull(r.getBreakSealAmount()));
        master.put("SealAmount2", zeroIfNull(r.getSealAmount2()));
        master.put("BreakSealAmount2", zeroIfNull(r.getBreakSealAmount2()));
        master.put("SealAmount3", zeroIfNull(r.getSealAmount3()));
        master.put("BreakSealAmount3", zeroIfNull(r.getBreakSealAmount3()));

        master.put("Zb", blank(r.getZb()));
        master.put("Zb2", blank(r.getZb2()));
        master.put("ZbRef", blank(r.getZbRef()));
        master.put("ZbRef2", blank(r.getZbRef2()));

        // Allocated by the procedure from SequenceNoMaster on insert and left
        // alone on edit; sent only so OPENJSON finds the keys it declares.
        master.put("CNumber", 0);
        master.put("CNumberDisplay", "");

        master.set("SaleInvoiceDetails", writeLines(lines));
        master.set("SaleOrderRefId", writeReferences(r, lines));

        ArrayNode payload = objectMapper.createArrayNode();
        payload.add(master);
        return payload.toString();
    }

    private ArrayNode writeLines(List<SaleInvoiceDetailRequestDTO> lines) {
        ArrayNode array = objectMapper.createArrayNode();
        for (SaleInvoiceDetailRequestDTO line : lines) {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("ItemMasterRefId", line.getItemMasterRefId());
            node.put("MRP", zeroIfNull(line.getMrp()));
            node.put("PurchaseRate", zeroIfNull(line.getPurchaseRate()));
            node.put("ItemQty", zeroIfNull(line.getItemQty()));
            node.put("DiscPer", zeroIfNull(line.getDiscountPercent()));
            node.put("DiscAmount", zeroIfNull(line.getDiscountAmount()));
            node.put("LandingCost", zeroIfNull(line.getLandingCost()));
            node.put("TaxPercent", zeroIfNull(line.getTaxPercent()));
            node.put("TaxAmount", zeroIfNull(line.getTaxAmount()));
            node.put("SalesRate", zeroIfNull(line.getSalesRate()));
            node.put("NetSalesRate", zeroIfNull(line.getNetSalesRate()));
            node.put("Amount", zeroIfNull(line.getAmount()));
            node.put("CurrencyValue", zeroIfNull(line.getCurrencyValue()));
            node.put("TaxRefId", zeroIfNull(line.getTaxRefId()));
            node.put("ActualAmount", zeroIfNull(line.getActualAmount()));
            node.put("SDRemarks", blank(line.getRemarks()));
            node.put("SaleOrderMasterRefId", zeroIfNull(line.getSaleOrderMasterRefId()));
            array.add(node);
        }
        return array;
    }

    /**
     * The distinct sale orders this invoice covers. Taken from the request when
     * it names them, otherwise derived from the lines - the screen builds the
     * list that way and the two must not disagree, because the procedure stamps
     * {@code SaleOrderMaster.InvoiceNo} from this list alone.
     */
    private ArrayNode writeReferences(SaleInvoiceRequestDTO r, List<SaleInvoiceDetailRequestDTO> lines) {
        List<Integer> ids = new ArrayList<>();
        if (r.getSaleOrderRefIds() != null) {
            ids.addAll(r.getSaleOrderRefIds());
        }
        if (ids.isEmpty()) {
            lines.stream()
                    .map(SaleInvoiceDetailRequestDTO::getSaleOrderMasterRefId)
                    .filter(Objects::nonNull)
                    .forEach(ids::add);
        }

        ArrayNode array = objectMapper.createArrayNode();
        for (Integer id : new LinkedHashSet<>(ids)) {
            if (id == null || id == 0) {
                continue;
            }
            ObjectNode node = objectMapper.createObjectNode();
            node.put("SaleOrderMasterRefId", id);
            array.add(node);
        }
        return array;
    }

    // ───────────────────────────────────────────────────────────── helpers ──

    private void putDate(ObjectNode node, String field, LocalDate value) {
        if (value == null) {
            node.putNull(field);
        } else {
            node.put(field, value.toString());
        }
    }

    private void putDateTime(ObjectNode node, String field, LocalDateTime value) {
        if (value == null) {
            node.putNull(field);
        } else {
            node.put(field, value.toString());
        }
    }

    private static String blank(String value) {
        return value == null ? "" : value;
    }

    private static int zeroIfNull(Integer value) {
        return value == null ? 0 : value;
    }

    private static double zeroIfNull(Double value) {
        return value == null ? 0d : value;
    }

    private static Integer asInteger(Object value) {
        return value instanceof Number number ? number.intValue() : null;
    }

    private static LocalDateTime asDateTime(Object value) {
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        return value instanceof LocalDateTime dateTime ? dateTime : null;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() || "null".equals(trimmed) ? null : trimmed;
    }

    /** The invoice number a fresh save would take, without allocating it. */
    @Transactional(readOnly = true)
    public String peekNextNumber(Integer companyId) {
        Integer next = jdbc.queryForObject(
                "SELECT ISNULL(MAX(SequenceNo) + 1, 1) FROM SequenceNoMaster WITH (NOLOCK) "
                        + "WHERE CompanyRefId = :comid AND SequenceName = :name",
                new MapSqlParameterSource()
                        .addValue("comid", companyId)
                        .addValue("name", SEQUENCE_NAME),
                Integer.class);
        return NUMBER_PREFIX + String.format("%0" + NUMBER_DIGITS + "d", next == null ? 1 : next);
    }
}
