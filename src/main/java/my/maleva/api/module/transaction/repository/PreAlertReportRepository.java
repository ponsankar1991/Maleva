package my.maleva.api.module.transaction.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.module.transaction.dto.PreAlertReportModel;
import my.maleva.api.module.transaction.dto.PreAlertSearchModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;

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

            log.info("Pre-Alert Report Query - comId: {}, customerId: {}, jobId: {}, fromDate: {}, toDate: {}, eta: {}, etaType: {}, pickupDate: {}, deliveryDone: {}, sPort: {}, search: {}",
                    searchModel.getComId(), searchModel.getCustomerId(), searchModel.getJobId(),
                    searchModel.getFromDate(), searchModel.getToDate(),
                    searchModel.getEta(), searchModel.getEtaType(), searchModel.getPickupDate(),
                    searchModel.getDeliveryDone(), searchModel.getSPort(), searchModel.getSearch());

            log.info("Executing Pre-Alert query with {} parameters: {}", params.size(), params);
            log.info("Full Query: {}", query);

            List<PreAlertReportModel> results = jdbcTemplate.query(query, params.toArray(), new BeanPropertyRowMapper<>(PreAlertReportModel.class));

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
        // Use camelCase alias for BeanPropertyRowMapper
        String dateDisplay = "ISNULL(A.ETA, '1900-01-01') as deta";

        if (searchModel.getEta() != null && searchModel.getEta()) {
            if (searchModel.getEtaType() != null) {
                if (searchModel.getEtaType() == 1) {
                    // Original ETA
                    dateDisplay = "ISNULL(A.OETA, '1900-01-01') as deta";
                } else if (searchModel.getEtaType() == 2) {
                    // Local ETA
                    dateDisplay = "ISNULL(A.ETA, '1900-01-01') as deta";
                } else {
                    // All ETAs
                    dateDisplay = "ISNULL(ISNULL(A.ETA, A.OETA), '1900-01-01') as deta";
                }
            }
        }

        // Use camelCase aliases to match Java field names
        // Use FORMAT() to match C# implementation exactly
        return "SELECT " +
                "FORMAT(A.SaleDate, 'dd/MM/yyyy HH:mm:ss') AS saleDate, " +
                "A.Id AS saleOrderMasterRefId, " +
                "A.Offvesselname, " +
                "A.Commodity, " +
                "A.SCN, " +
                "A.LSCN, " +
                "A.TruckSize, " +
                "A.BLCopy, " +
                "A.Loadingvesselname, " +
                "E.EmployeeName, " +
                "A.Origin, " +
                "A.Destination, " +
                "FORMAT(ISNULL(A.PickupDate, '1900-01-01'), 'dd/MM/yyyy HH:mm:ss') AS pickupDate, " +
                "FORMAT(ISNULL(A.DeliveryDate, '1900-01-01'), 'dd/MM/yyyy HH:mm:ss') AS deliveryDate, " +
                "FORMAT(ISNULL(A.ETA, '1900-01-01'), 'dd/MM/yyyy HH:mm:ss') AS eta, " +
                "FORMAT(ISNULL(A.ETB, '1900-01-01'), 'dd/MM/yyyy HH:mm:ss') AS etb, " +
                "FORMAT(ISNULL(A.ETD, '1900-01-01'), 'dd/MM/yyyy HH:mm:ss') AS etd, " +
                "FORMAT(ISNULL(A.OETA, '1900-01-01'), 'dd/MM/yyyy HH:mm:ss') AS oeta, " +
                "FORMAT(ISNULL(A.OETB, '1900-01-01'), 'dd/MM/yyyy HH:mm:ss') AS oetb, " +
                "FORMAT(ISNULL(A.OETD, '1900-01-01'), 'dd/MM/yyyy HH:mm:ss') AS oetd, " +
                "FORMAT(" + dateDisplay.replace(" as deta", "") + ", 'dd/MM/yyyy HH:mm:ss') AS deta, " +
                "A.Vessel, " +
                "A.OVessel, " +
                "A.CNumberDisplay AS jobNo, " +
                "A.SPort, " +
                "A.OPort, " +
                "J.Name AS jobName, " +
                "A.TotalWeight AS totalWeight, " +
                "A.Quantity AS quantity, " +
                "A.AWBNo, " +
                "PA.Remarks, " +
                "PA.Id AS paRefId, " +
                "ISNULL(Ag.AgentName, '') AS agentName, " +
                "ISNULL(Ag.MobileNo, '') AS agentPhone, " +
                "ISNULL(OAg.AgentName, '') AS oAgentName, " +
                "ISNULL(OAg.MobileNo, '') AS oAgentPhone, " +
                "C.CustomerName, " +
                "Js.Name AS jobStatus, " +
                "ISNULL(EB.EmployeeName, '') AS boardingOfficerName, " +
                "ISNULL(EB1.EmployeeName, '') AS boardingOfficerName1, " +
                "C.Id AS customerMasterRefId, " +
                "J.Id AS jobTypeMasterRefId, " +
                "JS.Id AS jobStatusMasterRefId, " +
                "A.BoardingOfficerRefid AS boardingOfficerRefId, " +
                "PA.BoardingOfficerName AS boardingOfficerNameFromPA, " +
                "Ag.Id AS agentRefId, " +
                "E.Id AS employeeMasterRefId ";
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
        // Sorting is now handled in the controller to match C# logic
        // Default sort by SaleDate (will be overridden in controller if ETA filter is active)
        return " ORDER BY A.SaleDate ASC";
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

            Boolean pickupDateFilter = searchModel.getPickupDate();
            Boolean etaFilter = searchModel.getEta();
            if (pickupDateFilter != null && pickupDateFilter) {
                params.add(fromDate);
                params.add(toDate);
            } else if (etaFilter != null && etaFilter) {
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
