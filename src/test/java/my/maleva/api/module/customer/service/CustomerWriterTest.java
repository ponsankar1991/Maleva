package my.maleva.api.module.customer.service;

import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.accountsgroupmaster.entity.AccountsGroupMaster;
import my.maleva.api.module.accountsgroupmaster.repository.AccountsGroupMasterRepository;
import my.maleva.api.module.customer.dto.CustomerDto;
import my.maleva.api.module.customer.entity.Customer;
import my.maleva.api.module.customer.repository.CustomerRepository;
import my.maleva.api.module.master.repository.CountryMasterRepository;
import my.maleva.api.module.master.repository.SymbolMasterRepository;
import my.maleva.api.module.payment.repository.PaymentTermsMasterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
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
 * Pins {@code SP_Customer}'s contract on the Java port.
 *
 * Each test names the rule from the procedure it protects, because the rules
 * look arbitrary out of context and a future tidy-up would otherwise "fix"
 * them: the split account row, the per-company number, and the upper-casing
 * that deliberately differs between the insert and update branches.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CustomerWriterTest {

    private static final int COMPANY = 6;
    private static final int PARENT_ACCOUNT_ID = 8;

    @Mock private CustomerRepository customers;
    @Mock private AccountsGroupMasterRepository accountGroups;
    @Mock private SymbolMasterRepository symbols;
    @Mock private PaymentTermsMasterRepository paymentTerms;
    @Mock private CountryMasterRepository countries;

    private CustomerWriter writer;

    @BeforeEach
    void setUp() {
        writer = new CustomerWriter(customers, accountGroups, symbols, paymentTerms, countries);

        AccountsGroupMaster parent = new AccountsGroupMaster();
        parent.setId(PARENT_ACCOUNT_ID);
        when(accountGroups.findFirstByAccountNameAndAccountCodeAndCompanyRefIdAndActive(
                "CUSTOMERS", "CUS", COMPANY, 1)).thenReturn(Optional.of(parent));
        when(accountGroups.countByParentIdAndCompanyRefId(PARENT_ACCOUNT_ID, COMPANY)).thenReturn(1121);
        when(accountGroups.save(any(AccountsGroupMaster.class))).thenAnswer(call -> {
            AccountsGroupMaster saved = call.getArgument(0);
            saved.setId(4663);
            return saved;
        });

        when(customers.findMaxCNumber(COMPANY)).thenReturn(1137);
        when(customers.save(any(Customer.class))).thenAnswer(call -> call.getArgument(0));

        when(symbols.existsByIdAndCompanyRefIdAndActive(anyInt(), eq(COMPANY), eq(1))).thenReturn(true);
        when(paymentTerms.existsByIdAndCompanyRefIdAndActive(anyInt(), eq(COMPANY), eq(1))).thenReturn(true);
        when(countries.existsById(anyInt())).thenReturn(true);
    }

    private CustomerDto dto() {
        CustomerDto dto = new CustomerDto();
        dto.setCompanyRefId(COMPANY);
        dto.setCustomerName("basong environmental");
        dto.setSymbolRefid(2);
        dto.setPaymentTermsRefid(3);
        dto.setCountryId(158);
        return dto;
    }

    private Customer insert(CustomerDto dto) {
        return writer.insert(dto, COMPANY);
    }

    @Test
    @DisplayName("insert creates the ledger account first and links it")
    void insertCreatesAccountRow() {
        Customer saved = insert(dto());

        ArgumentCaptor<AccountsGroupMaster> account = ArgumentCaptor.forClass(AccountsGroupMaster.class);
        verify(accountGroups).save(account.capture());

        assertThat(account.getValue().getAccountName()).isEqualTo("BASONG ENVIRONMENTAL");
        assertThat(account.getValue().getParentId()).isEqualTo(PARENT_ACCOUNT_ID);
        // 'CUS-' + (sibling count + 1), the procedure's @RowNumber.
        assertThat(account.getValue().getAccountCode()).isEqualTo("CUS-1122");
        assertThat(account.getValue().getActive()).isEqualTo(1);
        assertThat(account.getValue().getEditmode()).isEqualTo(1);
        assertThat(account.getValue().getNoChild()).isEqualTo(1);

        // Without this link the customer has no account at all.
        assertThat(saved.getAccountRefid()).isEqualTo(4663);
    }

    @Test
    @DisplayName("insert numbers from the company's own high-water mark")
    void insertAllocatesCustomerNumber() {
        Customer saved = insert(dto());

        assertThat(saved.getCNumber()).isEqualTo(1138);
        assertThat(saved.getCNumberDisplay()).isEqualTo("C000001138");
    }

    @Test
    @DisplayName("the first customer of a company starts at 1")
    void insertNumbersFirstCustomer() {
        when(customers.findMaxCNumber(COMPANY)).thenReturn(0);

        Customer saved = insert(dto());

        assertThat(saved.getCNumber()).isEqualTo(1);
        assertThat(saved.getCNumberDisplay()).isEqualTo("C000000001");
    }

    @Test
    @DisplayName("insert upper-cases the name columns but not Zipcode or GSTNO")
    void insertUpperCasesTheRightColumns() {
        CustomerDto dto = dto();
        dto.setAddress1("12 jalan besar");
        dto.setPersonId("roc-991");
        dto.setCity("klang");
        dto.setState("Selangor");
        dto.setUserName("basong");
        dto.setOName("siti");
        dto.setAName("raj");
        // Raw on insert; the update branch upper-cases these two.
        dto.setZipcode("42000");
        dto.setGstNo("gst-77");

        Customer saved = insert(dto);

        assertThat(saved.getCustomerName()).isEqualTo("BASONG ENVIRONMENTAL");
        assertThat(saved.getAddress1()).isEqualTo("12 JALAN BESAR");
        assertThat(saved.getPersonId()).isEqualTo("ROC-991");
        assertThat(saved.getCity()).isEqualTo("KLANG");
        assertThat(saved.getState()).isEqualTo("SELANGOR");
        assertThat(saved.getUserName()).isEqualTo("BASONG");
        assertThat(saved.getOName()).isEqualTo("SITI");
        assertThat(saved.getAName()).isEqualTo("RAJ");
        assertThat(saved.getZipcode()).isEqualTo("42000");
        assertThat(saved.getGstNo()).isEqualTo("gst-77");
    }

    @Test
    @DisplayName("insert coalesces absent text to empty and stamps the audit columns")
    void insertCoalescesAndStamps() {
        Customer saved = insert(dto());

        assertThat(saved.getAddress1()).isEmpty();
        assertThat(saved.getEmail()).isEmpty();
        assertThat(saved.getTokenId()).isEmpty();
        assertThat(saved.getModifiedBy()).isEqualTo("SA");
        assertThat(saved.getCreatedDate()).isNotNull();
        assertThat(saved.getModifiedDate()).isNotNull();
        // Tax columns are stored exactly as given — null stays null.
        assertThat(saved.getTinNo()).isNull();
    }

    @Test
    @DisplayName("an insert is always active, whatever the payload asks for")
    void insertForcesActive() {
        CustomerDto dto = dto();
        dto.setActive(2);

        assertThat(insert(dto).getActive()).isEqualTo(1);
    }

    @Test
    @DisplayName("the zero date means no expiry")
    void insertTreatsZeroDateAsNull() {
        CustomerDto dto = dto();
        dto.setExpiryDate(LocalDate.of(1900, 1, 1));
        assertThat(insert(dto).getExpiryDate()).isNull();

        dto.setExpiryDate(LocalDate.of(2027, 3, 31));
        assertThat(insert(dto).getExpiryDate()).isEqualTo(LocalDate.of(2027, 3, 31));
    }

    @Test
    @DisplayName("missing reference ids become 0, not a NOT NULL violation")
    void insertDefaultsReferenceIds() {
        CustomerDto dto = dto();
        dto.setSymbolRefid(null);
        dto.setPaymentTermsRefid(null);

        Customer saved = insert(dto);

        assertThat(saved.getSymbolRefid()).isZero();
        assertThat(saved.getPaymentTermsRefid()).isZero();
    }

    @Test
    @DisplayName("an unknown symbol is refused before anything is written")
    void insertRejectsUnknownSymbol() {
        when(symbols.existsByIdAndCompanyRefIdAndActive(2, COMPANY, 1)).thenReturn(false);

        assertThatThrownBy(() -> insert(dto()))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Currency symbol 2");

        // The procedure checks before it writes; no orphan account row.
        verify(accountGroups, never()).save(any());
        verify(customers, never()).save(any());
    }

    @Test
    @DisplayName("an unknown payment term is refused before anything is written")
    void insertRejectsUnknownPaymentTerm() {
        when(paymentTerms.existsByIdAndCompanyRefIdAndActive(3, COMPANY, 1)).thenReturn(false);

        assertThatThrownBy(() -> insert(dto()))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Payment term 3");
        verify(customers, never()).save(any());
    }

    @Test
    @DisplayName("zero means 'not chosen' and skips the master check")
    void insertAllowsZeroReferences() {
        CustomerDto dto = dto();
        dto.setSymbolRefid(0);
        dto.setPaymentTermsRefid(0);
        dto.setCountryId(0);

        assertThat(insert(dto).getSymbolRefid()).isZero();
        verify(symbols, never()).existsByIdAndCompanyRefIdAndActive(eq(0), anyInt(), anyInt());
    }

    @Test
    @DisplayName("a company with no CUSTOMERS group cannot take a customer")
    void insertRejectsMissingAccountGroup() {
        when(accountGroups.findFirstByAccountNameAndAccountCodeAndCompanyRefIdAndActive(
                "CUSTOMERS", "CUS", COMPANY, 1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> insert(dto()))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("CUSTOMERS");
        verify(customers, never()).save(any());
    }

    // ─── update ─────────────────────────────────────────────────────────

    private Customer existingRow() {
        Customer existing = new Customer();
        existing.setId(1245);
        existing.setCompanyRefId(COMPANY);
        existing.setAccountRefid(4662);
        existing.setCNumber(1137);
        existing.setCNumberDisplay("C000001137");
        existing.setLatitude("3.05");
        existing.setLongitude("101.44");
        existing.setTokenId("device-token");
        existing.setModifiedBy("SA");
        return existing;
    }

    @Test
    @DisplayName("an edit keeps the number, the coordinates and the device token")
    void updateLeavesIdentityColumnsAlone() {
        CustomerDto dto = dto();
        dto.setId(1245);
        dto.setActive(1);

        Customer saved = writer.update(existingRow(), dto, COMPANY);

        assertThat(saved.getCNumber()).isEqualTo(1137);
        assertThat(saved.getCNumberDisplay()).isEqualTo("C000001137");
        assertThat(saved.getLatitude()).isEqualTo("3.05");
        assertThat(saved.getLongitude()).isEqualTo("101.44");
        assertThat(saved.getTokenId()).isEqualTo("device-token");
    }

    @Test
    @DisplayName("an edit upper-cases Zipcode and GSTNO, and does not upper-case UserName")
    void updateUpperCasesDifferentlyFromInsert() {
        CustomerDto dto = dto();
        dto.setId(1245);
        dto.setActive(1);
        dto.setZipcode("42000a");
        dto.setGstNo("gst-77");
        dto.setUserName("basong");

        Customer saved = writer.update(existingRow(), dto, COMPANY);

        assertThat(saved.getZipcode()).isEqualTo("42000A");
        assertThat(saved.getGstNo()).isEqualTo("GST-77");
        assertThat(saved.getUserName()).isEqualTo("basong");
    }

    @Test
    @DisplayName("an edit takes Active from the payload and renames the account row")
    void updateAppliesActiveAndRenamesAccount() {
        AccountsGroupMaster account = new AccountsGroupMaster();
        account.setId(4662);
        account.setAccountName("OLD NAME");
        when(accountGroups.findByIdAndCompanyRefId(4662, COMPANY)).thenReturn(Optional.of(account));

        CustomerDto dto = dto();
        dto.setId(1245);
        dto.setActive(2);

        Customer saved = writer.update(existingRow(), dto, COMPANY);

        assertThat(saved.getActive()).isEqualTo(2);
        assertThat(account.getAccountName()).isEqualTo("BASONG ENVIRONMENTAL");
        verify(accountGroups).save(account);
    }

    @Test
    @DisplayName("a missing account row does not fail the edit")
    void updateToleratesMissingAccountRow() {
        when(accountGroups.findByIdAndCompanyRefId(4662, COMPANY)).thenReturn(Optional.empty());

        CustomerDto dto = dto();
        dto.setId(1245);
        dto.setActive(1);

        assertThat(writer.update(existingRow(), dto, COMPANY)).isNotNull();
        verify(accountGroups, never()).save(any());
    }
}
