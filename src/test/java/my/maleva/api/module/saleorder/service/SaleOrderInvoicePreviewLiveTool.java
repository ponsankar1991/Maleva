package my.maleva.api.module.saleorder.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import my.maleva.api.module.saleorder.dto.SaleOrderInvoicePreview;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Developer tool, not a test: builds the Create Invoice preview for one sale
 * order against the configured database and prints it. Read-only — it never
 * calls {@code create}.
 *
 * <pre>
 * mvn -o -q test -Dtest=SaleOrderInvoicePreviewLiveTool -Dsurefire.failIfNoSpecifiedTests=false \
 *     -Dlive.saleOrder=20995 -Dlive.company=6
 * </pre>
 */
@SpringBootTest
class SaleOrderInvoicePreviewLiveTool {

    @Autowired
    private SaleOrderInvoiceCreationService service;

    @Test
    @EnabledIfSystemProperty(named = "live.saleOrder", matches = ".+")
    void run() throws Exception {
        int saleOrderId = Integer.parseInt(System.getProperty("live.saleOrder"));
        int companyId = Integer.parseInt(System.getProperty("live.company", "6"));

        SaleOrderInvoicePreview preview = service.preview(saleOrderId, companyId);

        ObjectMapper json = new ObjectMapper().findAndRegisterModules();
        System.out.println("=== preview: " + json.writerWithDefaultPrettyPrinter().writeValueAsString(preview));
    }
}
