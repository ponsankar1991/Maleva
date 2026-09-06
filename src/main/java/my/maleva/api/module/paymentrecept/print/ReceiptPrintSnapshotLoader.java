package my.maleva.api.module.paymentrecept.print;

import lombok.RequiredArgsConstructor;
import my.maleva.api.common.config.ReceiptPrintProperties;
import my.maleva.api.module.customer.entity.Customer;
import my.maleva.api.module.customer.repository.CustomerRepository;
import my.maleva.api.module.invoice.entity.SaleMaster;
import my.maleva.api.module.invoice.print.AmountInWords;
import my.maleva.api.module.invoice.repository.SaleMasterRepository;
import my.maleva.api.module.master.entity.SymbolMaster;
import my.maleva.api.module.master.repository.SymbolMasterRepository;
import my.maleva.api.module.paymentrecept.entity.Receipt;
import my.maleva.api.module.paymentrecept.entity.ReceiptDetails;
import my.maleva.api.module.paymentrecept.repository.ReceiptDetailsRepository;
import my.maleva.api.module.paymentrecept.repository.ReceiptRepository;
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
 * Reads what the printed receipt voucher shows — the port of the legacy
 * {@code ReceiptServices.SelectReceiptReportData} SELECT that fed
 * {@code CRReceipt2.rpt}.
 *
 * <p>Kept from legacy: the contact line is {@code Customer.City}, the phone
 * is {@code OPhone}, the A/C code is the customer's QNE {@code CompanyCode},
 * the description is the receipt's remarks, a line's description is the
 * loading vessel or, when blank, the off vessel, the amount in words is
 * prefixed with the currency <em>name</em> ({@code SymbolMaster.CName},
 * "SINGAPORE"), and every row is left-joined so a receipt with no invoice
 * lines still prints. Changed: an opening-balance line prints as {@code OB}
 * instead of the constant {@code INV} legacy stamped on every row, because a
 * blank document number under "INV" told the customer nothing.
 */
@Component
@RequiredArgsConstructor
public class ReceiptPrintSnapshotLoader {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ReceiptRepository receipts;
    private final ReceiptDetailsRepository receiptDetails;
    private final CustomerRepository customers;
    private final SymbolMasterRepository symbols;
    private final SaleMasterRepository saleMasters;
    private final ReceiptPrintProperties printProperties;

    @Transactional(readOnly = true)
    public Optional<ReceiptPrintSnapshot> load(Integer receiptId, Integer companyId) {
        Receipt receipt = receipts.findById(receiptId).orElse(null);
        if (receipt == null || !Objects.equals(receipt.getCompanyRefId(), companyId)) {
            return Optional.empty();
        }

        Customer customer = receipt.getCustomerRefId() == null ? null
                : customers.findById(receipt.getCustomerRefId()).orElse(null);
        String currencyName = "";
        String currencySymbol = "RM";
        if (customer != null && customer.getSymbolRefid() != null) {
            Optional<SymbolMaster> symbol = symbols.findById(customer.getSymbolRefid());
            currencyName = symbol.map(SymbolMaster::getCName).map(String::trim).orElse("");
            currencySymbol = symbol.map(SymbolMaster::getSName).filter(s -> s != null && !s.isBlank())
                    .map(String::trim).orElse("RM");
        }

        BigDecimal amount = money(receipt.getAmount());

        return Optional.of(ReceiptPrintSnapshot.builder()
                .headerLines(List.copyOf(printProperties.getHeaderLines()))
                .heading(orEmpty(printProperties.getHeading()))
                .nbNote(orEmpty(printProperties.getNbNote()))
                .generatedNote(orEmpty(printProperties.getGeneratedNote()))
                .receiptId(receipt.getId())
                .receiptNo(orEmpty(receipt.getCNumberDisplay()))
                .receiptDate(receipt.getReceiptDate() == null ? null : receipt.getReceiptDate().toLocalDate())
                .chequeNo(orEmpty(receipt.getRefNumber()))
                .customerName(customer == null ? "" : orEmpty(customer.getCustomerName()))
                .customerAddress(customer == null ? "" : orEmpty(customer.getAddress1()))
                .customerPhone(customer == null ? "" : orEmpty(customer.getOPhone()))
                .attentionName(customer == null ? "" : orEmpty(customer.getCity()))
                .accountCode(customer == null ? "" : orEmpty(customer.getCompanyCode()))
                .accountName(customer == null ? "" : orEmpty(customer.getCustomerName()))
                .description(orEmpty(receipt.getRemarks()))
                .amount(amount)
                .currencyName(currencyName)
                .currencySymbol(currencySymbol)
                .subTotal(amount)
                .roundingAdjustment(BigDecimal.ZERO.setScale(2))
                .netTotal(amount)
                .amountInWords(AmountInWords.of(currencyName, amount))
                .lines(loadLines(receipt))
                .build());
    }

    private List<ReceiptPrintSnapshot.ReceiptPrintLine> loadLines(Receipt receipt) {
        List<ReceiptDetails> rows = new ArrayList<>(receiptDetails.findByReceiptRefId(receipt.getId()));
        rows.sort(Comparator.comparing(ReceiptDetails::getId, Comparator.nullsLast(Comparator.naturalOrder())));

        Map<Integer, SaleMaster> invoices = saleMasters.findAllById(
                        rows.stream().map(ReceiptDetails::getSaleMasterRefId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(SaleMaster::getId, Function.identity(), (a, b) -> a));

        List<ReceiptPrintSnapshot.ReceiptPrintLine> lines = new ArrayList<>();
        int n = 0;
        for (ReceiptDetails row : rows) {
            SaleMaster invoice = row.getSaleMasterRefId() == null ? null : invoices.get(row.getSaleMasterRefId());
            ReceiptPrintSnapshot.ReceiptPrintLine.ReceiptPrintLineBuilder line = ReceiptPrintSnapshot.ReceiptPrintLine.builder()
                    .rowNumber(++n)
                    .paidAmount(money(row.getReceiptAmount()));
            if (invoice != null) {
                String loading = orEmpty(invoice.getLoadingvesselname()).trim();
                String off = orEmpty(invoice.getOffvesselname()).trim();
                line.docType("INV")
                        .docNo(orEmpty(invoice.getCNumberDisplay()))
                        .docDate(invoice.getSaleDate() == null ? "" : invoice.getSaleDate().toLocalDate().format(DATE))
                        // Crystal: case when Loadingvesselname = '' then Offvesselname else Loadingvesselname end
                        .description(loading.isEmpty() ? off : loading)
                        .originalAmount(money(invoice.getAmount()));
            } else if (row.getCustomerOpenRefId() != null) {
                line.docType("OB").docNo("").docDate("").description("OPENING BALANCE")
                        .originalAmount(null);
            } else {
                line.docType("INV").docNo("").docDate("").description("").originalAmount(null);
            }
            lines.add(line.build());
        }
        return lines;
    }

    private static BigDecimal money(Number value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2);
        }
        BigDecimal decimal = value instanceof BigDecimal b ? b : BigDecimal.valueOf(value.doubleValue());
        return decimal.setScale(2, RoundingMode.HALF_UP);
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
