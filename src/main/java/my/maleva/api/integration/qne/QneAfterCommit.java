package my.maleva.api.integration.qne;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Runs a QNE push only after the local transaction has committed.
 *
 * <p>The legacy ordering is contract: the row was committed by its stored
 * procedure <em>before</em> the QNE call, so a QNE failure leaves a committed
 * row with an empty QNE code for the reconcile jobs to repair — never a QNE
 * document without a local row. Calling out mid-transaction would invert that
 * (and pin a DB connection under {@link QneClient}'s 30-minute read timeout).
 *
 * <p>Data access inside the callback still sees the original transaction's
 * resources, which is why the id write-back repository methods run in
 * {@code REQUIRES_NEW}.
 */
public final class QneAfterCommit {

    private QneAfterCommit() {
    }

    public static void run(Runnable push) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    push.run();
                }
            });
        } else {
            push.run();
        }
    }
}
