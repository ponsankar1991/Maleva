package my.maleva.api.module.billing.bill.service;

import my.maleva.api.module.accounting.repository.GLAccountsRepository;
import my.maleva.api.module.billing.bill.dto.BillDetailsInsertDto;
import my.maleva.api.module.billing.bill.dto.BillMasterInsertDto;
import my.maleva.api.module.billing.bill.dto.BillMasterSaveResponseDto;
import my.maleva.api.module.billing.bill.entity.BillDetails;
import my.maleva.api.module.billing.bill.entity.BillMaster;
import my.maleva.api.module.billing.bill.repository.BillDetailsRepository;
import my.maleva.api.module.billing.bill.repository.BillMasterRepository;
import my.maleva.api.module.billing.billorder.entity.BillsOrderMaster;
import my.maleva.api.module.billing.billorder.repository.BillsOrderMasterRepository;
import my.maleva.api.module.employee.repository.EmployeeMasterRepository;
import my.maleva.api.module.fleet.repository.DriverMasterRepository;
import my.maleva.api.module.fleet.repository.TruckMasterRepository;
import my.maleva.api.module.master.repository.SequenceNoMasterRepository;
import my.maleva.api.module.master.repository.SymbolMasterRepository;
import my.maleva.api.module.payment.repository.PaymentTermsMasterRepository;
import my.maleva.api.module.supplier.repository.SupplierRepository;
import my.maleva.api.module.user.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pins the behaviours {@code SP_BillMaster} performed that are easy to lose in
 * a hand port — the ones with no visible symptom until the accounts are wrong.
 */
class BillMasterSaveTest {

    private BillMasterRepository billMasters;
    private BillDetailsRepository billDetails;
    private SupplierRepository suppliers;
    private PaymentTermsMasterRepository paymentTerms;
    private SequenceNoMasterRepository sequences;
    private BillsOrderMasterRepository billsOrders;
    private AppUserRepository appUsers;
    private EmployeeMasterRepository employees;
    private TruckMasterRepository trucks;
    private DriverMasterRepository drivers;
    private BillMasterTransactionService service;

    @BeforeEach
    void setUp() {
        billMasters = mock(BillMasterRepository.class);
        billDetails = mock(BillDetailsRepository.class);
        suppliers = mock(SupplierRepository.class);
        paymentTerms = mock(PaymentTermsMasterRepository.class);
        sequences = mock(SequenceNoMasterRepository.class);
        billsOrders = mock(BillsOrderMasterRepository.class);
        appUsers = mock(AppUserRepository.class);
        employees = mock(EmployeeMasterRepository.class);
        trucks = mock(TruckMasterRepository.class);
        drivers = mock(DriverMasterRepository.class);

        service = new BillMasterTransactionService(
                billMasters, billDetails, suppliers, paymentTerms, sequences,
                mock(SymbolMasterRepository.class), mock(GLAccountsRepository.class),
                billsOrders, appUsers, employees, trucks, drivers,
                mock(NamedParameterJdbcTemplate.class));

        when(suppliers.existsById(anyInt())).thenReturn(true);
        when(billDetails.findByBillMasterRefId(anyInt())).thenReturn(List.of());
        when(sequences.findMaxBillMasterSequenceNo(anyInt(), anyInt(), anyInt())).thenReturn(4);
        when(billMasters.save(any(BillMaster.class))).thenAnswer(call -> {
            BillMaster saved = call.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(77);
            }
            return saved;
        });
    }

    private BillMasterInsertDto bill() {
        return BillMasterInsertDto.builder()
                .id(0)
                .supplierRefId(3)
                .employeeRefId(9)
                .paymentTermsRefid(2)
                .saleDate(LocalDateTime.of(2026, 8, 27, 10, 0))
                .amount(500f)
                .billDetails(List.of(BillDetailsInsertDto.builder()
                        .accountMasterRefId(6100)
                        .itemQty(1f)
                        .salesRate(500f)
                        .amount(500f)
                        .build()))
                .build();
    }

    private void allReferencesValid() {
        when(appUsers.existsByIdAndCompanyRefIdAndActive(anyInt(), anyInt(), anyInt()))
                .thenReturn(true);
        when(employees.existsByIdAndCompanyRefIdAndActive(anyInt(), anyInt(), anyInt()))
                .thenReturn(true);
        when(trucks.existsByIdAndCompanyRefIdAndActive(anyInt(), anyInt(), anyInt()))
                .thenReturn(true);
        when(drivers.existsByIdAndCompanyRefIdAndActive(anyInt(), anyInt(), anyInt()))
                .thenReturn(true);
        when(paymentTerms.existsByIdAndCompanyRefIdAndActive(anyInt(), anyInt(), anyInt()))
                .thenReturn(true);
    }

    @Test
    void numbersANewBillFromItsOwnMonthAndBumpsTheCounter() {
        allReferencesValid();

        BillMasterSaveResponseDto result = service.save(bill(), 6);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getId()).isEqualTo(77);
        // Counter stood at 4 for August 2026, so this bill is the fifth.
        assertThat(result.getBillNoDisplay()).isEqualTo("BIL2608/005");
    }

    @Test
    void stampsLastEmployeeSoTheGridShowsWhoTouchedItLast() {
        allReferencesValid();

        service.save(bill(), 6);

        verify(billMasters, org.mockito.Mockito.atLeastOnce()).save(
                org.mockito.ArgumentMatchers.argThat(saved ->
                        saved.getLastEmployeeRefId() != null
                                && saved.getLastEmployeeRefId() == 9));
    }

    @Test
    void flagsThePurchaseOrderAsInvoicedSoItCannotBeBilledTwice() {
        allReferencesValid();
        BillsOrderMaster order = new BillsOrderMaster();
        order.setId(31);
        when(billsOrders.findById(31)).thenReturn(Optional.of(order));

        BillMasterInsertDto dto = bill();
        dto.setBillsOrderMasterRefId(31);
        service.save(dto, 6);

        verify(billsOrders).save(order);
        assertThat(order.getPStatus()).isEqualTo(1);
        assertThat(order.getBillStatus()).isEqualTo("INVOICE MADE");
        assertThat(order.getModifiedBy()).isEqualTo("From Bills");
    }

    @Test
    void editingABillDoesNotRepointItAtAnotherPurchaseOrder() {
        allReferencesValid();
        BillMaster existing = new BillMaster();
        existing.setId(77);
        existing.setCompanyRefId(6);
        existing.setBillsOrderMasterRefId(31);
        existing.setCNumberDisplay("BIL2608/005");
        when(billMasters.findById(77)).thenReturn(Optional.of(existing));

        BillMasterInsertDto dto = bill();
        dto.setId(77);
        dto.setBillsOrderMasterRefId(99);
        BillMasterSaveResponseDto result = service.save(dto, 6);

        assertThat(result.isSuccess()).isTrue();
        assertThat(existing.getBillsOrderMasterRefId()).isEqualTo(31);
        // No second purchase order gets flagged on an edit.
        verify(billsOrders, never()).save(any(BillsOrderMaster.class));
    }

    @Test
    void rejectsAnInactiveEmployeeWithTheStoredProceduresMessage() {
        allReferencesValid();
        when(employees.existsByIdAndCompanyRefIdAndActive(eq(9), anyInt(), anyInt()))
                .thenReturn(false);

        BillMasterSaveResponseDto result = service.save(bill(), 6);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).isEqualTo("Employee Not Found Issue id9");
        verify(billMasters, never()).save(any(BillMaster.class));
    }

    @Test
    void refusesABillWithNoLines() {
        BillMasterInsertDto dto = bill();
        dto.setBillDetails(List.of());

        BillMasterSaveResponseDto result = service.save(dto, 6);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("at least one bill line");
    }

    @Test
    void refusesALineWithNoAccountCode() {
        BillMasterInsertDto dto = bill();
        dto.setBillDetails(List.of(BillDetailsInsertDto.builder().itemQty(1f).build()));

        BillMasterSaveResponseDto result = service.save(dto, 6);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("account code");
    }

    @Test
    void nullMoneyFieldsBecomeZeroBecauseTheColumnsAreNotNullable() {
        allReferencesValid();

        service.save(bill(), 6);

        verify(billDetails).saveAll(org.mockito.ArgumentMatchers.argThat(
                (Iterable<BillDetails> rows) -> {
                    BillDetails row = rows.iterator().next();
                    return row.getMrp() == 0f && row.getDiscPer() == 0f
                            && row.getTaxAmount() == 0f && row.getActualAmount1() == 0f;
                }));
    }
}
