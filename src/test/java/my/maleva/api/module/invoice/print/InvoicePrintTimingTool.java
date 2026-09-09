package my.maleva.api.module.invoice.print;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Developer tool, not a test: prints where the time goes when an invoice is
 * rendered, so "the report takes ten seconds" can be answered with numbers
 * instead of a guess.
 *
 * <p>Times each phase separately — template compile, snapshot load, Jasper
 * fill, PDF export — and repeats the whole thing, because the first render in
 * a process pays one-off costs (template compilation, font extension
 * discovery) that every later one does not.
 *
 * <pre>
 * mvn -o test -Dtest=InvoicePrintTimingTool -Dsurefire.failIfNoSpecifiedTests=false \
 *     -Dtiming.company=6            # newest invoice for the company
 * </pre>
 */
@SpringBootTest
class InvoicePrintTimingTool {

    @Autowired
    private InvoicePrintSnapshotLoader loader;

    @Autowired
    private SaleInvoicePdfService pdfService;

    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    @Test
    @EnabledIfSystemProperty(named = "timing.company", matches = ".+")
    void run() throws Exception {
        int companyId = Integer.parseInt(System.getProperty("timing.company"));
        int invoiceId = Integer.parseInt(System.getProperty("timing.invoice", "0"));
        if (invoiceId == 0) {
            List<Integer> ids = jdbc.query("""
                    SELECT TOP 1 Id FROM SaleMaster WITH (NOLOCK)
                    WHERE CompanyRefId = :comid AND Active = 1
                    ORDER BY Id DESC
                    """, new MapSqlParameterSource("comid", companyId), (rs, i) -> rs.getInt(1));
            if (ids.isEmpty()) {
                System.out.println("=== timing: no invoice for company " + companyId);
                return;
            }
            invoiceId = ids.get(0);
        }
        System.out.println("=== timing invoice " + invoiceId + " (company " + companyId + ")");

        // One-off costs, measured on their own.
        long t0 = System.nanoTime();
        JasperReport compiled;
        try (InputStream in = new ClassPathResource(SaleInvoicePdfService.TEMPLATE).getInputStream()) {
            compiled = JasperCompileManager.compileReport(in);
        }
        report("template compile (once per process)", t0);

        byte[] logo;
        try (InputStream in = new ClassPathResource(SaleInvoicePdfService.LOGO).getInputStream()) {
            logo = in.readAllBytes();
        }

        for (int pass = 1; pass <= 3; pass++) {
            System.out.println("--- pass " + pass);

            long tLoad = System.nanoTime();
            InvoicePrintSnapshot snapshot = loader.load(invoiceId, companyId).orElseThrow();
            report("  snapshot load (SQL)", tLoad);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("SNAPSHOT", snapshot);
            parameters.put("QR_IMAGE", snapshot.getQrPng() == null
                    ? null : new ByteArrayInputStream(snapshot.getQrPng()));
            parameters.put("LOGO", new ByteArrayInputStream(logo));

            long tFill = System.nanoTime();
            JasperPrint print = JasperFillManager.fillReport(
                    compiled, parameters, new JRBeanCollectionDataSource(snapshot.getLines()));
            report("  jasper fill", tFill);

            long tExport = System.nanoTime();
            byte[] pdf = JasperExportManager.exportReportToPdf(print);
            report("  pdf export (" + pdf.length / 1024 + " KB)", tExport);

            long tWhole = System.nanoTime();
            pdfService.render(invoiceId, companyId).orElseThrow();
            report("  whole render() as the endpoint calls it", tWhole);
        }
    }

    private void report(String label, long startedAt) {
        System.out.printf("%-52s %6d ms%n", label, (System.nanoTime() - startedAt) / 1_000_000);
    }
}
