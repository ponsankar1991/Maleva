package my.maleva.api.module.supplier.service;

import my.maleva.api.common.config.QneProperties;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.integration.qne.QneCall;
import my.maleva.api.integration.qne.QneGateway;
import my.maleva.api.integration.qne.QnePushLock;
import my.maleva.api.integration.qne.dto.QneSupplierResponse;
import my.maleva.api.module.supplier.dto.SupplierDto;
import my.maleva.api.module.supplier.dto.SupplierQneSyncResult;
import my.maleva.api.module.supplier.repository.SupplierQneSyncRepository;
import my.maleva.api.module.supplier.repository.SupplierQneSyncRepository.IdRepair;
import my.maleva.api.module.supplier.repository.SupplierQneSyncRepository.LinkedSupplier;
import my.maleva.api.module.supplier.repository.SupplierQneSyncRepository.NamedId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Pins "Update from QNE" (legacy UpdateSupplierId): the legacy mapping it
 * keeps, and each of the legacy faults it no longer has.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SupplierQneSyncServiceTest {

    private static final int COMPANY = 6;

    @Mock private QneGateway gateway;
    @Mock private QneProperties properties;
    @Mock private SupplierQneSyncRepository repository;
    @Mock private SupplierService suppliers;

    private final QnePushLock locks = new QnePushLock();
    private SupplierQneSyncService service;
    private final List<List<QneSupplierResponse>> pages = new ArrayList<>();
    private final AtomicInteger nextId = new AtomicInteger(900);

    @BeforeEach
    void setUp() {
        service = new SupplierQneSyncService(gateway, properties, locks, repository, suppliers);
        service.pageSize = 2;
        when(properties.isEnabled()).thenReturn(true);

        when(gateway.listSuppliers(anyInt(), anyInt())).thenAnswer(call -> {
            int skip = call.getArgument(0);
            int index = skip / service.pageSize;
            return ok(index < pages.size() ? pages.get(index) : List.of());
        });

        when(repository.linkedSuppliers(COMPANY)).thenReturn(List.of());
        when(repository.symbols(COMPANY)).thenReturn(List.of(new NamedId(2, "MYR"), new NamedId(4, "USD"), new NamedId(9, "MYR")));
        when(repository.paymentTerms(COMPANY)).thenReturn(List.of(new NamedId(3, "30 DAYS")));
        when(repository.repairQneIds(eq(COMPANY), anyList())).thenAnswer(call -> ((List<?>) call.getArgument(1)).size());
        when(suppliers.createFromQne(any(SupplierDto.class), anyString(), anyString())).thenAnswer(call -> nextId.incrementAndGet());
    }

    @SuppressWarnings("unchecked")
    private static QneCall<List<QneSupplierResponse>> ok(List<QneSupplierResponse> rows) {
        QneCall<List<QneSupplierResponse>> call = mock(QneCall.class);
        when(call.success()).thenReturn(true);
        when(call.data()).thenReturn(rows);
        return call;
    }

    private static QneSupplierResponse qne(String id, String code, String name) {
        QneSupplierResponse supplier = mock(QneSupplierResponse.class);
        when(supplier.getId()).thenReturn(id);
        when(supplier.getCompanyCode()).thenReturn(code);
        when(supplier.getCompanyName()).thenReturn(name);
        return supplier;
    }

    // ─── reading QNE ────────────────────────────────────────────────────

    @Test
    @DisplayName("every page of QNE is read, not just the first (legacy stopped at top=1000)")
    void readsEveryPage() {
        pages.add(List.of(qne("g1", "400-A", "A"), qne("g2", "400-B", "B")));
        pages.add(List.of(qne("g3", "400-C", "C"), qne("g4", "400-D", "D")));
        pages.add(List.of(qne("g5", "400-E", "E")));

        SupplierQneSyncResult result = service.syncFromQne(COMPANY);

        assertThat(result.inQne()).isEqualTo(5);
        assertThat(result.created()).isEqualTo(5);
        verify(gateway).listSuppliers(0, 2);
        verify(gateway).listSuppliers(2, 2);
        verify(gateway).listSuppliers(4, 2);
    }

    @Test
    @DisplayName("a QNE that ignores skip does not loop for ever")
    void stopsWhenQneRepeatsAPage() {
        List<QneSupplierResponse> same = List.of(qne("g1", "400-A", "A"), qne("g2", "400-B", "B"));
        when(gateway.listSuppliers(anyInt(), anyInt())).thenAnswer(call -> ok(same));

        SupplierQneSyncResult result = service.syncFromQne(COMPANY);

        assertThat(result.inQne()).isEqualTo(2);
        assertThat(result.created()).isEqualTo(2);
    }

    @Test
    @DisplayName("QNE refusing the list fails the sync before anything local is touched")
    void qneRefusalChangesNothing() {
        @SuppressWarnings("unchecked")
        QneCall<List<QneSupplierResponse>> refused = mock(QneCall.class);
        when(refused.success()).thenReturn(false);
        when(refused.message()).thenReturn("Invalid DbCode");
        when(gateway.listSuppliers(anyInt(), anyInt())).thenReturn(refused);

        SupplierQneSyncResult result = service.syncFromQne(COMPANY);

        assertThat(result.status()).isEqualTo(SupplierQneSyncResult.Status.FAILED);
        assertThat(result.message()).isEqualTo("Invalid DbCode");
        verifyNoInteractions(repository, suppliers);
    }

    @Test
    @DisplayName("with QNE switched off nothing is read or written")
    void disabled() {
        when(properties.isEnabled()).thenReturn(false);

        assertThat(service.syncFromQne(COMPANY).status()).isEqualTo(SupplierQneSyncResult.Status.DISABLED);
        verifyNoInteractions(gateway, repository, suppliers);
    }

    // ─── matching ───────────────────────────────────────────────────────

    @Test
    @DisplayName("a code already here — deleted or not, any casing — is never created again")
    void knownCodesAreNotRecreated() {
        // The repository query deliberately includes deleted suppliers.
        when(repository.linkedSuppliers(COMPANY)).thenReturn(List.of(new LinkedSupplier(55, "400-p001", "g1")));
        pages.add(List.of(qne("g1", "400-P001", "PETRON")));

        SupplierQneSyncResult result = service.syncFromQne(COMPANY);

        assertThat(result.alreadyLinked()).isEqualTo(1);
        assertThat(result.created()).isZero();
        assertThat(result.idsRepaired()).isZero();
        verify(suppliers, never()).createFromQne(any(), anyString(), anyString());
    }

    @Test
    @DisplayName("a known supplier with a missing or different QNE id has it repaired, in one batch")
    void repairsQneIds() {
        when(repository.linkedSuppliers(COMPANY)).thenReturn(List.of(
                new LinkedSupplier(55, "400-A", null),
                new LinkedSupplier(56, "400-B", "old-guid"),
                new LinkedSupplier(57, "400-C", "g3")));
        pages.add(List.of(qne("g1", "400-A", "A"), qne("g2", "400-B", "B")));
        pages.add(List.of(qne("g3", "400-C", "C")));

        SupplierQneSyncResult result = service.syncFromQne(COMPANY);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<IdRepair>> repairs = ArgumentCaptor.forClass(List.class);
        verify(repository).repairQneIds(eq(COMPANY), repairs.capture());
        assertThat(repairs.getValue()).containsExactly(new IdRepair("400-A", "g1"), new IdRepair("400-B", "g2"));
        assertThat(result.idsRepaired()).isEqualTo(2);
        assertThat(result.alreadyLinked()).isEqualTo(3);
    }

    @Test
    @DisplayName("a QNE supplier without a company code is skipped and counted, not created")
    void skipsBlankCodes() {
        pages.add(List.of(qne("g1", "  ", "NO CODE"), qne("g2", null, "NULL CODE")));
        pages.add(List.of());

        SupplierQneSyncResult result = service.syncFromQne(COMPANY);

        assertThat(result.skippedWithoutCode()).isEqualTo(2);
        assertThat(result.created()).isZero();
        assertThat(result.message()).contains("2 without a QNE code skipped");
    }

    @Test
    @DisplayName("the same code twice in QNE's list is created once")
    void duplicateInQneListCreatedOnce() {
        pages.add(List.of(qne("g1", "400-A", "A"), qne("g1", "400-a", "A AGAIN")));
        pages.add(List.of());

        SupplierQneSyncResult result = service.syncFromQne(COMPANY);

        assertThat(result.created()).isEqualTo(1);
        assertThat(result.alreadyLinked()).isEqualTo(1);
    }

    // ─── creating ───────────────────────────────────────────────────────

    @Test
    @DisplayName("a missing supplier is created with legacy's mapping and linked to its QNE code")
    void createsWithLegacyMapping() {
        QneSupplierResponse petron = qne("guid-9", "400-P009", "Petron Klang");
        when(petron.getAddress1()).thenReturn("Lot 5");
        when(petron.getAddress2()).thenReturn("");
        when(petron.getAddress3()).thenReturn("Jalan Kem");
        when(petron.getContactPerson()).thenReturn("Ms Tan");
        when(petron.getPhoneNo1()).thenReturn("03-1234");
        when(petron.getEmail()).thenReturn("ap@petron.example");
        when(petron.getCurrency()).thenReturn("MYR");
        when(petron.getTerm()).thenReturn("30 DAYS");
        pages.add(List.of(petron));

        service.syncFromQne(COMPANY);

        ArgumentCaptor<SupplierDto> dto = ArgumentCaptor.forClass(SupplierDto.class);
        verify(suppliers).createFromQne(dto.capture(), eq("guid-9"), eq("400-P009"));
        SupplierDto created = dto.getValue();
        assertThat(created.getCompanyRefId()).isEqualTo(COMPANY);
        assertThat(created.getSupplierName()).isEqualTo("Petron Klang");
        assertThat(created.getSupplierType()).isEqualTo("VENDOR");
        assertThat(created.getAddress1()).isEqualTo("Lot 5\nJalan Kem");
        assertThat(created.getCity()).isEqualTo("Ms Tan");
        assertThat(created.getMobileNo()).isEqualTo("03-1234");
        assertThat(created.getOPhone()).isEqualTo("03-1234");
        assertThat(created.getAPhone()).isEqualTo("03-1234");
        assertThat(created.getEmail()).isEqualTo("ap@petron.example");
        assertThat(created.getOEmail()).isEqualTo("ap@petron.example");
        assertThat(created.getAEmail()).isEqualTo("ap@petron.example");
        // The FIRST symbol named MYR, as FirstOrDefault picked.
        assertThat(created.getSymbolRefid()).isEqualTo(2);
        assertThat(created.getPaymentTermsRefid()).isEqualTo(3);
        assertThat(created.getSelfBilled()).isZero();
        assertThat(created.getMsicCodeRefId()).isZero();
        assertThat(created.getActive()).isEqualTo(1);
        assertThat(created.getTinNo()).isEmpty();
    }

    @Test
    @DisplayName("an unknown currency or term gives 0, like FirstOrDefault on no match")
    void unknownCurrencyAndTermAreZero() {
        SupplierDto dto = SupplierQneSyncService.toLocalSupplier(
                qne("g", "C", "N"), COMPANY, Map.of("MYR", 2), Map.of("30 DAYS", 3));

        assertThat(dto.getSymbolRefid()).isZero();
        assertThat(dto.getPaymentTermsRefid()).isZero();
    }

    @Test
    @DisplayName("text longer than its column is cut to the column, as SP_Supplier's OPENJSON did")
    void textIsCutToColumnWidths() {
        QneSupplierResponse longOne = qne("g", "C", "N".repeat(600));
        when(longOne.getAddress1()).thenReturn("A".repeat(250));
        when(longOne.getAddress2()).thenReturn("B".repeat(250));
        when(longOne.getContactPerson()).thenReturn("P".repeat(150));
        when(longOne.getPhoneNo1()).thenReturn("9".repeat(80));

        SupplierDto dto = SupplierQneSyncService.toLocalSupplier(longOne, COMPANY, Map.of(), Map.of());

        assertThat(dto.getSupplierName()).hasSize(500);
        assertThat(dto.getAddress1()).hasSize(300);
        assertThat(dto.getCity()).hasSize(100);
        assertThat(dto.getMobileNo()).hasSize(50);
    }

    @Test
    @DisplayName("one supplier that cannot be created does not stop the others, and is reported")
    void oneFailureDoesNotStopTheRest() {
        pages.add(List.of(qne("g1", "400-BAD", "BROKEN"), qne("g2", "400-OK", "FINE")));
        pages.add(List.of());
        when(suppliers.createFromQne(any(SupplierDto.class), eq("g1"), eq("400-BAD")))
                .thenThrow(new InvalidRequestException("Symbol Master Not Found Issue id 7"));

        SupplierQneSyncResult result = service.syncFromQne(COMPANY);

        assertThat(result.status()).isEqualTo(SupplierQneSyncResult.Status.COMPLETED);
        assertThat(result.created()).isEqualTo(1);
        assertThat(result.failures()).containsExactly("400-BAD BROKEN: Symbol Master Not Found Issue id 7");
        assertThat(result.message()).contains("1 failed");
    }

    // ─── concurrency ────────────────────────────────────────────────────

    @Test
    @DisplayName("a second sync for the company is refused while one is running")
    void secondSyncIsRefused() {
        locks.tryAcquire("supplier-qne-sync:" + COMPANY);

        assertThatThrownBy(() -> service.syncFromQne(COMPANY))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("already running");
        verifyNoInteractions(gateway);
    }

    @Test
    @DisplayName("the lock is released after a sync, even one that throws")
    void lockIsReleased() {
        when(gateway.listSuppliers(anyInt(), anyInt())).thenThrow(new IllegalStateException("network down"));

        assertThatThrownBy(() -> service.syncFromQne(COMPANY)).isInstanceOf(IllegalStateException.class);

        assertThat(locks.isHeld("supplier-qne-sync:" + COMPANY)).isFalse();
    }
}
