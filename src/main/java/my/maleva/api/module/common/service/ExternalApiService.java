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

/**
 * ExternalApiService - Integration with external APIs (QNE, E-Invoice, etc.)
 * Replaces .NET QneApi and EInvoiceApi methods from commonfunctions class
 */
@Service
public class ExternalApiService {

    private static final Logger logger = LoggerFactory.getLogger(ExternalApiService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${qne.api.dbcode:}")
    private String qneDbCode;

    @Value("${einvoice.api.url:}")
    private String einvoiceApiUrl;

    @Value("${einvoice.api.timeout:30}")
    private int apiTimeout;

    public ExternalApiService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Call QNE API with GET/POST/PUT support
     */
    public ApiResponse callQneApi(String url, int requestType, Object requestData) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("DbCode", qneDbCode);
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
            logger.error("Error calling QNE API", ex);
            return ApiResponse.error(ex.getMessage());
        }
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

