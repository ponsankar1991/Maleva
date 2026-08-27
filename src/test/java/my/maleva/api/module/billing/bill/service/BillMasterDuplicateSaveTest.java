package my.maleva.api.module.billing.bill.service;

import my.maleva.api.module.accounting.repository.GLAccountsRepository;
import my.maleva.api.module.billing.bill.dto.BillDetailsInsertDto;
import my.maleva.api.module.billing.bill.dto.BillMasterInsertDto;
import my.maleva.api.module.billing.bill.dto.BillMasterSaveResponseDto;
import my.maleva.api.module.billing.bill.entity.BillMaster;
import my.maleva.api.module.billing.bill.repository.BillDetailsRepository;
import my.maleva.api.module.billing.bill.repository.BillMasterRepository;
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
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A clerk clicking Save several times on a slow connection must end up with
 * one bill, not three. The browser guard the legacy screen used cannot
 * enforce that — a reload, a second tab or a retry all get past it — so these
 * pin the server-side rule.
 */
class BillMasterDuplicateSaveTest {

    private BillMasterRepository billMasters;
    private BillDetailsRepository billDetails;
    private BillsOrderMasterRepository billsOrders;
    private NamedParameterJdbcTemplate jdbc;
    private BillMasterTransactionService service;

    @BeforeEach
    void setUp() {
        billMasters = mock(BillMasterRepository.class);
        billDetails = mock(BillDetailsRepository.class);
        billsOrders = mock(BillsOrderMasterRepository.class);
        jdbc = mock(NamedParameterJdbcTemplate.class);

        SupplierRepository suppliers = mock(SupplierRepository.class);
        PaymentTermsMasterRepository paymentTerms = mock(PaymentTermsMasterRepository.class);
        SequenceNoMasterRepository sequences = mock(SequenceNoMasterRepository.class);
        AppUserRepository appUsers = mock(AppUserRepository.class);
        EmployeeMasterRepository employees = mock(EmployeeMasterRepository.class);
        TruckMasterRepository trucks = mock(TruckMasterRepository.class);
        DriverMasterRepository drivers = mock(DriverMasterRepository.class);

        service = new BillMasterTransactionService(
                billMasters, billDetails, suppliers, paymentTerms, sequences,
                mock(SymbolMasterRepository.class), mock(GLAccountsRepository.class),
                billsOrders, appUsers, employees, trucks, drivers, jdbc);

        when(suppliers.existsById(anyInt())).thenReturn(true);
        when(paymentTerms.existsByIdAndCompanyRefIdAndActive(anyInt(), anyInt(), anyInt()))
                .thenReturn(true);
        when(employees.existsByIdAndCompanyRefIdAndActive(anyInt(), anyInt(), anyInt()))
                .thenReturn(true);
        when(billDetails.findByBillMasterRefId(anyInt())).thenReturn(List.of());
        when(sequences.findMaxBillMasterSequenceNo(anyInt(), anyInt(), anyInt())).thenReturn(4);
        when(billMasters.save(any(BillMaster.class))).thenAnswer(call -> {
            BillMaster saved = call.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(77);
            }
            return saved;
        });
        // Lock granted by default.
        lockReturns(0);
        when(billMasters.findLiveByInvoiceNo(anyInt(), anyString())).thenReturn(List.of());
        when(billMasters.findRecentlyEnteredLikeThis(
                anyInt(), anyInt(), any(), anyFloat(), any())).thenReturn(List.of());
    }

    private void lockReturns(Integer status) {
        when(jdbc.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Integer.class)))
                .thenReturn(status);
    }

    private BillMasterInsertDto bill(String invoiceNo) {
        return BillMasterInsertDto.builder()
                .id(0)
                .supplierRefId(3)
                .employeeRefId(9)
                .paymentTermsRefid(2)
                .invoiceNo(invoiceNo)
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

    private BillMaster alreadySaved() {
        BillMaster existing = new BillMaster();
        existing.setId(77);
        existing.setCompanyRefId(6);
        existing.setCNumberDisplay("BIL2608/005");
        return existing;
    }

    @Test
    void aSecondClickReturnsTheFirstBillInsteadOfEnteringItAgain() {
        when(billMasters.findLiveByInvoiceNo(6, "SUP-INV-88"))
                .thenReturn(List.of(alreadySaved()));

        BillMasterSaveResponseDto result = service.save(bill("SUP-INV-88"), 6);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isDuplicate()).isTrue();
        assertThat(result.getId()).isEqualTo(77);
        assertThat(result.getBillNoDisplay()).isEqualTo("BIL2608/005");
        assertThat(result.getMessage()).contains("already saved as BIL2608/005");
        verify(billMasters, never()).save(any(BillMaster.class));
    }

    @Test
    void withoutAnInvoiceNumberARepeatIsCaughtBySupplierDateAndAmount() {
        when(billMasters.findRecentlyEnteredLikeThis(
                eq(6), eq(3), any(), eq(500f), any()))
                .thenReturn(List.of(alreadySaved()));

        BillMasterSaveResponseDto result = service.save(bill(""), 6);

        assertThat(result.isDuplicate()).isTrue();
        assertThat(result.getId()).isEqualTo(77);
        verify(billMasters, never()).save(any(BillMaster.class));
    }

    @Test
    void aGenuinelyNewBillStillSaves() {
        BillMasterSaveResponseDto result = service.save(bill("SUP-INV-99"), 6);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isDuplicate()).isFalse();
        assertThat(result.getBillNoDisplay()).isEqualTo("BIL2608/005");
        verify(billMasters, org.mockito.Mockito.atLeastOnce()).save(any(BillMaster.class));
    }

    @Test
    void aTwinAlreadyInFlightIsTurnedAwayRatherThanRacingIt() {
        // Negative status = the lock could not be taken, so an identical save
        // is mid-flight. Inserting now is exactly the duplicate we are here
        // to prevent.
        lockReturns(-1);

        BillMasterSaveResponseDto result = service.save(bill("SUP-INV-88"), 6);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("already being saved");
        verify(billMasters, never()).save(any(BillMaster.class));
    }

    @Test
    void aBrokenLockDoesNotBlockSaving() {
        // The guard is protection, not a gate: if the lock call itself fails
        // the bill must still go in.
        when(jdbc.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Integer.class)))
                .thenThrow(new RuntimeException("sp_getapplock unavailable"));

        BillMasterSaveResponseDto result = service.save(bill("SUP-INV-99"), 6);

        assertThat(result.isSuccess()).isTrue();
        verify(billMasters, org.mockito.Mockito.atLeastOnce()).save(any(BillMaster.class));
    }

    @Test
    void editingAnExistingBillIsNeverTreatedAsADuplicateOfItself() {
        BillMaster existing = alreadySaved();
        when(billMasters.findById(77)).thenReturn(java.util.Optional.of(existing));
        when(billMasters.findLiveByInvoiceNo(anyInt(), anyString()))
                .thenReturn(List.of(existing));

        BillMasterInsertDto dto = bill("SUP-INV-88");
        dto.setId(77);
        BillMasterSaveResponseDto result = service.save(dto, 6);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isDuplicate()).isFalse();
        assertThat(result.getMessage()).contains("updated");
    }
}
