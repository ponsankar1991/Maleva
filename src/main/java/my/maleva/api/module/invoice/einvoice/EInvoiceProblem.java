package my.maleva.api.module.invoice.einvoice;

/**
 * One reason an invoice cannot be sent, in words an operator can act on.
 *
 * <p>Every message names the invoice and, where it applies, the customer or
 * the line, and says what to fix: "Invoice INV000000123: customer ACME SDN
 * BHD has no TIN — update the customer master and push again." The legacy
 * validator reported a property path ({@code Invoice[0].AccountingCustomerParty
 * [0].Party[0].PartyIdentification}) that told the operator nothing.
 *
 * @param code    stable identifier for the rule, for logs and tests
 * @param message the operator-facing text
 */
public record EInvoiceProblem(String code, String message) {

    public static EInvoiceProblem of(String code, String message) {
        return new EInvoiceProblem(code, message);
    }
}
