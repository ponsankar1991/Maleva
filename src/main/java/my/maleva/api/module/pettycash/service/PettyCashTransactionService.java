package my.maleva.api.module.pettycash.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.module.employee.repository.EmployeeMasterRepository;
import my.maleva.api.module.master.entity.SequenceNoMaster;
import my.maleva.api.module.master.repository.SequenceNoMasterRepository;
import my.maleva.api.module.pettycash.dto.PettyCashDetailViewDto;
import my.maleva.api.module.pettycash.dto.PettyCashEditDto;
import my.maleva.api.module.pettycash.dto.PettyCashEditLineDto;
import my.maleva.api.module.pettycash.dto.PettyCashF5ViewDto;
import my.maleva.api.module.pettycash.dto.PettyCashMasterViewDto;
import my.maleva.api.module.pettycash.dto.PettyCashSaveLineDto;
import my.maleva.api.module.pettycash.dto.PettyCashSaveRequestDto;
import my.maleva.api.module.pettycash.dto.PettyCashSaveResponseDto;
import my.maleva.api.module.pettycash.dto.SelectPettyCashRequestDto;
import my.maleva.api.module.pettycash.entity.PettyCashDetail;
import my.maleva.api.module.pettycash.entity.PettyCashMaster;
import my.maleva.api.module.pettycash.repository.PettyCashDetailRepository;
import my.maleva.api.module.pettycash.repository.PettyCashMasterRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The Petty Cash screen's transactional operations — a simplified port of
 * {@code PaymentVoucherTransactionService}'s pattern (sequence numbering,
 * save/edit/search shapes), with no account/classification lookups, no QNE
 * push, and no PendingPayment claiming.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PettyCashTransactionService {

    private static final String SEQUENCE_NAME = "PettyCashMaster";
    private static final DateTimeFormatter GRID_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PettyCashMasterRepository pettyCashMasters;
    private final PettyCashDetailRepository details;
    private final EmployeeMasterRepository employees;
    private final SequenceNoMasterRepository sequences;
    private final NamedParameterJdbcTemplate jdbc;

    /* ── document number ───────────────────────────────────────────── */

    /**
     * The number the next petty cash record will get, for display on a blank
     * screen. Preview only — the real number is assigned inside {@link #save}.
     */
    @Transactional(readOnly = true)
    public String nextPettyCashNumber(Integer companyId) {
        return formatCNumberDisplay(nextSequence(companyId));
    }

    private int nextSequence(Integer companyId) {
        Integer max = sequences.findMaxSequenceNoByCompanyAndSequenceName(companyId, SEQUENCE_NAME);
        return (max == null || max == 0) ? 1 : max + 1;
    }

    /** Legacy format: {@code PTC} followed by a 9-digit running number. */
    static String formatCNumberDisplay(int sequence) {
        return "PTC" + String.format("%09d", sequence);
    }

    /**
     * Records the number just handed out, creating the counter row when it is
     * missing.
     */
    private void recordSequence(Integer companyId, int sequence) {
        SequenceNoMaster counter = sequences
                .findByCompanyRefIdAndSequenceName(companyId, SEQUENCE_NAME)
                .orElseGet(() -> {
                    SequenceNoMaster fresh = new SequenceNoMaster();
                    fresh.setCompanyRefId(companyId);
                    fresh.setSequenceName(SEQUENCE_NAME);
                    fresh.setSequenceDate(LocalDateTime.now());
                    return fresh;
                });
        counter.setSequenceNo(sequence);
        sequences.save(counter);
    }

    /* ── save ──────────────────────────────────────────────────────── */

    /** Insert or update one petty cash record with its lines. */
    @Transactional(rollbackFor = Exception.class)
    public PettyCashSaveResponseDto save(PettyCashSaveRequestDto dto, Integer companyId) {
        Integer employeeRefId = zeroToNull(dto.getEmployeeRefId());
        if (employeeRefId == null) {
            return failure("Select an employee");
        }
        if (!employees.existsByIdAndCompanyRefIdAndActive(employeeRefId, companyId, 1)) {
            return failure("Employee Not Found Issue id" + employeeRefId);
        }

        List<PettyCashSaveLineDto> lines = new ArrayList<>();
        for (PettyCashSaveLineDto line : dto.getPettyCashDetails() == null
                ? List.<PettyCashSaveLineDto>of() : dto.getPettyCashDetails()) {
            if (line != null && (!isBlank(line.getItems())
                    || (line.getAmount() != null && line.getAmount().compareTo(BigDecimal.ZERO) > 0))) {
                lines.add(line);
            }
        }
        if (lines.isEmpty()) {
            return failure("Enter at least one petty cash line with an item or amount");
        }

        LocalDateTime pettyCashDate = parseDateTime(dto.getPettyCashDate());
        if (pettyCashDate == null) {
            return failure("Enter a valid petty cash date");
        }

        boolean isNew = dto.getId() == null || dto.getId() == 0;
        LocalDateTime now = LocalDateTime.now();

        PettyCashMaster master;
        if (isNew) {
            master = new PettyCashMaster();
            master.setCompanyRefId(companyId);
            master.setActive(1);
            master.setCreatedDate(now);
            master.setCreatedBy("System");
            master.setModifiedBy("System");
            master.setCNumber(0);
            master.setCNumberDisplay("");
            // Insert only. SP_PettyCashMaster's UPDATE branch lists every
            // column it writes and EmployeeRefId is not among them, so the
            // employee who raised the slip survives someone else's edit —
            // the same rule Payment Voucher keeps. Unlike Payment Voucher
            // there is no LastEmployeeRefId column to record the editor in.
            master.setEmployeeRefId(employeeRefId);
        } else {
            master = pettyCashMasters.findById(dto.getId())
                    .filter(m -> Objects.equals(m.getCompanyRefId(), companyId))
                    .orElse(null);
            if (master == null) {
                return failure("Petty cash not found: " + dto.getId());
            }
        }

        master.setDepartment(orEmpty(dto.getDepartment()));
        master.setPaymentStatus(orDefault(dto.getPaymentStatus(), "SEND FOR APPROVAL"));
        master.setRemark(dto.getRemark());
        master.setPettyCashDate(pettyCashDate);
        master.setModifiedDate(now);

        BigDecimal total = BigDecimal.ZERO;
        for (PettyCashSaveLineDto line : lines) {
            total = total.add(line.getAmount() == null ? BigDecimal.ZERO : line.getAmount());
        }
        master.setAmount(total.toPlainString());

        master = pettyCashMasters.save(master);

        if (isNew) {
            int sequence = nextSequence(companyId);
            master.setCNumber(sequence);
            master.setCNumberDisplay(formatCNumberDisplay(sequence));
            master = pettyCashMasters.save(master);
            recordSequence(companyId, sequence);
        }

        applyLines(master, lines, now);

        log.info("Petty cash {} saved as {} ({} line(s))",
                master.getId(), master.getCNumberDisplay(), lines.size());
        return PettyCashSaveResponseDto.builder()
                .success(true)
                .message(isNew ? "Petty cash created successfully" : "Petty cash updated successfully")
                .id(master.getId())
                .cNumberDisplay(master.getCNumberDisplay())
                .build();
    }

    /**
     * Replaces the petty cash record's lines with the posted ones — deleted
     * and reinserted wholesale, never updated in place.
     */
    private void applyLines(PettyCashMaster master, List<PettyCashSaveLineDto> lines, LocalDateTime now) {
        List<PettyCashDetail> existing =
                details.findByPettyCashMasterRefIdOrderByIdAsc(master.getId());
        if (!existing.isEmpty()) {
            details.deleteAll(existing);
            details.flush();
        }

        for (PettyCashSaveLineDto line : lines) {
            if (isBlank(line.getItems())
                    && (line.getAmount() == null || line.getAmount().compareTo(BigDecimal.ZERO) == 0)) {
                continue;
            }
            PettyCashDetail row = new PettyCashDetail();
            row.setPettyCashMasterRefId(master.getId());
            row.setItems(line.getItems());
            row.setAmount(line.getAmount() == null ? BigDecimal.ZERO : line.getAmount());
            row.setNotes(line.getNotes());
            // An unset account dropdown posts 0; store it as NULL so the
            // GLAccounts join misses cleanly instead of chasing account 0.
            row.setAccountGroupRefId(zeroToNull(line.getAccountGroupRefId()));
            // Same for the classification picker. Payment Voucher stores a raw
            // 0 on its own rows for historical reasons; petty cash lines predate
            // the column entirely, so there is no 0-means-blank history here and
            // NULL is what "unclassified" means.
            row.setClassification(zeroToNull(line.getClassification()));
            row.setActive(1);
            row.setCreatedDate(now);
            row.setModifiedDate(now);
            details.saveAndFlush(row);
        }
    }

    /* ── edit ──────────────────────────────────────────────────────── */

    /**
     * Loads one petty cash record back into the screen.
     *
     * <p>Deleted records stay closed: legacy's {@code EditPettyCashMaster}
     * filtered {@code Active = 1}, and now that {@link #delete} is a soft
     * delete, dropping that filter would let a deleted slip be reopened and
     * re-saved. (Payment Voucher deliberately omits the filter because a
     * clerk can type a voucher number by hand; petty cash has no such path —
     * it is only ever reached from the grid, which shows live rows only.)
     */
    @Transactional(readOnly = true)
    public Optional<PettyCashEditDto> edit(Integer id, Integer companyId) {
        if (id == null || id == 0) {
            return Optional.empty();
        }
        PettyCashMaster master = pettyCashMasters.findById(id)
                .filter(m -> Objects.equals(m.getCompanyRefId(), companyId))
                .filter(m -> m.getActive() == null || m.getActive() == 1)
                .orElse(null);
        if (master == null) {
            return Optional.empty();
        }

        // Loaded by JDBC rather than through the repository because the labels
        // live elsewhere — in GLAccounts and Classification, the same joins
        // Payment Voucher's edit() uses to resolve AccountGroupRefId and
        // Classification.
        List<PettyCashEditLineDto> lines = jdbc.query(
                "SELECT B.Id, B.Items, B.Amount, B.Notes, B.AccountGroupRefId, B.Classification, "
                        + "ISNULL(AG.GLAccountCode,'') AS AccountCode, "
                        + "ISNULL(AG.Description,'')   AS AccountName, "
                        + "ISNULL(C.Description,'')    AS ClassificationName "
                        + "FROM PettyCashDetail B WITH(NOLOCK) "
                        + "LEFT JOIN GLAccounts AG WITH(NOLOCK) ON AG.RowIndex = B.AccountGroupRefId "
                        + "LEFT JOIN Classification C WITH(NOLOCK) ON C.Id = B.Classification "
                        + "WHERE B.PettyCashMasterRefId = :id ORDER BY B.Id",
                new MapSqlParameterSource("id", master.getId()),
                (rs, i) -> PettyCashEditLineDto.builder()
                        .id(rs.getInt("Id"))
                        .items(rs.getString("Items"))
                        .amount(decimal(rs, "Amount"))
                        .notes(rs.getString("Notes"))
                        .accountGroupRefId(nullableInt(rs, "AccountGroupRefId"))
                        .accountCode(rs.getString("AccountCode"))
                        .accountName(rs.getString("AccountName"))
                        .classification(nullableInt(rs, "Classification"))
                        .classificationName(rs.getString("ClassificationName"))
                        .build());

        return Optional.of(PettyCashEditDto.builder()
                .id(master.getId())
                .companyRefId(master.getCompanyRefId())
                .employeeRefId(master.getEmployeeRefId())
                .cNumber(master.getCNumber())
                .cNumberDisplay(master.getCNumberDisplay())
                .department(master.getDepartment())
                .pettyCashDate(master.getPettyCashDate())
                .sPettyCashDate(master.getPettyCashDate() == null
                        ? "" : GRID_DATE.format(master.getPettyCashDate()))
                .paymentStatus(master.getPaymentStatus())
                .remark(master.getRemark())
                .amount(master.getAmount())
                .pettyCashDetails(lines)
                .build());
    }

    /* ── delete ────────────────────────────────────────────────────── */

    /**
     * Soft-deletes a petty cash record ({@code Active=2}), the same way
     * Payment Voucher does, and only within the caller's own company.
     *
     * <p>Soft rather than hard: the lines are a separate table joined by a
     * plain FK with no cascade, so removing the master row outright would
     * strand every {@code PettyCashDetail} beneath it. {@link #search} already
     * filters on {@code Active = 1}, so a soft-deleted record leaves the grid.
     */
    @Transactional
    public boolean delete(Integer id, Integer companyId) {
        PettyCashMaster master = pettyCashMasters.findById(id)
                .filter(m -> Objects.equals(m.getCompanyRefId(), companyId))
                .orElse(null);
        if (master == null) {
            return false;
        }
        master.setActive(2);
        master.setModifiedDate(LocalDateTime.now());
        pettyCashMasters.save(master);
        log.info("Petty cash {} soft-deleted", id);
        return true;
    }

    /* ── F5 search ─────────────────────────────────────────────────── */

    /** The petty cash F5 grid: matching records plus all of their lines. */
    @Transactional(readOnly = true)
    public PettyCashF5ViewDto search(SelectPettyCashRequestDto request, Integer companyId) {
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("comid", companyId);
        StringBuilder where = new StringBuilder();

        boolean hasSearch = request.getSearch() != null && !request.getSearch().isBlank();
        if (hasSearch) {
            where.append(" AND A.CNumberDisplay = :search ");
            params.addValue("search", request.getSearch().trim());
        } else {
            if (request.getEmployeeId() != null && request.getEmployeeId() != 0) {
                where.append(" AND A.EmployeeRefId = :employeeId ");
                params.addValue("employeeId", request.getEmployeeId());
            }
            if (request.getPaymentStatus() != null && !request.getPaymentStatus().isBlank()) {
                where.append(" AND A.PaymentStatus = :paymentStatus ");
                params.addValue("paymentStatus", request.getPaymentStatus().trim());
            }
            where.append(" AND A.PettyCashDate BETWEEN :fromDate AND :toDate ");
            params.addValue("fromDate", startOfDay(request.getFromDate()));
            params.addValue("toDate", endOfDay(request.getToDate()));
        }

        String masterSql = "SELECT A.Id, A.CNumber, A.CNumberDisplay, A.EmployeeRefId, "
                + "FORMAT(ISNULL(A.PettyCashDate,'1900-01-01'),'dd/MM/yyyy') AS SPettyCashDate, "
                + "ISNULL(em.EmployeeName,'') AS EmployeeName, ISNULL(A.Department,'') AS Department, "
                + "ISNULL(A.Remark,'') AS Remark, ISNULL(A.PaymentStatus,'') AS PaymentStatus, "
                + "TRY_CAST(A.Amount AS DECIMAL(18,2)) AS Amount, A.Status "
                + "FROM PettyCashMaster A WITH(NOLOCK) "
                + "LEFT JOIN EmployeeMaster em WITH(NOLOCK) ON em.Id = A.EmployeeRefId "
                + "WHERE A.CompanyRefId = :comid AND A.Active = 1" + where
                + " ORDER BY A.PettyCashDate, A.Id";

        List<PettyCashMasterViewDto> headers = jdbc.query(masterSql, params, this::mapHeader);

        String detailSql = "SELECT B.Id, B.PettyCashMasterRefId, B.Items, B.Amount, B.Notes, "
                + "B.AccountGroupRefId, B.Classification, "
                + "ISNULL(AG.GLAccountCode,'') AS AccountCode, "
                + "ISNULL(AG.Description,'')   AS AccountName, "
                + "ISNULL(C.Description,'')    AS ClassificationName "
                + "FROM PettyCashDetail B WITH(NOLOCK) "
                + "INNER JOIN PettyCashMaster A WITH(NOLOCK) ON A.Id = B.PettyCashMasterRefId "
                + "LEFT JOIN GLAccounts AG WITH(NOLOCK) ON AG.RowIndex = B.AccountGroupRefId "
                + "LEFT JOIN Classification C WITH(NOLOCK) ON C.Id = B.Classification "
                + "WHERE A.CompanyRefId = :comid AND A.Active = 1" + where;

        List<PettyCashDetailViewDto> lines = jdbc.query(detailSql, params, (rs, i) ->
                PettyCashDetailViewDto.builder()
                        .id(rs.getInt("Id"))
                        .pettyCashMasterRefId(rs.getInt("PettyCashMasterRefId"))
                        .items(rs.getString("Items"))
                        .amount(decimal(rs, "Amount"))
                        .notes(rs.getString("Notes"))
                        .accountGroupRefId(nullableInt(rs, "AccountGroupRefId"))
                        .accountCode(rs.getString("AccountCode"))
                        .accountName(rs.getString("AccountName"))
                        .classification(nullableInt(rs, "Classification"))
                        .classificationName(rs.getString("ClassificationName"))
                        .build());

        BigDecimal total = jdbc.queryForObject(
                "SELECT ISNULL(SUM(TRY_CAST(A.Amount AS DECIMAL(18,2))), 0) "
                        + "FROM PettyCashMaster A WITH(NOLOCK) "
                        + "LEFT JOIN EmployeeMaster em WITH(NOLOCK) ON em.Id = A.EmployeeRefId "
                        + "WHERE A.CompanyRefId = :comid AND A.Active = 1" + where,
                params, BigDecimal.class);

        return PettyCashF5ViewDto.builder()
                .pettyCashMaster(headers)
                .pettyCashDetails(lines)
                .totalAmount(total == null ? BigDecimal.ZERO : total)
                .count(headers.size())
                .build();
    }

    private PettyCashMasterViewDto mapHeader(ResultSet rs, int rowNum) throws SQLException {
        return PettyCashMasterViewDto.builder()
                .id(rs.getInt("Id"))
                .cNumber(rs.getInt("CNumber"))
                .employeeRefId(nullableInt(rs, "EmployeeRefId"))
                .cNumberDisplay(rs.getString("CNumberDisplay"))
                .sPettyCashDate(rs.getString("SPettyCashDate"))
                .employeeName(rs.getString("EmployeeName"))
                .department(rs.getString("Department"))
                .remark(rs.getString("Remark"))
                .paymentStatus(rs.getString("PaymentStatus"))
                .amount(decimal(rs, "Amount"))
                .status(nullableInt(rs, "Status"))
                .build();
    }

    /* ── helpers ───────────────────────────────────────────────────── */

    private static PettyCashSaveResponseDto failure(String message) {
        return PettyCashSaveResponseDto.builder().success(false).message(message).build();
    }

    private static Integer zeroToNull(Integer value) {
        return (value == null || value == 0) ? null : value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String orDefault(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private static Integer nullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private static BigDecimal decimal(ResultSet rs, String column) throws SQLException {
        BigDecimal value = rs.getBigDecimal(column);
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * ISO ({@code yyyy-MM-dd}) is the contract; {@code dd/MM/yyyy} is accepted
     * because that is what the screen shows.
     */
    static LocalDate parseDate(String value) {
        if (isBlank(value)) {
            return null;
        }
        String text = value.trim();
        if (text.length() > 10 && (text.charAt(10) == 'T' || text.charAt(10) == ' ')) {
            text = text.substring(0, 10);
        }
        try {
            return LocalDate.parse(text);
        } catch (Exception ignored) {
            return LocalDate.parse(text, GRID_DATE);
        }
    }

    private static LocalDateTime parseDateTime(String value) {
        LocalDate date = parseDate(value);
        return date == null ? null : date.atStartOfDay();
    }

    private static LocalDateTime startOfDay(String value) {
        LocalDate date = parseDate(value);
        return date == null ? LocalDate.of(1900, 1, 1).atStartOfDay() : date.atStartOfDay();
    }

    private static LocalDateTime endOfDay(String value) {
        LocalDate date = parseDate(value);
        return date == null
                ? LocalDate.of(2999, 12, 31).atTime(23, 59, 59)
                : date.atTime(23, 59, 59);
    }
}
