package my.maleva.api.module.paymentrecept.print;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.mockito.Mockito;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

/**
 * Developer tool, not a test: times the receipt render — cold (template
 * compile), warm (fill + export), and cached — and prints the PDF size.
 * Runs only with {@code -Dreceipt.perf=true}.
 */
class ReceiptPdfPerfTool {

    @Test
    @EnabledIfSystemProperty(named = "receipt.perf", matches = "true")
    void time() {
        ReceiptPrintSnapshotLoader loader = Mockito.mock(ReceiptPrintSnapshotLoader.class);
        when(loader.load(anyInt(), anyInt())).thenAnswer(inv -> Optional.of(ReceiptPdfServiceTest.snapshot()));
        ReceiptPdfService service = new ReceiptPdfService(loader, ReceiptPdfServiceTest.receipts(), new ReportFonts());

        long t0 = System.nanoTime();
        byte[] cold = service.render(1, 6).orElseThrow().pdf();
        long t1 = System.nanoTime();
        System.out.printf("=== cold render (compile + fill + export): %d ms, %d bytes%n", (t1 - t0) / 1_000_000, cold.length);

        long warmTotal = 0;
        for (int i = 2; i <= 11; i++) {
            long s = System.nanoTime();
            service.render(i, 6).orElseThrow();
            warmTotal += System.nanoTime() - s;
        }
        System.out.printf("=== warm render (fill + export), avg of 10: %d ms%n", warmTotal / 10 / 1_000_000);

        long c0 = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            service.render(2, 6).orElseThrow();
        }
        System.out.printf("=== cached render, avg of 100: %d us%n", (System.nanoTime() - c0) / 100 / 1_000);
    }
}
