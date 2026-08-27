package my.maleva.api.module.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.module.billing.billorder.entity.BillsOrderMaster;
import my.maleva.api.module.billing.billorder.repository.BillsOrderMasterRepository;
import my.maleva.api.module.employee.repository.EmployeeMasterRepository;
import my.maleva.api.module.master.entity.SequenceNoMaster;
import my.maleva.api.module.master.repository.BankMasterRepository;
import my.maleva.api.module.master.repository.SequenceNoMasterRepository;
import my.maleva.api.module.payment.dto.PaymentDetailViewDto;
import my.maleva.api.module.payment.dto.PaymentEditDto;
import my.maleva.api.module.payment.dto.PaymentF5ViewDto;
import my.maleva.api.module.payment.dto.PaymentMasterViewDto;
import my.maleva.api.module.payment.dto.PaymentSaveDetailDto;
import my.maleva.api.module.payment.dto.PaymentSaveRequestDto;
import my.maleva.api.module.payment.dto.PaymentSaveResponseDto;
import my.maleva.api.module.payment.dto.SelectPaymentRequestDto;
import my.maleva.api.module.payment.dto.SupplierBalanceDto;
import my.maleva.api.module.payment.dto.SupplierBillDto;
import my.maleva.api.module.payment.entity.Payment;
import my.maleva.api.module.payment.entity.PaymentDetails;
import my.maleva.api.module.payment.repository.PaymentDetailsRepository;
import my.maleva.api.module.payment.repository.PaymentRepository;
import my.maleva.api.module.supplier.repository.SupplierRepository;
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
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * The Pay Bills screen's transactional operations — the Java port of legacy
 * {@code PaymentServices} (Services/Transaction/PaymentServices.cs).
 *
 * <p>{@code SP_Payment} is reimplemented here rather than called, matching how
 * the sibling bill module was migrated. The SP took a JSON blob built by string
 * concatenation, which is why the legacy service had to strip every apostrophe
 * out of user-typed remarks before saving; every statement below is
 * parameterised instead.
 *
 * <p>Two other database objects are ported with it: {@code RT_SupplierBills}
 * (the outstanding-documents grid) and {@code SupplierBalance_Single} /
 * {@code SupplierBalance} (the running balance shown beside the supplier).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentTransactionService {

    private static final String SEQUENCE_NAME = "Payment";

    /**
     * First entry of the screen's status list, which its dropdown selects on
     * open — and what 3,102 of the 3,231 live payments carry.
     */
    private static final String DEFAULT_PAYMENT_STATUS = "SEND FOR APPROVAL";
    private static final DateTimeFormatter GRID_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * How long two identical payments are treated as one double-click rather
     * than two real payments.
     */
    private static final Duration DUPLICATE_WINDOW = Duration.ofMinutes(2);

    private final PaymentRepository payments;
    private final PaymentDetailsRepository paymentDetails;
    private final SupplierRepository suppliers;
    private final BankMasterRepository banks;
    private final AppUserRepository appUsers;
    private final EmployeeMasterRepository employees;
    private final SequenceNoMasterRepository sequences;
    private final BillsOrderMasterRepository billsOrders;
    private final NamedParameterJdbcTemplate jdbc;

    /* ── document number ───────────────────────────────────────────── */

    /**
     * The number the next payment will get, for display on a blank screen.
     *
     * <p>Preview only — the number is assigned for real inside {@link #save},
     * so two clerks with the screen open do not both save as the same payment.
     */
    @Transactional(readOnly = true)
    public String nextPaymentNumber(Integer companyId) {
        return formatPaymentNumber(nextSequence(companyId));
    }

    private int nextSequence(Integer companyId) {
        Integer max = sequences.findMaxSequenceNoByCompanyAndSequenceName(companyId, SEQUENCE_NAME);
        return (max == null || max == 0) ? 1 : max + 1;
    }

    /** Legacy format: {@code PY} followed by a 9-digit running number. */
    static String formatPaymentNumber(int sequence) {
        return String.format("PY%09d", sequence);
    }

    /* ── save ──────────────────────────────────────────────────────── */

    /**
     * Insert or update one payment with the documents it settles.
     *
     * <p>Id 0/null inserts and assigns the document number; otherwise the
     * existing payment is updated and keeps its number — SP_Payment's UPDATE
     * branch had {@code CNumber}/{@code CNumberDisplay} commented out, and
     * re-pointing them on edit would renumber a payment that has already been
     * quoted to the supplier.
     *
     * <p>Lines are replaced wholesale, as the SP did: the posted grid is the
     * only source of truth for what this payment settles.
     */
    @Transactional(rollbackFor = Exception.class)
    public PaymentSaveResponseDto save(PaymentSaveRequestDto dto, Integer companyId) {
        if (dto.getSupplierRefId() == null || dto.getSupplierRefId() == 0) {
            return failure("Please select a supplier");
        }
        if (!suppliers.existsById(dto.getSupplierRefId())) {
            return failure("Supplier not found: " + dto.getSupplierRefId());
        }
        if (dto.getBankRefId() == null || dto.getBankRefId() == 0) {
            return failure("Please select Payment By");
        }

        // Only the documents the clerk actually put money against are stored;
        // the grid posts every outstanding bill, most of them with a zero.
        List<PaymentSaveDetailDto> lines = new ArrayList<>();
        for (PaymentSaveDetailDto line : safe(dto.getPaymentDetails())) {
            if (line != null && signum(line.getAmount()) != 0) {
                lines.add(line);
            }
        }
        if (lines.isEmpty()) {
            return failure("Enter an amount against at least one bill");
        }
        for (PaymentSaveDetailDto line : lines) {
            if (zeroToNull(line.getBillMasterRefId()) == null
                    && zeroToNull(line.getPurchaseMasterRefId()) == null
                    && zeroToNull(line.getSupplieropenRefId()) == null) {
                return failure("Every paid line must reference a bill, a purchase order "
                        + "or the supplier's opening balance");
            }
        }

        // The three reference checks SP_Payment ran before touching a row. Each
        // only applies when the id is actually set, and each requires the
        // reference to belong to this company and still be active.
        String reference = checkReferences(dto, companyId);
        if (reference != null) {
            return failure(reference);
        }

        LocalDateTime paymentDate = parseDateTime(dto.getPaymentDate());
        if (paymentDate == null) {
            return failure("Enter a valid payment date");
        }

        boolean isNew = dto.getId() == null || dto.getId() == 0;
        LocalDateTime now = LocalDateTime.now();

        if (isNew) {
            // A clerk clicking Save twice on a slow connection sends two
            // identical inserts, and this screen moves money — the browser
            // guard the legacy screen used cannot stop a reload, a retry or a
            // second tab, so the rule is enforced here.
            //
            // The lock serialises the copies; whichever gets in first creates
            // the payment and the rest then see it and answer with the same
            // number instead of paying the supplier twice.
            if (!lockSaveIntent(dto, companyId, paymentDate)) {
                return failure("This payment is already being saved — please wait a moment");
            }
            Payment duplicate = findDuplicate(dto, companyId, paymentDate, now);
            if (duplicate != null) {
                log.info("Duplicate save for payment {} ignored; returning the existing payment",
                        duplicate.getCNumberDisplay());
                return PaymentSaveResponseDto.builder()
                        .success(true)
                        .message("This payment was already saved as " + duplicate.getCNumberDisplay())
                        .id(duplicate.getId())
                        .paymentNoDisplay(duplicate.getCNumberDisplay())
                        .duplicate(true)
                        .build();
            }
        }

        // Captured before the lines are replaced, so an edit that stops paying
        // a bill still triggers a re-check of that bill's purchase order.
        Set<Integer> previouslySettledBills = new LinkedHashSet<>();
        if (!isNew) {
            for (PaymentDetails existing : paymentDetails.findByPaymentRefId(dto.getId())) {
                Integer billId = zeroToNull(existing.getBillMasterRefId());
                if (billId != null) {
                    previouslySettledBills.add(billId);
                }
            }
        }

        Payment payment;
        if (isNew) {
            payment = new Payment();
            payment.setCompanyRefId(companyId);
            payment.setCreatedDate(now);
            payment.setCreatedBy(actor(dto));
            payment.setCNumber(0);
            payment.setCNumberDisplay("");
        } else {
            payment = payments.findById(dto.getId()).orElse(null);
            if (payment == null || !Objects.equals(payment.getCompanyRefId(), companyId)) {
                return failure("Payment not found: " + dto.getId());
            }
        }

        applyHeader(payment, dto, companyId, paymentDate, now);

        // Saved before the lines so a new payment has an id to hang them on.
        payment = payments.save(payment);

        if (isNew) {
            int sequence = nextSequence(companyId);
            payment.setCNumber(sequence);
            payment.setCNumberDisplay(formatPaymentNumber(sequence));
            payment = payments.save(payment);
            recordSequence(companyId, sequence);
        }

        // Bills this payment used to settle but no longer does must be
        // re-checked too, or dropping a line leaves its order reading as paid.
        Set<Integer> touchedBills = new LinkedHashSet<>(previouslySettledBills);
        touchedBills.addAll(billIdsOf(lines));

        applyLines(payment, lines, companyId, now);
        syncPurchaseOrderStatus(touchedBills);

        log.info("Payment {} saved as {} ({} line(s))",
                payment.getId(), payment.getCNumberDisplay(), lines.size());
        return PaymentSaveResponseDto.builder()
                .success(true)
                .message(isNew ? "Payment created successfully" : "Payment updated successfully")
                .id(payment.getId())
                .paymentNoDisplay(payment.getCNumberDisplay())
                .build();
    }

    /**
     * Mirrors SP_Payment's reference validation, message for message.
     * Returns the complaint, or null when everything checks out.
     */
    private String checkReferences(PaymentSaveRequestDto dto, Integer companyId) {
        Integer userRefId = zeroToNull(dto.getUserRefId());
        if (userRefId != null
                && !appUsers.existsByIdAndCompanyRefIdAndActive(userRefId, companyId, 1)) {
            return "Login User Not Found Issue id" + userRefId;
        }
        Integer bankRefId = zeroToNull(dto.getBankRefId());
        if (bankRefId != null
                && !banks.existsByIdAndCompanyRefIdAndActive(bankRefId, companyId, 1)) {
            return "Bank  Not Found Issue id" + bankRefId;
        }
        Integer employeeRefId = zeroToNull(dto.getEmployeeRefId());
        if (employeeRefId != null
                && !employees.existsByIdAndCompanyRefIdAndActive(employeeRefId, companyId, 1)) {
            return "Employee Not Found Issue id" + employeeRefId;
        }
        return null;
    }

    private void applyHeader(Payment payment, PaymentSaveRequestDto dto, Integer companyId,
                             LocalDateTime paymentDate, LocalDateTime now) {
        payment.setCompanyRefId(companyId);
        payment.setSupplierRefId(dto.getSupplierRefId());
        payment.setBankRefId(dto.getBankRefId());
        payment.setUserRefId(zeroToNull(dto.getUserRefId()));
        if (payment.getId() == null) {
            // Insert only. SP_Payment's UPDATE branch deliberately leaves
            // EmployeeRefId alone, so this stays as whoever first entered the
            // payment. Overwriting it on edit would move the payment into the
            // editing clerk's name in the F5 grid and out of the original
            // clerk's "my payments" filter.
            payment.setEmployeeRefId(zeroToNull(dto.getEmployeeRefId()));
        }
        // Who touched the payment last — the SP wrote this from EmployeeRefId
        // on both insert and update, which is what keeps the two columns
        // differing after someone else edits the row.
        payment.setLastEmployeeRefId(zeroToNull(dto.getEmployeeRefId()));
        payment.setPaymentDate(paymentDate);
        payment.setRefNumber(dto.getRefNumber());
        payment.setPayTo(dto.getPayTo());
        payment.setDescription(dto.getDescription());
        payment.setRemarks(dto.getRemarks());
        // The screen's status dropdown opens on its first entry, so a payload
        // that omits the status means the clerk left it untouched.
        payment.setPaymentStatus(orDefault(dto.getPaymentStatus(), DEFAULT_PAYMENT_STATUS));
        payment.setPvStatus(dto.getPvStatus() == null ? 0 : dto.getPvStatus());
        payment.setAmount(dto.getAmount() == null ? BigDecimal.ZERO : dto.getAmount());
        payment.setBankCharges(f(dto.getBankCharges()));
        payment.setCurrencyValue(f(dto.getCurrencyValue()));
        payment.setActualAmount(f(dto.getActualAmount()));
        payment.setTinNo(dto.getTinNo());
        payment.setSstNo(dto.getSstNo());
        payment.setMsicCode(dto.getMsicCode());
        payment.setServiceTaxType(dto.getServiceTaxType());
        payment.setBankName(dto.getBankName());
        payment.setAccountNo(dto.getAccountNo());
        payment.setModifiedDate(now);
        payment.setModifiedBy(actor(dto));
        if (payment.getCreatedBy() == null) {
            payment.setCreatedBy(actor(dto));
        }
        if (payment.getCreatedDate() == null) {
            payment.setCreatedDate(now);
        }
    }

    /**
     * Replaces this payment's lines with the posted ones.
     *
     * <p>SP_Payment deleted the lot and re-inserted on every edit; doing the
     * same keeps a document that was dropped from the grid from silently
     * staying settled.
     */
    private void applyLines(Payment payment, List<PaymentSaveDetailDto> lines,
                            Integer companyId, LocalDateTime now) {
        List<PaymentDetails> existing = paymentDetails.findByPaymentRefId(payment.getId());
        if (!existing.isEmpty()) {
            paymentDetails.deleteAll(existing);
            paymentDetails.flush();
        }

        List<PaymentDetails> toSave = new ArrayList<>();
        for (PaymentSaveDetailDto line : lines) {
            PaymentDetails row = new PaymentDetails();
            row.setCompanyRefId(companyId);
            row.setPaymentRefId(payment.getId());
            row.setBillMasterRefId(zeroToNull(line.getBillMasterRefId()));
            row.setPurchaseMasterRefId(zeroToNull(line.getPurchaseMasterRefId()));
            row.setSupplieropenRefId(zeroToNull(line.getSupplieropenRefId()));
            row.setPaymentAmount(line.getAmount() == null ? BigDecimal.ZERO : line.getAmount());
            row.setCurrencyValue(f(line.getCurrencyValue()));
            row.setActualAmount(f(line.getActualAmount()));
            row.setCreatedDate(now);
            toSave.add(row);
        }
        paymentDetails.saveAll(toSave);
    }

    /** A bill counts as settled once rounding is the only thing left. */
    private static final BigDecimal SETTLED_TOLERANCE = new BigDecimal("0.01");

    /** What a purchase order reads as once billed but not yet paid off. */
    private static final String STATUS_INVOICED = "INVOICE MADE";
    private static final String STATUS_PAID = "PAYMENT COMPLETED";

    /**
     * Brings each affected purchase order's status in line with what its bill
     * has actually been paid.
     *
     * <p>The legacy service flagged the order {@code PAYMENT COMPLETED} on
     * <em>any</em> payment touching the bill, however small. Audited against
     * MalevanewDemo on 2026-08-27, that left 17 purchase orders reading as
     * fully paid with RM 102,808.50 still owed on them — one showed RM 100,000
     * paid against a RM 178,000 bill. So the total settled is compared with the
     * bill here, and the flag is also withdrawn when a payment is reduced or
     * deleted and the bill falls back into arrears.
     *
     * <p>Must run after the lines are flushed, or this payment's own
     * settlements are not counted yet.
     */
    private void syncPurchaseOrderStatus(Set<Integer> billIds) {
        if (billIds == null || billIds.isEmpty()) {
            return;
        }

        List<Map<String, Object>> bills = jdbc.queryForList(
                "SELECT b.Id AS billId, b.BillsOrderMasterRefId AS orderId, "
                        + "b.Amount AS billAmount, "
                        + "ISNULL((SELECT SUM(d.PaymentAmount) FROM PaymentDetails d WITH(NOLOCK) "
                        + "        WHERE d.BillMasterRefId = b.Id), 0) AS settled "
                        + "FROM BillMaster b WITH(NOLOCK) "
                        + "WHERE b.Id IN (:billIds) AND b.BillsOrderMasterRefId IS NOT NULL",
                new MapSqlParameterSource("billIds", billIds));

        for (Map<String, Object> row : bills) {
            Integer orderId = (Integer) row.get("orderId");
            BillsOrderMaster order = billsOrders.findById(orderId).orElse(null);
            if (order == null) {
                log.warn("Bill {} references purchase order {} which no longer exists — not flagged",
                        row.get("billId"), orderId);
                continue;
            }

            BigDecimal billAmount = toDecimal(row.get("billAmount"));
            BigDecimal settled = toDecimal(row.get("settled"));
            boolean fullyPaid = settled.compareTo(billAmount.subtract(SETTLED_TOLERANCE)) >= 0;
            String current = order.getBillStatus();

            String next;
            if (fullyPaid) {
                next = STATUS_PAID;
            } else if (STATUS_PAID.equalsIgnoreCase(current)) {
                // Was marked paid, isn't any more — a payment was reduced or
                // removed. Leaving it would hide a live debt.
                next = STATUS_INVOICED;
            } else {
                continue; // partly paid and never claimed otherwise: nothing to say
            }

            if (next.equalsIgnoreCase(current)) {
                continue;
            }
            order.setBillStatus(next);
            order.setModifiedDate(LocalDateTime.now());
            order.setModifiedBy("From Payment");
            billsOrders.save(order);
            log.info("Purchase order {} set to {} (bill {} of {} settled {})",
                    orderId, next, row.get("billId"), billAmount, settled);
        }
    }

    /** The bills a set of posted lines touches. */
    private static Set<Integer> billIdsOf(List<PaymentSaveDetailDto> lines) {
        Set<Integer> billIds = new LinkedHashSet<>();
        for (PaymentSaveDetailDto line : lines) {
            Integer billId = zeroToNull(line.getBillMasterRefId());
            if (billId != null) {
                billIds.add(billId);
            }
        }
        return billIds;
    }

    /**
     * Records the number just handed out.
     *
     * <p>The counter row is created when it is missing. SP_Payment only ever
     * issued an UPDATE here, so a company with no {@code Payment} row in
     * SequenceNoMaster silently numbered every payment {@code PY000000001}.
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

    /**
     * Takes a SQL Server application lock on this save's identity, so two
     * copies of the same click cannot both pass the duplicate check.
     *
     * <p>The lock is held by the transaction and released when it ends, and it
     * lives in the database rather than the JVM so it still works with more
     * than one app instance behind a load balancer.
     */
    private boolean lockSaveIntent(PaymentSaveRequestDto dto, Integer companyId,
                                   LocalDateTime paymentDate) {
        String key = "PaymentSave:" + companyId + ":" + saveFingerprint(dto, paymentDate);
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
    private static String saveFingerprint(PaymentSaveRequestDto dto, LocalDateTime paymentDate) {
        return "sup:" + dto.getSupplierRefId()
                + "|bank:" + dto.getBankRefId()
                + "|date:" + paymentDate.toLocalDate()
                + "|amt:" + (dto.getAmount() == null ? "0" : dto.getAmount().toPlainString())
                + "|ref:" + trimmed(dto.getRefNumber());
    }

    /**
     * The payment this save would duplicate, or null when it is genuinely new.
     *
     * <p>Unlike a bill there is no supplier document number to match on, so
     * this is a time window only: two payments to the same supplier from the
     * same bank for the same amount on the same day are a double-click when
     * they are seconds apart, and legitimate when they are not.
     */
    private Payment findDuplicate(PaymentSaveRequestDto dto, Integer companyId,
                                  LocalDateTime paymentDate, LocalDateTime now) {
        return payments.findRecentlyEnteredLikeThis(
                        companyId, dto.getSupplierRefId(), dto.getBankRefId(), paymentDate,
                        dto.getAmount() == null ? BigDecimal.ZERO : dto.getAmount(),
                        trimmed(dto.getRefNumber()), now.minus(DUPLICATE_WINDOW))
                .stream().findFirst().orElse(null);
    }

    /* ── delete ────────────────────────────────────────────────────── */

    /**
     * Removes a payment and the lines beneath it.
     *
     * <p>Legacy hard-deleted the master row only and left the child rows to a
     * cascade; the lines are removed explicitly here so the delete cannot leave
     * orphaned settlements that still count against a bill's balance.
     *
     * <p>A payment already pushed to the payment-voucher queue is refused —
     * the F5 grid disabled the button for those, but nothing enforced it.
     */
    @Transactional
    public boolean delete(Integer id, Integer companyId) {
        Payment payment = payments.findById(id).orElse(null);
        if (payment == null || !Objects.equals(payment.getCompanyRefId(), companyId)) {
            return false;
        }
        if (payment.getPvStatus() != null && payment.getPvStatus() == 1) {
            throw new IllegalStateException(
                    "Not able to delete — payment process already started");
        }
        paymentDetails.deleteAll(paymentDetails.findByPaymentRefId(id));
        payments.delete(payment);
        log.info("Payment {} deleted", id);
        return true;
    }

    /* ── edit ──────────────────────────────────────────────────────── */

    /**
     * Loads one payment back into the screen, by id or by the running number
     * the clerk typed.
     *
     * <p>The grid is not the saved lines: it is the supplier's whole
     * outstanding list with this payment's amounts written back onto the
     * matching rows, which is what lets a clerk move money from one document to
     * another and re-save.
     */
    @Transactional(readOnly = true)
    public Optional<PaymentEditDto> edit(Integer id, Integer paymentNumber, Integer companyId) {
        Payment payment = null;
        if (paymentNumber != null && paymentNumber != 0) {
            payment = payments.findByCompanyRefIdAndCNumber(companyId, paymentNumber).orElse(null);
        } else if (id != null && id != 0) {
            payment = payments.findById(id)
                    .filter(p -> Objects.equals(p.getCompanyRefId(), companyId))
                    .orElse(null);
        }
        if (payment == null) {
            return Optional.empty();
        }

        List<SupplierBillDto> grid =
                supplierBills(companyId, payment.getSupplierRefId(), payment.getId());

        // Write the saved amounts back onto the outstanding rows they settle.
        for (PaymentDetails line : paymentDetails.findByPaymentRefId(payment.getId())) {
            SupplierBillDto row = matchingRow(grid, line);
            if (row != null) {
                row.setAmount(line.getPaymentAmount());
            } else {
                log.warn("Payment {} settles document (bill={}, po={}, opening={}) "
                                + "that is no longer outstanding — its amount is not shown",
                        payment.getId(), line.getBillMasterRefId(),
                        line.getPurchaseMasterRefId(), line.getSupplieropenRefId());
            }
        }

        return Optional.of(PaymentEditDto.builder()
                .id(payment.getId())
                .companyRefId(payment.getCompanyRefId())
                .supplierRefId(payment.getSupplierRefId())
                .bankRefId(payment.getBankRefId())
                .employeeRefId(payment.getEmployeeRefId())
                .userRefId(payment.getUserRefId())
                .paymentDate(payment.getPaymentDate())
                .sPaymentDate(format(payment.getPaymentDate()))
                .cNumber(payment.getCNumber())
                .cNumberDisplay(payment.getCNumberDisplay())
                .refNumber(payment.getRefNumber())
                .payTo(payment.getPayTo())
                .description(payment.getDescription())
                .remarks(payment.getRemarks())
                .paymentStatus(payment.getPaymentStatus())
                .pvStatus(payment.getPvStatus())
                .amount(payment.getAmount())
                .bankCharges(payment.getBankCharges())
                .currencyValue(payment.getCurrencyValue())
                .actualAmount(payment.getActualAmount())
                .tinNo(payment.getTinNo())
                .sstNo(payment.getSstNo())
                .msicCode(payment.getMsicCode())
                .serviceTaxType(payment.getServiceTaxType())
                .bankName(payment.getBankName())
                .accountNo(payment.getAccountNo())
                .qneCode(payment.getQneCode())
                .qneId(payment.getQneId())
                .paymentDetails(grid)
                .build());
    }

    /**
     * The outstanding row a saved line settles.
     *
     * <p>Legacy matched on purchase order and bill only, so a payment against a
     * supplier's opening balance came back with a blank amount and zeroed
     * itself on the next save; the opening-balance case is matched here too.
     */
    private static SupplierBillDto matchingRow(List<SupplierBillDto> grid, PaymentDetails line) {
        Integer purchaseId = zeroToNull(line.getPurchaseMasterRefId());
        Integer billId = zeroToNull(line.getBillMasterRefId());
        Integer openingId = zeroToNull(line.getSupplieropenRefId());
        for (SupplierBillDto row : grid) {
            if (purchaseId != null && purchaseId.equals(row.getPurchaseMasterRefId())) {
                return row;
            }
            if (billId != null && billId.equals(row.getBillMasterRefId())) {
                return row;
            }
            if (openingId != null && openingId.equals(row.getSupplieropenRefId())) {
                return row;
            }
        }
        return null;
    }

    /* ── outstanding documents ─────────────────────────────────────── */

    /**
     * Everything this supplier is still owed money on — the Java port of
     * {@code RT_SupplierBills}.
     *
     * <p>{@code excludePaymentId} keeps the payment currently open in the
     * screen out of the "already paid" sum, so re-editing it shows the bills it
     * settles as still outstanding rather than gone.
     *
     * <p><b>Purchase orders are deliberately not listed.</b> {@code RT_SupplierBills}
     * has a third arm for {@code PurchaseMaster} that is commented out in the
     * database, and this port intentionally leaves it out rather than
     * "restoring" it. Checked against MalevanewDemo on 2026-08-27: 217 credit
     * purchase orders exist and are still being raised, but not one payment has
     * ever settled one — {@code PaymentDetails.PurchaseMasterRefId} is unused in
     * all 12,112 rows. Enabling it would put 213 documents worth ~RM136k in
     * front of every clerk as payable, so it is a business decision, not a
     * cleanup. Confirmed to stay off; reopen it only on an explicit request.
     */
    @Transactional(readOnly = true)
    public List<SupplierBillDto> supplierBills(Integer companyId, Integer supplierId,
                                               Integer excludePaymentId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("comid", companyId)
                .addValue("supplierId", supplierId)
                .addValue("excludeId", excludePaymentId == null ? 0 : excludePaymentId);

        String settled = "ISNULL((SELECT SUM(ISNULL(pd.PaymentAmount,0)) "
                + "FROM PaymentDetails pd WITH(NOLOCK) "
                + "INNER JOIN Payment py WITH(NOLOCK) ON py.Id = pd.PaymentRefId "
                + "WHERE pd.%s = P.Id AND py.Id <> :excludeId), 0)";

        String sql = "SELECT * FROM ("
                // Credit bills raised against this supplier.
                + "SELECT CAST(NULL AS int) AS PurchaseMasterRefId, P.Id AS BillMasterRefId, "
                + "CAST(NULL AS int) AS SupplieropenRefId, ISNULL(P.InvoiceNo,'') AS InvoiceNo, "
                + "ISNULL(P.CNumberDisplay,'') AS BillNo, P.SaleDate AS BillDate, "
                + "P.Amount AS BillAmount, " + String.format(settled, "BillMasterRefId") + " AS Payment "
                + "FROM BillMaster P WITH(NOLOCK) "
                + "WHERE P.CompanyRefId = :comid AND P.Active = 1 AND P.SaleType = 'CREDIT' "
                + "AND P.SupplierRefId = :supplierId "
                + "UNION ALL "
                // The balance the supplier was carrying when they were set up.
                + "SELECT CAST(NULL AS int), CAST(NULL AS int), P.Id, '', '', "
                + "CAST('1900-01-01' AS datetime), P.OpeningBalance, "
                + String.format(settled, "SupplieropenRefId") + " "
                + "FROM Supplier P WITH(NOLOCK) "
                + "WHERE P.CompanyRefId = :comid AND P.Active = 1 AND P.Id = :supplierId"
                + ") t WHERE (t.BillAmount - t.Payment) <> 0 "
                + "ORDER BY t.BillDate";

        return jdbc.query(sql, params, (rs, i) -> {
            BigDecimal billAmount = decimal(rs, "BillAmount");
            BigDecimal paid = decimal(rs, "Payment");
            LocalDateTime billDate = dateTime(rs, "BillDate");
            return SupplierBillDto.builder()
                    .purchaseMasterRefId(nullableInt(rs, "PurchaseMasterRefId"))
                    .billMasterRefId(nullableInt(rs, "BillMasterRefId"))
                    .supplieropenRefId(nullableInt(rs, "SupplieropenRefId"))
                    .invoiceNo(rs.getString("InvoiceNo"))
                    .billNo(rs.getString("BillNo"))
                    .sBillDate(billDate == null ? "" : GRID_DATE.format(billDate))
                    .billAmount(billAmount)
                    .payment(paid)
                    .balance(billAmount.subtract(paid))
                    .amount(BigDecimal.ZERO)
                    .build();
        });
    }

    /* ── supplier balance ──────────────────────────────────────────── */

    /**
     * A supplier's running balance as at a date — the Java port of
     * {@code SupplierBalance_Single}; with no {@code supplierId} it covers
     * every active supplier, as {@code SupplierBalance} did.
     */
    @Transactional(readOnly = true)
    public List<SupplierBalanceDto> supplierBalance(Integer companyId, Integer supplierId,
                                                    String tillDate) {
        LocalDate till = parseDate(tillDate);
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("comid", companyId)
                .addValue("tillDate", (till == null ? LocalDate.now() : till).atTime(23, 59, 59));

        StringBuilder sql = new StringBuilder(
                "SELECT S.Id, S.SupplierName, S.MobileNo, S.Address1, S.Address2, S.City, S.Zipcode, "
                        + "(ISNULL(S.OpeningBalance,0) "
                        + "+ ISNULL((SELECT SUM(Amount) FROM PurchaseMaster WITH(NOLOCK) "
                        + "WHERE SupplierRefId = S.Id AND CompanyRefId = :comid "
                        + "AND SaleDate <= :tillDate), 0) "
                        + "+ ISNULL((SELECT SUM(Amount) FROM BillMaster WITH(NOLOCK) "
                        + "WHERE SupplierRefId = S.Id AND CompanyRefId = :comid "
                        + "AND SaleDate <= :tillDate), 0) "
                        + "- ISNULL((SELECT SUM(Amount) FROM Payment WITH(NOLOCK) "
                        + "WHERE SupplierRefId = S.Id AND CompanyRefId = :comid "
                        + "AND PaymentDate <= :tillDate), 0)) AS Balance "
                        + "FROM Supplier S WITH(NOLOCK) "
                        + "WHERE S.CompanyRefId = :comid AND S.Active = 1");

        if (supplierId != null && supplierId != 0) {
            sql.append(" AND S.Id = :supplierId");
            params.addValue("supplierId", supplierId);
        }

        return jdbc.query(sql.toString(), params, (rs, i) -> SupplierBalanceDto.builder()
                .id(rs.getInt("Id"))
                .supplierName(rs.getString("SupplierName"))
                .mobileNo(rs.getString("MobileNo"))
                .address1(rs.getString("Address1"))
                .address2(rs.getString("Address2"))
                .city(rs.getString("City"))
                .zipcode(rs.getString("Zipcode"))
                .balance(decimal(rs, "Balance"))
                .build());
    }

    /**
     * The credit period on a payment term, in days — the screen adds it to the
     * payment date to show a due date.
     *
     * <p>Legacy named this parameter {@code SupplierId} but queried
     * {@code PaymentTermsMaster.Id} with it; the name is corrected here.
     */
    @Transactional(readOnly = true)
    public Integer paymentTermsDueDays(Integer paymentTermsId) {
        if (paymentTermsId == null || paymentTermsId == 0) {
            return null;
        }
        List<Integer> days = jdbc.queryForList(
                "SELECT TDays FROM PaymentTermsMaster WITH(NOLOCK) WHERE Id = :id",
                new MapSqlParameterSource("id", paymentTermsId), Integer.class);
        return days.isEmpty() ? null : days.get(0);
    }

    /* ── F5 search ─────────────────────────────────────────────────── */

    /**
     * The payment F5 grid: matching payments plus all of their lines.
     *
     * <p>A search term matches the payment number and, as in legacy, overrides
     * the date and every other filter — so a clerk can find one payment without
     * knowing when it was made.
     */
    @Transactional(readOnly = true)
    public PaymentF5ViewDto search(SelectPaymentRequestDto request, Integer companyId) {
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("comid", companyId);
        StringBuilder where = new StringBuilder();

        boolean hasSearch = request.getSearch() != null && !request.getSearch().isBlank();
        if (hasSearch) {
            where.append(" AND A.CNumberDisplay = :search ");
            params.addValue("search", request.getSearch().trim());
        } else {
            if (request.getSupplierId() != null && request.getSupplierId() != 0) {
                where.append(" AND A.SupplierRefId = :supplierId ");
                params.addValue("supplierId", request.getSupplierId());
            }
            if (request.getEmployeeId() != null && request.getEmployeeId() != 0) {
                where.append(" AND A.EmployeeRefId = :employeeId ");
                params.addValue("employeeId", request.getEmployeeId());
            }
            if (request.getDescription() != null && !request.getDescription().isBlank()) {
                where.append(" AND A.Description = :description ");
                params.addValue("description", request.getDescription().trim());
            }
            if (request.getPaymentStatus() != null && !request.getPaymentStatus().isBlank()) {
                where.append(" AND A.PaymentStatus = :paymentStatus ");
                params.addValue("paymentStatus", request.getPaymentStatus().trim());
            }
            where.append(" AND A.PaymentDate BETWEEN :fromDate AND :toDate ");
            params.addValue("fromDate", startOfDay(request.getFromDate()));
            params.addValue("toDate", endOfDay(request.getToDate()));
        }

        String masterSql = headerSelect()
                + "WHERE A.CompanyRefId = :comid" + where
                + " ORDER BY A.PaymentDate DESC, A.Id DESC";
        List<PaymentMasterViewDto> headers = jdbc.query(masterSql, params, this::mapHeader);

        // The line query re-applies the same filters through its own join back
        // to Payment, so the two halves of the grid always agree.
        String detailSql = "SELECT B.PaymentRefId AS SaleRefId, B.BillMasterRefId AS PDId, "
                + "B.PaymentAmount, "
                + "ISNULL(P.CNumberDisplay,'') AS PurchaseNo, "
                + "ISNULL(CONVERT(VARCHAR(26), P.SaleDate, 20),'') AS SPurchaseDate, "
                + "ISNULL(BM.CNumberDisplay,'') AS BillNo, "
                + "ISNULL(CONVERT(VARCHAR(26), BM.SaleDate, 20),'') AS SBillDate, "
                + "ISNULL(S.SupplierName,'') AS DSupplierName "
                + "FROM PaymentDetails B WITH(NOLOCK) "
                + "INNER JOIN Payment A WITH(NOLOCK) ON B.PaymentRefId = A.Id "
                + "LEFT JOIN PurchaseMaster P WITH(NOLOCK) ON P.Id = B.PurchaseMasterRefId "
                + "LEFT JOIN BillMaster BM WITH(NOLOCK) ON BM.Id = B.BillMasterRefId "
                + "LEFT JOIN Supplier S WITH(NOLOCK) ON S.Id = B.SupplieropenRefId "
                + "WHERE A.CompanyRefId = :comid" + where;

        List<PaymentDetailViewDto> lines = jdbc.query(detailSql, params, (rs, i) -> {
            String purchaseNo = rs.getString("PurchaseNo");
            String purchaseDate = rs.getString("SPurchaseDate");
            String billNo = rs.getString("BillNo");
            String billDate = rs.getString("SBillDate");
            // The grid only renders the purchase columns, so a line that
            // settles a bill has to arrive in them.
            return PaymentDetailViewDto.builder()
                    .saleRefId(rs.getInt("SaleRefId"))
                    .pdId(nullableInt(rs, "PDId"))
                    .purchaseNo(isBlank(purchaseNo) ? billNo : purchaseNo)
                    .sPurchaseDate(isBlank(purchaseDate) ? billDate : purchaseDate)
                    .billNo(billNo)
                    .sBillDate(billDate)
                    .dSupplierName(rs.getString("DSupplierName"))
                    .paymentAmount(decimal(rs, "PaymentAmount"))
                    .build();
        });

        // Summed by SQL Server over numeric(18,2), so the figure on screen is
        // the ledger's own and cannot drift from adding floats in a browser.
        BigDecimal total = jdbc.queryForObject(
                "SELECT ISNULL(SUM(A.Amount), 0) FROM Payment A WITH(NOLOCK) "
                        + "INNER JOIN Supplier B WITH(NOLOCK) ON A.SupplierRefId = B.Id "
                        + "LEFT JOIN EmployeeMaster E WITH(NOLOCK) ON E.Id = A.EmployeeRefId "
                        + "INNER JOIN BankMaster BA WITH(NOLOCK) ON BA.Id = A.BankRefId "
                        + "WHERE A.CompanyRefId = :comid" + where,
                params, BigDecimal.class);

        return PaymentF5ViewDto.builder()
                .paymentMaster(headers)
                .paymentDetails(lines)
                .totalAmount(total == null ? BigDecimal.ZERO : total)
                .count(headers.size())
                .build();
    }

    /**
     * Payments still waiting to be pushed onward.
     *
     * <p>{@code notInQne} true limits the list to payments QNE has never seen.
     */
    @Transactional(readOnly = true)
    public List<PaymentMasterViewDto> dueBills(Integer companyId, String fromDate, String toDate,
                                               Integer employeeId, boolean dateWise,
                                               boolean notInQne) {
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("comid", companyId);
        StringBuilder where = new StringBuilder();

        if (notInQne) {
            where.append(" AND ISNULL(A.QNECode,'') = '' ");
        }
        if (employeeId != null && employeeId != 0) {
            // Legacy built this clause without its leading AND, so asking for
            // one employee produced invalid SQL and the screen came back empty.
            where.append(" AND A.EmployeeRefId = :employeeId ");
            params.addValue("employeeId", employeeId);
        }
        if (dateWise) {
            where.append(" AND A.PaymentDate BETWEEN :fromDate AND :toDate ");
            params.addValue("fromDate", startOfDay(fromDate));
            params.addValue("toDate", endOfDay(toDate));
        }

        String sql = headerSelect() + "WHERE A.CompanyRefId = :comid" + where
                + " ORDER BY A.PaymentDate DESC, A.Id DESC";
        return jdbc.query(sql, params, this::mapHeader);
    }

    /* ── status ────────────────────────────────────────────────────── */

    /** Marks a payment as cleared, which is what the F5 grid's tick does. */
    @Transactional
    public boolean markCompleted(Integer id, Integer companyId) {
        Payment payment = payments.findById(id).orElse(null);
        if (payment == null || !Objects.equals(payment.getCompanyRefId(), companyId)) {
            return false;
        }
        payment.setPaymentStatus("COMPLETED");
        payment.setModifiedDate(LocalDateTime.now());
        payments.save(payment);
        return true;
    }

    /* ── shared projection ─────────────────────────────────────────── */

    /** Shared header projection for both grids — the columns the legacy sent. */
    private static String headerSelect() {
        return "SELECT A.Id, A.CNumber AS BillNo, A.CNumberDisplay AS BillNoDisplay, "
                + "FORMAT(ISNULL(A.PaymentDate,'1900-01-01'),'dd/MM/yyyy') AS BillDate, "
                + "FORMAT(ISNULL(A.Created_Date,'1900-01-01'),'dd/MM/yyyy hh:mm:ss') AS BillTime, "
                + "ISNULL(A.QNECode,'') AS QNECode, ISNULL(A.QNEId,'') AS QNEId, "
                + "ISNULL(E.EmployeeName,'') AS EmployeeName, B.SupplierName AS SupplierName, "
                + "BA.Name AS BankName, A.Amount AS Amount, ISNULL(A.RefNumber,'') AS RefNumber, "
                + "ISNULL(A.Remarks,'') AS Remarks, ISNULL(A.PaymentStatus,'') AS PaymentStatus, "
                + "A.PVStatus, ISNULL(A.PayTo,'') AS PayTo, ISNULL(A.Description,'') AS Description "
                + "FROM Payment A WITH(NOLOCK) "
                + "INNER JOIN Supplier B WITH(NOLOCK) ON A.SupplierRefId = B.Id "
                + "LEFT JOIN EmployeeMaster E WITH(NOLOCK) ON E.Id = A.EmployeeRefId "
                + "INNER JOIN BankMaster BA WITH(NOLOCK) ON BA.Id = A.BankRefId ";
    }

    private PaymentMasterViewDto mapHeader(ResultSet rs, int rowNum) throws SQLException {
        return PaymentMasterViewDto.builder()
                .id(rs.getInt("Id"))
                .billNo(rs.getInt("BillNo"))
                .billNoDisplay(rs.getString("BillNoDisplay"))
                .billDate(rs.getString("BillDate"))
                .billTime(rs.getString("BillTime"))
                .qneCode(rs.getString("QNECode"))
                .qneId(rs.getString("QNEId"))
                .employeeName(rs.getString("EmployeeName"))
                .supplierName(rs.getString("SupplierName"))
                .bankName(rs.getString("BankName"))
                .amount(decimal(rs, "Amount"))
                .refNumber(rs.getString("RefNumber"))
                .remarks(rs.getString("Remarks"))
                .paymentStatus(rs.getString("PaymentStatus"))
                .pvStatus(rs.getInt("PVStatus"))
                .payTo(rs.getString("PayTo"))
                .description(rs.getString("Description"))
                .build();
    }

    /* ── helpers ───────────────────────────────────────────────────── */

    private static PaymentSaveResponseDto failure(String message) {
        return PaymentSaveResponseDto.builder().success(false).message(message).build();
    }

    private static <T> List<T> safe(List<T> values) {
        return values == null ? List.of() : values;
    }

    private static Integer zeroToNull(Integer value) {
        return (value == null || value == 0) ? null : value;
    }

    private static Float f(Float value) {
        return value == null ? 0f : value;
    }

    private static int signum(BigDecimal value) {
        return value == null ? 0 : value.signum();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String trimmed(String value) {
        return value == null ? "" : value.trim();
    }

    private static String orDefault(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    /**
     * Who to stamp on Created_By/Modified_By — the employee logged into the
     * screen.
     *
     * <p><b>Deliberately different from legacy.</b> SP_Payment stamped
     * {@code suser_name()}, which is the shared SQL login: all 3,231 live rows
     * read {@code 'sa'}, so the columns record nothing. This records the
     * employee instead, matching how the bill module was migrated. Anything
     * that filters payments on {@code Created_By = 'sa'} would need updating —
     * nothing found does.
     */
    private static String actor(PaymentSaveRequestDto dto) {
        return dto.getEmployeeRefId() == null || dto.getEmployeeRefId() == 0
                ? "SYSTEM" : String.valueOf(dto.getEmployeeRefId());
    }

    private static String format(LocalDateTime value) {
        return value == null ? "" : GRID_DATE.format(value);
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
     * SQL Server hands a {@code numeric} column back as BigDecimal and a
     * {@code real} one as Float; BillMaster.Amount is the latter.
     */
    private static BigDecimal toDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        return BigDecimal.valueOf(((Number) value).doubleValue());
    }

    private static LocalDateTime dateTime(ResultSet rs, String column) throws SQLException {
        java.sql.Timestamp value = rs.getTimestamp(column);
        return value == null ? null : value.toLocalDateTime();
    }

    /**
     * Parses a date. ISO ({@code yyyy-MM-dd}) is the contract; {@code dd/MM/yyyy}
     * is accepted because that is what the screen shows. Ambiguous US-style
     * input is deliberately not guessed at — a wrong guess silently pays
     * against the wrong month.
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

    /** Inclusive upper bound — a payment made at 16:20 still falls inside its own day. */
    private static LocalDateTime endOfDay(String value) {
        LocalDate date = parseDate(value);
        return date == null
                ? LocalDate.of(2999, 12, 31).atTime(23, 59, 59)
                : date.atTime(23, 59, 59);
    }
}
