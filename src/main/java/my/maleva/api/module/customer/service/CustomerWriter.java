package my.maleva.api.module.customer.service;

import lombok.RequiredArgsConstructor;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.accountsgroupmaster.entity.AccountsGroupMaster;
import my.maleva.api.module.accountsgroupmaster.repository.AccountsGroupMasterRepository;
import my.maleva.api.module.customer.dto.CustomerDto;
import my.maleva.api.module.customer.entity.Customer;
import my.maleva.api.module.customer.repository.CustomerRepository;
import my.maleva.api.module.master.repository.CountryMasterRepository;
import my.maleva.api.module.master.repository.SymbolMasterRepository;
import my.maleva.api.module.payment.repository.PaymentTermsMasterRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Writes a customer in Java, the port of {@code SP_Customer}'s insert and
 * update branches.
 *
 * <p>The procedure serialises the payload to JSON, shreds it back with OPENJSON
 * into a temp table and reads it into eighty local variables to do one INSERT.
 * This does the same writes directly. The JSON round trip is not just slower:
 * the .NET caller built the string by hand and ran
 * {@code details.Replace("'", "")} over it first, so a customer named
 * O'BRIEN SHIPPING was silently stored as OBRIEN SHIPPING. Nothing here strips
 * anything.
 *
 * <p><b>Behaviour is copied, not improved.</b> The .NET screen still calls the
 * procedure against the same tables, so a customer written here must be
 * indistinguishable from one it wrote. The quirks below are deliberate:
 *
 * <ul>
 *   <li><b>A customer is two rows.</b> The insert creates an
 *       {@link AccountsGroupMaster} row under the company's CUSTOMERS/CUS group
 *       first and stores its id in {@code Customer.AccountRefid}. Without it the
 *       customer has no account and never appears in the ledger screens. An edit
 *       renames that row to match.</li>
 *   <li><b>The account code counts siblings.</b> {@code CUS-<childCount + 1>},
 *       which is not unique under concurrency and can be reused after a delete.
 *       Copied as-is — the legacy rows already look like this.</li>
 *   <li><b>Upper-casing differs between insert and update</b>, and not by
 *       design. Zipcode and GSTNO are stored as typed on insert but upper-cased
 *       on edit; UserName is the reverse. See {@link #upper} call sites — each
 *       one matches the branch of the procedure it came from.</li>
 *   <li><b>An edit does not touch CNumber, CNumberDisplay, Latitude, longitude,
 *       TokenId, Created_Date, Modified_By, AccountRefid or OpeningBalance.</b>
 *       The procedure comments the coordinate columns out and never lists the
 *       rest.</li>
 *   <li><b>An edit takes Active from the payload</b>; an insert always writes 1.</li>
 *   <li><b>Password is written raw on edit</b> (no null coalescing), so a null
 *       clears it. On insert it coalesces to empty.</li>
 * </ul>
 *
 * <p>One thing is deliberately <i>not</i> copied. The procedure's master checks
 * build their message as {@code 'Symbol Master Not Found Issue id' + @SymbolRefid},
 * concatenating a varchar with an int, which raises a conversion error instead of
 * the message — so an invalid reference surfaced as
 * "Conversion failed when converting the varchar value ... to data type int".
 * {@link #validateReferences} reports what actually went wrong.
 */
@Component
@RequiredArgsConstructor
public class CustomerWriter {

    /** The account group every customer hangs under, keyed by name AND code. */
    private static final String CUSTOMER_GROUP_NAME = "CUSTOMERS";
    private static final String CUSTOMER_GROUP_CODE = "CUS";

    /** Width of the zero-padded customer number: C + 9 digits. */
    private static final int CNUMBER_WIDTH = 9;

    private static final int ACTIVE = 1;

    /** T-SQL's zero date, what an empty string converts to when compared to a date. */
    private static final LocalDate ZERO_DATE = LocalDate.of(1900, 1, 1);

    /**
     * The procedure hardcodes 'SA' as the customer's Modified_By and uses
     * SUSER_NAME() for the account row. There is no SQL login behind a JDBC
     * pool worth recording, so both get the same constant the rows already
     * carry.
     */
    private static final String MODIFIED_BY = "SA";

    private final CustomerRepository customers;
    private final AccountsGroupMasterRepository accountGroups;
    private final SymbolMasterRepository symbols;
    private final PaymentTermsMasterRepository paymentTerms;
    private final CountryMasterRepository countries;

    /**
     * Creates the account row, then the customer that points at it.
     *
     * @return the saved customer, with its generated id, CNumber and AccountRefid
     */
    public Customer insert(CustomerDto dto, Integer companyId) {
        validateReferences(dto, companyId);

        AccountsGroupMaster account = insertAccountRow(dto.getCustomerName(), companyId);

        int nextNumber = nextCNumber(companyId);
        LocalDateTime now = LocalDateTime.now();

        Customer customer = new Customer();
        customer.setCompanyRefId(companyId);
        customer.setAccountRefid(account.getId());
        customer.setCNumber(nextNumber);
        customer.setCNumberDisplay(formatCNumber(nextNumber));
        customer.setCustomerName(upper(dto.getCustomerName()));

        customer.setAddress1(upper(orEmpty(dto.getAddress1())));
        customer.setAddress2(upper(orEmpty(dto.getAddress2())));
        customer.setAddress3(upper(orEmpty(dto.getAddress3())));
        customer.setPersonId(upper(orEmpty(dto.getPersonId())));
        customer.setCity(upper(orEmpty(dto.getCity())));
        customer.setState(upper(orEmpty(dto.getState())));
        // Not upper-cased on insert, but upper-cased on update. The procedure
        // really is inconsistent here; both branches are reproduced as written.
        customer.setZipcode(orEmpty(dto.getZipcode()));
        customer.setCountry(upper(orEmpty(dto.getCountry())));
        customer.setCountryId(dto.getCountryId());
        customer.setCustomerCity(dto.getCustomerCity());

        customer.setSymbolRefid(refId(dto.getSymbolRefid()));
        customer.setPaymentTermsRefid(refId(dto.getPaymentTermsRefid()));
        customer.setGstNo(orEmpty(dto.getGstNo()));
        customer.setEmail(orEmpty(dto.getEmail()));
        customer.setMobileNo(orEmpty(dto.getMobileNo()));
        customer.setUserName(upper(orEmpty(dto.getUserName())));
        customer.setPassword(orEmpty(dto.getPassword()));

        customer.setLatitude(orEmpty(dto.getLatitude()));
        customer.setLongitude(orEmpty(dto.getLongitude()));
        customer.setTokenId(orEmpty(dto.getTokenId()));

        applyContacts(customer, dto);
        applyTaxAndBank(customer, dto);

        customer.setCompanyCode(upper(dto.getCompanyCode()));
        customer.setUpdateId(dto.getUpdateId());

        // An insert is always active, whatever the payload says.
        customer.setActive(ACTIVE);
        customer.setCreatedDate(now);
        customer.setModifiedDate(now);
        customer.setModifiedBy(MODIFIED_BY);

        return customers.save(customer);
    }

    /**
     * Updates the customer in place and renames its account row.
     *
     * @param existing the row already loaded and confirmed to belong to this company
     */
    public Customer update(Customer existing, CustomerDto dto, Integer companyId) {
        validateReferences(dto, companyId);

        existing.setCompanyRefId(companyId);
        existing.setCustomerName(upper(dto.getCustomerName()));

        existing.setAddress1(upper(orEmpty(dto.getAddress1())));
        existing.setAddress2(upper(orEmpty(dto.getAddress2())));
        existing.setAddress3(upper(orEmpty(dto.getAddress3())));
        existing.setPersonId(upper(orEmpty(dto.getPersonId())));
        existing.setCity(upper(orEmpty(dto.getCity())));
        existing.setState(upper(orEmpty(dto.getState())));
        // Upper-cased here, not on insert. See the class note.
        existing.setZipcode(upper(orEmpty(dto.getZipcode())));
        existing.setCountry(upper(orEmpty(dto.getCountry())));
        existing.setCountryId(dto.getCountryId());
        existing.setCustomerCity(dto.getCustomerCity());

        existing.setSymbolRefid(refId(dto.getSymbolRefid()));
        existing.setPaymentTermsRefid(refId(dto.getPaymentTermsRefid()));
        existing.setGstNo(upper(orEmpty(dto.getGstNo())));
        existing.setEmail(orEmpty(dto.getEmail()));
        existing.setMobileNo(orEmpty(dto.getMobileNo()));
        // Not upper-cased here, unlike the insert.
        existing.setUserName(orEmpty(dto.getUserName()));
        // Raw, with no coalesce: a null payload clears the stored password.
        existing.setPassword(dto.getPassword());

        // Latitude, longitude and TokenId are commented out of the procedure's
        // UPDATE. An edit must not disturb a device's registration.

        applyContacts(existing, dto);
        applyTaxAndBank(existing, dto);

        existing.setCompanyCode(upper(dto.getCompanyCode()));
        existing.setUpdateId(dto.getUpdateId());

        existing.setActive(dto.getActive());
        existing.setModifiedDate(LocalDateTime.now());
        // Modified_By is not in the procedure's SET list.

        Customer saved = customers.save(existing);
        renameAccountRow(saved, companyId);
        return saved;
    }

    // ─── The account row ────────────────────────────────────────────────

    private AccountsGroupMaster insertAccountRow(String customerName, Integer companyId) {
        AccountsGroupMaster parent = accountGroups
                .findFirstByAccountNameAndAccountCodeAndCompanyRefIdAndActive(
                        CUSTOMER_GROUP_NAME, CUSTOMER_GROUP_CODE, companyId, ACTIVE)
                .orElseThrow(() -> new InvalidRequestException(
                        "This company has no active CUSTOMERS (CUS) account group, "
                                + "so a customer account cannot be created."));

        // Sibling count + 1, exactly as the procedure computes it. Two customers
        // created in the same second can therefore be handed the same code; the
        // legacy data already contains such pairs, so this is not tightened here.
        int nextChild = accountGroups.countByParentIdAndCompanyRefId(parent.getId(), companyId) + 1;

        LocalDateTime now = LocalDateTime.now();
        AccountsGroupMaster account = new AccountsGroupMaster();
        account.setCompanyRefId(companyId);
        account.setAccountName(upper(customerName));
        account.setAccountCode(CUSTOMER_GROUP_CODE + "-" + nextChild);
        account.setParentId(parent.getId());
        account.setEditmode(1);
        account.setNoChild(1);
        account.setActive(ACTIVE);
        account.setCreatedDate(now);
        account.setModifiedDate(now);
        account.setModifiedBy(MODIFIED_BY);

        return accountGroups.save(account);
    }

    /**
     * Keeps the ledger's name in step with the customer's. Silent when the link
     * is missing or points elsewhere: the procedure's UPDATE simply matched no
     * row, and a rename is not worth failing a save over.
     */
    private void renameAccountRow(Customer customer, Integer companyId) {
        if (customer.getAccountRefid() == null) {
            return;
        }
        accountGroups.findByIdAndCompanyRefId(customer.getAccountRefid(), companyId)
                .ifPresent(account -> {
                    account.setAccountName(upper(customer.getCustomerName()));
                    accountGroups.save(account);
                });
    }

    // ─── Numbering ──────────────────────────────────────────────────────

    /**
     * {@code MAX(CNumber) + 1} for this company — there is no SequenceNoMaster
     * row behind customers, unlike invoices.
     *
     * <p>Read-then-insert, the same race the procedure has: two creates landing
     * together can both read 1137 and both write 1138. The transaction does not
     * prevent it, because a MAX over rows that do not exist yet takes no lock.
     * Left as-is to match; closing it properly means a unique index on
     * (CompanyRefId, CNumber) plus a retry, which is a schema change.
     */
    private int nextCNumber(Integer companyId) {
        Integer highest = customers.findMaxCNumber(companyId);
        return (highest == null ? 0 : highest) + 1;
    }

    /** {@code 'C' + RIGHT('000000000' + cast(n as varchar), 9)}. */
    private String formatCNumber(int number) {
        String digits = String.valueOf(number);
        if (digits.length() > CNUMBER_WIDTH) {
            // RIGHT() keeps the LAST nine characters, so a number that outgrows
            // the width wraps rather than widening. Matched, not fixed.
            digits = digits.substring(digits.length() - CNUMBER_WIDTH);
        }
        return "C" + "0".repeat(CNUMBER_WIDTH - digits.length()) + digits;
    }

    // ─── Shared column groups ───────────────────────────────────────────

    /** Operations and accounts contacts: identical rules in both branches. */
    private void applyContacts(Customer customer, CustomerDto dto) {
        customer.setOEmail(orEmpty(dto.getOEmail()));
        customer.setOEmail1(orEmpty(dto.getOEmail1()));
        customer.setOName(upper(orEmpty(dto.getOName())));
        customer.setOPhone(orEmpty(dto.getOPhone()));
        customer.setAEmail(orEmpty(dto.getAEmail()));
        customer.setAEmail1(orEmpty(dto.getAEmail1()));
        customer.setAName(upper(orEmpty(dto.getAName())));
        customer.setAPhone(orEmpty(dto.getAPhone()));
    }

    /**
     * Tax, e-invoice and bank columns. Stored exactly as typed in both branches
     * — no upper-casing and no coalescing, so a null stays a null here.
     */
    private void applyTaxAndBank(Customer customer, CustomerDto dto) {
        customer.setTinNo(dto.getTinNo());
        customer.setSstNo(dto.getSstNo());
        customer.setMsicCode(dto.getMsicCode());
        customer.setServiceTaxType(dto.getServiceTaxType());
        customer.setBankName(dto.getBankName());
        customer.setAccountNo(dto.getAccountNo());
        customer.setTintype(dto.getTintype());
        customer.setCustomerTin(dto.getCustomerTin());
        customer.setEInvoice(dto.getEInvoice());
        customer.setExemptionNo(dto.getExemptionNo());
        customer.setExemptionDetails(dto.getExemptionDetails());
        customer.setRegistrationNo(dto.getRegistrationNo());
        customer.setExpiryDate(expiryDate(dto.getExpiryDate()));
    }

    // ─── Reference checks ───────────────────────────────────────────────

    /**
     * The three master checks the procedure runs before it writes anything.
     * Zero means "not chosen" and is allowed through, as it is there.
     */
    private void validateReferences(CustomerDto dto, Integer companyId) {
        Integer symbolId = dto.getSymbolRefid();
        if (symbolId != null && symbolId != 0
                && !symbols.existsByIdAndCompanyRefIdAndActive(symbolId, companyId, ACTIVE)) {
            throw new InvalidRequestException(
                    "Currency symbol " + symbolId + " is not an active symbol for this company.");
        }

        Integer termsId = dto.getPaymentTermsRefid();
        if (termsId != null && termsId != 0
                && !paymentTerms.existsByIdAndCompanyRefIdAndActive(termsId, companyId, ACTIVE)) {
            throw new InvalidRequestException(
                    "Payment term " + termsId + " is not an active payment term for this company.");
        }

        Integer countryId = dto.getCountryId();
        if (countryId != null && countryId != 0 && !countries.existsById(countryId)) {
            throw new InvalidRequestException("Country " + countryId + " does not exist.");
        }
    }

    // ─── T-SQL helpers ──────────────────────────────────────────────────

    /**
     * {@code if @ExpiryDate = '' set @ExpiryDate = null}.
     *
     * <p>That comparison is not the empty-string test it looks like: the
     * variable is a {@code date}, and T-SQL converts {@code ''} to 1900-01-01
     * before comparing. So the rule is really "the zero date means no date",
     * and a form that posts 1900-01-01 must store NULL rather than a date that
     * would later read as a long-expired exemption.
     */
    private LocalDate expiryDate(LocalDate value) {
        return ZERO_DATE.equals(value) ? null : value;
    }

    /** {@code isnull(x, '')}. */
    private String orEmpty(String value) {
        return value == null ? "" : value;
    }

    /**
     * {@code UPPER(x)}, null-safe the way T-SQL is: UPPER(NULL) is NULL.
     *
     * <p>{@code Locale.ROOT} on purpose. The no-argument {@code toUpperCase()}
     * uses the JVM's default locale, and on a Turkish one "i" becomes "İ" — so
     * the same customer name would be stored differently depending on the
     * server's regional settings, and would stop matching the rows SQL Server's
     * collation-based UPPER already wrote.
     */
    private String upper(String value) {
        return value == null ? null : value.toUpperCase(Locale.ROOT);
    }

    /**
     * A reference id for a NOT NULL column. SymbolRefid and PaymentTermsRefid
     * are {@code int NOT NULL} in the database, but nothing stops a caller
     * omitting them; a null would surface as a constraint violation from deep
     * inside Hibernate rather than anything a caller can act on. Zero is what
     * "not chosen" already means to {@link #validateReferences} and to the
     * procedure's {@code If @SymbolRefid <> 0} guards.
     */
    private int refId(Integer value) {
        return value == null ? 0 : value;
    }
}
