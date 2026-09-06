package my.maleva.api.module.invoice.einvoice;

import lombok.RequiredArgsConstructor;
import my.maleva.api.module.customer.entity.Customer;
import my.maleva.api.module.customer.repository.CustomerRepository;
import my.maleva.api.module.invoice.entity.SaleDetails;
import my.maleva.api.module.invoice.entity.SaleMaster;
import my.maleva.api.module.invoice.repository.SaleDetailsRepository;
import my.maleva.api.module.invoice.repository.SaleMasterRepository;
import my.maleva.api.module.itemmaster.entity.ItemMaster;
import my.maleva.api.module.itemmaster.repository.ItemMasterRepository;
import my.maleva.api.module.master.entity.Classification;
import my.maleva.api.module.master.entity.CountryMaster;
import my.maleva.api.module.master.entity.SymbolMaster;
import my.maleva.api.module.master.repository.ClassificationRepository;
import my.maleva.api.module.master.repository.CountryMasterRepository;
import my.maleva.api.module.master.repository.SymbolMasterRepository;
import my.maleva.api.module.umo.entity.Uom;
import my.maleva.api.module.umo.repository.UomRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Reads an invoice and everything joined to it into an {@link EInvoiceSnapshot}.
 *
 * <p>The legacy query was one SELECT with six INNER joins. Any missing master
 * row — a customer without a currency, an item without a UOM — silently made
 * lines (or the whole invoice) disappear from the result, and the push then
 * sent header totals that no longer matched the lines. Here every lookup is
 * explicit; a missing row becomes a named {@link EInvoiceProblem} and the
 * line is kept, so the validator can refuse with the reason instead of the
 * government receiving a document that does not add up.
 */
@Component
@RequiredArgsConstructor
public class EInvoiceSnapshotLoader {

    private final SaleMasterRepository saleMasters;
    private final SaleDetailsRepository saleDetails;
    private final CustomerRepository customers;
    private final SymbolMasterRepository symbols;
    private final CountryMasterRepository countries;
    private final ItemMasterRepository itemMasters;
    private final UomRepository uoms;
    private final ClassificationRepository classifications;

    /** The invoice, or empty when it does not exist or belongs to another company. */
    @Transactional(readOnly = true)
    public Optional<EInvoiceSnapshot> load(Integer invoiceId, Integer companyId) {
        SaleMaster invoice = saleMasters.findById(invoiceId).orElse(null);
        if (invoice == null || !Objects.equals(invoice.getCompanyRefId(), companyId)) {
            return Optional.empty();
        }

        List<EInvoiceProblem> problems = new ArrayList<>();
        String invoiceNo = invoice.getCNumberDisplay();

        EInvoiceSnapshot.Header header = EInvoiceSnapshot.Header.builder()
                .invoiceId(invoice.getId())
                .companyId(invoice.getCompanyRefId())
                .invoiceNo(invoiceNo)
                .saleDate(invoice.getSaleDate())
                .referenceNo(invoice.getRemarks1())
                .amount(EInvoiceMoney.of(invoice.getAmount()))
                .taxAmount(EInvoiceMoney.of(invoice.getTaxAmount()))
                .grossAmount(EInvoiceMoney.of(invoice.getGrossAmount()))
                .active(Integer.valueOf(1).equals(invoice.getActive()))
                .eInvoiceUid(invoice.getEInvoiceUid())
                .eInvoiceSubmissionUid(invoice.getEInvoiceSUid())
                .eInvoiceLongId(invoice.getEInvoiceLongId())
                .eInvoiceStatus(invoice.getEInvoiceStatus())
                .build();

        EInvoiceSnapshot.Customer customer = loadCustomer(invoice, invoiceNo, problems);
        List<EInvoiceSnapshot.Line> lines = loadLines(invoice, invoiceNo, problems);

        return Optional.of(EInvoiceSnapshot.builder()
                .header(header)
                .customer(customer)
                .lines(lines)
                .loadProblems(List.copyOf(problems))
                .build());
    }

    // ───────────────────────────────────────────────────────────── customer ──

    private EInvoiceSnapshot.Customer loadCustomer(SaleMaster invoice, String invoiceNo,
                                                   List<EInvoiceProblem> problems) {
        Customer customer = invoice.getCustomerRefId() == null
                ? null
                : customers.findById(invoice.getCustomerRefId()).orElse(null);
        if (customer == null) {
            problems.add(EInvoiceProblem.of("customer.missing",
                    "Invoice " + invoiceNo + ": its customer (id " + invoice.getCustomerRefId()
                            + ") no longer exists"));
            return null;
        }

        // Legacy joined the CUSTOMER's currency, not the invoice's SymbolRefId.
        // Kept: older invoice rows have no SymbolRefId of their own.
        String currency = null;
        if (customer.getSymbolRefid() != null) {
            currency = symbols.findById(customer.getSymbolRefid()).map(SymbolMaster::getSName).orElse(null);
        }
        if (currency == null) {
            problems.add(EInvoiceProblem.of("customer.currency.missing",
                    "Invoice " + invoiceNo + ": customer " + customer.getCustomerName()
                            + " has no currency set — choose one on the customer master and push again"));
        }

        String countryCode = "";
        if (customer.getCountryId() != null) {
            countryCode = countries.findById(customer.getCountryId()).map(CountryMaster::getCode).orElse("");
        }

        return EInvoiceSnapshot.Customer.builder()
                .customerId(customer.getId())
                .name(customer.getCustomerName())
                .tin(customer.getCustomerTin())
                .registrationNo(customer.getRegistrationNo())
                .phone(customer.getOPhone())
                .email(customer.getEmail())
                .city(customer.getCustomerCity())
                .postalZone(customer.getZipcode())
                .address1(customer.getAddress1())
                .state(customer.getState())
                .countryCode(countryCode == null ? "" : countryCode.trim())
                .currencyCode(normaliseCurrency(currency))
                .build();
    }

    /**
     * The legacy rule was an exact-match {@code "RM" → "MYR"}; {@code "rm"} or
     * {@code "RM "} went to LHDN verbatim and failed. Trim and upper-case first.
     */
    static String normaliseCurrency(String stored) {
        if (stored == null) {
            return null;
        }
        String code = stored.trim().toUpperCase(Locale.ROOT);
        return "RM".equals(code) ? "MYR" : code;
    }

    // ──────────────────────────────────────────────────────────────── lines ──

    private List<EInvoiceSnapshot.Line> loadLines(SaleMaster invoice, String invoiceNo,
                                                  List<EInvoiceProblem> problems) {
        List<SaleDetails> details = saleDetails.findBySaleMasterRefId(invoice.getId()).stream()
                .sorted(Comparator.comparing(SaleDetails::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        if (details.isEmpty()) {
            problems.add(EInvoiceProblem.of("lines.none", "Invoice " + invoiceNo + " has no lines"));
            return List.of();
        }

        Map<Integer, ItemMaster> items = itemMasters.findAllById(
                        details.stream().map(SaleDetails::getItemMasterRefId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(ItemMaster::getId, Function.identity()));

        Map<Integer, String> uomNames = uoms.findAllById(
                        items.values().stream().map(ItemMaster::getUomCode).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(Uom::getId, uom -> uom.getDescription() == null ? "" : uom.getDescription()));

        Map<Integer, Integer> classificationCodes = classifications.findAllById(
                        items.values().stream().map(ItemMaster::getSaleClassification).filter(Objects::nonNull).distinct().toList())
                .stream()
                // 0 is what legacy turned a missing classification into ("000"); treat it as missing.
                .filter(c -> c.getClassificationCode() != null && c.getClassificationCode() > 0)
                .collect(Collectors.toMap(Classification::getId, Classification::getClassificationCode));

        List<EInvoiceSnapshot.Line> lines = new ArrayList<>(details.size());
        int row = 0;
        for (SaleDetails detail : details) {
            row++;
            ItemMaster item = detail.getItemMasterRefId() == null ? null : items.get(detail.getItemMasterRefId());
            String uom = null;
            Integer classificationCode = null;
            String productCode = null;
            String productName = null;

            if (item == null) {
                // Legacy's INNER JOIN dropped this line and pushed the rest.
                problems.add(EInvoiceProblem.of("line.item.missing",
                        "Invoice " + invoiceNo + " line " + row + ": its product (id " + detail.getItemMasterRefId()
                                + ") no longer exists in the item master"));
            } else {
                productCode = item.getProdCode();
                productName = item.getPName();
                if (item.getUomCode() != null) {
                    uom = uomNames.get(item.getUomCode());
                }
                if (uom == null) {
                    problems.add(EInvoiceProblem.of("line.uom.missing",
                            "Invoice " + invoiceNo + " line " + row + " (" + productCode
                                    + "): the product's unit of measure no longer exists"));
                }
                if (item.getSaleClassification() != null) {
                    classificationCode = classificationCodes.get(item.getSaleClassification());
                }
            }

            lines.add(EInvoiceSnapshot.Line.builder()
                    .rowNumber(row)
                    .detailId(detail.getId())
                    .itemMasterRefId(detail.getItemMasterRefId())
                    .productCode(productCode)
                    .productName(productName)
                    .remarks(detail.getSdRemarks())
                    .quantity(EInvoiceMoney.quantity(detail.getItemQty()))
                    .unitPrice(EInvoiceMoney.of(detail.getSalesRate()))
                    .taxPercent(EInvoiceMoney.quantity(detail.getTaxPercent()))
                    .taxAmount(EInvoiceMoney.of(detail.getTaxAmount()))
                    .amount(EInvoiceMoney.of(detail.getAmount()))
                    .uom(uom)
                    .classificationCode(classificationCode)
                    .build());
        }
        return List.copyOf(lines);
    }
}
