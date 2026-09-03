package my.maleva.api.module.ai.purchaseorder.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.integration.llm.LlmGateway;
import my.maleva.api.integration.llm.LlmRequest;
import my.maleva.api.integration.llm.LlmResponse;
import my.maleva.api.module.accounting.entity.GLAccounts;
import my.maleva.api.module.accounting.repository.GLAccountsRepository;
import my.maleva.api.module.ai.purchaseorder.dto.PurchaseOrderExtractionResponse;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PurchaseOrderExtractionServiceImplTest {

    private static final int COMPANY = 6;

    @Mock
    private LlmGateway gateway;
    @Mock
    private SupplierRepository supplierRepository;
    @Mock
    private GLAccountsRepository glAccountsRepository;
    @Mock
    private PaymentTermsMasterRepository paymentTermsRepository;
    @Mock
    private BillsOrderMasterRepository billsOrderMasterRepository;
    @Mock
    private TruckMasterRepository truckMasterRepository;
    @Mock
    private DriverMasterRepository driverMasterRepository;
    @Mock
    private ProductMasterRepository productMasterRepository;

    private PurchaseOrderExtractionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PurchaseOrderExtractionServiceImpl(gateway, new ObjectMapper(), supplierRepository,
                glAccountsRepository, paymentTermsRepository, billsOrderMasterRepository, truckMasterRepository,
                driverMasterRepository, productMasterRepository);

        Supplier scania = new Supplier();
        scania.setId(9);
        scania.setSupplierName("SCANIA (MALAYSIA) SDN BHD");
        scania.setPaymentTermsRefid(3);
        Supplier petronas = new Supplier();
        petronas.setId(7);
        petronas.setSupplierName("PETRONAS DAGANGAN BHD");
        when(supplierRepository.findByCompanyRefIdAndActive(COMPANY, 1)).thenReturn(List.of(scania, petronas));

        GLAccounts repair = new GLAccounts();
        repair.setGlAccountCode("6300-000");
        repair.setDescription("REPAIR & MAINTENANCE");
        repair.setRowIndex(6300);
        GLAccounts port = new GLAccounts();
        port.setGlAccountCode("6400-000");
        port.setDescription("PORT CHARGES");
        port.setRowIndex(6400);
        when(glAccountsRepository.findByCompanyAndExpense(COMPANY, 0)).thenReturn(List.of(repair, port));

        PaymentTermsMaster thirty = new PaymentTermsMaster();
        thirty.setId(3);
        thirty.setCompanyRefId(COMPANY);
        thirty.setTermsName("30 DAYS");
        thirty.setTDays(30);
        thirty.setActive(1);
        when(paymentTermsRepository.findAll()).thenReturn(List.of(thirty));

        when(billsOrderMasterRepository.findDistinctDescriptionsByCompany(COMPANY))
                .thenReturn(List.of("PORT CHARGES", "Maintenance", " "));
        PaymentVoucherComboDto existing = mock(PaymentVoucherComboDto.class);
        when(existing.getInvoiceNo()).thenReturn("QT-2026-08-0421");
        when(existing.getAccountName()).thenReturn("SCANIA (MALAYSIA) SDN BHD");
        when(billsOrderMasterRepository.findInvoiceNumbersByCompany(COMPANY)).thenReturn(List.of(existing));

        TruckMaster jka = new TruckMaster();
        jka.setId(11);
        jka.setTruckName("JKA 1234");
        TruckMaster wxy = new TruckMaster();
        wxy.setId(12);
        wxy.setTruckName("WXY9876");
        when(truckMasterRepository.findByCompanyRefIdAndActive(COMPANY, 1)).thenReturn(List.of(jka, wxy));

        DriverMaster ahmad = new DriverMaster();
        ahmad.setId(21);
        ahmad.setDriverName("AHMAD BIN ALI");
        DriverMaster muthu = new DriverMaster();
        muthu.setId(22);
        muthu.setDriverName("MUTHU A/L RAMAN");
        when(driverMasterRepository.findByCompanyRefIdAndActive(COMPANY, 1)).thenReturn(List.of(ahmad, muthu));

        ProductMaster brakePad = new ProductMaster();
        brakePad.setId(31);
        brakePad.setProdCode("1906399");
        brakePad.setPname("BRAKE PAD SET");
        when(productMasterRepository.findByCompanyRefIdAndActivestatus(COMPANY, 1)).thenReturn(List.of(brakePad));
    }

    private static MockMultipartFile pdf() {
        return new MockMultipartFile("file", "scania.pdf", "application/pdf", "%PDF-1.4 fake".getBytes());
    }

    private static LlmResponse answer(String json) {
        return new LlmResponse("gemini", "gemini-3.5-flash-lite", json, 900L, 300L, 1400, "stop");
    }

    @Test
    void resolvesSupplierTruckDriverAccountsStoreItemAndDates() {
        String json = """
                {
                  "documentType": "Quotation",
                  "supplier": {"name": "Scania (Malaysia) Sdn Bhd", "sstNo": "J31-1808-22001234"},
                  "documentNo": "qt-2026-08-0421",
                  "documentDate": "30/08/2026",
                  "deliveryDate": "5 Sep 2026",
                  "paymentTermsText": "30 days",
                  "vehiclePlateNo": "jka-1234",
                  "driverName": "Ahmad Ali",
                  "jobNo": "SO2609/012",
                  "descriptionCategory": "maintenance",
                  "currencyCode": "RM",
                  "subtotal": 1860, "taxAmount": 0, "totalAmount": "1,860.00",
                  "lines": [
                    {"description": "Brake pad set front axle", "itemCode": "1906399", "quantity": 2, "uom": "SET", "unitPrice": 680, "taxPercent": 0, "amount": 1360, "accountCode": "6300-000"},
                    {"description": "Labour - brake service", "quantity": 1, "unitPrice": 500, "amount": 500, "accountCode": "6300-000", "serialNo": "WO-77"}
                  ],
                  "remarks": "Valid 7 days"
                }
                """;
        when(gateway.complete(any())).thenReturn(answer(json));

        PurchaseOrderExtractionResponse result = service.extract(COMPANY, pdf(), null);

        assertThat(result.getProvider()).isEqualTo("gemini");
        assertThat(result.getSupplier().getSupplierId()).isEqualTo(9);
        assertThat(result.getSupplier().getPaymentTermsId()).isEqualTo(3);
        PurchaseOrderExtractionResponse.Header header = result.getHeader();
        assertThat(header.getDocumentType()).isEqualTo("QUOTATION");
        assertThat(header.getInvoiceNo()).isEqualTo("qt-2026-08-0421");
        assertThat(header.getInvoiceDate()).isEqualTo("2026-08-30");
        assertThat(header.getPoDate()).isEqualTo("2026-08-30");
        assertThat(header.getDueDate()).isEqualTo("2026-09-29");
        assertThat(header.getDeliveryDate()).isEqualTo("2026-09-05");
        assertThat(header.getCurrencyCode()).isEqualTo("MYR");
        assertThat(header.getDescription()).isEqualTo("MAINTENANCE");
        assertThat(header.getTruckId()).isEqualTo(11);
        assertThat(header.getTruckName()).isEqualTo("JKA 1234");
        assertThat(header.getDriverId()).isEqualTo(21);
        assertThat(header.getDriverMatchedName()).isEqualTo("AHMAD BIN ALI");
        assertThat(header.getJobNo()).isEqualTo("SO2609/012");
        assertThat(header.getRemarks()).isEqualTo("Valid 7 days");
        assertThat(header.getTotalAmount()).isEqualByComparingTo("1860.00");

        assertThat(result.getLines()).hasSize(2);
        PurchaseOrderExtractionResponse.Line pads = result.getLines().get(0);
        assertThat(pads.getAccountId()).isEqualTo(6300);
        assertThat(pads.getStoreItemId()).isEqualTo(31);
        assertThat(pads.getStoreItemCode()).isEqualTo("1906399");
        assertThat(pads.getUom()).isEqualTo("SET");
        assertThat(pads.getQuantity()).isEqualByComparingTo("2");
        assertThat(pads.getUnitPrice()).isEqualByComparingTo("680");
        assertThat(pads.getAmount()).isEqualByComparingTo("1360");
        PurchaseOrderExtractionResponse.Line labour = result.getLines().get(1);
        assertThat(labour.getSerialNo()).isEqualTo("WO-77");
        assertThat(labour.getTaxPercent()).isEqualByComparingTo("0");
        assertThat(labour.getStoreItemId()).isNull();

        // The only warning is the duplicate document number already on a PO.
        assertThat(result.getWarnings()).hasSize(1);
        assertThat(result.getWarnings().get(0)).contains("already exists").contains("SCANIA");

        ArgumentCaptor<LlmRequest> captor = ArgumentCaptor.forClass(LlmRequest.class);
        verify(gateway).complete(captor.capture());
        LlmRequest sent = captor.getValue();
        assertThat(sent.getTask()).isEqualTo("purchase-order-extraction");
        assertThat(sent.getCompanyRefId()).isEqualTo(COMPANY);
        assertThat(sent.isJsonOutput()).isTrue();
        assertThat(sent.getUserPrompt()).contains("PORT CHARGES").contains("MAINTENANCE").contains("6300-000 | REPAIR & MAINTENANCE");
        assertThat(sent.getSampleOutput()).contains("Scania");
    }

    @Test
    void unmatchedTruckDriverAndDescriptionBecomeWarnings() {
        String json = """
                {"documentType": "invoice", "supplier": {"name": "Petronas Dagangan Berhad"},
                 "documentNo": "INV-9", "documentDate": "2026-08-01", "vehiclePlateNo": "PNG 1", "driverName": "Zulkifli",
                 "descriptionCategory": "TYRES", "currencyCode": "USD", "totalAmount": 100,
                 "lines": [{"description": "Diesel", "quantity": 1, "unitPrice": 100, "amount": 100, "accountCode": "9999"}]}
                """;
        when(gateway.complete(any())).thenReturn(answer(json));

        PurchaseOrderExtractionResponse result = service.extract(COMPANY, pdf(), "stub");

        assertThat(result.getSupplier().getSupplierId()).isEqualTo(7);
        assertThat(result.getHeader().getDocumentType()).isEqualTo("INVOICE");
        assertThat(result.getHeader().getTruckId()).isNull();
        assertThat(result.getHeader().getDriverId()).isNull();
        assertThat(result.getHeader().getDescription()).isEqualTo("TYRES");
        assertThat(result.getHeader().getCurrencyCode()).isEqualTo("USD");
        assertThat(result.getLines().get(0).getAccountId()).isNull();
        assertThat(result.getWarnings())
                .anyMatch(w -> w.contains("Vehicle 'PNG 1'"))
                .anyMatch(w -> w.contains("Driver 'Zulkifli'"))
                .anyMatch(w -> w.contains("Description 'TYRES'"))
                .anyMatch(w -> w.contains("currency is USD"))
                .anyMatch(w -> w.contains("no matching account code"));

        ArgumentCaptor<LlmRequest> captor = ArgumentCaptor.forClass(LlmRequest.class);
        verify(gateway).complete(captor.capture());
        assertThat(captor.getValue().getProviderKey()).isEqualTo("stub");
    }

    @Test
    void totalOnlyDocumentGetsOneLineNamedAfterTheDocument() {
        String json = """
                {"documentType": "DELIVERY_ORDER", "supplier": {"name": "Scania Malaysia"}, "documentNo": "DO-5",
                 "documentDate": "2026-08-02", "totalAmount": 212, "taxAmount": 12, "lines": []}
                """;
        when(gateway.complete(any())).thenReturn(answer(json));

        PurchaseOrderExtractionResponse result = service.extract(COMPANY, pdf(), null);

        assertThat(result.getLines()).hasSize(1);
        assertThat(result.getLines().get(0).getDescription()).isEqualTo("AS PER DELIVERY ORDER DO-5");
        assertThat(result.getLines().get(0).getUnitPrice()).isEqualByComparingTo("200.00");
        assertThat(result.getLines().get(0).getTaxPercent()).isEqualByComparingTo("6.00");
        assertThat(result.getWarnings()).anyMatch(w -> w.contains("one line was created"));
    }

    @Test
    void rejectsBadInput() {
        assertThatThrownBy(() -> service.extract(null, pdf(), null)).isInstanceOf(InvalidRequestException.class);
        MockMultipartFile doc = new MockMultipartFile("file", "quote.docx", "application/octet-stream", new byte[]{1});
        assertThatThrownBy(() -> service.extract(COMPANY, doc, null)).isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void fleetMatcherHandlesPlatesAndMalaysianNames() {
        TruckMaster a = new TruckMaster();
        a.setId(1);
        a.setTruckName("JKA 1234");
        TruckMaster b = new TruckMaster();
        b.setId(2);
        b.setTruckName("JKA1234 (TRAILER)");
        assertThat(FleetMatcher.truck("jka-1234", List.of(a, b))).map(TruckMaster::getId).contains(1);
        assertThat(FleetMatcher.truck("JKA", List.of(a, b))).isEmpty();
        assertThat(FleetMatcher.truck("1234", List.of(b))).map(TruckMaster::getId).contains(2);

        DriverMaster d1 = new DriverMaster();
        d1.setId(1);
        d1.setDriverName("AHMAD BIN ALI");
        DriverMaster d2 = new DriverMaster();
        d2.setId(2);
        d2.setDriverName("AHMAD B. ALI");
        DriverMaster d3 = new DriverMaster();
        d3.setId(3);
        d3.setDriverName("ALI BIN AHMAD");
        assertThat(FleetMatcher.driver("Ahmad Ali", List.of(d1))).map(DriverMaster::getId).contains(1);
        // An exact normalised match beats a token permutation...
        assertThat(FleetMatcher.driver("Ahmad Ali", List.of(d1, d3))).map(DriverMaster::getId).contains(1);
        // ...but two drivers that normalise to the same name are a tie.
        assertThat(FleetMatcher.driver("Ahmad Ali", List.of(d1, d2))).isEmpty();
        assertThat(FleetMatcher.driver("Muthu a/l Raman", List.of(d1))).isEmpty();
    }

    @Test
    void documentTypeIsNormalised() {
        assertThat(PurchaseOrderExtractionServiceImpl.documentType("tax invoice")).isEqualTo("INVOICE");
        assertThat(PurchaseOrderExtractionServiceImpl.documentType("proforma")).isEqualTo("PROFORMA_INVOICE");
        assertThat(PurchaseOrderExtractionServiceImpl.documentType("DO")).isEqualTo("DELIVERY_ORDER");
        assertThat(PurchaseOrderExtractionServiceImpl.documentType("something")).isEqualTo("OTHER");
        assertThat(PurchaseOrderExtractionServiceImpl.documentType(null)).isNull();
    }
}
