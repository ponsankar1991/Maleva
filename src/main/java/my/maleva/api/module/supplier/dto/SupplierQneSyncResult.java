package my.maleva.api.module.supplier.dto;

import java.util.List;

/**
 * The outcome of "Update from QNE" — legacy {@code UpdateSupplierId}, which
 * answered only Success or QNE's message and left the operator guessing what
 * it had done.
 *
 * @param status             COMPLETED, DISABLED (qne.enabled off), or FAILED
 *                           (QNE refused the list; nothing was changed locally)
 * @param message            a one-line summary, or QNE's text on failure
 * @param inQne              suppliers read from QNE, every page
 * @param alreadyLinked      QNE suppliers that already exist here by QNE code
 * @param idsRepaired        of those, how many had a missing or different QNE id stored
 * @param created            QNE suppliers created here
 * @param skippedWithoutCode QNE suppliers with no company code, which cannot be matched
 * @param failures           suppliers that could not be created, "CODE NAME: reason"
 */
public record SupplierQneSyncResult(
        Status status,
        String message,
        int inQne,
        int alreadyLinked,
        int idsRepaired,
        int created,
        int skippedWithoutCode,
        List<String> failures) {

    public enum Status { COMPLETED, DISABLED, FAILED }

    public static SupplierQneSyncResult disabled() {
        return new SupplierQneSyncResult(Status.DISABLED, "QNE integration is switched off", 0, 0, 0, 0, 0, List.of());
    }

    public static SupplierQneSyncResult failed(String message) {
        return new SupplierQneSyncResult(Status.FAILED, message, 0, 0, 0, 0, 0, List.of());
    }
}
