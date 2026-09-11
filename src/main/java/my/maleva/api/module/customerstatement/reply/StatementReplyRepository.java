package my.maleva.api.module.customerstatement.reply;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * What customers write back — {@code dbo.CustomerStatementMailReply}, its
 * attachments, and where the mailbox reader left off ({@code MailboxSyncState}).
 *
 * <p>Writes run inside a {@link TransactionTemplate}: the pool hands out
 * autocommit-off connections, so a bare write would be rolled back on return
 * (see {@code StatementMailLogRepository}).
 */
@Slf4j
@Repository
public class StatementReplyRepository {

    static final String REPLIES = "dbo.CustomerStatementMailReply";
    static final String ATTACHMENTS = "dbo.CustomerStatementMailAttachment";
    static final String SYNC = "dbo.MailboxSyncState";

    private final NamedParameterJdbcTemplate jdbc;
    private final TransactionTemplate tx;

    public StatementReplyRepository(NamedParameterJdbcTemplate jdbc, PlatformTransactionManager transactionManager) {
        this.jdbc = jdbc;
        this.tx = new TransactionTemplate(transactionManager);
    }

    /** A reply to store, once the mailbox reader has matched it to a customer. */
    public record ReplyEntry(
            int companyId, int customerId, Long mailLogRefId,
            String mailbox, String folder, long mailboxUid,
            String messageId, String inReplyTo,
            String fromAddress, String fromName, String to, String cc, String subject,
            String bodyText, String bodyHtml, boolean hasAttachments, LocalDateTime receivedAt) {
    }

    public record Reply(
            long id, int companyId, int customerId, Long mailLogRefId,
            String messageId, String inReplyTo,
            String fromAddress, String fromName, String to, String cc, String subject,
            String bodyText, boolean hasAttachments, LocalDateTime receivedAt,
            String readBy, LocalDateTime readAt) {
    }

    public record Attachment(long id, long replyId, String fileName, String contentType, long sizeBytes, String storagePath) {
    }

    /** Per customer: the latest reply and how many there are, for the screen's "Reply" column. */
    public record Summary(
            int customerId, long lastReplyId, LocalDateTime lastReplyAt,
            String lastFrom, String lastFromName, String lastSubject, String lastSnippet,
            int replyCount, int unreadCount) {
    }

    public record SyncState(String mailbox, String folder, long uidValidity, long lastUid, LocalDateTime lastRunAt, String lastError) {
    }

    private static final String REPLY_COLUMNS = "Id, CompanyRefId, CustomerRefId, MailLogRefId, MessageId, InReplyTo, FromAddress, FromName, "
            + "ToAddresses, CcAddresses, Subject, BodyText, HasAttachments, ReceivedAt, ReadBy, ReadAt";

    private static final RowMapper<Reply> REPLY = (rs, i) -> new Reply(
            rs.getLong("Id"), rs.getInt("CompanyRefId"), rs.getInt("CustomerRefId"), rs.getObject("MailLogRefId", Long.class),
            rs.getString("MessageId"), rs.getString("InReplyTo"), rs.getString("FromAddress"), rs.getString("FromName"),
            rs.getString("ToAddresses"), rs.getString("CcAddresses"), rs.getString("Subject"), rs.getString("BodyText"),
            rs.getBoolean("HasAttachments"), rs.getObject("ReceivedAt", LocalDateTime.class),
            rs.getString("ReadBy"), rs.getObject("ReadAt", LocalDateTime.class));

    private static final RowMapper<Attachment> ATTACHMENT = (rs, i) -> new Attachment(
            rs.getLong("Id"), rs.getLong("ReplyRefId"), rs.getString("FileName"), rs.getString("ContentType"),
            rs.getLong("SizeBytes"), rs.getString("StoragePath"));

    // ── replies ────────────────────────────────────────────────────────────

    public boolean exists(String mailbox, String folder, long uid) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM " + REPLIES + " WHERE Mailbox = :m AND Folder = :f AND MailboxUid = :u",
                new MapSqlParameterSource("m", mailbox).addValue("f", folder).addValue("u", uid), Integer.class);
        return n != null && n > 0;
    }

    /** Store a reply; returns its id. */
    public long insert(ReplyEntry e) {
        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("companyId", e.companyId())
                .addValue("customerId", e.customerId())
                .addValue("mailLogRefId", e.mailLogRefId(), Types.BIGINT)
                .addValue("mailbox", cut(e.mailbox(), 100))
                .addValue("folder", cut(e.folder(), 100))
                .addValue("uid", e.mailboxUid())
                .addValue("messageId", cut(e.messageId(), 255))
                .addValue("inReplyTo", cut(e.inReplyTo(), 255))
                .addValue("fromAddress", cut(e.fromAddress() == null ? "" : e.fromAddress(), 255))
                .addValue("fromName", cut(e.fromName(), 200))
                .addValue("to", cut(e.to(), 1000))
                .addValue("cc", cut(e.cc(), 1000))
                .addValue("subject", cut(e.subject(), 500))
                .addValue("bodyText", e.bodyText())
                .addValue("bodyHtml", e.bodyHtml())
                .addValue("hasAttachments", e.hasAttachments())
                .addValue("receivedAt", e.receivedAt(), Types.TIMESTAMP);
        Long id = tx.execute(status -> jdbc.queryForObject("""
                INSERT INTO %s (CompanyRefId, CustomerRefId, MailLogRefId, Mailbox, Folder, MailboxUid, MessageId, InReplyTo,
                                FromAddress, FromName, ToAddresses, CcAddresses, Subject, BodyText, BodyHtml, HasAttachments, ReceivedAt)
                VALUES (:companyId, :customerId, :mailLogRefId, :mailbox, :folder, :uid, :messageId, :inReplyTo,
                        :fromAddress, :fromName, :to, :cc, :subject, :bodyText, :bodyHtml, :hasAttachments, :receivedAt);
                SELECT CAST(SCOPE_IDENTITY() AS BIGINT)
                """.formatted(REPLIES), p, Long.class));
        return id == null ? 0L : id;
    }

    public void insertAttachment(long replyId, String fileName, String contentType, long sizeBytes, String storagePath) {
        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("replyId", replyId)
                .addValue("fileName", cut(fileName, 255))
                .addValue("contentType", cut(contentType, 100))
                .addValue("size", sizeBytes)
                .addValue("path", cut(storagePath, 500));
        tx.executeWithoutResult(status -> jdbc.update("INSERT INTO " + ATTACHMENTS
                + " (ReplyRefId, FileName, ContentType, SizeBytes, StoragePath) VALUES (:replyId, :fileName, :contentType, :size, :path)", p));
    }

    /** The latest reply per customer of the company, with counts. */
    public List<Summary> findLatest(int companyId) {
        return jdbc.query("""
                SELECT Id, CustomerRefId, ReceivedAt, FromAddress, FromName, Subject, Snippet, ReplyCount, UnreadCount
                FROM (SELECT Id, CustomerRefId, ReceivedAt, FromAddress, FromName, Subject,
                             LEFT(ISNULL(BodyText, ''), 1000) AS Snippet,
                             COUNT(*) OVER (PARTITION BY CustomerRefId) AS ReplyCount,
                             SUM(CASE WHEN ReadAt IS NULL THEN 1 ELSE 0 END) OVER (PARTITION BY CustomerRefId) AS UnreadCount,
                             ROW_NUMBER() OVER (PARTITION BY CustomerRefId ORDER BY ReceivedAt DESC, Id DESC) AS rn
                      FROM %s WITH (NOLOCK)
                      WHERE CompanyRefId = :companyId) x
                WHERE rn = 1
                """.formatted(REPLIES),
                new MapSqlParameterSource("companyId", companyId),
                (rs, i) -> new Summary(rs.getInt("CustomerRefId"), rs.getLong("Id"), rs.getObject("ReceivedAt", LocalDateTime.class),
                        rs.getString("FromAddress"), rs.getString("FromName"), rs.getString("Subject"), rs.getString("Snippet"),
                        rs.getInt("ReplyCount"), rs.getInt("UnreadCount")));
    }

    /** An unread reply with its customer's name, for the header bell. */
    public record Unread(long id, int customerId, String customerName, String fromAddress, String fromName,
                         String subject, String snippet, LocalDateTime receivedAt,
                         String statementSubject, LocalDateTime statementSentAt) {
    }

    /** Replies nobody has opened, newest first, with the customer's name from the master. */
    public List<Unread> findUnread(int companyId, int limit) {
        return jdbc.query("""
                SELECT TOP (:limit) r.Id, r.CustomerRefId, c.CustomerName, r.FromAddress, r.FromName, r.Subject,
                       LEFT(ISNULL(r.BodyText, ''), 1000) AS Snippet, r.ReceivedAt,
                       l.Subject AS StatementSubject, l.SentAt AS StatementSentAt
                FROM %s r WITH (NOLOCK)
                LEFT JOIN Customer c WITH (NOLOCK) ON c.Id = r.CustomerRefId
                LEFT JOIN dbo.CustomerStatementMailLog l WITH (NOLOCK) ON l.Id = r.MailLogRefId
                WHERE r.CompanyRefId = :companyId AND r.ReadAt IS NULL
                ORDER BY r.ReceivedAt DESC, r.Id DESC
                """.formatted(REPLIES),
                new MapSqlParameterSource("companyId", companyId).addValue("limit", Math.max(1, limit)),
                (rs, i) -> new Unread(rs.getLong("Id"), rs.getInt("CustomerRefId"), rs.getString("CustomerName"),
                        rs.getString("FromAddress"), rs.getString("FromName"), rs.getString("Subject"),
                        rs.getString("Snippet"), rs.getObject("ReceivedAt", LocalDateTime.class),
                        rs.getString("StatementSubject"), rs.getObject("StatementSentAt", LocalDateTime.class)));
    }

    public int countUnread(int companyId) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM " + REPLIES + " WITH (NOLOCK) WHERE CompanyRefId = :companyId AND ReadAt IS NULL",
                new MapSqlParameterSource("companyId", companyId), Integer.class);
        return n == null ? 0 : n;
    }

    /** Every reply from this customer, oldest first. */
    public List<Reply> findByCustomer(int companyId, int customerId) {
        return jdbc.query("SELECT " + REPLY_COLUMNS + " FROM " + REPLIES + " WITH (NOLOCK) "
                        + "WHERE CompanyRefId = :companyId AND CustomerRefId = :customerId ORDER BY ReceivedAt, Id",
                new MapSqlParameterSource("companyId", companyId).addValue("customerId", customerId), REPLY);
    }

    public Optional<Reply> findById(long id) {
        return jdbc.query("SELECT " + REPLY_COLUMNS + " FROM " + REPLIES + " WITH (NOLOCK) WHERE Id = :id",
                new MapSqlParameterSource("id", id), REPLY).stream().findFirst();
    }

    public List<Attachment> findAttachments(List<Long> replyIds) {
        if (replyIds == null || replyIds.isEmpty()) {
            return List.of();
        }
        return jdbc.query("SELECT Id, ReplyRefId, FileName, ContentType, SizeBytes, StoragePath FROM " + ATTACHMENTS
                        + " WITH (NOLOCK) WHERE ReplyRefId IN (:ids) ORDER BY Id",
                new MapSqlParameterSource("ids", replyIds), ATTACHMENT);
    }

    public Optional<Attachment> findAttachment(long replyId, long attachmentId) {
        return jdbc.query("SELECT Id, ReplyRefId, FileName, ContentType, SizeBytes, StoragePath FROM " + ATTACHMENTS
                        + " WITH (NOLOCK) WHERE Id = :id AND ReplyRefId = :replyId",
                new MapSqlParameterSource("id", attachmentId).addValue("replyId", replyId), ATTACHMENT).stream().findFirst();
    }

    /** Mark read by this user; a reply already read keeps its first reader. */
    public void markRead(long id, String user) {
        tx.executeWithoutResult(status -> jdbc.update("UPDATE " + REPLIES
                        + " SET ReadBy = :user, ReadAt = SYSDATETIME() WHERE Id = :id AND ReadAt IS NULL",
                new MapSqlParameterSource("id", id).addValue("user", cut(user, 100))));
    }

    // ── where the reader left off ──────────────────────────────────────────

    public Optional<SyncState> syncState(String mailbox, String folder) {
        return jdbc.query("SELECT Mailbox, Folder, UidValidity, LastUid, LastRunAt, LastError FROM " + SYNC
                        + " WHERE Mailbox = :m AND Folder = :f",
                new MapSqlParameterSource("m", mailbox).addValue("f", folder),
                (rs, i) -> new SyncState(rs.getString("Mailbox"), rs.getString("Folder"), rs.getLong("UidValidity"),
                        rs.getLong("LastUid"), rs.getObject("LastRunAt", LocalDateTime.class), rs.getString("LastError")))
                .stream().findFirst();
    }

    public void saveSyncState(SyncState s) {
        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("m", cut(s.mailbox(), 100)).addValue("f", cut(s.folder(), 100))
                .addValue("validity", s.uidValidity()).addValue("lastUid", s.lastUid())
                .addValue("ranAt", s.lastRunAt(), Types.TIMESTAMP).addValue("error", cut(s.lastError(), 1000));
        tx.executeWithoutResult(status -> {
            int updated = jdbc.update("UPDATE " + SYNC + " SET UidValidity = :validity, LastUid = :lastUid, LastRunAt = :ranAt, LastError = :error "
                    + "WHERE Mailbox = :m AND Folder = :f", p);
            // SET NOCOUNT ON makes every update report -1 here, so check the row instead of the count
            if (updated <= 0) {
                Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM " + SYNC + " WHERE Mailbox = :m AND Folder = :f", p, Integer.class);
                if (n == null || n == 0) {
                    jdbc.update("INSERT INTO " + SYNC + " (Mailbox, Folder, UidValidity, LastUid, LastRunAt, LastError) "
                            + "VALUES (:m, :f, :validity, :lastUid, :ranAt, :error)", p);
                }
            }
        });
    }

    /** Note a failed scan without moving the position. */
    public void saveSyncError(String mailbox, String folder, String error) {
        try {
            Optional<SyncState> current = syncState(mailbox, folder);
            saveSyncState(new SyncState(mailbox, folder,
                    current.map(SyncState::uidValidity).orElse(0L), current.map(SyncState::lastUid).orElse(0L),
                    LocalDateTime.now(), error));
        } catch (DataAccessException ex) {
            log.warn("Could not record the mailbox scan error: {}", ex.getMessage());
        }
    }

    private static String cut(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}
