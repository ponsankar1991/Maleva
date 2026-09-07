package my.maleva.api.module.salecreditmaster.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.customer.entity.Customer;
import my.maleva.api.module.customer.repository.CustomerRepository;
import my.maleva.api.module.employee.repository.EmployeeMasterRepository;
import my.maleva.api.module.invoice.entity.SaleMaster;
import my.maleva.api.module.invoice.repository.SaleMasterRepository;
import my.maleva.api.module.itemmaster.entity.ItemMaster;
import my.maleva.api.module.itemmaster.repository.ItemMasterRepository;
import my.maleva.api.module.master.entity.SequenceNoMaster;
import my.maleva.api.module.master.repository.SequenceNoMasterRepository;
import my.maleva.api.module.salecreditmaster.dto.SaleCreditBillDto;
import my.maleva.api.module.salecreditmaster.dto.SaleCreditDetailRequest;
import my.maleva.api.module.salecreditmaster.dto.SaleCreditEditDto;
import my.maleva.api.module.salecreditmaster.dto.SaleCreditEditLineDto;
import my.maleva.api.module.salecreditmaster.dto.SaleCreditInvoiceLookupDto;
import my.maleva.api.module.salecreditmaster.dto.SaleCreditKnockOffRequest;
import my.maleva.api.module.salecreditmaster.dto.SaleCreditSaveRequest;
import my.maleva.api.module.salecreditmaster.dto.SaleCreditSaveResponse;
import my.maleva.api.module.salecreditmaster.dto.SaleCreditSearchRequest;
import my.maleva.api.module.salecreditmaster.dto.SaleCreditViewDetailDto;
import my.maleva.api.module.salecreditmaster.dto.SaleCreditViewDto;
import my.maleva.api.module.salecreditmaster.dto.SaleCreditViewKnockOffDto;
import my.maleva.api.module.salecreditmaster.dto.SaleCreditViewRowDto;
import my.maleva.api.module.salecreditmaster.entity.SaleCreditDetails;
import my.maleva.api.module.salecreditmaster.entity.SaleCreditKnockOff;
import my.maleva.api.module.salecreditmaster.entity.SaleCreditMaster;
import my.maleva.api.module.salecreditmaster.repository.SaleCreditBillQueryRepository;
import my.maleva.api.module.salecreditmaster.repository.SaleCreditDetailsRepository;
import my.maleva.api.module.salecreditmaster.repository.SaleCreditKnockOffRepository;
import my.maleva.api.module.salecreditmaster.repository.SaleCreditMasterRepository;
import my.maleva.api.module.salecreditmaster.repository.SaleCreditViewQueryRepository;
import my.maleva.api.module.salecreditmaster.service.SaleCreditEntryService;
import my.maleva.api.module.umo.entity.Uom;
import my.maleva.api.module.umo.repository.UomRepository;
import my.maleva.api.module.user.repository.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The Sale Credit entry screen, in Java — the port of
 * {@code SP_SaleCreditMaster} and of {@code SaleCreditServices}.
 *
 * <p><b>Money is computed here, not accepted from the browser.</b> The screen
 * sends quantity, rate, tax percent and the knock-off amounts; every derived
 * figure — line tax, line amount, header gross/tax/amount/coinage and the
 * currency conversion — is recomputed with the screen's own arithmetic before
 * anything is stored. Legacy stored whatever JavaScript had put in the grid,
 * and its {@code Calculation()} added the line totals as strings
 * ({@code producttotal = producttotal + Amt} where {@code Amt} came from
 * {@code toFixed(2)}), so the Coinage it derived from that sum was nonsense on
 * every note with more than one line.
 *
 * <p>Other behaviour carried over from the procedure unchanged: a credit note
 * is replaced wholesale on edit (lines and knock-offs deleted and re-inserted),
 * the number is allocated only on insert and never changes, and the reference
 * checks on user, employee and invoice are the same checks.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SaleCreditEntryServiceImpl implements SaleCreditEntryService {

    /** The row of {@code SequenceNoMaster} that numbers credit notes. */
    private static final String SEQUENCE_NAME = "SaleCreditMaster";
    private static final String NUMBER_PREFIX = "CN";

    /** Knock-offs must add up to the note's value; one sen of slack for rounding. */
    private static final BigDecimal ONE_SEN = new BigDecimal("0.01");

    private final SaleCreditMasterRepository creditNotes;
    private final SaleCreditDetailsRepository creditDetails;
    private final SaleCreditKnockOffRepository knockOffs;
    private final SaleCreditBillQueryRepository billQueries;
    private final SaleCreditViewQueryRepository viewQueries;
    private final SequenceNoMasterRepository sequences;
    private final AppUserRepository appUsers;
    private final EmployeeMasterRepository employees;
    private final CustomerRepository customers;
    private final SaleMasterRepository saleMasters;
    private final ItemMasterRepository itemMasters;
    private final UomRepository uoms;

    // ─────────────────────────────────────────────────────────────── number ──

    @Override
    @Transactional(readOnly = true)
    public String nextCreditNoteNo(Integer companyId) {
        requireCompany(companyId);
        return format(nextSequenceNo(companyId));
    }

    /**
     * The next number this company will use.
     *
     * <p>Both sources are consulted on purpose. {@code SequenceNoMaster} is the
     * allocator, but the procedure only ever ran {@code UPDATE SequenceNoMaster}
     * — never an INSERT — so a company whose row was never created kept
     * reporting 0 and every credit note was numbered CN000000001. Taking the
     * higher of the allocator and the notes already stored makes the sequence
     * continue instead of restarting, and {@link #allocateNumber} creates the
     * missing row.
     */
    private int nextSequenceNo(Integer companyId) {
        Integer allocated = sequences.findMaxSequenceNoByCompanyAndSequenceName(companyId, SEQUENCE_NAME);
        Integer issued = creditNotes.findMaxCNumber(companyId);
        return Math.max(allocated == null ? 0 : allocated, issued == null ? 0 : issued) + 1;
    }

    private static String format(int sequenceNo) {
        return NUMBER_PREFIX + String.format("%09d", sequenceNo);
    }

    // ──────────────────────────────────────────────────────────────── bills ──

    @Override
    @Transactional(readOnly = true)
    public List<SaleCreditBillDto> customerBills(Integer companyId, Integer customerId, Integer excludeCreditNoteId) {
        requireCompany(companyId);
        if (customerId == null || customerId <= 0) {
            throw new InvalidRequestException("Please Select Customer");
        }
        return billQueries.selectCustomerBills(companyId, customerId, excludeCreditNoteId == null ? 0 : excludeCreditNoteId);
    }

    // ───────────────────────────────────────────────────────────────── save ──

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SaleCreditSaveResponse save(SaleCreditSaveRequest request, Integer headerCompanyId) {
        if (request == null) {
            return SaleCreditSaveResponse.failure("Empty credit note: nothing to save");
        }
        Integer companyId = request.getCompanyRefId() != null && request.getCompanyRefId() > 0
                ? request.getCompanyRefId() : headerCompanyId;
        if (companyId == null || companyId <= 0) {
            return SaleCreditSaveResponse.failure("Company ID (Comid) is required");
        }

        try {
            Customer customer = requireCustomer(companyId, request.getCustomerRefId());
            requireUser(companyId, request.getUserRefId());
            requireEmployee(companyId, request.getEmployeeRefId());
            SaleMaster invoice = requireInvoice(companyId, customer, request.getSaleMasterRefId());

            double currencyValue = request.getCurrencyValue() == null ? 0d : request.getCurrencyValue();
            if (currencyValue <= 0) {
                // gridemptycheck(): the note cannot be valued without the rate.
                throw new InvalidRequestException(
                        "The customer's currency rate is missing — re-select the customer and try again");
            }

            List<Line> lines = priceLines(request.getSaleCreditDetails());
            if (lines.isEmpty()) {
                throw new InvalidRequestException("Enter at least one product line");
            }
            Totals totals = Totals.of(lines);

            List<KnockOff> settlements = readKnockOffs(request.getSaleCreditKnockOffDetails());
            validateKnockOffs(companyId, request.getCustomerRefId(), currentId(request), settlements, totals.amount());

            boolean isEdit = currentId(request) > 0;
            SaleCreditMaster note = isEdit
                    ? loadForEdit(companyId, currentId(request))
                    : new SaleCreditMaster();

            LocalDateTime saleDate = parseDate(request.getSaleDate());
            note.setCompanyRefId(companyId);
            note.setCustomerRefId(request.getCustomerRefId());
            note.setUserRefId(zeroToNull(request.getUserRefId()));
            note.setSaleMasterRefId(invoice.getId());
            note.setSaleDate(saleDate);
            note.setRemarks(request.getRemarks());
            note.setCStatus(request.getCStatus() == null ? 0 : request.getCStatus());
            note.setCurrencyValue(currencyValue);
            note.setAmount(totals.amount());
            note.setGrossAmount(totals.amount().doubleValue());
            note.setTaxAmount(totals.tax().doubleValue());
            note.setDiscountAmount(0d);
            note.setPlusAmount(0d);
            note.setMinusAmount(0d);
            note.setCoinage(totals.coinage().doubleValue());
            note.setActualAmount(totals.amount().multiply(BigDecimal.valueOf(currencyValue))
                    .setScale(2, RoundingMode.HALF_UP).doubleValue());
            note.setModifiedDate(LocalDateTime.now());
            note.setModifiedBy("SYSTEM");
            // The procedure wrote EmployeeRefId once, on insert, and kept the
            // latest editor in LastEmployeeRefId. Same here.
            note.setLastEmployeeRefId(zeroToNull(request.getEmployeeRefId()));

            String creditNoteNo;
            if (isEdit) {
                creditNoteNo = note.getCNumberDisplay();
                creditNotes.save(note);
                creditDetails.deleteBySaleCreditMasterRefId(note.getId());
                knockOffs.deleteBySaleCreditMasterRefId(note.getId());
            } else {
                note.setEmployeeRefId(zeroToNull(request.getEmployeeRefId()));
                note.setCreatedDate(LocalDateTime.now());
                note.setCreatedBy("SYSTEM");
                note.setCNumber(0);
                note.setCNumberDisplay("");
                note = creditNotes.save(note);
                int sequenceNo = allocateNumber(companyId);
                creditNoteNo = format(sequenceNo);
                note.setCNumber(sequenceNo);
                note.setCNumberDisplay(creditNoteNo);
                note = creditNotes.save(note);
            }

            storeLines(note.getId(), lines, currencyValue);
            storeKnockOffs(companyId, note.getId(), settlements, currencyValue);

            log.info("{} credit note {} (id {}) for company {}: {} line(s), {} knock-off(s), amount {}",
                    isEdit ? "Updated" : "Created", creditNoteNo, note.getId(), companyId,
                    lines.size(), settlements.size(), totals.amount());
            return SaleCreditSaveResponse.success(note.getId(), creditNoteNo,
                    "SaleCredit " + creditNoteNo + (isEdit ? " Updated Successfully" : " Created Successfully"));
        } catch (InvalidRequestException refused) {
            // The screen shows this text; it is not a server fault.
            return SaleCreditSaveResponse.failure(refused.getMessage());
        }
    }

    private static int currentId(SaleCreditSaveRequest request) {
        return request.getId() == null ? 0 : request.getId();
    }

    private SaleCreditMaster loadForEdit(Integer companyId, Integer id) {
        SaleCreditMaster note = creditNotes.findById(id)
                .filter(row -> Objects.equals(row.getCompanyRefId(), companyId))
                .orElseThrow(() -> new InvalidRequestException("SaleCredit " + id + " was not found for this company"));
        // A note QNE already holds cannot be corrected from here: the two
        // ledgers would silently diverge. Legacy allowed the edit.
        if (note.getQneCode() != null && !note.getQneCode().isBlank()) {
            throw new InvalidRequestException("SaleCredit " + note.getCNumberDisplay() + " is already in QNE as "
                    + note.getQneCode() + " and cannot be changed here. Correct it in QNE instead.");
        }
        if (note.getEInvoiceUid() != null && !note.getEInvoiceUid().isBlank()) {
            throw new InvalidRequestException("SaleCredit " + note.getCNumberDisplay()
                    + " has been submitted to LHDN and cannot be changed. Cancel it with LHDN first.");
        }
        return note;
    }

    /**
     * Takes the sequence for this company, creating the row when it is
     * missing. Runs inside the save transaction, so two concurrent saves are
     * serialised by the row lock the update takes.
     */
    private int allocateNumber(Integer companyId) {
        int next = nextSequenceNo(companyId);
        SequenceNoMaster sequence = sequences.findByCompanyRefIdAndSequenceName(companyId, SEQUENCE_NAME)
                .orElseGet(() -> {
                    SequenceNoMaster fresh = new SequenceNoMaster();
                    fresh.setCompanyRefId(companyId);
                    fresh.setSequenceName(SEQUENCE_NAME);
                    return fresh;
                });
        sequence.setSequenceNo(next);
        sequence.setSequenceDate(LocalDateTime.now());
        sequences.save(sequence);
        return next;
    }

    // ───────────────────────────────────────────────────────── save: lines ──

    /**
     * The grid's arithmetic, re-run on the server:
     * <pre>
     *   tax    = round2(qty × rate × pct / 100)
     *   amount = round2(qty × rate + tax)      (tax-inclusive, as stored)
     * </pre>
     * Rows without a product, or with nothing to charge, are dropped — the
     * screen always carries one trailing blank row.
     */
    private List<Line> priceLines(List<SaleCreditDetailRequest> requested) {
        List<Line> lines = new ArrayList<>();
        if (requested == null) {
            return lines;
        }
        for (SaleCreditDetailRequest row : requested) {
            if (row == null || row.getItemMasterRefId() == null || row.getItemMasterRefId() <= 0) {
                continue;
            }
            BigDecimal qty = decimal(row.getItemQty());
            BigDecimal rate = decimal(row.getSalesRate());
            BigDecimal pct = decimal(row.getTaxPercent());
            if (qty.signum() <= 0) {
                continue; // an untouched row: the grid always carries a trailing blank
            }
            if (rate.signum() < 0 || pct.signum() < 0) {
                throw new InvalidRequestException("A product line has a negative rate or tax percentage");
            }
            BigDecimal net = qty.multiply(rate);
            BigDecimal tax = net.multiply(pct).divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal amount = net.add(tax).setScale(2, RoundingMode.HALF_UP);
            if (amount.signum() == 0) {
                continue; // legacy filtered rows whose Amount was 0
            }
            lines.add(new Line(row.getItemMasterRefId(), qty, rate, pct, tax, amount));
        }
        return lines;
    }

    private void storeLines(Integer creditNoteId, List<Line> lines, double currencyValue) {
        List<SaleCreditDetails> rows = new ArrayList<>(lines.size());
        for (Line line : lines) {
            rows.add(SaleCreditDetails.builder()
                    .saleCreditMasterRefId(creditNoteId)
                    .itemMasterRefId(line.itemId())
                    .itemQty(line.qty().doubleValue())
                    .salesRate(line.rate().doubleValue())
                    .taxPercent(line.taxPercent().doubleValue())
                    .taxAmount(line.tax().doubleValue())
                    .amount(line.amount().doubleValue())
                    .currencyValue(currencyValue)
                    .actualAmount(line.amount().multiply(BigDecimal.valueOf(currencyValue))
                            .setScale(2, RoundingMode.HALF_UP).doubleValue())
                    // The screen zeroed these on every keystroke; kept explicit
                    // so the columns are never left null.
                    .mrp(0d).purchaseRate(0d).landingCost(0d).discPer(0d).discAmount(0d).netSalesRate(0d)
                    .createdDate(LocalDateTime.now())
                    .modifiedDate(LocalDateTime.now())
                    .build());
        }
        creditDetails.saveAll(rows);
    }

    // ────────────────────────────────────────────────────── save: knock-offs ──

    private List<KnockOff> readKnockOffs(List<SaleCreditKnockOffRequest> requested) {
        List<KnockOff> settlements = new ArrayList<>();
        if (requested == null) {
            return settlements;
        }
        for (SaleCreditKnockOffRequest row : requested) {
            if (row == null || row.getSaleCreditAmount() == null) {
                continue;
            }
            BigDecimal amount = row.getSaleCreditAmount().setScale(2, RoundingMode.HALF_UP);
            if (amount.signum() == 0) {
                continue;
            }
            if (amount.signum() < 0) {
                throw new InvalidRequestException("A knock-off amount is negative");
            }
            Integer invoiceId = zeroToNull(row.getSaleMasterRefId());
            Integer openingId = zeroToNull(row.getCustomeropenRefId());
            if (invoiceId == null && openingId == null) {
                throw new InvalidRequestException("A knock-off row names neither an invoice nor an opening balance");
            }
            settlements.add(new KnockOff(invoiceId, openingId, amount));
        }
        return settlements;
    }

    /**
     * The knock-off grid must account for the whole value of the note
     * ({@code gridemptycheck1}: "Total Not Matching Please check"), and no
     * document may be credited beyond what it still owes.
     *
     * <p>The second rule is new. The grid clamped an over-typed amount to 0
     * client-side, which any other caller could bypass; an invoice credited
     * past its balance turns the customer ledger negative and the error only
     * surfaces months later in a statement.
     */
    private void validateKnockOffs(Integer companyId, Integer customerId, int creditNoteId,
                                   List<KnockOff> settlements, BigDecimal noteAmount) {
        if (settlements.isEmpty()) {
            throw new InvalidRequestException("Total Not Matching Please check — nothing is knocked off");
        }
        BigDecimal settled = settlements.stream()
                .map(KnockOff::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (settled.subtract(noteAmount).abs().compareTo(ONE_SEN) > 0) {
            throw new InvalidRequestException("Total Not Matching Please check — the knock-off total "
                    + settled.toPlainString() + " differs from the credit note total " + noteAmount.toPlainString());
        }

        List<SaleCreditBillDto> outstanding = billQueries.selectCustomerBills(companyId, customerId, creditNoteId);
        for (KnockOff settlement : settlements) {
            SaleCreditBillDto document = outstanding.stream()
                    .filter(bill -> settlement.invoiceId() != null
                            ? settlement.invoiceId().equals(bill.getSaleMasterRefId())
                            : settlement.openingBalanceId().equals(bill.getCustomeropenRefId()))
                    .findFirst()
                    .orElseThrow(() -> new InvalidRequestException(settlement.label()
                            + " is not an outstanding document of this customer — reload the bills and try again"));
            BigDecimal balance = document.getBalance() == null ? BigDecimal.ZERO : document.getBalance();
            if (settlement.amount().subtract(balance).compareTo(ONE_SEN) > 0) {
                throw new InvalidRequestException(settlement.label() + " has only "
                        + balance.toPlainString() + " outstanding; " + settlement.amount().toPlainString()
                        + " cannot be credited against it");
            }
        }
    }

    private void storeKnockOffs(Integer companyId, Integer creditNoteId, List<KnockOff> settlements, double currencyValue) {
        List<SaleCreditKnockOff> rows = new ArrayList<>(settlements.size());
        for (KnockOff settlement : settlements) {
            rows.add(SaleCreditKnockOff.builder()
                    .companyRefId(companyId)
                    .saleCreditMasterRefId(creditNoteId)
                    .saleMasterRefId(settlement.invoiceId())
                    .customerOpenRefId(settlement.openingBalanceId())
                    .saleCreditAmount(settlement.amount())
                    .currencyValue(currencyValue)
                    .actualAmount(settlement.amount().multiply(BigDecimal.valueOf(currencyValue))
                            .setScale(2, RoundingMode.HALF_UP).doubleValue())
                    .createdDate(LocalDateTime.now())
                    .build());
        }
        knockOffs.saveAll(rows);
    }

    // ───────────────────────────────────────────────────────────────── edit ──

    @Override
    @Transactional(readOnly = true)
    public Optional<SaleCreditEditDto> edit(Integer companyId, Integer id, Integer creditNoteNumber) {
        requireCompany(companyId);
        SaleCreditMaster note = null;
        if (creditNoteNumber != null && creditNoteNumber != 0) {
            note = creditNotes.findByCompanyRefIdAndCNumber(companyId, creditNoteNumber).orElse(null);
        } else if (id != null && id > 0) {
            note = creditNotes.findById(id)
                    .filter(row -> Objects.equals(row.getCompanyRefId(), companyId)).orElse(null);
        }
        if (note == null) {
            return Optional.empty();
        }

        // The customer's whole outstanding list, this note excluded, with this
        // note's amounts merged back onto the documents it settles. Legacy
        // matched invoice knock-offs only, so an opening-balance knock-off was
        // silently lost every time the note was opened and saved again.
        List<SaleCreditBillDto> bills =
                billQueries.selectCustomerBills(companyId, note.getCustomerRefId(), note.getId());
        for (SaleCreditKnockOff saved : knockOffs.findBySaleCreditMasterRefId(note.getId())) {
            for (SaleCreditBillDto bill : bills) {
                boolean sameInvoice = saved.getSaleMasterRefId() != null
                        && saved.getSaleMasterRefId().equals(bill.getSaleMasterRefId());
                boolean sameOpening = saved.getSaleMasterRefId() == null && saved.getCustomerOpenRefId() != null
                        && saved.getCustomerOpenRefId().equals(bill.getCustomeropenRefId());
                if (sameInvoice || sameOpening) {
                    bill.setSaleCreditAmount(saved.getSaleCreditAmount() == null
                            ? BigDecimal.ZERO : saved.getSaleCreditAmount());
                    bill.setSdId(saved.getId());
                    if (saved.getCurrencyValue() != null) {
                        bill.setCurrencyValue(BigDecimal.valueOf(saved.getCurrencyValue()));
                    }
                    if (saved.getActualAmount() != null) {
                        bill.setActualAmount(BigDecimal.valueOf(saved.getActualAmount()));
                    }
                }
            }
        }

        LocalDateTime date = note.getSaleDate();
        return Optional.of(SaleCreditEditDto.builder()
                .id(note.getId())
                .companyRefId(note.getCompanyRefId())
                .customerRefId(note.getCustomerRefId())
                .customerName(customers.findById(note.getCustomerRefId())
                        .map(Customer::getCustomerName).orElse(""))
                .employeeRefId(note.getEmployeeRefId())
                .userRefId(note.getUserRefId())
                .cNumber(note.getCNumber())
                .cNumberDisplay(note.getCNumberDisplay())
                .saleDate(date == null ? "" : date.toLocalDate().toString())
                .sSaleDate(date == null ? "" : date.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .saleMasterRefId(note.getSaleMasterRefId())
                .saleNo(note.getSaleMasterRefId() == null ? "" : saleMasters.findById(note.getSaleMasterRefId())
                        .map(SaleMaster::getCNumberDisplay).orElse(""))
                .amount(note.getAmount())
                .grossAmount(money(note.getGrossAmount()))
                .taxAmount(money(note.getTaxAmount()))
                .coinage(money(note.getCoinage()))
                // CurrencyValue is float32 at rest, so the raw double reads
                // 3.0799999237; round to what was actually entered.
                .currencyValue(note.getCurrencyValue() == null ? null
                        : Math.round(note.getCurrencyValue() * 10000d) / 10000d)
                .actualAmount(money(note.getActualAmount()))
                .remarks(note.getRemarks())
                .cStatus(note.getCStatus())
                .qneCode(note.getQneCode())
                .qneId(note.getQneId())
                .eInvoiceUid(note.getEInvoiceUid())
                .eInvoiceStatus(note.getEInvoiceStatus())
                .details(loadLines(note.getId()))
                .knockOffs(bills)
                .build());
    }

    private List<SaleCreditEditLineDto> loadLines(Integer creditNoteId) {
        List<SaleCreditDetails> rows = creditDetails.findBySaleCreditMasterRefId(creditNoteId).stream()
                .sorted(Comparator.comparing(SaleCreditDetails::getId,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        Map<Integer, ItemMaster> items = itemMasters.findAllById(rows.stream()
                        .map(SaleCreditDetails::getItemMasterRefId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(ItemMaster::getId, Function.identity()));
        Map<Integer, String> uomNames = uoms.findAllById(items.values().stream()
                        .map(ItemMaster::getUomCode).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(Uom::getId, uom -> uom.getDescription() == null ? "" : uom.getDescription()));

        List<SaleCreditEditLineDto> lines = new ArrayList<>(rows.size());
        for (SaleCreditDetails row : rows) {
            ItemMaster item = row.getItemMasterRefId() == null ? null : items.get(row.getItemMasterRefId());
            lines.add(SaleCreditEditLineDto.builder()
                    .sdId(row.getId())
                    .itemMasterRefId(row.getItemMasterRefId())
                    // A line whose product was deleted keeps its figures and
                    // shows blank text, rather than vanishing as legacy's
                    // INNER JOIN made it do.
                    .productCode(item == null ? "" : item.getProdCode())
                    .productName(item == null ? "" : item.getPName())
                    .uom(item == null || item.getUomCode() == null ? "" : uomNames.getOrDefault(item.getUomCode(), ""))
                    .itemQty(money(row.getItemQty()))
                    .salesRate(money(row.getSalesRate()))
                    .taxPercent(money(row.getTaxPercent()))
                    .taxAmount(money(row.getTaxAmount()))
                    .amount(money(row.getAmount()))
                    .currencyValue(money(row.getCurrencyValue()))
                    .actualAmount(money(row.getActualAmount()))
                    .build());
        }
        return lines;
    }

    // ─────────────────────────────────────────────────────────────── search ──

    @Override
    @Transactional(readOnly = true)
    public SaleCreditViewDto search(SaleCreditSearchRequest request) {
        requireCompany(request.getCompanyId());
        List<SaleCreditViewRowDto> rows = viewQueries.selectCreditNotes(request);
        if (rows.isEmpty()) {
            return SaleCreditViewDto.builder()
                    .creditNotes(List.of()).details(List.of()).knockOffs(List.of())
                    .totalAmount(BigDecimal.ZERO).count(0)
                    .build();
        }
        List<SaleCreditViewDetailDto> details = viewQueries.selectDetails(request);
        List<SaleCreditViewKnockOffDto> settlements = viewQueries.selectKnockOffs(request);
        BigDecimal total = rows.get(0).getTotalAmount() == null ? BigDecimal.ZERO : rows.get(0).getTotalAmount();
        return SaleCreditViewDto.builder()
                .creditNotes(rows)
                .details(details)
                .knockOffs(settlements)
                .totalAmount(total)
                .count(rows.size())
                .build();
    }

    // ─────────────────────────────────────────────────────────────── delete ──

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String delete(Integer id, Integer companyId) {
        requireCompany(companyId);
        if (id == null || id <= 0) {
            throw new InvalidRequestException("SaleCredit id is required");
        }
        SaleCreditMaster note = creditNotes.findById(id)
                .filter(row -> Objects.equals(row.getCompanyRefId(), companyId))
                .orElseThrow(() -> new InvalidRequestException("SaleCredit " + id + " was not found for this company"));

        // Legacy ran a bare "Delete SaleCreditMaster where Id=" — with no
        // company check, on notes QNE or LHDN already held, and leaving the
        // detail and knock-off rows behind. Those orphaned knock-offs kept
        // reducing the balance of invoices whose credit note no longer existed.
        if (note.getQneCode() != null && !note.getQneCode().isBlank()) {
            throw new InvalidRequestException("SaleCredit " + note.getCNumberDisplay() + " is already in QNE as "
                    + note.getQneCode() + " and cannot be deleted here. Cancel it in QNE first.");
        }
        if (note.getEInvoiceUid() != null && !note.getEInvoiceUid().isBlank()) {
            throw new InvalidRequestException("SaleCredit " + note.getCNumberDisplay()
                    + " has been submitted to LHDN and cannot be deleted. Cancel it with LHDN first.");
        }

        creditDetails.deleteBySaleCreditMasterRefId(note.getId());
        knockOffs.deleteBySaleCreditMasterRefId(note.getId());
        creditNotes.delete(note);
        log.info("Deleted credit note {} ({}) of company {}", note.getId(), note.getCNumberDisplay(), companyId);
        return "SaleCredit " + note.getCNumberDisplay() + " Deleted Successfully";
    }

    // ─────────────────────────────────────────────────────── invoice lookup ──

    @Override
    @Transactional(readOnly = true)
    public Optional<SaleCreditInvoiceLookupDto> lookupInvoice(Integer companyId, String invoiceNo, Integer invoiceId) {
        requireCompany(companyId);
        boolean byNumber = invoiceNo != null && !invoiceNo.isBlank();
        if (!byNumber && (invoiceId == null || invoiceId <= 0)) {
            throw new InvalidRequestException("Please Enter Job No !!!.");
        }
        // By number for the box the operator types in; by id for the ?Id= link
        // the Sale Invoice screen opens this screen with.
        Optional<SaleMaster> found = byNumber
                ? saleMasters.findByCompanyAndNumberDisplay(companyId, invoiceNo.trim(), 1).stream().findFirst()
                : saleMasters.findById(invoiceId)
                        .filter(row -> Objects.equals(row.getCompanyRefId(), companyId))
                        .filter(row -> Integer.valueOf(1).equals(row.getActive()));
        return found
                .map(invoice -> SaleCreditInvoiceLookupDto.builder()
                        .id(invoice.getId())
                        .invoiceNo(invoice.getCNumberDisplay())
                        .invoiceDate(invoice.getSaleDate() == null ? "" : invoice.getSaleDate().toLocalDate()
                                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                        .customerRefId(invoice.getCustomerRefId())
                        .customerName(invoice.getCustomerRefId() == null ? "" : customers.findById(invoice.getCustomerRefId())
                                .map(Customer::getCustomerName).orElse(""))
                        .amount(money(invoice.getAmount()))
                        .build());
    }

    // ─────────────────────────────────────────────────── reference checks ──

    private static void requireCompany(Integer companyId) {
        if (companyId == null || companyId <= 0) {
            throw new InvalidRequestException("Company is required");
        }
    }

    private Customer requireCustomer(Integer companyId, Integer customerId) {
        if (customerId == null || customerId <= 0) {
            throw new InvalidRequestException("Please Select Customer");
        }
        return customers.findById(customerId)
                .filter(customer -> Objects.equals(customer.getCompanyRefId(), companyId))
                .orElseThrow(() -> new InvalidRequestException("Customer " + customerId + " was not found for this company"));
    }

    private void requireUser(Integer companyId, Integer userId) {
        if (userId != null && userId != 0 && !appUsers.existsByIdAndCompanyRefIdAndActive(userId, companyId, 1)) {
            throw new InvalidRequestException("Login User Not Found Issue id " + userId);
        }
    }

    private void requireEmployee(Integer companyId, Integer employeeId) {
        if (employeeId != null && employeeId != 0
                && !employees.existsByIdAndCompanyRefIdAndActive(employeeId, companyId, 1)) {
            throw new InvalidRequestException("Employee Not Found Issue id " + employeeId);
        }
    }

    /**
     * The invoice the credit note is raised against. The screen refuses to save
     * without it ("Please Enter Job No !!!.") and the procedure checked it
     * exists and is active; both are kept.
     *
     * <p>Added: the invoice must belong to the same customer. Nothing stopped
     * a note that knocked off customer A's bills from pointing at customer B's
     * invoice, and that reference is what the QNE credit note and the LHDN
     * billing reference are built from.
     */
    private SaleMaster requireInvoice(Integer companyId, Customer customer, Integer invoiceId) {
        if (invoiceId == null || invoiceId <= 0) {
            throw new InvalidRequestException("Please Enter Job No !!!.");
        }
        SaleMaster invoice = saleMasters.findById(invoiceId)
                .filter(row -> Objects.equals(row.getCompanyRefId(), companyId))
                .filter(row -> Integer.valueOf(1).equals(row.getActive()))
                .orElseThrow(() -> new InvalidRequestException("SaleMaster Not Found Issue id " + invoiceId));
        if (!Objects.equals(invoice.getCustomerRefId(), customer.getId())) {
            throw new InvalidRequestException("Invoice " + invoice.getCNumberDisplay()
                    + " belongs to a different customer than the one selected");
        }
        return invoice;
    }

    // ────────────────────────────────────────────────────────────── helpers ──

    private static Integer zeroToNull(Integer value) {
        return value == null || value == 0 ? null : value;
    }

    private static BigDecimal decimal(Double value) {
        return value == null ? BigDecimal.ZERO : BigDecimal.valueOf(value);
    }

    private static BigDecimal money(Double value) {
        return value == null ? null : BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal money(BigDecimal value) {
        return value == null ? null : value.setScale(2, RoundingMode.HALF_UP);
    }

    /** Accepts yyyy-MM-dd and the dd/MM/yyyy the legacy screen sent. */
    private static LocalDateTime parseDate(String value) {
        if (value == null || value.isBlank()) {
            return LocalDate.now().atStartOfDay();
        }
        String text = value.trim();
        try {
            if (text.length() >= 10 && text.charAt(4) == '-') {
                return LocalDate.parse(text.substring(0, 10)).atStartOfDay();
            }
            if (text.length() == 10 && text.charAt(2) == '/') {
                return LocalDate.parse(text, DateTimeFormatter.ofPattern("dd/MM/yyyy")).atStartOfDay();
            }
        } catch (DateTimeParseException ignored) {
            // fall through to the refusal below
        }
        throw new InvalidRequestException("SaleCredit date '" + value + "' must be yyyy-MM-dd or dd/MM/yyyy");
    }

    /** One priced product line. */
    private record Line(Integer itemId, BigDecimal qty, BigDecimal rate,
                        BigDecimal taxPercent, BigDecimal tax, BigDecimal amount) {
    }

    /** One document the note is knocked off against. */
    private record KnockOff(Integer invoiceId, Integer openingBalanceId, BigDecimal amount) {

        String label() {
            return invoiceId != null ? "Invoice reference " + invoiceId : "The customer's opening balance";
        }
    }

    /** The header figures, derived from the lines. */
    private record Totals(BigDecimal amount, BigDecimal tax, BigDecimal coinage) {

        static Totals of(List<Line> lines) {
            BigDecimal amount = BigDecimal.ZERO;
            BigDecimal tax = BigDecimal.ZERO;
            for (Line line : lines) {
                amount = amount.add(line.amount());
                tax = tax.add(line.tax());
            }
            amount = amount.setScale(2, RoundingMode.HALF_UP);
            // Rounding to the ringgit, as the screen's RoundoffPaise = "2" did.
            BigDecimal coinage = amount.setScale(0, RoundingMode.HALF_UP).subtract(amount);
            return new Totals(amount, tax.setScale(2, RoundingMode.HALF_UP), coinage);
        }
    }
}
