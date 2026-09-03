package my.maleva.api.module.ai.billextraction.service.impl;

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
import my.maleva.api.module.ai.billextraction.dto.BillExtractionResponse;
import my.maleva.api.module.ai.billextraction.dto.ExtractedBill;
import my.maleva.api.module.ai.billextraction.service.BillExtractionService;
import my.maleva.api.module.ai.common.ExtractionSupport;
import my.maleva.api.module.ai.common.GlAccountMatcher;
import my.maleva.api.module.ai.common.SupplierMatcher;
import my.maleva.api.module.master.entity.PaymentTermsMaster;
import my.maleva.api.module.payment.repository.PaymentTermsMasterRepository;
import my.maleva.api.module.supplier.entity.Supplier;
import my.maleva.api.module.supplier.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static my.maleva.api.module.ai.common.ExtractionSupport.currency;
import static my.maleva.api.module.ai.common.ExtractionSupport.daysFor;
import static my.maleva.api.module.ai.common.ExtractionSupport.format;
import static my.maleva.api.module.ai.common.ExtractionSupport.parseDate;
import static my.maleva.api.module.ai.common.ExtractionSupport.scale;
import static my.maleva.api.module.ai.common.ExtractionSupport.termsFromText;
import static my.maleva.api.module.ai.common.ExtractionSupport.trimToNull;

/**
 * Reads a bill with the configured AI provider, then resolves what it read
 * against the company's masters. Reference data is loaded before the model
 * call so no database connection is held during the (slow) request.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BillExtractionServiceImpl implements BillExtractionService {

    /** Mirrors the Bills form's fixed description list. */
    static final List<String> DESCRIPTION_CATEGORIES = List.of(
            "HIRE PURCHASE", "SALARY", "OTHER EXPENSES", "DIRECTOR EXPENSES", "TENANCY", "MAINTENANCE",
            "UTILITY", "VENDOR", "BACKLOG", "FUEL", "TOLL", "CLAIM", "KASTAM DUTY", "BOAT PAYMENT",
            "ADVANCE", "WAGES");

    private final LlmGateway gateway;
    private final ObjectMapper objectMapper;
    private final SupplierRepository supplierRepository;
    private final GLAccountsRepository glAccountsRepository;
    private final PaymentTermsMasterRepository paymentTermsRepository;

    @Override
    public BillExtractionResponse extract(Integer companyRefId, MultipartFile file, String providerKey) {
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

        LlmRequest request = LlmRequest.builder()
                .task(LlmTasks.BILL_EXTRACTION)
                .companyRefId(companyRefId)
                .providerKey(providerKey)
                .systemPrompt(BillExtractionPrompt.SYSTEM)
                .userPrompt(BillExtractionPrompt.user(accounts, DESCRIPTION_CATEGORIES))
                .attachments(List.of(attachment))
                .jsonOutput(true)
                .jsonSchema(BillExtractionPrompt.schema(objectMapper))
                .temperature(0.0)
                .sampleOutput(BillExtractionPrompt.SAMPLE_OUTPUT)
                .build();

        LlmResponse response = gateway.complete(request);
        ExtractedBill extracted = LlmJson.parse(objectMapper, response.text(), ExtractedBill.class, response.providerKey());
        BillExtractionResponse result = assemble(extracted, response, suppliers, accounts, terms);
        log.info("Bill read by {} ({}): supplier={} invoice={} lines={} warnings={}",
                response.providerKey(), response.model(), result.getSupplier().getSupplierId(),
                result.getHeader().getInvoiceNo(), result.getLines().size(), result.getWarnings().size());
        return result;
    }

    BillExtractionResponse assemble(ExtractedBill extracted, LlmResponse response, List<Supplier> suppliers,
                                    List<GLAccounts> accounts, List<PaymentTermsMaster> terms) {
        List<String> warnings = new ArrayList<>();
        if (extracted == null) {
            extracted = new ExtractedBill();
        }

        // --- supplier -------------------------------------------------------
        ExtractedBill.ExtractedSupplier extractedSupplier = extracted.getSupplier() == null
                ? new ExtractedBill.ExtractedSupplier() : extracted.getSupplier();
        String extractedName = trimToNull(extractedSupplier.getName());
        List<SupplierMatcher.Match> ranked = SupplierMatcher.rank(extractedSupplier.toHint(), suppliers);
        Optional<SupplierMatcher.Match> best = SupplierMatcher.best(ranked);
        Integer supplierId = best.map(m -> m.supplier().getId()).orElse(null);
        double confidence = best.map(SupplierMatcher.Match::score)
                .orElse(ranked.isEmpty() ? 0.0 : ranked.get(0).score());
        List<BillExtractionResponse.Candidate> candidates = ranked.stream()
                .filter(m -> m.score() >= 0.35)
                .limit(3)
                .map(m -> new BillExtractionResponse.Candidate(m.supplier().getId(), m.supplier().getSupplierName(), m.score()))
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
            paymentTermsId = termsFromText(extracted.getPaymentTermsText(), terms);
        }

        // --- header ---------------------------------------------------------
        LocalDate invoiceDate = parseDate(extracted.getInvoiceDate(), "invoice date", warnings);
        LocalDate dueDate = parseDate(extracted.getDueDate(), "due date", warnings);
        if (dueDate == null && invoiceDate != null && paymentTermsId != null) {
            Integer days = daysFor(paymentTermsId, terms);
            if (days != null) {
                dueDate = invoiceDate.plusDays(days);
            }
        }
        String invoiceNo = trimToNull(extracted.getInvoiceNo());
        if (invoiceNo == null) {
            warnings.add("No invoice number was read from the document");
        }
        if (invoiceDate == null) {
            warnings.add("No invoice date was read from the document");
        }
        String purchaseOrderNo = trimToNull(extracted.getPurchaseOrderNo());
        String currency = currency(extracted.getCurrencyCode());
        BigDecimal total = scale(extracted.getTotalAmount());

        // --- lines ----------------------------------------------------------
        GlAccountMatcher accountMatcher = new GlAccountMatcher(accounts);
        List<BillExtractionResponse.Line> lines = new ArrayList<>();
        int unresolved = 0;
        BigDecimal sum = BigDecimal.ZERO;
        for (ExtractedBill.ExtractedLine line : extracted.getLines() == null ? List.<ExtractedBill.ExtractedLine>of() : extracted.getLines()) {
            if (line == null) {
                continue;
            }
            BigDecimal quantity = line.getQuantity() == null || line.getQuantity().signum() <= 0 ? BigDecimal.ONE : line.getQuantity();
            BigDecimal unitPrice = line.getUnitPrice();
            BigDecimal amount = line.getAmount();
            BigDecimal taxPercent = line.getTaxPercent();
            BigDecimal taxAmount = line.getTaxAmount();
            if (unitPrice == null && amount != null) {
                BigDecimal base = taxAmount != null ? amount.subtract(taxAmount) : amount;
                unitPrice = base.divide(quantity, 4, RoundingMode.HALF_UP);
            }
            if (unitPrice == null) {
                unitPrice = BigDecimal.ZERO;
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
            lines.add(BillExtractionResponse.Line.builder()
                    .description(trimToNull(line.getDescription()) == null ? "" : line.getDescription().trim())
                    .quantity(scale(quantity))
                    .unitPrice(unitPrice.setScale(4, RoundingMode.HALF_UP).stripTrailingZeros().scale() > 2
                            ? unitPrice.setScale(4, RoundingMode.HALF_UP) : scale(unitPrice))
                    .taxPercent(scale(taxPercent))
                    .taxAmount(scale(taxAmount))
                    .amount(scale(amount))
                    .accountCode(account.map(GLAccounts::getGlAccountCode).orElse(null))
                    .accountId(account.map(GLAccounts::getRowIndex).orElse(null))
                    .accountName(account.map(GLAccounts::getDescription).orElse(null))
                    .build());
            sum = sum.add(amount);
        }
        if (lines.isEmpty()) {
            if (total != null) {
                BigDecimal tax = extracted.getTaxAmount() == null ? BigDecimal.ZERO : extracted.getTaxAmount();
                BigDecimal base = total.subtract(tax);
                BigDecimal pct = base.signum() > 0
                        ? tax.multiply(BigDecimal.valueOf(100)).divide(base, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
                lines.add(BillExtractionResponse.Line.builder()
                        .description("AS PER INVOICE" + (invoiceNo == null ? "" : " " + invoiceNo))
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

        BillExtractionResponse.Header header = BillExtractionResponse.Header.builder()
                .invoiceNo(invoiceNo)
                .invoiceDate(format(invoiceDate))
                .billDate(format(invoiceDate))
                .dueDate(format(dueDate))
                .currencyCode(currency)
                .subtotal(scale(extracted.getSubtotal()))
                .taxAmount(scale(extracted.getTaxAmount()))
                .totalAmount(total)
                .paymentTermsText(trimToNull(extracted.getPaymentTermsText()))
                .description(category(extracted.getDescriptionCategory()))
                .remarks(purchaseOrderNo == null ? null : "PO " + purchaseOrderNo)
                .purchaseOrderNo(purchaseOrderNo)
                .build();

        return BillExtractionResponse.builder()
                .provider(response.providerKey())
                .model(response.model())
                .latencyMs(response.latencyMs())
                .inputTokens(response.inputTokens())
                .outputTokens(response.outputTokens())
                .supplier(BillExtractionResponse.SupplierMatch.builder()
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

    static String category(String raw) {
        String value = trimToNull(raw);
        if (value == null) {
            return null;
        }
        String upper = value.toUpperCase(Locale.ROOT);
        for (String category : DESCRIPTION_CATEGORIES) {
            if (category.equals(upper)) {
                return category;
            }
        }
        return null;
    }
}
