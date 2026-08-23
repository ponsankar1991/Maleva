package my.maleva.api.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Serves stored attachments over the URL prefix their stored paths use.
 *
 * The legacy app got this for free: uploads lived under the IIS web root, so
 * {@code /Upload/6/SalesOrder/12056/a.jpg} was both the path on disk and the
 * URL. Storage sits outside the deployable here, so the mapping has to be
 * declared - without it the paths returned by the upload API resolve to
 * nothing, which is why the React app still points its previews at the old
 * .NET host.
 */
@Configuration
public class AttachmentResourceConfig implements WebMvcConfigurer {

    private final FileUploadConfig fileUploadConfig;

    public AttachmentResourceConfig(FileUploadConfig fileUploadConfig) {
        this.fileUploadConfig = fileUploadConfig;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String urlPattern = normalizePrefix(fileUploadConfig.getPublicUrlPrefix()) + "/**";
        String location = fileUploadConfig.getStorageRoot().toUri().toString();

        registry.addResourceHandler(urlPattern)
                .addResourceLocations(location)
                .setCachePeriod(3600);
    }

    private String normalizePrefix(String prefix) {
        String value = (prefix == null || prefix.isBlank()) ? "/uploads" : prefix.trim();
        if (!value.startsWith("/")) {
            value = "/" + value;
        }
        while (value.length() > 1 && value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }
}
