package my.maleva.api.module.billing.billorder.service;

/**
 * Common functions for BillsOrderMaster operations
 * Equivalent to .NET commonfunctions class
 *
 * Provides shared functionality like:
 * - WhatsApp message sending
 * - Email notifications
 * - QNE API calls
 * - Report generation
 */
public interface BillsOrderCommonService {

    /**
     * Send WhatsApp message
     *
     * @param mobileNumber Mobile number(s) to send to (can be comma-separated)
     * @param messageData The message content
     * @param wType Message type: 1=text, 2=image, 3=pdf
     */
    void sendWhatsAppMessage(String mobileNumber, String messageData, int wType);

    /**
     * Send WhatsApp message with attachment
     *
     * @param mobileNumber Mobile number(s)
     * @param messageData Message content
     * @param attachmentUrl URL of attachment
     * @param wType Message type: 1=text, 2=image, 3=pdf
     */
    void sendWhatsAppMessageWithAttachment(
            String mobileNumber,
            String messageData,
            String attachmentUrl,
            int wType);

    /**
     * Send email notification
     *
     * @param emailAddresses Comma-separated email addresses
     * @param subject Email subject
     * @param body Email body (HTML)
     */
    void sendEmail(String emailAddresses, String subject, String body);

    /**
     * Make API call to QNE system
     *
     * @param url The API endpoint
     * @param data Request data
     * @param type 1=GET, 2=POST, 3=PUT
     * @return Response from QNE
     */
    String callQneApi(String url, Object data, int type);
}

