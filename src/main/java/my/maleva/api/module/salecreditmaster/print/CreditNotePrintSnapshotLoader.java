package my.maleva.api.module.salecreditmaster.print;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.common.config.InvoicePrintProperties;
import my.maleva.api.integration.myinvois.MyInvoisQrCode;
import my.maleva.api.integration.myinvois.MyInvoisUrls;
import my.maleva.api.module.customer.entity.Customer;
import my.maleva.api.module.customer.repository.CustomerRepository;
import my.maleva.api.module.employee.entity.EmployeeMaster;
import my.maleva.api.module.employee.repository.EmployeeMasterRepository;
import my.maleva.api.module.invoice.einvoice.EInvoiceMoney;
import my.maleva.api.module.invoice.entity.SaleMaster;
import my.maleva.api.module.invoice.print.AmountInWords;
import my.maleva.api.module.invoice.repository.SaleMasterRepository;
import my.maleva.api.module.itemmaster.entity.ItemMaster;
import my.maleva.api.module.itemmaster.repository.ItemMasterRepository;
import my.maleva.api.module.master.entity.SymbolMaster;
import my.maleva.api.module.master.repository.SymbolMasterRepository;
import my.maleva.api.module.salecreditmaster.entity.SaleCreditDetails;
import my.maleva.api.module.salecreditmaster.entity.SaleCreditMaster;
import my.maleva.api.module.salecreditmaster.repository.SaleCreditDetailsRepository;
import my.maleva.api.module.salecreditmaster.repository.SaleCreditMasterRepository;
import my.maleva.api.module.umo.entity.Uom;
import my.maleva.api.module.umo.repository.UomRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Reads what the printed credit note shows — the port of the legacy
 * {@code Printfunction} SELECT and of the Crystal {@code CRCreditNote.rpt}
 * formulas.
 *
 * <p>Kept from legacy: the currency word for the amount in words is
 * {@code SymbolMaster.CName} (SINGAPORE, RINGGIT MALAYSIA …), the attention
 * name is {@code Customer.City}, the SALES MAN is the employee on the note,
 * and the "Tax Invoice No" line is the credited invoice's number followed by
 * its date in brackets.
 *
 * <p>Changed: NET TOTAL is the stored note amount and SUB TOTAL the sum of the
 * lines' tax-exclusive values, rather than Crystal adding tax to an amount
 * that already included it; and nothing here writes to the database or calls
 * LHDN — refreshing the LHDN status is {@link CreditNotePrintEInvoiceBackfill},
 * run before this.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreditNotePrintSnapshotLoader {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final SaleCreditMasterRepository creditNotes;
    private final SaleCreditDetailsRepository creditDetails;
    private final SaleMasterRepository saleMasters;
    private final CustomerRepository customers;
    private final SymbolMasterRepository symbols;
    private final EmployeeMasterRepository employees;
    private final ItemMasterRepository itemMasters;
    private final UomRepository uoms;
    private final MyInvoisUrls urls;
    private final MyInvoisQrCode qrCode;
    private final InvoicePrintProperties printProperties;
    private final CreditNotePrintProperties creditNoteProperties;

    @Transactional(readOnly = true)
    public Optional<CreditNotePrintSnapshot> load(Integer creditNoteId, Integer companyId) {
        SaleCreditMaster note = creditNotes.findById(creditNoteId).orElse(null);
        if (note == null || !Objects.equals(note.getCompanyRefId(), companyId)) {
            return Optional.empty();
        }

        Customer customer = note.getCustomerRefId() == null ? null
                : customers.findById(note.getCustomerRefId()).orElse(null);
        // Legacy printed the currency's WORD here (CName), not its code.
        String currencyWord = "";
        if (customer != null && customer.getSymbolRefid() != null) {
            currencyWord = symbols.findById(customer.getSymbolRefid())
                    .map(SymbolMaster::getCName).orElse("");
        }

        SaleMaster invoice = note.getSaleMasterRefId() == null ? null
                : saleMasters.findById(note.getSaleMasterRefId()).orElse(null);

        List<CreditNotePrintSnapshot.CreditNotePrintLine> lines = loadLines(note);
        BigDecimal subtotal = lines.stream()
                .map(line -> line.getNetAmount().subtract(line.getTaxAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netTotal = note.getAmount() == null ? BigDecimal.ZERO
                : note.getAmount().setScale(2, RoundingMode.HALF_UP);

        String shareUrl = shareUrl(note);
        return Optional.of(CreditNotePrintSnapshot.builder()
                .headerLines(printProperties.getHeaderLines())
                .heading(creditNoteProperties.getHeading())
                .creditNoteId(note.getId())
                .creditNoteNo(note.getCNumberDisplay())
                .creditNoteDate(note.getSaleDate() == null ? null : note.getSaleDate().toLocalDate())
                .customerName(customer == null ? "" : customer.getCustomerName())
                .customerAddress(customer == null ? "" : customer.getAddress1())
                .customerPhone(customer == null ? "" : customer.getOPhone())
                .attentionName(customer == null ? "" : customer.getCity())
                .salesMan(note.getEmployeeRefId() == null ? "" : employees.findById(note.getEmployeeRefId())
                        .map(EmployeeMaster::getEmployeeName).orElse(""))
                .invoiceNo(invoice == null ? "" : invoice.getCNumberDisplay())
                .invoiceDate(invoice == null || invoice.getSaleDate() == null ? null : invoice.getSaleDate().toLocalDate())
                .taxInvoiceNo(taxInvoiceNo(invoice))
                .subtotal(subtotal)
                .gstAmount(EInvoiceMoney.of(note.getTaxAmount()))
                .roundingAdjustment(EInvoiceMoney.of(note.getCoinage()))
                .netTotal(netTotal)
                .amountInWords(AmountInWords.of(currencyWord, netTotal))
                .eInvoiceUid(note.getEInvoiceUid())
                .eInvoiceLongId(note.getEInvoiceLongId())
                .eInvoiceStatus(note.getEInvoiceStatus())
                .eInvoiceValidatedAt(note.getEInvoicePushVDT())
                .eInvoiceShareUrl(shareUrl)
                .qrPng(renderQr(shareUrl))
                .notes(creditNoteProperties.getNotes())
                .generatedNoteLine1(creditNoteProperties.getGeneratedNoteLine1())
                .generatedNoteLine2(creditNoteProperties.getGeneratedNoteLine2())
                .lines(lines)
                .build());
    }

    /** Crystal: {@code INV000039783(28/02/2026)}; blank when the note has no invoice. */
    private static String taxInvoiceNo(SaleMaster invoice) {
        if (invoice == null || invoice.getCNumberDisplay() == null) {
            return "";
        }
        String number = invoice.getCNumberDisplay().trim();
        return invoice.getSaleDate() == null
                ? number
                : number + "(" + invoice.getSaleDate().toLocalDate().format(DATE) + ")";
    }

    private List<CreditNotePrintSnapshot.CreditNotePrintLine> loadLines(SaleCreditMaster note) {
        List<SaleCreditDetails> details = creditDetails.findBySaleCreditMasterRefId(note.getId()).stream()
                .sorted(Comparator.comparing(SaleCreditDetails::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        Map<Integer, ItemMaster> items = itemMasters.findAllById(details.stream()
                        .map(SaleCreditDetails::getItemMasterRefId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(ItemMaster::getId, Function.identity()));
        Map<Integer, String> uomNames = uoms.findAllById(items.values().stream()
                        .map(ItemMaster::getUomCode).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(Uom::getId, uom -> uom.getDescription() == null ? "" : uom.getDescription()));

        List<CreditNotePrintSnapshot.CreditNotePrintLine> lines = new ArrayList<>(details.size());
        int row = 0;
        for (SaleCreditDetails detail : details) {
            row++;
            ItemMaster item = detail.getItemMasterRefId() == null ? null : items.get(detail.getItemMasterRefId());
            BigDecimal quantity = orZero(EInvoiceMoney.quantity(detail.getItemQty()));
            BigDecimal unitPrice = orZero(EInvoiceMoney.of(detail.getSalesRate()));
            BigDecimal discount = orZero(EInvoiceMoney.of(detail.getDiscAmount()));
            BigDecimal taxPercent = orZero(EInvoiceMoney.quantity(detail.getTaxPercent()));
            BigDecimal taxAmount = orZero(EInvoiceMoney.of(detail.getTaxAmount()));
            // TAXABLE AMT. is what GST was charged on: the tax-exclusive value
            // of a taxed line, and zero on an untaxed one — which is what the
            // Crystal document prints for a 0% line.
            BigDecimal taxable = taxPercent.signum() == 0
                    ? BigDecimal.ZERO.setScale(2)
                    : quantity.multiply(unitPrice).subtract(discount).setScale(2, RoundingMode.HALF_UP);

            lines.add(CreditNotePrintSnapshot.CreditNotePrintLine.builder()
                    .rowNumber(row)
                    .productCode(item == null ? "" : item.getProdCode())
                    .description(item == null ? "" : item.getPName())
                    .quantity(quantity)
                    .uom(item == null || item.getUomCode() == null ? "" : uomNames.getOrDefault(item.getUomCode(), ""))
                    .unitPrice(unitPrice)
                    .discountAmount(discount)
                    .taxableAmount(taxable)
                    .taxPercent(taxPercent)
                    .taxAmount(taxAmount)
                    .netAmount(orZero(EInvoiceMoney.of(detail.getAmount())))
                    .build());
        }
        return lines;
    }

    private static BigDecimal orZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2) : value;
    }

    private String shareUrl(SaleCreditMaster note) {
        if (note.getEInvoiceUid() == null || note.getEInvoiceUid().isBlank()
                || note.getEInvoiceLongId() == null || note.getEInvoiceLongId().isBlank()) {
            return null;
        }
        return urls.documentShareLink(note.getEInvoiceUid(), note.getEInvoiceLongId());
    }

    private byte[] renderQr(String shareUrl) {
        if (shareUrl == null) {
            return null;
        }
        try {
            return qrCode.png(shareUrl);
        } catch (RuntimeException ex) {
            // The note prints with or without a picture of its link.
            log.warn("QR code for {} could not be rendered: {}", shareUrl, ex.getMessage());
            return null;
        }
    }
}
