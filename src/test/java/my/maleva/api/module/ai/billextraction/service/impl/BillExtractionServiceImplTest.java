package my.maleva.api.module.ai.billextraction.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.integration.llm.LlmGateway;
import my.maleva.api.integration.llm.LlmRequest;
import my.maleva.api.integration.llm.LlmResponse;
import my.maleva.api.module.accounting.entity.GLAccounts;
import my.maleva.api.module.accounting.repository.GLAccountsRepository;
import my.maleva.api.module.ai.billextraction.dto.BillExtractionResponse;
import my.maleva.api.module.master.entity.PaymentTermsMaster;
import my.maleva.api.module.payment.repository.PaymentTermsMasterRepository;
import my.maleva.api.module.supplier.entity.Supplier;
import my.maleva.api.module.supplier.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BillExtractionServiceImplTest {

    private static final int COMPANY = 6;

    @Mock
    private LlmGateway gateway;
    @Mock
    private SupplierRepository supplierRepository;
    @Mock
    private GLAccountsRepository glAccountsRepository;
    @Mock
    private PaymentTermsMasterRepository paymentTermsRepository;

    private BillExtractionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new BillExtractionServiceImpl(gateway, new ObjectMapper(), supplierRepository,
                glAccountsRepository, paymentTermsRepository);

        Supplier petronas = new Supplier();
        petronas.setId(7);
        petronas.setSupplierName("PETRONAS DAGANGAN BHD");
        petronas.setPaymentTermsRefid(3);
        Supplier shell = new Supplier();
        shell.setId(8);
        shell.setSupplierName("SHELL MALAYSIA TRADING SDN BHD");
        when(supplierRepository.findByCompanyRefIdAndActive(COMPANY, 1)).thenReturn(List.of(petronas, shell));

        GLAccounts fuel = new GLAccounts();
        fuel.setGlAccountCode("6100-000");
        fuel.setDescription("FUEL & DIESEL");
        fuel.setRowIndex(6100);
        GLAccounts toll = new GLAccounts();
        toll.setGlAccountCode("6200-000");
        toll.setDescription("TOLL CHARGES");
        toll.setRowIndex(6200);
        when(glAccountsRepository.findByCompanyAndExpense(COMPANY, 0)).thenReturn(List.of(fuel, toll));

        PaymentTermsMaster thirty = new PaymentTermsMaster();
        thirty.setId(3);
        thirty.setCompanyRefId(COMPANY);
        thirty.setTermsName("30 DAYS");
        thirty.setTDays(30);
        thirty.setActive(1);
        PaymentTermsMaster cash = new PaymentTermsMaster();
        cash.setId(4);
        cash.setCompanyRefId(COMPANY);
        cash.setTermsName("CASH");
        cash.setTDays(0);
        cash.setActive(1);
        PaymentTermsMaster otherCompany = new PaymentTermsMaster();
        otherCompany.setId(5);
        otherCompany.setCompanyRefId(99);
        otherCompany.setTDays(60);
        otherCompany.setActive(1);
        when(paymentTermsRepository.findAll()).thenReturn(List.of(thirty, cash, otherCompany));
    }

    private static MockMultipartFile pdf() {
        return new MockMultipartFile("file", "petronas.pdf", "application/pdf", "%PDF-1.4 fake".getBytes());
    }

    private static LlmResponse answer(String json) {
        return new LlmResponse("claude", "claude-opus-5", json, 1500L, 400L, 2100, "end_turn");
    }

    @Test
    void resolvesSupplierTermsAccountsAndDueDate() {
        String json = """
                ```json
                {
                  "supplier": {"name": "Petronas Dagangan Berhad", "sstNo": "W10-1808-31006123"},
                  "invoiceNo": "PDB-2026-004521",
                  "invoiceDate": "28/08/2026",
                  "dueDate": null,
                  "currencyCode": "RM",
                  "paymentTermsText": "30 days",
                  "purchaseOrderNo": "PO-1188",
                  "subtotal": "2,450.00",
                  "taxAmount": 0,
                  "totalAmount": "RM 2,450.00",
                  "descriptionCategory": "fuel",
                  "lines": [
                    {"description": "Diesel Euro 5 B10", "quantity": 1000, "unitPrice": 2.45, "taxPercent": 0, "amount": 2450, "accountCode": "6100-000"}
                  ]
                }
                ```
                """;
        when(gateway.complete(any())).thenReturn(answer(json));

        BillExtractionResponse result = service.extract(COMPANY, pdf(), null);

        assertThat(result.getProvider()).isEqualTo("claude");
        assertThat(result.getSupplier().getSupplierId()).isEqualTo(7);
        assertThat(result.getSupplier().getPaymentTermsId()).isEqualTo(3);
        assertThat(result.getSupplier().getCandidates()).extracting(BillExtractionResponse.Candidate::id).contains(7);
        assertThat(result.getHeader().getInvoiceNo()).isEqualTo("PDB-2026-004521");
        assertThat(result.getHeader().getInvoiceDate()).isEqualTo("2026-08-28");
        assertThat(result.getHeader().getBillDate()).isEqualTo("2026-08-28");
        assertThat(result.getHeader().getDueDate()).isEqualTo("2026-09-27");
        assertThat(result.getHeader().getCurrencyCode()).isEqualTo("MYR");
        assertThat(result.getHeader().getTotalAmount()).isEqualByComparingTo("2450.00");
        assertThat(result.getHeader().getDescription()).isEqualTo("FUEL");
        assertThat(result.getHeader().getRemarks()).isEqualTo("PO PO-1188");
        assertThat(result.getLines()).hasSize(1);
        BillExtractionResponse.Line line = result.getLines().get(0);
        assertThat(line.getAccountId()).isEqualTo(6100);
        assertThat(line.getAccountCode()).isEqualTo("6100-000");
        assertThat(line.getQuantity()).isEqualByComparingTo("1000");
        assertThat(line.getUnitPrice()).isEqualByComparingTo("2.45");
        assertThat(line.getAmount()).isEqualByComparingTo("2450");
        assertThat(result.getWarnings()).isEmpty();

        ArgumentCaptor<LlmRequest> captor = ArgumentCaptor.forClass(LlmRequest.class);
        verify(gateway).complete(captor.capture());
        LlmRequest sent = captor.getValue();
        assertThat(sent.getTask()).isEqualTo("bill-extraction");
        assertThat(sent.getCompanyRefId()).isEqualTo(COMPANY);
        assertThat(sent.isJsonOutput()).isTrue();
        assertThat(sent.getAttachments()).hasSize(1);
        assertThat(sent.getAttachments().get(0).isPdf()).isTrue();
        assertThat(sent.getUserPrompt()).contains("6100-000 | FUEL & DIESEL").contains("TOLL");
        assertThat(sent.getSampleOutput()).contains("Petronas");
    }

    @Test
    void unknownSupplierAndAccountProduceWarningsNotFailures() {
        String json = """
                {"supplier": {"name": "Unknown Vendor Sdn Bhd"}, "invoiceNo": "INV-1", "invoiceDate": "2026-08-01",
                 "totalAmount": 106, "taxAmount": 6, "lines": [
                   {"description": "Service", "quantity": 1, "unitPrice": 100, "taxPercent": 6, "accountCode": "9999-999"}]}
                """;
        when(gateway.complete(any())).thenReturn(answer(json));

        BillExtractionResponse result = service.extract(COMPANY, pdf(), "ollama");

        assertThat(result.getSupplier().getSupplierId()).isNull();
        assertThat(result.getSupplier().getExtractedName()).isEqualTo("Unknown Vendor Sdn Bhd");
        assertThat(result.getLines().get(0).getAccountId()).isNull();
        assertThat(result.getLines().get(0).getTaxAmount()).isEqualByComparingTo("6.00");
        assertThat(result.getLines().get(0).getAmount()).isEqualByComparingTo("106.00");
        assertThat(result.getWarnings())
                .anyMatch(w -> w.contains("Unknown Vendor Sdn Bhd"))
                .anyMatch(w -> w.contains("no matching account code"));
        assertThat(result.getHeader().getDueDate()).isNull();

        ArgumentCaptor<LlmRequest> captor = ArgumentCaptor.forClass(LlmRequest.class);
        verify(gateway).complete(captor.capture());
        assertThat(captor.getValue().getProviderKey()).isEqualTo("ollama");
    }

    @Test
    void totalOnlyDocumentGetsOneSyntheticLine() {
        String json = """
                {"supplier": {"name": "Shell Malaysia Trading"}, "invoiceNo": "R-77", "invoiceDate": "1 Aug 2026",
                 "totalAmount": 212.00, "taxAmount": 12.00, "lines": []}
                """;
        when(gateway.complete(any())).thenReturn(answer(json));

        BillExtractionResponse result = service.extract(COMPANY, pdf(), null);

        assertThat(result.getSupplier().getSupplierId()).isEqualTo(8);
        assertThat(result.getLines()).hasSize(1);
        assertThat(result.getLines().get(0).getUnitPrice()).isEqualByComparingTo("200.00");
        assertThat(result.getLines().get(0).getTaxPercent()).isEqualByComparingTo("6.00");
        assertThat(result.getLines().get(0).getAmount()).isEqualByComparingTo("212.00");
        assertThat(result.getWarnings()).anyMatch(w -> w.contains("one line was created"));
    }

    @Test
    void mismatchedTotalIsFlagged() {
        String json = """
                {"supplier": {"name": "Petronas Dagangan Bhd"}, "invoiceNo": "X", "invoiceDate": "2026-08-01",
                 "totalAmount": 500, "lines": [{"description": "a", "quantity": 1, "unitPrice": 100, "amount": 100}]}
                """;
        when(gateway.complete(any())).thenReturn(answer(json));

        BillExtractionResponse result = service.extract(COMPANY, pdf(), null);

        assertThat(result.getWarnings()).anyMatch(w -> w.contains("does not match the sum of lines"));
    }

    @Test
    void rejectsUnsupportedFileTypes() {
        MockMultipartFile doc = new MockMultipartFile("file", "bill.docx", "application/octet-stream", new byte[]{1});
        assertThatThrownBy(() -> service.extract(COMPANY, doc, null))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("PDF, PNG, JPG");
    }

    @Test
    void rejectsMissingCompanyAndEmptyFile() {
        assertThatThrownBy(() -> service.extract(null, pdf(), null)).isInstanceOf(InvalidRequestException.class);
        MockMultipartFile empty = new MockMultipartFile("file", "bill.pdf", "application/pdf", new byte[0]);
        assertThatThrownBy(() -> service.extract(COMPANY, empty, null)).isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void paymentTermsFallBackToTheDocumentText() {
        assertThat(BillExtractionServiceImpl.termsFromText("Net 30", terms())).isEqualTo(3);
        assertThat(BillExtractionServiceImpl.termsFromText("30 DAYS FROM INVOICE", terms())).isEqualTo(3);
        assertThat(BillExtractionServiceImpl.termsFromText("Cash on delivery", terms())).isEqualTo(4);
        assertThat(BillExtractionServiceImpl.termsFromText("45 days", terms())).isNull();
        assertThat(BillExtractionServiceImpl.termsFromText(null, terms())).isNull();
    }

    @Test
    void datesInCommonMalaysianLayoutsParse() {
        List<String> warnings = new java.util.ArrayList<>();
        assertThat(BillExtractionServiceImpl.parseDate("03/04/2026", "d", warnings)).hasToString("2026-04-03");
        assertThat(BillExtractionServiceImpl.parseDate("2026-04-03T00:00:00", "d", warnings)).hasToString("2026-04-03");
        assertThat(BillExtractionServiceImpl.parseDate("3 Apr 2026", "d", warnings)).hasToString("2026-04-03");
        assertThat(BillExtractionServiceImpl.parseDate("not a date", "d", warnings)).isNull();
        assertThat(warnings).hasSize(1);
    }

    private static List<PaymentTermsMaster> terms() {
        PaymentTermsMaster thirty = new PaymentTermsMaster();
        thirty.setId(3);
        thirty.setTermsName("30 DAYS");
        thirty.setTDays(30);
        PaymentTermsMaster cash = new PaymentTermsMaster();
        cash.setId(4);
        cash.setTermsName("CASH");
        cash.setTDays(0);
        return List.of(thirty, cash);
    }

    @Test
    void lenientNumbersHandleCurrencyAndSeparators() {
        assertThat(my.maleva.api.module.ai.billextraction.dto.LenientDecimalDeserializer.parse("RM 1,234.50"))
                .isEqualByComparingTo(new BigDecimal("1234.50"));
        assertThat(my.maleva.api.module.ai.billextraction.dto.LenientDecimalDeserializer.parse("(12.00)"))
                .isEqualByComparingTo(new BigDecimal("-12.00"));
        assertThat(my.maleva.api.module.ai.billextraction.dto.LenientDecimalDeserializer.parse("-")).isNull();
    }
}
