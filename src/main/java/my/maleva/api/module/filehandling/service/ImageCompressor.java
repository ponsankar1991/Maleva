package my.maleva.api.module.filehandling.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/** Downscales and re-encodes an uploaded image before it is stored. */
public interface ImageCompressor {

    /** True when {@code extension} (leading dot, any case) is a raster image we re-encode. */
    boolean supports(String extension);

    /**
     * Reads {@code source} and writes the compressed result to {@code target}.
     *
     * @throws IOException if the stream does not decode as an image, or the write fails
     */
    void compressTo(InputStream source, Path target) throws IOException;
}
