package my.maleva.api.module.customerstatement.reply;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Turns Spring scheduling on for the mailbox reader, gated on its own flag
 * — the same way the GPS sync does it — so switching the reader off also
 * leaves scheduling untouched for everything else.
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "mail.statement.replies", name = "enabled", havingValue = "true", matchIfMissing = true)
public class StatementReplySchedulingConfig {
}
