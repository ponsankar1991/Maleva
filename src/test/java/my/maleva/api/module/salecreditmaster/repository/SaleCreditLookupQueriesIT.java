package my.maleva.api.module.salecreditmaster.repository;

import my.maleva.api.module.invoice.repository.SaleMasterRepository;
import my.maleva.api.module.salecreditmaster.service.SaleCreditEntryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Executes the credit-note screen's lookups against MalevanewDemo.
 *
 * <p>These exist because of a failure mode that no unit test and no successful
 * startup can catch: a derived query such as
 * {@code findFirstByCompanyRefIdAndCNumberDisplayAndActive} resolves its path
 * through the Lombok accessor {@code getCNumberDisplay()}, whose bean property
 * name keeps both capitals — {@link java.beans.Introspector} does not
 * decapitalise a name whose first two letters are upper case. Hibernate maps
 * these entities by field and only knows {@code cNumberDisplay}, so the
 * generated JPQL was only rejected the first time the endpoint was called:
 *
 * <pre>
 * Could not resolve attribute 'CNumberDisplay' of ...SaleMaster
 *   [SELECT s FROM SaleMaster s WHERE ... s.CNumberDisplay = :cNumberDisplay ...]
 * </pre>
 *
 * <p>Every query below is now spelled out as JPQL. Running them is what proves
 * it: no data is needed, because the fault was in the query, not the rows.
 */
@SpringBootTest
@Transactional
class SaleCreditLookupQueriesIT {

    private static final int COMPANY = 6;
    /** A number no document carries, so the assertions do not depend on the data. */
    private static final String UNUSED_NUMBER = "ZZ-NO-SUCH-DOCUMENT";
    private static final int UNUSED_CNUMBER = 2_000_000_000;

    @Autowired private SaleMasterRepository saleMasters;
    @Autowired private SaleCreditMasterRepository creditNotes;
    @Autowired private SaleCreditEntryService entryService;

    @Test
    void invoiceLookupByDisplayNumberExecutes() {
        assertThat(saleMasters.findByCompanyAndNumberDisplay(COMPANY, UNUSED_NUMBER, 1)).isEmpty();
    }

    @Test
    void theInvoiceNoBoxAnswersInsteadOfFailing() {
        // GET /api/sale-credits/invoice-lookup answered 500 before the fix.
        assertThat(entryService.lookupInvoice(COMPANY, UNUSED_NUMBER, null)).isEmpty();
    }

    @Test
    void creditNoteLookupsByCNumberExecute() {
        assertThat(creditNotes.findByCompanyRefIdAndCNumber(COMPANY, UNUSED_CNUMBER)).isEmpty();
        assertThat(creditNotes.existsByCompanyRefIdAndCNumber(COMPANY, UNUSED_CNUMBER)).isFalse();
        assertThat(creditNotes.findMaxCNumber(COMPANY)).isNotNull();
    }

    @Test
    void loadingACreditNoteByItsNumberExecutes() {
        // The entry screen's "open by credit note number" path.
        assertThat(entryService.edit(COMPANY, null, UNUSED_CNUMBER)).isEmpty();
    }

    @Test
    void theKnockOffAndViewQueriesExecute() {
        // Both are hand-written SQL rather than JPQL, but they are the rest of
        // what the screen calls on load; running them keeps a typo in either
        // from reaching the browser as a 500.
        assertThatCode(() -> entryService.customerBills(COMPANY, 1, 0)).doesNotThrowAnyException();
        assertThatCode(() -> entryService.nextCreditNoteNo(COMPANY)).doesNotThrowAnyException();
    }
}
