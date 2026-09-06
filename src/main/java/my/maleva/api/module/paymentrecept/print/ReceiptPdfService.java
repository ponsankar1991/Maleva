package my.maleva.api.module.paymentrecept.print;

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
 * Renders the receipt voucher as a PDF.
 *
 * <p>Replaces the Crystal {@code CRReceipt2.rpt} that the legacy screen
 * reached two ways: {@code ReportViewer.aspx?ReportName=ReceiptReport} for
 * the EXPORT button and {@code ReceiptExportReport()} writing a file under
 * {@code /Pdf} for the mail. Both needed the rows parked in an ASP.NET
 * session moments earlier and the second raced when two users exported at
 * once. Here the voucher is a plain HTTP resource rendered in memory: the
 * screen previews the very bytes that the mail attaches.
 *
 * <p>The template ({@code reports/receipt-voucher.jrxml}) is compiled once,
 * on first use, and cached for the life of the process.
 */
@Slf4j
@Service
public class ReceiptPdfService {

    static final String TEMPLATE = "reports/receipt-voucher.jrxml";
    static final String LOGO = "reports/logo.png";

    private final ReceiptPrintSnapshotLoader loader;
    private volatile JasperReport compiled;
    private volatile byte[] logoBytes;

    public ReceiptPdfService(ReceiptPrintSnapshotLoader loader) {
        this.loader = loader;
    }

    /** The voucher as PDF bytes, or empty when the receipt does not exist for the company. */
    public Optional<RenderedReceipt> render(Integer receiptId, Integer companyId) {
        Optional<ReceiptPrintSnapshot> loaded = loader.load(receiptId, companyId);
        if (loaded.isEmpty()) {
            return Optional.empty();
        }
        ReceiptPrintSnapshot snapshot = loaded.get();
        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("SNAPSHOT", snapshot);
            parameters.put("LOGO", new ByteArrayInputStream(logo()));

            JasperPrint print = JasperFillManager.fillReport(
                    template(), parameters, new JRBeanCollectionDataSource(snapshot.getLines()));
            byte[] pdf = JasperExportManager.exportReportToPdf(print);
            return Optional.of(new RenderedReceipt(fileName(snapshot.getReceiptNo(), receiptId), pdf));
        } catch (JRException ex) {
            throw new IllegalStateException("Receipt " + snapshot.getReceiptNo() + " could not be rendered", ex);
        }
    }

    /**
     * {@code Receipt<number>.pdf}, the legacy export name — CNumberDisplay can
     * hold characters that are not valid in a file name, so only letters and
     * digits survive, as {@code ReceiptExportReport} did.
     */
    static String fileName(String receiptNo, Integer receiptId) {
        StringBuilder name = new StringBuilder("Receipt");
        if (receiptNo != null) {
            receiptNo.chars().filter(Character::isLetterOrDigit).forEach(c -> name.append((char) c));
        }
        if (name.length() == "Receipt".length()) {
            name.append(receiptId);
        }
        return name.append(".pdf").toString();
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
                        throw new JRException("Receipt template " + TEMPLATE + " is missing from the classpath", io);
                    }
                    compiled = report;
                    log.info("Compiled receipt print template {}", TEMPLATE);
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
                log.warn("Receipt logo {} not found on the classpath; printing without it", LOGO);
                bytes = new byte[0];
            }
            logoBytes = bytes;
        }
        return bytes;
    }

    /** A rendered PDF and the file name to offer it under. */
    public record RenderedReceipt(String fileName, byte[] pdf) {
    }
}
