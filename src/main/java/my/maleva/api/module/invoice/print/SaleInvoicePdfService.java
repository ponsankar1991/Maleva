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
        eInvoiceBackfill.ensureStatusKnown(invoiceId, companyId);
        Optional<InvoicePrintSnapshot> loaded = loader.load(invoiceId, companyId);
        if (loaded.isEmpty()) {
            return Optional.empty();
        }
        InvoicePrintSnapshot snapshot = loaded.get();
        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("SNAPSHOT", snapshot);
            parameters.put("QR_IMAGE", snapshot.getQrPng() == null ? null : new ByteArrayInputStream(snapshot.getQrPng()));
            parameters.put("LOGO", new ByteArrayInputStream(logo()));

            JasperPrint print = JasperFillManager.fillReport(
                    template(), parameters, new JRBeanCollectionDataSource(snapshot.getLines()));
            byte[] pdf = JasperExportManager.exportReportToPdf(print);

            String fileName = (snapshot.getInvoiceNo() == null || snapshot.getInvoiceNo().isBlank()
                    ? "invoice-" + invoiceId : snapshot.getInvoiceNo().trim()) + ".pdf";
            return Optional.of(new RenderedInvoice(fileName, pdf));
        } catch (JRException ex) {
            throw new IllegalStateException("Invoice " + snapshot.getInvoiceNo() + " could not be rendered", ex);
        }
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

    /** A rendered PDF and the file name to offer it under. */
    public record RenderedInvoice(String fileName, byte[] pdf) {
    }
}
