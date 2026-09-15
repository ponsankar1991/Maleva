package my.maleva.api.module.supplier.service;

import my.maleva.api.common.config.QneProperties;
import my.maleva.api.integration.qne.QneCall;
import my.maleva.api.integration.qne.QneGateway;
import my.maleva.api.integration.qne.dto.QneSupplierRequest;
import my.maleva.api.integration.qne.dto.QneSupplierResponse;
import my.maleva.api.module.master.entity.SymbolMaster;
import my.maleva.api.module.master.repository.SymbolMasterRepository;
import my.maleva.api.module.supplier.dto.SupplierDto;
import my.maleva.api.module.supplier.dto.SupplierQneOutcome;
import my.maleva.api.module.supplier.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** Pins the decisions legacy InsertSupplier made around its QNE push. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SupplierQneServiceTest {

    private static final int COMPANY = 6;
    private static final int SUPPLIER_ID = 402;

    @Mock private QneGateway gateway;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS) private QneProperties properties;
    @Mock private SupplierRepository suppliers;
    @Mock private SymbolMasterRepository symbols;

    private SupplierQneService service;
    private SupplierDto saved;

    @BeforeEach
    void setUp() {
        service = new SupplierQneService(gateway, properties, suppliers, symbols);
        when(properties.isEnabled()).thenReturn(true);
        when(properties.getControlCodes().getSupplier()).thenReturn("800-2000");

        saved = new SupplierDto();
        saved.setId(SUPPLIER_ID);
        saved.setCompanyRefId(COMPANY);

        SymbolMaster myr = mock(SymbolMaster.class);
        when(myr.getSName()).thenReturn("MYR");
        when(symbols.findByIdAndCompanyRefId(2, COMPANY)).thenReturn(Optional.of(myr));
    }

    private SupplierDto typed() {
        SupplierDto dto = new SupplierDto();
        dto.setCompanyRefId(COMPANY);
        dto.setSupplierName("Petron Klang");
        dto.setSymbolRefid(2);
        return dto;
    }

    @SuppressWarnings("unchecked")
    private void qneAnswers(boolean success, String id, String code, String message) {
        QneCall<QneSupplierResponse> call = mock(QneCall.class);
        when(call.success()).thenReturn(success);
        when(call.message()).thenReturn(message);
        if (success) {
            QneSupplierResponse response = mock(QneSupplierResponse.class);
            when(response.getId()).thenReturn(id);
            when(response.getCompanyCode()).thenReturn(code);
            when(call.data()).thenReturn(response);
        }
        when(gateway.createSupplier(any(QneSupplierRequest.class))).thenReturn(call);
    }

    private QneSupplierRequest sentRequest() {
        ArgumentCaptor<QneSupplierRequest> request = ArgumentCaptor.forClass(QneSupplierRequest.class);
        verify(gateway).createSupplier(request.capture());
        return request.getValue();
    }

    @Test
    @DisplayName("a supplier with no QNE code is created in QNE and the identity written back")
    void pushesAndWritesBackIdentity() {
        qneAnswers(true, "guid-1", "400-P001", null);

        SupplierQneOutcome outcome = service.pushSaved(saved, typed());

        assertThat(outcome.status()).isEqualTo(SupplierQneOutcome.Status.PUSHED);
        assertThat(outcome.qneId()).isEqualTo("guid-1");
        assertThat(outcome.qneCode()).isEqualTo("400-P001");
        verify(suppliers).claimQneIdentity(SUPPLIER_ID, "guid-1", "400-P001");

        QneSupplierRequest request = sentRequest();
        assertThat(request.getCurrency()).isEqualTo("MYR");
        assertThat(request.getCompanyName()).isEqualTo("Petron Klang");
        assertThat(request.getControlAccount()).isEqualTo("800-2000");
    }

    @Test
    @DisplayName("the push does not read the supplier again: the save handed it over")
    void doesNotRereadTheSupplier() {
        qneAnswers(true, "guid-1", "400-P001", null);

        service.pushSaved(saved, typed());

        verify(suppliers, never()).findById(anyInt());
        verify(suppliers, never()).findByCompanyRefId(anyInt());
    }

    @Test
    @DisplayName("QNE's refusal comes back with its message and writes nothing")
    void reportsRefusal() {
        qneAnswers(false, null, null, "CompanyName already exists");

        SupplierQneOutcome outcome = service.pushSaved(saved, typed());

        assertThat(outcome.status()).isEqualTo(SupplierQneOutcome.Status.FAILED);
        assertThat(outcome.message()).isEqualTo("CompanyName already exists");
        verify(suppliers, never()).claimQneIdentity(anyInt(), anyString(), anyString());
    }

    @Test
    @DisplayName("a push that throws is reported as FAILED, not as a clean save")
    void reportsCrashAsFailure() {
        when(gateway.createSupplier(any(QneSupplierRequest.class))).thenThrow(new IllegalStateException("socket closed"));

        SupplierQneOutcome outcome = service.pushSaved(saved, typed());

        assertThat(outcome.status()).isEqualTo(SupplierQneOutcome.Status.FAILED);
        assertThat(outcome.message()).contains("socket closed");
    }

    @Test
    @DisplayName("a supplier already in QNE is not sent again (the Type 3 update was never dispatched)")
    void skipsSupplierAlreadyInQne() {
        saved.setQneCode("400-P001");
        saved.setQneId("guid-1");

        SupplierQneOutcome outcome = service.pushSaved(saved, typed());

        assertThat(outcome.status()).isEqualTo(SupplierQneOutcome.Status.ALREADY_IN_QNE);
        assertThat(outcome.qneCode()).isEqualTo("400-P001");
        verifyNoInteractions(gateway, symbols);
    }

    @Test
    @DisplayName("with QNE switched off nothing is sent or read, like qneapilist.qneapi = false")
    void skipsWhenDisabled() {
        when(properties.isEnabled()).thenReturn(false);

        SupplierQneOutcome outcome = service.pushSaved(saved, typed());

        assertThat(outcome.status()).isEqualTo(SupplierQneOutcome.Status.DISABLED);
        verifyNoInteractions(gateway, symbols, suppliers);
    }

    @Test
    @DisplayName("an unknown symbol sends an empty currency, like ?? \"\"")
    void emptyCurrencyForUnknownSymbol() {
        qneAnswers(true, "guid-1", "400-P001", null);
        SupplierDto dto = typed();
        dto.setSymbolRefid(99);

        service.pushSaved(saved, dto);

        assertThat(sentRequest().getCurrency()).isEmpty();
    }

    @Test
    @DisplayName("no symbol chosen sends an empty currency without a lookup")
    void emptyCurrencyWithoutLookupForZeroSymbol() {
        qneAnswers(true, "guid-1", "400-P001", null);
        SupplierDto dto = typed();
        dto.setSymbolRefid(0);

        service.pushSaved(saved, dto);

        assertThat(sentRequest().getCurrency()).isEmpty();
        verifyNoInteractions(symbols);
    }
}
