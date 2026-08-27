package my.maleva.api.module.common.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import my.maleva.api.integration.qne.QneClient;
import my.maleva.api.integration.qne.QneResult;

/**
 * ExternalApiService - Integration with external APIs (QNE, E-Invoice, etc.)
 * Replaces .NET QneApi and EInvoiceApi methods from commonfunctions class
 */
@Service
public class ExternalApiService {

    private static final Logger logger = LoggerFactory.getLogger(ExternalApiService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final QneClient qneClient;

    @Value("${einvoice.api.url:}")
    private String einvoiceApiUrl;

    @Value("${einvoice.api.timeout:30}")
    private int apiTimeout;

    public ExternalApiService(RestTemplate restTemplate, ObjectMapper objectMapper, QneClient qneClient) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.qneClient = qneClient;
    }

    /**
     * Call QNE API with GET/POST/PUT support.
     *
     * <p>Delegates to {@link QneClient}. The previous inline implementation
     * here read the {@code DbCode} header from a config key that did not
     * exist, so every request went out with an empty tenant code — and
     * {@code RestTemplate.exchange} throws on 4xx, so the error branch that
     * pretended to pass QNE's message through was unreachable. Both are the
     * client's job now.
     *
     * @deprecated new code should use {@link my.maleva.api.integration.qne.QneGateway}
     *             for typed operations, or {@link QneClient} directly.
     */
    @Deprecated
    public ApiResponse callQneApi(String url, int requestType, Object requestData) {
        QneResult result = switch (requestType) {
            case 1 -> qneClient.get(url);
            case 2 -> qneClient.post(url, requestData);
            case 3 -> qneClient.put(url, requestData);
            default -> null;
        };
        if (result == null) {
            return ApiResponse.error("Unknown request type: " + requestType);
        }
        return result.success()
                ? ApiResponse.success(result.message())
                : ApiResponse.error(result.message());
    }

    /**
     * Call E-Invoice API with authentication
     */
    public ApiResponse callEInvoiceApi(String url, int requestType, Object requestData,
                                      String bearerToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + bearerToken);
            headers.set("Accept", "application/json");
            headers.set("Content-Type", "application/json");

            ResponseEntity<String> response = null;

            switch (requestType) {
                case 1: // GET
                    response = restTemplate.exchange(url, HttpMethod.GET,
                            new HttpEntity<>(headers), String.class);
                    break;

                case 2: // POST
                    String postData = objectMapper.writeValueAsString(requestData);
                    HttpEntity<String> postEntity = new HttpEntity<>(postData, headers);
                    response = restTemplate.exchange(url, HttpMethod.POST, postEntity, String.class);
                    break;

                case 3: // PUT
                    String putData = objectMapper.writeValueAsString(requestData);
                    HttpEntity<String> putEntity = new HttpEntity<>(putData, headers);
                    response = restTemplate.exchange(url, HttpMethod.PUT, putEntity, String.class);
                    break;

                default:
                    return ApiResponse.error("Unknown request type: " + requestType);
            }

            if (response != null && response.getStatusCode().is2xxSuccessful()) {
                return ApiResponse.success(response.getBody());
            } else {
                return ApiResponse.error(response != null ? response.getBody() : "API call failed");
            }

        } catch (Exception ex) {
            logger.error("Error calling E-Invoice API", ex);
            return ApiResponse.error(ex.getMessage());
        }
    }

    /**
     * Simple API Response wrapper
     */
    public static class ApiResponse {
        private boolean isSuccess;
        private String message;

        public ApiResponse(boolean isSuccess, String message) {
            this.isSuccess = isSuccess;
            this.message = message;
        }

        public static ApiResponse success(String message) {
            return new ApiResponse(true, message);
        }

        public static ApiResponse error(String message) {
            return new ApiResponse(false, message);
        }

        public boolean isSuccess() {
            return isSuccess;
        }

        public String getMessage() {
            return message;
        }
    }
}

