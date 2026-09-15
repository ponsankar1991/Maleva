package my.maleva.api.module.supplier.dto;

/**
 * What happened in QNE after a supplier save — the part of legacy
 * {@code InsertSupplier}'s answer that said whether QNE took the supplier.
 *
 * @param status  PUSHED (created in QNE now), ALREADY_IN_QNE (it has a QNE code,
 *                nothing sent — legacy built an update payload and never sent it),
 *                DISABLED (qne.enabled is off, like qneapilist.qneapi = false),
 *                FAILED (QNE refused or could not be reached; the supplier is saved)
 * @param qneId   QNE's GUID for the supplier, when known
 * @param qneCode QNE's company code, when known
 * @param message QNE's own text on failure, as the legacy MsgBox showed it
 */
public record SupplierQneOutcome(Status status, String qneId, String qneCode, String message) {

    public enum Status { PUSHED, ALREADY_IN_QNE, DISABLED, FAILED }

    public static SupplierQneOutcome pushed(String qneId, String qneCode) {
        return new SupplierQneOutcome(Status.PUSHED, qneId, qneCode,
                qneCode == null ? "QNE accepted the supplier but returned no company code" : "Pushed to QNE as " + qneCode);
    }

    public static SupplierQneOutcome alreadyInQne(String qneId, String qneCode) {
        return new SupplierQneOutcome(Status.ALREADY_IN_QNE, qneId, qneCode, "Already in QNE as " + qneCode);
    }

    public static SupplierQneOutcome disabled() {
        return new SupplierQneOutcome(Status.DISABLED, null, null, "QNE integration is switched off");
    }

    public static SupplierQneOutcome failed(String message) {
        return new SupplierQneOutcome(Status.FAILED, null, null, message);
    }
}
