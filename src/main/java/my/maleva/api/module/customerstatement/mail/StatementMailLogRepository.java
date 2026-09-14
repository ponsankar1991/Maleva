package my.maleva.api.module.customerstatement.mail;

import lombok.extern.slf4j.Slf4j;
import my.maleva.api.module.customerstatement.dto.StatementLastSent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * The send log — {@code dbo.CustomerStatementMailLog}, one row per mail we
 * send a customer about their statement: the statement itself
 * ({@code Kind = STATEMENT}) or an answer written from the screen
 * ({@code Kind = REPLY}). Sent or refused.
 *
 * <p>Legacy kept no record: the only trace of a statement having been sent
 * was the accounts mailbox's Sent folder. The screen needs "last sent" per
 * customer to keep a 500-mail round from mailing anyone twice, so every send
 * writes a row here. Each row also carries the mail's Message-ID: that is
 * what a customer's reply points back to (In-Reply-To / References), and how
 * the mailbox reader knows which customer wrote.
 *
 * <p><b>Never in the way of a send.</b> The table is created at startup when
 * missing (see {@code db/customer-statement-mail-log.sql}); if that cannot be
 * done — the app's SQL login may not create tables — the repository marks
 * itself unavailable, warns once, and every write and read becomes a no-op.
 * A statement still goes out; only the "last sent" column stays empty until
 * the script is run by hand.
 *
 * <p><b>Writes commit explicitly.</b> The pool hands out connections with
 * autocommit OFF ({@code hikari.auto-commit: false}, for Hibernate), so a
 * bare {@code JdbcTemplate} write outside a transaction is silently rolled
 * back when the connection returns to the pool. The DDL and every INSERT run
 * inside a {@link TransactionTemplate}. Reads need none.
 */
@Slf4j
@Repository
public class StatementMailLogRepository {

    static final String TABLE = "dbo.CustomerStatementMailLog";
    static final String SCRIPT = "db/customer-statement-mail-log.sql";

    public static final String KIND_STATEMENT = "STATEMENT";
    public static final String KIND_REPLY = "REPLY";

    private final NamedParameterJdbcTemplate jdbc;
    private final TransactionTemplate tx;
    private final boolean autoCreate;
    private volatile boolean available;

    public StatementMailLogRepository(NamedParameterJdbcTemplate jdbc,
                                      PlatformTransactionManager transactionManager,
                                      @Value("${mail.statement.log.auto-create:true}") boolean autoCreate) {
        this.jdbc = jdbc;
        this.tx = new TransactionTemplate(transactionManager);
        this.autoCreate = autoCreate;
    }

    /**
     * A row to record. {@code status} is SENT or FAILED; {@code kind} is
     * STATEMENT or REPLY; {@code messageId} is the mail's own Message-ID
     * (angle brackets included); {@code inReplyTo} / {@code replyToRefId}
     * name the customer mail an answer answers.
     */
    public record Entry(
            int companyId, int customerId, String customerName,
            List<String> sentTo, List<String> cc, String subject, String reminder,
            LocalDate statementDate, BigDecimal overdueAmount, String currency, String attachmentName,
            String status, String error, String jobId, String sentBy, LocalDateTime sentAt,
            String kind, String messageId, String inReplyTo, Long replyToRefId,
            String bodyText) {

        public Entry(
                int companyId, int customerId, String customerName,
                List<String> sentTo, List<String> cc, String subject, String reminder,
                LocalDate statementDate, BigDecimal overdueAmount, String currency, String attachmentName,
                String status, String error, String jobId, String sentBy, LocalDateTime sentAt,
                String kind, String messageId, String inReplyTo, Long replyToRefId) {
            this(companyId, customerId, customerName, sentTo, cc, subject, reminder, statementDate,
                    overdueAmount, currency, attachmentName, status, error, jobId, sentBy, sentAt,
                    kind, messageId, inReplyTo, replyToRefId, null);
        }
    }

    /** One logged mail, as the conversation view and the reply matcher read it. */
    public record Row(
            long id, int companyId, int customerId, String customerName,
            String sentTo, String cc, String subject, String reminder, String attachmentName,
            String status, String error, String sentBy, LocalDateTime sentAt,
            String kind, String messageId, String inReplyTo, Long replyToRefId,
            String bodyText) {
    }

    private static final RowMapper<Row> ROW = (rs, i) -> new Row(
            rs.getLong("Id"), rs.getInt("CompanyRefId"), rs.getInt("CustomerRefId"), rs.getString("CustomerName"),
            rs.getString("SentTo"), rs.getString("Cc"), rs.getString("Subject"), rs.getString("Reminder"),
            rs.getString("AttachmentName"), rs.getString("Status"), rs.getString("Error"), rs.getString("SentBy"),
            rs.getObject("SentAt", LocalDateTime.class), rs.getString("Kind"), rs.getString("MessageId"),
            rs.getString("InReplyTo"), rs.getObject("ReplyToRefId", Long.class),
            rs.getString("BodyText"));

    private static final String ROW_COLUMNS = "Id, CompanyRefId, CustomerRefId, CustomerName, SentTo, Cc, Subject, Reminder, "
            + "AttachmentName, Status, Error, SentBy, SentAt, Kind, MessageId, InReplyTo, ReplyToRefId, BodyText";

    public boolean isAvailable() {
        return available;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void ensureTable() {
        try {
            if (autoCreate) {
                String ddl = script();
                tx.executeWithoutResult(status -> jdbc.getJdbcOperations().execute(ddl));
            }
            // Probe on its own connection, so a table that only existed inside a
            // rolled-back transaction is found out now and not on the first send.
            jdbc.getJdbcOperations().queryForObject("SELECT COUNT(*) FROM " + TABLE + " WHERE 1 = 0", Integer.class);
            available = true;
            log.info("Customer statement send log ready ({})", TABLE);
        } catch (DataAccessException | TransactionException | IllegalStateException ex) {
            available = false;
            log.warn("Customer statement send log unavailable - statements still send, but 'last sent' will be empty. "
                    + "Run {} by hand. Cause: {}", SCRIPT, root(ex));
        }
    }

    /** Record one mail. Never throws: a log that cannot be written must not undo a mail that went. */
    public boolean insert(Entry e) {
        if (!available) {
            log.debug("Send log unavailable; not recording statement mail to customer {}", e.customerId());
            return false;
        }
        try {
            MapSqlParameterSource p = new MapSqlParameterSource()
                    .addValue("companyId", e.companyId())
                    .addValue("customerId", e.customerId())
                    .addValue("customerName", cut(e.customerName(), 200))
                    .addValue("sentTo", cut(String.join(", ", e.sentTo()), 1000))
                    .addValue("cc", cut(e.cc() == null ? null : String.join(", ", e.cc()), 500))
                    .addValue("subject", cut(e.subject(), 300))
                    .addValue("reminder", cut(e.reminder() == null ? "" : e.reminder(), 50))
                    .addValue("statementDate", e.statementDate(), Types.DATE)
                    .addValue("overdueAmount", e.overdueAmount())
                    .addValue("currency", cut(e.currency(), 10))
                    .addValue("attachmentName", cut(e.attachmentName(), 200))
                    .addValue("status", e.status())
                    .addValue("error", cut(e.error(), 1000))
                    .addValue("jobId", cut(e.jobId(), 40))
                    .addValue("sentBy", cut(e.sentBy(), 100))
                    .addValue("sentAt", e.sentAt(), Types.TIMESTAMP)
                    .addValue("kind", e.kind() == null ? KIND_STATEMENT : e.kind())
                    .addValue("messageId", cut(e.messageId(), 255))
                    .addValue("inReplyTo", cut(e.inReplyTo(), 255))
                    .addValue("replyToRefId", e.replyToRefId(), Types.BIGINT)
                    .addValue("bodyText", e.bodyText());
            String sql = """
                    INSERT INTO %s (CompanyRefId, CustomerRefId, CustomerName, SentTo, Cc, Subject, Reminder,
                                    StatementDate, OverdueAmount, Currency, AttachmentName, Status, Error, JobId, SentBy, SentAt,
                                    Kind, MessageId, InReplyTo, ReplyToRefId, BodyText)
                    VALUES (:companyId, :customerId, :customerName, :sentTo, :cc, :subject, :reminder,
                            :statementDate, :overdueAmount, :currency, :attachmentName, :status, :error, :jobId, :sentBy, :sentAt,
                            :kind, :messageId, :inReplyTo, :replyToRefId, :bodyText)
                    """.formatted(TABLE);
            tx.executeWithoutResult(status -> jdbc.update(sql, p));
            return true;
        } catch (DataAccessException | TransactionException ex) {
            log.warn("Statement mail to customer {} was {} but could not be logged: {}",
                    e.customerId(), e.status(), root(ex));
            return false;
        }
    }

    /** The most recent accepted STATEMENT per customer of the company. Empty when the log is unavailable. */
    public List<StatementLastSent> findLastSent(int companyId) {
        if (!available) {
            return List.of();
        }
        try {
            return jdbc.query("""
                    SELECT CustomerRefId, SentAt, Reminder, SentTo, SentBy
                    FROM (SELECT CustomerRefId, SentAt, Reminder, SentTo, SentBy,
                                 ROW_NUMBER() OVER (PARTITION BY CustomerRefId ORDER BY SentAt DESC, Id DESC) AS rn
                          FROM %s WITH (NOLOCK)
                          WHERE CompanyRefId = :companyId AND Status = 'SENT' AND Kind = 'STATEMENT') x
                    WHERE rn = 1
                    """.formatted(TABLE),
                    new MapSqlParameterSource("companyId", companyId),
                    (rs, i) -> new StatementLastSent(
                            rs.getInt("CustomerRefId"),
                            rs.getObject("SentAt", LocalDateTime.class),
                            rs.getString("Reminder"),
                            rs.getString("SentTo"),
                            rs.getString("SentBy")));
        } catch (DataAccessException ex) {
            log.warn("Could not read the statement send log: {}", root(ex));
            return List.of();
        }
    }

    /** The accepted mail with this Message-ID, if we sent one. Case-insensitive on the id. */
    public Optional<Row> findByMessageId(String messageId) {
        if (!available || messageId == null || messageId.isBlank()) {
            return Optional.empty();
        }
        try {
            List<Row> rows = jdbc.query("SELECT TOP 1 " + ROW_COLUMNS + " FROM " + TABLE + " WITH (NOLOCK) "
                            + "WHERE MessageId = :id AND Status = 'SENT' ORDER BY Id DESC",
                    new MapSqlParameterSource("id", messageId.trim()), ROW);
            return rows.stream().findFirst();
        } catch (DataAccessException ex) {
            log.warn("Could not look up Message-ID {}: {}", messageId, root(ex));
            return Optional.empty();
        }
    }

    /** The latest accepted mail whose subject is exactly this (a reply's subject with its "Re:" stripped). */
    public Optional<Row> findLatestBySubject(String subject) {
        if (!available || subject == null || subject.isBlank()) {
            return Optional.empty();
        }
        try {
            List<Row> rows = jdbc.query("SELECT TOP 1 " + ROW_COLUMNS + " FROM " + TABLE + " WITH (NOLOCK) "
                            + "WHERE Subject = :subject AND Status = 'SENT' ORDER BY SentAt DESC, Id DESC",
                    new MapSqlParameterSource("subject", cut(subject.trim(), 300)), ROW);
            return rows.stream().findFirst();
        } catch (DataAccessException ex) {
            log.warn("Could not look up subject '{}': {}", subject, root(ex));
            return Optional.empty();
        }
    }

    /**
     * The latest accepted mail that went to this address — the customer a
     * one-off or temporary address belongs to. The SentTo column is a
     * comma-separated list, so the match is on the whole address between
     * separators, case-insensitively.
     */
    public Optional<Row> findLatestBySentTo(String address) {
        if (!available || address == null || !address.contains("@")) {
            return Optional.empty();
        }
        try {
            List<Row> rows = jdbc.query("SELECT TOP 1 " + ROW_COLUMNS + " FROM " + TABLE + " WITH (NOLOCK) "
                            + "WHERE Status = 'SENT' AND ',' + REPLACE(LOWER(SentTo), ' ', '') + ',' LIKE :pattern "
                            + "ORDER BY SentAt DESC, Id DESC",
                    new MapSqlParameterSource("pattern", "%," + address.trim().toLowerCase(java.util.Locale.ROOT)
                            .replace("[", "[[]").replace("%", "[%]").replace("_", "[_]") + ",%"), ROW);
            return rows.stream().findFirst();
        } catch (DataAccessException ex) {
            log.warn("Could not look up the address {}: {}", address, root(ex));
            return Optional.empty();
        }
    }

    /** Everything we sent this customer, oldest first — one side of the conversation view. */
    public List<Row> findByCustomer(int companyId, int customerId) {
        if (!available) {
            return List.of();
        }
        try {
            return jdbc.query("SELECT " + ROW_COLUMNS + " FROM " + TABLE + " WITH (NOLOCK) "
                            + "WHERE CompanyRefId = :companyId AND CustomerRefId = :customerId ORDER BY SentAt, Id",
                    new MapSqlParameterSource("companyId", companyId).addValue("customerId", customerId), ROW);
        } catch (DataAccessException ex) {
            log.warn("Could not read the send log for customer {}: {}", customerId, root(ex));
            return List.of();
        }
    }

    private static String script() {
        try (InputStream in = new ClassPathResource(SCRIPT).getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Script " + SCRIPT + " is missing from the classpath", ex);
        }
    }

    private static String cut(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }

    private static String root(Throwable ex) {
        Throwable t = ex;
        while (t.getCause() != null && t.getCause() != t) {
            t = t.getCause();
        }
        return t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage().trim();
    }
}
