package my.maleva.api.module.transaction.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import my.maleva.api.module.transaction.dto.*;
import my.maleva.api.module.transaction.repository.PreAlertReportRepository;
import my.maleva.api.module.transaction.service.PreAlertReportService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service implementation for Pre-Alert Report functionality
 * Handles business logic and orchestration for pre-alert operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PreAlertReportServiceImpl implements PreAlertReportService {

    private final PreAlertReportRepository preAlertRepository;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Get pre-alert report data based on search criteria
     * Implements all filtering, sorting, and pagination logic
     */
    @Override
    public List<PreAlertReportModel> getPreAlertReport(PreAlertSearchModel searchModel) {
        log.info("Fetching pre-alert report for comId={}, customerId={}, jobId={}",
                searchModel.getComId(), searchModel.getCustomerId(), searchModel.getJobId());

        try {
            // Validate input
            if (searchModel.getComId() == null || searchModel.getComId() <= 0) {
                log.warn("Invalid comId provided");
                return Collections.emptyList();
            }

            // Fetch data from repository
            List<PreAlertReportModel> results = preAlertRepository.getPreAlertReportData(searchModel);

            if (results == null || results.isEmpty()) {
                log.warn("No pre-alert report data found for the given criteria");
                return Collections.emptyList();
            }

            log.info("Successfully retrieved {} pre-alert records", results.size());
            return results;

        } catch (Exception e) {
            log.error("Error fetching pre-alert report for comId={}: {}",
                    searchModel.getComId(), e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Get pre-alert report with pagination support
     */
    @Override
    public List<PreAlertReportModel> getPreAlertReportPaginated(PreAlertSearchModel searchModel) {
        log.info("Fetching paginated pre-alert report for comId={}",
                searchModel.getComId());

        try {
            // Get all results first
            List<PreAlertReportModel> allResults = getPreAlertReport(searchModel);

            if (allResults.isEmpty()) {
                return Collections.emptyList();
            }

            // Return all results (pagination removed to match C# model)
            return allResults;

        } catch (Exception e) {
            log.error("Error fetching paginated pre-alert report for comId={}: {}",
                    searchModel.getComId(), e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Get count of pre-alert records matching search criteria
     * Useful for calculating total pages in pagination
     */
    @Override
    public long getPreAlertReportCount(PreAlertSearchModel searchModel) {
        log.debug("Counting pre-alert records for comId={}", searchModel.getComId());

        try {
            List<PreAlertReportModel> results = getPreAlertReport(searchModel);
            return results.size();

        } catch (Exception e) {
            log.error("Error counting pre-alert records: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Export pre-alert report to CSV format
     * Optional utility method for report export functionality
     */
    @Override
    public String exportPreAlertReportToCSV(PreAlertSearchModel searchModel) {
        log.info("Exporting pre-alert report to CSV for comId={}", searchModel.getComId());

        try {
            List<PreAlertReportModel> results = getPreAlertReport(searchModel);

            if (results.isEmpty()) {
                log.warn("No data to export");
                return ""; // Return empty CSV header
            }

            // Build CSV header
            StringBuilder csv = new StringBuilder();
            csv.append("Sale Date,Job No,Loading Vessel,Offvessell,Commodity,SCN,LSCN,");
            csv.append("Origin,Destination,S.Port,O.Port,Pickup Date,Delivery Date,");
            csv.append("ETA,ETB,ETD,OETA,OETB,OETD,Customer,Job Status,Employee,");
            csv.append("Agent Name,Agent Phone,O.Agent Name,O.Agent Phone,Remarks\n");

            // Add data rows
            for (PreAlertReportModel model : results) {
                csv.append(escapeCSV(model.getSaleDate() != null ? model.getSaleDate() : "")).append(",");
                csv.append(escapeCSV(model.getJobNo())).append(",");
                csv.append(escapeCSV(model.getLoadingVesselName())).append(",");
                csv.append(escapeCSV(model.getOffVesselName())).append(",");
                csv.append(escapeCSV(model.getCommodity())).append(",");
                csv.append(escapeCSV(model.getScn())).append(",");
                csv.append(escapeCSV(model.getLscn())).append(",");
                csv.append(escapeCSV(model.getOrigin())).append(",");
                csv.append(escapeCSV(model.getDestination())).append(",");
                csv.append(escapeCSV(model.getSPort())).append(",");
                csv.append(escapeCSV(model.getOPort())).append(",");
                csv.append(escapeCSV(model.getPickupDate())).append(",");
                csv.append(escapeCSV(model.getDeliveryDate())).append(",");
                csv.append(escapeCSV(model.getEta())).append(",");
                csv.append(escapeCSV(model.getEtb())).append(",");
                csv.append(escapeCSV(model.getEtd())).append(",");
                csv.append(escapeCSV(model.getOeta())).append(",");
                csv.append(escapeCSV(model.getOetb())).append(",");
                csv.append(escapeCSV(model.getOetd())).append(",");
                csv.append(escapeCSV(model.getCustomerName())).append(",");
                csv.append(escapeCSV(model.getJobStatus())).append(",");
                csv.append(escapeCSV(model.getEmployeeName())).append(",");
                csv.append(escapeCSV(model.getAgentName())).append(",");
                csv.append(escapeCSV(model.getAgentPhone())).append(",");
                csv.append(escapeCSV(model.getOAgentName())).append(",");
                csv.append(escapeCSV(model.getOAgentPhone())).append(",");
                csv.append(escapeCSV(model.getRemarks())).append("\n");
            }

            log.info("Successfully exported {} records to CSV", results.size());
            return csv.toString();

        } catch (Exception e) {
            log.error("Error exporting pre-alert report to CSV: {}", e.getMessage(), e);
            return "";
        }
    }

    /**
     * Helper method to escape CSV special characters
     */
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }

        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }
    
    @Override
    @Transactional
    public Object insertPreAlert(List<PreAlertMasterDto> objBrand, Integer comId) {
        log.info("Inserting {} PreAlert master records for comId: {}",
                objBrand != null ? objBrand.size() : 0, comId);

        Map<String, Object> response = new HashMap<>();
        try {
            // =====================================================================
            // INPUT VALIDATION
            // =====================================================================
            if (objBrand == null || objBrand.isEmpty()) {
                response.put("ok", false);
                response.put("message", "No data provided");
                return response;
            }

            if (comId == null || comId <= 0) {
                response.put("ok", false);
                response.put("message", "Invalid company ID");
                return response;
            }

            // =====================================================================
            // BUILD SP-COMPATIBLE JSON STRUCTURE
            // =====================================================================
            // SP_PreAlert expects:
            // @master: JSON array with each record containing nested "PreAlert"
            //          which is itself a JSON string array of detail rows
            // @ComId: Company ID
            //
            // Detail row logic (handled by SP):
            // - New rows: Id = 0 or null → SP executes INSERT
            // - Existing rows: Id > 0 → SP executes UPDATE
            // - Deleted rows: Omit from list → SP does NOT delete (by design)
            // =====================================================================
            List<Map<String, Object>> masterRecords = new java.util.ArrayList<>();
            try {
                for (PreAlertMasterDto dto : objBrand) {
                    masterRecords.add(buildMasterRecordForProcedure(dto));
                }
            }


            catch (Exception e) {
                response.put("ok", false);
                response.put("message", "Error building master records: " + e.getMessage());
                return response;
            }

            // Convert the list of master objects to JSON array
            String masterJson = objectMapper.writeValueAsString(masterRecords);
            log.debug("Master JSON payload for SP_PreAlert: {} records", masterRecords.size());

            // =====================================================================
            // EXECUTE STORED PROCEDURE
            // =====================================================================
            List<Map<String, Object>> resultRows = jdbcTemplate.queryForList(
                    "EXEC SP_PreAlert ?, ?",
                    masterJson,
                    comId
            );

            if (resultRows.isEmpty()) {
                response.put("ok", false);
                response.put("message", "No result returned from SP_PreAlert");
                return response;
            }

            // =====================================================================
            // PARSE STORED PROCEDURE RESPONSE
            // =====================================================================
            Map<String, Object> spResult = resultRows.get(0);
            int resultCode = toInt(spResult.get("Result"));
            String message = stringValue(spResult.get("Msg"));
            String billNo = stringValue(spResult.get("BillNo"));
            int id = toInt(spResult.get("Id"));

            boolean isSuccess = resultCode == 1;

            response.put("ok", isSuccess);
            response.put("message", isSuccess ? "PreAlert created successfully" : message);
            response.put("Id", id);
            response.put("BillNo", billNo);

            // Backward compatibility fields
            response.put("Name", billNo);
            response.put("Data1", billNo);
            response.put("Data2", id);

            log.info("PreAlert insert {} - Result: {}, BillNo: {}, Id: {}",
                    isSuccess ? "succeeded" : "failed",
                    resultCode, billNo, id);

        }
        catch (Exception ex) {
            log.error("Error in insertPreAlert", ex);
            response.put("ok", false);
            response.put("message", "Error: " + ex.getMessage());
            response.put("Id", 0);
            response.put("BillNo", "");
            response.put("Data1", "");
            response.put("Data2", 0);
        }

        return response;
    }

    private int toInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    /**
     * Update PreAlert record via stored procedure.
     * Updates existing PreAlert master and handles detail row changes.
     * Critical: Detail row ID handling for UPDATE operation:
     * • NEW detail rows:      Set PreAlertDto.id = 0 (or null)  → SP will INSERT
     * • MODIFIED detail rows: Set PreAlertDto.id = original ID → SP will UPDATE
     * • UNCHANGED detail rows: Include with original ID or omit → SP will UPDATE or skip
     * • DELETE detail rows:   Omit from list (SP does NOT delete) → Row remains in DB
     * Important: The stored procedure SP_PreAlert DOES NOT support DELETE of detail rows.
     * Old PreAlert detail records are preserved. Only INSERT for new and UPDATE for existing.
     * @param masterDto Updated PreAlert master (Id must be > 0) with detail rows
     * @param comId Company ID
     * @return Response object with success status (ok=true/false), message, and Id
     */
    @Override
    @Transactional
    public Object updatePreAlert(PreAlertMasterDto masterDto, Integer comId) {
        log.info("Updating PreAlert master record - id: {}, comId: {}", masterDto.getId(), comId);

        Map<String, Object> response = new HashMap<>();
        try {
            // =====================================================================
            // VALIDATION
            // =====================================================================
            if (masterDto.getId() == null || masterDto.getId() <= 0) {
                response.put("ok", false);
                response.put("message", "Valid PreAlert ID is required for update");
                return response;
            }

            if (comId == null || comId <= 0) {
                response.put("ok", false);
                response.put("message", "Invalid company ID");
                return response;
            }

            // =====================================================================
            // BUILD JSON STRUCTURE FOR UPDATE OPERATION
            // Detail row logic (SP_PreAlert handles):
            // - Id = 0: INSERT new detail rows
            // - Id > 0: UPDATE existing detail rows
            // - Omitted rows: Remain untouched (no DELETE)
            // =====================================================================
            Map<String, Object> masterMap = buildMasterRecordForProcedure(masterDto);

            List<Map<String, Object>> updateRecords = java.util.Collections.singletonList(masterMap);
            String masterJson = objectMapper.writeValueAsString(updateRecords);

            log.debug("Built update JSON for SP_PreAlert - {} detail rows",
                    masterDto.getPreAlertRows() != null ? masterDto.getPreAlertRows().size() : 0);

            // =====================================================================
            // EXECUTE STORED PROCEDURE
            // =====================================================================
            List<Map<String, Object>> resultRows = jdbcTemplate.queryForList(
                    "EXEC SP_PreAlert ?, ?",
                    masterJson,
                    comId
            );

            if (resultRows.isEmpty()) {
                response.put("ok", false);
                response.put("message", "No result returned from SP_PreAlert");
                return response;
            }

            // =====================================================================
            // PARSE STORED PROCEDURE RESPONSE
            // =====================================================================
            Map<String, Object> spResult = resultRows.get(0);
            int resultCode = toInt(spResult.get("Result"));
            String message = stringValue(spResult.get("Msg"));
            String billNo = stringValue(spResult.get("BillNo"));
            int id = toInt(spResult.get("Id"));

            boolean isSuccess = resultCode == 1;

            response.put("ok", isSuccess);
            response.put("message", isSuccess ? "PreAlert updated successfully" : message);
            response.put("Id", id);
            response.put("BillNo", billNo);

            // Backward compatibility fields
            response.put("Name", billNo);
            response.put("Data1", billNo);
            response.put("Data2", id);

            log.info("PreAlert update {} - Result: {}, Id: {}",
                    isSuccess ? "succeeded" : "failed",
                    resultCode, id);

        } catch (Exception ex) {
            log.error("Error in updatePreAlert", ex);
            response.put("ok", false);
            response.put("message", "Error updating PreAlert: " + ex.getMessage());
            response.put("Id", 0);
            response.put("BillNo", "");
            response.put("Data1", "");
            response.put("Data2", 0);
        }

        return response;
    }

    @Override
    public Object editPreAlert(Integer id, Integer preAlertNo, Integer comId) {
        log.info("Editing PreAlert with id: {}, preAlertNo: {}, comId: {}", id, preAlertNo, comId);

        Map<String, Object> response = new HashMap<>();
        try {
            // If preAlertNo is provided, find the id from PreAlertMaster
            if (preAlertNo != null && preAlertNo != 0) {
                Integer foundId = jdbcTemplate.queryForObject(
                        "SELECT Id FROM PreAlertMaster WITH(NOLOCK) WHERE CompanyRefId = ? AND CNumber = ?",
                        Integer.class, comId, preAlertNo);
                if (foundId != null) {
                    id = foundId;
                } else {
                    response.put("ok", false);
                    response.put("message", "Invalid PreAlert Number!");
                    return response;
                }
            }

            // =====================================================================
            // Query to fetch PreAlert master and detail rows
            // =====================================================================
            String sql = "SELECT A.Id, A.CompanyRefId, A.CustomerMasterRefId, A.SaleOrderMasterRefId, A.JobTypeMasterRefId, " +
                    "A.EntryDate AS Date, A.FromDate, A.ToDate, A.Port, A.Vessel, A.OETA, A.LETA, A.ALLETA, A.NONE, " +
                    "A.ChkPort, A.ChkVessel, A.ChkPickupDate, A.ChkConsolidated, A.ChkDeliveryDone, A.Active, " +
                    "A.CNumberDisplay AS PreAlertNo, B.Id AS SDId, B.CustomerMasterRefId, B.JobTypeMasterRefId, " +
                    "B.JobStatusMasterRefId, B.ShipName, B.Vessel, B.Commodity, B.ETA, B.ETB, B.ETD, B.JobNo, " +
                    "ISNULL(C.CustomerName, '') AS CustomerName, B.Port, B.Weight, B.Package, B.AWBNo, B.AgentName, " +
                    "JS.Name AS JobStatus, B.AgentPhone, B.Remarks, B.SCN, B.Active, J.Name AS JobName, " +
                    "B.PreAlertMasterRefId " +
                    "FROM PreAlertMaster A WITH(NOLOCK) " +
                    "INNER JOIN PreAlert B WITH(NOLOCK) ON A.Id = B.PreAlertMasterRefId " +
                    "LEFT JOIN Customer C WITH(NOLOCK) ON C.Id = B.CustomerMasterRefId " +
                    "LEFT JOIN SaleOrderMaster SM WITH(NOLOCK) ON SM.Id = B.SaleOrderMasterRefId " +
                    "LEFT JOIN JobTypeMaster J WITH(NOLOCK) ON J.Id = SM.JobMasterRefId " +
                    "LEFT JOIN JobStatusMaster JS WITH(NOLOCK) ON JS.Id = SM.JStatus " +
                    "WHERE A.Id = ? AND A.CompanyRefId = ?";

            List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql, id, comId);

            if (resultList.isEmpty()) {
                response.put("ok", false);
                response.put("message", "Invalid PreAlert Number!");
                return response;
            }

            // =====================================================================
            // Group results by PreAlertMaster ID and build master + detail DTOs
            // =====================================================================
            Map<Integer, PreAlertMasterDto> masterMap = new HashMap<>();
            for (Map<String, Object> row : resultList) {
                Integer masterId = (Integer) row.get("Id");

                PreAlertMasterDto master = masterMap.computeIfAbsent(masterId, k -> buildMasterDtoFromRow(row));
                PreAlertDto detail = buildDetailDtoFromRow(row);
                master.getPreAlertRows().add(detail);
            }

            List<PreAlertMasterDto> result = new java.util.ArrayList<>(masterMap.values());

            response.put("ok", true);
            response.put("message", "Success");
            response.put("data", result);

            log.info("Successfully fetched PreAlert for editing - ID: {}", id);

        } catch (Exception ex) {
            log.error("Error editing PreAlert with ID: {} - {}", id, ex.getMessage(), ex);
            response.put("ok", false);
            response.put("message", ex.getMessage());
            response.put("data", "Api Details : PreAlert_editPreAlert");
        }

        return response;
    }

    @Override
    public Object selectPreAlert(F5Dto objlist) {
        log.info("Selecting PreAlerts with filter: {}", objlist);
        Map<String, Object> response = new HashMap<>();
        try {
            if (objlist == null || objlist.getComid() == null || objlist.getComid() <= 0) {
                response.put("ok", false);
                response.put("message", "Company ID is required");
                return response;
            }

            StringBuilder where = new StringBuilder(" WHERE A.CompanyRefId = ? AND A.Active = 1 ");
            List<Object> params = new java.util.ArrayList<>();
            params.add(objlist.getComid());

            if (objlist.getSearch() != null && !objlist.getSearch().trim().isEmpty()) {
                where.append(" AND A.CNumberDisplay = ? ");
                params.add(objlist.getSearch().trim());
            }

            String masterSql =
                    "SELECT A.Id, A.CNumber AS PreAlertNo, A.CNumberDisplay AS PreAlertNoDisplay, " +
                            "FORMAT(ISNULL(A.EntryDate, '1900-01-01'), 'dd/MM/yyyy') AS Date, " +
                            "A.ChkConsolidated, A.FromDate AS SFromDate, A.ToDate AS SToDate " +
                            "FROM PreAlertMaster A WITH (NOLOCK) " +
                            where;

            String detailSql =
                    "SELECT B.Id, B.PreAlertMasterRefId, B.SaleOrderMasterRefId, B.JobTypeMasterRefId, " +
                            "B.BoardingOfficerName, B.BoardingOfficerRefId, B.JobStatusMasterRefId, " +
                            "ISNULL(B.Commodity, '') AS Commodity, B.Port, B.JobNo, B.Weight, B.Package, " +
                            "B.AWBNo, B.AgentName, B.AgentPhone, B.Remarks, B.SCN, B.ETA, B.ETB, B.ETD, " +
                            "B.Active, JS.Name AS JobStatus, JS.Name AS Jobstatus, SM.EmployeeRefId AS EmployeeMasterRefId " +
                            "FROM PreAlert B WITH (NOLOCK) " +
                            "INNER JOIN PreAlertMaster A WITH (NOLOCK) ON B.PreAlertMasterRefId = A.Id " +
                            "LEFT JOIN Customer C WITH (NOLOCK) ON C.Id = B.CustomerMasterRefId " +
                            "LEFT JOIN SaleOrderMaster SM WITH (NOLOCK) ON SM.Id = B.SaleOrderMasterRefId " +
                            "LEFT JOIN JobStatusMaster JS WITH (NOLOCK) ON JS.Id = SM.JStatus " +
                            where;

            List<Map<String, Object>> masters = jdbcTemplate.queryForList(masterSql, params.toArray());
            List<Map<String, Object>> details = jdbcTemplate.queryForList(detailSql, params.toArray());

            Map<String, Object> wrapper = new HashMap<>();
            wrapper.put("PreAlertMaster", masters);
            wrapper.put("PreAlertDetails", details);

            List<Map<String, Object>> data = Collections.singletonList(wrapper);
            response.put("ok", true);
            response.put("message", "Success");
            response.put("Data", data);
            response.put("data", data);
            response.put("Data1", data);
            response.put("data1", data);
        } catch (Exception ex) {
            log.error("Error selecting PreAlerts", ex);
            response.put("ok", false);
            response.put("message", ex.getMessage());
            response.put("data1", "Api Details : PLANING_SelectPLANING");
        }
        return response;
    }

    @Override
    public Object maxPreAlertReportNo(F5Dto obj) {
        log.info("Fetching max PreAlert report number with filter: {}", obj);
        Map<String, Object> response = new HashMap<>();
        try {
            Integer nextSequenceNo = jdbcTemplate.queryForObject(
                    "SELECT ISNULL(MAX(SequenceNo) + 1, 1) FROM SequenceNoMaster WITH (NOLOCK) " +
                            "WHERE CompanyRefId = ? AND SequenceName = 'PreAlert'",
                    Integer.class,
                    obj.getComid()
            );
            String preAlertNo = "PA" + String.format("%04d", nextSequenceNo == null ? 1 : nextSequenceNo) +
                    "/" + java.time.Year.now().getValue();

            response.put("ok", true);
            response.put("message", "Success");
            response.put("No", preAlertNo);
            response.put("data1", preAlertNo);
        } catch (Exception ex) {
            log.error("Error fetching max PreAlert report number", ex);
            response.put("ok", false);
            response.put("message", ex.getMessage());
            response.put("data1", "Api Details : PreAlertReportNo_MaxPreAlertReportNo");
        }
        return response;
    }

    @Override
    public Object preAlertReport(TransactionDto obj) {
        log.info("Generating PreAlert report for transaction: comId={}, customerId={}, jobId={}", obj.getComid(), obj.getCustomerId(), obj.getJobid());

        Map<String, Object> response = new HashMap<>();
        try {
            if (obj.getComid() == null || obj.getComid() <= 0) {
                response.put("ok", false);
                response.put("message", "Company ID is required");
                response.put("data", null);
                return response;
            }

            StringBuilder whereClause = new StringBuilder();
            List<Object> params = new java.util.ArrayList<>();

            // Build WHERE conditions
            if (obj.getCustomerId() != null && obj.getCustomerId() != 0) {
                whereClause.append(" AND A.CustomerRefId = ?");
                params.add(obj.getCustomerId());
            }

            if (obj.getJobid() != null && obj.getJobid() != 0) {
                whereClause.append(" AND A.JobMasterRefId = ?");
                params.add(obj.getJobid());
            }

            String selectDETA = ", ISNULL(A.ETA, '1900-01-01') AS DETA";

            if (obj.getFromdate() != null) {
                if (Boolean.TRUE.equals(obj.getPickupdate())) {
                    whereClause.append(" AND CAST(A.PickupDate AS DATE) BETWEEN ? AND ?");
                    params.add(obj.getFromdate());
                    params.add(obj.getTodate());
                } else if (Boolean.TRUE.equals(obj.getEta())) {
                    if (obj.getEtaType() != null && obj.getEtaType() == 1) {
                        whereClause.append(" AND (CAST(A.OETA AS DATE) BETWEEN ? AND ?)");
                        params.add(obj.getFromdate());
                        params.add(obj.getTodate());
                        selectDETA = ", ISNULL(A.OETA, '1900-01-01') AS DETA";
                    } else if (obj.getEtaType() != null && obj.getEtaType() == 2) {
                        whereClause.append(" AND (CAST(A.ETA AS DATE) BETWEEN ? AND ?)");
                        params.add(obj.getFromdate());
                        params.add(obj.getTodate());
                        selectDETA = ", ISNULL(A.ETA, '1900-01-01') AS DETA";
                    } else {
                        whereClause.append(" AND ((CAST(A.ETA AS DATE) BETWEEN ? AND ?) OR (CAST(A.OETA AS DATE) BETWEEN ? AND ?))");
                        params.add(obj.getFromdate());
                        params.add(obj.getTodate());
                        params.add(obj.getFromdate());
                        params.add(obj.getTodate());
                        selectDETA = ", ISNULL(ISNULL(A.ETA, A.OETA), '1900-01-01') AS DETA";
                    }
                } else {
                    whereClause.append(" AND CAST(A.SaleDate AS DATE) BETWEEN ? AND ?");
                    params.add(obj.getFromdate());
                    params.add(obj.getTodate());
                }
            }

            if (Boolean.TRUE.equals(obj.getDeliveryDone())) {
                whereClause.append(" AND JS.Name NOT IN ('DELIVERY DONE', 'WAITING FOR POD', 'WAITING FOR BILLING', 'JOB COMPLET', 'Z-CANCEL')");
            }

            if (obj.getSPort() != null && !obj.getSPort().trim().isEmpty()) {
                whereClause.append(" AND (A.SPort LIKE ? OR A.OPort LIKE ?)");
                params.add("%" + obj.getSPort().trim() + "%");
                params.add("%" + obj.getSPort().trim() + "%");
            }

            if (obj.getSearch() != null && !obj.getSearch().trim().isEmpty()) {
                whereClause.append(" AND (A.Offvesselname LIKE ? OR A.Loadingvesselname LIKE ?)");
                params.add("%" + obj.getSearch().trim() + "%");
                params.add("%" + obj.getSearch().trim() + "%");
            }

            String sql = "SELECT A.SaleDate, A.Offvesselname, A.Commodity, A.SCN, A.LSCN, A.TruckSize, A.BLCopy, A.Loadingvesselname, " +
                    "E.EmployeeName, A.Origin, A.Destination, " +
                    "FORMAT(ISNULL(A.PickupDate, '1900-01-01'), 'dd/MM/yyyy HH:mm:ss') AS PickupDate, " +
                    "FORMAT(ISNULL(A.DeliveryDate, '1900-01-01'), 'dd/MM/yyyy HH:mm:ss') AS DeliveryDate, " +
                    "FORMAT(ISNULL(A.ETA, '1900-01-01'), 'dd/MM/yyyy HH:mm:ss') AS ETA " + selectDETA + ", " +
                    "FORMAT(ISNULL(A.ETB, '1900-01-01'), 'dd/MM/yyyy HH:mm:ss') AS ETB, " +
                    "FORMAT(ISNULL(A.ETD, '1900-01-01'), 'dd/MM/yyyy HH:mm:ss') AS ETD, " +
                    "FORMAT(ISNULL(A.OETA, '1900-01-01'), 'dd/MM/yyyy HH:mm:ss') AS OETA, " +
                    "FORMAT(ISNULL(A.OETB, '1900-01-01'), 'dd/MM/yyyy HH:mm:ss') AS OETB, " +
                    "FORMAT(ISNULL(A.OETD, '1900-01-01'), 'dd/MM/yyyy HH:mm:ss') AS OETD, " +
                    "A.Vessel, A.OVessel, A.CNumberDisplay AS JobNo, A.SPort, A.OPort, J.Name AS JobName, " +
                    "A.TotalWeight, A.Quantity, A.AWBNo, A.Remarks, " +
                    "ISNULL(Ag.AgentName, '') AS AgentName, ISNULL(Ag.MobileNo, '') AS AgentPhone, " +
                    "ISNULL(OAg.AgentName, '') AS OAgentName, ISNULL(OAg.MobileNo, '') AS OAgentPhone, " +
                    "C.CustomerName, JS.Name AS Jobstatus, " +
                    "ISNULL(EB.EmployeeName, '') AS BoardingOfficerName, " +
                    "ISNULL(EB1.EmployeeName, '') AS BoardingOfficerName1, " +
                    "C.Id AS CustomerMasterRefId, J.Id AS JobTypeMasterRefId, JS.Id AS JobStatusMasterRefId, " +
                    "Ag.Id AS AgentRefId " +
                    "FROM SaleOrderMaster A WITH(NOLOCK) " +
                    "INNER JOIN Customer C WITH(NOLOCK) ON C.Id = A.CustomerRefId " +
                    "INNER JOIN JobTypeMaster J WITH(NOLOCK) ON J.Id = A.JobMasterRefId " +
                    "INNER JOIN JobStatusMaster JS WITH(NOLOCK) ON JS.Id = A.JStatus " +
                    "LEFT JOIN Agent Ag WITH(NOLOCK) ON Ag.Id = A.AgentMasterRefid " +
                    "LEFT JOIN Agent OAg WITH(NOLOCK) ON OAg.Id = A.OAgentMasterRefid " +
                    "LEFT JOIN EmployeeMaster E ON E.Id = A.EmployeeRefId " +
                    "LEFT JOIN EmployeeMaster EB WITH(NOLOCK) ON EB.Id = A.BoardingOfficerRefid " +
                    "LEFT JOIN EmployeeMaster EB1 WITH(NOLOCK) ON EB1.Id = A.BoardingOfficer1Refid " +
                    "WHERE A.CompanyRefId = ? AND A.Active = 1 AND A.JStatus != 12" + whereClause;

            params.add(0, obj.getComid()); // Add comId at the beginning

            List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql, params.toArray());

            if (resultList.isEmpty()) {
                response.put("ok", false);
                response.put("message", "No records found");
                response.put("data", null);
            } else {
                // Convert to PreAlertReportModel list
                List<PreAlertReportModel> dataList = resultList.stream().map(row -> {
                    PreAlertReportModel model = new PreAlertReportModel();
                    model.setSaleDate(row.get("SaleDate") != null ? (String) row.get("SaleDate") : null);
                    model.setOffVesselName((String) row.get("Offvesselname"));
                    model.setCommodity((String) row.get("Commodity"));
                    model.setScn((String) row.get("SCN"));
                    model.setLscn((String) row.get("LSCN"));
                    model.setTruckSize((String) row.get("TruckSize"));
                    model.setBlCopy((String) row.get("BLCopy"));
                    model.setLoadingVesselName((String) row.get("Loadingvesselname"));
                    model.setEmployeeName((String) row.get("EmployeeName"));
                    model.setOrigin((String) row.get("Origin"));
                    model.setDestination((String) row.get("Destination"));
                    // Parse formatted dates back to LocalDateTime if needed
                    model.setPickupDate((String) row.get("PickupDate"));
                    model.setDeliveryDate((String) row.get("DeliveryDate"));
                    model.setEta((String) row.get("ETA"));
                    model.setEtb((String) row.get("ETB"));
                    model.setEtd((String) row.get("ETD"));
                    model.setOeta((String) row.get("OETA"));
                    model.setOetb((String) row.get("OETB"));
                    model.setOetd((String) row.get("OETD"));
                    model.setDeta((String) row.get("DETA"));
                    model.setVessel((String) row.get("Vessel"));
                    model.setOVessel((String) row.get("OVessel"));
                    model.setJobNo((String) row.get("JobNo"));
                    model.setSPort((String) row.get("SPort"));
                    model.setOPort((String) row.get("OPort"));
                    model.setJobName((String) row.get("JobName"));
                    model.setTotalWeight(row.get("TotalWeight") != null ? (String) row.get("TotalWeight") : null);
                    model.setQuantity(row.get("Quantity") != null ? (String) row.get("Quantity") : null);
                    model.setAwbNo((String) row.get("AWBNo"));
                    model.setRemarks((String) row.get("Remarks"));
                    model.setAgentName((String) row.get("AgentName"));
                    model.setAgentPhone((String) row.get("AgentPhone"));
                    model.setOAgentName((String) row.get("OAgentName"));
                    model.setOAgentPhone((String) row.get("OAgentPhone"));
                    model.setCustomerName((String) row.get("CustomerName"));
                    model.setJobStatus((String) row.get("Jobstatus"));
                    model.setBoardingOfficerName((String) row.get("BoardingOfficerName"));
                    model.setBoardingOfficerName1((String) row.get("BoardingOfficerName1"));
                    model.setCustomerMasterRefId((Integer) row.get("CustomerMasterRefId"));
                    model.setJobTypeMasterRefId((Integer) row.get("JobTypeMasterRefId"));
                    model.setJobStatusMasterRefId((Integer) row.get("JobStatusMasterRefId"));
                    model.setAgentRefId((Integer) row.get("AgentRefId"));
                    return model;
                }).collect(java.util.stream.Collectors.toList());

                // Sort based on ETA flag
                if (Boolean.TRUE.equals(obj.getEta())) {
                    dataList.sort(java.util.Comparator.comparing(PreAlertReportModel::getDeta, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())));
                } else {
                    dataList.sort(java.util.Comparator.comparing(PreAlertReportModel::getSaleDate, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())));
                }

                response.put("ok", true);
                response.put("message", "Success");
                response.put("data", dataList);
                response.put("data2", obj); // Include the input object as in C#
            }

        } catch (Exception ex) {
            log.error("Error generating PreAlert report", ex);
            response.put("ok", false);
            response.put("message", "Error generating report: " + ex.getMessage());
            response.put("data", null);
        }

        return response;
    }


    /**
     * Build PreAlertMasterDto from database result set row.
     * Extracts all master-level fields with proper type conversion.
     *
     * @param row Database result row
     * @return Populated PreAlertMasterDto with child list initialized
     */
    private PreAlertMasterDto buildMasterDtoFromRow(Map<String, Object> row) {
        Integer masterId = (Integer) row.get("Id");
        PreAlertMasterDto m = new PreAlertMasterDto();

        m.setId(masterId);
        m.setCompanyRefId(nullSafeGetInt(row, "CompanyRefId"));
        m.setCustomerMasterRefId(nullSafeGetInt(row, "CustomerMasterRefId"));
        m.setSaleOrderMasterRefId(nullSafeGetInt(row, "SaleOrderMasterRefId"));
        m.setJobTypeMasterRefId(nullSafeGetInt(row, "JobTypeMasterRefId"));
        m.setDate(nullSafeGetDate(row, "Date"));
        m.setFromDate(nullSafeGetDate(row, "FromDate"));
        m.setToDate(nullSafeGetDate(row, "ToDate"));
        m.setPort(nullSafeGetString(row, "Port"));
        m.setVessel(nullSafeGetString(row, "Vessel"));
        m.setOeta(nullSafeGetString(row, "OETA"));
        m.setLeta(nullSafeGetString(row, "LETA"));
        m.setAlleta(nullSafeGetString(row, "ALLETA"));
        m.setNone(nullSafeGetString(row, "NONE"));
        m.setChkPort(nullSafeGetString(row, "ChkPort"));
        m.setChkVessel(nullSafeGetString(row, "ChkVessel"));
        m.setChkPickupDate(nullSafeGetString(row, "ChkPickupDate"));
        m.setChkConsolidated(nullSafeGetString(row, "ChkConsolidated"));
        m.setChkDeliveryDone(nullSafeGetString(row, "ChkDeliveryDone"));
        m.setActive(nullSafeGetInt(row, "Active"));
        m.setCNumberDisplay(nullSafeGetString(row, "PreAlertNo"));
        m.setPreAlertRows(new java.util.ArrayList<>());

        return m;
    }

    /**
     * Build PreAlertDto (detail row) from database result set row.
     * Extracts all detail-level fields with proper type conversion.
     *
     * @param row Database result row
     * @return Populated PreAlertDto
     */
    private PreAlertDto buildDetailDtoFromRow(Map<String, Object> row) {
        PreAlertDto detail = new PreAlertDto();

        detail.setId(nullSafeGetInt(row, "SDId"));
        detail.setCustomerMasterRefId(nullSafeGetInt(row, "CustomerMasterRefId"));
        detail.setJobTypeMasterRefId(nullSafeGetInt(row, "JobTypeMasterRefId"));
        detail.setJobStatusMasterRefId(nullSafeGetInt(row, "JobStatusMasterRefId"));
        detail.setShipName(nullSafeGetString(row, "ShipName"));
        detail.setVessel(nullSafeGetString(row, "Vessel"));
        detail.setCommodity(nullSafeGetString(row, "Commodity"));
        detail.setEta(nullSafeGetString(row, "ETA"));
        detail.setEtb(nullSafeGetString(row, "ETB"));
        detail.setEtd(nullSafeGetString(row, "ETD"));
        detail.setJobNo(nullSafeGetString(row, "JobNo"));
        detail.setPort(nullSafeGetString(row, "Port"));
        detail.setWeight(nullSafeGetString(row, "Weight"));
        detail.setPackageInfo(nullSafeGetString(row, "Package"));
        detail.setAwbNo(nullSafeGetString(row, "AWBNo"));
        detail.setAgentName(nullSafeGetString(row, "AgentName"));
        detail.setAgentPhone(nullSafeGetString(row, "AgentPhone"));
        detail.setRemarks(nullSafeGetString(row, "Remarks"));
        detail.setScn(nullSafeGetString(row, "SCN"));
        detail.setActive(nullSafeGetInt(row, "Active"));
        detail.setPreAlertMasterRefId(nullSafeGetInt(row, "PreAlertMasterRefId"));

        return detail;
    }

    /**
     * Safely extract String value from result row, handling nulls.
     */
    private String nullSafeGetString(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * Safely extract Integer value from result row, handling nulls.
     */
    private Integer nullSafeGetInt(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        return null;
    }

    /**
     * Safely extract java.time.LocalDate from Timestamp in result row.
     */
    private java.time.LocalDate nullSafeGetDate(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toLocalDateTime().toLocalDate();
        }
        return null;
    }

    /**
     * This method transforms DTO fields into the format expected by SP_PreAlert.
     * Production-ready extraction: Centralizes mapping logic for better maintainability.
     * Field order is critical - must match SP_PreAlert OPENJSON WITH clause.
     *
     * @param master The PreAlertMasterDto containing master and detail rows
     * @return Map with all fields properly transformed for stored procedure
     * @throws Exception if JSON serialization of detail rows fails
     */
    private Map<String, Object> buildMasterRecordForProcedure(PreAlertMasterDto master) throws Exception {
        Map<String, Object> masterMap = new java.util.LinkedHashMap<>();

        // =====================================================================
        // IDENTITY AND REFERENCES
        // =====================================================================
        masterMap.put("Id", master.getId() != null ? master.getId() : 0);
        masterMap.put("CompanyRefId", master.getCompanyRefId() != null ? master.getCompanyRefId() : 0);
        masterMap.put("CustomerMasterRefId", master.getCustomerMasterRefId() != null ? master.getCustomerMasterRefId() : 0);
        masterMap.put("JobTypeMasterRefId", master.getJobTypeMasterRefId() != null ? master.getJobTypeMasterRefId() : 0);
        masterMap.put("EmployeeRefId", master.getEmployeeRefId() != null ? master.getEmployeeRefId() : 0);

        // =====================================================================
        // BOOKING/SEQUENCE FIELDS
        // =====================================================================
        masterMap.put("CNumber", master.getCNumber() != null ? master.getCNumber() : 0);
        masterMap.put("Port", master.getPort() != null ? master.getPort() : "");
        masterMap.put("BoardingOfficerName", master.getBoardingOfficerName() != null ? master.getBoardingOfficerName() : "");

        // =====================================================================
        // DATE FIELDS
        // =====================================================================
        masterMap.put("Date", master.getDate() != null ? master.getDate() : null);
        masterMap.put("Vessel", master.getVessel() != null ? master.getVessel() : "");

        // =====================================================================
        // ETA/ETD FIELDS
        // =====================================================================
        masterMap.put("OETA", master.getOeta() != null ? master.getOeta() : "");
        masterMap.put("LETA", master.getLeta() != null ? master.getLeta() : "");
        masterMap.put("ALLETA", master.getAlleta() != null ? master.getAlleta() : "");
        masterMap.put("NONE", master.getNone() != null ? master.getNone() : "");

        // =====================================================================
        // CHECKBOX/FLAG FIELDS
        // =====================================================================
        masterMap.put("ChkPort", master.getChkPort() != null ? master.getChkPort() : "");
        masterMap.put("ChkVessel", master.getChkVessel() != null ? master.getChkVessel() : "");
        masterMap.put("ChkPickupDate", master.getChkPickupDate() != null ? master.getChkPickupDate() : "");
        masterMap.put("CNumberDisplay", master.getCNumberDisplay() != null ? master.getCNumberDisplay() : "");
        masterMap.put("ChkConsolidated", master.getChkConsolidated() != null ? master.getChkConsolidated() : "");
        masterMap.put("ChkDeliveryDone", master.getChkDeliveryDone() != null ? master.getChkDeliveryDone() : "");

        // =====================================================================
        // DATE RANGE FIELDS
        // =====================================================================
        masterMap.put("FromDate", master.getFromDate() != null ? master.getFromDate() : null);
        masterMap.put("EntryDate", master.getEntryDate() != null ? master.getEntryDate() : null);
        masterMap.put("ToDate", master.getToDate() != null ? master.getToDate() : null);

        // =====================================================================
        // STATUS FIELD
        // =====================================================================
        masterMap.put("Active", master.getActive() != null ? master.getActive() : 1);

        // =====================================================================
        // CONVERT DETAIL ROWS TO JSON STRING
        // =====================================================================
        // SP_PreAlert expects @PreAlert to be a JSON string array
        // Format: "[{"Id":0,"CustomerMasterRefId":54,...}]"
        // DO NOT serialize this again - it will be escaped and break the SP parsing
        List<PreAlertDto> detailRows = master.getPreAlertRows();
        String preAlertString = master.getPreAlert();

        if (detailRows != null && !detailRows.isEmpty()) {
            // Use the List directly - Jackson will serialize it to JSON array
            masterMap.put("PreAlert", detailRows);
            log.debug("Using PreAlert list directly - {} records", detailRows.size());
        } else if (preAlertString != null && !preAlertString.trim().isEmpty()) {
            // Parse the JSON string back to List and use it directly
            try {
                List<PreAlertDto> parsedList = objectMapper.readValue(preAlertString,
                    new com.fasterxml.jackson.core.type.TypeReference<List<PreAlertDto>>() {});
                masterMap.put("PreAlert", parsedList);
                log.debug("Parsed PreAlert JSON string to list - {} records", parsedList.size());
            } catch (Exception e) {
                log.warn("Failed to parse PreAlert JSON string, using empty array: {}", e.getMessage());
                masterMap.put("PreAlert", new java.util.ArrayList<>());
            }
        } else {
            masterMap.put("PreAlert", new java.util.ArrayList<>());
            log.debug("No detail rows provided - using empty array");
        }


        log.debug("Master record built successfully - Id: {}, CompanyRefId: {}, DetailRows: {}",
                master.getId(), master.getCompanyRefId(),
                detailRows != null ? detailRows.size() : 0);

        return masterMap;
    }
}
