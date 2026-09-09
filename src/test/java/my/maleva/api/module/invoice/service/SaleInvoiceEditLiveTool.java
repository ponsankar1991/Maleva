package my.maleva.api.module.invoice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import my.maleva.api.module.invoice.dto.SaleInvoiceEditDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;

/**
 * Developer tool, not a test: loads one real invoice the way the edit screen
 * does and prints it. Read-only.
 *
 * <pre>
 * mvn -o surefire:test -Dtest=SaleInvoiceEditLiveTool -Dsurefire.failIfNoSpecifiedTests=false \
 *     -Dlive.company=6            # newest invoice for the company
 * mvn ... -Dlive.invoice=43933    # or one specific invoice
 * </pre>
 */
@SpringBootTest
class SaleInvoiceEditLiveTool {

    @Autowired
    private SaleInvoiceEditService service;

    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    @Test
    @EnabledIfSystemProperty(named = "live.company", matches = ".+")
    void run() throws Exception {
        int companyId = Integer.parseInt(System.getProperty("live.company"));
        int invoiceId = Integer.parseInt(System.getProperty("live.invoice", "0"));

        if (invoiceId == 0) {
            // The newest invoice that actually has lines — an empty one would
            // not show whether the line mapping works.
            List<Integer> ids = jdbc.query("""
                    SELECT TOP 1 SM.Id
                    FROM SaleMaster SM WITH (NOLOCK)
                    WHERE SM.CompanyRefId = :comid AND SM.Active = 1
                      AND EXISTS (SELECT 1 FROM SaleDetails SD WITH (NOLOCK)
                                  WHERE SD.SaleMasterRefId = SM.Id)
                    ORDER BY SM.Id DESC
                    """, new MapSqlParameterSource("comid", companyId), (rs, i) -> rs.getInt(1));
            if (ids.isEmpty()) {
                System.out.println("=== no invoice with lines for company " + companyId);
                return;
            }
            invoiceId = ids.get(0);
        }

        SaleInvoiceEditDto dto = service.load(invoiceId, companyId);
        ObjectMapper json = new ObjectMapper().findAndRegisterModules();
        System.out.println("=== edit-load invoice " + invoiceId + ": "
                + json.writerWithDefaultPrettyPrinter().writeValueAsString(dto));
    }
}
