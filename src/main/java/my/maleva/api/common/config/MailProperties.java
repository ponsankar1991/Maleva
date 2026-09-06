package my.maleva.api.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * MailProperties - Configuration for email service
 */
@Configuration
@ConfigurationProperties(prefix = "mail")
public class MailProperties {

    private From from = new From();
    private Smtp smtp = new Smtp();
    private Long timeout = 30000L;

    /**
     * Who receives the "Invoice Created" mail sent from the Sale Invoice view's
     * Share button. Legacy hard-coded this list in commonfunctions.TESTMail3.
     */
    private java.util.List<String> invoiceRecipients = new java.util.ArrayList<>();

    /**
     * Copied on every customer receipt mail sent from the Receipt screen.
     * Legacy read {@code config:AccountsMailCc} from Web.config and fell back
     * to the accounts mailbox pair; the operator can still edit the CC box
     * before sending.
     */
    private java.util.List<String> receiptCc = new java.util.ArrayList<>();

    /** Default subject of the receipt mail; editable on the screen. */
    private String receiptSubject = "Payment Received - Thank You";

    /**
     * Optional IMAP mailbox that gets a copy of every sent receipt mail in its
     * Sent folder. Plain SMTP never files a copy, which is why sent receipts
     * were missing from the mailbox before legacy switched to MailKit. Only
     * used when {@code mail.imap.host} is set (env {@code MAIL_IMAP_HOST}).
     */
    private Imap imap = new Imap();

    public java.util.List<String> getInvoiceRecipients() {
        return invoiceRecipients;
    }

    public void setInvoiceRecipients(java.util.List<String> invoiceRecipients) {
        this.invoiceRecipients = invoiceRecipients;
    }

    public java.util.List<String> getReceiptCc() {
        return receiptCc;
    }

    public void setReceiptCc(java.util.List<String> receiptCc) {
        this.receiptCc = receiptCc;
    }

    public String getReceiptSubject() {
        return receiptSubject;
    }

    public void setReceiptSubject(String receiptSubject) {
        this.receiptSubject = receiptSubject;
    }

    public Imap getImap() {
        return imap;
    }

    public void setImap(Imap imap) {
        this.imap = imap;
    }

    public static class Imap {
        private String host;
        private Integer port;
        private String username;
        private String password;
        /** Folder to file sent copies in; the usual names are tried when blank. */
        private String sentFolder;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getSentFolder() {
            return sentFolder;
        }

        public void setSentFolder(String sentFolder) {
            this.sentFolder = sentFolder;
        }
    }

    public static class From {
        private String email;
        private String name;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class Smtp {
        private String host;
        private Integer port;
        private String username;
        private String password;
        private Boolean auth;
        private Starttls starttls = new Starttls();

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public Boolean getAuth() {
            return auth;
        }

        public void setAuth(Boolean auth) {
            this.auth = auth;
        }

        public Starttls getStarttls() {
            return starttls;
        }

        public void setStarttls(Starttls starttls) {
            this.starttls = starttls;
        }
    }

    public static class Starttls {
        private Boolean enabled;
        private Boolean required;

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public Boolean getRequired() {
            return required;
        }

        public void setRequired(Boolean required) {
            this.required = required;
        }
    }

    public From getFrom() {
        return from;
    }

    public void setFrom(From from) {
        this.from = from;
    }

    public Smtp getSmtp() {
        return smtp;
    }

    public void setSmtp(Smtp smtp) {
        this.smtp = smtp;
    }

    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }
}

