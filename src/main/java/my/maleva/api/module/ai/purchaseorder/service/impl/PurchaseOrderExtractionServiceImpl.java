package my.maleva.api.module.ai.purchaseorder.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.integration.llm.LlmAttachment;
import my.maleva.api.integration.llm.LlmGateway;
import my.maleva.api.integration.llm.LlmJson;
import my.maleva.api.integration.llm.LlmRequest;
import my.maleva.api.integration.llm.LlmResponse;
import my.maleva.api.integration.llm.LlmTasks;
import my.maleva.api.module.accounting.entity.GLAccounts;
import my.maleva.api.module.accounting.repository.GLAccountsRepository;
import my.maleva.api.module.ai.common.ExtractionSupport;
import my.maleva.api.module.ai.common.GlAccountMatcher;
import my.maleva.api.module.ai.common.SupplierMatcher;
import my.maleva.api.module.ai.purchaseorder.dto.ExtractedPurchaseOrder;
import my.maleva.api.module.ai.purchaseorder.dto.PurchaseOrderExtractionResponse;
import my.maleva.api.module.ai.purchaseorder.service.PurchaseOrderExtractionService;
import my.maleva.api.module.billing.billorder.dto.PaymentVoucherComboDto;
import my.maleva.api.module.billing.billorder.repository.BillsOrderMasterRepository;
import my.maleva.api.module.fleet.entity.DriverMaster;
import my.maleva.api.module.fleet.entity.TruckMaster;
import my.maleva.api.module.fleet.repository.DriverMasterRepository;
import my.maleva.api.module.fleet.repository.TruckMasterRepository;
import my.maleva.api.module.master.entity.PaymentTermsMaster;
import my.maleva.api.module.payment.repository.PaymentTermsMasterRepository;
import my.maleva.api.module.productmaster.entity.ProductMaster;
import my.maleva.api.module.productmaster.repository.ProductMasterRepository;
import my.maleva.api.module.supplier.entity.Supplier;
import my.maleva.api.module.supplier.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static my.maleva.api.module.ai.common.ExtractionSupport.currency;
import static my.maleva.api.module.ai.common.ExtractionSupport.daysFor;
import static my.maleva.api.module.ai.common.ExtractionSupport.format;
import static my.maleva.api.module.ai.common.ExtractionSupport.parseDate;
import static my.maleva.api.module.ai.common.ExtractionSupport.scale;
import static my.maleva.api.module.ai.common.ExtractionSupport.termsFromText;
import static my.maleva.api.module.ai.common.ExtractionSupport.trimToNull;

/**
 * Reads a supplier document with the configured AI provider and turns it
 * into a purchase-order draft. Same pipeline as the bill reader, plus the
 * PO-only fields: truck, driver, job number, vessels, serial numbers and
 * store items for workshop orders.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseOrderExtractionServiceImpl implements PurchaseOrderExtractionService {

    /** The workshop descriptions the legacy screen offers when nothing has been used yet. */
    static final List<String> MAINTENANCE_DESCRIPTIONS = List.of("MAINTENANCE", "BREAKDOWN", "REPAIR", "SERVICE", "SPARE PARTS");

    private final LlmGateway gateway;
    private final ObjectMapper objectMapper;
    private final SupplierRepository supplierRepository;
    private final GLAccountsRepository glAccountsRepository;
    private final PaymentTermsMasterRepository paymentTermsRepository;
    private final BillsOrderMasterRepository billsOrderMasterRepository;
    private final TruckMasterRepository truckMasterRepository;
    private final DriverMasterRepository driverMasterRepository;
    private final ProductMasterRepository productMasterRepository;

    @Override
    public PurchaseOrderExtractionResponse extract(Integer companyRefId, MultipartFile file, String providerKey) {
        if (companyRefId == null) {
            throw new InvalidRequestException("companyId is required");
        }
        LlmAttachment attachment = ExtractionSupport.toAttachment(file);

        List<GLAccounts> accounts = glAccountsRepository.findByCompanyAndExpense(companyRefId, 0);
        List<Supplier> suppliers = supplierRepository.findByCompanyRefIdAndActive(companyRefId, 1);
        List<PaymentTermsMaster> terms = paymentTermsRepository.findAll().stream()
                .filter(t -> companyRefId.equals(t.getCompanyRefId()))
                .filter(t -> t.getActive() == null || t.getActive() == 1)
                .toList();
        List<String> descriptions = descriptionsFor(companyRefId);
        List<TruckMaster> trucks = truckMasterRepository.findByCompanyRefIdAndActive(companyRefId, 1);
        List<DriverMaster> drivers = driverMasterRepository.findByCompanyRefIdAndActive(companyRefId, 1);
        List<PaymentVoucherComboDto> existingInvoices = billsOrderMasterRepository.findInvoiceNumbersByCompany(companyRefId);

        LlmRequest request = LlmRequest.builder()
                .task(LlmTasks.PURCHASE_ORDER_EXTRACTION)
                .companyRefId(companyRefId)
                .providerKey(providerKey)
                .systemPrompt(PurchaseOrderExtractionPrompt.SYSTEM)
                .userPrompt(PurchaseOrderExtractionPrompt.user(accounts, descriptions))
                .attachments(List.of(attachment))
                .jsonOutput(true)
                .jsonSchema(PurchaseOrderExtractionPrompt.schema(objectMapper))
                .temperature(0.0)
                .sampleOutput(PurchaseOrderExtractionPrompt.SAMPLE_OUTPUT)
                .build();

        LlmResponse response = gateway.complete(request);
        ExtractedPurchaseOrder extracted = LlmJson.parse(objectMapper, response.text(),
                ExtractedPurchaseOrder.class, response.providerKey());

        Context context = new Context(companyRefId, suppliers, accounts, terms, descriptions, trucks, drivers, existingInvoices);
        PurchaseOrderExtractionResponse result = assemble(extracted, response, context);
        log.info("Purchase order document read by {} ({}): type={} supplier={} docNo={} lines={} warnings={}",
                response.providerKey(), response.model(), result.getHeader().getDocumentType(),
                result.getSupplier().getSupplierId(), result.getHeader().getInvoiceNo(),
                result.getLines().size(), result.getWarnings().size());
        return result;
    }

    /** Distinct past descriptions plus the workshop defaults, de-duplicated, upper-cased. */
    List<String> descriptionsFor(Integer companyRefId) {
        Set<String> out = new LinkedHashSet<>();
        List<String> past = billsOrderMasterRepository.findDistinctDescriptionsByCompany(companyRefId);
        if (past != null) {
            for (String value : past) {
                String trimmed = trimToNull(value);
                if (trimmed != null) {
                    out.add(trimmed.toUpperCase(Locale.ROOT));
                }
            }
        }
        out.addAll(MAINTENANCE_DESCRIPTIONS);
        return new ArrayList<>(out);
    }

    /** Everything the resolver needs, loaded before the model call. */
    record Context(Integer companyRefId, List<Supplier> suppliers, List<GLAccounts> accounts,
                   List<PaymentTermsMaster> terms, List<String> descriptions, List<TruckMaster> trucks,
                   List<DriverMaster> drivers, List<PaymentVoucherComboDto> existingInvoices) {
    }

    PurchaseOrderExtractionResponse assemble(ExtractedPurchaseOrder extracted, LlmResponse response, Context ctx) {
        List<String> warnings = new ArrayList<>();
        if (extracted == null) {
            extracted = new ExtractedPurchaseOrder();
        }

        // --- supplier -------------------------------------------------------
        ExtractedPurchaseOrder.ExtractedSupplier extractedSupplier = extracted.getSupplier() == null
                ? new ExtractedPurchaseOrder.ExtractedSupplier() : extracted.getSupplier();
        String extractedName = trimToNull(extractedSupplier.getName());
        List<SupplierMatcher.Match> ranked = SupplierMatcher.rank(extractedSupplier.toHint(), ctx.suppliers());
        Optional<SupplierMatcher.Match> best = SupplierMatcher.best(ranked);
        Integer supplierId = best.map(m -> m.supplier().getId()).orElse(null);
        double confidence = best.map(SupplierMatcher.Match::score)
                .orElse(ranked.isEmpty() ? 0.0 : ranked.get(0).score());
        List<PurchaseOrderExtractionResponse.Candidate> candidates = ranked.stream()
                .filter(m -> m.score() >= 0.35)
                .limit(3)
                .map(m -> new PurchaseOrderExtractionResponse.Candidate(m.supplier().getId(), m.supplier().getSupplierName(), m.score()))
                .toList();
        if (supplierId == null) {
            warnings.add(extractedName == null
                    ? "No supplier name was read from the document"
                    : "Supplier '" + extractedName + "' was not found in the supplier master - pick it manually");
        }
        Integer paymentTermsId = best.map(m -> m.supplier().getPaymentTermsRefid())
                .filter(id -> id != null && id > 0)
                .orElse(null);
        if (paymentTermsId == null) {
            paymentTermsId = termsFromText(extracted.getPaymentTermsText(), ctx.terms());
        }

        // --- header ---------------------------------------------------------
        LocalDate invoiceDate = parseDate(extracted.getDocumentDate(), "document date", warnings);
        LocalDate orderDate = parseDate(extracted.getOrderDate(), "order date", warnings);
        LocalDate dueDate = parseDate(extracted.getDueDate(), "due date", warnings);
        LocalDate deliveryDate = parseDate(extracted.getDeliveryDate(), "delivery date", warnings);
        if (dueDate == null && invoiceDate != null && paymentTermsId != null) {
            Integer days = daysFor(paymentTermsId, ctx.terms());
            if (days != null) {
                dueDate = invoiceDate.plusDays(days);
            }
        }
        String invoiceNo = trimToNull(extracted.getDocumentNo());
        if (invoiceNo == null) {
            warnings.add("No document number was read - the Invoice No is required to save");
        } else {
            duplicateInvoice(invoiceNo, ctx.existingInvoices()).ifPresent(owner ->
                    warnings.add("Invoice no '" + invoiceNo + "' already exists on a purchase order"
                            + (owner.isBlank() ? "" : " for " + owner)));
        }
        if (invoiceDate == null) {
            warnings.add("No document date was read from the document");
        }
        String currency = currency(extracted.getCurrencyCode());
        if (!"MYR".equals(currency)) {
            warnings.add("Document currency is " + currency + "; purchase orders are recorded in MYR");
        }
        BigDecimal total = scale(extracted.getTotalAmount());

        String description = matchDescription(extracted.getDescriptionCategory(), ctx.descriptions(), warnings);
        boolean maintenance = description != null && MAINTENANCE_DESCRIPTIONS.contains(description);

        String plate = trimToNull(extracted.getVehiclePlateNo());
        Optional<TruckMaster> truck = plate == null ? Optional.empty() : FleetMatcher.truck(plate, ctx.trucks());
        if (plate != null && truck.isEmpty()) {
            warnings.add("Vehicle '" + plate + "' was not found in the truck master - pick the truck manually");
        }
        String driverName = trimToNull(extracted.getDriverName());
        Optional<DriverMaster> driver = driverName == null ? Optional.empty() : FleetMatcher.driver(driverName, ctx.drivers());
        if (driverName != null && driver.isEmpty()) {
            warnings.add("Driver '" + driverName + "' was not found in the driver master - pick the driver manually");
        }

        // --- lines ----------------------------------------------------------
        GlAccountMatcher accountMatcher = new GlAccountMatcher(ctx.accounts());
        List<PurchaseOrderExtractionResponse.Line> lines = new ArrayList<>();
        int unresolved = 0;
        BigDecimal sum = BigDecimal.ZERO;
        List<ExtractedPurchaseOrder.ExtractedLine> extractedLines = extracted.getLines() == null
                ? List.of() : extracted.getLines();
        boolean needStoreItems = maintenance && extractedLines.stream().anyMatch(l -> l != null
                && (trimToNull(l.getItemCode()) != null || trimToNull(l.getItemName()) != null));
        List<ProductMaster> products = needStoreItems
                ? productMasterRepository.findByCompanyRefIdAndActivestatus(ctx.companyRefId(), 1) : List.of();

        for (ExtractedPurchaseOrder.ExtractedLine line : extractedLines) {
            if (line == null) {
                continue;
            }
            BigDecimal quantity = line.getQuantity() == null || line.getQuantity().signum() <= 0 ? BigDecimal.ONE : line.getQuantity();
            BigDecimal unitPrice = line.getUnitPrice();
            BigDecimal amount = line.getAmount();
            BigDecimal taxPercent = line.getTaxPercent();
            BigDecimal taxAmount = line.getTaxAmount();
            BigDecimal discount = line.getDiscountAmount() == null ? BigDecimal.ZERO : line.getDiscountAmount();
            if (unitPrice == null && amount != null) {
                BigDecimal base = taxAmount != null ? amount.subtract(taxAmount) : amount;
                unitPrice = base.divide(quantity, 4, RoundingMode.HALF_UP);
            }
            if (unitPrice == null) {
                unitPrice = BigDecimal.ZERO;
            }
            if (discount.signum() > 0 && amount != null) {
                // The PO grid has no discount column: fold a line discount into the unit price.
                BigDecimal base = (taxAmount != null ? amount.subtract(taxAmount) : amount);
                unitPrice = base.divide(quantity, 4, RoundingMode.HALF_UP);
            }
            BigDecimal base = quantity.multiply(unitPrice);
            if (taxPercent == null && taxAmount != null && base.signum() > 0) {
                taxPercent = taxAmount.multiply(BigDecimal.valueOf(100)).divide(base, 2, RoundingMode.HALF_UP);
            }
            if (taxPercent == null) {
                taxPercent = BigDecimal.ZERO;
            }
            if (taxAmount == null) {
                taxAmount = base.multiply(taxPercent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }
            if (amount == null) {
                amount = base.add(taxAmount);
            }
            Optional<GLAccounts> account = accountMatcher.match(line.getAccountCode());
            if (account.isEmpty()) {
                unresolved++;
            }
            Optional<ProductMaster> storeItem = needStoreItems ? storeItem(line, products) : Optional.empty();

            String text = trimToNull(line.getDescription());
            if (text == null) {
                text = trimToNull(line.getItemName());
            }
            String lineRemarks = trimToNull(line.getRemarks());
            if (text != null && lineRemarks != null && !text.toUpperCase(Locale.ROOT).contains(lineRemarks.toUpperCase(Locale.ROOT))) {
                text = text + " - " + lineRemarks;
            }
            lines.add(PurchaseOrderExtractionResponse.Line.builder()
                    .description(text == null ? "" : text)
                    .serialNo(trimToNull(line.getSerialNo()))
                    .quantity(scale(quantity))
                    .unitPrice(unitPrice.setScale(4, RoundingMode.HALF_UP).stripTrailingZeros().scale() > 2
                            ? unitPrice.setScale(4, RoundingMode.HALF_UP) : scale(unitPrice))
                    .taxPercent(scale(taxPercent))
                    .taxAmount(scale(taxAmount))
                    .amount(scale(amount))
                    .accountCode(account.map(GLAccounts::getGlAccountCode).orElse(null))
                    .accountId(account.map(GLAccounts::getRowIndex).orElse(null))
                    .accountName(account.map(GLAccounts::getDescription).orElse(null))
                    .storeItemId(storeItem.map(ProductMaster::getId).orElse(null))
                    .storeItemCode(storeItem.map(ProductMaster::getProdCode).orElse(null))
                    .storeItemName(storeItem.map(ProductMaster::getPname).orElse(null))
                    .uom(trimToNull(line.getUom()))
                    .build());
            sum = sum.add(amount);
        }
        if (lines.isEmpty()) {
            if (total != null) {
                BigDecimal tax = extracted.getTaxAmount() == null ? BigDecimal.ZERO : extracted.getTaxAmount();
                BigDecimal base = total.subtract(tax);
                BigDecimal pct = base.signum() > 0
                        ? tax.multiply(BigDecimal.valueOf(100)).divide(base, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
                lines.add(PurchaseOrderExtractionResponse.Line.builder()
                        .description("AS PER " + documentLabel(extracted.getDocumentType()) + (invoiceNo == null ? "" : " " + invoiceNo))
                        .quantity(BigDecimal.ONE.setScale(2))
                        .unitPrice(scale(base))
                        .taxPercent(scale(pct))
                        .taxAmount(scale(tax))
                        .amount(total)
                        .build());
                sum = total;
                warnings.add("No line items were read; one line was created from the document total");
            } else {
                warnings.add("No line items or total could be read from the document");
            }
        }
        if (unresolved > 0) {
            warnings.add(unresolved + " line(s) have no matching account code - choose the account manually");
        }
        if (total != null && !lines.isEmpty() && sum.subtract(total).abs().compareTo(new BigDecimal("0.05")) > 0) {
            warnings.add("Document total " + total.toPlainString() + " does not match the sum of lines " + scale(sum).toPlainString());
        }

        PurchaseOrderExtractionResponse.Header header = PurchaseOrderExtractionResponse.Header.builder()
                .documentType(documentType(extracted.getDocumentType()))
                .invoiceNo(invoiceNo)
                .invoiceDate(format(invoiceDate))
                .poDate(format(orderDate != null ? orderDate : invoiceDate))
                .dueDate(format(dueDate))
                .deliveryDate(format(deliveryDate))
                .currencyCode(currency)
                .subtotal(scale(extracted.getSubtotal()))
                .taxAmount(scale(extracted.getTaxAmount()))
                .totalAmount(total)
                .paymentTermsText(trimToNull(extracted.getPaymentTermsText()))
                .description(description)
                .remarks(trimToNull(extracted.getRemarks()))
                .purchaseOrderNo(trimToNull(extracted.getPurchaseOrderNo()))
                .jobNo(trimToNull(extracted.getJobNo()))
                .loadingVessel(trimToNull(extracted.getLoadingVessel()))
                .offVessel(trimToNull(extracted.getOffVessel()))
                .vehiclePlateNo(plate)
                .truckId(truck.map(TruckMaster::getId).orElse(null))
                .truckName(truck.map(TruckMaster::getTruckName).orElse(null))
                .driverName(driverName)
                .driverId(driver.map(DriverMaster::getId).orElse(null))
                .driverMatchedName(driver.map(DriverMaster::getDriverName).orElse(null))
                .build();

        return PurchaseOrderExtractionResponse.builder()
                .provider(response.providerKey())
                .model(response.model())
                .latencyMs(response.latencyMs())
                .inputTokens(response.inputTokens())
                .outputTokens(response.outputTokens())
                .supplier(PurchaseOrderExtractionResponse.SupplierMatch.builder()
                        .extractedName(extractedName)
                        .supplierId(supplierId)
                        .matchConfidence(confidence)
                        .candidates(candidates)
                        .paymentTermsId(paymentTermsId)
                        .build())
                .header(header)
                .lines(lines)
                .warnings(warnings)
                .build();
    }

    /** Exact (case-insensitive) match against the company's list; otherwise the raw text plus a warning. */
    static String matchDescription(String raw, List<String> descriptions, List<String> warnings) {
        String value = trimToNull(raw);
        if (value == null) {
            return null;
        }
        String upper = value.toUpperCase(Locale.ROOT);
        for (String candidate : descriptions) {
            if (candidate != null && candidate.trim().toUpperCase(Locale.ROOT).equals(upper)) {
                return candidate.trim().toUpperCase(Locale.ROOT);
            }
        }
        warnings.add("Description '" + value + "' is not in this company's list - choose one");
        return upper;
    }

    static Optional<ProductMaster> storeItem(ExtractedPurchaseOrder.ExtractedLine line, List<ProductMaster> products) {
        String code = ExtractionSupport.compact(line.getItemCode());
        if (!code.isEmpty()) {
            for (ProductMaster product : products) {
                if (code.equals(ExtractionSupport.compact(product.getProdCode()))) {
                    return Optional.of(product);
                }
            }
        }
        String name = trimToNull(line.getItemName());
        if (name == null) {
            return Optional.empty();
        }
        String wanted = name.toUpperCase(Locale.ROOT);
        ProductMaster only = null;
        int hits = 0;
        for (ProductMaster product : products) {
            String pname = product.getPname() == null ? "" : product.getPname().trim().toUpperCase(Locale.ROOT);
            if (pname.isEmpty()) {
                continue;
            }
            if (pname.equals(wanted)) {
                return Optional.of(product);
            }
            if (pname.length() >= 4 && (pname.contains(wanted) || wanted.contains(pname))) {
                only = product;
                hits++;
            }
        }
        return hits == 1 ? Optional.of(only) : Optional.empty();
    }

    static Optional<String> duplicateInvoice(String invoiceNo, List<PaymentVoucherComboDto> existing) {
        if (existing == null) {
            return Optional.empty();
        }
        String wanted = invoiceNo.trim().toUpperCase(Locale.ROOT);
        for (PaymentVoucherComboDto row : existing) {
            if (row == null || row.getInvoiceNo() == null) {
                continue;
            }
            if (wanted.equals(row.getInvoiceNo().trim().toUpperCase(Locale.ROOT))) {
                return Optional.of(row.getAccountName() == null ? "" : row.getAccountName().trim());
            }
        }
        return Optional.empty();
    }

    static String documentType(String raw) {
        String value = trimToNull(raw);
        if (value == null) {
            return null;
        }
        String upper = value.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "_");
        return switch (upper) {
            case "QUOTATION", "QUOTE" -> "QUOTATION";
            case "PROFORMA_INVOICE", "PROFORMA" -> "PROFORMA_INVOICE";
            case "INVOICE", "TAX_INVOICE" -> "INVOICE";
            case "DELIVERY_ORDER", "DO" -> "DELIVERY_ORDER";
            case "PURCHASE_ORDER", "PO" -> "PURCHASE_ORDER";
            default -> "OTHER";
        };
    }

    private static String documentLabel(String raw) {
        String type = documentType(raw);
        return type == null || type.equals("OTHER") ? "DOCUMENT" : type.replace('_', ' ');
    }
}
