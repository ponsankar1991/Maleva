package my.maleva.api.module.customer.service;

import my.maleva.api.common.config.QneProperties;
import my.maleva.api.integration.qne.QneCall;
import my.maleva.api.integration.qne.QneGateway;
import my.maleva.api.integration.qne.QnePushLock;
import my.maleva.api.integration.qne.QnePushResult;
import my.maleva.api.integration.qne.dto.QneCustomerRequest;
import my.maleva.api.integration.qne.dto.QneCustomerResponse;
import my.maleva.api.module.customer.entity.Customer;
import my.maleva.api.module.customer.repository.CustomerRepository;
import my.maleva.api.module.master.repository.SymbolMasterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
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

/** Pins the customer list's "Push to QNE" button: when it sends, when it refuses, and why. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CustomerQnePushTest {

    private static final int COMPANY = 6;
    private static final int CUSTOMER_ID = 1047;

    @Mock private QneGateway gateway;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS) private QneProperties properties;
    @Mock private CustomerRepository customers;
    @Mock private SymbolMasterRepository symbols;

    private final QnePushLock pushLock = new QnePushLock();
    private CustomerQneService service;
    private Customer customer;

    @BeforeEach
    void setUp() {
        service = new CustomerQneService(gateway, properties, customers, symbols, pushLock);
        when(properties.isEnabled()).thenReturn(true);
        when(properties.getControlCodes().getCustomer()).thenReturn("300-0000");

        customer = new Customer();
        customer.setId(CUSTOMER_ID);
        customer.setCompanyRefId(COMPANY);
        customer.setCustomerName("BASONG ENVIRONMENTAL");
        customer.setActive(1);
        when(customers.findByIdAndCompanyRefId(CUSTOMER_ID, COMPANY)).thenReturn(Optional.of(customer));
    }

    @SuppressWarnings("unchecked")
    private void qneAnswers(boolean success, String id, String code, String message) {
        QneCall<QneCustomerResponse> call = mock(QneCall.class);
        when(call.success()).thenReturn(success);
        when(call.message()).thenReturn(message);
        if (success) {
            QneCustomerResponse response = mock(QneCustomerResponse.class);
            when(response.getId()).thenReturn(id);
            when(response.getCompanyCode()).thenReturn(code);
            when(call.data()).thenReturn(response);
        }
        when(gateway.createCustomer(any(QneCustomerRequest.class))).thenReturn(call);
    }

    @Test
    @DisplayName("a customer QNE does not have is created there and its QNE code stored")
    void pushesAndStoresIdentity() {
        qneAnswers(true, "guid-1", "300-B001", null);

        QnePushResult result = service.push(CUSTOMER_ID, COMPANY);

        assertThat(result.success()).isTrue();
        assertThat(result.qneCode()).isEqualTo("300-B001");
        assertThat(result.errorStatus()).isNull();
        verify(customers).claimQneIdentity(CUSTOMER_ID, "guid-1", "300-B001");
        assertThat(pushLock.isHeld("customer:" + CUSTOMER_ID)).isFalse();
    }

    @Test
    @DisplayName("a customer already in QNE is not sent again")
    void customerAlreadyInQneIsNotSent() {
        customer.setCompanyCode("300-B001");
        customer.setUpdateId("guid-1");

        QnePushResult result = service.push(CUSTOMER_ID, COMPANY);

        assertThat(result.success()).isTrue();
        assertThat(result.qneCode()).isEqualTo("300-B001");
        verifyNoInteractions(gateway);
    }

    @Test
    @DisplayName("QNE's refusal comes back as QNE's message, not an HTTP error, and stores nothing")
    void qneRefusalIsReported() {
        qneAnswers(false, null, null, "CompanyName BASONG ENVIRONMENTAL already exists");

        QnePushResult result = service.push(CUSTOMER_ID, COMPANY);

        assertThat(result.success()).isFalse();
        assertThat(result.errorStatus()).isNull();
        assertThat(result.message()).isEqualTo("CompanyName BASONG ENVIRONMENTAL already exists");
        verify(customers, never()).claimQneIdentity(anyInt(), anyString(), anyString());
    }

    @Test
    @DisplayName("a second click while a push is still waiting on QNE is refused")
    void concurrentPushIsRefused() {
        pushLock.tryAcquire("customer:" + CUSTOMER_ID);

        QnePushResult result = service.push(CUSTOMER_ID, COMPANY);

        assertThat(result.errorStatus()).isEqualTo(409);
        assertThat(result.message()).contains("already being sent");
        verifyNoInteractions(gateway, customers);
    }

    @Test
    @DisplayName("a customer of another company is not found, and nothing is sent")
    void otherCompanyIsNotFound() {
        QnePushResult result = service.push(CUSTOMER_ID, COMPANY + 1);

        assertThat(result.errorStatus()).isEqualTo(404);
        verifyNoInteractions(gateway);
    }

    @Test
    @DisplayName("a deleted customer is not sent to QNE")
    void deletedCustomerIsRefused() {
        customer.setActive(2);

        QnePushResult result = service.push(CUSTOMER_ID, COMPANY);

        assertThat(result.errorStatus()).isEqualTo(409);
        assertThat(result.message()).contains("deleted");
        verifyNoInteractions(gateway);
    }

    @Test
    @DisplayName("with QNE switched off the button says so instead of failing quietly")
    void disabledQneIsReported() {
        when(properties.isEnabled()).thenReturn(false);

        QnePushResult result = service.push(CUSTOMER_ID, COMPANY);

        assertThat(result.errorStatus()).isEqualTo(409);
        assertThat(result.message()).contains("switched off");
        verifyNoInteractions(gateway, customers);
    }

    @Test
    @DisplayName("a push that throws is reported with its reason, and the lock is released")
    void crashIsReportedAndLockReleased() {
        when(gateway.createCustomer(any(QneCustomerRequest.class))).thenThrow(new IllegalStateException("socket closed"));

        QnePushResult result = service.push(CUSTOMER_ID, COMPANY);

        assertThat(result.success()).isFalse();
        assertThat(result.message()).contains("socket closed");
        assertThat(pushLock.isHeld("customer:" + CUSTOMER_ID)).isFalse();
    }
}
