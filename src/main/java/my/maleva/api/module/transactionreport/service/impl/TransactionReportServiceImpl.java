package my.maleva.api.module.transactionreport.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.module.transactionreport.dto.PaymentDoneRequestDto;
import my.maleva.api.module.transactionreport.dto.PaymentDoneRowDto;
import my.maleva.api.module.transactionreport.dto.PaymentDoneViewDto;
import my.maleva.api.module.transactionreport.repository.TransactionReportRepository;
import my.maleva.api.module.transactionreport.service.TransactionReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionReportServiceImpl implements TransactionReportService {

    private final TransactionReportRepository repository;

    /** {@code dd/MM/yyyy}, the format the legacy date pickers posted. */
    private static final Pattern DMY = Pattern.compile("^(\\d{2})/(\\d{2})/(\\d{4})$");

    /** {@code yyyy-MM-dd}, optionally with a time part this method discards. */
    private static final Pattern ISO = Pattern.compile("^(\\d{4})-(\\d{2})-(\\d{2}).*$");

    @Override
    public PaymentDoneViewDto getCompletedPayments(PaymentDoneRequestDto request) {
        // Both bounds are widened to whole days, matching legacy — it bracketed
        // the range with 00:00:00 / 23:59:59 so a payment stamped late on the
        // To date still counted. The columns are datetime, so a row landing in
        // the final fraction of a second is excluded; that is the original's
        // behaviour and the already-migrated dashboard copy keeps it too.
        String from = toDate(request.getFromDate(), LocalDate.now().withDayOfMonth(1)) + " 00:00:00";
        String to = toDate(request.getToDate(), LocalDate.now()) + " 23:59:59";

        List<String> descriptions = normalizeDescriptions(request.getDescriptions());

        List<PaymentDoneRowDto> rows = repository.findCompletedPayments(
                request.getComid(), from, to, request.getSupplierId(), request.getPayTo(), descriptions);

        BigDecimal total = repository.sumCompletedPayments(
                request.getComid(), from, to, request.getSupplierId(), request.getPayTo(), descriptions);

        log.debug("Payment Completed: comid={} {}..{} categories={} -> {} rows, total {}",
                request.getComid(), from, to, descriptions.size(), rows.size(), total);

        return PaymentDoneViewDto.builder()
                .rows(rows)
                .totalAmount(total)
                .count(rows.size())
                .build();
    }

    /**
     * Drops blanks and duplicates so an all-blank category list reads as "no
     * category filter" rather than as {@code Description IN ('')}, which would
     * match nothing and look like an empty month.
     */
    private static List<String> normalizeDescriptions(List<String> descriptions) {
        if (descriptions == null) return List.of();
        return descriptions.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
    }

    /** Accepts {@code yyyy-MM-dd} or {@code dd/MM/yyyy}; falls back rather than throwing. */
    private static String toDate(String value, LocalDate fallback) {
        String text = value == null ? "" : value.trim();
        if (text.isEmpty()) return fallback.toString();

        Matcher iso = ISO.matcher(text);
        if (iso.matches()) return iso.group(1) + "-" + iso.group(2) + "-" + iso.group(3);

        Matcher dmy = DMY.matcher(text);
        if (dmy.matches()) return dmy.group(3) + "-" + dmy.group(2) + "-" + dmy.group(1);

        log.warn("Unrecognised date '{}' on the Payment Completed report; using {}", text, fallback);
        return fallback.toString();
    }
}
