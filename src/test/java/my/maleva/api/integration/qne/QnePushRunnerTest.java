package my.maleva.api.integration.qne;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class QnePushRunnerTest {

    private final QnePushLock lock = new QnePushLock();
    private final QnePushRunner runner = new QnePushRunner(lock);

    private static final String KEY = "sale-invoice:43933";
    private static final Duration PATIENCE = Duration.ofSeconds(10);

    @Test
    void aPushRunsInTheBackgroundAndItsResultIsCollectedOnce() throws Exception {
        QnePushRunner.StartOutcome outcome = runner.start(KEY,
                () -> QnePushResult.ok("guid", "IV-0001", null, "Invoice pushed to QNE as IV-0001"));

        assertThat(outcome).isEqualTo(QnePushRunner.StartOutcome.STARTED);

        QnePushRunner.State done = runner.awaitDone(KEY, PATIENCE);
        assertThat(done.done()).isTrue();
        assertThat(done.result().success()).isTrue();
        assertThat(done.result().qneCode()).isEqualTo("IV-0001");

        // The controller forgets a result once it has handed it over, so the
        // screen is not told the same thing twice.
        runner.forget(KEY);
        assertThat(runner.state(KEY).done()).isFalse();
    }

    @Test
    void theCallerIsNotHeldWhileThePushRuns() throws Exception {
        CountDownLatch release = new CountDownLatch(1);
        long before = System.nanoTime();

        runner.start(KEY, () -> {
            try {
                release.await(PATIENCE.toMillis(), TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return QnePushResult.ok("guid", "IV-0001", null, "done");
        });
        Duration blocked = Duration.ofNanos(System.nanoTime() - before);

        // The whole point: starting a two-minute push must return at once.
        assertThat(blocked).isLessThan(Duration.ofSeconds(2));
        assertThat(runner.state(KEY).running()).isTrue();

        release.countDown();
        assertThat(runner.awaitDone(KEY, PATIENCE).done()).isTrue();
    }

    @Test
    void theSameInvoiceIsNeverPushedTwiceAtOnce() throws Exception {
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger pushes = new AtomicInteger();

        runner.start(KEY, () -> {
            pushes.incrementAndGet();
            try {
                release.await(PATIENCE.toMillis(), TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return QnePushResult.ok("guid", "IV-0001", null, "done");
        });

        // The duplicate-invoice guard has to survive the move to the
        // background — this is the click that used to create a second
        // invoice in QNE.
        QnePushRunner.StartOutcome second = runner.start(KEY,
                () -> QnePushResult.ok("guid2", "IV-0002", null, "second"));

        assertThat(second).isEqualTo(QnePushRunner.StartOutcome.ALREADY_RUNNING);

        release.countDown();
        runner.awaitDone(KEY, PATIENCE);
        assertThat(pushes.get()).isEqualTo(1);
    }

    @Test
    void differentInvoicesPushAtTheSameTime() throws Exception {
        // Four pushes that only finish once all four have started: if they ran
        // one after another this would deadlock and time out.
        CountDownLatch allStarted = new CountDownLatch(QnePushRunner.MAX_CONCURRENT);
        for (int i = 1; i <= QnePushRunner.MAX_CONCURRENT; i++) {
            runner.start("sale-invoice:" + i, () -> {
                allStarted.countDown();
                try {
                    allStarted.await(PATIENCE.toMillis(), TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return QnePushResult.ok("guid", "IV", null, "done");
            });
        }

        assertThat(allStarted.await(PATIENCE.toMillis(), TimeUnit.MILLISECONDS)).isTrue();
        for (int i = 1; i <= QnePushRunner.MAX_CONCURRENT; i++) {
            assertThat(runner.awaitDone("sale-invoice:" + i, PATIENCE).done()).isTrue();
        }
    }

    @Test
    void aPushThatThrowsIsReportedAndUnlocksTheInvoice() throws Exception {
        runner.start(KEY, () -> {
            throw new IllegalStateException("QNE exploded");
        });

        QnePushRunner.State done = runner.awaitDone(KEY, PATIENCE);
        assertThat(done.result().success()).isFalse();
        assertThat(done.result().message()).contains("QNE exploded");

        // A crashed push must not leave the invoice un-pushable for ever.
        runner.forget(KEY);
        assertThat(runner.start(KEY, () -> QnePushResult.ok("g", "c", null, "retry")))
                .isEqualTo(QnePushRunner.StartOutcome.STARTED);
    }

    @Test
    void anUnknownInvoiceIsNeitherRunningNorDone() {
        QnePushRunner.State state = runner.state("sale-invoice:999");

        assertThat(state.running()).isFalse();
        assertThat(state.done()).isFalse();
    }
}
