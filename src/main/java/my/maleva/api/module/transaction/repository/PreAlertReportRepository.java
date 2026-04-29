package my.maleva.api.module.transaction.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.module.transaction.dto.PreAlertReportModel;
import my.maleva.api.module.transaction.dto.PreAlertSearchModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for Pre-Alert Report database operations
 * Handles complex SQL queries for pre-alert data retrieval
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class PreAlertReportRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Get Pre-Alert Report data based on search criteria
     * Mirrors legacy PreAlertReportView method from C# implementation
     *
     * @param searchModel Filter criteria
     * @return List of PreAlertReportModel
     */
    public List<PreAlertReportModel> getPreAlertReportData(PreAlertSearchModel searchModel) {
        try {
            // Build dynamic query based on filters
            String query = buildQuery(searchModel);
            List<Object> params = buildParams(searchModel);

            log.debug("Executing Pre-Alert query with {} parameters", params.size());
            log.debug("Query: {}", query);

            List<PreAlertReportModel> results = jdbcTemplate.query(
                    query,
                    params.toArray(),
                    new BeanPropertyRowMapper<>(PreAlertReportModel.class)
            );

            log.info("Pre-Alert query returned {} records for comId={}", results.size(), searchModel.getComId());
            return results;

        } catch (Exception e) {
            log.error("Error executing pre-alert report query: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch pre-alert report data", e);
        }
    }

    /**
     * Build the SQL query based on search criteria
     */
    private String buildQuery(PreAlertSearchModel searchModel) {
        // Build SELECT clause with date formatting
        String select = buildSelectClause(searchModel);

        // Base FROM and JOIN clauses
        String from = buildFromClause();

        // Build WHERE clause based on filters
        String where = buildWhereClause(searchModel);

        // Build ORDER BY clause
        String orderBy = buildOrderByClause(searchModel);

        String query = select + from + where + orderBy;
        return query;
    }

    /**
     * Build SELECT clause - determines which date to display based on filter type
     */
    private String buildSelectClause(PreAlertSearchModel searchModel) {
        String dateDisplay = "ISNULL(A.ETA, '1900-01-01') as deta";

        if (searchModel.getEta() != null && searchModel.getEta()) {
            if (searchModel.getEtaType() != null) {
                if (searchModel.getEtaType() == 1) {
                    dateDisplay = "ISNULL(A.OETA, '1900-01-01') as deta";
                } else if (searchModel.getEtaType() == 2) {
                    dateDisplay = "ISNULL(A.ETA, '1900-01-01') as deta";
                } else {
                    dateDisplay = "ISNULL(ISNULL(A.ETA, A.OETA), '1900-01-01') as deta";
                }
            }
        }

        return "SELECT " +
                "A.SaleDate, " +
                "A.Id as saleOrderMasterRefId, " +
                "A.Offvesselname as offVesselName, " +
                "A.Commodity as commodity, " +
                "A.SCN as scn, " +
                "A.LSCN as lscn, " +
                "A.TruckSize as truckSize, " +
                "A.BLCopy as blCopy, " +
                "A.Loadingvesselname as loadingVesselName, " +
                "E.EmployeeName as employeeName, " +
                "A.Origin as origin, " +
                "A.Destination as destination, " +
                "FORMAT(ISNULL(A.PickupDate, '1900-01-01'), 'yyyy-MM-dd HH:mm:ss') as pickupDate, " +
                "FORMAT(ISNULL(A.DeliveryDate, '1900-01-01'), 'yyyy-MM-dd HH:mm:ss') as deliveryDate, " +
                "FORMAT(ISNULL(A.ETA, '1900-01-01'), 'yyyy-MM-dd HH:mm:ss') as eta, " +
                "FORMAT(ISNULL(A.ETB, '1900-01-01'), 'yyyy-MM-dd HH:mm:ss') as etb, " +
                "FORMAT(ISNULL(A.ETD, '1900-01-01'), 'yyyy-MM-dd HH:mm:ss') as etd, " +
                "FORMAT(ISNULL(A.OETA, '1900-01-01'), 'yyyy-MM-dd HH:mm:ss') as oeta, " +
                "FORMAT(ISNULL(A.OETB, '1900-01-01'), 'yyyy-MM-dd HH:mm:ss') as oetb, " +
                "FORMAT(ISNULL(A.OETD, '1900-01-01'), 'yyyy-MM-dd HH:mm:ss') as oetd, " +
                "FORMAT(" + dateDisplay.split(" as ")[0] + ", 'yyyy-MM-dd HH:mm:ss') as deta, " +
                "A.Vessel as vessel, " +
                "A.OVessel as oVessel, " +
                "A.CNumberDisplay as jobNo, " +
                "A.SPort as sPort, " +
                "A.OPort as oPort, " +
                "J.Name as jobName, " +
                "A.TotalWeight as totalWeight, " +
                "A.Quantity as quantity, " +
                "A.AWBNo as awbNo, " +
                "PA.Remarks as remarks, " +
                "PA.Id as paRefId, " +
                "ISNULL(Ag.AgentName, '') as agentName, " +
                "ISNULL(Ag.MobileNo, '') as agentPhone, " +
                "ISNULL(OAg.AgentName, '') as oAgentName, " +
                "ISNULL(OAg.MobileNo, '') as oAgentPhone, " +
                "C.CustomerName as customerName, " +
                "Js.Name as jobStatus, " +
                "ISNULL(EB.EmployeeName, '') as boardingOfficerName, " +
                "ISNULL(EB1.EmployeeName, '') as boardingOfficerName1, " +
                "C.Id as customerMasterRefId, " +
                "J.Id as jobTypeMasterRefId, " +
                "JS.Id as jobStatusMasterRefId, " +
                "PA.BoardingOfficerName as boardingOfficerNameFromPA, " +
                "Ag.Id as agentRefId, " +
                "E.Id as employeeMasterRefId ";
    }

    /**
     * Build FROM and JOIN clauses
     */
    private String buildFromClause() {
        return "FROM SaleOrderMaster A WITH(NOLOCK) " +
                "INNER JOIN Customer C WITH(NOLOCK) ON C.Id = A.CustomerRefId " +
                "INNER JOIN JobTypeMaster J WITH(NOLOCK) ON J.Id = A.JobMasterRefId " +
                "INNER JOIN JobStatusMaster JS WITH(NOLOCK) ON JS.Id = A.JStatus " +
                "LEFT JOIN Agent Ag WITH(NOLOCK) ON Ag.Id = A.AgentMasterRefId " +
                "LEFT JOIN Agent OAg WITH(NOLOCK) ON OAg.Id = A.OAgentMasterRefId " +
                "LEFT JOIN EmployeeMaster E WITH(NOLOCK) ON E.Id = A.EmployeeRefId " +
                "LEFT JOIN EmployeeMaster EB WITH(NOLOCK) ON EB.Id = A.BoardingOfficerRefId " +
                "LEFT JOIN EmployeeMaster EB1 WITH(NOLOCK) ON EB1.Id = A.BoardingOfficer1RefId " +
                "LEFT JOIN PreAlert PA WITH(NOLOCK) ON PA.JobNo = A.CNumberDisplay ";
    }

    /**
     * Build WHERE clause based on search criteria
     */
    private String buildWhereClause(PreAlertSearchModel searchModel) {
        List<String> conditions = new ArrayList<>();

        // Company filter (required)
        if (searchModel.getComId() != null && searchModel.getComId() > 0) {
            conditions.add("A.CompanyRefId = ?");
        }

        // Active records only
        conditions.add("A.Active = 1");

        // Exclude cancelled jobs
        conditions.add("A.JStatus != 12");

        // Customer filter
        if (searchModel.getCustomerId() != null && searchModel.getCustomerId() > 0) {
            conditions.add("A.CustomerRefId = ?");
        }

        // Job filter
        if (searchModel.getJobId() != null && searchModel.getJobId() > 0) {
            conditions.add("A.JobMasterRefId = ?");
        }

        // Date range filters
        if (searchModel.getFromDate() != null && searchModel.getToDate() != null) {
            Boolean pickupDateFilter = searchModel.getPickupDate();
            Boolean etaFilter = searchModel.getEta();

            if (pickupDateFilter != null && pickupDateFilter) {
                // Filter by pickup date
                conditions.add("CAST(A.PickupDate AS DATE) BETWEEN ? AND ?");
            } else if (etaFilter != null && etaFilter) {
                // Filter by ETA based on type
                Integer etaType = searchModel.getEtaType() != null ? searchModel.getEtaType() : 0;

                if (etaType == 1) {
                    // OETA only
                    conditions.add("CAST(A.OETA AS DATE) BETWEEN ? AND ?");
                } else if (etaType == 2) {
                    // ETA only
                    conditions.add("CAST(A.ETA AS DATE) BETWEEN ? AND ?");
                } else {
                    // Both ETA and OETA
                    conditions.add("(CAST(A.ETA AS DATE) BETWEEN ? AND ? OR CAST(A.OETA AS DATE) BETWEEN ? AND ?)");
                }
            } else {
                // Default: filter by sale date
                conditions.add("CAST(A.SaleDate AS DATE) BETWEEN ? AND ?");
            }
        }

        // Delivery status filter
        if (searchModel.getDeliveryDone() != null && searchModel.getDeliveryDone()) {
            conditions.add("JS.Name NOT IN ('DELIVERY DONE', 'WAITING FOR POD', 'WAITING FOR BILLING', 'JOB COMPLET', 'Z-CANCEL')");
        }

        // Port search filter
        if (searchModel.getSPort() != null && !searchModel.getSPort().trim().isEmpty()) {
            conditions.add("(A.SPort LIKE ? OR A.OPort LIKE ?)");
        }

        // Vessel search filter
        if (searchModel.getSearch() != null && !searchModel.getSearch().trim().isEmpty()) {
            conditions.add("(A.Offvesselname LIKE ? OR A.Loadingvesselname LIKE ?)");
        }

        return " WHERE " + String.join(" AND ", conditions);
    }

    /**
     * Build ORDER BY clause
     */
    private String buildOrderByClause(PreAlertSearchModel searchModel) {
        String sortBy = "SaleDate";
        String sortOrder = "ASC";

        if (searchModel.getSortBy() != null && "DETA".equalsIgnoreCase(searchModel.getSortBy())) {
            sortBy = "deta";
        }

        if (searchModel.getSortOrder() != null && "DESC".equalsIgnoreCase(searchModel.getSortOrder())) {
            sortOrder = "DESC";
        }

        return " ORDER BY " + sortBy + " " + sortOrder;
    }

    /**
     * Build parameters list for the query
     */
    private List<Object> buildParams(PreAlertSearchModel searchModel) {
        List<Object> params = new ArrayList<>();

        // Company ID (required)
        if (searchModel.getComId() != null && searchModel.getComId() > 0) {
            params.add(searchModel.getComId());
        }

        // Customer filter
        if (searchModel.getCustomerId() != null && searchModel.getCustomerId() > 0) {
            params.add(searchModel.getCustomerId());
        }

        // Job filter
        if (searchModel.getJobId() != null && searchModel.getJobId() > 0) {
            params.add(searchModel.getJobId());
        }

        // Date range parameters
        if (searchModel.getFromDate() != null && searchModel.getToDate() != null) {
            String fromDate = searchModel.getFromDate().format(DATE_FORMATTER);
            String toDate = searchModel.getToDate().format(DATE_FORMATTER);

            Boolean etaFilter = searchModel.getEta();
            if (etaFilter != null && etaFilter) {
                Integer etaType = searchModel.getEtaType() != null ? searchModel.getEtaType() : 0;

                if (etaType == 0) {
                    // Both ETA and OETA - add dates twice
                    params.add(fromDate);
                    params.add(toDate);
                    params.add(fromDate);
                    params.add(toDate);
                } else {
                    // Single date range
                    params.add(fromDate);
                    params.add(toDate);
                }
            } else {
                // Single date range (for pickup or sale date)
                params.add(fromDate);
                params.add(toDate);
            }
        }

        // Port search filter
        if (searchModel.getSPort() != null && !searchModel.getSPort().trim().isEmpty()) {
            String portPattern = "%" + searchModel.getSPort() + "%";
            params.add(portPattern);
            params.add(portPattern);
        }

        // Vessel search filter
        if (searchModel.getSearch() != null && !searchModel.getSearch().trim().isEmpty()) {
            String searchPattern = "%" + searchModel.getSearch() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
        }

        log.debug("Built {} parameters for pre-alert query", params.size());
        return params;
    }
}


