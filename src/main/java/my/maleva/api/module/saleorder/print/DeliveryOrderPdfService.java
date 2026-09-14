package my.maleva.api.module.saleorder.print;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.common.config.InvoicePrintProperties;
import my.maleva.api.module.saleorder.dto.DoConvertResult;
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
import java.util.List;
import java.util.Map;

/**
 * Renders a prepared delivery order as a PDF.
 *
 * <p>Replaces the Crystal {@code CRDoReport.rpt} that the legacy Sale Invoice
 * view opened through {@code ReportViewer.aspx?ReportName=DoReport} after
 * {@code /SaleOrder/DoConvert}. That path parked the rows in an ASP.NET session
 * for the viewer to pick up; here the rows {@link DoConvertResult} already
 * carries are filled straight into {@code reports/delivery-order.jrxml}.
 *
 * <p>The letterhead is the invoice's ({@code invoice-print.header-lines}), the
 * same six lines ReportViewer passed to the DO report. The heading, terms and
 * signature captions are {@code delivery-order-print.*}. The template is
 * compiled once, on first use, and cached for the life of the process.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryOrderPdfService {

    static final String TEMPLATE = "reports/delivery-order.jrxml";

    /** The MALEVA wordmark the legacy DO printed, at print resolution. */
    static final String LOGO = "reports/logo-wordmark.png";

    private final InvoicePrintProperties letterhead;
    private final DeliveryOrderPrintProperties text;
    private volatile JasperReport compiled;
    private volatile byte[] logoBytes;

    /**
     * The DO as PDF bytes.
     *
     * @param prepared a successful {@link DoConvertResult} with at least one row
     * @throws IllegalArgumentException when the DO was not prepared or has no rows
     * @throws IllegalStateException    when the template cannot be filled
     */
    public RenderedDeliveryOrder render(DoConvertResult prepared) {
        if (prepared == null || !prepared.ok() || prepared.rows() == null || prepared.rows().isEmpty()) {
            throw new IllegalArgumentException("A delivery order must be prepared before it can be printed");
        }
        List<DoConvertResult.DoView> rows = prepared.rows();
        DoConvertResult.DoView first = rows.get(0);
        String doNo = firstNonBlank(first.getDoNo(), prepared.doNo(), "DO-" + prepared.doId());

        // Only lines with something to say go in the description table; the
        // header facts are the DO's, read from its first row as Crystal did.
        List<DoConvertResult.DoView> described = rows.stream()
                .filter(row -> row.getDoDescription() != null && !row.getDoDescription().isBlank())
                .toList();

        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("LOGO", new ByteArrayInputStream(logo()));
            parameters.put("HEADER_LINES", letterhead.getHeaderLines());
            parameters.put("HEADING", text.getHeading());
            parameters.put("JOB_NO", trim(first.getJobNo()));
            parameters.put("JOB_TYPE", trim(first.getJobName()));
            parameters.put("LOADING_VESSEL", trim(first.getLoadingVesselName()));
            parameters.put("OFF_VESSEL", trim(first.getOffVesselName()));
            parameters.put("DO_NO", doNo);
            parameters.put("DO_DATE", trim(first.getSaleDate()));
            parameters.put("CUSTOMER", trim(first.getCustomerName()));
            parameters.put("ADDRESS", trim(first.getAddress()));
            parameters.put("ATTN", trim(first.getAttnName()));
            parameters.put("AWB_NO", trim(first.getAwbNo()));
            parameters.put("BL_COPY", trim(first.getBlCopy()));
            parameters.put("PACKAGES", trim(first.getQuantity()));
            parameters.put("WEIGHT", trim(first.getTotalWeight()));
            parameters.put("CAPTAIN_EMAIL_LABEL", text.getCaptainEmailLabel());
            parameters.put("DELIVERY_TERM_LABEL", text.getDeliveryTermLabel());
            parameters.put("TERMS_TITLE", text.getTermsTitle());
            parameters.put("TERMS_TEXT", termsText(text.getTerms()));
            parameters.put("ISSUER_SIGNATURE_LABEL", text.getIssuerSignatureLabel());
            parameters.put("RECIPIENT_SIGNATURE_LABEL", text.getRecipientSignatureLabel());

            JasperPrint print = JasperFillManager.fillReport(template(), parameters, new JRBeanCollectionDataSource(described));
            byte[] pdf = JasperExportManager.exportReportToPdf(print);
            return new RenderedDeliveryOrder(fileNameFor(doNo), pdf);
        } catch (JRException ex) {
            throw new IllegalStateException("Delivery order " + doNo + " could not be rendered", ex);
        }
    }

    /** The terms as one block: each paragraph on its own, a blank line between, as Crystal spaced them. */
    static String termsText(List<String> terms) {
        if (terms == null) {
            return "";
        }
        return String.join("\n\n", terms.stream().filter(t -> t != null && !t.isBlank()).map(String::strip).toList());
    }

    /** A download name the browser can use as-is, e.g. {@code DO000016139.pdf}. */
    static String fileNameFor(String doNo) {
        String safe = doNo == null ? "" : doNo.trim().replaceAll("[^A-Za-z0-9._-]", "_");
        return (safe.isEmpty() ? "delivery-order" : safe) + ".pdf";
    }

    private static String trim(String value) {
        return value == null ? "" : value.strip();
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
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
                        throw new JRException("Delivery order template " + TEMPLATE + " is missing from the classpath", io);
                    }
                    compiled = report;
                    log.info("Compiled delivery order print template {}", TEMPLATE);
                }
            }
        }
        return report;
    }

    /** The wordmark, read once from the classpath; an empty array if it is missing. */
    private byte[] logo() {
        byte[] bytes = logoBytes;
        if (bytes == null) {
            try (InputStream in = new ClassPathResource(LOGO).getInputStream()) {
                bytes = in.readAllBytes();
            } catch (IOException missing) {
                log.warn("Delivery order logo {} not found on the classpath; printing without it", LOGO);
                bytes = new byte[0];
            }
            logoBytes = bytes;
        }
        return bytes;
    }

    /** A rendered PDF and the file name to offer it under. */
    public record RenderedDeliveryOrder(String fileName, byte[] pdf) {
    }
}
