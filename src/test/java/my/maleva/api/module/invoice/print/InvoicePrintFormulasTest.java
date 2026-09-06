package my.maleva.api.module.invoice.print;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The Crystal formula fields of CRInvoice.rpt, pinned one by one so a change
 * to the loader cannot silently alter what the customer's copy says.
 *
 * <pre>
 * BLAWB : AWB and BL → "AWB/BL"; AWB only → AWB; else BL
 * Land  : Loadingvesselname, else 'NIL'      (Vessel onboard)
 * off   : Offvesselname, else 'NIL'          (Vessel offland)
 * CN    : CustomerName + '/' + Remarks when Remarks <> ''   (in the loader)
 * PName : SDRemarks when <> '', else PName                  (in the loader)
 * subTotalAmount : SalesRate * ItemQty                      (in the loader)
 * Amountinwords  : see AmountInWordsTest
 * </pre>
 */
class InvoicePrintFormulasTest {

    @Test
    void blawbJoinsBothOrShowsWhicheverExists() {
        assertThat(InvoicePrintSnapshotLoader.blAwb("AWB123", "BL-778")).isEqualTo("AWB123/BL-778");
        assertThat(InvoicePrintSnapshotLoader.blAwb("AWB123", "")).isEqualTo("AWB123");
        assertThat(InvoicePrintSnapshotLoader.blAwb("", "BL-778")).isEqualTo("BL-778");
        assertThat(InvoicePrintSnapshotLoader.blAwb(null, null)).isEqualTo("");
        assertThat(InvoicePrintSnapshotLoader.blAwb(" AWB123 ", " BL-778 ")).isEqualTo("AWB123/BL-778");
    }

    @Test
    void vesselRowsPrintNilWhenEmpty() {
        assertThat(InvoicePrintSnapshotLoader.nilIfBlank("MV STAR")).isEqualTo("MV STAR");
        assertThat(InvoicePrintSnapshotLoader.nilIfBlank("")).isEqualTo("NIL");
        assertThat(InvoicePrintSnapshotLoader.nilIfBlank("   ")).isEqualTo("NIL");
        assertThat(InvoicePrintSnapshotLoader.nilIfBlank(null)).isEqualTo("NIL");
    }
}
