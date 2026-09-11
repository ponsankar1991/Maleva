package my.maleva.api.module.customerstatement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.company.repository.MasterSettingRepository;
import my.maleva.api.module.customerstatement.dto.AgeingBucket;
import my.maleva.api.module.customerstatement.dto.CustomerStatement;
import my.maleva.api.module.customerstatement.dto.StatementLine;
import my.maleva.api.module.customerstatement.dto.StatementRequest;
import my.maleva.api.module.customerstatement.dto.StatementResult;
import my.maleva.api.module.customerstatement.repository.CustomerStatementQueryRepository;
import my.maleva.api.module.customerstatement.repository.CustomerStatementQueryRepository.AgeingRow;
import my.maleva.api.module.customerstatement.repository.CustomerStatementQueryRepository.CreditNoteKnockoff;
import my.maleva.api.module.customerstatement.repository.CustomerStatementQueryRepository.CustomerHeader;
import my.maleva.api.module.customerstatement.repository.CustomerStatementQueryRepository.OpeningBalance;
import my.maleva.api.module.customerstatement.repository.CustomerStatementQueryRepository.OutstandingInvoice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Builds Statements of Account — the port of legacy
 * {@code SelectCustomerStatementAllReport} with the arithmetic moved out of
 * the browser and into one place.
 *
 * <p>Legacy left the running balance, the overdue amount and the overdue date
 * to the screen's JavaScript, where {@code balance} was reset per customer so
 * an all-customers run reported only the last one, and the send button read
 * recipients from row index 1 only. Every figure the page or the mail needs is
 * computed here, once.
 *
 * <p><b>One deliberate correction.</b> With "Include Credit Note" on, legacy
 * listed each invoice at its outstanding amount — already net of credit-note
 * knockoffs — and then listed the credit note as a credit line too, so a
 * RM 1,000 invoice with a RM 200 credit note showed a debit of 800 and a
 * credit of 200: a balance of 600 against a true 800. Here, when credit notes
 * are shown, the invoice line is net of receipts only and the credit note
 * carries the knockoff, so the running balance lands on what is actually owed.
 * With the toggle off the lines are exactly legacy's.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerStatementService {

    /**
     * MasterSetting variable holding the company's statement cutoff.
     * Invoices before it are never listed.
     */
    public static final String CUTOFF_SETTING = "STATEMENT_CUTOFF_DATE";

    /**
     * Legacy's hardcoded {@code '2024-10-01'}, in five places. It is the day
     * the ledger was cut over and everything earlier settled in the opening
     * balance — a business fact, so it lives in MasterSetting per company and
     * this is only the fallback for a company that has not set one.
     */
    static final LocalDate LEGACY_CUTOFF = LocalDate.of(2024, 10, 1);

    private static final DateTimeFormatter LABEL = DateTimeFormatter.ofPattern("MMM yy", Locale.ENGLISH);

    private final CustomerStatementQueryRepository queries;
    private final MasterSettingRepository settings;

    @Transactional(readOnly = true)
    public StatementResult build(StatementRequest request) {
        int companyId = request.getCompanyId();
        Integer customerId = request.isSingleCustomer() ? request.getCustomerId() : null;
        LocalDate today = LocalDate.now();

        LocalDate cutoff = cutoffFor(companyId);
        LocalDate periodFrom;
        LocalDate periodToExclusive;
        if (request.isUseDateRange()) {
            if (request.getFromDate() == null || request.getToDate() == null) {
                throw new InvalidRequestException("From and To dates are required when a date range is used.");
            }
            if (request.getFromDate().isAfter(request.getToDate())) {
                // Legacy: "From Date Is Greater Than To Date!!"
                throw new InvalidRequestException("From date is after the To date.");
            }
            periodFrom = request.getFromDate();
            periodToExclusive = request.getToDate().plusDays(1);
        } else {
            periodFrom = cutoff;
            periodToExclusive = null;
        }

        // Twelve rolling months ending with the current month, as legacy sized it.
        YearMonth thisMonth = YearMonth.from(today);
        LocalDate ageingFrom = thisMonth.minusMonths(11).atDay(1);
        LocalDate ageingToExclusive = thisMonth.plusMonths(1).atDay(1);

        List<OutstandingInvoice> invoices =
                queries.findOutstandingInvoices(companyId, customerId, periodFrom, periodToExclusive);
        if (invoices.isEmpty()) {
            return StatementResult.builder()
                    .statements(List.of()).customerCount(0).lineCount(0)
                    .cutoffDate(cutoff).periodFrom(periodFrom)
                    .periodTo(periodToExclusive == null ? null : periodToExclusive.minusDays(1))
                    .ageingFrom(ageingFrom).ageingTo(ageingToExclusive.minusDays(1))
                    .build();
        }

        Map<Integer, List<OutstandingInvoice>> invoicesByCustomer = invoices.stream()
                .collect(Collectors.groupingBy(OutstandingInvoice::customerId, LinkedHashMap::new, Collectors.toList()));

        // Credit notes only matter against invoices that are on the statement —
        // the legacy post-filter, applied per knockoff rather than per note.
        Map<Integer, List<CreditNoteKnockoff>> knockoffsByCustomer = Map.of();
        if (request.isIncludeCreditNotes()) {
            var onStatement = invoices.stream().map(OutstandingInvoice::saleMasterId).collect(Collectors.toSet());
            knockoffsByCustomer = queries.findCreditNoteKnockoffs(companyId, customerId, cutoff).stream()
                    .filter(k -> onStatement.contains(k.saleMasterId()))
                    .collect(Collectors.groupingBy(CreditNoteKnockoff::customerId));
        }

        Map<Integer, Map<YearMonth, BigDecimal>> ageingByCustomer = new LinkedHashMap<>();
        for (AgeingRow row : queries.findAgeing(companyId, customerId, ageingFrom, ageingToExclusive)) {
            ageingByCustomer.computeIfAbsent(row.customerId(), id -> new LinkedHashMap<>())
                    .merge(YearMonth.of(row.year(), row.month()), row.balance(), BigDecimal::add);
        }

        // Legacy always took the opening as of the day before the cutoff, even
        // when a date range was chosen. Kept.
        Map<Integer, BigDecimal> openingByCustomer = queries
                .findOpeningBalances(companyId, customerId, cutoff.minusDays(1)).stream()
                .collect(Collectors.toMap(OpeningBalance::customerId, OpeningBalance::balance, (a, b) -> a));

        Map<Integer, CustomerHeader> headers = queries.findHeaders(companyId, invoicesByCustomer.keySet()).stream()
                .collect(Collectors.toMap(CustomerHeader::customerId, Function.identity(), (a, b) -> a));

        List<CustomerStatement> statements = new ArrayList<>();
        int lineCount = 0;
        for (Map.Entry<Integer, List<OutstandingInvoice>> entry : invoicesByCustomer.entrySet()) {
            int id = entry.getKey();
            CustomerStatement statement = assemble(
                    headers.get(id), entry.getValue(),
                    knockoffsByCustomer.getOrDefault(id, List.of()),
                    request.isIncludeCreditNotes(),
                    ageingByCustomer.getOrDefault(id, Map.of()),
                    openingByCustomer.getOrDefault(id, BigDecimal.ZERO),
                    thisMonth, today);
            statements.add(statement);
            lineCount += statement.getLines().size();
        }
        statements.sort(Comparator.comparing(CustomerStatement::getCustomerName, String.CASE_INSENSITIVE_ORDER));

        log.info("Customer statement built - company {}, customer {}, {} customers, {} lines",
                companyId, customerId, statements.size(), lineCount);

        return StatementResult.builder()
                .statements(statements)
                .customerCount(statements.size())
                .lineCount(lineCount)
                .cutoffDate(cutoff)
                .periodFrom(periodFrom)
                .periodTo(periodToExclusive == null ? null : periodToExclusive.minusDays(1))
                .ageingFrom(ageingFrom)
                .ageingTo(ageingToExclusive.minusDays(1))
                .build();
    }

    private CustomerStatement assemble(CustomerHeader header, List<OutstandingInvoice> invoices,
                                       List<CreditNoteKnockoff> knockoffs, boolean showCreditNotes,
                                       Map<YearMonth, BigDecimal> ageing, BigDecimal opening,
                                       YearMonth thisMonth, LocalDate today) {

        List<StatementLine> raw = new ArrayList<>();
        for (OutstandingInvoice inv : invoices) {
            // With credit notes shown, their share is carried by their own
            // lines below; without, the invoice line is net of them (legacy).
            BigDecimal debit = showCreditNotes
                    ? inv.amount().subtract(inv.received())
                    : inv.outstanding();
            raw.add(new StatementLine(StatementLine.Kind.INVOICE, inv.saleDate(), inv.documentNo(),
                    blankToNull(inv.reference()), blankToNull(inv.description()),
                    debit, BigDecimal.ZERO, BigDecimal.ZERO));
        }
        for (CreditNoteKnockoff k : knockoffs) {
            raw.add(new StatementLine(StatementLine.Kind.CREDIT_NOTE, k.creditDate(), k.creditNoteNo(),
                    null, k.invoiceNo(), BigDecimal.ZERO, k.knockedAmount(), BigDecimal.ZERO));
        }
        // Legacy ordered by CustomerName, BillDate; the id keeps same-day lines stable.
        raw.sort(Comparator.comparing(StatementLine::date, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(StatementLine::documentNo, Comparator.nullsLast(Comparator.naturalOrder())));

        // Crystal's @RuningBalance: on the customer's first record
        // OB := Opening + BillAmount - PaidAmount, then OB += Bill - Paid. The
        // opening balance (CustomerBalance() the day before the cutoff) is the
        // starting point, so the last line's balance is what the customer owes.
        BigDecimal running = opening == null ? BigDecimal.ZERO : opening;
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        List<StatementLine> lines = new ArrayList<>(raw.size());
        LocalDate latest = null;
        for (StatementLine line : raw) {
            running = running.add(line.debit()).subtract(line.credit());
            totalDebit = totalDebit.add(line.debit());
            totalCredit = totalCredit.add(line.credit());
            lines.add(line.withBalance(running));
            if (line.date() != null && (latest == null || line.date().isAfter(latest))) {
                latest = line.date();
            }
        }

        List<AgeingBucket> buckets = new ArrayList<>(12);
        for (int back = 11; back >= 0; back--) {
            YearMonth ym = thisMonth.minusMonths(back);
            buckets.add(new AgeingBucket(ym.getYear(), ym.getMonthValue(),
                    ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                    ym.format(LABEL),
                    ageing.getOrDefault(ym, BigDecimal.ZERO)));
        }

        CustomerHeader h = header != null ? header : new CustomerHeader(
                invoices.get(0).customerId(), invoices.get(0).customerName(),
                null, null, null, null, null, null, null, null, null, null, null, null);

        return CustomerStatement.builder()
                .customerId(h.customerId())
                .customerName(h.customerName())
                .address1(blankToNull(h.address1()))
                .address2(blankToNull(h.address2()))
                .address3(blankToNull(h.address3()))
                .phone(blankToNull(h.phone()))
                .attn(blankToNull(h.attn()))
                .accountCode(blankToNull(h.accountCode()))
                .terms(blankToNull(h.terms()))
                .currency(blankToNull(h.currency()))
                .emails(Stream.of(h.aEmail(), h.aEmail1(), h.oEmail(), h.oEmail1())
                        .map(CustomerStatementService::blankToNull)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList())
                .statementDate(today)
                .lines(lines)
                .totalDebit(totalDebit)
                .totalCredit(totalCredit)
                .closingBalance(running)
                .ageing(buckets)
                .openingBalance(opening)
                .overdueAmount(running)
                .overdueAsOf(latest)
                .build();
    }

    /** The company's cutoff from MasterSetting, else legacy's constant. */
    LocalDate cutoffFor(int companyId) {
        return settings.findFirstByCompanyRefIdAndVariableName(companyId, CUTOFF_SETTING)
                .map(s -> s.getSValue())
                .map(String::trim)
                .filter(v -> !v.isEmpty())
                .map(v -> {
                    try {
                        return LocalDate.parse(v);
                    } catch (RuntimeException ex) {
                        log.warn("MasterSetting {} for company {} is not an ISO date ({}); using {}",
                                CUTOFF_SETTING, companyId, v, LEGACY_CUTOFF);
                        return LEGACY_CUTOFF;
                    }
                })
                .orElse(LEGACY_CUTOFF);
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
