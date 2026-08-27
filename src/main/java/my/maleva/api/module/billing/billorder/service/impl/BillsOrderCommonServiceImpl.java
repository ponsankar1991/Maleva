package my.maleva.api.module.billing.billorder.service.impl;

import my.maleva.api.module.billing.billorder.service.BillsOrderCommonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Implementation of BillsOrderCommonService for WhatsApp, Email, and API integrations
 * Equivalent to .NET commonfunctions class for BillsOrderMaster operations
 *
 * Provides shared functionality for:
 * - WhatsApp message sending via external service
 * - Email notifications
 * - QNE API calls for external system integration
 */
@Service
public class BillsOrderCommonServiceImpl implements BillsOrderCommonService {

    private static final Logger logger = LoggerFactory.getLogger(BillsOrderCommonServiceImpl.class);

    private final RestTemplate restTemplate;

    @Value("${app.whatsapp.api.url:#{null}}")
    private String whatsAppApiUrl;

    @Value("${app.whatsapp.api.key:#{null}}")
    private String whatsAppApiKey;

    @Value("${app.email.api.url:#{null}}")
    private String emailApiUrl;

    private final my.maleva.api.integration.qne.QneClient qneClient;
    private final my.maleva.api.common.config.QneProperties qneProperties;

    @Value("${app.whatsapp.enabled:false}")
    private boolean whatsAppEnabled;

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    public BillsOrderCommonServiceImpl(RestTemplate restTemplate,
            my.maleva.api.integration.qne.QneClient qneClient,
            my.maleva.api.common.config.QneProperties qneProperties) {
        this.qneClient = qneClient;
        this.qneProperties = qneProperties;
        this.restTemplate = restTemplate;
    }

    /**
     * Send WhatsApp message via external WhatsApp service provider
     * Equivalent to .NET WhatsAppSend method
     *
     * @param mobileNumber Mobile number(s) to send to (can be comma-separated)
     * @param messageData  The message content
     * @param wType        Message type: 1=text, 2=image, 3=pdf
     */
    @Override
    public void sendWhatsAppMessage(String mobileNumber, String messageData, int wType) {
        if (!whatsAppEnabled) {
            logger.debug("WhatsApp notifications are disabled in configuration");
            return;
        }

        if (mobileNumber == null || mobileNumber.trim().isEmpty()) {
            logger.warn("Cannot send WhatsApp: No mobile number provided");
            return;
        }

        try {
            logger.info("Sending WhatsApp message to: {}", maskPhoneNumber(mobileNumber));

            if (whatsAppApiUrl == null || whatsAppApiUrl.trim().isEmpty()) {
                logger.warn("WhatsApp API URL not configured");
                return;
            }

            // Build WhatsApp request payload
            WhatsAppRequest request = new WhatsAppRequest();
            request.setMobileNumber(mobileNumber);
            request.setMessage(messageData);
            request.setMessageType(wType);
            request.setTimestamp(System.currentTimeMillis());

            // Send via REST API
            try {
                WhatsAppResponse response = restTemplate.postForObject(
                        whatsAppApiUrl,
                        buildWhatsAppPayload(request),
                        WhatsAppResponse.class
                );

                if (response != null && response.isSuccess()) {
                    logger.info("✓ WhatsApp message sent successfully - Message ID: {}", response.getMessageId());
                } else {
                    logger.warn("✗ WhatsApp API returned error: {}",
                            response != null ? response.getErrorMessage() : "Unknown error");
                }
            } catch (RestClientException ex) {
                logger.error("✗ Failed to send WhatsApp message via API", ex);
            }

        } catch (Exception ex) {
            logger.error("✗ Error sending WhatsApp message", ex);
        }
    }

    /**
     * Send WhatsApp message with attachment
     * Equivalent to .NET WhatsAppSend with file attachment
     *
     * @param mobileNumber  Mobile number(s)
     * @param messageData   Message content
     * @param attachmentUrl URL of attachment
     * @param wType         Message type: 1=text, 2=image, 3=pdf
     */
    @Override
    public void sendWhatsAppMessageWithAttachment(
            String mobileNumber,
            String messageData,
            String attachmentUrl,
            int wType) {

        if (!whatsAppEnabled) {
            logger.debug("WhatsApp notifications are disabled");
            return;
        }

        if (mobileNumber == null || mobileNumber.trim().isEmpty()) {
            logger.warn("Cannot send WhatsApp with attachment: No mobile number provided");
            return;
        }

        try {
            logger.info("Sending WhatsApp message with attachment to: {}", maskPhoneNumber(mobileNumber));

            if (whatsAppApiUrl == null || whatsAppApiUrl.trim().isEmpty()) {
                logger.warn("WhatsApp API URL not configured");
                return;
            }

            WhatsAppRequest request = new WhatsAppRequest();
            request.setMobileNumber(mobileNumber);
            request.setMessage(messageData);
            request.setMessageType(wType);
            request.setAttachmentUrl(attachmentUrl);
            request.setTimestamp(System.currentTimeMillis());

            try {
                WhatsAppResponse response = restTemplate.postForObject(
                        whatsAppApiUrl,
                        buildWhatsAppPayload(request),
                        WhatsAppResponse.class
                );

                if (response != null && response.isSuccess()) {
                    logger.info("✓ WhatsApp message with attachment sent - Message ID: {}", response.getMessageId());
                } else {
                    logger.warn("✗ WhatsApp API error: {}",
                            response != null ? response.getErrorMessage() : "Unknown");
                }
            } catch (RestClientException ex) {
                logger.error("✗ Failed to send WhatsApp with attachment", ex);
            }

        } catch (Exception ex) {
            logger.error("✗ Error sending WhatsApp with attachment", ex);
        }
    }

    /**
     * Send email notification
     * Equivalent to .NET SendEmail method
     *
     * @param emailAddresses Comma-separated email addresses
     * @param subject        Email subject
     * @param body           Email body (HTML)
     */
    @Override
    public void sendEmail(String emailAddresses, String subject, String body) {
        if (!emailEnabled) {
            logger.debug("Email notifications are disabled in configuration");
            return;
        }

        if (emailAddresses == null || emailAddresses.trim().isEmpty()) {
            logger.warn("Cannot send email: No email addresses provided");
            return;
        }

        try {
            logger.info("Sending email to: {} with subject: {}", maskEmail(emailAddresses), subject);

            if (emailApiUrl == null || emailApiUrl.trim().isEmpty()) {
                logger.warn("Email API URL not configured");
                return;
            }

            EmailRequest request = new EmailRequest();
            request.setTo(emailAddresses);
            request.setSubject(subject);
            request.setBody(body);
            request.setIsHtml(true);
            request.setTimestamp(System.currentTimeMillis());

            try {
                EmailResponse response = restTemplate.postForObject(
                        emailApiUrl,
                        request,
                        EmailResponse.class
                );

                if (response != null && response.isSuccess()) {
                    logger.info("✓ Email sent successfully - Message ID: {}", response.getMessageId());
                } else {
                    logger.warn("✗ Email API error: {}",
                            response != null ? response.getErrorMessage() : "Unknown");
                }
            } catch (RestClientException ex) {
                logger.error("✗ Failed to send email via API", ex);
            }

        } catch (Exception ex) {
            logger.error("✗ Error sending email", ex);
        }
    }

    /**
     * Make API call to QNE system for external integration.
     *
     * <p>Delegates to {@link my.maleva.api.integration.qne.QneClient}. The
     * previous inline version read {@code app.qne.api.url}, a key that exists
     * nowhere, so it always logged "not configured" and returned null. The
     * relative-URL contract is kept: {@code url} is appended to the configured
     * QNE base URL.
     *
     * @param url  endpoint path relative to the QNE base URL
     * @param data request body for POST/PUT
     * @param type 1=GET, 2=POST, 3=PUT
     * @return the QNE response body, or null on failure (legacy contract)
     * @deprecated new code should use {@link my.maleva.api.integration.qne.QneGateway}
     */
    @Deprecated
    @Override
    public String callQneApi(String url, Object data, int type) {
        String base = qneProperties.getBaseUrl();
        String fullUrl = base.endsWith("/") || url.startsWith("/") ? base + url : base + "/" + url;
        my.maleva.api.integration.qne.QneResult result = switch (type) {
            case 1 -> qneClient.get(fullUrl);
            case 2 -> qneClient.post(fullUrl, data);
            case 3 -> qneClient.put(fullUrl, data);
            default -> null;
        };
        if (result == null) {
            logger.warn("Unknown QNE API call type: {}", type);
            return null;
        }
        return result.success() ? result.message() : null;
    }

    /**
     * Build WhatsApp API payload
     */
    private Object buildWhatsAppPayload(WhatsAppRequest request) {
        return request;
    }

    /**
     * Mask phone number for logging (show only last 4 digits)
     */
    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) {
            return "****";
        }
        return "****" + phoneNumber.substring(phoneNumber.length() - 4);
    }

    /**
     * Mask email for logging
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "****";
        }
        String[] parts = email.split("@");
        if (parts[0].length() < 2) {
            return "*@" + parts[1];
        }
        return parts[0].charAt(0) + "***@" + parts[1];
    }

    // Inner classes for API communication

    public static class WhatsAppRequest {
        private String mobileNumber;
        private String message;
        private int messageType;
        private String attachmentUrl;
        private long timestamp;

        // Getters and setters
        public String getMobileNumber() { return mobileNumber; }
        public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public int getMessageType() { return messageType; }
        public void setMessageType(int messageType) { this.messageType = messageType; }

        public String getAttachmentUrl() { return attachmentUrl; }
        public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }

    public static class WhatsAppResponse {
        private boolean success;
        private String messageId;
        private String errorMessage;

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessageId() { return messageId; }
        public void setMessageId(String messageId) { this.messageId = messageId; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    public static class EmailRequest {
        private String to;
        private String subject;
        private String body;
        private boolean isHtml;
        private long timestamp;

        // Getters and setters
        public String getTo() { return to; }
        public void setTo(String to) { this.to = to; }

        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }

        public String getBody() { return body; }
        public void setBody(String body) { this.body = body; }

        public boolean isHtml() { return isHtml; }
        public void setIsHtml(boolean html) { isHtml = html; }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }

    public static class EmailResponse {
        private boolean success;
        private String messageId;
        private String errorMessage;

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessageId() { return messageId; }
        public void setMessageId(String messageId) { this.messageId = messageId; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
}

