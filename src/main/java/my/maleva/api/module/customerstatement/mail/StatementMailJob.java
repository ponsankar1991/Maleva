package my.maleva.api.module.customerstatement.mail;

import my.maleva.api.module.customerstatement.dto.CustomerStatement;
import my.maleva.api.module.customerstatement.dto.StatementMailJobView;
import my.maleva.api.module.customerstatement.dto.StatementResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The state of one bulk statement run, shared between the request thread
 * that created it, the worker thread that sends it, and the polling calls
 * that read it.
 *
 * <p>Held in memory only: a run is minutes long and the durable record of
 * what went out is the send log ({@link StatementMailLogRepository}), one row
 * per mail. A restart in mid-run loses the progress view, not the log — the
 * screen's "last sent" column shows what had already gone.
 *
 * <p>Fields the worker changes are {@code volatile}; the views built from
 * them are snapshots, consistent enough for a progress bar. The statements
 * themselves are dropped once the run finishes ({@link #release()}), so a
 * 500-customer run does not keep 500 statements alive for the retention
 * window.
 */
final class StatementMailJob {

    enum Status { QUEUED, RUNNING, DONE, CANCELLED, FAILED }

    enum ItemStatus { PENDING, SENDING, SENT, FAILED, SKIPPED, CANCELLED }

    static final class Item {
        final int customerId;
        final String customerName;
        final List<String> emails;
        final BigDecimal overdueAmount;
        final String currency;
        volatile ItemStatus status;
        volatile String error;
        volatile LocalDateTime sentAt;
        volatile String subject;
        volatile String attachmentName;
        /** The statement to render and quote; null once the run is over. */
        volatile CustomerStatement statement;

        Item(int customerId, String customerName, List<String> emails, CustomerStatement statement,
             ItemStatus status, String error) {
            this.customerId = customerId;
            this.customerName = customerName;
            this.emails = List.copyOf(emails);
            this.statement = statement;
            this.overdueAmount = statement == null ? null : statement.getOverdueAmount();
            this.currency = statement == null ? null : statement.getCurrency();
            this.status = status;
            this.error = error;
        }

        void sent(String subject, String attachmentName) {
            this.subject = subject;
            this.attachmentName = attachmentName;
            this.sentAt = LocalDateTime.now();
            this.error = null;
            this.status = ItemStatus.SENT;
        }

        void failed(String error) {
            this.error = error;
            this.status = ItemStatus.FAILED;
        }

        StatementMailJobView.Item view() {
            return new StatementMailJobView.Item(customerId, customerName, emails, status.name(), error,
                    sentAt, subject, attachmentName, overdueAmount, currency);
        }
    }

    private final String id;
    private final int companyId;
    private final String reminder;
    private final String requestedBy;
    private final LocalDateTime createdAt = LocalDateTime.now();
    private final List<Item> items;
    private final AtomicBoolean cancelRequested = new AtomicBoolean();

    private volatile Status status = Status.QUEUED;
    private volatile LocalDateTime startedAt;
    private volatile LocalDateTime finishedAt;
    private volatile String error;
    /** The whole result the statements came from — period and ageing window for the PDFs; null once over. */
    private volatile StatementResult result;

    StatementMailJob(String id, int companyId, String reminder, String requestedBy,
                     StatementResult result, List<Item> items) {
        this.id = id;
        this.companyId = companyId;
        this.reminder = reminder == null ? "" : reminder;
        this.requestedBy = requestedBy;
        this.result = result;
        this.items = List.copyOf(items);
    }

    String id() {
        return id;
    }

    int companyId() {
        return companyId;
    }

    String reminder() {
        return reminder;
    }

    String requestedBy() {
        return requestedBy;
    }

    List<Item> items() {
        return items;
    }

    StatementResult result() {
        return result;
    }

    Status status() {
        return status;
    }

    LocalDateTime finishedAt() {
        return finishedAt;
    }

    boolean isActive() {
        return status == Status.QUEUED || status == Status.RUNNING;
    }

    boolean isCancelRequested() {
        return cancelRequested.get();
    }

    void requestCancel() {
        cancelRequested.set(true);
    }

    void markRunning() {
        startedAt = LocalDateTime.now();
        status = Status.RUNNING;
    }

    /** The run itself stopped — the relay refused the login, or a bug. Remaining work is marked failed. */
    void fail(String why) {
        error = why;
        for (Item item : items) {
            if (item.status == ItemStatus.PENDING || item.status == ItemStatus.SENDING) {
                item.failed(why);
            }
        }
        status = Status.FAILED;
    }

    /** Normal end: DONE, or CANCELLED when Cancel was pressed and work was left. */
    void finish() {
        if (status == Status.FAILED) {
            finishedAt = LocalDateTime.now();
            return;
        }
        boolean leftOver = false;
        for (Item item : items) {
            if (item.status == ItemStatus.PENDING || item.status == ItemStatus.SENDING) {
                item.error = "Cancelled before it was reached";
                item.status = ItemStatus.CANCELLED;
                leftOver = true;
            }
        }
        status = leftOver ? Status.CANCELLED : Status.DONE;
        finishedAt = LocalDateTime.now();
    }

    /** Drop the statements; the counts and per-customer outcome stay for the screen. */
    void release() {
        result = null;
        for (Item item : items) {
            item.statement = null;
        }
    }

    StatementMailJobView view() {
        int sent = 0, failed = 0, skipped = 0, cancelled = 0, pending = 0;
        List<StatementMailJobView.Item> views = new ArrayList<>(items.size());
        for (Item item : items) {
            switch (item.status) {
                case SENT -> sent++;
                case FAILED -> failed++;
                case SKIPPED -> skipped++;
                case CANCELLED -> cancelled++;
                default -> pending++;
            }
            views.add(item.view());
        }
        return new StatementMailJobView(id, companyId, status.name(), reminder, requestedBy,
                createdAt, startedAt, finishedAt, error,
                items.size(), sent, failed, skipped, cancelled, pending, views);
    }
}
