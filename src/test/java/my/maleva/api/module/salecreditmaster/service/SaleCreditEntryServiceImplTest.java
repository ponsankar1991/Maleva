package my.maleva.api.module.salecreditmaster.service;

import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.customer.entity.Customer;
import my.maleva.api.module.customer.repository.CustomerRepository;
import my.maleva.api.module.employee.repository.EmployeeMasterRepository;
import my.maleva.api.module.invoice.entity.SaleMaster;
import my.maleva.api.module.invoice.repository.SaleMasterRepository;
import my.maleva.api.module.itemmaster.repository.ItemMasterRepository;
import my.maleva.api.module.master.entity.SequenceNoMaster;
import my.maleva.api.module.master.repository.SequenceNoMasterRepository;
import my.maleva.api.module.salecreditmaster.dto.SaleCreditBillDto;
import my.maleva.api.module.salecreditmaster.dto.SaleCreditDetailRequest;
import my.maleva.api.module.salecreditmaster.dto.SaleCreditKnockOffRequest;
import my.maleva.api.module.salecreditmaster.dto.SaleCreditSaveRequest;
import my.maleva.api.module.salecreditmaster.dto.SaleCreditSaveResponse;
import my.maleva.api.module.salecreditmaster.entity.SaleCreditDetails;
import my.maleva.api.module.salecreditmaster.entity.SaleCreditKnockOff;
import my.maleva.api.module.salecreditmaster.entity.SaleCreditMaster;
import my.maleva.api.module.salecreditmaster.repository.SaleCreditBillQueryRepository;
import my.maleva.api.module.salecreditmaster.repository.SaleCreditDetailsRepository;
import my.maleva.api.module.salecreditmaster.repository.SaleCreditKnockOffRepository;
import my.maleva.api.module.salecreditmaster.repository.SaleCreditMasterRepository;
import my.maleva.api.module.salecreditmaster.repository.SaleCreditViewQueryRepository;
import my.maleva.api.module.salecreditmaster.service.impl.SaleCreditEntryServiceImpl;
import my.maleva.api.module.umo.repository.UomRepository;
import my.maleva.api.module.user.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The rules {@code SP_SaleCreditMaster} enforced (or failed to), now in Java:
 * the money the browser sends is recomputed, the knock-offs must account for
 * the note, no document may be over-credited, and the number continues even
 * when the sequence row was never created.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SaleCreditEntryServiceImplTest {

    private static final int COMPANY = 6;
    private static final int CUSTOMER = 10;
    private static final int INVOICE = 500;

    @Mock private SaleCreditMasterRepository creditNotes;
    @Mock private SaleCreditDetailsRepository creditDetails;
    @Mock private SaleCreditKnockOffRepository knockOffs;
    @Mock private SaleCreditBillQueryRepository billQueries;
    @Mock private SaleCreditViewQueryRepository viewQueries;
    @Mock private SequenceNoMasterRepository sequences;
    @Mock private AppUserRepository appUsers;
    @Mock private EmployeeMasterRepository employees;
    @Mock private CustomerRepository customers;
    @Mock private SaleMasterRepository saleMasters;
    @Mock private ItemMasterRepository itemMasters;
    @Mock private UomRepository uoms;

    private SaleCreditEntryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SaleCreditEntryServiceImpl(creditNotes, creditDetails, knockOffs, billQueries, viewQueries,
                sequences, appUsers, employees, customers, saleMasters, itemMasters, uoms);

        Customer customer = new Customer();
        customer.setId(CUSTOMER);
        customer.setCompanyRefId(COMPANY);
        customer.setCustomerName("ACME LOGISTICS SDN BHD");
        when(customers.findById(CUSTOMER)).thenReturn(Optional.of(customer));

        SaleMaster invoice = new SaleMaster();
        invoice.setId(INVOICE);
        invoice.setCompanyRefId(COMPANY);
        invoice.setCustomerRefId(CUSTOMER);
        invoice.setActive(1);
        invoice.setCNumberDisplay("INV000000123");
        when(saleMasters.findById(INVOICE)).thenReturn(Optional.of(invoice));

        // 212.00 still outstanding on that invoice
        when(billQueries.selectCustomerBills(eq(COMPANY), eq(CUSTOMER), anyInt()))
                .thenReturn(List.of(SaleCreditBillDto.builder()
                        .saleMasterRefId(INVOICE)
                        .billNo("INV000000123")
                        .billAmount(new BigDecimal("212.00"))
                        .settled(BigDecimal.ZERO)
                        .balance(new BigDecimal("212.00"))
                        .build()));

        when(creditNotes.save(any(SaleCreditMaster.class))).thenAnswer(call -> {
            SaleCreditMaster note = call.getArgument(0);
            if (note.getId() == null) {
                note.setId(77);
            }
            return note;
        });
    }

    @Test
    void lineAndHeaderMoneyAreComputedFromQuantityRateAndTax() {
        // 2 × 100.00 @ 6% → tax 12.00, line 212.00; header gross = amount = 212.00
        SaleCreditSaveResponse response = service.save(request(line(2d, 100d, 6d), knockOff("212.00")), COMPANY);

        assertThat(response.getOk()).isTrue();
        assertThat(response.getName()).isEqualTo("CN000000001");

        ArgumentCaptor<List<SaleCreditDetails>> lines = ArgumentCaptor.forClass(List.class);
        verify(creditDetails).saveAll(lines.capture());
        assertThat(lines.getValue()).singleElement().satisfies(line -> {
            assertThat(line.getTaxAmount()).isEqualTo(12.00d);
            assertThat(line.getAmount()).isEqualTo(212.00d);
            // The screen zeroed these on every keystroke; they must never be null.
            assertThat(line.getDiscAmount()).isZero();
            assertThat(line.getNetSalesRate()).isZero();
        });

        ArgumentCaptor<SaleCreditMaster> saved = ArgumentCaptor.forClass(SaleCreditMaster.class);
        verify(creditNotes, org.mockito.Mockito.atLeastOnce()).save(saved.capture());
        SaleCreditMaster note = saved.getValue();
        assertThat(note.getAmount()).isEqualByComparingTo("212.00");
        assertThat(note.getGrossAmount()).isEqualTo(212.00d);
        assertThat(note.getTaxAmount()).isEqualTo(12.00d);
        // Rounding to the ringgit, as the screen's RoundoffPaise = "2" did.
        assertThat(note.getCoinage()).isEqualTo(0.00d);
        assertThat(note.getActualAmount()).isEqualTo(212.00d);
    }

    @Test
    void knockOffsThatDoNotAddUpToTheNoteAreRefused() {
        SaleCreditSaveResponse response = service.save(request(line(2d, 100d, 6d), knockOff("100.00")), COMPANY);

        assertThat(response.getOk()).isFalse();
        assertThat(response.getMessage()).contains("Total Not Matching");
        verify(creditDetails, never()).saveAll(any());
    }

    @Test
    void aDocumentCannotBeCreditedBeyondItsOutstandingBalance() {
        // 3 × 100.00 @ 6% = 318.00 against an invoice that owes 212.00
        SaleCreditSaveResponse response = service.save(request(line(3d, 100d, 6d), knockOff("318.00")), COMPANY);

        assertThat(response.getOk()).isFalse();
        assertThat(response.getMessage()).contains("only 212.00 outstanding");
        verify(knockOffs, never()).saveAll(any());
    }

    @Test
    void theKnockOffIsStoredAgainstTheInvoiceWithItsConvertedValue() {
        SaleCreditSaveResponse response = service.save(request(line(2d, 100d, 6d), knockOff("212.00")), COMPANY);
        assertThat(response.getOk()).isTrue();

        ArgumentCaptor<List<SaleCreditKnockOff>> rows = ArgumentCaptor.forClass(List.class);
        verify(knockOffs).saveAll(rows.capture());
        assertThat(rows.getValue()).singleElement().satisfies(row -> {
            assertThat(row.getSaleMasterRefId()).isEqualTo(INVOICE);
            assertThat(row.getCustomerOpenRefId()).isNull();
            assertThat(row.getSaleCreditAmount()).isEqualByComparingTo("212.00");
            assertThat(row.getActualAmount()).isEqualTo(212.00d);
        });
    }

    @Test
    void aMissingSequenceRowIsCreatedAndTheNumberContinuesFromTheNotesAlreadyIssued() {
        // The procedure only ever ran UPDATE SequenceNoMaster, so a company with
        // no row kept numbering every note CN000000001.
        when(sequences.findMaxSequenceNoByCompanyAndSequenceName(COMPANY, "SaleCreditMaster")).thenReturn(null);
        when(creditNotes.findMaxCNumber(COMPANY)).thenReturn(41);
        when(sequences.findByCompanyRefIdAndSequenceName(COMPANY, "SaleCreditMaster")).thenReturn(Optional.empty());

        SaleCreditSaveResponse response = service.save(request(line(2d, 100d, 6d), knockOff("212.00")), COMPANY);

        assertThat(response.getName()).isEqualTo("CN000000042");
        ArgumentCaptor<SequenceNoMaster> sequence = ArgumentCaptor.forClass(SequenceNoMaster.class);
        verify(sequences).save(sequence.capture());
        assertThat(sequence.getValue().getSequenceNo()).isEqualTo(42);
        assertThat(sequence.getValue().getSequenceName()).isEqualTo("SaleCreditMaster");
        assertThat(sequence.getValue().getCompanyRefId()).isEqualTo(COMPANY);
    }

    @Test
    void aCreditNoteAgainstAnotherCustomersInvoiceIsRefused() {
        SaleMaster other = new SaleMaster();
        other.setId(INVOICE);
        other.setCompanyRefId(COMPANY);
        other.setCustomerRefId(999);
        other.setActive(1);
        other.setCNumberDisplay("INV000000123");
        when(saleMasters.findById(INVOICE)).thenReturn(Optional.of(other));

        SaleCreditSaveResponse response = service.save(request(line(2d, 100d, 6d), knockOff("212.00")), COMPANY);

        assertThat(response.getOk()).isFalse();
        assertThat(response.getMessage()).contains("belongs to a different customer");
    }

    @Test
    void savingWithoutACurrencyRateIsRefused() {
        SaleCreditSaveRequest request = request(line(2d, 100d, 6d), knockOff("212.00"));
        request.setCurrencyValue(0d);

        SaleCreditSaveResponse response = service.save(request, COMPANY);

        assertThat(response.getOk()).isFalse();
        assertThat(response.getMessage()).contains("currency rate is missing");
    }

    @Test
    void aNoteAlreadyInQneCannotBeDeleted() {
        SaleCreditMaster note = new SaleCreditMaster();
        note.setId(77);
        note.setCompanyRefId(COMPANY);
        note.setCNumberDisplay("CN000000042");
        note.setQneCode("CN-0001");
        when(creditNotes.findById(77)).thenReturn(Optional.of(note));

        assertThatThrownBy(() -> service.delete(77, COMPANY))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("already in QNE");
        verify(creditNotes, never()).delete(any());
    }

    @Test
    void deletingRemovesTheLinesAndKnockOffsWithTheNote() {
        SaleCreditMaster note = new SaleCreditMaster();
        note.setId(77);
        note.setCompanyRefId(COMPANY);
        note.setCNumberDisplay("CN000000042");
        when(creditNotes.findById(77)).thenReturn(Optional.of(note));

        // Legacy ran a bare delete of the master and left these rows behind,
        // still reducing the balance of invoices with no credit note.
        assertThat(service.delete(77, COMPANY)).contains("CN000000042");
        verify(creditDetails).deleteBySaleCreditMasterRefId(77);
        verify(knockOffs).deleteBySaleCreditMasterRefId(77);
        verify(creditNotes).delete(note);
    }

    // ────────────────────────────────────────────────────────────── fixtures ──

    private static SaleCreditSaveRequest request(SaleCreditDetailRequest line, SaleCreditKnockOffRequest settlement) {
        return SaleCreditSaveRequest.builder()
                .id(0)
                .companyRefId(COMPANY)
                .customerRefId(CUSTOMER)
                .saleMasterRefId(INVOICE)
                .saleDate("2026-09-07")
                .currencyValue(1d)
                .remarks("SHORT DELIVERY")
                .saleCreditDetails(List.of(line))
                .saleCreditKnockOffDetails(List.of(settlement))
                .build();
    }

    private static SaleCreditDetailRequest line(double qty, double rate, double taxPercent) {
        return SaleCreditDetailRequest.builder()
                .itemMasterRefId(3)
                .itemQty(qty)
                .salesRate(rate)
                .taxPercent(taxPercent)
                .build();
    }

    private static SaleCreditKnockOffRequest knockOff(String amount) {
        return SaleCreditKnockOffRequest.builder()
                .saleMasterRefId(INVOICE)
                .saleCreditAmount(new BigDecimal(amount))
                .build();
    }
}
