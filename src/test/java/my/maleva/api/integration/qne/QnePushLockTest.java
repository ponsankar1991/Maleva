package my.maleva.api.integration.qne;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class QnePushLockTest {

    private final QnePushLock lock = new QnePushLock();

    @Test
    void theFirstPushOfADocumentIsAllowed() {
        assertThat(lock.tryAcquire("sale-invoice:43933")).isTrue();
    }

    @Test
    void aSecondPushIsRefusedWhileTheFirstIsStillRunning() {
        assertThat(lock.tryAcquire("sale-invoice:43933")).isTrue();

        // This is the duplicate: the operator clicking again after the browser
        // timed out, while QNE is still working on the first call.
        assertThat(lock.tryAcquire("sale-invoice:43933")).isFalse();
    }

    @Test
    void aRetryIsAllowedOnceTheFirstPushHasFinished() {
        assertThat(lock.tryAcquire("sale-invoice:43933")).isTrue();
        lock.release("sale-invoice:43933");

        assertThat(lock.tryAcquire("sale-invoice:43933")).isTrue();
    }

    @Test
    void differentInvoicesDoNotBlockEachOther() {
        assertThat(lock.tryAcquire("sale-invoice:1")).isTrue();
        assertThat(lock.tryAcquire("sale-invoice:2")).isTrue();
        assertThat(lock.tryAcquire("receipt:1")).isTrue();
    }

    @Test
    void anAbandonedPushStopsBlockingRetriesAfterItsTimeout() throws Exception {
        QnePushLock shortLived = new QnePushLock(Duration.ofMillis(40));
        assertThat(shortLived.tryAcquire("sale-invoice:43933")).isTrue();
        assertThat(shortLived.tryAcquire("sale-invoice:43933")).isFalse();

        Thread.sleep(60);

        // A push that can no longer be running must never block the document
        // for ever — otherwise a crashed call would make it un-pushable.
        assertThat(shortLived.tryAcquire("sale-invoice:43933")).isTrue();
    }

    @Test
    void releasingAKeyNobodyHoldsIsHarmless() {
        lock.release("sale-invoice:999");

        assertThat(lock.tryAcquire("sale-invoice:999")).isTrue();
    }

    @Test
    void onlyOneOfManySimultaneousClicksGetsThrough() throws Exception {
        int clicks = 16;
        ExecutorService pool = Executors.newFixedThreadPool(clicks);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(clicks);
        AtomicInteger allowed = new AtomicInteger();

        for (int i = 0; i < clicks; i++) {
            pool.execute(() -> {
                try {
                    start.await();
                    if (lock.tryAcquire("sale-invoice:43933")) {
                        allowed.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        assertThat(done.await(10, TimeUnit.SECONDS)).isTrue();
        pool.shutdownNow();

        assertThat(allowed.get()).isEqualTo(1);
    }

    @Test
    void theStaleWindowOutlastsTheClientsOwnReadTimeout() {
        // Otherwise a slow-but-live push would be declared abandoned and a
        // second one let through — which is the duplicate this guard exists
        // to prevent.
        assertThat(QnePushLock.STALE_AFTER).isGreaterThan(QneClient.READ_TIMEOUT);
    }
}
