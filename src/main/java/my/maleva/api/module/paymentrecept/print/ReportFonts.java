package my.maleva.api.module.paymentrecept.print;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.SimpleJasperReportsContext;
import net.sf.jasperreports.engine.fonts.FontFamily;
import net.sf.jasperreports.engine.fonts.SimpleFontFace;
import net.sf.jasperreports.engine.fonts.SimpleFontFamily;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * The typeface of the printed vouchers.
 *
 * <p>Crystal set every receipt in <b>Verdana</b> (the fonts embedded in a
 * legacy {@code crReport.pdf} are Verdana subsets), and the customer copy has
 * always looked that way. Verdana ships with Windows but may not be
 * redistributed, so it is picked up from the operating system when the server
 * has it and otherwise replaced by DejaVu Sans from the
 * {@code jasperreports-fonts} jar — a free face drawn to the same proportions,
 * so the layout measured against the Crystal output still fits.
 *
 * <p>Templates reference the family as {@code fontName="Verdana"}; the faces
 * are embedded in the PDF, so the document renders identically wherever it is
 * opened.
 */
@Slf4j
@Component
public class ReportFonts {

    static final String FAMILY = "Verdana";
    private static final Path WINDOWS_FONTS = Path.of(System.getenv().getOrDefault("WINDIR", "C:\\Windows"), "Fonts");
    private static final String DEJAVU = "net/sf/jasperreports/fonts/dejavu/";

    private final JasperReportsContext context;

    public ReportFonts() {
        SimpleJasperReportsContext ctx = new SimpleJasperReportsContext(DefaultJasperReportsContext.getInstance());
        ctx.setExtensions(FontFamily.class, List.of(family()));
        this.context = ctx;
    }

    public JasperReportsContext context() {
        return context;
    }

    private static SimpleFontFamily family() {
        SimpleFontFamily family = new SimpleFontFamily();
        family.setName(FAMILY);
        family.setPdfEmbedded(true);
        family.setPdfEncoding("Identity-H");

        Path normal = WINDOWS_FONTS.resolve("verdana.ttf");
        if (Files.isRegularFile(normal)) {
            family.setNormalFace(face(normal.toString()));
            family.setBoldFace(face(WINDOWS_FONTS.resolve("verdanab.ttf").toString()));
            family.setItalicFace(face(WINDOWS_FONTS.resolve("verdanai.ttf").toString()));
            family.setBoldItalicFace(face(WINDOWS_FONTS.resolve("verdanaz.ttf").toString()));
            log.info("Report font '{}' from {}", FAMILY, WINDOWS_FONTS);
        } else {
            family.setNormalFace(face(DEJAVU + "DejaVuSans.ttf"));
            family.setBoldFace(face(DEJAVU + "DejaVuSans-Bold.ttf"));
            family.setItalicFace(face(DEJAVU + "DejaVuSans-Oblique.ttf"));
            family.setBoldItalicFace(face(DEJAVU + "DejaVuSans-BoldOblique.ttf"));
            log.info("Report font '{}' not installed on this server; embedding DejaVu Sans instead", FAMILY);
        }
        return family;
    }

    private static SimpleFontFace face(String ttf) {
        SimpleFontFace face = new SimpleFontFace(DefaultJasperReportsContext.getInstance());
        face.setTtf(ttf);
        return face;
    }
}
