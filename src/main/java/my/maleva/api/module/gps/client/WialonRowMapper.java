package my.maleva.api.module.gps.client;

import my.maleva.api.module.gps.dto.GpsEngineHourRow;
import my.maleva.api.module.gps.dto.GpsFillingRow;
import my.maleva.api.module.gps.dto.wialon.WialonResultCell;
import my.maleva.api.module.gps.dto.wialon.WialonResultRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Turns raw Wialon report rows into typed rows.
 *
 * Wialon returns a tree. The "d" field of a row is the number of children at the
 * next nesting level, so d == 0 is a leaf holding real cell values and anything
 * else is a grouping row whose leaves sit in "r". The legacy job only ever
 * descended one level; this walks the tree to any depth, which is what happens
 * when a report groups by both unit and day.
 *
 * Column positions match the legacy ParseFuelFilling / ParseEnginHour methods.
 */
@Component
public class WialonRowMapper {

    private static final Logger logger = LoggerFactory.getLogger(WialonRowMapper.class);

    /** Wialon prints this when a report cell has no value. */
    private static final String EMPTY_MARKER = "-----";

    /**
     * Date formats seen in Wialon output. The engine-hours report renders
     * "dd MMM yyyy HH:mm:ss"; fillings render "dd.MM.yyyy HH:mm:ss". The rest
     * are carried over from the legacy fallback list.
     */
    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ENGLISH));

    // Column indices, fuel-fillings and speedings tables
    private static final int FILL_VEHICLE = 1;
    private static final int FILL_TIME = 2;
    private static final int FILL_LOCATION = 3;
    private static final int FILL_COUNT = 4;
    private static final int FILL_FILLED = 5;
    private static final int FILL_DRIVER = 6;

    // Column indices, unit_engine_hours table
    private static final int ENG_BEGIN_TIME = 1;
    private static final int ENG_BEGIN_LOCATION = 2;
    private static final int ENG_END_TIME = 3;
    private static final int ENG_END_LOCATION = 4;
    private static final int ENG_TOTAL_TIME = 5;
    private static final int ENG_IN_MOTION = 6;
    private static final int ENG_IDLING = 7;
    private static final int ENG_MILEAGE = 8;
    private static final int ENG_CONSUMED_FLS = 9;

    /** Flattens the row tree down to the leaves that actually carry values. */
    public List<WialonResultRow> flatten(List<WialonResultRow> rows) {
        List<WialonResultRow> leaves = new ArrayList<>();
        collectLeaves(rows, leaves, 0);
        return leaves;
    }

    private void collectLeaves(List<WialonResultRow> rows, List<WialonResultRow> out, int depth) {
        if (rows == null || depth > 10) {
            return;
        }
        for (WialonResultRow row : rows) {
            if (row == null) {
                continue;
            }
            boolean isLeaf = row.getChildCount() == null || row.getChildCount() == 0;
            if (isLeaf) {
                out.add(row);
            } else {
                collectLeaves(row.getChildren(), out, depth + 1);
            }
        }
    }

    /** Maps a fuel-fillings or speedings leaf row. Returns null when unusable. */
    public GpsFillingRow toFillingRow(WialonResultRow row) {
        if (!hasCells(row, FILL_DRIVER)) {
            logger.debug("Skipping filling row with too few cells: {}", cellCount(row));
            return null;
        }
        String rawTime = text(row, FILL_TIME);
        return GpsFillingRow.builder()
                .vehicle(text(row, FILL_VEHICLE))
                .rawTime(rawTime)
                .time(parseDate(rawTime))
                .location(text(row, FILL_LOCATION))
                .count(text(row, FILL_COUNT))
                .filled(text(row, FILL_FILLED))
                .driver(text(row, FILL_DRIVER))
                .build();
    }

    /** Maps an engine-hours leaf row. Returns null when unusable. */
    public GpsEngineHourRow toEngineHourRow(WialonResultRow row) {
        if (!hasCells(row, ENG_CONSUMED_FLS)) {
            logger.debug("Skipping engine-hours row with too few cells: {}", cellCount(row));
            return null;
        }
        String rawBegin = text(row, ENG_BEGIN_TIME);
        String rawEnd = text(row, ENG_END_TIME);
        return GpsEngineHourRow.builder()
                .rawBeginTime(rawBegin)
                .beginTime(parseDate(rawBegin))
                .rawEndTime(rawEnd)
                .endTime(parseDate(rawEnd))
                .beginLocation(text(row, ENG_BEGIN_LOCATION))
                .endLocation(text(row, ENG_END_LOCATION))
                .totalTime(text(row, ENG_TOTAL_TIME))
                .inMotion(text(row, ENG_IN_MOTION))
                .idling(text(row, ENG_IDLING))
                .mileage(text(row, ENG_MILEAGE))
                .consumedByFlsInIdleRun(text(row, ENG_CONSUMED_FLS))
                .build();
    }

    /**
     * Parses a Wialon rendered timestamp, trying every known format.
     * Returns null for blanks and for the "-----" placeholder rather than
     * throwing, which is what the legacy job crashed on.
     */
    public LocalDateTime parseDate(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty() || EMPTY_MARKER.equals(trimmed)) {
            return null;
        }
        for (DateTimeFormatter format : DATE_FORMATS) {
            try {
                return LocalDateTime.parse(trimmed, format);
            } catch (DateTimeParseException ignored) {
                // try the next pattern
            }
        }
        logger.warn("Unrecognised Wialon date format: {}", trimmed);
        return null;
    }

    // ---------------------------------------------------------------- internals

    private boolean hasCells(WialonResultRow row, int maxIndex) {
        return row != null && row.getCells() != null && row.getCells().size() > maxIndex;
    }

    private int cellCount(WialonResultRow row) {
        return row == null || row.getCells() == null ? 0 : row.getCells().size();
    }

    private String text(WialonResultRow row, int index) {
        List<WialonResultCell> cells = row.getCells();
        if (cells == null || index >= cells.size() || cells.get(index) == null) {
            return null;
        }
        String value = cells.get(index).getText();
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return EMPTY_MARKER.equals(trimmed) ? null : trimmed;
    }
}
