package my.maleva.api.module.invoice.einvoice;

/**
 * Which LHDN document is being built, and what it refers back to.
 *
 * <p>The invoice and the credit note share every element except two: the
 * document type code, and the billing reference. A credit note has to name the
 * invoice it corrects — its number and the UUID LHDN gave that invoice — or
 * LHDN records it as an unrelated document and the customer's tax position
 * never nets off.
 *
 * @param typeCode              LHDN document type: 01 invoice, 02 credit note
 * @param originalDocumentNo    the corrected invoice's number; null for an invoice
 * @param originalDocumentUuid  the corrected invoice's LHDN UUID; null for an invoice
 * @param additionalReference   free-text reference sent as AdditionalDocumentReference
 */
public record EInvoiceDocumentKind(String typeCode,
                                   String originalDocumentNo,
                                   String originalDocumentUuid,
                                   String additionalReference) {

    /** LHDN document type 01 = Invoice. */
    public static final String TYPE_INVOICE = "01";
    /** LHDN document type 02 = Credit Note. */
    public static final String TYPE_CREDIT_NOTE = "02";

    /** How LHDN labels the UUID in a document reference. */
    public static final String UUID_DOCUMENT_TYPE = "LHDNM Unique Identifier Number";

    /** A plain invoice; its reference is whatever was typed in Remarks1. */
    public static EInvoiceDocumentKind invoice(String additionalReference) {
        return new EInvoiceDocumentKind(TYPE_INVOICE, null, null, additionalReference);
    }

    /**
     * A credit note against an invoice.
     *
     * @param invoiceNo   the invoice's number, e.g. INV000000123
     * @param invoiceUuid the invoice's LHDN UUID; blank when that invoice was
     *                    never e-invoiced, in which case only the number is sent
     */
    public static EInvoiceDocumentKind creditNote(String invoiceNo, String invoiceUuid) {
        return new EInvoiceDocumentKind(TYPE_CREDIT_NOTE, invoiceNo, invoiceUuid, invoiceNo);
    }

    public boolean isCreditNote() {
        return TYPE_CREDIT_NOTE.equals(typeCode);
    }
}
