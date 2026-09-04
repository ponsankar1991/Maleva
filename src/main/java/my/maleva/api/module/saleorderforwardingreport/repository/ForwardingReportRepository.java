package my.maleva.api.module.saleorderforwardingreport.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.module.saleorderforwardingreport.dto.ForwardingReportRowDto;
import my.maleva.api.module.saleorderforwardingreport.dto.ForwardingReportSearchRequest;
import my.maleva.api.module.saleorderforwardingreport.dto.ForwardingS1OptionsDto;
import my.maleva.api.module.saleorderforwardingreport.dto.ZbReportRowDto;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Queries behind the sale order forwarding report.
 *
 * <p>Every filter is a bind parameter. The legacy service built these WHERE
 * clauses by string concatenation, so a job number or a reference containing an
 * apostrophe broke the statement outright — and the same concatenation was
 * reachable from an unauthenticated screen.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ForwardingReportRepository {

    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    /* ─── Forwarding report ───────────────────────────────────────────── */

    /**
     * The forwarding grid: one row per populated leg of each matching order.
     *
     * <p>A sale order stores its three forwarding legs as three parallel sets of
     * columns, so the query selects the same order three times — once per leg —
     * and unions them, relabelling each leg's columns to a common name. `FWNo`
     * carries which leg a row came from.
     *
     * <p>Two legacy filter behaviours look like bugs and are reproduced on
     * purpose, because the desk relies on them:
     * <ul>
     *   <li>A job number <b>replaces</b> every other filter, the date range
     *       included — searching one job finds it whenever it was forwarded.</li>
     *   <li>The SMK box likewise <b>discards</b> the rest of that leg's filters,
     *       and is matched against each leg's own SMK column, so one box finds an
     *       SMK number wherever it sits.</li>
     * </ul>
     */
    public List<ForwardingReportRowDto> searchForwarding(ForwardingReportSearchRequest request) {
        MapSqlParameterSource params = new MapSqlParameterSource("comId", request.getComId());

        List<String> leg1 = new ArrayList<>();
        List<String> leg2 = new ArrayList<>();
        List<String> leg3 = new ArrayList<>();

        if (hasText(request.getCNumberDisplay())) {
            params.addValue("jobNo", request.getCNumberDisplay().trim());
            leg1.add("AND A.CNumberDisplay = :jobNo");
            leg2.add("AND A.CNumberDisplay = :jobNo");
            leg3.add("AND A.CNumberDisplay = :jobNo");
        } else {
            addDateRange(request, params, leg1, leg2, leg3);
            addVesselName(request.getVesselName(), params, leg1, leg2, leg3);

            addEquals(leg1, params, "A.Forwarding", "forwarding", request.getForwarding());
            addEquals(leg2, params, "A.Forwarding2", "forwarding2", request.getForwarding2());
            addEquals(leg3, params, "A.Forwarding3", "forwarding3", request.getForwarding3());

            addEquals(leg1, params, "A.Forwarding1S1", "fw1s1", request.getForwarding1S1());
            addEquals(leg1, params, "A.Forwarding1S2", "fw1s2", request.getForwarding1S2());
            addEquals(leg2, params, "A.Forwarding2S1", "fw2s1", request.getForwarding2S1());
            addEquals(leg2, params, "A.Forwarding2S2", "fw2s2", request.getForwarding2S2());
            addEquals(leg3, params, "A.Forwarding3S1", "fw3s1", request.getForwarding3S1());
            addEquals(leg3, params, "A.Forwarding3S2", "fw3s2", request.getForwarding3S2());

            addEquals(leg1, params, "A.ForwardingEnterRef", "enterRef", request.getForwardingEnterRef());
            addEquals(leg1, params, "A.ForwardingExitRef", "exitRef", request.getForwardingExitRef());
            addEquals(leg2, params, "A.ForwardingEnterRef2", "enterRef2", request.getForwardingEnterRef2());
            addEquals(leg2, params, "A.ForwardingExitRef2", "exitRef2", request.getForwardingExitRef2());
            addEquals(leg3, params, "A.ForwardingEnterRef3", "enterRef3", request.getForwardingEnterRef3());
            addEquals(leg3, params, "A.ForwardingExitRef3", "exitRef3", request.getForwardingExitRef3());

            addIdEquals(leg1, params, "A.SealbyRefid", "sealBy", request.getSealByRefId());
            addIdEquals(leg1, params, "A.SealbreakbyRefid", "sealBreak", request.getSealBreakByRefId());
            addIdEquals(leg2, params, "A.SealbyRefid2", "sealBy2", request.getSealByRefId2());
            addIdEquals(leg2, params, "A.SealbreakbyRefid2", "sealBreak2", request.getSealBreakByRefId2());
            addIdEquals(leg3, params, "A.SealbyRefid3", "sealBy3", request.getSealByRefId3());
            addIdEquals(leg3, params, "A.SealbreakbyRefid3", "sealBreak3", request.getSealBreakByRefId3());

            addEquals(leg2, params, "A.ForwardingSMKNo2", "smk2", request.getForwardingSmkNo2());
            addEquals(leg3, params, "A.ForwardingSMKNo3", "smk3", request.getForwardingSmkNo3());
        }

        // The SMK box replaces, rather than joins, whatever was collected above.
        if (hasText(request.getForwardingSmkNo())) {
            params.addValue("smk", request.getForwardingSmkNo().trim());
            leg1.clear();
            leg2.clear();
            leg3.clear();
            leg1.add("AND A.ForwardingSMKNo = :smk");
            leg2.add("AND A.ForwardingSMKNo2 = :smk");
            leg3.add("AND A.ForwardingSMKNo3 = :smk");
        }

        if (hasText(request.getForwardingS1Search())) {
            params.addValue("s1Search", request.getForwardingS1Search().trim());
            leg1.add("AND A.Forwarding1S1 = :s1Search");
            leg2.add("AND A.Forwarding2S1 = :s1Search");
            leg3.add("AND A.Forwarding3S1 = :s1Search");
        }

        String sql = legSelect(1, String.join(" ", leg1))
                + " UNION ALL "
                + legSelect(2, String.join(" ", leg2))
                + " UNION ALL "
                + legSelect(3, String.join(" ", leg3))
                + " ORDER BY SaleDate";

        return namedJdbcTemplate.query(sql, params, FORWARDING_ROW_MAPPER);
    }

    /**
     * One leg of the union.
     *
     * <p>{@code leg} selects the column suffix — leg 1's columns are unsuffixed,
     * legs 2 and 3 carry the matching digit. It is an int chosen by this class,
     * never anything a caller supplied, so interpolating it into the SQL is safe.
     */
    private String legSelect(int leg, String extraWhere) {
        String n = leg == 1 ? "" : String.valueOf(leg);
        // Leg 1's S-columns are Forwarding1S1/Forwarding1S2; legs 2 and 3 follow
        // the leg number. So the digit is always the leg, unlike the columns above.
        String s = String.valueOf(leg);
        String alias = "SB" + leg;
        String breakAlias = "BS" + leg;

        return """
                SELECT A.Id,
                       %d AS FWNo,
                       A.Original,
                       A.SaleDate,
                       A.CNumberDisplay,
                       FORMAT(ISNULL(A.SaleDate, '1900-01-01'), 'dd/MM/yyyy') AS SaleDateDisplay,
                       FORMAT(ISNULL(A.Forwarding%sDate, '1900-01-01'), 'dd/MM/yyyy') AS ForwardingDateDisplay,
                       ISNULL(CONVERT(VARCHAR(26), A.Forwarding%sDate, 20), '') AS ForwardingDate,
                       A.Forwarding%s AS Forwarding,
                       A.ForwardingEnterRef%s AS ForwardingEnterRef,
                       A.ForwardingExitRef%s AS ForwardingExitRef,
                       A.ForwardingSMKNo%s AS ForwardingSMKNo,
                       A.SealbyRefid%s AS SealbyRefid,
                       A.SealbreakbyRefid%s AS SealbreakbyRefid,
                       A.Forwarding%sS1 AS ForwardingS1,
                       A.Forwarding%sS2 AS ForwardingS2,
                       %s.EmployeeName AS SealByEmployee,
                       %s.EmployeeName AS BreakSealEmployee,
                       JT.Name AS JobType
                FROM SaleOrderMaster A WITH(NOLOCK)
                LEFT JOIN EmployeeMaster %s WITH(NOLOCK) ON %s.Id = A.SealbyRefid%s
                LEFT JOIN EmployeeMaster %s WITH(NOLOCK) ON %s.Id = A.SealbreakbyRefid%s
                LEFT JOIN JobTypeMaster JT WITH(NOLOCK) ON JT.Id = A.JobMasterRefId
                WHERE A.CompanyRefId = :comId AND A.Active = 1 %s
                """.formatted(
                leg,
                n, n,           // Forwarding<n>Date twice
                n, n, n, n,     // Forwarding, EnterRef, ExitRef, SMKNo
                n, n,           // Sealby, Sealbreakby
                s, s,           // Forwarding<s>S1 / S2
                alias, breakAlias,
                alias, alias, n,
                breakAlias, breakAlias, n,
                extraWhere);
    }

    private static final RowMapper<ForwardingReportRowDto> FORWARDING_ROW_MAPPER = (rs, rowNum) ->
            ForwardingReportRowDto.builder()
                    .id(rs.getInt("Id"))
                    .fwNo(rs.getInt("FWNo"))
                    .original(readNullableBoolean(rs, "Original"))
                    .cNumberDisplay(rs.getString("CNumberDisplay"))
                    .saleDateDisplay(rs.getString("SaleDateDisplay"))
                    .forwardingDateDisplay(rs.getString("ForwardingDateDisplay"))
                    .forwardingDate(rs.getString("ForwardingDate"))
                    .forwarding(rs.getString("Forwarding"))
                    .forwardingEnterRef(rs.getString("ForwardingEnterRef"))
                    .forwardingExitRef(rs.getString("ForwardingExitRef"))
                    .forwardingSmkNo(rs.getString("ForwardingSMKNo"))
                    .forwardingS1(rs.getString("ForwardingS1"))
                    .forwardingS2(rs.getString("ForwardingS2"))
                    .sealByRefId(readNullableInt(rs, "SealbyRefid"))
                    .sealBreakByRefId(readNullableInt(rs, "SealbreakbyRefid"))
                    .sealByEmployee(rs.getString("SealByEmployee"))
                    .breakSealEmployee(rs.getString("BreakSealEmployee"))
                    .jobType(rs.getString("JobType"))
                    .build();

    /* ─── ZB report ───────────────────────────────────────────────────── */

    /**
     * The ZB grid: one row per sale order, both ZB slots as columns.
     *
     * <p>Note this filters on `SaleDate`, not on a forwarding date — the ZB tab
     * is about when the order was raised, which is why switching tabs can change
     * the row count for the same date range.
     */
    public List<ZbReportRowDto> searchZb(ForwardingReportSearchRequest request) {
        MapSqlParameterSource params = new MapSqlParameterSource("comId", request.getComId());
        List<String> where = new ArrayList<>();

        if (hasText(request.getCNumberDisplay())) {
            params.addValue("jobNo", request.getCNumberDisplay().trim());
            where.add("AND A.CNumberDisplay = :jobNo");
        } else {
            LocalDate from = parseDate(request.getFromDate());
            LocalDate to = parseDate(request.getToDate());
            if (from != null && to != null) {
                params.addValue("saleFrom", from.toString() + " 00:00:00");
                params.addValue("saleTo", to.toString() + " 23:59:59");
                where.add("AND A.SaleDate BETWEEN :saleFrom AND :saleTo");
            }
            if (hasText(request.getVesselName())) {
                params.addValue("vessel", request.getVesselName().trim());
                where.add("AND (A.Offvesselname = :vessel OR A.Loadingvesselname = :vessel)");
            }
            addEquals(where, params, "A.Zb", "zb", request.getZb());
            addEquals(where, params, "A.Zb2", "zb2", request.getZb2());
            addEquals(where, params, "A.ZbRef", "zbRef", request.getZbRef());
            addEquals(where, params, "A.ZbRef2", "zbRef2", request.getZbRef2());
        }

        String sql = """
                SELECT A.Id,
                       A.SaleDate,
                       A.CNumberDisplay,
                       FORMAT(ISNULL(A.SaleDate, '1900-01-01'), 'dd/MM/yyyy') AS SaleDateDisplay,
                       A.Zb, A.Zb2, A.ZbRef, A.ZbRef2,
                       JT.Name AS JobType
                FROM SaleOrderMaster A WITH(NOLOCK)
                LEFT JOIN JobTypeMaster JT WITH(NOLOCK) ON JT.Id = A.JobMasterRefId
                WHERE A.CompanyRefId = :comId AND A.Active = 1 %s
                ORDER BY A.SaleDate
                """.formatted(String.join(" ", where));

        return namedJdbcTemplate.query(sql, params, (rs, rowNum) -> ZbReportRowDto.builder()
                .id(rs.getInt("Id"))
                .cNumberDisplay(rs.getString("CNumberDisplay"))
                .saleDateDisplay(rs.getString("SaleDateDisplay"))
                .zb(rs.getString("Zb"))
                .zbRef(rs.getString("ZbRef"))
                .zb2(rs.getString("Zb2"))
                .zbRef2(rs.getString("ZbRef2"))
                .jobType(rs.getString("JobType"))
                .build());
    }

    /* ─── Filter dropdowns ────────────────────────────────────────────── */

    /** The six S1/S2 option lists, one query each, as legacy did. */
    public ForwardingS1OptionsDto findS1Options(Integer comId) {
        return ForwardingS1OptionsDto.builder()
                .forwarding1S1(distinctValues("Forwarding1S1", comId))
                .forwarding1S2(distinctValues("Forwarding1S2", comId))
                .forwarding2S1(distinctValues("Forwarding2S1", comId))
                .forwarding2S2(distinctValues("Forwarding2S2", comId))
                .forwarding3S1(distinctValues("Forwarding3S1", comId))
                .forwarding3S2(distinctValues("Forwarding3S2", comId))
                .build();
    }

    /**
     * @param column one of the six S-column names, chosen by {@link #findS1Options}
     *               and never taken from a caller, so interpolation is safe here.
     */
    private List<String> distinctValues(String column, Integer comId) {
        String sql = """
                SELECT DISTINCT TRIM(A.%s) AS Value
                FROM SaleOrderMaster A WITH(NOLOCK)
                WHERE A.%s IS NOT NULL AND TRIM(A.%s) <> ''
                  AND A.Active <> 2 AND A.CompanyRefId = :comId
                ORDER BY Value
                """.formatted(column, column, column);

        return namedJdbcTemplate.queryForList(
                sql, new MapSqlParameterSource("comId", comId), String.class);
    }

    /** Vessel names seen on either the offloading or the loading side. */
    public List<String> findVesselNames(Integer comId) {
        String sql = """
                SELECT DISTINCT TRIM(A.Offvesselname) AS VesselName
                FROM SaleOrderMaster A WITH(NOLOCK)
                WHERE A.Offvesselname IS NOT NULL AND TRIM(A.Offvesselname) <> ''
                  AND A.Active <> 2 AND A.CompanyRefId = :comId
                UNION
                SELECT DISTINCT TRIM(A.Loadingvesselname) AS VesselName
                FROM SaleOrderMaster A WITH(NOLOCK)
                WHERE A.Loadingvesselname IS NOT NULL AND TRIM(A.Loadingvesselname) <> ''
                  AND A.Active <> 2 AND A.CompanyRefId = :comId
                ORDER BY VesselName
                """;

        return namedJdbcTemplate.queryForList(
                sql, new MapSqlParameterSource("comId", comId), String.class);
    }

    /* ─── Writes ──────────────────────────────────────────────────────── */

    /**
     * Re-date one leg. Returns the number of rows the UPDATE matched.
     *
     * <p>{@code fwNo} is validated by the request DTO before it reaches here and
     * re-checked below, so the column name is never caller-controlled.
     */
    public int updateForwardingDate(Integer comId, Integer jobId, int fwNo, String forwardingDate) {
        String column = switch (fwNo) {
            case 1 -> "ForwardingDate";
            case 2 -> "Forwarding2Date";
            case 3 -> "Forwarding3Date";
            default -> throw new IllegalArgumentException("fwNo must be 1, 2 or 3 but was " + fwNo);
        };

        String sql = """
                UPDATE SaleOrderMaster
                SET Modified_Date = GETDATE(), %s = :forwardingDate
                WHERE Id = :jobId AND CompanyRefId = :comId
                """.formatted(column);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("forwardingDate", hasText(forwardingDate) ? forwardingDate.trim() : null)
                .addValue("jobId", jobId)
                .addValue("comId", comId);

        return namedJdbcTemplate.update(sql, params);
    }

    /** The forwarding legs already in use on one order, for the Excel import. */
    public ExistingLegs findExistingLegs(Integer comId, String jobNumber) {
        String sql = """
                SELECT TOP 1 Id, Forwarding, Forwarding2, Forwarding3
                FROM SaleOrderMaster WITH(NOLOCK)
                WHERE CNumberDisplay = :jobNumber AND CompanyRefId = :comId
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("jobNumber", jobNumber)
                .addValue("comId", comId);

        List<ExistingLegs> rows = namedJdbcTemplate.query(sql, params, (rs, rowNum) -> {
            ExistingLegs legs = new ExistingLegs();
            legs.id = rs.getInt("Id");
            legs.forwarding = rs.getString("Forwarding");
            legs.forwarding2 = rs.getString("Forwarding2");
            legs.forwarding3 = rs.getString("Forwarding3");
            return legs;
        });

        return rows.isEmpty() ? null : rows.get(0);
    }

    /** Which of an order's three forwarding legs are already filled. */
    public static class ExistingLegs {
        public Integer id;
        public String forwarding;
        public String forwarding2;
        public String forwarding3;

        /** The first free leg (1, 2 or 3), or null when all three are used. */
        public Integer firstFreeLeg() {
            if (!hasText(forwarding)) return 1;
            if (!hasText(forwarding2)) return 2;
            if (!hasText(forwarding3)) return 3;
            return null;
        }
    }

    /**
     * Fill one forwarding leg from an imported spreadsheet row.
     *
     * @param leg 1, 2 or 3, from {@link ExistingLegs#firstFreeLeg()}
     * @return rows affected — see the caveat below
     */
    public int applyImportedLeg(Integer comId,
                                Integer saleOrderId,
                                int leg,
                                String formType,
                                String enterRef,
                                String s1,
                                String smkNo,
                                String forwardingDate) {
        String n = leg == 1 ? "" : String.valueOf(leg);
        String s = String.valueOf(leg);

        String sql = """
                UPDATE SaleOrderMaster
                SET Modified_Date = GETDATE(),
                    Forwarding%s = :formType,
                    ForwardingEnterRef%s = :enterRef,
                    Forwarding%sS1 = :s1,
                    ForwardingSMKNo%s = :smkNo,
                    Forwarding%sDate = :forwardingDate
                WHERE Id = :saleOrderId AND CompanyRefId = :comId
                """.formatted(n, n, s, n, n);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("formType", formType)
                .addValue("enterRef", enterRef)
                .addValue("s1", s1)
                .addValue("smkNo", smkNo)
                .addValue("forwardingDate", forwardingDate)
                .addValue("saleOrderId", saleOrderId)
                .addValue("comId", comId);

        return namedJdbcTemplate.update(sql, params);
    }

    /* ─── Helpers ─────────────────────────────────────────────────────── */

    private void addDateRange(ForwardingReportSearchRequest request,
                              MapSqlParameterSource params,
                              List<String> leg1, List<String> leg2, List<String> leg3) {
        LocalDate from = parseDate(request.getFromDate());
        LocalDate to = parseDate(request.getToDate());
        if (from == null || to == null) return;

        // Half-open upper bound rather than `<= toDate`: the forwarding columns
        // are datetimes, so a same-day 14:30 row is inside `< to + 1 day` but
        // outside `<= to`, which resolves to midnight.
        params.addValue("fromDate", from.toString());
        params.addValue("toDateExclusive", to.plusDays(1).toString());

        leg1.add("AND A.ForwardingDate >= :fromDate AND A.ForwardingDate < :toDateExclusive");
        leg2.add("AND A.Forwarding2Date >= :fromDate AND A.Forwarding2Date < :toDateExclusive");
        leg3.add("AND A.Forwarding3Date >= :fromDate AND A.Forwarding3Date < :toDateExclusive");
    }

    private void addVesselName(String vesselName,
                               MapSqlParameterSource params,
                               List<String> leg1, List<String> leg2, List<String> leg3) {
        if (!hasText(vesselName)) return;
        params.addValue("vessel", vesselName.trim());
        String clause = "AND (A.Offvesselname = :vessel OR A.Loadingvesselname = :vessel)";
        leg1.add(clause);
        leg2.add(clause);
        leg3.add(clause);
    }

    private void addEquals(List<String> clauses, MapSqlParameterSource params,
                           String column, String param, String value) {
        if (!hasText(value)) return;
        params.addValue(param, value.trim());
        clauses.add("AND " + column + " = :" + param);
    }

    /** Legacy treated 0 as "no filter" for the seal employee ids; so does this. */
    private void addIdEquals(List<String> clauses, MapSqlParameterSource params,
                             String column, String param, Integer value) {
        if (value == null || value == 0) return;
        params.addValue(param, value);
        clauses.add("AND " + column + " = :" + param);
    }

    /** Accepts `yyyy-MM-dd` and the legacy `yyyy/MM/dd`; null on anything else. */
    private LocalDate parseDate(String value) {
        if (!hasText(value)) return null;
        String normalised = value.trim().replace('/', '-');
        if (normalised.length() > 10) normalised = normalised.substring(0, 10);
        try {
            return LocalDate.parse(normalised);
        } catch (DateTimeParseException e) {
            log.warn("Unparseable date '{}' on the forwarding report; filter dropped", value);
            return null;
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static Integer readNullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    /**
     * `Original` is a bit column on some databases and an int on others; both
     * read cleanly through getBoolean, but a NULL must stay null rather than
     * become false, because the grid colours only explicit originals.
     */
    private static Boolean readNullableBoolean(ResultSet rs, String column) throws SQLException {
        boolean value = rs.getBoolean(column);
        return rs.wasNull() ? null : value;
    }
}
