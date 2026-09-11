package my.maleva.api.module.customerstatement.print;

import lombok.extern.slf4j.Slf4j;
import my.maleva.api.module.customerstatement.dto.CustomerStatement;
import my.maleva.api.module.customerstatement.dto.StatementResult;
import my.maleva.api.module.paymentrecept.print.ReportFonts;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Renders Statements of Account to PDF — the port of the Crystal path in
 * legacy {@code ReportViewer.aspx.cs#CustomerStatementReport}.
 *
 * <p>Legacy parked the query rows in the HTTP session, loaded
 * {@code CRCustomerStatementAllReport.rpt}, set eight letterhead parameters
 * from string literals, and either showed the viewer or exported to
 * {@code /Exports/CustomerStatement{n}.pdf} on disk. This takes the computed
 * {@link StatementResult} and returns bytes; nothing touches the session or
 * the file system, and the letterhead literals are here, in one place,
 * exactly as they were — the sample PDF is the reference for their wording.
 *
 * <p>Same shape as {@code ReceiptPdfService}: the template compiles once,
 * the logo is read once, the fonts context embeds Verdana.
 */
@Slf4j
@Service
public class CustomerStatementPdfService {

    static final String TEMPLATE = "reports/customer-statement.jrxml";
    static final String LOGO = "reports/logo.png";

    /**
     * The letterhead ReportViewer.aspx.cs hardcoded as Crystal parameters.
     * ADDRESS4 is the sample PDF's wording; the C# source has it wrapped in a
     * stray markdown link. These belong in CompanyMaster eventually — they are
     * one company's details in code — but that is a data move, not a port.
     */
    static final String COMPANY = "MALEVA (M) SDN BHD";
    static final String REG_NO = "Company Reg. No.: 201101015652 | Tax Reg. No.: W10-1809-32001584 | TIN: C22173439020";
    static final String ADDRESS1 = "No 20-1 JLN MPPMU 1 MEDAN PERNIAGAAN,";
    static final String ADDRESS2 = "70300 Seremban, Negeri Sembilan, Malaysia";
    static final String ADDRESS3 = "Tel: 012-290 7151 & 012-241 7151";
    static final String ADDRESS4 = "URL: www.maleva.com.my  Email: operation@maleva.com.my";
    static final String TITLE = "STATEMENT OF ACCOUNT";
    static final String NOTE1 = "WE SHALL BE GRATEFUL IF YOU LET US HAVE PAYMENT AS SOON AS POSSIBLE";
    static final String NOTE2 = "ANY DISCREPANCY IN THIS STATEMENT PLEASE INFORM US IN WRITING WITHIN 10 DAYS";

    private static final DateTimeFormatter FILE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final ReportFonts fonts;
    private volatile JasperReport compiled;
    private volatile byte[] logoBytes;

    public CustomerStatementPdfService(ReportFonts fonts) {
        this.fonts = fonts;
    }

    /** Compiles the template once the app is up, so the first View does not pay for it. */
    @EventListener(ApplicationReadyEvent.class)
    public void warmUp() {
        Thread warm = new Thread(() -> {
            try {
                long started = System.nanoTime();
                template();
                logo();
                log.info("Customer statement template warmed up in {} ms", (System.nanoTime() - started) / 1_000_000);
            } catch (Exception ex) {
                log.warn("Customer statement template warm-up failed (it will compile on first use): {}", ex.getMessage());
            }
        }, "customer-statement-warmup");
        warm.setDaemon(true);
        warm.start();
    }

    /** A rendered PDF and the file name to offer it under. */
    public record RenderedStatement(String fileName, byte[] pdf) {
    }

    /**
     * Every statement in the result, one customer per page run, as one PDF.
     *
     * @throws IllegalStateException when the result holds no statements —
     *         Jasper would produce a document with no pages, which a viewer
     *         shows as a broken file; the caller should not have asked
     */
    public RenderedStatement render(StatementResult result) {
        if (result.getStatements() == null || result.getStatements().isEmpty()) {
            throw new IllegalStateException("There is nothing to print: no customer has an outstanding invoice under these filters");
        }
        List<StatementPrintRow> rows = StatementPrintRow.flatten(result.getStatements());
        try {
            long started = System.nanoTime();
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("LOGO", new ByteArrayInputStream(logo()));
            parameters.put("COMPANY", COMPANY);
            parameters.put("REG_NO", REG_NO);
            parameters.put("ADDRESS1", ADDRESS1);
            parameters.put("ADDRESS2", ADDRESS2);
            parameters.put("ADDRESS3", ADDRESS3);
            parameters.put("ADDRESS4", ADDRESS4);
            parameters.put("TITLE", TITLE);
            parameters.put("NOTE1", NOTE1);
            parameters.put("NOTE2", NOTE2);

            JasperPrint print = JasperFillManager.getInstance(fonts.context())
                    .fill(template(), parameters, new JRBeanCollectionDataSource(rows));
            byte[] pdf = toCompressedPdf(print);
            String fileName = fileName(result);
            log.debug("Customer statement rendered: {} customers, {} lines, {} pages, {} bytes in {} ms",
                    result.getCustomerCount(), rows.size(), print.getPages().size(), pdf.length,
                    (System.nanoTime() - started) / 1_000_000);
            return new RenderedStatement(fileName, pdf);
        } catch (JRException ex) {
            throw new IllegalStateException("The customer statement could not be rendered", ex);
        }
    }

    /**
     * One customer's statement out of a many-customer result, as its own PDF —
     * the attachment for that customer's mail. The period and ageing window
     * are the result's; only the statement list is narrowed, so the document
     * is identical to what a single-customer View would render.
     */
    public RenderedStatement renderOne(StatementResult whole, CustomerStatement statement) {
        return render(StatementResult.builder()
                .statements(List.of(statement))
                .customerCount(1)
                .lineCount(statement.getLines() == null ? 0 : statement.getLines().size())
                .cutoffDate(whole.getCutoffDate())
                .periodFrom(whole.getPeriodFrom())
                .periodTo(whole.getPeriodTo())
                .ageingFrom(whole.getAgeingFrom())
                .ageingTo(whole.getAgeingTo())
                .build());
    }

    /**
     * {@code CustomerStatement_<customer>_<yyyyMMdd>.pdf}, or {@code _All_} for
     * an every-customer run. Legacy named every export
     * {@code CustomerStatement<customerId>.pdf} and reused the name, so a
     * browser offered the same file name for every customer.
     */
    private static String fileName(StatementResult result) {
        String who = result.getCustomerCount() == 1
                ? result.getStatements().get(0).getCustomerName()
                : "All";
        String safe = who.replaceAll("[^A-Za-z0-9]+", "_").replaceAll("^_+|_+$", "");
        if (safe.length() > 40) {
            safe = safe.substring(0, 40);
        }
        return "CustomerStatement_" + safe + "_" + LocalDate.now().format(FILE_DATE) + ".pdf";
    }

    private byte[] toCompressedPdf(JasperPrint print) throws JRException {
        JRPdfExporter exporter = new JRPdfExporter(fonts.context());
        exporter.setExporterInput(new SimpleExporterInput(print));
        ByteArrayOutputStream out = new ByteArrayOutputStream(64 * 1024);
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
        SimplePdfExporterConfiguration configuration = new SimplePdfExporterConfiguration();
        configuration.setCompressed(true);
        configuration.setMetadataTitle("Customer Statement of Account");
        exporter.setConfiguration(configuration);
        exporter.exportReport();
        return out.toByteArray();
    }

    private JasperReport template() throws JRException {
        JasperReport report = compiled;
        if (report == null) {
            synchronized (this) {
                report = compiled;
                if (report == null) {
                    try (InputStream in = new ClassPathResource(TEMPLATE).getInputStream()) {
                        report = JasperCompileManager.getInstance(fonts.context()).compile(in);
                    } catch (IOException io) {
                        throw new JRException("Statement template " + TEMPLATE + " is missing from the classpath", io);
                    }
                    compiled = report;
                    log.info("Compiled customer statement template {}", TEMPLATE);
                }
            }
        }
        return report;
    }

    private byte[] logo() {
        byte[] bytes = logoBytes;
        if (bytes == null) {
            try (InputStream in = new ClassPathResource(LOGO).getInputStream()) {
                bytes = in.readAllBytes();
            } catch (IOException missing) {
                log.warn("Statement logo {} not found on the classpath; printing without it", LOGO);
                bytes = new byte[0];
            }
            logoBytes = bytes;
        }
        return bytes;
    }
}
