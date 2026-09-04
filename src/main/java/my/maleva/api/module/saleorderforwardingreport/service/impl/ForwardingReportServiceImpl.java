package my.maleva.api.module.saleorderforwardingreport.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.module.saleorderforwardingreport.dto.ExcelImportResultDto;
import my.maleva.api.module.saleorderforwardingreport.dto.ForwardingDateUpdateRequest;
import my.maleva.api.module.saleorderforwardingreport.dto.ForwardingReportRowDto;
import my.maleva.api.module.saleorderforwardingreport.dto.ForwardingReportSearchRequest;
import my.maleva.api.module.saleorderforwardingreport.dto.ForwardingS1OptionsDto;
import my.maleva.api.module.saleorderforwardingreport.dto.ZbReportRowDto;
import my.maleva.api.module.saleorderforwardingreport.repository.ForwardingReportRepository;
import my.maleva.api.module.saleorderforwardingreport.service.ForwardingReportService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ForwardingReportServiceImpl implements ForwardingReportService {

    private final ForwardingReportRepository repository;

    /* ─── Excel layout ────────────────────────────────────────────────── */

    /**
     * Column positions in the customs acknowledgement sheet, 0-based.
     *
     * <p>These are the same fixed positions the legacy importer read (it used
     * EPPlus' 1-based indexes, hence the letters in the names). The sheet is a
     * customs export with a stable layout, so positional reading is what it is;
     * what is new is {@link #MIN_COLUMNS}, which rejects a sheet too narrow to
     * contain them instead of silently reading blanks out of every row.
     */
    private static final int COL_FORM_TYPE = 1;      // B
    private static final int COL_S1 = 5;             // F
    private static final int COL_ENTER_REF = 7;      // H
    private static final int COL_SMK_NO = 36;        // AK
    private static final int COL_RESPONSE_DATE = 37; // AL
    private static final int COL_MARK_NO = 88;       // CK

    /** The sheet must reach at least the mark-number column to be usable. */
    private static final int MIN_COLUMNS = COL_MARK_NO + 1;

    /** First data row, 0-based — row 1 is the header. */
    private static final int FIRST_DATA_ROW = 1;

    /** How many per-row outcomes to return before truncating. */
    private static final int MAX_DETAIL_ROWS = 500;

    /** The job number is embedded in a longer mark string, e.g. "…MY12345…". */
    private static final Pattern MY_NUMBER = Pattern.compile("MY\\d+");

    private static final DateTimeFormatter SQL_DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final DataFormatter DATA_FORMATTER = new DataFormatter();

    /* ─── Reads ───────────────────────────────────────────────────────── */

    @Override
    public List<ForwardingReportRowDto> searchForwarding(ForwardingReportSearchRequest request) {
        return repository.searchForwarding(request);
    }

    @Override
    public List<ZbReportRowDto> searchZb(ForwardingReportSearchRequest request) {
        return repository.searchZb(request);
    }

    @Override
    public ForwardingS1OptionsDto getS1Options(Integer comId) {
        return repository.findS1Options(comId);
    }

    @Override
    public List<String> getVesselNames(Integer comId) {
        return repository.findVesselNames(comId);
    }

    /* ─── Writes ──────────────────────────────────────────────────────── */

    @Override
    public boolean updateForwardingDate(ForwardingDateUpdateRequest request) {
        int affected = repository.updateForwardingDate(
                request.getComId(),
                request.getJobId(),
                request.getFwNo(),
                request.getForwardingDate());

        // SET NOCOUNT ON is on in this database, so an UPDATE can report -1 rows
        // even when it succeeded. Only an explicit 0 proves nothing matched.
        return affected != 0;
    }

    @Override
    public ExcelImportResultDto importExcel(Integer comId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file selected");
        }
        String name = file.getOriginalFilename();
        if (name != null && !name.toLowerCase().endsWith(".xlsx")) {
            throw new IllegalArgumentException(
                    "Only .xlsx files can be imported; received '" + name + "'");
        }
        try (InputStream stream = file.getInputStream()) {
            return importExcel(comId, stream, name);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Forwarding Excel import failed for comId={}: {}", comId, e.getMessage(), e);
            throw new IllegalStateException("Could not read the spreadsheet: " + e.getMessage(), e);
        }
    }

    /**
     * Apply a customs acknowledgement sheet to the company's sale orders.
     *
     * <p>Each row names a job by its MY number and carries the acknowledgement
     * that belongs on it. The order's three forwarding legs are filled in turn:
     * the row lands on the first leg that is still empty, and an order with all
     * three used is skipped rather than overwritten.
     *
     * <p>Rows are applied one at a time and a failure does not stop the run —
     * that is legacy behaviour and it is the right one here, since one malformed
     * row out of five hundred should not reject the file. What has changed is
     * that every row's fate is now reported back rather than swallowed: the
     * legacy importer returned "Updated Successfully" whether it had written five
     * hundred rows or none at all.
     */
    @Override
    public ExcelImportResultDto importExcel(Integer comId, InputStream stream, String originalFilename) {
        List<ExcelImportResultDto.Outcome> details = new ArrayList<>();
        int total = 0;
        int updated = 0;
        int skipped = 0;
        int failed = 0;

        try (Workbook workbook = new XSSFWorkbook(stream)) {
            Sheet sheet = workbook.getNumberOfSheets() == 0 ? null : workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                throw new IllegalArgumentException("The spreadsheet is empty.");
            }

            Row header = sheet.getRow(sheet.getFirstRowNum());
            if (header == null || header.getLastCellNum() < MIN_COLUMNS) {
                throw new IllegalArgumentException(
                        "This does not look like a customs acknowledgement sheet: it needs at least "
                                + MIN_COLUMNS + " columns (through column CK) but has "
                                + (header == null ? 0 : Math.max(header.getLastCellNum(), 0)) + ".");
            }

            for (int rowIndex = FIRST_DATA_ROW; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (isBlank(row)) continue;

                total++;
                int excelRowNumber = rowIndex + 1;
                ExcelImportResultDto.Outcome outcome;
                try {
                    outcome = applyRow(comId, row, excelRowNumber);
                } catch (Exception rowError) {
                    log.warn("Forwarding import row {} failed: {}", excelRowNumber, rowError.getMessage(), rowError);
                    outcome = ExcelImportResultDto.Outcome.builder()
                            .rowNumber(excelRowNumber)
                            .status(ExcelImportResultDto.Status.FAILED)
                            .reason(rowError.getMessage())
                            .build();
                }

                switch (outcome.getStatus()) {
                    case UPDATED -> updated++;
                    case SKIPPED -> skipped++;
                    case FAILED -> failed++;
                }
                if (details.size() < MAX_DETAIL_ROWS) {
                    details.add(outcome);
                }
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Forwarding Excel import failed for comId={}, file={}: {}",
                    comId, originalFilename, e.getMessage(), e);
            throw new IllegalStateException("Could not read the spreadsheet: " + e.getMessage(), e);
        }

        log.info("Forwarding import for comId={} file={}: {} rows, {} updated, {} skipped, {} failed",
                comId, originalFilename, total, updated, skipped, failed);

        return ExcelImportResultDto.builder()
                .totalRows(total)
                .updatedCount(updated)
                .skippedCount(skipped)
                .failedCount(failed)
                .details(details)
                .detailsTruncated(total > details.size())
                .build();
    }

    /** Apply one spreadsheet row, returning what happened to it. */
    private ExcelImportResultDto.Outcome applyRow(Integer comId, Row row, int excelRowNumber) {
        String markNo = cellText(row, COL_MARK_NO);
        String jobNumber = extractMyNumber(markNo);

        if (jobNumber == null) {
            return skip(excelRowNumber, null,
                    markNo.isEmpty()
                            ? "column CK (mark no) is empty"
                            : "no MY number found in column CK ('" + truncate(markNo) + "')");
        }

        ForwardingReportRepository.ExistingLegs legs = repository.findExistingLegs(comId, jobNumber);
        if (legs == null) {
            return skip(excelRowNumber, jobNumber, "no sale order with job number " + jobNumber);
        }

        Integer leg = legs.firstFreeLeg();
        if (leg == null) {
            return skip(excelRowNumber, jobNumber,
                    "all three forwarding legs on " + jobNumber + " are already filled");
        }

        String formType = cellText(row, COL_FORM_TYPE);
        String s1 = cellText(row, COL_S1);
        String enterRef = cellText(row, COL_ENTER_REF);
        String smkNo = cellText(row, COL_SMK_NO);

        // Legacy stamped "now" when the response date cell was blank, so the leg
        // always got a date. Kept, because a forwarding leg with no date drops
        // out of every date-ranged view on the dashboard and this report.
        LocalDateTime responseDate = cellDateTime(row, COL_RESPONSE_DATE);
        String forwardingDate = (responseDate == null ? LocalDateTime.now() : responseDate)
                .format(SQL_DATE_TIME);

        int affected = repository.applyImportedLeg(
                comId, legs.id, leg, formType, enterRef, s1, smkNo, forwardingDate);

        // SET NOCOUNT ON makes a successful UPDATE report -1 here, so only an
        // explicit 0 means the row went nowhere.
        if (affected == 0) {
            return ExcelImportResultDto.Outcome.builder()
                    .rowNumber(excelRowNumber)
                    .jobNumber(jobNumber)
                    .status(ExcelImportResultDto.Status.FAILED)
                    .reason("the update matched no row for " + jobNumber)
                    .build();
        }

        return ExcelImportResultDto.Outcome.builder()
                .rowNumber(excelRowNumber)
                .jobNumber(jobNumber)
                .status(ExcelImportResultDto.Status.UPDATED)
                .fwNo(leg)
                .build();
    }

    private ExcelImportResultDto.Outcome skip(int rowNumber, String jobNumber, String reason) {
        return ExcelImportResultDto.Outcome.builder()
                .rowNumber(rowNumber)
                .jobNumber(jobNumber)
                .status(ExcelImportResultDto.Status.SKIPPED)
                .reason(reason)
                .build();
    }

    /* ─── Cell reading ────────────────────────────────────────────────── */

    /**
     * A cell's displayed text, trimmed; empty string when missing.
     *
     * <p>Uses POI's {@link DataFormatter} so a numeric cell reads the way it
     * looks in Excel — a reference typed as a number comes back "123456" rather
     * than "123456.0".
     */
    private String cellText(Row row, int column) {
        Cell cell = row.getCell(column, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return "";
        return DATA_FORMATTER.formatCellValue(cell).trim();
    }

    /**
     * A cell as a date-time, whether it is a real Excel date or typed as text.
     *
     * @return null when the cell is empty or cannot be read as a date
     */
    private LocalDateTime cellDateTime(Row row, int column) {
        Cell cell = row.getCell(column, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return null;

        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue();
        }

        String text = DATA_FORMATTER.formatCellValue(cell).trim();
        if (text.isEmpty()) return null;

        for (DateTimeFormatter format : TEXT_DATE_FORMATS) {
            try {
                return LocalDateTime.parse(text, format);
            } catch (Exception ignored) {
                // try the next shape
            }
        }
        try {
            return java.time.LocalDate.parse(text.substring(0, Math.min(10, text.length()))
                    .replace('/', '-')).atStartOfDay();
        } catch (Exception ignored) {
            log.debug("Row {} column {}: '{}' is not a date", row.getRowNum() + 1, column, text);
            return null;
        }
    }

    /** The shapes seen in these sheets when the date column is text, not a date. */
    private static final DateTimeFormatter[] TEXT_DATE_FORMATS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
    };

    /** True when the row is absent or every cell we care about is blank. */
    private boolean isBlank(Row row) {
        if (row == null) return true;
        return cellText(row, COL_MARK_NO).isEmpty()
                && cellText(row, COL_FORM_TYPE).isEmpty()
                && cellText(row, COL_SMK_NO).isEmpty();
    }

    /** The MY number inside a longer mark string, or null when there is none. */
    private String extractMyNumber(String input) {
        if (input == null || input.isEmpty()) return null;
        Matcher matcher = MY_NUMBER.matcher(input);
        return matcher.find() ? matcher.group() : null;
    }

    private String truncate(String value) {
        return value.length() <= 40 ? value : value.substring(0, 40) + "…";
    }
}
