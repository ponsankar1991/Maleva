package my.maleva.api.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Attachment storage settings, bound from {@code file.upload.*}.
 *
 * The legacy .NET app kept uploads inside the web root and addressed them by
 * IIS virtual path, so the storage location and the public URL were the same
 * string. They are separate here: {@link #getUploadDir()} is a filesystem path
 * that can live outside the deployable, and {@link #getPublicUrlPrefix()} is
 * the URL that maps onto it.
 */
@Configuration
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadConfig {

    /**
     * Defaults are the legacy names, so a Spring upload lands in the same
     * {@code Upload/<company>/<folder>/<id>/} tree the .NET screens wrote and
     * is served on the same {@code /Upload/...} path they stored in
     * {@code FilePath} columns. Set {@code FILE_UPLOAD_DIR} to the IIS folder in
     * production so both applications read one disk.
     */
    private String uploadDir = "Upload";
    private String publicUrlPrefix = "/Upload";
    /**
     * Per-file cap. 25 MB: the legacy IIS host accepted roughly 30 MB per
     * request, and scanned supplier documents regularly land just over the
     * old 10 MB Spring default, which rejected a file that IIS had taken.
     */
    private Long maxFileSize = 26214400L;

    /** Per-request cap across every part; must hold {@link #maxFiles} files of {@link #maxFileSize}. */
    private Long maxRequestSize = 104857600L;

    private Integer maxFiles = 10;

    /** Longest edge, in pixels, an image is downscaled to before storage. */
    private Integer imageMaxDimension = 900;

    /** JPEG encoder quality, 0..1. The legacy compressor used 50%. */
    private Float jpegQuality = 0.5f;

    /** Render resolution used when a PDF page becomes an image. */
    private Integer pdfRenderDpi = 300;

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    /** {@link #getUploadDir()} as an absolute, normalized path. */
    public Path getStorageRoot() {
        return Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public String getPublicUrlPrefix() {
        return publicUrlPrefix;
    }

    public void setPublicUrlPrefix(String publicUrlPrefix) {
        this.publicUrlPrefix = publicUrlPrefix;
    }

    public Long getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(Long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public Long getMaxRequestSize() {
        return maxRequestSize;
    }

    public void setMaxRequestSize(Long maxRequestSize) {
        this.maxRequestSize = maxRequestSize;
    }

    public Integer getMaxFiles() {
        return maxFiles;
    }

    public void setMaxFiles(Integer maxFiles) {
        this.maxFiles = maxFiles;
    }

    public Integer getImageMaxDimension() {
        return imageMaxDimension;
    }

    public void setImageMaxDimension(Integer imageMaxDimension) {
        this.imageMaxDimension = imageMaxDimension;
    }

    public Float getJpegQuality() {
        return jpegQuality;
    }

    public void setJpegQuality(Float jpegQuality) {
        this.jpegQuality = jpegQuality;
    }

    public Integer getPdfRenderDpi() {
        return pdfRenderDpi;
    }

    public void setPdfRenderDpi(Integer pdfRenderDpi) {
        this.pdfRenderDpi = pdfRenderDpi;
    }
}
