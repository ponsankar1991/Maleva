package my.maleva.api.module.patmentvouchmaster.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.module.billing.billorder.entity.BillsOrderMaster;
import my.maleva.api.module.billing.billorder.repository.BillsOrderMasterRepository;
import my.maleva.api.module.employee.repository.EmployeeMasterRepository;
import my.maleva.api.module.master.entity.SequenceNoMaster;
import my.maleva.api.module.master.repository.SequenceNoMasterRepository;
import my.maleva.api.module.patmentvouchmaster.dto.PaymentVoucherDetailViewDto;
import my.maleva.api.module.patmentvouchmaster.dto.PaymentVoucherEditDto;
import my.maleva.api.module.patmentvouchmaster.dto.PaymentVoucherEditLineDto;
import my.maleva.api.module.patmentvouchmaster.dto.PaymentVoucherF5ViewDto;
import my.maleva.api.module.patmentvouchmaster.dto.PaymentVoucherMasterViewDto;
import my.maleva.api.module.patmentvouchmaster.dto.PaymentVoucherSaveLineDto;
import my.maleva.api.module.patmentvouchmaster.dto.PaymentVoucherSaveRequestDto;
import my.maleva.api.module.patmentvouchmaster.dto.PaymentVoucherSaveResponseDto;
import my.maleva.api.module.patmentvouchmaster.dto.SelectPaymentVoucherRequestDto;
import my.maleva.api.module.patmentvouchmaster.entity.PaymentVoucherDetails;
import my.maleva.api.module.patmentvouchmaster.entity.PaymentVoucherMaster;
import my.maleva.api.module.patmentvouchmaster.repository.PaymentVoucherDetailsRepository;
import my.maleva.api.module.patmentvouchmaster.repository.PaymentVoucherMasterRepository;
import my.maleva.api.module.user.repository.AppUserRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The Payment Voucher screen's transactional operations — the Java port of
 * legacy {@code PaymentVoucherServices} (Services/Transaction/…), with
 * {@code SP_PaymentVoucherMaster} reimplemented here rather than called,
 * matching how the sibling payment and bill modules were migrated.
 *
 * <p>The SP took a JSON blob built by string concatenation, which is why the
 * legacy service stripped every apostrophe out of user-typed descriptions
 * before saving; every statement below is parameterised instead.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentVoucherTransactionService {

    private static final String SEQUENCE_NAME = "PaymentVoucher";
    private static final DateTimeFormatter GRID_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * How long two identical vouchers are treated as one double-click rather
     * than two real vouchers.
     */
    private static final Duration DUPLICATE_WINDOW = Duration.ofMinutes(2);

    private final PaymentVoucherMasterRepository vouchers;
    private final PaymentVoucherDetailsRepository voucherDetails;
    private final AppUserRepository appUsers;
    private final EmployeeMasterRepository employees;
    private final SequenceNoMasterRepository sequences;
    private final BillsOrderMasterRepository billsOrders;
    private final NamedParameterJdbcTemplate jdbc;

    /* ── document number ───────────────────────────────────────────── */

    /**
     * The number the next voucher will get, for display on a blank screen.
     * Preview only — the real number is assigned inside {@link #save}.
     */
    @Transactional(readOnly = true)
    public String nextVoucherNumber(Integer companyId) {
        return formatVoucherNumber(nextSequence(companyId));
    }

    private int nextSequence(Integer companyId) {
        Integer max = sequences.findMaxSequenceNoByCompanyAndSequenceName(companyId, SEQUENCE_NAME);
        return (max == null || max == 0) ? 1 : max + 1;
    }

    /** Legacy format: {@code PV} followed by a 9-digit running number. */
    static String formatVoucherNumber(int sequence) {
        return String.format("PV%09d", sequence);
    }

    /* ── save ──────────────────────────────────────────────────────── */

    /**
     * Insert or update one voucher with its expense lines.
     *
     * <p>Semantics pinned by {@code SP_PaymentVoucherMaster}:
     * <ul>
     *   <li>Id 0/null inserts and assigns the number; the UPDATE branch has
     *       CNumber/CNumberDisplay commented out, so an edit never renumbers.</li>
     *   <li>The UPDATE writes {@code LastEmployeeRefId} but deliberately not
     *       {@code EmployeeRefId} — the original enterer survives someone
     *       else's edit (the same trap already found on Payment).</li>
     *   <li>Lines are replaced wholesale; a line with a {@code SubExpenseRefid}
     *       claims the earliest unclaimed PendingPayment slot due this month.</li>
     *   <li>On INSERT only, a voucher raised from a purchase order flags that
     *       order {@code PStatus=1, BillStatus='PAYMENT COMPLETED'}.</li>
     * </ul>
     */
    @Transactional(rollbackFor = Exception.class)
    public PaymentVoucherSaveResponseDto save(PaymentVoucherSaveRequestDto dto, Integer companyId) {
        if (dto.getPaymentById() == null || dto.getPaymentById() == 0) {
            return failure("Select the paying bank");
        }
        if (isBlank(dto.getDescription())) {
            return failure("Select a description");
        }

        List<PaymentVoucherSaveLineDto> lines = new ArrayList<>();
        for (PaymentVoucherSaveLineDto line : dto.getPaymentVoucherDetails() == null
                ? List.<PaymentVoucherSaveLineDto>of() : dto.getPaymentVoucherDetails()) {
            if (line != null && zeroToNull(line.getAccountGroupRefId()) != null) {
                lines.add(line);
            }
        }
        if (lines.isEmpty()) {
            return failure("Enter at least one expense line with an account");
        }

        // The two reference checks SP_PaymentVoucherMaster ran: each applies
        // only when the id is set, and each requires the reference to belong
        // to this company and still be active.
        //
        // The wording is the SP's, but its version could never actually be
        // read: `'…Issue id' + @UserRefId` concatenates varchar to int, which
        // SQL Server answers with "Conversion failed…" (verified live). So the
        // clerk got a type error instead of the reason. Same shape as the
        // LeviEntry conversion errors already on record.
        Integer userRefId = zeroToNull(dto.getUserRefId());
        if (userRefId != null
                && !appUsers.existsByIdAndCompanyRefIdAndActive(userRefId, companyId, 1)) {
            return failure("Login User Not Found Issue id" + userRefId);
        }
        Integer employeeRefId = zeroToNull(dto.getEmployeeRefId());
        if (employeeRefId != null
                && !employees.existsByIdAndCompanyRefIdAndActive(employeeRefId, companyId, 1)) {
            return failure("Employee Not Found Issue id" + employeeRefId);
        }

        LocalDateTime voucherDate = parseDateTime(dto.getPaymentVoucherDate());
        if (voucherDate == null) {
            return failure("Enter a valid voucher date");
        }

        boolean isNew = dto.getId() == null || dto.getId() == 0;
        LocalDateTime now = LocalDateTime.now();

        if (isNew) {
            // A double-click, retry or second tab must not raise the voucher
            // twice — the legacy screen's guard was browser-side only.
            if (!lockSaveIntent(dto, companyId, voucherDate)) {
                return failure("This voucher is already being saved — please wait a moment");
            }
            PaymentVoucherMaster duplicate = findDuplicate(dto, companyId, voucherDate, now);
            if (duplicate != null) {
                log.info("Duplicate save for voucher {} ignored; returning the existing voucher",
                        duplicate.getCNumberDisplay());
                return PaymentVoucherSaveResponseDto.builder()
                        .success(true)
                        .message("This voucher was already saved as " + duplicate.getCNumberDisplay())
                        .id(duplicate.getId())
                        .voucherNoDisplay(duplicate.getCNumberDisplay())
                        .duplicate(true)
                        .build();
            }
        }

        PaymentVoucherMaster voucher;
        if (isNew) {
            voucher = new PaymentVoucherMaster();
            voucher.setCompanyRefId(companyId);
            voucher.setActive(1);
            voucher.setCreatedDate(now);
            voucher.setCreatedBy(actor(dto));
            voucher.setModifiedBy(actor(dto));
            voucher.setCNumber(0);
            voucher.setCNumberDisplay("");
            // Insert only — the SP writes EmployeeRefId once and never again.
            voucher.setEmployeeRefId(employeeRefId);
        } else {
            voucher = vouchers.findById(dto.getId())
                    .filter(v -> Objects.equals(v.getCompanyRefId(), companyId))
                    .orElse(null);
            if (voucher == null) {
                return failure("Payment voucher not found: " + dto.getId());
            }
            // The SP's UPDATE branch sets Modified_Date but, unlike its
            // INSERT, not Modified_By — preserved as found.
        }

        voucher.setUserRefId(userRefId);
        voucher.setLastEmployeeRefId(employeeRefId);
        voucher.setPayTo(orEmpty(dto.getPayTo()));
        // Nullable, and left that way: 6,142 live rows are NULL against 2,600
        // empty strings, and the PayFrom combo filters out both.
        voucher.setPayFrom(dto.getPayFrom());
        voucher.setPaymentById(dto.getPaymentById());
        voucher.setPaymentVoucherDate(voucherDate);
        voucher.setDescription(dto.getDescription());
        voucher.setRefNo(orEmpty(dto.getRefNo()));
        voucher.setPaymentStatus(orDefault(dto.getPaymentStatus(), "SEND FOR APPROVAL"));
        voucher.setAmount(dto.getAmount() == null ? BigDecimal.ZERO : dto.getAmount());
        voucher.setBankCharges(f(dto.getBankCharges()));
        voucher.setCurrencyValue(f(dto.getCurrencyValue()));
        voucher.setActualAmount(f(dto.getActualAmount()));
        // Stored as sent, 0 included: the SP's null-conversion block covers
        // UserRefId, EmployeeRefId and BillsOrderMasterRefId — deliberately
        // not Selectedpaytoid. Live rows bear that out: 4,903 carry 0 and
        // 3,844 NULL, both meaning "no receipt matched", so anything reading
        // this column has to treat the two the same. Only 4 rows are real.
        voucher.setPaymentReceiptid(dto.getSelectedPaytoid());
        voucher.setBillsOrderMasterRefId(zeroToNull(dto.getBillsOrderMasterRefId()));
        voucher.setModifiedDate(now);

        voucher = vouchers.save(voucher);

        if (isNew) {
            int sequence = nextSequence(companyId);
            voucher.setCNumber(sequence);
            voucher.setCNumberDisplay(formatVoucherNumber(sequence));
            voucher = vouchers.save(voucher);
            recordSequence(companyId, sequence);
            markPurchaseOrderPaid(voucher.getBillsOrderMasterRefId());
        }

        applyLines(voucher, lines, now);

        log.info("Payment voucher {} saved as {} ({} line(s))",
                voucher.getId(), voucher.getCNumberDisplay(), lines.size());
        return PaymentVoucherSaveResponseDto.builder()
                .success(true)
                .message(isNew ? "Payment voucher created successfully"
                        : "Payment voucher updated successfully")
                .id(voucher.getId())
                .voucherNoDisplay(voucher.getCNumberDisplay())
                .build();
    }

    /**
     * Replaces the voucher's lines with the posted ones, claiming pending
     * payments as it goes.
     *
     * <p>Lines are inserted one at a time with a flush between, so each claim
     * sees the previous line's — the SP did it in a single INSERT..SELECT
     * where two lines with the same sub-expense could claim the same slot.
     */
    private void applyLines(PaymentVoucherMaster voucher,
                            List<PaymentVoucherSaveLineDto> lines, LocalDateTime now) {
        List<PaymentVoucherDetails> existing =
                voucherDetails.findByPaymentVoucherMasterRefIdOrderByIdAsc(voucher.getId());
        if (!existing.isEmpty()) {
            voucherDetails.deleteAll(existing);
            voucherDetails.flush();
        }

        for (PaymentVoucherSaveLineDto line : lines) {
            PaymentVoucherDetails row = new PaymentVoucherDetails();
            row.setPaymentVoucherMasterRefId(voucher.getId());
            row.setAccountGroupRefId(line.getAccountGroupRefId());
            row.setDescription(line.getDescription());
            row.setAmount(f(line.getAmount()));
            row.setCurrencyValue(f(line.getCurrencyValue()));
            row.setActualAmount(f(line.getActualAmount()));
            // The SP's detail CASE nulls only SubExpenseRefid; Classification
            // is stored raw, 0 included — 156 live lines carry 0 and none are
            // NULL, and every reader LEFT JOINs it, so 0 stays 0.
            row.setClassification(line.getClassification());
            Integer subExpense = zeroToNull(line.getSubExpenseRefid());
            row.setSubExpenseRefid(subExpense);
            row.setPendingPaymentRefId(subExpense == null ? null : claimPendingPayment(subExpense));
            row.setCreatedDate(now);
            row.setModifiedDate(now);
            voucherDetails.saveAndFlush(row);
        }
    }

    /**
     * The earliest PendingPayment slot for this sub-expense that is due in the
     * current month and not yet claimed by any voucher line — the SP's exact
     * subquery, bound instead of concatenated. Null when nothing is waiting.
     */
    private Integer claimPendingPayment(Integer subExpenseRefid) {
        List<Integer> ids = jdbc.queryForList(
                "SELECT TOP 1 Id FROM PendingPayment WITH(NOLOCK) "
                        + "WHERE SubExpenseRefId = :subExpense "
                        + "AND Id NOT IN (SELECT ISNULL(PendingPaymentRefId, 0) "
                        + "               FROM PaymentVoucherDetails WITH(NOLOCK)) "
                        + "AND MONTH(DueDate) = MONTH(GETDATE()) "
                        + "AND YEAR(DueDate) = YEAR(GETDATE()) "
                        + "ORDER BY DueDate ASC",
                new MapSqlParameterSource("subExpense", subExpenseRefid),
                Integer.class);
        return ids.isEmpty() ? null : ids.get(0);
    }

    /**
     * Flags the purchase order this voucher settles. SP behaviour: INSERT
     * branch only — editing a voucher never re-flags or un-flags an order.
     */
    private void markPurchaseOrderPaid(Integer billsOrderMasterRefId) {
        if (billsOrderMasterRefId == null) {
            return;
        }
        BillsOrderMaster order = billsOrders.findById(billsOrderMasterRefId).orElse(null);
        if (order == null) {
            log.warn("Voucher references purchase order {} which no longer exists — not flagged",
                    billsOrderMasterRefId);
            return;
        }
        order.setPStatus(1);
        order.setBillStatus("PAYMENT COMPLETED");
        order.setModifiedDate(LocalDateTime.now());
        order.setModifiedBy("From PaymentVoucher");
        billsOrders.save(order);
        log.info("Purchase order {} flagged as paid by voucher", billsOrderMasterRefId);
    }

    /**
     * Records the number just handed out, creating the counter row when it is
     * missing — the SP only ever UPDATEd it, so a company with no
     * {@code PaymentVoucher} row silently numbered every voucher 1.
     */
    private void recordSequence(Integer companyId, int sequence) {
        SequenceNoMaster counter = sequences
                .findByCompanyRefIdAndSequenceName(companyId, SEQUENCE_NAME)
                .orElseGet(() -> {
                    SequenceNoMaster fresh = new SequenceNoMaster();
                    fresh.setCompanyRefId(companyId);
                    fresh.setSequenceName(SEQUENCE_NAME);
                    fresh.setSequenceDate(LocalDateTime.now());
                    return fresh;
                });
        counter.setSequenceNo(sequence);
        sequences.save(counter);
    }

    private boolean lockSaveIntent(PaymentVoucherSaveRequestDto dto, Integer companyId,
                                   LocalDateTime voucherDate) {
        String key = "PaymentVoucherSave:" + companyId
                + ":payTo:" + trimmed(dto.getPayTo())
                + "|bank:" + dto.getPaymentById()
                + "|date:" + voucherDate.toLocalDate()
                + "|amt:" + (dto.getAmount() == null ? "0" : dto.getAmount().toPlainString())
                + "|ref:" + trimmed(dto.getRefNo());
        try {
            Integer status = jdbc.queryForObject(
                    "DECLARE @status int; "
                            + "EXEC @status = sp_getapplock @Resource = :key, "
                            + "@LockMode = 'Exclusive', @LockOwner = 'Transaction', "
                            + "@LockTimeout = 15000; SELECT @status",
                    new MapSqlParameterSource("key", key),
                    Integer.class);
            return status == null || status >= 0;
        } catch (Exception ex) {
            // Never let the guard itself block a save.
            log.warn("Could not take the duplicate-save lock ({}); continuing unguarded",
                    ex.getMessage());
            return true;
        }
    }

    private PaymentVoucherMaster findDuplicate(PaymentVoucherSaveRequestDto dto, Integer companyId,
                                               LocalDateTime voucherDate, LocalDateTime now) {
        return vouchers.findRecentlyEnteredLikeThis(
                        companyId, orEmpty(dto.getPayTo()), dto.getPaymentById(), voucherDate,
                        dto.getAmount() == null ? BigDecimal.ZERO : dto.getAmount(),
                        trimmed(dto.getRefNo()), now.minus(DUPLICATE_WINDOW))
                .stream().findFirst().orElse(null);
    }

    /* ── delete / status ───────────────────────────────────────────── */

    /**
     * Soft-deletes a voucher ({@code Active=2}), as legacy did.
     *
     * <p>A voucher already in QNE is refused: deleting it locally would leave
     * QNE holding a voucher this system no longer has.
     */
    @Transactional
    public boolean delete(Integer id, Integer companyId) {
        PaymentVoucherMaster voucher = vouchers.findById(id)
                .filter(v -> Objects.equals(v.getCompanyRefId(), companyId))
                .orElse(null);
        if (voucher == null) {
            return false;
        }
        if (!isBlank(voucher.getQneCode())) {
            throw new IllegalStateException(
                    "Not able to delete — this voucher is already in QNE as "
                            + voucher.getQneCode() + ". Cancel it in QNE first.");
        }
        voucher.setActive(2);
        voucher.setModifiedDate(LocalDateTime.now());
        vouchers.save(voucher);
        log.info("Payment voucher {} soft-deleted", id);
        return true;
    }

    /** Marks a voucher as cleared — legacy {@code UpdatePaymentVoucherStatus}. */
    @Transactional
    public boolean markCompleted(Integer id, Integer companyId) {
        PaymentVoucherMaster voucher = vouchers.findById(id)
                .filter(v -> Objects.equals(v.getCompanyRefId(), companyId))
                .orElse(null);
        if (voucher == null) {
            return false;
        }
        voucher.setPaymentStatus("COMPLETED");
        voucher.setModifiedDate(LocalDateTime.now());
        vouchers.save(voucher);
        return true;
    }

    /* ── edit ──────────────────────────────────────────────────────── */

    /**
     * Loads one voucher back into the screen, by id or by the running number
     * the clerk typed.
     *
     * <p>As in legacy, there is no Active filter here: the F5 list only shows
     * live vouchers, but a number typed by hand loads even a deleted one.
     */
    @Transactional(readOnly = true)
    public Optional<PaymentVoucherEditDto> edit(Integer id, Integer voucherNumber, Integer companyId) {
        PaymentVoucherMaster voucher = null;
        if (voucherNumber != null && voucherNumber != 0) {
            voucher = vouchers.findByCompanyRefIdAndCNumber(companyId, voucherNumber).orElse(null);
        } else if (id != null && id != 0) {
            voucher = vouchers.findById(id)
                    .filter(v -> Objects.equals(v.getCompanyRefId(), companyId))
                    .orElse(null);
        }
        if (voucher == null) {
            return Optional.empty();
        }

        List<PaymentVoucherEditLineDto> lines = jdbc.query(
                "SELECT B.Id, B.AccountGroupRefId, ISNULL(B.Description,'') AS Description, "
                        + "B.Amount, B.SubExpenseRefid, B.PendingPaymentRefId, B.Classification, "
                        + "ISNULL(AG.GLAccountCode,'') AS AccountCode, "
                        + "ISNULL(AG.Description,'') AS AccountName, "
                        + "ISNULL(C.Description,'') AS ClassificationName "
                        + "FROM PaymentVoucherDetails B WITH(NOLOCK) "
                        + "LEFT JOIN GLAccounts AG WITH(NOLOCK) ON AG.RowIndex = B.AccountGroupRefId "
                        + "LEFT JOIN Classification C WITH(NOLOCK) ON C.Id = B.Classification "
                        + "WHERE B.PaymentVoucherMasterRefId = :id ORDER BY B.Id",
                new MapSqlParameterSource("id", voucher.getId()),
                (rs, i) -> PaymentVoucherEditLineDto.builder()
                        .id(rs.getInt("Id"))
                        .accountGroupRefId(nullableInt(rs, "AccountGroupRefId"))
                        .accountCode(rs.getString("AccountCode"))
                        .accountName(rs.getString("AccountName"))
                        .description(rs.getString("Description"))
                        .amount(decimal(rs, "Amount"))
                        .subExpenseRefid(nullableInt(rs, "SubExpenseRefid"))
                        .pendingPaymentRefId(nullableInt(rs, "PendingPaymentRefId"))
                        .classification(nullableInt(rs, "Classification"))
                        .classificationName(rs.getString("ClassificationName"))
                        .build());

        return Optional.of(PaymentVoucherEditDto.builder()
                .id(voucher.getId())
                .companyRefId(voucher.getCompanyRefId())
                .userRefId(voucher.getUserRefId())
                .employeeRefId(voucher.getEmployeeRefId())
                .cNumber(voucher.getCNumber())
                .cNumberDisplay(voucher.getCNumberDisplay())
                .paymentById(voucher.getPaymentById())
                .paymentVoucherDate(voucher.getPaymentVoucherDate())
                .sPaymentVoucherDate(voucher.getPaymentVoucherDate() == null
                        ? "" : GRID_DATE.format(voucher.getPaymentVoucherDate()))
                .payTo(voucher.getPayTo())
                .payFrom(voucher.getPayFrom())
                .description(voucher.getDescription())
                .refNo(voucher.getRefNo())
                .paymentStatus(voucher.getPaymentStatus())
                .amount(voucher.getAmount())
                .bankCharges(toDecimal(voucher.getBankCharges()))
                .billsOrderMasterRefId(voucher.getBillsOrderMasterRefId())
                .selectedPaytoid(voucher.getPaymentReceiptid())
                .qneCode(voucher.getQneCode())
                .qneId(voucher.getQneId())
                .paymentVoucherDetails(lines)
                .build());
    }

    /* ── F5 search ─────────────────────────────────────────────────── */

    /** The voucher F5 grid: matching vouchers plus all of their lines. */
    @Transactional(readOnly = true)
    public PaymentVoucherF5ViewDto search(SelectPaymentVoucherRequestDto request, Integer companyId) {
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("comid", companyId);
        StringBuilder where = new StringBuilder();

        boolean hasSearch = request.getSearch() != null && !request.getSearch().isBlank();
        if (hasSearch) {
            // Legacy reset the WHERE clause here, so the number is the ONLY filter.
            where.append(" AND A.CNumberDisplay = :search ");
            params.addValue("search", request.getSearch().trim());
        } else {
            if (request.getEmployeeId() != null && request.getEmployeeId() != 0) {
                where.append(" AND A.EmployeeRefId = :employeeId ");
                params.addValue("employeeId", request.getEmployeeId());
            }
            if (request.getPaymentStatus() != null && !request.getPaymentStatus().isBlank()) {
                where.append(" AND A.PaymentStatus = :paymentStatus ");
                params.addValue("paymentStatus", request.getPaymentStatus().trim());
            }
            if (request.getPayTo() != null && !request.getPayTo().isBlank()) {
                where.append(" AND A.PayTo = :payTo ");
                params.addValue("payTo", request.getPayTo().trim());
            }
            if (request.getDescription() != null && !request.getDescription().isBlank()) {
                where.append(" AND A.Description = :description ");
                params.addValue("description", request.getDescription().trim());
            }
            where.append(" AND A.PaymentVoucherDate BETWEEN :fromDate AND :toDate ");
            params.addValue("fromDate", startOfDay(request.getFromDate()));
            params.addValue("toDate", endOfDay(request.getToDate()));
        }

        String masterSql = "SELECT A.Id, A.CNumber, A.CNumberDisplay, "
                + "FORMAT(ISNULL(A.PaymentVoucherDate,'1900-01-01'),'dd/MM/yyyy') AS SPaymentVoucherDate, "
                + "ISNULL(em.EmployeeName,'') AS EmployeeName, ISNULL(AG.Name,'') AS PaymentByIdName, "
                + "ISNULL(A.PayTo,'') AS PayTo, ISNULL(A.Description,'') AS Description, "
                + "ISNULL(A.RefNo,'') AS RefNo, ISNULL(A.PaymentStatus,'') AS PaymentStatus, "
                + "A.Amount, ISNULL(A.QNECode,'') AS QNECode, ISNULL(A.QNEId,'') AS QNEId, "
                + "ISNULL(A.EInvoiceUid,'') AS EInvoiceUid "
                + "FROM PaymentVoucherMaster A WITH(NOLOCK) "
                + "LEFT JOIN BankMaster AG WITH(NOLOCK) ON AG.Id = A.PaymentById "
                + "LEFT JOIN EmployeeMaster em WITH(NOLOCK) ON em.Id = A.EmployeeRefId "
                + "WHERE A.CompanyRefId = :comid AND A.Active = 1" + where
                + " ORDER BY A.PaymentVoucherDate, A.Id";

        List<PaymentVoucherMasterViewDto> headers = jdbc.query(masterSql, params, this::mapHeader);

        String detailSql = "SELECT B.Id, B.PaymentVoucherMasterRefId, B.AccountGroupRefId, "
                + "ISNULL(B.Description,'') AS Description, B.Amount, "
                + "ISNULL(AG.GLAccountCode,'') AS AccountCode, "
                + "ISNULL(AG.Description,'') AS AccountName "
                + "FROM PaymentVoucherDetails B WITH(NOLOCK) "
                + "INNER JOIN PaymentVoucherMaster A WITH(NOLOCK) ON A.Id = B.PaymentVoucherMasterRefId "
                + "LEFT JOIN GLAccounts AG WITH(NOLOCK) ON AG.RowIndex = B.AccountGroupRefId "
                + "WHERE A.CompanyRefId = :comid AND A.Active = 1" + where;

        List<PaymentVoucherDetailViewDto> lines = jdbc.query(detailSql, params, (rs, i) ->
                PaymentVoucherDetailViewDto.builder()
                        .id(rs.getInt("Id"))
                        .paymentVoucherMasterRefId(rs.getInt("PaymentVoucherMasterRefId"))
                        .accountGroupRefId(nullableInt(rs, "AccountGroupRefId"))
                        .accountCode(rs.getString("AccountCode"))
                        .accountName(rs.getString("AccountName"))
                        .description(rs.getString("Description"))
                        .amount(decimal(rs, "Amount"))
                        .build());

        BigDecimal total = jdbc.queryForObject(
                "SELECT ISNULL(SUM(A.Amount), 0) FROM PaymentVoucherMaster A WITH(NOLOCK) "
                        + "LEFT JOIN BankMaster AG WITH(NOLOCK) ON AG.Id = A.PaymentById "
                        + "LEFT JOIN EmployeeMaster em WITH(NOLOCK) ON em.Id = A.EmployeeRefId "
                        + "WHERE A.CompanyRefId = :comid AND A.Active = 1" + where,
                params, BigDecimal.class);

        return PaymentVoucherF5ViewDto.builder()
                .paymentVoucherMaster(headers)
                .paymentVoucherDetails(lines)
                .totalAmount(total == null ? BigDecimal.ZERO : total)
                .count(headers.size())
                .build();
    }

    /** Descriptions used on earlier vouchers, for the description dropdown. */
    @Transactional(readOnly = true)
    public List<String> descriptions(Integer companyId) {
        return jdbc.queryForList(
                "SELECT DISTINCT LTRIM(RTRIM(A.Description)) FROM PaymentVoucherMaster A WITH(NOLOCK) "
                        + "WHERE A.Description IS NOT NULL AND LTRIM(RTRIM(A.Description)) <> '' "
                        + "AND A.Active <> 2 AND A.CompanyRefId = :comid ORDER BY 1",
                new MapSqlParameterSource("comid", companyId),
                String.class);
    }

    private PaymentVoucherMasterViewDto mapHeader(ResultSet rs, int rowNum) throws SQLException {
        return PaymentVoucherMasterViewDto.builder()
                .id(rs.getInt("Id"))
                .cNumber(rs.getInt("CNumber"))
                .cNumberDisplay(rs.getString("CNumberDisplay"))
                .sPaymentVoucherDate(rs.getString("SPaymentVoucherDate"))
                .employeeName(rs.getString("EmployeeName"))
                .paymentByIdName(rs.getString("PaymentByIdName"))
                .payTo(rs.getString("PayTo"))
                .description(rs.getString("Description"))
                .refNo(rs.getString("RefNo"))
                .paymentStatus(rs.getString("PaymentStatus"))
                .amount(decimal(rs, "Amount"))
                .qneCode(rs.getString("QNECode"))
                .qneId(rs.getString("QNEId"))
                .eInvoiceUid(rs.getString("EInvoiceUid"))
                .build();
    }

    /* ── helpers ───────────────────────────────────────────────────── */

    private static PaymentVoucherSaveResponseDto failure(String message) {
        return PaymentVoucherSaveResponseDto.builder().success(false).message(message).build();
    }

    private static Integer zeroToNull(Integer value) {
        return (value == null || value == 0) ? null : value;
    }

    private static Float f(BigDecimal value) {
        return value == null ? 0f : value.floatValue();
    }

    private static BigDecimal toDecimal(Float value) {
        return value == null ? BigDecimal.ZERO : BigDecimal.valueOf(value.doubleValue());
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String trimmed(String value) {
        return value == null ? "" : value.trim();
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String orDefault(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    /** Who to stamp on the row — the employee logged into the screen. */
    private static String actor(PaymentVoucherSaveRequestDto dto) {
        return dto.getEmployeeRefId() == null || dto.getEmployeeRefId() == 0
                ? "SYSTEM" : String.valueOf(dto.getEmployeeRefId());
    }

    private static Integer nullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private static BigDecimal decimal(ResultSet rs, String column) throws SQLException {
        BigDecimal value = rs.getBigDecimal(column);
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * ISO ({@code yyyy-MM-dd}) is the contract; {@code dd/MM/yyyy} is accepted
     * because that is what the screen shows. Ambiguous US-style input is
     * deliberately not guessed at.
     */
    static LocalDate parseDate(String value) {
        if (isBlank(value)) {
            return null;
        }
        String text = value.trim();
        if (text.length() > 10 && (text.charAt(10) == 'T' || text.charAt(10) == ' ')) {
            text = text.substring(0, 10);
        }
        try {
            return LocalDate.parse(text);
        } catch (Exception ignored) {
            return LocalDate.parse(text, GRID_DATE);
        }
    }

    private static LocalDateTime parseDateTime(String value) {
        LocalDate date = parseDate(value);
        return date == null ? null : date.atStartOfDay();
    }

    private static LocalDateTime startOfDay(String value) {
        LocalDate date = parseDate(value);
        return date == null ? LocalDate.of(1900, 1, 1).atStartOfDay() : date.atStartOfDay();
    }

    private static LocalDateTime endOfDay(String value) {
        LocalDate date = parseDate(value);
        return date == null
                ? LocalDate.of(2999, 12, 31).atTime(23, 59, 59)
                : date.atTime(23, 59, 59);
    }
}
