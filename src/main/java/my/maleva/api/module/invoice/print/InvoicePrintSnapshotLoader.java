package my.maleva.api.module.invoice.print;

import lombok.RequiredArgsConstructor;
import my.maleva.api.common.config.InvoicePrintProperties;
import my.maleva.api.integration.myinvois.MyInvoisQrCode;
import my.maleva.api.integration.myinvois.MyInvoisUrls;
import my.maleva.api.module.billing.bill.entity.DoMaster;
import my.maleva.api.module.billing.bill.repository.DoMasterRepository;
import my.maleva.api.module.customer.entity.Customer;
import my.maleva.api.module.customer.repository.CustomerRepository;
import my.maleva.api.module.invoice.einvoice.EInvoiceMoney;
import my.maleva.api.module.invoice.entity.SaleDetails;
import my.maleva.api.module.invoice.entity.SaleMaster;
import my.maleva.api.module.invoice.repository.SaleDetailsRepository;
import my.maleva.api.module.invoice.repository.SaleMasterReferenceRepository;
import my.maleva.api.module.invoice.repository.SaleMasterRepository;
import my.maleva.api.module.itemmaster.entity.ItemMaster;
import my.maleva.api.module.itemmaster.repository.ItemMasterRepository;
import my.maleva.api.module.master.entity.PaymentTermsMaster;
import my.maleva.api.module.master.entity.SymbolMaster;
import my.maleva.api.module.master.entity.TaxMaster;
import my.maleva.api.module.master.repository.SymbolMasterRepository;
import my.maleva.api.module.master.repository.TaxMasterRepository;
import my.maleva.api.module.payment.repository.PaymentTermsMasterRepository;
import my.maleva.api.module.umo.entity.Uom;
import my.maleva.api.module.umo.repository.UomRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Reads what the printed invoice shows — the port of the legacy
 * {@code Printfunction} SELECTs and of the Crystal formula fields.
 *
 * <p>Kept from legacy: the job block is blanked when the invoice covers more
 * than one sale order; the truck name is the newest RTI that carried the job;
 * the currency symbol defaults to RM; the vessel rows print NIL when empty.
 * Changed: an invoice with no sale order pointing at it still prints (legacy
 * INNER-joined SaleOrderMaster and answered "Not found" for 46 real
 * invoices); an inactive invoice prints too; nothing here polls LHDN or
 * writes to the database; and NET TOTAL is the stored invoice amount —
 * Crystal's {@code Totalamount} added the tax to an amount that already
 * included it, which only looked right on untaxed invoices.
 */
@Component
@RequiredArgsConstructor
public class InvoicePrintSnapshotLoader {

    private final SaleMasterRepository saleMasters;
    private final SaleDetailsRepository saleDetails;
    private final SaleMasterReferenceRepository saleMasterReferences;
    private final CustomerRepository customers;
    private final SymbolMasterRepository symbols;
    private final PaymentTermsMasterRepository paymentTerms;
    private final ItemMasterRepository itemMasters;
    private final UomRepository uoms;
    private final TaxMasterRepository taxes;
    private final DoMasterRepository doMasters;
    private final NamedParameterJdbcTemplate jdbc;
    private final MyInvoisUrls urls;
    private final MyInvoisQrCode qrCode;
    private final InvoicePrintProperties printProperties;

    @Transactional(readOnly = true)
    public Optional<InvoicePrintSnapshot> load(Integer invoiceId, Integer companyId) {
        SaleMaster invoice = saleMasters.findById(invoiceId).orElse(null);
        if (invoice == null || !Objects.equals(invoice.getCompanyRefId(), companyId)) {
            return Optional.empty();
        }

        Customer customer = invoice.getCustomerRefId() == null ? null
                : customers.findById(invoice.getCustomerRefId()).orElse(null);
        String currency = "RM";
        String terms = "";
        if (customer != null) {
            if (customer.getSymbolRefid() != null) {
                currency = symbols.findById(customer.getSymbolRefid()).map(SymbolMaster::getSName)
                        .filter(s -> s != null && !s.isBlank()).orElse("RM");
            }
            if (customer.getPaymentTermsRefid() != null) {
                terms = paymentTerms.findById(customer.getPaymentTermsRefid())
                        .map(PaymentTermsMaster::getTermsName).orElse("");
            }
        }

        boolean multiJob = saleMasterReferences.countBySaleMasterRefId(invoice.getId()) > 1;
        JobLink job = multiJob ? JobLink.NONE : jobLink(invoice.getId());

        List<InvoicePrintSnapshot.InvoicePrintLine> lines = loadLines(invoice);
        BigDecimal subtotal = lines.stream().map(InvoicePrintSnapshot.InvoicePrintLine::getLineSubtotal)
                .filter(Objects::nonNull).reduce(EInvoiceMoney.zero(), BigDecimal::add);
        BigDecimal tax = EInvoiceMoney.of(invoice.getTaxAmount());
        BigDecimal net = EInvoiceMoney.of(invoice.getAmount());

        String remarks = orEmpty(invoice.getRemarks()).trim();
        String customerName = customer == null ? "" : orEmpty(customer.getCustomerName());

        InvoicePrintSnapshot.InvoicePrintSnapshotBuilder b = InvoicePrintSnapshot.builder()
                .headerLines(List.copyOf(printProperties.getHeaderLines()))
                .heading(orEmpty(printProperties.getHeading()))
                .notes(List.copyOf(printProperties.getNotes()))
                .generatedNote(orEmpty(printProperties.getGeneratedNote()))
                .invoiceId(invoice.getId())
                .invoiceNo(invoice.getCNumberDisplay())
                .invoiceDate(invoice.getSaleDate() == null ? null : invoice.getSaleDate().toLocalDate())
                // Crystal CN: name/remarks when the invoice carries remarks
                .customerLine(remarks.isEmpty() ? customerName : customerName + "/" + remarks)
                .customerAddress(customer == null ? "" : orEmpty(customer.getAddress1()))
                .attentionName(customer == null ? "" : orEmpty(customer.getCity()))
                .attentionPhone(customer == null ? "" : orEmpty(customer.getOPhone()))
                .paymentTerms(terms)
                .currencySymbol(currency)
                .reference(orEmpty(invoice.getRemarks1()))
                .truckSize(orEmpty(invoice.getTruckSize()))
                .subtotal(subtotal)
                .taxTotal(tax)
                .roundingAdjustment(EInvoiceMoney.of(invoice.getCoinage()))
                .netTotal(net)
                .amountInWords(AmountInWords.of(currency, net))
                .eInvoiceUid(orEmpty(invoice.getEInvoiceUid()))
                .eInvoiceLongId(orEmpty(invoice.getEInvoiceLongId()))
                .eInvoiceStatus(orEmpty(invoice.getEInvoiceStatus()))
                .eInvoiceValidatedAt(invoice.getEInvoicePushVDT())
                .lines(lines);

        if (multiJob) {
            b.jobNo("").origin("").destination("").weight("").packages("")
                    .vesselOnboard("NIL").vesselOffland("NIL").doNo("").commodity("")
                    .blAwb("").truckName("");
        } else {
            b.jobNo(job.jobNo)
                    .origin(orEmpty(invoice.getOrigin()))
                    .destination(orEmpty(invoice.getDestination()))
                    .weight(orEmpty(invoice.getTotalWeight()))
                    .packages(orEmpty(invoice.getQuantity()))
                    .vesselOnboard(nilIfBlank(invoice.getLoadingvesselname()))
                    .vesselOffland(nilIfBlank(invoice.getOffvesselname()))
                    .doNo(doNumber(invoice))
                    .commodity(orEmpty(invoice.getCommodity()))
                    .collectionDate(invoice.getPickupDate() == null ? null : invoice.getPickupDate().toLocalDate())
                    .deliveryDate(invoice.getDeliveryDate() == null ? null : invoice.getDeliveryDate().toLocalDate())
                    .blAwb(blAwb(invoice.getAwbNo(), invoice.getBlCopy()))
                    .truckName(job.truckName);
        }

        String uid = orEmpty(invoice.getEInvoiceUid());
        String longId = orEmpty(invoice.getEInvoiceLongId());
        if (!uid.isBlank() && !longId.isBlank()) {
            String share = urls.documentShareLink(uid, longId);
            b.eInvoiceShareUrl(share);
            try {
                b.qrPng(qrCode.png(share));
            } catch (RuntimeException qrFailed) {
                b.qrPng(null); // the invoice prints without the picture
            }
        }
        return Optional.of(b.build());
    }

    // ────────────────────────────────────────────────────────────── pieces ──

    /** Crystal {@code BLAWB}: both joined with a slash, else whichever exists. */
    static String blAwb(String awb, String bl) {
        String a = orEmpty(awb).trim();
        String b = orEmpty(bl).trim();
        if (!a.isEmpty() && !b.isEmpty()) {
            return a + "/" + b;
        }
        return a.isEmpty() ? b : a;
    }

    static String nilIfBlank(String value) {
        return value == null || value.isBlank() ? "NIL" : value;
    }

    private String doNumber(SaleMaster invoice) {
        if (invoice.getDocNo() == null) {
            return "";
        }
        return doMasters.findById(invoice.getDocNo()).map(DoMaster::getCNumberDisplay).orElse("");
    }

    /**
     * The sale order this invoice settles and the truck that carried it —
     * legacy's {@code SaleOrderMaster s ON s.InvoiceNo = sm.Id} join and its
     * correlated RTI subquery, as one query so an invoice with no job still
     * prints (with blanks).
     */
    private JobLink jobLink(Integer invoiceId) {
        List<JobLink> rows = jdbc.query("""
                SELECT TOP 1 s.CNumberDisplay AS JobNo,
                       ISNULL((SELECT TOP 1 T.TruckName
                               FROM RTIMaster R WITH (NOLOCK)
                               INNER JOIN RTIDetails RD WITH (NOLOCK) ON RD.RTIMasterRefId = R.Id
                               INNER JOIN TruckMaster T WITH (NOLOCK) ON T.Id = R.TruckRefid
                               WHERE RD.SaleOrderMasterRefId = s.Id
                               ORDER BY R.Created_Date DESC), '') AS TruckName
                FROM SaleOrderMaster s WITH (NOLOCK)
                WHERE s.InvoiceNo = :invoiceId
                ORDER BY s.Id
                """,
                new MapSqlParameterSource("invoiceId", invoiceId),
                (rs, i) -> new JobLink(orEmpty(rs.getString("JobNo")), orEmpty(rs.getString("TruckName"))));
        return rows.isEmpty() ? JobLink.NONE : rows.get(0);
    }

    private List<InvoicePrintSnapshot.InvoicePrintLine> loadLines(SaleMaster invoice) {
        List<SaleDetails> details = saleDetails.findBySaleMasterRefId(invoice.getId()).stream()
                .sorted(Comparator.comparing(SaleDetails::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        Map<Integer, ItemMaster> items = itemMasters.findAllById(
                        details.stream().map(SaleDetails::getItemMasterRefId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(ItemMaster::getId, Function.identity()));
        Map<Integer, String> uomNames = uoms.findAllById(
                        items.values().stream().map(ItemMaster::getUomCode).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(Uom::getId, u -> orEmpty(u.getDescription())));
        Map<Integer, String> taxCodes = taxes.findAllById(
                        details.stream().map(SaleDetails::getTaxRefId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(TaxMaster::getId, t -> orEmpty(t.getCode())));

        List<InvoicePrintSnapshot.InvoicePrintLine> lines = new ArrayList<>(details.size());
        int row = 0;
        for (SaleDetails d : details) {
            row++;
            ItemMaster item = d.getItemMasterRefId() == null ? null : items.get(d.getItemMasterRefId());
            BigDecimal qty = EInvoiceMoney.quantity(d.getItemQty());
            BigDecimal rate = EInvoiceMoney.of(d.getSalesRate());
            String remarks = orEmpty(d.getSdRemarks()).trim();
            lines.add(InvoicePrintSnapshot.InvoicePrintLine.builder()
                    .rowNumber(row)
                    .productCode(item == null ? "" : orEmpty(item.getProdCode()))
                    // Crystal PName: what the operator typed, else the item name
                    .description(!remarks.isEmpty() ? remarks : item == null ? "" : orEmpty(item.getPName()))
                    .quantity(qty)
                    .uom(item == null || item.getUomCode() == null ? "" : uomNames.getOrDefault(item.getUomCode(), ""))
                    .unitPrice(rate)
                    .discountPercent(EInvoiceMoney.quantity(d.getDiscPer()))
                    .taxCode(d.getTaxRefId() == null ? "" : taxCodes.getOrDefault(d.getTaxRefId(), ""))
                    .taxPercent(EInvoiceMoney.quantity(d.getTaxPercent()))
                    .taxAmount(EInvoiceMoney.of(d.getTaxAmount()))
                    // Crystal subTotalAmount: SalesRate × ItemQty
                    .lineSubtotal(qty == null || rate == null ? null : EInvoiceMoney.round(qty.multiply(rate)))
                    .amount(EInvoiceMoney.of(d.getAmount()))
                    .build());
        }
        return lines;
    }

    private record JobLink(String jobNo, String truckName) {
        static final JobLink NONE = new JobLink("", "");
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
