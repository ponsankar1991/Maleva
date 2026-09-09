package my.maleva.api.module.invoice.print;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Renders the printed sale invoice as a PDF.
 *
 * <p>Replaces the Crystal report the legacy screen opened through
 * {@code ReportViewer.aspx}. That path needed the rows parked in an ASP.NET
 * session by the same server moments earlier; a React app on another origin
 * has no such session, so the invoice is now a plain HTTP resource: load,
 * fill the template, return bytes.
 *
 * <p>The template ({@code reports/sale-invoice.jrxml}) is compiled once, on
 * first use, and cached for the life of the process. Compilation needs the
 * ecj compiler on the classpath, which the JasperReports dependency brings.
 */
@Slf4j
@Service
public class SaleInvoicePdfService {

    static final String TEMPLATE = "reports/sale-invoice.jrxml";
    static final String LOGO = "reports/logo.png";

    private final InvoicePrintSnapshotLoader loader;
    private final InvoicePrintEInvoiceBackfill eInvoiceBackfill;
    private volatile JasperReport compiled;
    private volatile byte[] logoBytes;

    public SaleInvoicePdfService(InvoicePrintSnapshotLoader loader, InvoicePrintEInvoiceBackfill eInvoiceBackfill) {
        this.loader = loader;
        this.eInvoiceBackfill = eInvoiceBackfill;
    }

    /**
     * The invoice as PDF bytes, or empty when it does not exist for the company.
     *
     * <p>As the legacy print did, an invoice already with LHDN whose status,
     * long id or validated time is still unknown gets those read and saved
     * first, so the paper carries the status and the QR.
     */
    public Optional<RenderedInvoice> render(Integer invoiceId, Integer companyId) {
        // Started, not waited for. This is a call to LHDN with a two-minute
        // timeout, and the PDF does not need it: it prints what is stored, and
        // an invoice that has been printed once already has it. Waiting for a
        // government API before rendering was what made opening the report
        // slow. When a status really was missing, the read lands moments later
        // and the next print carries the status and the QR.
        eInvoiceBackfill.refreshInBackground(invoiceId, companyId);

        // Timed in three parts. "The report is slow" needs a number to act on,
        // and the parts move independently: the reads are database time, the
        // fill and export are ours, and the first render of a process also
        // pays for compiling the template.
        long startedAt = System.nanoTime();
        Optional<InvoicePrintSnapshot> loaded = loader.load(invoiceId, companyId);
        long loadMillis = millisSince(startedAt);
        if (loaded.isEmpty()) {
            return Optional.empty();
        }
        InvoicePrintSnapshot snapshot = loaded.get();
        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("SNAPSHOT", snapshot);
            parameters.put("QR_IMAGE", snapshot.getQrPng() == null ? null : new ByteArrayInputStream(snapshot.getQrPng()));
            parameters.put("LOGO", new ByteArrayInputStream(logo()));

            long fillStartedAt = System.nanoTime();
            JasperPrint print = JasperFillManager.fillReport(
                    template(), parameters, new JRBeanCollectionDataSource(snapshot.getLines()));
            long fillMillis = millisSince(fillStartedAt);

            long exportStartedAt = System.nanoTime();
            byte[] pdf = JasperExportManager.exportReportToPdf(print);
            long exportMillis = millisSince(exportStartedAt);

            long totalMillis = millisSince(startedAt);
            if (totalMillis >= SLOW_RENDER_MILLIS) {
                log.warn("Invoice {} rendered in {} ms (read {} ms, fill {} ms, export {} ms, {} KB)",
                        snapshot.getInvoiceNo(), totalMillis, loadMillis, fillMillis, exportMillis, pdf.length / 1024);
            } else {
                log.info("Invoice {} rendered in {} ms (read {} ms, fill {} ms, export {} ms, {} KB)",
                        snapshot.getInvoiceNo(), totalMillis, loadMillis, fillMillis, exportMillis, pdf.length / 1024);
            }

            String fileName = (snapshot.getInvoiceNo() == null || snapshot.getInvoiceNo().isBlank()
                    ? "invoice-" + invoiceId : snapshot.getInvoiceNo().trim()) + ".pdf";
            return Optional.of(new RenderedInvoice(fileName, pdf));
        } catch (JRException ex) {
            throw new IllegalStateException("Invoice " + snapshot.getInvoiceNo() + " could not be rendered", ex);
        }
    }

    /**
     * Compiles the template once the application is up, off the startup
     * thread, so the first operator to press Print does not pay for it.
     *
     * <p>Compiling the .jrxml takes seconds — it runs a Java compiler over the
     * report's expressions. Cached for the life of the process either way;
     * this only decides whether the cost lands on a user or on an idle
     * background thread just after boot.
     */
    @org.springframework.context.event.EventListener(
            org.springframework.boot.context.event.ApplicationReadyEvent.class)
    void warmTemplate() {
        Thread warmUp = new Thread(() -> {
            try {
                template();
            } catch (JRException ex) {
                // Not fatal: the next print compiles it and reports properly.
                log.warn("Could not pre-compile the invoice template; the first print will compile it", ex);
            }
        }, "invoice-template-warmup");
        warmUp.setDaemon(true);
        warmUp.start();
    }

    private JasperReport template() throws JRException {
        JasperReport report = compiled;
        if (report == null) {
            synchronized (this) {
                report = compiled;
                if (report == null) {
                    try (InputStream in = new ClassPathResource(TEMPLATE).getInputStream()) {
                        report = JasperCompileManager.compileReport(in);
                    } catch (IOException io) {
                        throw new JRException("Invoice template " + TEMPLATE + " is missing from the classpath", io);
                    }
                    compiled = report;
                    log.info("Compiled invoice print template {}", TEMPLATE);
                }
            }
        }
        return report;
    }

    /** The company logo, read once from the classpath; an empty array if it is missing. */
    private byte[] logo() {
        byte[] bytes = logoBytes;
        if (bytes == null) {
            try (InputStream in = new ClassPathResource(LOGO).getInputStream()) {
                bytes = in.readAllBytes();
            } catch (IOException missing) {
                log.warn("Invoice logo {} not found on the classpath; printing without it", LOGO);
                bytes = new byte[0];
            }
            logoBytes = bytes;
        }
        return bytes;
    }

    /** Above this a render is worth complaining about in the log. */
    private static final long SLOW_RENDER_MILLIS = 2_000;

    private static long millisSince(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }

    /** A rendered PDF and the file name to offer it under. */
    public record RenderedInvoice(String fileName, byte[] pdf) {
    }
}
