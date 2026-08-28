package my.maleva.api.module.pettycash.service;

import my.maleva.api.module.employee.repository.EmployeeMasterRepository;
import my.maleva.api.module.master.repository.SequenceNoMasterRepository;
import my.maleva.api.module.pettycash.dto.PettyCashSaveLineDto;
import my.maleva.api.module.pettycash.dto.PettyCashSaveRequestDto;
import my.maleva.api.module.pettycash.dto.PettyCashSaveResponseDto;
import my.maleva.api.module.pettycash.entity.PettyCashDetail;
import my.maleva.api.module.pettycash.entity.PettyCashMaster;
import my.maleva.api.module.pettycash.repository.PettyCashDetailRepository;
import my.maleva.api.module.pettycash.repository.PettyCashMasterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pins the behaviours {@code SP_PettyCashMaster} performed that are easy to
 * lose in a hand port — the running number, the wholesale line replacement,
 * and the company scoping the stored procedure never had.
 */
class PettyCashTransactionServiceTest {

    private PettyCashMasterRepository masters;
    private PettyCashDetailRepository details;
    private EmployeeMasterRepository employees;
    private SequenceNoMasterRepository sequences;
    private PettyCashTransactionService service;

    @BeforeEach
    void setUp() {
        masters = mock(PettyCashMasterRepository.class);
        details = mock(PettyCashDetailRepository.class);
        employees = mock(EmployeeMasterRepository.class);
        sequences = mock(SequenceNoMasterRepository.class);

        service = new PettyCashTransactionService(
                masters, details, employees, sequences,
                mock(NamedParameterJdbcTemplate.class));

        when(employees.existsByIdAndCompanyRefIdAndActive(anyInt(), anyInt(), anyInt()))
                .thenReturn(true);
        when(details.findByPettyCashMasterRefIdOrderByIdAsc(anyInt())).thenReturn(List.of());
        when(sequences.findMaxSequenceNoByCompanyAndSequenceName(anyInt(), any()))
                .thenReturn(4);
        when(sequences.findByCompanyRefIdAndSequenceName(anyInt(), any()))
                .thenReturn(Optional.empty());
        when(masters.save(any(PettyCashMaster.class))).thenAnswer(call -> {
            PettyCashMaster saved = call.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(77);
            }
            return saved;
        });
    }

    private PettyCashSaveRequestDto entry() {
        return PettyCashSaveRequestDto.builder()
                .id(0)
                .employeeRefId(9)
                .department("ACCOUNTS")
                .pettyCashDate("2026-08-28")
                .paymentStatus("SEND FOR APPROVAL")
                .remark("August float")
                .pettyCashDetails(List.of(
                        PettyCashSaveLineDto.builder()
                                .items("Taxi fare").amount(new BigDecimal("25.50")).notes("client visit")
                                .build(),
                        PettyCashSaveLineDto.builder()
                                .items("Stationery").amount(new BigDecimal("14.25")).notes("")
                                .build()))
                .build();
    }

    @Test
    void numbersANewEntryFromTheCounterAndBumpsIt() {
        PettyCashSaveResponseDto result = service.save(entry(), 6);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getId()).isEqualTo(77);
        // Counter stood at 4, so this entry is the fifth — 'PTC' + 9 digits.
        assertThat(result.getCNumberDisplay()).isEqualTo("PTC000000005");
        verify(sequences).save(org.mockito.ArgumentMatchers.argThat(
                counter -> counter.getSequenceNo() == 5));
    }

    /**
     * The counter row is created when absent. The stored procedure only ever
     * UPDATEd it, so a company with no {@code PettyCashMaster} row silently
     * numbered every entry 1.
     */
    @Test
    void createsTheCounterRowWhenTheCompanyHasNoneYet() {
        when(sequences.findMaxSequenceNoByCompanyAndSequenceName(anyInt(), any())).thenReturn(null);

        assertThat(service.save(entry(), 6).getCNumberDisplay()).isEqualTo("PTC000000001");
        verify(sequences).save(org.mockito.ArgumentMatchers.argThat(
                counter -> counter.getSequenceNo() == 1
                        && "PettyCashMaster".equals(counter.getSequenceName())
                        && counter.getCompanyRefId() == 6));
    }

    /** The total is the server's sum of the lines, never the client's figure. */
    @Test
    void totalsTheLinesItselfRatherThanTrustingThePostedAmount() {
        service.save(entry(), 6);

        verify(masters, org.mockito.Mockito.atLeastOnce()).save(
                org.mockito.ArgumentMatchers.argThat(saved -> "39.75".equals(saved.getAmount())));
    }

    @Test
    void editingAnEntryNeverRenumbersIt() {
        PettyCashMaster existing = new PettyCashMaster();
        existing.setId(31);
        existing.setCompanyRefId(6);
        existing.setCNumber(2);
        existing.setCNumberDisplay("PTC000000002");
        when(masters.findById(31)).thenReturn(Optional.of(existing));

        PettyCashSaveRequestDto dto = entry();
        dto.setId(31);
        PettyCashSaveResponseDto result = service.save(dto, 6);

        assertThat(result.getCNumberDisplay()).isEqualTo("PTC000000002");
        assertThat(existing.getCNumber()).isEqualTo(2);
        // No number handed out, so the counter must not move.
        verify(sequences, never()).save(any());
    }

    /** Lines are replaced wholesale, as {@code SP_PettyCashMaster} did. */
    @Test
    void replacesEveryLineOnEditRatherThanUpdatingInPlace() {
        PettyCashMaster existing = new PettyCashMaster();
        existing.setId(31);
        existing.setCompanyRefId(6);
        existing.setCNumberDisplay("PTC000000002");
        when(masters.findById(31)).thenReturn(Optional.of(existing));

        PettyCashDetail stale = new PettyCashDetail();
        stale.setId(500);
        stale.setPettyCashMasterRefId(31);
        when(details.findByPettyCashMasterRefIdOrderByIdAsc(31)).thenReturn(List.of(stale));

        PettyCashSaveRequestDto dto = entry();
        dto.setId(31);
        service.save(dto, 6);

        verify(details).deleteAll(List.of(stale));
        verify(details, org.mockito.Mockito.times(2)).saveAndFlush(any(PettyCashDetail.class));
    }

    /**
     * {@code SP_PettyCashMaster}'s UPDATE branch never writes EmployeeRefId,
     * so the person who raised the slip survives someone else's edit.
     */
    @Test
    void editingAnEntryLeavesTheOriginalEmployeeInPlace() {
        PettyCashMaster existing = new PettyCashMaster();
        existing.setId(31);
        existing.setCompanyRefId(6);
        existing.setEmployeeRefId(4);
        existing.setCNumberDisplay("PTC000000002");
        when(masters.findById(31)).thenReturn(Optional.of(existing));

        PettyCashSaveRequestDto dto = entry();
        dto.setId(31);
        dto.setEmployeeRefId(9);
        service.save(dto, 6);

        assertThat(existing.getEmployeeRefId()).isEqualTo(4);
    }

    /** A soft-deleted slip cannot be reopened and re-saved. */
    @Test
    void doesNotLoadADeletedEntryForEditing() {
        PettyCashMaster deleted = new PettyCashMaster();
        deleted.setId(31);
        deleted.setCompanyRefId(6);
        deleted.setActive(2);
        when(masters.findById(31)).thenReturn(Optional.of(deleted));

        assertThat(service.edit(31, 6)).isEmpty();
    }

    /** Scoping the stored procedure never had: another company's row is invisible. */
    @Test
    void refusesToEditAnotherCompanysEntry() {
        PettyCashMaster otherCompany = new PettyCashMaster();
        otherCompany.setId(31);
        otherCompany.setCompanyRefId(9);
        when(masters.findById(31)).thenReturn(Optional.of(otherCompany));

        PettyCashSaveRequestDto dto = entry();
        dto.setId(31);
        PettyCashSaveResponseDto result = service.save(dto, 6);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("not found");
        verify(masters, never()).save(any(PettyCashMaster.class));
    }

    @Test
    void refusesAnEntryWithNoUsableLines() {
        PettyCashSaveRequestDto dto = entry();
        dto.setPettyCashDetails(List.of(PettyCashSaveLineDto.builder()
                .items("").amount(BigDecimal.ZERO).notes("").build()));

        PettyCashSaveResponseDto result = service.save(dto, 6);

        assertThat(result.isSuccess()).isFalse();
        verify(masters, never()).save(any(PettyCashMaster.class));
    }

    @Test
    void refusesAnEmployeeFromAnotherCompany() {
        when(employees.existsByIdAndCompanyRefIdAndActive(anyInt(), anyInt(), anyInt()))
                .thenReturn(false);

        PettyCashSaveResponseDto result = service.save(entry(), 6);

        assertThat(result.isSuccess()).isFalse();
        verify(masters, never()).save(any(PettyCashMaster.class));
    }

    /**
     * Delete is soft and company-scoped — the lines hang off a plain FK with
     * no cascade, so removing the master outright would strand them.
     */
    @Test
    void softDeletesOnlyWithinTheCallersCompany() {
        PettyCashMaster mine = new PettyCashMaster();
        mine.setId(31);
        mine.setCompanyRefId(6);
        mine.setActive(1);
        when(masters.findById(31)).thenReturn(Optional.of(mine));

        assertThat(service.delete(31, 6)).isTrue();
        assertThat(mine.getActive()).isEqualTo(2);

        PettyCashMaster theirs = new PettyCashMaster();
        theirs.setId(32);
        theirs.setCompanyRefId(9);
        theirs.setActive(1);
        when(masters.findById(32)).thenReturn(Optional.of(theirs));

        assertThat(service.delete(32, 6)).isFalse();
        assertThat(theirs.getActive()).isEqualTo(1);
    }

    /**
     * The optional chart-of-accounts account and e-Invoice classification ride
     * along on the line, and an unset picker — which posts 0 — is stored as
     * NULL, not as account 0 or classification 0. {@code AccountGroupRefId} is
     * a {@code GLAccounts.RowIndex} and {@code Classification} a
     * {@code Classification.Id}, so a stored 0 would be a reference to nothing.
     * (Payment Voucher keeps a raw 0 on its own rows for historical reasons;
     * petty cash lines predate both columns, so there is no such history here.)
     */
    @Test
    void storesTheLineAccountAndTurnsAnUnsetOneIntoNull() {
        PettyCashSaveRequestDto dto = entry();
        dto.setPettyCashDetails(List.of(
                PettyCashSaveLineDto.builder()
                        .items("Taxi fare").amount(new BigDecimal("25.50")).notes("client visit")
                        .accountGroupRefId(812)
                        .classification(36)
                        .build(),
                PettyCashSaveLineDto.builder()
                        .items("Stationery").amount(new BigDecimal("14.25")).notes("")
                        .accountGroupRefId(0)
                        .classification(0)
                        .build()));

        assertThat(service.save(dto, 6).isSuccess()).isTrue();

        ArgumentCaptor<PettyCashDetail> saved = ArgumentCaptor.forClass(PettyCashDetail.class);
        verify(details, org.mockito.Mockito.times(2)).saveAndFlush(saved.capture());

        assertThat(saved.getAllValues().get(0).getAccountGroupRefId()).isEqualTo(812);
        assertThat(saved.getAllValues().get(0).getClassification()).isEqualTo(36);
        assertThat(saved.getAllValues().get(1).getAccountGroupRefId()).isNull();
        assertThat(saved.getAllValues().get(1).getClassification()).isNull();
    }

    /** The screen shows {@code dd/MM/yyyy}; ISO is the contract. Both parse. */
    @Test
    void acceptsBothTheIsoAndTheOnScreenDateFormat() {
        PettyCashSaveRequestDto iso = entry();
        iso.setPettyCashDate("2026-08-28");
        assertThat(service.save(iso, 6).isSuccess()).isTrue();

        PettyCashSaveRequestDto onScreen = entry();
        onScreen.setPettyCashDate("28/08/2026");
        assertThat(service.save(onScreen, 6).isSuccess()).isTrue();
    }
}
