package my.maleva.api.module.supplier.dto;

/**
 * A supplier save's answer: the row as stored (after the QNE write-back), and
 * the QNE outcome beside it rather than instead of it — the supplier is saved
 * either way, which legacy's {@code ok = false} on a QNE refusal hid.
 */
public record SupplierSaveResponse(SupplierDto supplier, SupplierQneOutcome qne) {
}
