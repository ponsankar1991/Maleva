package my.maleva.api.module.ai.planning.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Read-only SQL over the planning tables for the suggestion feature, plus the
 * optional suggestion log. Uses the same join graph as the planning screen's
 * own queries (PLANINGMaster -> PLANINGDetails -> SaleOrderMaster).
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class PlanningHistoryReader {

    private final NamedParameterJdbcTemplate jdbc;

    /** One past planning line. driverId is 0 when the row only carries a typed name. */
    public record HistoryRow(LocalDate planDate, Integer saleOrderMasterRefId, Integer truckId, Integer driverId,
                             String driverName, String originD, String destinationD, Integer customerRefId,
                             String origin, String destination, String sPort, String oPort) {
    }

    /** A planned line on a given day, used for availability and continuity. */
    public record AssignmentRow(Integer planningMasterId, LocalDate planDate, Integer saleOrderMasterRefId,
                                Integer truckId, Integer driverId, String driverName, String originD,
                                String destinationD) {
    }

    private static final String HISTORY_SQL = """
            SELECT CAST(COALESCE(B.PickupDateD, A.SaleDate) AS DATE) AS PlanDate,
                   B.SaleOrderMasterRefId,
                   ISNULL(B.TruckRefid, 0) AS TruckRefid,
                   ISNULL(B.DriverRefId, 0) AS DriverRefId,
                   ISNULL(B.DriverName, '') AS DriverName,
                   ISNULL(B.OriginD, '') AS OriginD,
                   ISNULL(B.DestinationD, '') AS DestinationD,
                   ISNULL(SM.CustomerRefId, 0) AS CustomerRefId,
                   ISNULL(SM.Origin, '') AS Origin,
                   ISNULL(SM.Destination, '') AS Destination,
                   ISNULL(SM.SPort, '') AS SPort,
                   ISNULL(SM.OPort, '') AS OPort
            FROM PLANINGMaster A WITH(NOLOCK)
            INNER JOIN PLANINGDetails B WITH(NOLOCK) ON A.Id = B.PLANINGMasterRefId
            INNER JOIN SaleOrderMaster SM WITH(NOLOCK) ON SM.Id = B.SaleOrderMasterRefId
            WHERE A.CompanyRefId = :companyId
              AND A.Active = 1
              AND CAST(COALESCE(B.PickupDateD, A.SaleDate) AS DATE) BETWEEN :fromDate AND :toDate
            """;

    private static final String ASSIGNMENTS_SQL = """
            SELECT A.Id AS PlanningMasterId,
                   CAST(COALESCE(B.PickupDateD, A.SaleDate) AS DATE) AS PlanDate,
                   B.SaleOrderMasterRefId,
                   ISNULL(B.TruckRefid, 0) AS TruckRefid,
                   ISNULL(B.DriverRefId, 0) AS DriverRefId,
                   ISNULL(B.DriverName, '') AS DriverName,
                   ISNULL(B.OriginD, '') AS OriginD,
                   ISNULL(B.DestinationD, '') AS DestinationD
            FROM PLANINGMaster A WITH(NOLOCK)
            INNER JOIN PLANINGDetails B WITH(NOLOCK) ON A.Id = B.PLANINGMasterRefId
            WHERE A.CompanyRefId = :companyId
              AND A.Active = 1
              AND CAST(COALESCE(B.PickupDateD, A.SaleDate) AS DATE) BETWEEN :fromDate AND :toDate
            ORDER BY PlanDate, B.SortBy, B.Id
            """;

    public List<HistoryRow> history(Integer companyId, LocalDate from, LocalDate to) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("companyId", companyId)
                .addValue("fromDate", from)
                .addValue("toDate", to);
        return jdbc.query(HISTORY_SQL, params, (rs, i) -> new HistoryRow(
                rs.getDate("PlanDate").toLocalDate(),
                rs.getInt("SaleOrderMasterRefId"),
                rs.getInt("TruckRefid"),
                rs.getInt("DriverRefId"),
                rs.getString("DriverName"),
                rs.getString("OriginD"),
                rs.getString("DestinationD"),
                rs.getInt("CustomerRefId"),
                rs.getString("Origin"),
                rs.getString("Destination"),
                rs.getString("SPort"),
                rs.getString("OPort")));
    }

    public List<AssignmentRow> assignments(Integer companyId, LocalDate from, LocalDate to) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("companyId", companyId)
                .addValue("fromDate", from)
                .addValue("toDate", to);
        return jdbc.query(ASSIGNMENTS_SQL, params, (rs, i) -> new AssignmentRow(
                rs.getInt("PlanningMasterId"),
                rs.getDate("PlanDate").toLocalDate(),
                rs.getInt("SaleOrderMasterRefId"),
                rs.getInt("TruckRefid"),
                rs.getInt("DriverRefId"),
                rs.getString("DriverName"),
                rs.getString("OriginD"),
                rs.getString("DestinationD")));
    }

    // ---------------------------------------------------------------------
    // Suggestion log (table AiPlanningSuggestion, created by db/sql/AI_PLANNING_SUGGESTION_TABLE.sql).
    // The feature works without it; a missing table is logged once per call and ignored.
    // ---------------------------------------------------------------------

    public record SuggestionLogRow(Integer saleOrderMasterRefId, Integer suggestedTruckId, Integer suggestedDriverId,
                                   Integer chosenTruckId, Integer chosenDriverId) {
    }

    public int logSuggestions(Integer companyId, LocalDate planningDate, Integer planningMasterId, String user,
                              List<SuggestionLogRow> rows) {
        if (rows.isEmpty()) {
            return 0;
        }
        String sql = """
                INSERT INTO AiPlanningSuggestion
                    (CompanyRefId, PlanningDate, PlanningMasterId, SaleOrderMasterRefId,
                     SuggestedTruckId, SuggestedDriverId, Created_Date, Created_By)
                VALUES (:companyId, :planningDate, :planningMasterId, :saleOrderId, :truckId, :driverId, :now, :user)
                """;
        int count = 0;
        try {
            LocalDateTime now = LocalDateTime.now();
            for (SuggestionLogRow row : rows) {
                jdbc.update(sql, new MapSqlParameterSource()
                        .addValue("companyId", companyId)
                        .addValue("planningDate", planningDate)
                        .addValue("planningMasterId", planningMasterId)
                        .addValue("saleOrderId", row.saleOrderMasterRefId())
                        .addValue("truckId", row.suggestedTruckId())
                        .addValue("driverId", row.suggestedDriverId())
                        .addValue("now", Timestamp.valueOf(now))
                        .addValue("user", user));
                count++;
            }
        } catch (DataAccessException ex) {
            log.warn("Planning suggestion log not written ({}). Run db/sql/AI_PLANNING_SUGGESTION_TABLE.sql to enable it.",
                    rootMessage(ex));
        }
        return count;
    }

    public int recordFeedback(Integer companyId, LocalDate planningDate, Integer planningMasterId,
                              List<SuggestionLogRow> rows) {
        if (rows.isEmpty()) {
            return 0;
        }
        String sql = """
                UPDATE AiPlanningSuggestion
                SET ChosenTruckId = :chosenTruckId,
                    ChosenDriverId = :chosenDriverId,
                    TruckAccepted = CASE WHEN SuggestedTruckId IS NOT NULL AND SuggestedTruckId = :chosenTruckId THEN 1 ELSE 0 END,
                    DriverAccepted = CASE WHEN SuggestedDriverId IS NOT NULL AND SuggestedDriverId = :chosenDriverId THEN 1 ELSE 0 END,
                    PlanningMasterId = COALESCE(:planningMasterId, PlanningMasterId),
                    Decided_Date = :now
                WHERE Id = (
                    SELECT MAX(Id) FROM AiPlanningSuggestion
                    WHERE CompanyRefId = :companyId AND PlanningDate = :planningDate
                      AND SaleOrderMasterRefId = :saleOrderId
                )
                """;
        int count = 0;
        try {
            LocalDateTime now = LocalDateTime.now();
            for (SuggestionLogRow row : rows) {
                count += jdbc.update(sql, new MapSqlParameterSource()
                        .addValue("companyId", companyId)
                        .addValue("planningDate", planningDate)
                        .addValue("planningMasterId", planningMasterId)
                        .addValue("saleOrderId", row.saleOrderMasterRefId())
                        .addValue("chosenTruckId", row.chosenTruckId())
                        .addValue("chosenDriverId", row.chosenDriverId())
                        .addValue("now", Timestamp.valueOf(now)));
            }
        } catch (DataAccessException ex) {
            log.warn("Planning suggestion feedback not written ({}). Run db/sql/AI_PLANNING_SUGGESTION_TABLE.sql to enable it.",
                    rootMessage(ex));
        }
        return Math.max(count, 0);
    }

    private static String rootMessage(Throwable ex) {
        Throwable t = ex;
        while (t.getCause() != null && t.getCause() != t) {
            t = t.getCause();
        }
        return t.getMessage();
    }
}
