package my.maleva.api.module.filehandling.model;

import my.maleva.api.common.exception.InvalidRequestException;

import java.util.Locale;

/**
 * How an incoming file is turned into stored bytes.
 *
 * The legacy .NET controller expressed this as four near-identical actions -
 * {@code UploadFile}, {@code UploadFile2}, {@code UploadFile3} and
 * {@code UploadFile5} - that differed only in which branch of the extension
 * check they carried. The difference is a strategy, not an endpoint, so it is
 * one parameter here and the numbered names survive only in
 * {@link #fromLegacyAction(String)} for the screens still calling them.
 */
public enum AttachmentUploadMode {

    /**
     * Every file goes through the image compressor. Matches legacy
     * {@code UploadFile}: a non-image sent here fails to decode, which is why
     * later revisions of the legacy action grew the extension check below.
     */
    IMAGES_ONLY,

    /**
     * Images are compressed, everything else is stored byte-for-byte. Matches
     * legacy {@code UploadFile2} and {@code UploadFile3}, and is the right
     * default for a general document attachment.
     */
    MIXED,

    /**
     * Like {@link #MIXED}, except a PDF is rasterised to one JPEG per page and
     * the PDF itself is discarded. Matches legacy {@code UploadFile5}, which
     * existed so the approval screens could show page thumbnails inline.
     */
    PDF_AS_IMAGES;

    /**
     * Maps a legacy action name onto the mode that reproduces its behaviour.
     * Used only by the backwards-compatible endpoints.
     */
    public static AttachmentUploadMode fromLegacyAction(String actionName) {
        String action = actionName == null ? "" : actionName.trim().toLowerCase(Locale.ROOT);
        return switch (action) {
            case "uploadfile" -> IMAGES_ONLY;
            case "uploadfile2", "uploadfile3" -> MIXED;
            case "uploadfile5" -> PDF_AS_IMAGES;
            default -> throw new InvalidRequestException("Unknown legacy upload action: " + actionName);
        };
    }

    /** Parses the {@code mode} request parameter, defaulting to {@link #MIXED}. */
    public static AttachmentUploadMode parse(String value) {
        if (value == null || value.isBlank()) {
            return MIXED;
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new InvalidRequestException("Unknown upload mode: " + value);
        }
    }
}
