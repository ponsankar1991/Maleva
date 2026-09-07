package my.maleva.api.module.salecreditmaster.print;

import lombok.RequiredArgsConstructor;
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
 * Renders the printed credit note as a PDF.
 *
 * <p>Replaces the Crystal {@code CreditNote} report the legacy screen opened
 * through {@code ReportViewer.aspx}. That path needed the rows parked in an
 * ASP.NET session by the same server moments earlier; a React app on another
 * origin has no such session, so the credit note is now a plain HTTP
 * resource: load, fill the template, return bytes.
 *
 * <p>The template ({@code reports/credit-note.jrxml}) is compiled once, on
 * first use, and cached for the life of the process.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SaleCreditPdfService {

    static final String TEMPLATE = "reports/credit-note.jrxml";
    static final String LOGO = "reports/logo.png";

    private final CreditNotePrintSnapshotLoader loader;
    private final CreditNotePrintEInvoiceBackfill eInvoiceBackfill;
    private volatile JasperReport compiled;
    private volatile byte[] logoBytes;

    /**
     * The credit note as PDF bytes, or empty when it does not exist for the
     * company. As the legacy print did, a note already with LHDN whose status,
     * long id or validated time is still unknown gets those read and saved
     * first, so the paper carries the status and the QR.
     */
    public Optional<RenderedCreditNote> render(Integer creditNoteId, Integer companyId) {
        eInvoiceBackfill.ensureStatusKnown(creditNoteId, companyId);
        Optional<CreditNotePrintSnapshot> loaded = loader.load(creditNoteId, companyId);
        if (loaded.isEmpty()) {
            return Optional.empty();
        }
        CreditNotePrintSnapshot snapshot = loaded.get();
        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("SNAPSHOT", snapshot);
            parameters.put("QR_IMAGE", snapshot.getQrPng() == null ? null : new ByteArrayInputStream(snapshot.getQrPng()));
            parameters.put("LOGO", new ByteArrayInputStream(logo()));

            JasperPrint print = JasperFillManager.fillReport(
                    template(), parameters, new JRBeanCollectionDataSource(snapshot.getLines()));
            byte[] pdf = JasperExportManager.exportReportToPdf(print);

            String fileName = (snapshot.getCreditNoteNo() == null || snapshot.getCreditNoteNo().isBlank()
                    ? "credit-note-" + creditNoteId : snapshot.getCreditNoteNo().trim()) + ".pdf";
            return Optional.of(new RenderedCreditNote(fileName, pdf));
        } catch (JRException ex) {
            throw new IllegalStateException("Credit note " + snapshot.getCreditNoteNo() + " could not be rendered", ex);
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
                        throw new JRException("Credit note template " + TEMPLATE + " is missing from the classpath", io);
                    }
                    compiled = report;
                    log.info("Compiled credit note print template {}", TEMPLATE);
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
                log.warn("Credit note logo {} not found on the classpath; printing without it", LOGO);
                bytes = new byte[0];
            }
            logoBytes = bytes;
        }
        return bytes;
    }

    /** A rendered PDF and the file name to offer it under. */
    public record RenderedCreditNote(String fileName, byte[] pdf) {
    }
}
