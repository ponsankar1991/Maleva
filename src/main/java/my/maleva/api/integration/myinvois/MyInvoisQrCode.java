package my.maleva.api.integration.myinvois;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * The QR printed on a validated e-invoice. It encodes the LHDN share link
 * ({@code <portal>/<uuid>/share/<longId>}) so anyone with the paper can open
 * the document on the MyInvois portal.
 *
 * <p>Rendering matches the legacy QRCoder call — error-correction level Q,
 * 20 pixels per module, the standard 4-module quiet zone, black on white,
 * PNG — so the printed size is unchanged.
 */
@Component
public class MyInvoisQrCode {

    static final int PIXELS_PER_MODULE = 20;
    static final int QUIET_ZONE_MODULES = 4;

    /**
     * PNG bytes for the given text.
     *
     * @throws IllegalStateException when the text cannot be encoded; callers
     *         that can live without the picture catch it
     */
    public byte[] png(String text) {
        try {
            Map<EncodeHintType, Object> hints = Map.of(
                    EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.Q,
                    EncodeHintType.MARGIN, QUIET_ZONE_MODULES,
                    EncodeHintType.CHARACTER_SET, "UTF-8");

            // Size 0,0 asks for the smallest matrix (modules + margin); the
            // second pass scales it so every module is PIXELS_PER_MODULE wide.
            BitMatrix minimal = new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, 0, 0, hints);
            int pixels = minimal.getWidth() * PIXELS_PER_MODULE;
            BitMatrix scaled = new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, pixels, pixels, hints);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(scaled, "PNG", out);
            return out.toByteArray();
        } catch (WriterException | IOException ex) {
            throw new IllegalStateException("QR code could not be rendered", ex);
        }
    }
}
