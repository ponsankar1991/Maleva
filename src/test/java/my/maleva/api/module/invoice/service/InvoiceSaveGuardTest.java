package my.maleva.api.module.invoice.service;

import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.invoice.dto.SaleInvoiceSaveResult;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The rule this guard exists for: one Save is one invoice, however many times
 * the operator presses it.
 */
class InvoiceSaveGuardTest {

    private final InvoiceSaveGuard guard = new InvoiceSaveGuard();

    private static final String KEY = "form-9f2c";

    private SaleInvoiceSaveResult saved() {
        return SaleInvoiceSaveResult.builder()
                .id(44100).billNo("INV000044100").billNumber(44100).created(true).build();
    }

    @Test
    void theFirstSaveOwnsTheKeyAndProceeds() {
        assertThat(guard.begin(KEY)).isEmpty();
    }

    @Test
    void aSecondPressWhileTheFirstIsStillSavingIsRefused() {
        guard.begin(KEY);

        // This is the duplicate: the operator sees nothing happen and presses
        // Save again while the first request is still writing.
        assertThatThrownBy(() -> guard.begin(KEY))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("already being saved");
    }

    @Test
    void aRetryAfterTheSaveFinishedReplaysItInsteadOfSavingAgain() {
        guard.begin(KEY);
        guard.complete(KEY, saved());

        // The reply was lost, so the browser sent the same save again. It must
        // get the invoice that exists, not make a second one.
        SaleInvoiceSaveResult replayed = guard.begin(KEY).orElseThrow();
        assertThat(replayed.getBillNo()).isEqualTo("INV000044100");
        assertThat(replayed.getId()).isEqualTo(44100);
    }

    @Test
    void aFailedSaveReleasesTheKeySoItCanBeTriedAgain() {
        guard.begin(KEY);
        guard.abandon(KEY);

        // Nothing was written, so the next attempt must be allowed to write.
        assertThat(guard.begin(KEY)).isEmpty();
    }

    @Test
    void differentSavesDoNotBlockEachOther() {
        assertThat(guard.begin("form-a")).isEmpty();
        assertThat(guard.begin("form-b")).isEmpty();
    }

    @Test
    void aReplayableResultIsForgottenOnceItIsOldEnough() {
        InvoiceSaveGuard shortLived = new InvoiceSaveGuard(Duration.ofMillis(-1));
        shortLived.begin(KEY);
        shortLived.complete(KEY, saved());

        // Held only long enough for a realistic retry, so the map cannot grow
        // without bound on a busy day.
        assertThat(shortLived.begin(KEY)).isEmpty();
    }

    @Test
    void onlyOneOfManySimultaneousPressesEverSaves() throws Exception {
        int presses = 24;
        ExecutorService pool = Executors.newFixedThreadPool(presses);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(presses);
        AtomicInteger allowed = new AtomicInteger();
        AtomicInteger refused = new AtomicInteger();

        for (int i = 0; i < presses; i++) {
            pool.execute(() -> {
                try {
                    start.await();
                    if (guard.begin(KEY).isEmpty()) {
                        allowed.incrementAndGet();
                    }
                } catch (InvalidRequestException refusedHere) {
                    refused.incrementAndGet();
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
        assertThat(refused.get()).isEqualTo(presses - 1);
    }
}
