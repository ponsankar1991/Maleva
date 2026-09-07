package my.maleva.api.module.salecreditmaster.service;

import my.maleva.api.module.salecreditmaster.dto.SaleCreditBillDto;
import my.maleva.api.module.salecreditmaster.dto.SaleCreditEditDto;
import my.maleva.api.module.salecreditmaster.dto.SaleCreditInvoiceLookupDto;
import my.maleva.api.module.salecreditmaster.dto.SaleCreditSaveRequest;
import my.maleva.api.module.salecreditmaster.dto.SaleCreditSaveResponse;
import my.maleva.api.module.salecreditmaster.dto.SaleCreditSearchRequest;
import my.maleva.api.module.salecreditmaster.dto.SaleCreditViewDto;

import java.util.List;
import java.util.Optional;

/**
 * The Sale Credit entry screen — the Java port of the legacy
 * {@code SaleCreditController} / {@code SaleCreditServices} pair, which drove
 * everything through {@code Exec [SP_SaleCreditMaster] '<json>'}.
 *
 * <p>The stored procedure is not called. Its logic lives here, where the
 * reference checks can name what is wrong instead of failing on
 * {@code 'Employee Not Found Issue id' + @EmployeeRefId} (varchar + int, which
 * raises a conversion error and hides the real reason), and where the money is
 * recomputed rather than trusted from the browser.
 *
 * <p>The generic CRUD of {@code SaleCreditMasterService} is left alone; this
 * interface is only what the screen needs.
 */
public interface SaleCreditEntryService {

    /**
     * The next credit note number, for display before the note is saved
     * ("CN000000012"). The real number is allocated inside
     * {@link #save(SaleCreditSaveRequest, Integer)}.
     */
    String nextCreditNoteNo(Integer companyId);

    /** Everything the customer still owes, for the knock-off grid. */
    List<SaleCreditBillDto> customerBills(Integer companyId, Integer customerId, Integer excludeCreditNoteId);

    /** Insert or replace a credit note, its lines and its knock-offs, in one transaction. */
    SaleCreditSaveResponse save(SaleCreditSaveRequest request, Integer headerCompanyId);

    /**
     * A saved credit note for the entry screen.
     *
     * @param id                 the credit note id, or null when loading by number
     * @param creditNoteNumber   {@code SaleCreditMaster.CNumber}, or null/0 when loading by id
     */
    Optional<SaleCreditEditDto> edit(Integer companyId, Integer id, Integer creditNoteNumber);

    /** The SALECREDIT ENTRY VIEW grid. */
    SaleCreditViewDto search(SaleCreditSearchRequest request);

    /** Deletes a credit note with its lines and knock-offs; returns the operator message. */
    String delete(Integer id, Integer companyId);

    /**
     * The invoice behind the screen's "Invoice No" box.
     *
     * @param invoiceNo the number the operator typed, e.g. INV000000123
     * @param invoiceId the invoice's row id — the {@code ?Id=} the Sale Invoice
     *                  screen links here with; one of the two must be given
     */
    Optional<SaleCreditInvoiceLookupDto> lookupInvoice(Integer companyId, String invoiceNo, Integer invoiceId);
}
