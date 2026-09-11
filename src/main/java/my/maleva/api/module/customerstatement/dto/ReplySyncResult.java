package my.maleva.api.module.customerstatement.dto;

import java.time.LocalDateTime;

/**
 * What a mailbox scan did, or is doing.
 *
 * @param scanned  new mails looked at
 * @param imported the ones that were statement replies and were stored
 * @param error    why the scan stopped, when it did
 * @param running  a scan is in progress right now
 */
public record ReplySyncResult(
        String mailbox,
        String folder,
        int scanned,
        int imported,
        LocalDateTime ranAt,
        String error,
        boolean running
) {
}
