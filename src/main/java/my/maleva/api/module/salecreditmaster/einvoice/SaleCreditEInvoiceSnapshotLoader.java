package my.maleva.api.module.salecreditmaster.einvoice;

import lombok.RequiredArgsConstructor;
import my.maleva.api.module.customer.entity.Customer;
import my.maleva.api.module.customer.repository.CustomerRepository;
import my.maleva.api.module.invoice.einvoice.EInvoiceMoney;
import my.maleva.api.module.invoice.einvoice.EInvoiceProblem;
import my.maleva.api.module.invoice.einvoice.EInvoiceSnapshot;
import my.maleva.api.module.invoice.einvoice.EInvoiceSnapshotLoader;
import my.maleva.api.module.invoice.entity.SaleMaster;
import my.maleva.api.module.invoice.repository.SaleMasterRepository;
import my.maleva.api.module.itemmaster.entity.ItemMaster;
import my.maleva.api.module.itemmaster.repository.ItemMasterRepository;
import my.maleva.api.module.master.entity.Classification;
import my.maleva.api.module.master.entity.CountryMaster;
import my.maleva.api.module.master.entity.SymbolMaster;
import my.maleva.api.module.master.repository.ClassificationRepository;
import my.maleva.api.module.master.repository.CountryMasterRepository;
import my.maleva.api.module.master.repository.SymbolMasterRepository;
import my.maleva.api.module.salecreditmaster.entity.SaleCreditDetails;
import my.maleva.api.module.salecreditmaster.entity.SaleCreditMaster;
import my.maleva.api.module.salecreditmaster.repository.SaleCreditDetailsRepository;
import my.maleva.api.module.salecreditmaster.repository.SaleCreditMasterRepository;
import my.maleva.api.module.umo.entity.Uom;
import my.maleva.api.module.umo.repository.UomRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Reads a credit note and everything joined to it into the same
 * {@link EInvoiceSnapshot} the sale invoice uses, so one validator and one
 * document builder serve both.
 *
 * <p>The legacy query for this ({@code EInvoiceCreditConvert}) was a single
 * SELECT with eight joins, four of them INNER. A customer with no payment
 * term, an item with no UOM, or a credit note not raised against an invoice
 * made the whole result empty, and the code then read {@code resultsm[0]} and
 * threw an index error the operator saw as "Technical Fault Contact Software
 * Vendor". Here every lookup is explicit and a missing row becomes a named
 * problem the operator can act on.
 *
 * @see EInvoiceSnapshotLoader the same job for a sale invoice
 */
@Component
@RequiredArgsConstructor
public class SaleCreditEInvoiceSnapshotLoader {

    private final SaleCreditMasterRepository creditNotes;
    private final SaleCreditDetailsRepository creditDetails;
    private final SaleMasterRepository saleMasters;
    private final CustomerRepository customers;
    private final SymbolMasterRepository symbols;
    private final CountryMasterRepository countries;
    private final ItemMasterRepository itemMasters;
    private final UomRepository uoms;
    private final ClassificationRepository classifications;

    /** The credit note, or empty when it does not exist or belongs to another company. */
    @Transactional(readOnly = true)
    public Optional<Loaded> load(Integer creditNoteId, Integer companyId) {
        SaleCreditMaster note = creditNotes.findById(creditNoteId).orElse(null);
        if (note == null || !Objects.equals(note.getCompanyRefId(), companyId)) {
            return Optional.empty();
        }

        List<EInvoiceProblem> problems = new ArrayList<>();
        String creditNoteNo = note.getCNumberDisplay();

        // The invoice this note corrects. Its number and LHDN UUID become the
        // credit note's billing reference; a note against an invoice that was
        // never e-invoiced still goes, carrying the number alone.
        SaleMaster invoice = note.getSaleMasterRefId() == null ? null
                : saleMasters.findById(note.getSaleMasterRefId()).orElse(null);
        if (invoice == null) {
            problems.add(EInvoiceProblem.of("creditnote.invoice.missing",
                    "Credit note " + creditNoteNo + " is not linked to an invoice, so LHDN cannot be told "
                            + "which document it corrects"));
        }

        EInvoiceSnapshot.Header header = EInvoiceSnapshot.Header.builder()
                .invoiceId(note.getId())
                .companyId(note.getCompanyRefId())
                .invoiceNo(creditNoteNo)
                .saleDate(note.getSaleDate())
                .referenceNo(invoice == null ? null : invoice.getCNumberDisplay())
                .amount(EInvoiceMoney.ofExact(note.getAmount()))
                .taxAmount(EInvoiceMoney.of(note.getTaxAmount()))
                .grossAmount(EInvoiceMoney.of(note.getGrossAmount()))
                // A credit note has no Active column; it exists or it does not.
                .active(true)
                .eInvoiceUid(note.getEInvoiceUid())
                .eInvoiceSubmissionUid(note.getEInvoiceSUid())
                .eInvoiceLongId(note.getEInvoiceLongId())
                .eInvoiceStatus(note.getEInvoiceStatus())
                .build();

        EInvoiceSnapshot snapshot = EInvoiceSnapshot.builder()
                .header(header)
                .customer(loadCustomer(note, creditNoteNo, problems))
                .lines(loadLines(note, creditNoteNo, problems))
                .loadProblems(List.copyOf(problems))
                .build();

        return Optional.of(new Loaded(snapshot,
                invoice == null ? null : invoice.getCNumberDisplay(),
                invoice == null ? null : invoice.getEInvoiceUid()));
    }

    // ───────────────────────────────────────────────────────────── customer ──

    private EInvoiceSnapshot.Customer loadCustomer(SaleCreditMaster note, String creditNoteNo,
                                                   List<EInvoiceProblem> problems) {
        Customer customer = note.getCustomerRefId() == null ? null
                : customers.findById(note.getCustomerRefId()).orElse(null);
        if (customer == null) {
            problems.add(EInvoiceProblem.of("customer.missing",
                    "Credit note " + creditNoteNo + ": its customer (id " + note.getCustomerRefId()
                            + ") no longer exists"));
            return null;
        }

        String currency = null;
        if (customer.getSymbolRefid() != null) {
            currency = symbols.findById(customer.getSymbolRefid()).map(SymbolMaster::getSName).orElse(null);
        }
        if (currency == null) {
            problems.add(EInvoiceProblem.of("customer.currency.missing",
                    "Credit note " + creditNoteNo + ": customer " + customer.getCustomerName()
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
                // "RM" is what the symbol master stores; LHDN wants ISO 4217.
                .currencyCode(EInvoiceSnapshotLoader.normaliseCurrency(currency))
                .build();
    }

    // ──────────────────────────────────────────────────────────────── lines ──

    private List<EInvoiceSnapshot.Line> loadLines(SaleCreditMaster note, String creditNoteNo,
                                                  List<EInvoiceProblem> problems) {
        List<SaleCreditDetails> details = creditDetails.findBySaleCreditMasterRefId(note.getId()).stream()
                .sorted(Comparator.comparing(SaleCreditDetails::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        if (details.isEmpty()) {
            problems.add(EInvoiceProblem.of("lines.none", "Credit note " + creditNoteNo + " has no lines"));
            return List.of();
        }

        Map<Integer, ItemMaster> items = itemMasters.findAllById(details.stream()
                        .map(SaleCreditDetails::getItemMasterRefId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(ItemMaster::getId, Function.identity()));

        Map<Integer, String> uomNames = uoms.findAllById(items.values().stream()
                        .map(ItemMaster::getUomCode).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(Uom::getId, uom -> uom.getDescription() == null ? "" : uom.getDescription()));

        Map<Integer, Integer> classificationCodes = classifications.findAllById(items.values().stream()
                        .map(ItemMaster::getSaleClassification).filter(Objects::nonNull).distinct().toList())
                .stream()
                // 0 is what legacy turned a missing classification into ("000"); treat it as missing.
                .filter(row -> row.getClassificationCode() != null && row.getClassificationCode() > 0)
                .collect(Collectors.toMap(Classification::getId, Classification::getClassificationCode));

        List<EInvoiceSnapshot.Line> lines = new ArrayList<>(details.size());
        int row = 0;
        for (SaleCreditDetails detail : details) {
            row++;
            ItemMaster item = detail.getItemMasterRefId() == null ? null : items.get(detail.getItemMasterRefId());
            String uom = null;
            Integer classificationCode = null;
            String productCode = null;
            String productName = null;

            if (item == null) {
                problems.add(EInvoiceProblem.of("line.item.missing",
                        "Credit note " + creditNoteNo + " line " + row + ": its product (id "
                                + detail.getItemMasterRefId() + ") no longer exists in the item master"));
            } else {
                productCode = item.getProdCode();
                productName = item.getPName();
                if (item.getUomCode() != null) {
                    uom = uomNames.get(item.getUomCode());
                }
                if (uom == null) {
                    problems.add(EInvoiceProblem.of("line.uom.missing",
                            "Credit note " + creditNoteNo + " line " + row + " (" + productCode
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
                    // Credit note lines have no per-line remarks column; the
                    // product name is the description, as the QNE push does too.
                    .remarks(null)
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

    /**
     * A loaded credit note plus the identity of the invoice it corrects.
     *
     * @param snapshot     what the validator and the builder work on
     * @param invoiceNo    the corrected invoice's number, null when unlinked
     * @param invoiceUuid  that invoice's LHDN UUID, null when it was never e-invoiced
     */
    public record Loaded(EInvoiceSnapshot snapshot, String invoiceNo, String invoiceUuid) {
    }
}
