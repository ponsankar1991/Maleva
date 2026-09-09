package my.maleva.api.integration.qne;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Runs QNE pushes off the request thread.
 *
 * <p>A QNE create takes minutes. Nothing on our side is slow — one HTTP call,
 * a handful of small reads — so the wait cannot be optimised away; it belongs
 * to QNE. What can be removed is the operator sitting in front of a spinner
 * for the whole of it. The screen now starts the push and gets an answer at
 * once; the push finishes in the background and the row updates when it does.
 *
 * <p>It also means several invoices push <em>at the same time</em>. Pushing
 * five used to be five waits end to end; now they overlap, bounded by
 * {@link #MAX_CONCURRENT} so QNE is never hit with an unbounded burst.
 *
 * <p>Single-flight still applies: the work is submitted through
 * {@link QnePushLock}, so a document already being pushed is never pushed
 * twice — the duplicate-invoice guard survives the move to the background.
 *
 * <p>Results are kept in memory for {@link #RESULT_RETENTION} so the polling
 * screen can collect them. They are a convenience, not a record: the durable
 * outcome is the QNECode written on the document itself, which is what the
 * list reads. A restart loses the tickets, not the pushes' effects.
 */
@Component
@RequiredArgsConstructor
public class QnePushRunner {

    private static final Logger logger = LoggerFactory.getLogger(QnePushRunner.class);

    /** Enough to overlap a batch, few enough not to flood QNE. */
    static final int MAX_CONCURRENT = 4;
    /** How long a finished result stays collectable by the screen. */
    static final Duration RESULT_RETENTION = Duration.ofMinutes(30);

    private final QnePushLock pushLock;

    private final ExecutorService pool = Executors.newFixedThreadPool(MAX_CONCURRENT, new ThreadFactory() {
        private final AtomicInteger counter = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "qne-push-" + counter.getAndIncrement());
            // Daemon: a push in flight must not hold the JVM open at shutdown.
            thread.setDaemon(true);
            return thread;
        }
    });

    private final Map<String, Finished> finished = new ConcurrentHashMap<>();

    /** What starting a push did. */
    public enum StartOutcome {
        /** Accepted; poll {@link #state(String)} for the result. */
        STARTED,
        /** The same document is already being pushed; nothing new was sent. */
        ALREADY_RUNNING
    }

    /** Where a push has got to. */
    public record State(boolean running, QnePushResult result, Duration runningFor) {
        public boolean done() {
            return result != null;
        }
    }

    private record Finished(QnePushResult result, Instant at) {
    }

    /**
     * Starts a push in the background, unless one is already running for the
     * same key.
     *
     * @param key  what is being pushed, e.g. {@code "sale-invoice:43933"}
     * @param work the push itself; must not need the request thread or its
     *             security context, and must handle its own failures by
     *             returning a result rather than throwing
     */
    public StartOutcome start(String key, Supplier<QnePushResult> work) {
        if (!pushLock.tryAcquire(key)) {
            return StartOutcome.ALREADY_RUNNING;
        }
        // Only clear a previous result once this push has really been accepted,
        // so a refused start still shows the last outcome rather than nothing.
        finished.remove(key);
        evictExpired();

        try {
            pool.execute(() -> {
                try {
                    QnePushResult result = work.get();
                    finished.put(key, new Finished(result, Instant.now()));
                    logger.info("QNE push {} finished: {}", key, result.message());
                } catch (RuntimeException ex) {
                    logger.error("QNE push {} failed", key, ex);
                    finished.put(key, new Finished(
                            QnePushResult.rejected("The push failed: " + ex.getMessage()), Instant.now()));
                } finally {
                    pushLock.release(key);
                }
            });
        } catch (RuntimeException ex) {
            // Never leave the key locked because the pool refused the task.
            pushLock.release(key);
            throw ex;
        }
        return StartOutcome.STARTED;
    }

    /**
     * @return the running push, the result of the last finished one, or a
     *         state that is neither when nothing is known about this key
     */
    public State state(String key) {
        Finished done = finished.get(key);
        if (done != null) {
            return new State(false, done.result(), Duration.ZERO);
        }
        Duration runningFor = pushLock.runningFor(key);
        return new State(!runningFor.isZero(), null, runningFor);
    }

    /** Drops the remembered result for a key, once the screen has shown it. */
    public void forget(String key) {
        finished.remove(key);
    }

    private void evictExpired() {
        Instant cutoff = Instant.now().minus(RESULT_RETENTION);
        finished.entrySet().removeIf(entry -> entry.getValue().at().isBefore(cutoff));
    }

    @PreDestroy
    void shutdown() {
        pool.shutdown();
        try {
            // Long enough for a push to land, short enough not to stall a
            // deploy: the threads are daemons, so an overrun cannot hang exit.
            if (!pool.awaitTermination(20, TimeUnit.SECONDS)) {
                logger.warn("QNE pushes still running at shutdown; they were abandoned");
                pool.shutdownNow();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            pool.shutdownNow();
        }
    }

    /** Only for tests: waits until this key has a finished result. */
    State awaitDone(String key, Duration timeout) throws InterruptedException {
        Instant deadline = Instant.now().plus(timeout);
        while (Instant.now().isBefore(deadline)) {
            State state = state(key);
            if (state.done()) {
                return state;
            }
            Thread.sleep(10);
        }
        return state(key);
    }
}
