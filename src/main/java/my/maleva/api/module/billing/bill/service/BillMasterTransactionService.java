package my.maleva.api.module.billing.bill.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.module.accounting.entity.GLAccounts;
import my.maleva.api.module.accounting.repository.GLAccountsRepository;
import my.maleva.api.module.billing.bill.dto.BillDetailsEditDto;
import my.maleva.api.module.billing.bill.dto.BillDetailsInsertDto;
import my.maleva.api.module.billing.bill.dto.BillDetailsViewDto;
import my.maleva.api.module.billing.bill.dto.BillMasterEditDto;
import my.maleva.api.module.billing.bill.dto.BillMasterF5ViewDto;
import my.maleva.api.module.billing.bill.dto.BillMasterInsertDto;
import my.maleva.api.module.billing.bill.dto.BillMasterSaveResponseDto;
import my.maleva.api.module.billing.bill.dto.BillMasterViewDto;
import my.maleva.api.module.billing.bill.dto.SelectBillMasterRequestDto;
import my.maleva.api.module.billing.bill.entity.BillDetails;
import my.maleva.api.module.billing.bill.entity.BillMaster;
import my.maleva.api.module.billing.bill.repository.BillDetailsRepository;
import my.maleva.api.module.billing.bill.repository.BillMasterRepository;
import my.maleva.api.module.billing.billorder.entity.BillsOrderMaster;
import my.maleva.api.module.billing.billorder.repository.BillsOrderMasterRepository;
import my.maleva.api.module.employee.repository.EmployeeMasterRepository;
import my.maleva.api.module.fleet.repository.DriverMasterRepository;
import my.maleva.api.module.fleet.repository.TruckMasterRepository;
import my.maleva.api.module.user.repository.AppUserRepository;
import my.maleva.api.module.master.entity.SequenceNoMaster;
import my.maleva.api.module.master.entity.SymbolMaster;
import my.maleva.api.module.master.repository.SequenceNoMasterRepository;
import my.maleva.api.module.master.repository.SymbolMasterRepository;
import my.maleva.api.module.payment.repository.PaymentTermsMasterRepository;
import my.maleva.api.module.supplier.entity.Supplier;
import my.maleva.api.module.supplier.repository.SupplierRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * The bill screen's transactional operations — the Java port of legacy
 * {@code BillMasterServices} (Services/Transaction/BillMasterServices.cs).
 *
 * <p>{@code SP_BillMaster} is reimplemented here in Java rather than called,
 * matching how the sibling purchase-order module was migrated: the SP took a
 * JSON blob built by string concatenation, which is why the legacy code had to
 * strip every apostrophe out of user-typed remarks before saving.
 *
 * <p>Every query below is parameterised; the legacy versions concatenated the
 * search box and ids straight into SQL.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BillMasterTransactionService {

    private static final String SEQUENCE_NAME = "BillMaster";
    private static final DateTimeFormatter GRID_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final BillMasterRepository billMasters;
    private final BillDetailsRepository billDetails;
    private final SupplierRepository suppliers;
    private final PaymentTermsMasterRepository paymentTerms;
    private final SequenceNoMasterRepository sequences;
    private final SymbolMasterRepository symbols;
    private final GLAccountsRepository glAccounts;
    private final BillsOrderMasterRepository billsOrders;
    private final AppUserRepository appUsers;
    private final EmployeeMasterRepository employees;
    private final TruckMasterRepository trucks;
    private final DriverMasterRepository drivers;
    private final NamedParameterJdbcTemplate jdbc;

    /* ── document number ───────────────────────────────────────────── */

    /**
     * The number the next bill will get, for display on a blank screen.
     *
     * <p>Preview only — the number is assigned for real inside {@link #save},
     * so two clerks with the screen open do not both save as the same bill.
     */
    @Transactional(readOnly = true)
    public String nextBillNumber(Integer companyId) {
        LocalDate today = LocalDate.now();
        return formatBillNumber(today, nextSequence(companyId, today));
    }

    private Integer nextSequence(Integer companyId, LocalDate on) {
        Integer max = sequences.findMaxBillMasterSequenceNo(
                companyId, on.getYear(), on.getMonthValue());
        return (max == null || max == 0) ? 1 : max + 1;
    }

    /** Legacy format: BIL + 2-digit year + 2-digit month + / + 3-digit running number. */
    static String formatBillNumber(LocalDate on, int sequence) {
        return String.format("BIL%02d%02d/%03d",
                on.getYear() % 100, on.getMonthValue(), sequence);
    }

    /* ── save ──────────────────────────────────────────────────────── */

    /**
     * Insert or update one bill with its lines.
     *
     * <p>Id 0/null inserts and assigns the document number; otherwise the
     * existing bill is updated and keeps its number. Lines missing from the
     * payload are deleted, so the grid is the source of truth.
     */
    @Transactional(rollbackFor = Exception.class)
    public BillMasterSaveResponseDto save(BillMasterInsertDto dto, Integer companyId) {
        List<BillDetailsInsertDto> lines = dto.getBillDetails() == null
                ? List.of()
                : dto.getBillDetails().stream().filter(Objects::nonNull).toList();
        if (lines.isEmpty()) {
            return failure("Add at least one bill line");
        }
        for (BillDetailsInsertDto line : lines) {
            if (line.getAccountMasterRefId() == null || line.getAccountMasterRefId() == 0) {
                return failure("Every line needs an account code");
            }
        }
        if (dto.getSupplierRefId() == null || !suppliers.existsById(dto.getSupplierRefId())) {
            return failure("Supplier not found: " + dto.getSupplierRefId());
        }

        // The five reference checks SP_BillMaster ran before touching a row.
        // Each only applies when the id is actually set, and each requires the
        // reference to belong to this company and still be active.
        String reference = checkReferences(dto, companyId);
        if (reference != null) {
            return failure(reference);
        }

        boolean isNew = dto.getId() == null || dto.getId() == 0;
        LocalDateTime now = LocalDateTime.now();

        if (isNew) {
            // A clerk clicking Save three times on a slow connection sends
            // three identical inserts. The browser guard the legacy screen
            // used cannot stop that — a reload, a second tab or a retry all
            // slip past it — so the rule is enforced here.
            //
            // The lock serialises the copies; whichever gets in first creates
            // the bill and the rest then see it and answer with the same
            // number instead of entering it again.
            if (!lockSaveIntent(dto, companyId)) {
                return failure("This bill is already being saved — please wait a moment");
            }
            BillMaster duplicate = findDuplicate(dto, companyId, now);
            if (duplicate != null) {
                log.info("Duplicate save for bill {} ignored; returning the existing bill",
                        duplicate.getCNumberDisplay());
                return BillMasterSaveResponseDto.builder()
                        .success(true)
                        .message("This bill was already saved as " + duplicate.getCNumberDisplay())
                        .id(duplicate.getId())
                        .billNoDisplay(duplicate.getCNumberDisplay())
                        .duplicate(true)
                        .build();
            }
        }

        BillMaster bill;
        if (isNew) {
            bill = new BillMaster();
            bill.setCompanyRefId(companyId);
            bill.setCreatedDate(now);
            bill.setCreatedBy(dto.getEmployeeRefId() == null
                    ? "SYSTEM" : String.valueOf(dto.getEmployeeRefId()));
            bill.setActive(1);
        } else {
            bill = billMasters.findById(dto.getId()).orElse(null);
            if (bill == null || !Objects.equals(bill.getCompanyRefId(), companyId)) {
                return failure("Bill not found: " + dto.getId());
            }
        }

        applyHeader(bill, dto, companyId, now);

        // Saved before the lines so a new bill has an id to hang them on.
        bill = billMasters.save(bill);

        if (isNew) {
            // Numbered from the bill date, not today — a bill back-dated into
            // last month belongs in last month's series.
            LocalDate on = bill.getSaleDate() == null
                    ? LocalDate.now() : bill.getSaleDate().toLocalDate();
            int sequence = nextSequence(companyId, on);
            bill.setCNumber(sequence);
            bill.setCNumberDisplay(formatBillNumber(on, sequence));
            bill = billMasters.save(bill);
            recordSequence(companyId, on, sequence);
            markPurchaseOrderInvoiced(bill.getBillsOrderMasterRefId());
        }

        applyLines(bill, lines, now);

        log.info("Bill {} saved as {} ({} line(s))",
                bill.getId(), bill.getCNumberDisplay(), lines.size());
        return BillMasterSaveResponseDto.builder()
                .success(true)
                .message(isNew ? "Bill created successfully" : "Bill updated successfully")
                .id(bill.getId())
                .billNoDisplay(bill.getCNumberDisplay())
                .build();
    }

    /**
     * How long two identical un-invoiced bills are treated as one double-click
     * rather than two real bills.
     */
    private static final Duration DUPLICATE_WINDOW = Duration.ofMinutes(2);

    /**
     * Takes a SQL Server application lock on this save's identity, so two
     * copies of the same click cannot both pass the duplicate check.
     *
     * <p>The lock is held by the transaction and released when it ends, and it
     * lives in the database rather than the JVM so it still works with more
     * than one app instance behind a load balancer. The legacy stored
     * procedure reached for the same mechanism ({@code sp_releaseapplock},
     * left commented out in SP_BillMaster).
     *
     * <p>A lock that cannot be taken within the timeout means an identical
     * save is genuinely in flight — the caller should not insert.
     */
    private boolean lockSaveIntent(BillMasterInsertDto dto, Integer companyId) {
        String key = "BillMasterSave:" + companyId + ":" + saveFingerprint(dto);
        try {
            Integer status = jdbc.queryForObject(
                    "DECLARE @status int; "
                            + "EXEC @status = sp_getapplock @Resource = :key, "
                            + "@LockMode = 'Exclusive', @LockOwner = 'Transaction', "
                            + "@LockTimeout = 15000; SELECT @status",
                    new MapSqlParameterSource("key", key),
                    Integer.class);
            // 0 = granted, 1 = granted after waiting; negatives are timeout,
            // deadlock or a cancelled request.
            return status == null || status >= 0;
        } catch (Exception ex) {
            // Never let the guard itself block a save — the duplicate check
            // below still runs, it just loses its protection against a
            // simultaneous twin.
            log.warn("Could not take the duplicate-save lock ({}); continuing unguarded",
                    ex.getMessage());
            return true;
        }
    }

    /** Identifies one save attempt: the same click always produces the same string. */
    private static String saveFingerprint(BillMasterInsertDto dto) {
        String invoiceNo = dto.getInvoiceNo() == null ? "" : dto.getInvoiceNo().trim();
        if (!invoiceNo.isEmpty()) {
            return "inv:" + invoiceNo;
        }
        return "sup:" + dto.getSupplierRefId()
                + "|date:" + (dto.getSaleDate() == null ? "" : dto.getSaleDate().toLocalDate())
                + "|amt:" + f(dto.getAmount());
    }

    /**
     * The bill this save would duplicate, or null when it is genuinely new.
     *
     * <p>A supplier invoice number identifies a bill outright, so a repeat of
     * it is the same bill however long ago the first save was. Without one
     * there is nothing to match on but supplier, date and amount, which two
     * real bills could legitimately share — so that check only looks at the
     * last couple of minutes, which is the double-click it is meant to catch.
     */
    private BillMaster findDuplicate(BillMasterInsertDto dto, Integer companyId, LocalDateTime now) {
        String invoiceNo = dto.getInvoiceNo() == null ? "" : dto.getInvoiceNo().trim();
        if (!invoiceNo.isEmpty()) {
            return billMasters.findLiveByInvoiceNo(companyId, invoiceNo)
                    .stream().findFirst().orElse(null);
        }
        if (dto.getSaleDate() == null) {
            return null;
        }
        return billMasters.findRecentlyEnteredLikeThis(
                        companyId, dto.getSupplierRefId(), dto.getSaleDate(),
                        f(dto.getAmount()), now.minus(DUPLICATE_WINDOW))
                .stream().findFirst().orElse(null);
    }

    /**
     * Mirrors SP_BillMaster's reference validation, message for message.
     * Returns the complaint, or null when everything checks out.
     */
    private String checkReferences(BillMasterInsertDto dto, Integer companyId) {
        Integer userRefId = zeroToNull(dto.getUserRefId());
        if (userRefId != null
                && !appUsers.existsByIdAndCompanyRefIdAndActive(userRefId, companyId, 1)) {
            return "Login User Not Found Issue id" + userRefId;
        }
        Integer employeeRefId = zeroToNull(dto.getEmployeeRefId());
        if (employeeRefId != null
                && !employees.existsByIdAndCompanyRefIdAndActive(employeeRefId, companyId, 1)) {
            return "Employee Not Found Issue id" + employeeRefId;
        }
        Integer truckRefId = zeroToNull(dto.getTruckRefid());
        if (truckRefId != null
                && !trucks.existsByIdAndCompanyRefIdAndActive(truckRefId, companyId, 1)) {
            return "Truck Not Found Issue id" + truckRefId;
        }
        Integer driverRefId = zeroToNull(dto.getDriverRefid());
        if (driverRefId != null
                && !drivers.existsByIdAndCompanyRefIdAndActive(driverRefId, companyId, 1)) {
            return "Driver Not Found Issue id" + driverRefId;
        }
        Integer termsRefId = zeroToNull(dto.getPaymentTermsRefid());
        if (termsRefId != null
                && !paymentTerms.existsByIdAndCompanyRefIdAndActive(termsRefId, companyId, 1)) {
            return "Payment Terms Not Found Issue id" + termsRefId;
        }
        return null;
    }

    private void applyHeader(BillMaster bill, BillMasterInsertDto dto,
                             Integer companyId, LocalDateTime now) {
        bill.setCompanyRefId(companyId);
        bill.setUserRefId(zeroToNull(dto.getUserRefId()));
        bill.setEmployeeRefId(zeroToNull(dto.getEmployeeRefId()));
        // Who touched the bill last — the SP wrote this from EmployeeRefId on
        // both insert and update, and the F5 grid reads it.
        bill.setLastEmployeeRefId(zeroToNull(dto.getEmployeeRefId()));
        bill.setSupplierRefId(dto.getSupplierRefId());
        bill.setSaleDate(dto.getSaleDate() == null ? now : dto.getSaleDate());
        bill.setInvoiceNo(dto.getInvoiceNo());
        // InvoiceDate is NOT NULL in the table; the bill date stands in when
        // the supplier invoice is undated, as the legacy SP did.
        bill.setInvoiceDate(dto.getInvoiceDate() == null ? bill.getSaleDate() : dto.getInvoiceDate());
        bill.setDueDate(dto.getDueDate());
        bill.setSaleType(orDefault(dto.getSaleType(), "CASH"));
        bill.setBillStatus(dto.getBillStatus());
        bill.setDescription(dto.getDescription());
        bill.setRemarks(dto.getRemarks());
        bill.setPaymentTermsRefid(dto.getPaymentTermsRefid());
        bill.setTruckRefid(zeroToNull(dto.getTruckRefid()));
        bill.setDriverRefid(zeroToNull(dto.getDriverRefid()));
        bill.setCoinage(f(dto.getCoinage()));
        bill.setGrossAmount(f(dto.getGrossAmount()));
        bill.setTaxAmount(f(dto.getTaxAmount()));
        bill.setDiscountAmount(f(dto.getDiscountAmount()));
        bill.setPlusAmount(f(dto.getPlusAmount()));
        bill.setMinusAmount(f(dto.getMinusAmount()));
        bill.setAmount(f(dto.getAmount()));
        bill.setCurrencyValue(f(dto.getCurrencyValue()));
        bill.setCurrencyValue1(f(dto.getCurrencyValue1()));
        bill.setActualAmount(f(dto.getActualAmount()));
        if (bill.getId() == null) {
            // Insert only: SP_BillMaster's UPDATE branch leaves this column
            // alone, so editing a bill can never re-point it at another
            // purchase order (which would strand the first one as invoiced).
            bill.setBillsOrderMasterRefId(zeroToNull(dto.getBillsOrderMasterRefId()));
        }
        bill.setModifiedDate(now);
        bill.setModifiedBy(dto.getEmployeeRefId() == null
                ? "SYSTEM" : String.valueOf(dto.getEmployeeRefId()));
        if (bill.getActive() == null) {
            bill.setActive(1);
        }
        if (bill.getCNumber() == null) {
            bill.setCNumber(0);
        }
        if (bill.getCNumberDisplay() == null) {
            bill.setCNumberDisplay("");
        }
        if (bill.getCreatedBy() == null) {
            bill.setCreatedBy("SYSTEM");
        }
        if (bill.getCreatedDate() == null) {
            bill.setCreatedDate(now);
        }
    }

    /** Upserts the posted lines and deletes the ones the grid dropped. */
    private void applyLines(BillMaster bill, List<BillDetailsInsertDto> lines, LocalDateTime now) {
        Map<Integer, BillDetails> existing = new LinkedHashMap<>();
        for (BillDetails row : billDetails.findByBillMasterRefId(bill.getId())) {
            existing.put(row.getId(), row);
        }

        List<BillDetails> toSave = new ArrayList<>();
        for (BillDetailsInsertDto line : lines) {
            BillDetails row = (line.getId() != null && line.getId() != 0)
                    ? existing.remove(line.getId()) : null;
            if (row == null) {
                row = new BillDetails();
                row.setCreatedDate(now);
            }
            row.setBillMasterRefId(bill.getId());
            row.setAccountMasterRefId(line.getAccountMasterRefId());
            row.setMrp(f(line.getMrp()));
            row.setPurchaseRate(f(line.getPurchaseRate()));
            row.setItemQty(f(line.getItemQty()));
            row.setDiscPer(f(line.getDiscPer()));
            row.setDiscAmount(f(line.getDiscAmount()));
            row.setLandingCost(f(line.getLandingCost()));
            row.setTaxPercent(f(line.getTaxPercent()));
            row.setTaxAmount(f(line.getTaxAmount()));
            row.setSalesRate(f(line.getSalesRate()));
            row.setNetSalesRate(f(line.getNetSalesRate()));
            row.setAmount(f(line.getAmount()));
            row.setRemarksD(line.getRemarksD());
            row.setCurrencyValue(f(line.getCurrencyValue()));
            row.setActualAmount(f(line.getActualAmount()));
            row.setActualAmount1(f(line.getActualAmount1()));
            row.setModifiedDate(now);
            toSave.add(row);
        }

        if (!existing.isEmpty()) {
            billDetails.deleteAll(existing.values());
        }
        billDetails.saveAll(toSave);
    }

    /**
     * Flags the purchase order this bill was raised from as invoiced.
     *
     * <p>SP_BillMaster did this on insert only. Without it the PO keeps
     * showing as awaiting invoice and can be converted a second time, so the
     * supplier ends up billed twice for one order.
     */
    private void markPurchaseOrderInvoiced(Integer billsOrderMasterRefId) {
        if (billsOrderMasterRefId == null || billsOrderMasterRefId == 0) {
            return;
        }
        BillsOrderMaster order = billsOrders.findById(billsOrderMasterRefId).orElse(null);
        if (order == null) {
            log.warn("Bill references purchase order {} which no longer exists — not flagged",
                    billsOrderMasterRefId);
            return;
        }
        order.setPStatus(1);
        order.setBillStatus("INVOICE MADE");
        order.setModifiedDate(LocalDateTime.now());
        order.setModifiedBy("From Bills");
        billsOrders.save(order);
        log.info("Purchase order {} flagged as invoiced", billsOrderMasterRefId);
    }

    private void recordSequence(Integer companyId, LocalDate on, int sequence) {
        SequenceNoMaster counter = sequences
                .findByCompanyRefIdAndSequenceNameAndSequenceYearAndSequenceMonth(
                        companyId, SEQUENCE_NAME, on.getYear(), on.getMonthValue())
                .orElseGet(() -> {
                    SequenceNoMaster fresh = new SequenceNoMaster();
                    fresh.setCompanyRefId(companyId);
                    fresh.setSequenceName(SEQUENCE_NAME);
                    fresh.setSequenceYear(on.getYear());
                    fresh.setSequenceMonth(on.getMonthValue());
                    fresh.setSequenceDate(LocalDateTime.now());
                    return fresh;
                });
        counter.setSequenceNo(sequence);
        sequences.save(counter);
    }

    /* ── delete ────────────────────────────────────────────────────── */

    /**
     * Soft-deletes a bill (Active=2), the way legacy did — payment history
     * still references it, so the row must stay.
     */
    @Transactional
    public boolean delete(Integer id, Integer companyId) {
        BillMaster bill = billMasters.findById(id).orElse(null);
        if (bill == null || !Objects.equals(bill.getCompanyRefId(), companyId)) {
            return false;
        }
        bill.setActive(2);
        bill.setModifiedDate(LocalDateTime.now());
        billMasters.save(bill);
        return true;
    }

    /* ── edit ──────────────────────────────────────────────────────── */

    /**
     * Loads one bill for editing, by id or — when {@code billNumber} is given
     * — by the running number the clerk typed.
     */
    @Transactional(readOnly = true)
    public Optional<BillMasterEditDto> edit(Integer id, Integer billNumber, Integer companyId) {
        BillMaster bill = null;
        if (billNumber != null && billNumber != 0) {
            bill = billMasters.findByCompanyRefIdAndCNumber(companyId, billNumber).orElse(null);
        } else if (id != null && id != 0) {
            bill = billMasters.findById(id)
                    .filter(b -> Objects.equals(b.getCompanyRefId(), companyId))
                    .orElse(null);
        }
        if (bill == null || (bill.getActive() != null && bill.getActive() == 2)) {
            return Optional.empty();
        }

        List<BillDetails> rows = billDetails.findByBillMasterRefId(bill.getId());
        Map<Integer, GLAccounts> accounts = accountsByRowIndex(
                rows.stream().map(BillDetails::getAccountMasterRefId)
                        .filter(Objects::nonNull).distinct().toList());

        List<BillDetailsEditDto> lines = new ArrayList<>();
        for (BillDetails row : rows) {
            GLAccounts account = accounts.get(row.getAccountMasterRefId());
            lines.add(BillDetailsEditDto.builder()
                    .id(row.getId())
                    .billMasterRefId(row.getBillMasterRefId())
                    .accountMasterRefId(row.getAccountMasterRefId())
                    .productCode(account == null ? "" : account.getGlAccountCode())
                    .productName(account == null ? "" : account.getDescription())
                    .mrp(row.getMrp())
                    .purchaseRate(row.getPurchaseRate())
                    .itemQty(row.getItemQty())
                    .discPer(row.getDiscPer())
                    .discAmount(row.getDiscAmount())
                    .landingCost(row.getLandingCost())
                    .taxPercent(row.getTaxPercent())
                    .taxAmount(row.getTaxAmount())
                    .salesRate(row.getSalesRate())
                    .netSalesRate(row.getNetSalesRate())
                    .amount(row.getAmount())
                    .remarksD(row.getRemarksD())
                    .currencyValue(row.getCurrencyValue())
                    .actualAmount(row.getActualAmount())
                    .actualAmount1(row.getActualAmount1())
                    .build());
        }

        return Optional.of(BillMasterEditDto.builder()
                .id(bill.getId())
                .companyRefId(bill.getCompanyRefId())
                .userRefId(bill.getUserRefId())
                .employeeRefId(bill.getEmployeeRefId())
                .supplierRefId(bill.getSupplierRefId())
                .saleDate(bill.getSaleDate())
                .sSaleDate(format(bill.getSaleDate()))
                .invoiceNo(bill.getInvoiceNo())
                .invoiceDate(bill.getInvoiceDate())
                .sInvoiceDate(format(bill.getInvoiceDate()))
                .dueDate(bill.getDueDate())
                .sDueDate(bill.getDueDate() == null ? "" : GRID_DATE.format(bill.getDueDate()))
                .saleType(bill.getSaleType())
                .billStatus(bill.getBillStatus())
                .cNumberDisplay(bill.getCNumberDisplay())
                .cNumber(bill.getCNumber())
                .coinage(bill.getCoinage())
                .grossAmount(bill.getGrossAmount())
                .taxAmount(bill.getTaxAmount())
                .discountAmount(bill.getDiscountAmount())
                .plusAmount(bill.getPlusAmount())
                .minusAmount(bill.getMinusAmount())
                .amount(bill.getAmount())
                .remarks(bill.getRemarks())
                .description(bill.getDescription())
                .paymentTermsRefid(bill.getPaymentTermsRefid())
                .currencyValue(bill.getCurrencyValue())
                .currencyValue1(bill.getCurrencyValue1())
                .actualAmount(bill.getActualAmount())
                .truckRefid(bill.getTruckRefid())
                .driverRefid(bill.getDriverRefid())
                .billsOrderMasterRefId(bill.getBillsOrderMasterRefId())
                .active(bill.getActive())
                .qneCode(bill.getQneCode())
                .qneId(bill.getQneId())
                .billDetails(lines)
                .build());
    }

    private Map<Integer, GLAccounts> accountsByRowIndex(List<Integer> rowIndexes) {
        Map<Integer, GLAccounts> byRowIndex = new HashMap<>();
        if (rowIndexes.isEmpty()) {
            return byRowIndex;
        }
        for (GLAccounts account : glAccounts.findByRowIndexIn(rowIndexes)) {
            if (account.getRowIndex() != null) {
                byRowIndex.putIfAbsent(account.getRowIndex(), account);
            }
        }
        return byRowIndex;
    }

    /* ── F5 search ─────────────────────────────────────────────────── */

    /**
     * The bill F5 grid: matching headers plus all of their lines.
     *
     * <p>A search term matches the bill number or the supplier invoice number
     * and, as in legacy, overrides the date and supplier filters entirely —
     * so a clerk can find one bill without knowing when it was raised.
     */
    @Transactional(readOnly = true)
    public BillMasterF5ViewDto search(SelectBillMasterRequestDto request, Integer companyId) {
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("comid", companyId);
        StringBuilder where = new StringBuilder();

        boolean hasSearch = request.getSearch() != null && !request.getSearch().isBlank();
        if (hasSearch) {
            where.append(" AND (A.CNumberDisplay = :search OR A.InvoiceNo = :search) ");
            params.addValue("search", request.getSearch().trim());
        } else {
            if (request.getId() != null && request.getId() != 0) {
                where.append(" AND A.SupplierRefId = :supplierId ");
                params.addValue("supplierId", request.getId());
            }
            if (request.getEmployeeid() != null && request.getEmployeeid() != 0) {
                where.append(" AND A.EmployeeRefId = :employeeId ");
                params.addValue("employeeId", request.getEmployeeid());
            }
            String dateColumn = (request.getInvoicecheck() != null && request.getInvoicecheck() == 1)
                    ? "A.InvoiceDate" : "A.SaleDate";
            where.append(" AND ").append(dateColumn).append(" BETWEEN :fromDate AND :toDate ");
            params.addValue("fromDate", startOfDay(request.getFromdate()));
            params.addValue("toDate", endOfDay(request.getTodate()));
        }

        String masterSql = headerSelect()
                + "WHERE A.CompanyRefId = :comid AND A.Active = 1" + where
                + " ORDER BY A.SaleDate DESC, A.Id DESC";
        List<BillMasterViewDto> headers = jdbc.query(masterSql, params, this::mapHeader);

        String detailSql = "SELECT B.DiscAmount AS DiscountAmt, B.DiscPer AS DiscountPercent, "
                + "B.ItemQty, B.MRP, I.Description AS ProductName, B.SalesRate AS SaleRate, "
                + "B.BillMasterRefId AS SaleRefId, A.TaxAmount AS TaxAmt, B.TaxPercent, "
                + "I.GLAccountCode AS ProductCode, B.Amount AS SAmount, "
                + "ISNULL(B.RemarksD,'') AS RemarksD "
                + "FROM BillDetails B WITH(NOLOCK) "
                + "INNER JOIN BillMaster A WITH(NOLOCK) ON B.BillMasterRefId = A.Id "
                + "INNER JOIN GLAccounts I WITH(NOLOCK) ON B.AccountMasterRefId = I.RowIndex "
                + "WHERE A.CompanyRefId = :comid AND A.Active = 1" + where;

        List<BillDetailsViewDto> lines = jdbc.query(detailSql, params, (rs, i) ->
                BillDetailsViewDto.builder()
                        .discountAmt(rs.getFloat("DiscountAmt"))
                        .discountPercent(rs.getFloat("DiscountPercent"))
                        .itemQty(rs.getFloat("ItemQty"))
                        .mrp(rs.getFloat("MRP"))
                        .productName(rs.getString("ProductName"))
                        .saleRate(rs.getFloat("SaleRate"))
                        .saleRefId(rs.getInt("SaleRefId"))
                        .taxAmt(rs.getFloat("TaxAmt"))
                        .taxPercent(rs.getFloat("TaxPercent"))
                        .productCode(rs.getString("ProductCode"))
                        .sAmount(rs.getFloat("SAmount"))
                        .remarksD(rs.getString("RemarksD"))
                        .build());

        return BillMasterF5ViewDto.builder().billMaster(headers).billDetails(lines).build();
    }

    /**
     * Bills that are due and not yet paid.
     *
     * <p>{@code dueWithinRange} true limits to bills falling due inside the
     * dates; false returns everything already overdue.
     */
    @Transactional(readOnly = true)
    public List<BillMasterViewDto> dueBills(Integer companyId, String fromDate, String toDate,
                                            Integer supplierId, Integer employeeId,
                                            boolean dueWithinRange) {
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("comid", companyId);
        StringBuilder where = new StringBuilder(
                " AND A.Id NOT IN (SELECT PD.BillMasterRefId FROM Payment PM WITH(NOLOCK) "
                        + "INNER JOIN PaymentDetails PD WITH(NOLOCK) ON PM.Id = PD.PaymentRefId "
                        + "WHERE PD.BillMasterRefId IS NOT NULL) ");

        if (dueWithinRange) {
            where.append(" AND A.DueDate BETWEEN :fromDate AND :toDate ");
            params.addValue("fromDate", startOfDay(fromDate));
            params.addValue("toDate", endOfDay(toDate));
        } else {
            where.append(" AND A.DueDate < GETDATE() ");
        }
        if (employeeId != null && employeeId != 0) {
            where.append(" AND A.EmployeeRefId = :employeeId ");
            params.addValue("employeeId", employeeId);
        }
        if (supplierId != null && supplierId != 0) {
            where.append(" AND A.SupplierRefId = :supplierId ");
            params.addValue("supplierId", supplierId);
        }

        String sql = headerSelect()
                + "WHERE A.CompanyRefId = :comid AND A.Active = 1" + where
                + " ORDER BY A.DueDate ASC";
        return jdbc.query(sql, params, this::mapHeader);
    }

    /** Shared header projection for both grids — same columns the legacy sent. */
    private static String headerSelect() {
        return "SELECT A.Id, ISNULL(A.QNECode,'') AS QNECode, ISNULL(A.QNEId,'') AS QNEId, "
                + "ISNULL(E.EmployeeName,'') AS EmployeeName, "
                + "FORMAT(ISNULL(A.SaleDate,'1900-01-01'),'dd/MM/yyyy') AS BillDate, "
                + "ISNULL(A.InvoiceNo,'') AS InvoiceNo, "
                + "FORMAT(ISNULL(A.InvoiceDate,'1900-01-01'),'dd/MM/yyyy') AS InvoiceDate, "
                + "A.CNumberDisplay AS BillNoDisplay, ISNULL(A.BillStatus,'') AS BillStatus, "
                + "FORMAT(ISNULL(A.Created_Date,'1900-01-01'),'dd/MM/yyyy hh:mm:ss') AS BillTime, "
                + "B.SupplierName AS SupplierName, A.Amount AS NetAmt, A.SaleType AS SaleType, "
                + "A.CNumber AS BillNo, ISNULL(J.TruckName,'') AS TruckName, "
                + "ISNULL(K.DriverName,'') AS DriverName, ISNULL(A.Remarks,'') AS Remarks "
                + "FROM BillMaster A WITH(NOLOCK) "
                + "INNER JOIN Supplier B WITH(NOLOCK) ON A.SupplierRefId = B.Id "
                + "LEFT JOIN EmployeeMaster E WITH(NOLOCK) ON E.Id = A.EmployeeRefId "
                + "LEFT JOIN TruckMaster J WITH(NOLOCK) ON J.Id = A.TruckRefid "
                + "LEFT JOIN DriverMaster K WITH(NOLOCK) ON K.Id = A.DriverRefid ";
    }

    private BillMasterViewDto mapHeader(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return BillMasterViewDto.builder()
                .id(rs.getInt("Id"))
                .qneCode(rs.getString("QNECode"))
                .qneId(rs.getString("QNEId"))
                .employeeName(rs.getString("EmployeeName"))
                .billDate(rs.getString("BillDate"))
                .invoiceNo(rs.getString("InvoiceNo"))
                .invoiceDate(rs.getString("InvoiceDate"))
                .billNoDisplay(rs.getString("BillNoDisplay"))
                .billStatus(rs.getString("BillStatus"))
                .billTime(rs.getString("BillTime"))
                .supplierName(rs.getString("SupplierName"))
                .netAmt(rs.getFloat("NetAmt"))
                .saleType(rs.getString("SaleType"))
                .billNo(rs.getInt("BillNo"))
                .truckName(rs.getString("TruckName"))
                .driverName(rs.getString("DriverName"))
                .remarks(rs.getString("Remarks"))
                .build();
    }

    /* ── screen lookups ────────────────────────────────────────────── */

    /** Descriptions used on previous bills, for the description dropdown. */
    @Transactional(readOnly = true)
    public List<String> descriptions(Integer companyId) {
        return jdbc.queryForList(
                "SELECT DISTINCT LTRIM(RTRIM(A.Description)) AS AccountName "
                        + "FROM BillMaster A WITH(NOLOCK) "
                        + "WHERE A.Description IS NOT NULL AND LTRIM(RTRIM(A.Description)) <> '' "
                        + "AND A.Active <> 2 AND A.CompanyRefId = :comid "
                        + "ORDER BY AccountName",
                new MapSqlParameterSource("comid", companyId),
                String.class);
    }

    /** The supplier's currency rate, which seeds the bill's conversion. */
    @Transactional(readOnly = true)
    public Float supplierCurrencyValue(Integer companyId, Integer supplierId) {
        Supplier supplier = suppliers.findById(supplierId).orElse(null);
        if (supplier == null || !Objects.equals(supplier.getCompanyRefId(), companyId)
                || (supplier.getActive() != null && supplier.getActive() == 2)
                || supplier.getSymbolRefid() == null) {
            return null;
        }
        return symbols.findById(supplier.getSymbolRefid())
                .map(SymbolMaster::getCurrencyValue)
                .orElse(null);
    }

    /**
     * How much has already been paid against this bill.
     *
     * <p>The screen blocks an edit that would drop the bill total below this,
     * which would leave the supplier over-paid.
     */
    @Transactional(readOnly = true)
    public double paidAmount(Integer companyId, Integer billId) {
        Double paid = jdbc.queryForObject(
                "SELECT SUM(ISNULL(PD.PaymentAmount,0)) FROM PaymentDetails PD WITH(NOLOCK) "
                        + "INNER JOIN BillMaster BM WITH(NOLOCK) ON PD.BillMasterRefId = BM.Id "
                        + "WHERE BM.Id = :billId AND BM.Active <> 2 AND BM.CompanyRefId = :comid",
                new MapSqlParameterSource().addValue("billId", billId).addValue("comid", companyId),
                Double.class);
        return paid == null ? 0d : paid;
    }

    /**
     * Whether a supplier invoice number is already on another bill.
     *
     * <p>Legacy shipped every invoice number in the company to the browser and
     * searched the array there; this answers the actual question in SQL.
     */
    @Transactional(readOnly = true)
    public boolean invoiceNoExists(Integer companyId, String invoiceNo, Integer excludeBillId) {
        if (invoiceNo == null || invoiceNo.isBlank()) {
            return false;
        }
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("comid", companyId)
                .addValue("invoiceNo", invoiceNo.trim())
                .addValue("excludeId", excludeBillId == null ? 0 : excludeBillId);
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(1) FROM BillMaster BM WITH(NOLOCK) WHERE BM.Active <> 2 "
                        + "AND BM.CompanyRefId = :comid "
                        + "AND LTRIM(RTRIM(ISNULL(BM.InvoiceNo,''))) = :invoiceNo "
                        + "AND BM.Id <> :excludeId",
                params, Integer.class);
        return count != null && count > 0;
    }

    /* ── helpers ───────────────────────────────────────────────────── */

    private static BillMasterSaveResponseDto failure(String message) {
        return BillMasterSaveResponseDto.builder().success(false).message(message).build();
    }

    private static Integer zeroToNull(Integer value) {
        return (value == null || value == 0) ? null : value;
    }

    private static Float f(Float value) {
        return value == null ? 0f : value;
    }

    private static String orDefault(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private static String format(LocalDateTime value) {
        return value == null ? "" : GRID_DATE.format(value);
    }

    /**
     * Parses a filter date. ISO ({@code yyyy-MM-dd}) is the contract;
     * {@code dd/MM/yyyy} is accepted because that is what the screen shows.
     * Ambiguous US-style input is deliberately not guessed at — a wrong guess
     * silently returns the wrong month of bills.
     */
    static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String text = value.trim();
        if (text.length() > 10 && text.charAt(10) == 'T') {
            text = text.substring(0, 10);
        }
        try {
            return LocalDate.parse(text);
        } catch (Exception ignored) {
            return LocalDate.parse(text, GRID_DATE);
        }
    }

    private static LocalDateTime startOfDay(String value) {
        LocalDate date = parseDate(value);
        return date == null ? LocalDate.of(1900, 1, 1).atStartOfDay() : date.atStartOfDay();
    }

    /** Inclusive upper bound — a bill saved at 16:20 still falls inside its own day. */
    private static LocalDateTime endOfDay(String value) {
        LocalDate date = parseDate(value);
        return date == null
                ? LocalDate.of(2999, 12, 31).atTime(23, 59, 59)
                : date.atTime(23, 59, 59);
    }
}
