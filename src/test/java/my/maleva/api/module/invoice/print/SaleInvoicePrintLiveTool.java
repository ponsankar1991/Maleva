package my.maleva.api.module.invoice.print;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Developer tool, not a test: renders one real invoice through the full
 * Spring context (JPA + JDBC + Jasper) against the configured database, so a
 * 500 from {@code GET /api/v1/sale-invoices/{id}/print} can be reproduced
 * with its stack trace and the PDF inspected.
 *
 * <pre>
 * mvn -o -q test -Dtest=SaleInvoicePrintLiveTool -Dsurefire.failIfNoSpecifiedTests=false \
 *     -Dlive.print.invoice=15897 -Dlive.print.company=6 -Dlive.print.out=C:/tmp
 * </pre>
 */
@SpringBootTest
class SaleInvoicePrintLiveTool {

    @Autowired
    private SaleInvoicePdfService pdfService;

    @Test
    @EnabledIfSystemProperty(named = "live.print.invoice", matches = "\\d+")
    void run() throws Exception {
        int invoiceId = Integer.parseInt(System.getProperty("live.print.invoice"));
        int companyId = Integer.parseInt(System.getProperty("live.print.company", "6"));

        SaleInvoicePdfService.RenderedInvoice rendered = pdfService.render(invoiceId, companyId)
                .orElseThrow(() -> new IllegalStateException("Invoice " + invoiceId + " not found for company " + companyId));

        System.out.println("=== rendered " + rendered.fileName() + " (" + rendered.pdf().length + " bytes)");
        String out = System.getProperty("live.print.out");
        if (out != null && !out.isBlank()) {
            Path target = Path.of(out, rendered.fileName());
            Files.write(target, rendered.pdf());
            System.out.println("=== written " + target);
        }
    }
}
