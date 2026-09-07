package my.maleva.api.module.invoice.einvoice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import my.maleva.api.integration.myinvois.MyInvoisDocumentCodec;
import my.maleva.api.integration.myinvois.ubl.UblDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The two things that make a document a credit note rather than an invoice:
 * the type code and the reference back to the invoice it corrects. Everything
 * else is the shared document, pinned by {@link EInvoiceDocumentBuilderTest}.
 */
class EInvoiceCreditNoteDocumentTest {

    private static final Instant ISSUED_AT = Instant.parse("2026-09-05T01:30:00Z");

    private EInvoiceDocumentBuilder builder;
    private final MyInvoisDocumentCodec codec = new MyInvoisDocumentCodec();
    private final ObjectMapper reader = new ObjectMapper();

    @BeforeEach
    void setUp() {
        builder = new EInvoiceDocumentBuilder(EInvoiceFixtures.properties());
    }

    @Test
    void creditNoteCarriesTypeCode02() throws Exception {
        JsonNode note = creditNote("INV000004711", "INV-UUID-1");

        assertThat(note.at("/InvoiceTypeCode/0/_").asText()).isEqualTo("02");
        assertThat(note.at("/InvoiceTypeCode/0/listVersionID").asText()).isEqualTo("1.0");
    }

    @Test
    void creditNoteNamesTheInvoiceItCorrects() throws Exception {
        JsonNode note = creditNote("INV000004711", "INV-UUID-1");

        assertThat(note.at("/BillingReference/0/InvoiceDocumentReference/0/ID/0/_").asText())
                .isEqualTo("INV000004711");
        assertThat(note.at("/BillingReference/0/InvoiceDocumentReference/0/UUID/0/_").asText())
                .isEqualTo("INV-UUID-1");
        assertThat(note.at("/BillingReference/0/InvoiceDocumentReference/0/DocumentType/0/_").asText())
                .isEqualTo("LHDNM Unique Identifier Number");
        // legacy sent the invoice number a second time as an additional reference
        assertThat(note.at("/BillingReference/1/AdditionalDocumentReference/0/ID/0/_").asText())
                .isEqualTo("INV000004711");
    }

    @Test
    void anInvoiceNeverEInvoicedContributesItsNumberButNoEmptyUuid() throws Exception {
        JsonNode note = creditNote("INV000004711", "  ");

        assertThat(note.at("/BillingReference/0/InvoiceDocumentReference/0/ID/0/_").asText())
                .isEqualTo("INV000004711");
        // Legacy emitted UUID and DocumentType with an empty value, which LHDN rejects.
        assertThat(note.at("/BillingReference/0/InvoiceDocumentReference/0/UUID").isMissingNode()).isTrue();
        assertThat(note.at("/BillingReference/0/InvoiceDocumentReference/0/DocumentType").isMissingNode()).isTrue();
    }

    @Test
    void aCreditNoteWithNoInvoiceAtAllSendsNoBillingReference() throws Exception {
        JsonNode note = creditNote(null, null);

        assertThat(note.at("/BillingReference").isMissingNode()).isTrue();
    }

    @Test
    void theDefaultBuildIsStillAPlainInvoice() throws Exception {
        JsonNode invoice = invoice(builder.build(EInvoiceFixtures.snapshot(), ISSUED_AT));

        assertThat(invoice.at("/InvoiceTypeCode/0/_").asText()).isEqualTo("01");
        assertThat(invoice.at("/BillingReference/0/InvoiceDocumentReference").isMissingNode()).isTrue();
        assertThat(invoice.at("/BillingReference/0/AdditionalDocumentReference/0/ID/0/_").asText())
                .isEqualTo("PO-778");
    }

    private JsonNode creditNote(String invoiceNo, String invoiceUuid) throws Exception {
        return invoice(builder.build(EInvoiceFixtures.snapshot(), ISSUED_AT,
                EInvoiceDocumentKind.creditNote(invoiceNo, invoiceUuid)));
    }

    private JsonNode invoice(UblDocument document) throws Exception {
        return reader.readTree(codec.toJson(document)).get("Invoice").get(0);
    }
}
