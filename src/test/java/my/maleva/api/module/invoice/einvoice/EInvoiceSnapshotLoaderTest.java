package my.maleva.api.module.invoice.einvoice;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * The boundary between JPA entities (Double, free text) and the snapshot the
 * validator trusts (BigDecimal, normalised). Every rule here is one the
 * legacy INNER-join query got wrong silently.
 */
class EInvoiceSnapshotLoaderTest {

    private SaleMasterRepository saleMasters;
    private SaleDetailsRepository saleDetails;
    private CustomerRepository customers;
    private SymbolMasterRepository symbols;
    private CountryMasterRepository countries;
    private ItemMasterRepository items;
    private UomRepository uoms;
    private ClassificationRepository classifications;
    private EInvoiceSnapshotLoader loader;

    @BeforeEach
    void setUp() {
        saleMasters = Mockito.mock(SaleMasterRepository.class);
        saleDetails = Mockito.mock(SaleDetailsRepository.class);
        customers = Mockito.mock(CustomerRepository.class);
        symbols = Mockito.mock(SymbolMasterRepository.class);
        countries = Mockito.mock(CountryMasterRepository.class);
        items = Mockito.mock(ItemMasterRepository.class);
        uoms = Mockito.mock(UomRepository.class);
        classifications = Mockito.mock(ClassificationRepository.class);
        loader = new EInvoiceSnapshotLoader(saleMasters, saleDetails, customers, symbols, countries, items, uoms, classifications);

        SaleMaster invoice = new SaleMaster();
        invoice.setId(4711);
        invoice.setCompanyRefId(1);
        invoice.setCNumberDisplay("INV000004711");
        invoice.setCustomerRefId(9);
        invoice.setActive(1);
        invoice.setAmount((double) 262.0f);
        invoice.setTaxAmount((double) 12.0f);
        invoice.setGrossAmount((double) 262.0f);
        when(saleMasters.findById(4711)).thenReturn(Optional.of(invoice));

        Customer customer = new Customer();
        customer.setId(9);
        customer.setCustomerName("ACME");
        customer.setSymbolRefid(4);
        customer.setCountryId(60);
        when(customers.findById(9)).thenReturn(Optional.of(customer));

        SymbolMaster rm = new SymbolMaster();
        rm.setId(4);
        rm.setSName(" rm ");
        when(symbols.findById(4)).thenReturn(Optional.of(rm));

        CountryMaster mys = new CountryMaster();
        mys.setId(60);
        mys.setCode("MYS");
        when(countries.findById(60)).thenReturn(Optional.of(mys));

        SaleDetails second = detail(102, 56, 1f, 50f, 0f, 0f, 50f);
        SaleDetails first = detail(101, 55, 2f, 100f, 6f, 12f, 212f);
        when(saleDetails.findBySaleMasterRefId(4711)).thenReturn(List.of(second, first)); // out of order on purpose

        ItemMaster handling = item(55, "HANDLING", "CARGO HANDLING", 7, 3);
        ItemMaster docfee = item(56, "DOCFEE", "DOCUMENTATION FEE", 7, 3);
        when(items.findAllById(any())).thenReturn(List.of(handling, docfee));

        Uom unit = new Uom();
        unit.setId(7);
        unit.setDescription("UNIT(S)");
        when(uoms.findAllById(any())).thenReturn(List.of(unit));

        Classification cls = new Classification();
        cls.setId(3);
        cls.setClassificationCode(22);
        when(classifications.findAllById(any())).thenReturn(List.of(cls));
    }

    @Test
    void float32EntityValuesBecomeSenExactMoneyAndTheCurrencyIsNormalised() {
        EInvoiceSnapshot snapshot = loader.load(4711, 1).orElseThrow();

        assertThat(snapshot.loadProblems()).isEmpty();
        assertThat(snapshot.header().amount().toPlainString()).isEqualTo("262.00");
        assertThat(snapshot.customer().currencyCode()).isEqualTo("MYR");
        assertThat(snapshot.customer().countryCode()).isEqualTo("MYS");
    }

    @Test
    void linesAreNumberedInSaleDetailsIdOrderNotQueryOrder() {
        EInvoiceSnapshot snapshot = loader.load(4711, 1).orElseThrow();

        assertThat(snapshot.lines()).extracting(EInvoiceSnapshot.Line::detailId).containsExactly(101, 102);
        assertThat(snapshot.lines()).extracting(EInvoiceSnapshot.Line::rowNumber).containsExactly(1, 2);
        EInvoiceSnapshot.Line first = snapshot.lines().get(0);
        assertThat(first.productCode()).isEqualTo("HANDLING");
        assertThat(first.uom()).isEqualTo("UNIT(S)");
        assertThat(first.classificationCode()).isEqualTo(22);
        assertThat(first.quantity().toPlainString()).isEqualTo("2.00");
        assertThat(first.amount().toPlainString()).isEqualTo("212.00");
    }

    @Test
    void wrongCompanyIsNotFound() {
        assertThat(loader.load(4711, 2)).isEmpty();
        assertThat(loader.load(999, 1)).isEmpty();
    }

    @Test
    void missingItemOrUomBecomesANamedProblemAndTheLineIsKept() {
        // legacy's INNER JOIN silently dropped such lines and pushed the rest
        when(items.findAllById(any())).thenReturn(List.of(item(55, "HANDLING", "CARGO HANDLING", 99, 3)));

        EInvoiceSnapshot snapshot = loader.load(4711, 1).orElseThrow();

        assertThat(snapshot.lines()).hasSize(2);
        assertThat(snapshot.loadProblems()).extracting(EInvoiceProblem::code)
                .containsExactlyInAnyOrder("line.uom.missing", "line.item.missing");
        assertThat(snapshot.loadProblems().get(0).message()).contains("INV000004711");
    }

    @Test
    void customerWithoutACurrencyIsReported() {
        when(symbols.findById(4)).thenReturn(Optional.empty());

        EInvoiceSnapshot snapshot = loader.load(4711, 1).orElseThrow();

        assertThat(snapshot.customer().currencyCode()).isNull();
        assertThat(snapshot.loadProblems()).extracting(EInvoiceProblem::code).containsExactly("customer.currency.missing");
    }

    @Test
    void classificationCodeZeroCountsAsMissing() {
        Classification zero = new Classification();
        zero.setId(3);
        zero.setClassificationCode(0);
        when(classifications.findAllById(any())).thenReturn(List.of(zero));

        EInvoiceSnapshot snapshot = loader.load(4711, 1).orElseThrow();

        assertThat(snapshot.lines().get(0).classificationCode()).isNull();
    }

    @Test
    void currencyNormalisationMapsOnlyRmToMyr() {
        assertThat(EInvoiceSnapshotLoader.normaliseCurrency("RM")).isEqualTo("MYR");
        assertThat(EInvoiceSnapshotLoader.normaliseCurrency(" rm ")).isEqualTo("MYR");
        assertThat(EInvoiceSnapshotLoader.normaliseCurrency("sgd")).isEqualTo("SGD");
        assertThat(EInvoiceSnapshotLoader.normaliseCurrency(null)).isNull();
    }

    private static SaleDetails detail(int id, int itemId, float qty, float rate, float pct, float tax, float amount) {
        SaleDetails d = new SaleDetails();
        d.setId(id);
        d.setSaleMasterRefId(4711);
        d.setItemMasterRefId(itemId);
        d.setItemQty((double) qty);
        d.setSalesRate((double) rate);
        d.setTaxPercent((double) pct);
        d.setTaxAmount((double) tax);
        d.setAmount((double) amount);
        return d;
    }

    private static ItemMaster item(int id, String code, String name, int uomId, int classificationId) {
        ItemMaster i = new ItemMaster();
        i.setId(id);
        i.setProdCode(code);
        i.setPName(name);
        i.setUomCode(uomId);
        i.setSaleClassification(classificationId);
        return i;
    }
}
