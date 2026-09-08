package my.maleva.api.module.saleorder.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import my.maleva.api.module.invoice.dto.MultiInvoiceDto;
import my.maleva.api.module.saleorder.dto.SaleOrderDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * Developer tool, not a test: runs the multi-sale-order load the Sale Invoice
 * screen uses, against the configured database, so a "No data found for
 * selected jobs" on screen can be pinned to the server or to the browser.
 *
 * <pre>
 * mvn -o -q test -Dtest=MultiSaleOrderLiveTool -Dsurefire.failIfNoSpecifiedTests=false \
 *     -Dlive.jobs=20498 -Dlive.company=6
 * </pre>
 */
@SpringBootTest
class MultiSaleOrderLiveTool {

    @Autowired
    private SaleOrderMasterService service;

    @Test
    @EnabledIfSystemProperty(named = "live.jobs", matches = ".+")
    void run() throws Exception {
        List<Integer> ids = java.util.Arrays.stream(System.getProperty("live.jobs").split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).map(Integer::valueOf).toList();
        int companyId = Integer.parseInt(System.getProperty("live.company", "6"));

        List<SaleOrderDTO> rows = service.editMultiSaleOrder(
                MultiInvoiceDto.builder().id(ids).comid(companyId).build());

        System.out.println("=== rows: " + rows.size());
        ObjectMapper json = new ObjectMapper().findAndRegisterModules();
        for (SaleOrderDTO row : rows) {
            String body = json.writeValueAsString(row);
            System.out.println("=== row: " + (body.length() > 1200 ? body.substring(0, 1200) + "..." : body));
        }
    }
}
