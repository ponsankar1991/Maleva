package my.maleva.api.module.invoice.service;

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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Saves a sale invoice.
 *
 * <p>The rows are written by {@link SaleInvoiceWriter}, in Java. This service
 * owns everything around that write: the foreign-key checks, the invoice
 * number, the lock that serialises numbering, and the transaction it all runs
 * in.
 *
 * <p><b>Why not SP_SaleMaster.</b> The .NET screens still call the procedure
 * and can carry on doing so; this application does not. The procedure
 * serialises the invoice to JSON, shreds it back out with OPENJSON into a temp
 * table, loops over that table a row at a time and re-declares a hundred
 * locals to perform one insert. Doing the same writes directly is faster,
 * debuggable and testable. The two stay compatible — same tables, same
 * columns, same numbering — so an invoice written here is indistinguishable
 * from one the procedure wrote; every quirk worth keeping is documented on
 * {@link SaleInvoiceWriter}.
 *
 * <p><b>What this does better than the procedure.</b>
 * <ul>
 *   <li>Foreign keys are checked before anything is written. The procedure's
 *       own checks build their message with {@code 'text' + @intVariable},
 *       which raises a conversion error instead of returning the message, so
 *       the operator saw a type error rather than "Employee Not Found".</li>
 *   <li>The SequenceNoMaster row is created when missing. The procedure's
 *       bootstrap branch runs {@code UPDATE SequenceNoMaster ...} when no row
 *       exists, which matches nothing, so the sequence never advanced and
 *       every invoice for that company came out numbered 1.</li>
 *   <li>Number allocation is serialised per company with an application lock.
 *       The procedure reads {@code MAX(SequenceNo)} and updates it as two
 *       statements with nothing in between; its own lock call is commented
 *       out.</li>
 *   <li>No JSON round trip, so nothing has to survive the legacy caller's
 *       {@code Replace("'", "")} over the payload, which deleted every
 *       apostrophe in a remark or an address before it reached the database.</li>
 * </ul>
 */
@Service
public class SaleInvoiceTransactionService {

    private static final Logger logger = LoggerFactory.getLogger(SaleInvoiceTransactionService.class);

    /** SequenceNoMaster.SequenceName for invoice numbers. */
    private static final String SEQUENCE_NAME = "SaleMaster";

    private static final String NUMBER_PREFIX = "INV";
    private static final int NUMBER_DIGITS = 9;

    /** Rows that count as live. */
    private static final int ACTIVE = 1;

    private final NamedParameterJdbcTemplate jdbc;
    private final SequenceNoMasterRepository sequences;
    private final SaleInvoiceWriter writer;
    private final InvoiceSaveGuard saveGuard;

    public SaleInvoiceTransactionService(NamedParameterJdbcTemplate jdbc,
                                         SequenceNoMasterRepository sequences,
                                         SaleInvoiceWriter writer,
                                         InvoiceSaveGuard saveGuard) {
        this.jdbc = jdbc;
        this.sequences = sequences;
        this.writer = writer;
        this.saveGuard = saveGuard;
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
            // An edit clears the existing lines before writing these, so an
            // empty list would silently empty the invoice.
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

        String saveKey = trimToNull(request.getClientRequestId());
        if (saveKey == null) {
            // No key, so nothing here can tell a second press from a second
            // invoice. Saved as before.
            return writeInvoice(request, lines, companyId, creating);
        }

        Optional<SaleInvoiceSaveResult> alreadySaved = saveGuard.begin(saveKey);
        if (alreadySaved.isPresent()) {
            // This exact save already finished. Hand back the invoice it made
            // instead of making a second one.
            return alreadySaved.get();
        }
        try {
            SaleInvoiceSaveResult result = writeInvoice(request, lines, companyId, creating);
            recordOnCommit(saveKey, result);
            return result;
        } catch (RuntimeException failed) {
            saveGuard.abandon(saveKey);
            throw failed;
        }
    }

    /**
     * Marks the key finished only once the transaction has committed.
     *
     * <p>Recording it earlier would let a save that later rolled back be
     * replayed as a success. A rollback releases the key instead, so the
     * operator can genuinely try again.
     */
    private void recordOnCommit(String saveKey, SaleInvoiceSaveResult result) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            saveGuard.complete(saveKey, result);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_COMMITTED) {
                    saveGuard.complete(saveKey, result);
                } else {
                    saveGuard.abandon(saveKey);
                }
            }
        });
    }

    /**
     * Writes the invoice and hands back what the screen needs.
     *
     * <p>Runs inside the caller's transaction, after the foreign-key checks
     * and while the number lock is held, so the header, its lines, the
     * references and the sale-order stamps all commit or all fail together.
     */
    private SaleInvoiceSaveResult writeInvoice(SaleInvoiceRequestDTO request,
                                               List<SaleInvoiceDetailRequestDTO> lines,
                                               Integer companyId,
                                               boolean creating) {
        Integer savedId = writer.write(request, lines);

        String billNo;
        Integer billNumber;
        if (creating) {
            billNumber = allocateNumber(companyId);
            billNo = NUMBER_PREFIX + String.format("%0" + NUMBER_DIGITS + "d", billNumber);
            jdbc.update("UPDATE SaleMaster SET CNumber = :number, CNumberDisplay = :display WHERE Id = :id",
                    new MapSqlParameterSource()
                            .addValue("number", billNumber)
                            .addValue("display", billNo)
                            .addValue("id", savedId));
        } else {
            // An edit never renumbers. Report the number it already carries.
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
                .saleTime(LocalDateTime.now())
                .created(creating)
                .build();
    }

    /**
     * Takes the next invoice number for the company.
     *
     * <p>One atomic statement, not a lock plus a read plus a write. The old
     * shape took {@code sp_getapplock} with {@code @LockOwner = 'Transaction'}
     * <em>before</em> the invoice was written, so it was held for the whole
     * save: every save for a company queued behind every other, with a
     * 15-second timeout — and when that timeout passed the code logged
     * "saving unguarded" and carried on, which is precisely when two saves
     * could take the same number.
     *
     * <p>{@code UPDATE ... OUTPUT} increments and reads as one statement, so
     * no two callers can see the same value however many save at once. It runs
     * at the end of the save, so the row lock it takes is held for a moment
     * instead of for the length of the transaction. The number still belongs
     * to this transaction: a rollback returns it and the sequence has no gap.
     */
    private Integer allocateNumber(Integer companyId) {
        List<Integer> allocated = jdbc.queryForList(
                "UPDATE SequenceNoMaster SET SequenceNo = ISNULL(SequenceNo, 0) + 1 "
                        + "OUTPUT INSERTED.SequenceNo "
                        + "WHERE CompanyRefId = :comid AND SequenceName = :name",
                new MapSqlParameterSource().addValue("comid", companyId).addValue("name", SEQUENCE_NAME),
                Integer.class);
        if (allocated.isEmpty()) {
            // ensureSequenceRowExists ran first, so the row is only missing if
            // it was deleted underneath us. Refuse rather than number this
            // invoice 1 and collide with the first one ever issued.
            throw new InvalidRequestException(
                    "No invoice number sequence exists for this company; the invoice was not saved");
        }
        return allocated.get(0);
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
        List<Lookup> wanted = new ArrayList<>(17);
        addLookup(wanted, "Login user", "AppUser", r.getUserRefId());
        addLookup(wanted, "Employee", "EmployeeMaster", r.getEmployeeRefId());
        addLookup(wanted, "Agent company", "AgentCompanyMaster", r.getAgentCompanyRefId());
        addLookup(wanted, "Agent", "Agent", r.getAgentMasterRefId());
        addLookup(wanted, "Off agent company", "AgentCompanyMaster", r.getOAgentCompanyRefId());
        addLookup(wanted, "Off agent", "Agent", r.getOAgentMasterRefId());
        addLookup(wanted, "Truck", "TruckMaster", r.getTruckRefId());
        addLookup(wanted, "Driver", "DriverMaster", r.getDriverRefId());
        addLookup(wanted, "Forklift operator", "EmployeeMaster", r.getForkliftByRefId());
        addLookup(wanted, "Seal by", "EmployeeMaster", r.getSealByRefId());
        addLookup(wanted, "Break seal by", "EmployeeMaster", r.getSealBreakByRefId());
        addLookup(wanted, "Seal by 2", "EmployeeMaster", r.getSealByRefId2());
        addLookup(wanted, "Break seal by 2", "EmployeeMaster", r.getSealBreakByRefId2());
        addLookup(wanted, "Seal by 3", "EmployeeMaster", r.getSealByRefId3());
        addLookup(wanted, "Break seal by 3", "EmployeeMaster", r.getSealBreakByRefId3());
        addLookup(wanted, "Boarding officer", "EmployeeMaster", r.getBoardingOfficerRefId());
        addLookup(wanted, "Boarding officer 2", "EmployeeMaster", r.getBoardingOfficer1RefId());
        if (wanted.isEmpty()) {
            return;
        }

        Set<String> found = findExisting(wanted, companyId);
        for (Lookup lookup : wanted) {
            if (!found.contains(lookup.key())) {
                throw new InvalidRequestException(
                        lookup.label() + " " + lookup.id() + " was not found for this company");
            }
        }
    }

    /** One id the invoice points at, and the table it has to exist in. */
    private record Lookup(String label, String table, Integer id) {
        String key() {
            return table + ":" + id;
        }
    }

    /** Skips ids the procedure skipped: 0 and null both mean "not supplied". */
    private static void addLookup(List<Lookup> wanted, String label, String table, Integer id) {
        if (id != null && id != 0) {
            wanted.add(new Lookup(label, table, id));
        }
    }

    /**
     * Which of those ids exist, in one round trip.
     *
     * <p>This used to be seventeen separate {@code SELECT COUNT(*)} queries,
     * one per reference, run on every single save whether or not the fields
     * were filled in. They are now one statement: the tables actually
     * referenced are unioned together and every id is checked at once. A
     * typical invoice names one or two tables, so a save that cost up to
     * seventeen round trips now costs one.
     *
     * <p>The table names are not user input — they are the literals above —
     * so building the statement from them is safe; every id is still bound.
     */
    private Set<String> findExisting(List<Lookup> wanted, Integer companyId) {
        Map<String, List<Integer>> byTable = new LinkedHashMap<>();
        for (Lookup lookup : wanted) {
            byTable.computeIfAbsent(lookup.table(), table -> new ArrayList<>()).add(lookup.id());
        }

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("comid", companyId)
                .addValue("active", ACTIVE);
        List<String> branches = new ArrayList<>(byTable.size());
        int index = 0;
        for (Map.Entry<String, List<Integer>> entry : byTable.entrySet()) {
            String idsParam = "ids" + index++;
            params.addValue(idsParam, entry.getValue());
            branches.add("SELECT '" + entry.getKey() + "' AS TableName, Id FROM [" + entry.getKey() + "] "
                    + "WITH (NOLOCK) WHERE CompanyRefId = :comid AND Active = :active AND Id IN (:" + idsParam + ")");
        }

        return new HashSet<>(jdbc.query(String.join(" UNION ALL ", branches), params,
                (rs, i) -> rs.getString("TableName") + ":" + rs.getInt("Id")));
    }

    // ─────────────────────────────────────────────────────────── numbering ──

    /**
     * Makes sure the company has a SequenceNoMaster row before a number is
     * taken from it, seeded from the highest CNumber already issued so an
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

    // ─────────────────────────────────────────────────────────── helpers ──

    private static Integer asInteger(Object value) {
        return value instanceof Number number ? number.intValue() : null;
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
