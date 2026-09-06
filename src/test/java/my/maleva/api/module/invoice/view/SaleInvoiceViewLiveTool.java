package my.maleva.api.module.invoice.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.time.LocalDate;

/**
 * Developer tool, not a test: runs the real view query against the demo
 * database through {@link SaleInvoiceViewService}, bypassing HTTP security,
 * and prints what the screen would receive. Proves the SQL, the row mapping
 * and the JSON names in one go.
 *
 * <pre>
 * mvn -o -q test -Dtest=SaleInvoiceViewLiveTool -Dsurefire.failIfNoSpecifiedTests=false \
 *     -Dlive.view.company=1 -Dlive.view.from=2026-08-01 -Dlive.view.to=2026-09-06
 * </pre>
 */
class SaleInvoiceViewLiveTool {

    @Test
    @EnabledIfSystemProperty(named = "live.view.company", matches = "\\d+")
    void run() throws Exception {
        DriverManagerDataSource ds = new DriverManagerDataSource(
                System.getProperty("db.url",
                        "jdbc:sqlserver://103.215.139.8:1433;databaseName=MalevanewDemo;encrypt=true;trustServerCertificate=true;loginTimeout=30"),
                System.getProperty("db.user", "sa"),
                System.getProperty("db.password", "Kassamy@123"));
        SaleInvoiceViewService service = new SaleInvoiceViewService(new NamedParameterJdbcTemplate(ds));
        ObjectMapper json = new ObjectMapper().findAndRegisterModules();

        SaleInvoiceViewFilter.SaleInvoiceViewFilterBuilder base = SaleInvoiceViewFilter.builder()
                .companyId(Integer.parseInt(System.getProperty("live.view.company")))
                .fromDate(LocalDate.parse(System.getProperty("live.view.from")))
                .toDate(LocalDate.parse(System.getProperty("live.view.to")));

        SaleInvoiceViewResult byDate = service.view(base.build());
        System.out.println("=== date range: " + byDate.master().size() + " invoices, " + byDate.details().size() + " lines");
        if (!byDate.master().isEmpty()) {
            System.out.println("first row: " + json.writeValueAsString(byDate.master().get(0)));
            System.out.println("first line: " + json.writeValueAsString(byDate.details().get(0)));

            String no = byDate.master().get(0).getBillNoDisplay();
            SaleInvoiceViewResult bySearch = service.view(base.search(no).customerId(-1).build());
            System.out.println("=== exact search " + no + ": " + bySearch.master().size() + " invoice(s)");

            String job = byDate.master().stream().map(SaleInvoiceViewRow::getJobNo)
                    .filter(j -> j != null && !j.isBlank()).findFirst().orElse("");
            if (!job.isBlank()) {
                SaleInvoiceViewResult byJob = service.view(base.search(job).searchByJobNo(true).build());
                System.out.println("=== job search " + job + ": " + byJob.master().size() + " invoice(s)");
            }
        }
        SaleInvoiceViewResult unpushed = service.view(base.search(null).unpushedOnly(true).build());
        System.out.println("=== unpushed only: " + unpushed.master().size() + " invoices");
        SaleInvoiceViewResult eta = service.view(base.search(null).eta(true).etaType(0).build());
        System.out.println("=== eta mode: " + eta.master().size() + " invoices"
                + (eta.master().isEmpty() ? "" : ", DETA of first = " + eta.master().get(0).getDeta()));
    }
}
