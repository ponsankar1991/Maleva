package my.maleva.api.module.paymentrecept.print;

import lombok.extern.slf4j.Slf4j;
import my.maleva.api.module.paymentrecept.entity.Receipt;
import my.maleva.api.module.paymentrecept.repository.ReceiptRepository;
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
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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
 * <p>Speed: the template is compiled and the logo read once, at startup,
 * so the first EXPORT of the day does not pay the two-second compile; the
 * PDF streams are Flate-compressed; and a rendered voucher is kept for a few
 * minutes keyed on the receipt's {@code Modified_Date}, so the mail window's
 * preview and the SEND that follows it render once, not twice. A save bumps
 * the date and so bypasses the cached copy.
 */
@Slf4j
@Service
public class ReceiptPdfService {

    static final String TEMPLATE = "reports/receipt-voucher.jrxml";
    static final String LOGO = "reports/logo.png";
    static final Duration CACHE_TTL = Duration.ofMinutes(5);
    static final int CACHE_MAX = 200;

    private final ReceiptPrintSnapshotLoader loader;
    private final ReceiptRepository receipts;
    private final ReportFonts fonts;
    private volatile JasperReport compiled;
    private volatile byte[] logoBytes;
    private final ConcurrentHashMap<String, CachedRender> cache = new ConcurrentHashMap<>();

    public ReceiptPdfService(ReceiptPrintSnapshotLoader loader, ReceiptRepository receipts, ReportFonts fonts) {
        this.loader = loader;
        this.receipts = receipts;
        this.fonts = fonts;
    }

    /** Compiles the template and reads the logo off the request path, once the app is up. */
    @EventListener(ApplicationReadyEvent.class)
    public void warmUp() {
        Thread warm = new Thread(() -> {
            try {
                long started = System.nanoTime();
                template();
                logo();
                log.info("Receipt print template warmed up in {} ms", (System.nanoTime() - started) / 1_000_000);
            } catch (Exception ex) {
                log.warn("Receipt print template warm-up failed (it will compile on first use): {}", ex.getMessage());
            }
        }, "receipt-print-warmup");
        warm.setDaemon(true);
        warm.start();
    }

    /** The voucher as PDF bytes, or empty when the receipt does not exist for the company. */
    public Optional<RenderedReceipt> render(Integer receiptId, Integer companyId) {
        Receipt receipt = receipts.findById(receiptId).orElse(null);
        if (receipt == null || !Objects.equals(receipt.getCompanyRefId(), companyId)) {
            return Optional.empty();
        }
        String key = companyId + ":" + receiptId;
        LocalDateTime stamp = receipt.getModifiedDate();
        CachedRender hit = cache.get(key);
        if (hit != null && hit.isFresh(stamp)) {
            return Optional.of(hit.rendered());
        }

        Optional<ReceiptPrintSnapshot> loaded = loader.load(receiptId, companyId);
        if (loaded.isEmpty()) {
            return Optional.empty();
        }
        ReceiptPrintSnapshot snapshot = loaded.get();
        try {
            long started = System.nanoTime();
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("SNAPSHOT", snapshot);
            parameters.put("LOGO", new ByteArrayInputStream(logo()));

            JasperPrint print = JasperFillManager.getInstance(fonts.context()).fill(
                    template(), parameters, new JRBeanCollectionDataSource(snapshot.getLines()));
            byte[] pdf = toCompressedPdf(print);
            RenderedReceipt rendered = new RenderedReceipt(fileName(snapshot.getReceiptNo(), receiptId), pdf);
            remember(key, stamp, rendered);
            log.debug("Receipt {} rendered in {} ms ({} bytes)", snapshot.getReceiptNo(),
                    (System.nanoTime() - started) / 1_000_000, pdf.length);
            return Optional.of(rendered);
        } catch (JRException ex) {
            throw new IllegalStateException("Receipt " + snapshot.getReceiptNo() + " could not be rendered", ex);
        }
    }

    /** Drops any cached copy of one receipt (call after a save or delete). */
    public void evict(Integer receiptId, Integer companyId) {
        cache.remove(companyId + ":" + receiptId);
    }

    private byte[] toCompressedPdf(JasperPrint print) throws JRException {
        // the fonts context knows the Verdana faces to embed
        JRPdfExporter exporter = new JRPdfExporter(fonts.context());
        exporter.setExporterInput(new SimpleExporterInput(print));
        ByteArrayOutputStream out = new ByteArrayOutputStream(64 * 1024);
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
        SimplePdfExporterConfiguration configuration = new SimplePdfExporterConfiguration();
        configuration.setCompressed(true);
        configuration.setMetadataTitle("Receipt Voucher");
        exporter.setConfiguration(configuration);
        exporter.exportReport();
        return out.toByteArray();
    }

    private void remember(String key, LocalDateTime stamp, RenderedReceipt rendered) {
        if (cache.size() >= CACHE_MAX) {
            // the map is small and bounded; a full sweep of stale entries is cheaper than an LRU
            long now = System.nanoTime();
            cache.entrySet().removeIf(e -> e.getValue().expired(now));
            if (cache.size() >= CACHE_MAX) {
                cache.clear();
            }
        }
        cache.put(key, new CachedRender(stamp, System.nanoTime(), rendered));
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
                        report = JasperCompileManager.getInstance(fonts.context()).compile(in);
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

    private record CachedRender(LocalDateTime modifiedStamp, long renderedAtNanos, RenderedReceipt rendered) {
        boolean expired(long nowNanos) {
            return nowNanos - renderedAtNanos > CACHE_TTL.toNanos();
        }

        boolean isFresh(LocalDateTime currentStamp) {
            return !expired(System.nanoTime()) && Objects.equals(modifiedStamp, currentStamp);
        }
    }
}
