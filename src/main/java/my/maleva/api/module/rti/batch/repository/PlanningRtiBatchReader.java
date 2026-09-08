package my.maleva.api.module.rti.batch.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Every read the "create all RTI" flow needs, one set-based query each.
 *
 * <p>The shape matters as much as the SQL: a plan of 23 rows across 11 trucks is
 * normal here, so anything done per row would be twenty-odd round trips before a
 * single RTI exists. Each method below answers for the whole plan at once.
 */
@Repository
public class PlanningRtiBatchReader {

    private final NamedParameterJdbcTemplate jdbc;

    public PlanningRtiBatchReader(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** The plan's own header, or null when it does not belong to this company. */
    public PlanHeader planHeader(Integer planningId, Integer companyId) {
        List<PlanHeader> found = jdbc.query("""
                SELECT A.Id,
                       ISNULL(A.CNumberDisplay, '') AS PlanningNo,
                       ISNULL(CONVERT(VARCHAR(10), A.SaleDate, 23), '') AS PlanningDate,
                       ISNULL(A.EmployeeRefId, 0) AS EmployeeRefId
                FROM PLANINGMaster A WITH(NOLOCK)
                WHERE A.Id = :planningId
                  AND A.CompanyRefId = :companyId
                  AND A.Active = 1
                """,
                new MapSqlParameterSource()
                        .addValue("planningId", planningId)
                        .addValue("companyId", companyId),
                (rs, i) -> new PlanHeader(
                        rs.getInt("Id"),
                        rs.getString("PlanningNo"),
                        rs.getString("PlanningDate"),
                        rs.getInt("EmployeeRefId")));
        return found.isEmpty() ? null : found.get(0);
    }

    /**
     * Every row of the plan, already joined to its job and to any RTI it is
     * already part of.
     *
     * <p>The RTI lookup rides along as an OUTER APPLY rather than a query per
     * row — it is the duplicate check, and it has to cover the whole plan
     * before the first RTI is written.
     */
    public List<PlanRow> planRows(Integer planningId, Integer companyId) {
        return jdbc.query("""
                SELECT
                    B.Id                                        AS PlanningDetailId,
                    ISNULL(B.SaleOrderMasterRefId, 0)           AS SaleOrderMasterRefId,
                    ISNULL(B.TruckRefid, 0)                     AS TruckRefId,
                    ISNULL(NULLIF(LTRIM(RTRIM(T.TruckName)), ''), ISNULL(B.TruckNameD, '')) AS TruckName,
                    ISNULL(B.DriverRefId, 0)                    AS DriverRefId,
                    ISNULL(NULLIF(LTRIM(RTRIM(B.DriverName)), ''), ISNULL(B.DriverNameD, '')) AS DriverName,
                    ISNULL(B.SortBy, 0)                         AS SortBy,
                    ISNULL(NULLIF(LTRIM(RTRIM(B.OriginD)), ''), ISNULL(SM.Origin, ''))           AS Origin,
                    ISNULL(NULLIF(LTRIM(RTRIM(B.DestinationD)), ''), ISNULL(SM.Destination, '')) AS Destination,
                    ISNULL(CONVERT(VARCHAR(16), ISNULL(B.PickupDateD, SM.PickupDate), 120), '')     AS PickupDate,
                    ISNULL(CONVERT(VARCHAR(16), ISNULL(B.DeliveryDateD, SM.DeliveryDate), 120), '') AS DeliveryDate,
                    ISNULL(CONVERT(VARCHAR(10), ISNULL(B.PickupDateD, ISNULL(SM.PickupDate, A.SaleDate)), 23), '') AS PickupDay,
                    ISNULL(SM.CNumberDisplay, '')               AS JobNo,
                    ISNULL(C.CustomerName, '')                  AS CustomerName,
                    ISNULL(SM.PickupAddress, '')                AS PickupAddress,
                    ISNULL(SM.DeliveryAddress, '')              AS DeliveryAddress,
                    ISNULL(SM.pickuptimelist, '')               AS PickupTimeList,
                    ISNULL(SM.pickupQuantitylist, '')           AS PickupQuantityList,
                    ISNULL(SM.DeliveryQuantitylist, '')         AS DeliveryQuantityList,
                    ISNULL(SM.Delivertimelist, '')              AS DeliveryTimeList,
                    ISNULL(existing.RtiId, 0)                   AS ExistingRtiId,
                    ISNULL(existing.RtiNo, '')                  AS ExistingRtiNo,
                    ISNULL(existing.RtiDate, '')                AS ExistingRtiDate
                FROM PLANINGDetails B WITH(NOLOCK)
                INNER JOIN PLANINGMaster A WITH(NOLOCK) ON A.Id = B.PLANINGMasterRefId
                LEFT JOIN SaleOrderMaster SM WITH(NOLOCK) ON SM.Id = B.SaleOrderMasterRefId
                LEFT JOIN Customer C WITH(NOLOCK) ON C.Id = SM.CustomerRefId
                LEFT JOIN TruckMaster T WITH(NOLOCK) ON T.Id = B.TruckRefid
                OUTER APPLY (
                    SELECT TOP 1 RM.Id AS RtiId, RM.CNumberDisplay AS RtiNo,
                           CONVERT(VARCHAR(10), RM.SaleDate, 23) AS RtiDate
                    FROM RTIDetails RD WITH(NOLOCK)
                    INNER JOIN RTIMaster RM WITH(NOLOCK) ON RM.Id = RD.RTIMasterRefId
                    WHERE RD.SaleOrderMasterRefId = B.SaleOrderMasterRefId
                      AND RM.Active = 1
                    ORDER BY RM.Id DESC
                ) existing
                WHERE A.Id = :planningId
                  AND A.CompanyRefId = :companyId
                ORDER BY ISNULL(B.SortBy, 0), B.Id
                """,
                new MapSqlParameterSource()
                        .addValue("planningId", planningId)
                        .addValue("companyId", companyId),
                (rs, i) -> new PlanRow(
                        rs.getInt("PlanningDetailId"),
                        rs.getInt("SaleOrderMasterRefId"),
                        rs.getInt("TruckRefId"),
                        rs.getString("TruckName"),
                        rs.getInt("DriverRefId"),
                        rs.getString("DriverName"),
                        rs.getInt("SortBy"),
                        rs.getString("Origin"),
                        rs.getString("Destination"),
                        rs.getString("PickupDate"),
                        rs.getString("DeliveryDate"),
                        rs.getString("PickupDay"),
                        rs.getString("JobNo"),
                        rs.getString("CustomerName"),
                        rs.getString("PickupAddress"),
                        rs.getString("DeliveryAddress"),
                        rs.getString("PickupTimeList"),
                        rs.getString("PickupQuantityList"),
                        rs.getString("DeliveryQuantityList"),
                        rs.getString("DeliveryTimeList"),
                        rs.getInt("ExistingRtiId"),
                        rs.getString("ExistingRtiNo"),
                        rs.getString("ExistingRtiDate")));
    }

    /**
     * The driver who last ran each truck, from the RTIs already on record.
     *
     * <p>Most planning rows carry no driver at all — the planner picks one when
     * the RTI is opened. Measured against this company's own history, the truck's
     * previous driver is the same driver about 89% of the time, which makes it a
     * fair suggestion to put in front of a person, and a poor one to save without
     * asking. It is offered, marked, and always overridable.
     */
    public Map<Integer, LastDriver> lastDriverByTruck(Integer companyId, LocalDate since) {
        Map<Integer, LastDriver> byTruck = new HashMap<>();
        jdbc.query("""
                SELECT ranked.TruckRefid AS TruckRefId, ranked.DriverRefid AS DriverRefId, ranked.DriverName, ranked.LastRun
                FROM (
                    SELECT M.TruckRefid, M.DriverRefid, D.DriverName,
                           CONVERT(VARCHAR(10), M.SaleDate, 23) AS LastRun,
                           ROW_NUMBER() OVER (PARTITION BY M.TruckRefid ORDER BY M.SaleDate DESC, M.Id DESC) AS rn
                    FROM RTIMaster M WITH(NOLOCK)
                    INNER JOIN DriverMaster D WITH(NOLOCK) ON D.Id = M.DriverRefid AND D.Active = 1
                    WHERE M.Active = 1
                      AND M.CompanyRefId = :companyId
                      AND ISNULL(M.TruckRefid, 0) > 0
                      AND ISNULL(M.DriverRefid, 0) > 0
                      AND M.SaleDate >= :since
                ) ranked
                WHERE ranked.rn = 1
                """,
                new MapSqlParameterSource()
                        .addValue("companyId", companyId)
                        .addValue("since", java.sql.Date.valueOf(since)),
                rs -> {
                    byTruck.put(rs.getInt("TruckRefId"),
                            new LastDriver(rs.getInt("DriverRefId"), rs.getString("DriverName"), rs.getString("LastRun")));
                });
        return byTruck;
    }

    /**
     * Active drivers for the company — id and name only.
     *
     * <p>Licence, GDL and port-pass dates are deliberately not read. A driver
     * picked on the plan is the driver the planner means, and that data is not
     * kept current enough to argue with them over.
     */
    public List<DriverRow> activeDrivers(Integer companyId) {
        return jdbc.query("""
                SELECT D.Id, ISNULL(D.DriverName, '') AS DriverName
                FROM DriverMaster D WITH(NOLOCK)
                WHERE D.CompanyRefId = :companyId AND D.Active = 1
                """,
                new MapSqlParameterSource("companyId", companyId),
                (rs, i) -> new DriverRow(rs.getInt("Id"), rs.getString("DriverName")));
    }

    /** Active trucks, used to resolve a truck typed by name on a search row. */
    public List<TruckRow> activeTrucks(Integer companyId) {
        return jdbc.query("""
                SELECT T.Id, ISNULL(T.TruckName, '') AS TruckName
                FROM TruckMaster T WITH(NOLOCK)
                WHERE T.CompanyRefId = :companyId AND T.Active = 1
                """,
                new MapSqlParameterSource("companyId", companyId),
                (rs, i) -> new TruckRow(rs.getInt("Id"), rs.getString("TruckName")));
    }

    /**
     * Jobs out of the given set that already sit in an active RTI, re-checked
     * inside the write transaction.
     *
     * <p>The preview's check can go stale between reading and confirming, so it
     * is asked again with the lock held. Without this, two planners pressing at
     * the same moment would both pass and both create.
     */
    public Map<Integer, String> jobsAlreadyInRti(Collection<Integer> saleOrderMasterRefIds) {
        Map<Integer, String> byJob = new HashMap<>();
        if (saleOrderMasterRefIds == null || saleOrderMasterRefIds.isEmpty()) {
            return byJob;
        }
        jdbc.query("""
                SELECT RD.SaleOrderMasterRefId AS JobId,
                       MAX(RM.CNumberDisplay) AS RtiNo,
                       MAX(RM.Id) AS RtiId
                FROM RTIDetails RD WITH(NOLOCK)
                INNER JOIN RTIMaster RM WITH(NOLOCK) ON RM.Id = RD.RTIMasterRefId
                WHERE RM.Active = 1
                  AND RD.SaleOrderMasterRefId IN (:jobIds)
                GROUP BY RD.SaleOrderMasterRefId
                """,
                new MapSqlParameterSource("jobIds", saleOrderMasterRefIds),
                rs -> {
                    byJob.put(rs.getInt("JobId"), rs.getString("RtiNo"));
                });
        return byJob;
    }

    public record PlanHeader(Integer id, String planningNo, String planningDate, Integer employeeRefId) {
    }

    public record PlanRow(
            Integer planningDetailId,
            Integer saleOrderMasterRefId,
            Integer truckRefId,
            String truckName,
            Integer driverRefId,
            String driverName,
            Integer sortBy,
            String origin,
            String destination,
            String pickupDate,
            String deliveryDate,
            String pickupDay,
            String jobNo,
            String customerName,
            String pickupAddress,
            String deliveryAddress,
            String pickupTimeList,
            String pickupQuantityList,
            String deliveryQuantityList,
            String deliveryTimeList,
            Integer existingRtiId,
            String existingRtiNo,
            String existingRtiDate) {
    }

    public record LastDriver(Integer driverRefId, String driverName, String lastRun) {
    }

    public record DriverRow(Integer id, String name) {
    }

    public record TruckRow(Integer id, String name) {
    }
}
