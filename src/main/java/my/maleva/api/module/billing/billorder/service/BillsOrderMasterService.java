package my.maleva.api.module.billing.billorder.service;

import my.maleva.api.module.billing.billorder.dto.*;
import my.maleva.api.module.billing.billorder.model.BillsOrderMasterModel;
import my.maleva.api.module.billing.billorder.model.BillsOrderDetailsModel;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.common.dto.ResponseViewModel;
import my.maleva.api.module.billing.billorder.mapper.BillsOrderMasterMapper;
import my.maleva.api.module.billing.billorder.entity.BillsOrderMaster;
import my.maleva.api.module.billing.billorder.repository.BillsOrderMasterRepository;
import my.maleva.api.module.master.repository.SequenceNoMasterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BillsOrderMasterService {

    private static final Logger logger = LoggerFactory.getLogger(BillsOrderMasterService.class);

    private final BillsOrderMasterRepository repository;
    private final BillsOrderMasterMapper mapper;
    private final SequenceNoMasterRepository sequenceNoMasterRepository;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public BillsOrderMasterService(BillsOrderMasterRepository repository,
                                   BillsOrderMasterMapper mapper,
                                   SequenceNoMasterRepository sequenceNoMasterRepository,
                                   NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.repository = repository;
        this.mapper = mapper;
        this.sequenceNoMasterRepository = sequenceNoMasterRepository;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }
    /**
     * Get all bills orders by company ID
     */
    public List<BillsOrderMasterDto> getByCompanyRefId(Integer companyRefId) {
        logger.info("Fetching bills orders for company: {}", companyRefId);
        return repository.findByCompanyRefId(companyRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all bills orders by supplier ID
     */
    public List<BillsOrderMasterDto> getBySupplierRefId(Integer supplierRefId) {
        logger.info("Fetching bills orders for supplier: {}", supplierRefId);
        return repository.findBySupplierRefId(supplierRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get active bills orders for a company
     */
    public List<BillsOrderMasterDto> getActiveByCompany(Integer companyRefId) {
        logger.info("Fetching active bills orders for company: {}", companyRefId);
        return getByCompanyRefId(companyRefId).stream()
                .filter(dto -> dto.getActive() == 1)
                .collect(Collectors.toList());
    }

    /**
     * Count bills orders by company
     */
    public Long countByCompanyRefId(Integer companyRefId) {
        logger.info("Counting bills orders for company: {}", companyRefId);
        return getByCompanyRefId(companyRefId).stream().count();
    }

    /**
     * Check invoice numbers - Get all unique invoice numbers for a company
     * Equivalent to .NET CheckInvoiceNo method
     * Returns PaymentVoucherCombo list where Active != 2
     *
     * @param companyRefId Company ID
     * @return List of PaymentVoucherComboDto with invoice numbers
     */
    public List<PaymentVoucherComboDto> checkInvoiceNo(Integer companyRefId) {
        logger.info("Checking invoice numbers for company: {}", companyRefId);
        try {
            List<PaymentVoucherComboDto> invoiceNumbers = repository.findInvoiceNumbersByCompany(companyRefId);
            logger.info("Found {} invoice numbers for company: {}", invoiceNumbers.size(), companyRefId);
            return invoiceNumbers;
        } catch (Exception ex) {
            logger.error("Error checking invoice numbers for company: {}", companyRefId, ex);
            throw new RuntimeException("Error fetching invoice numbers: " + ex.getMessage());
        }
    }

    /**
     * Get the maximum bills order master number
     * Equivalent to .NET MaxBillsOrderMasterNo method
     * Generates the next sequence number in format "PO" + padded to 9 digits with leading zeros
     *
     * @param companyRefId Company ID
     * @return ResponseViewModel with generated order number in Data1
     */
    @Transactional
    public ResponseViewModel maxBillsOrderMasterNo(Integer companyRefId) {
        ResponseViewModel ro;

        try {
            logger.info("Getting max bills order number for company: {}", companyRefId);

            // Get the maximum sequence number from SequenceNoMaster
            Integer maxSequenceNo = sequenceNoMasterRepository.findMaxBillsOrderSequenceNo(companyRefId);

            if (maxSequenceNo == null) {
                maxSequenceNo = 0;
            }

            // Increment by 1 to get the next sequence number
            Integer nextSequenceNo = maxSequenceNo + 1;

            // Format as "PO" + padded to 9 digits with leading zeros
            String billsOrderNumber = "PO" + String.format("%09d", nextSequenceNo);

            ro = ResponseViewModel.builder()
                    .isSuccess(true)
                    .statusCode(200)
                    .message("Bills order number generated successfully")
                    .data1(billsOrderNumber)
                    .build();

            logger.info("Generated bills order number: {} for company: {}", billsOrderNumber, companyRefId);

        } catch (Exception ex) {
            logger.error("Error getting max bills order number for company: {}", companyRefId, ex);

            ro = ResponseViewModel.builder()
                    .isSuccess(false)
                    .statusCode(500)
                    .message(ex.getMessage() != null ? ex.getMessage() : "Error generating bills order number")
                    .data1("Api Details: BillsOrderMaster_MaxBillsOrderMasterNo")
                    .build();
        }

        return ro;
    }

    /**
     * Select distinct descriptions for a company
     * Equivalent to .NET SelectDescription method
     * Returns distinct description values where Active != 2 and description is not empty
     *
     * @param companyRefId Company ID
     * @return ResponseViewModel with list of descriptions in Data1
     */
    public ResponseViewModel selectDescription(Integer companyRefId) {
        ResponseViewModel ro;

        try {
            logger.info("Fetching distinct descriptions for company: {}", companyRefId);

            // Get all distinct descriptions from BillsOrderMaster
            List<String> descriptions = repository.findDistinctDescriptionsByCompany(companyRefId);

            ro = ResponseViewModel.builder()
                    .isSuccess(true)
                    .statusCode(200)
                    .message("Descriptions retrieved successfully")
                    .data1(descriptions)
                    .build();

            logger.info("Found {} distinct descriptions for company: {}", descriptions.size(), companyRefId);

        } catch (Exception ex) {
            logger.error("Error fetching descriptions for company: {}", companyRefId, ex);

            ro = ResponseViewModel.builder()
                    .isSuccess(false)
                    .statusCode(500)
                    .message(ex.getMessage() != null ? ex.getMessage() : "Error fetching descriptions")
                    .data1("Api Details: BillsOrderMaster_SelectDescription")
                    .build();
        }

        return ro;
    }

    /**
     * Select distinct PayTo values for a company
     * Equivalent to .NET SelectPaymentTo method
     * Returns distinct PayTo values where Active != 2 and PayTo is not empty/null
     *
     * @param companyRefId Company ID
     * @return ResponseViewModel with list of PayTo values in Data1
     */
    public ResponseViewModel selectPaymentTo(Integer companyRefId) {
        ResponseViewModel ro;

        try {
            logger.info("Fetching distinct PayTo values for company: {}", companyRefId);

            // Get all distinct PayTo values from BillsOrderMaster
            List<String> paymentToList = repository.findDistinctPayToByCompany(companyRefId);

            ro = ResponseViewModel.builder()
                    .isSuccess(true)
                    .statusCode(200)
                    .message("Payment To list retrieved successfully")
                    .data1(paymentToList)
                    .build();

            logger.info("Found {} distinct PayTo values for company: {}", paymentToList.size(), companyRefId);

        } catch (Exception ex) {
            logger.error("Error fetching PayTo values for company: {}", companyRefId, ex);

            ro = ResponseViewModel.builder()
                    .isSuccess(false)
                    .statusCode(500)
                    .message(ex.getMessage() != null ? ex.getMessage() : "Error fetching PayTo values")
                    .data1("Api Details: BillsOrderMaster_SelectPaymentTo")
                    .build();
        }

        return ro;
    }

    /**
     * Delete (Soft Delete) a Bills Order Master record
     * Equivalent to .NET DeleteBillsOrderMaster method
     * Sets Active=2 (soft delete flag) where PStatus=0 (not locked/posted)
     * Only allows deletion if the record is not locked (PStatus=0)
     *
     * @param id the bills order master ID to delete
     * @param companyRefId the company ID (for validation/logging)
     * @return ResponseViewModel with success/failure status
     */
    @Transactional
    public ResponseViewModel deleteBillsOrderMaster(Integer id, Integer companyRefId) {
        ResponseViewModel ro;

        try {
            logger.info("Attempting to delete bills order ID: {} for company: {}", id, companyRefId);

            // Perform soft delete (set Active=2 where PStatus=0)
            int rowsUpdated = repository.softDeleteById(id);

            if (rowsUpdated > 0) {
                ro = ResponseViewModel.builder()
                        .isSuccess(true)
                        .statusCode(200)
                        .message("Bills order deleted successfully")
                        .data1(rowsUpdated)
                        .build();

                logger.info("Successfully deleted bills order ID: {} for company: {}", id, companyRefId);

            } else {
                ro = ResponseViewModel.builder()
                        .isSuccess(false)
                        .statusCode(400)
                        .message("Bills order not found or already locked for posting (PStatus != 0)")
                        .data1("Api Details: BillsOrderMaster_DeleteBillsOrderMaster")
                        .build();

                logger.warn("Failed to delete bills order ID: {} for company: {}. " +
                        "Record may not exist or is locked (PStatus != 0)", id, companyRefId);
            }

        } catch (Exception ex) {
            logger.error("Error deleting bills order ID: {} for company: {}", id, companyRefId, ex);

            ro = ResponseViewModel.builder()
                    .isSuccess(false)
                    .statusCode(500)
                    .message(ex.getMessage() != null ? ex.getMessage() : "Error deleting bills order")
                    .data1("Api Details: BillsOrderMaster_DeleteBillsOrderMaster")
                    .build();
        }

        return ro;
    }

    /**
     * Select Bills Order Master with complex filters
     * Equivalent to .NET SelectBillsOrderMaster method
     * Retrieves combined BillsOrderMaster and BillsOrderDetails data with dynamic filtering
     *
     * @param comid Company ID (required)
     * @param fromdate From date (yyyy-MM-dd format)
     * @param todate To date (yyyy-MM-dd format)
     * @param id Supplier ID (optional, 0 = all suppliers)
     * @param employeeid Employee ID (optional, 0 = all employees)
     * @param search Search keyword (optional)
     * @param invoicecheck Invoice date flag (1 = use invoice date, 0 = use sale date)
     * @param status Bill status filter (optional: "Pending" or specific status)
     * @return BillsOrderF5ViewDto with master and detail records
     */
    public BillsOrderF5ViewDto selectBillsOrderMaster(Integer comid, String fromdate, String todate,
                                                       Integer id, Integer employeeid, String search,
                                                       Integer invoicecheck, String status) {
        logger.info("SelectBillsOrderMaster called - comid: {}, fromdate: {}, todate: {}, id: {}, " +
                "employeeid: {}, search: {}, invoicecheck: {}, status: {}", 
                comid, fromdate, todate, id, employeeid, search, invoicecheck, status);

        try {
            StringBuilder whereClause = new StringBuilder();
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("comid", comid);

            // Build WHERE clause based on filters
            if (id != null && id != 0) {
                whereClause.append(" AND A.SupplierRefId = :supplierId");
                params.addValue("supplierId", id);
            }

            if (employeeid != null && employeeid != 0) {
                whereClause.append(" AND A.EmployeeRefId = :employeeId");
                params.addValue("employeeId", employeeid);
            }

            if (status != null && !status.isEmpty()) {
                if ("Pending".equalsIgnoreCase(status)) {
                    whereClause.append(" AND A.PStatus = 0");
                } else {
                    whereClause.append(" AND A.BillStatus = :billStatus");
                    params.addValue("billStatus", status);
                }
            }

            if (search != null && !search.isEmpty()) {
                whereClause.setLength(0); // Reset where clause for search
                whereClause.append(" AND (A.CNumberDisplay = :search OR A.InvoiceNo = :search OR SA.CNumberDisplay = :search)");
                params.addValue("search", search);
            } else {
                // Apply date filter only if no search
                if (invoicecheck != null && invoicecheck == 1) {
                    whereClause.append(" AND A.InvoiceDate BETWEEN :fromDate AND :toDate");
                } else {
                    whereClause.append(" AND A.SaleDate BETWEEN :fromDate AND :toDate");
                }
                params.addValue("fromDate", fromdate);
                params.addValue("toDate", todate);
            }

            // Query BillsOrderMaster records
            String masterSql = "SELECT A.Id, A.PStatus, ISNULL(E.EmployeeName, '') as EmployeeName, " +
                    "FORMAT(ISNULL(A.SaleDate, '1900-01-01'), 'dd/MM/yyyy') as BillDate, " +
                    "A.InvoiceNo, FORMAT(ISNULL(A.InvoiceDate, '1900-01-01'), 'dd/MM/yyyy') as InvoiceDate, " +
                    "A.CNumberDisplay as BillNoDisplay, " +
                    "FORMAT(ISNULL(A.Created_Date, '1900-01-01'), 'dd/MM/yyyy hh:mm:ss') as BillTime, " +
                    "B.SupplierName, A.Amount as NetAmt, A.SaleType, A.CNumber as BillNo, " +
                    "ISNULL(J.TruckName, '') as TruckName, ISNULL(K.DriverName, '') as DriverName, " +
                    "ISNULL(SA.CNumberDisplay, '') as BillNoDisplay1 " +
                    "FROM BillsOrderMaster A WITH(NOLOCK) " +
                    "INNER JOIN Supplier B WITH(NOLOCK) ON A.SupplierRefId = B.Id " +
                    "LEFT JOIN EmployeeMaster E WITH(NOLOCK) ON E.Id = A.EmployeeRefId " +
                    "LEFT JOIN TruckMaster J WITH(NOLOCK) ON J.Id = A.TruckRefid " +
                    "LEFT JOIN DriverMaster K WITH(NOLOCK) ON K.id = A.DriverRefid " +
                    "LEFT JOIN SaleOrderMaster SA WITH(NOLOCK) ON SA.Id = A.SaleMasterRefId " +
                    "WHERE A.CompanyRefId = :comid AND A.Active = 1" + whereClause.toString() +
                    " ORDER BY A.SaleDate DESC";

            List<BillsOrderMasterViewDto> masterRecords = namedParameterJdbcTemplate.query(
                    masterSql,
                    params,
                    (rs, rowNum) -> BillsOrderMasterViewDto.builder()
                            .id(rs.getInt("Id"))
                            .pStatus(rs.getInt("PStatus"))
                            .employeeName(rs.getString("EmployeeName"))
                            .billDate(rs.getString("BillDate"))
                            .invoiceNo(rs.getString("InvoiceNo"))
                            .invoiceDate(rs.getString("InvoiceDate"))
                            .billNoDisplay(rs.getString("BillNoDisplay"))
                            .billTime(rs.getString("BillTime"))
                            .supplierName(rs.getString("SupplierName"))
                            .netAmt(rs.getFloat("NetAmt"))
                            .saleType(rs.getString("SaleType"))
                            .billNo(rs.getInt("BillNo"))
                            .truckName(rs.getString("TruckName"))
                            .driverName(rs.getString("DriverName"))
                            .billNoDisplay1(rs.getString("BillNoDisplay1"))
                            .build()
            );

            // Query BillsOrderDetails records
            String detailsSql = "SELECT B.DiscAmount as DiscountAmt, B.DiscPer as DiscountPercent, " +
                    "B.ItemQty, B.MRP, I.Description as ProductName, B.SalesRate as SaleRate, " +
                    "B.BillsOrderMasterRefId as SaleRefId, A.TaxAmount as TaxAmt, B.TaxPercent, " +
                    "I.GlAccountCode as ProductCode, B.Amount as SAmount, B.RemarksD " +
                    "FROM BillsOrderDetails B WITH(NOLOCK) " +
                    "INNER JOIN BillsOrderMaster A WITH(NOLOCK) ON B.BillsOrderMasterRefId = A.Id " +
                    "LEFT JOIN SaleOrderMaster SA WITH(NOLOCK) ON SA.Id = A.SaleMasterRefId " +
                    "INNER JOIN GLAccounts I WITH(NOLOCK) ON B.AccountMasterRefId = I.RowIndex " +
                    "WHERE A.CompanyRefId = :comid AND A.Active = 1" + whereClause.toString();

            List<BillsOrderDetailsViewDto> detailRecords = namedParameterJdbcTemplate.query(
                    detailsSql,
                    params,
                    (rs, rowNum) -> BillsOrderDetailsViewDto.builder()
                            .discountAmt(rs.getFloat("DiscountAmt"))
                            .discountPercent(rs.getFloat("DiscountPercent"))
                            .itemQty(rs.getFloat("ItemQty"))
                            .mrp(rs.getFloat("MRP"))
                            .productName(rs.getString("ProductName"))
                            .saleRate(rs.getFloat("SaleRate"))
                            .saleRefId(rs.getInt("SaleRefId"))
                            .taxAmt(rs.getFloat("TaxAmt"))
                            .taxPercent(rs.getFloat("TaxPercent"))
                            .productCode(rs.getString("ProductCode"))
                            .sAmount(rs.getFloat("SAmount"))
                            .remarksD(rs.getString("RemarksD"))
                            .build()
            );

            logger.info("SelectBillsOrderMaster executed successfully - {} master records, {} detail records",
                    masterRecords.size(), detailRecords.size());

            return BillsOrderF5ViewDto.builder()
                    .billsOrderMaster(masterRecords)
                    .billsOrderDetails(detailRecords)
                    .build();

        } catch (Exception ex) {
            logger.error("Error in selectBillsOrderMaster for company: {}", comid, ex);
            throw new RuntimeException("Error retrieving bills order master: " + 
                    (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage()), ex);
        }
    }

    /**
     * Edit Bills Order Master Record
     * Equivalent to .NET EditBillsOrderMaster method
     * Retrieves a single bills order master record with all its details for editing
     * If BillsOrderMasterNo is provided, it will be used to fetch the ID
     *
     * @param id Bills Order Master ID (optional if billsOrderMasterNo is provided)
     * @param billsOrderMasterNo Bills Order Master Number/CNumber (optional)
     * @param comid Company ID (required)
     * @return ResponseViewModel with BillsOrderMasterModel data and details
     */
    public ResponseViewModel editBillsOrderMaster(Integer id, Integer billsOrderMasterNo, Integer comid) {
        ResponseViewModel ro = new ResponseViewModel();

        try {
            logger.info("EditBillsOrderMaster called - id: {}, billsOrderMasterNo: {}, comid: {}",
                    id, billsOrderMasterNo, comid);

            // If BillsOrderMasterNo is provided, fetch the ID from CNumber
            if (billsOrderMasterNo != null && billsOrderMasterNo != 0) {
                String findIdQuery = "SELECT id FROM BillsOrderMaster WITH(NOLOCK) " +
                        "WHERE CompanyRefId = :comid AND CNumber = :cnumber";

                MapSqlParameterSource params = new MapSqlParameterSource()
                        .addValue("comid", comid)
                        .addValue("cnumber", billsOrderMasterNo);

                try {
                    Integer fetchedId = namedParameterJdbcTemplate.queryForObject(
                            findIdQuery,
                            params,
                            Integer.class
                    );
                    if (fetchedId != null) {
                        id = fetchedId;
                        logger.info("Fetched ID {} from CNumber {}", id, billsOrderMasterNo);
                    }
                } catch (Exception ex) {
                    logger.warn("Could not find ID from CNumber {}: {}", billsOrderMasterNo, ex.getMessage());
                    id = 0;
                }
            }

            if (id == null || id == 0) {
                ro.setSuccess(false);
                ro.setStatusCode(404);
                ro.setMessage("Invalid Bills Order Master No !!!.");
                ro.setData1("Api Details: BillsOrderMaster_EditBillsOrderMaster");
                logger.warn("Invalid ID or BillsOrderMasterNo provided");
                return ro;
            }

            // Complex SQL query to fetch master and detail records
            String query = "SELECT A.Id, A.CompanyRefId, A.UserRefId, A.EmployeeRefId, A.InvoiceNo, " +
                    "A.InvoiceDate, A.SupplierRefId, A.SaleDate, A.SaleType, A.CNumberDisplay, " +
                    "A.CNumber, A.Coinage, A.GrossAmount, A.TaxAmount, A.DiscountAmount, " +
                    "A.PlusAmount, A.MinusAmount, A.Amount, A.Remarks, A.Active, A.BillStatus, " +
                    "A.PayTo, A.OffVessal, A.LodingVessal, A.Created_Date, A.Created_By, " +
                    "A.Modified_Date, A.Modified_By, A.TruckRefid, A.DriverRefid, A.SaleMasterRefId, " +
                    "A.Description, A.DueDate, ISNULL(A.DueDate, '') as SDueDate, A.PaymentTermsRefid, " +
                    "ISNULL(SA.CNumberDisplay, '') as JobNo, B.Id as SDId, B.ProductRefId, B.QuoteValue, " +
                    "B.SerialNo, B.AccountMasterRefId, B.MRP, B.PurchaseRate, B.ItemQty, B.DiscPer, " +
                    "B.DiscAmount, B.LandingCost, B.TaxPercent, B.TaxAmount, B.SalesRate, B.NetSalesRate, " +
                    "B.Amount, B.RemarksD, I.GlAccountCode as ProductCode, I.Description as ProductName " +
                    "FROM BillsOrderMaster A WITH(NOLOCK) " +
                    "INNER JOIN BillsOrderDetails B WITH(NOLOCK) ON A.Id = B.BillsOrderMasterRefId " +
                    "INNER JOIN GLAccounts I WITH(NOLOCK) ON I.RowIndex = B.AccountMasterRefId " +
                    "LEFT JOIN SaleOrderMaster SA WITH(NOLOCK) ON SA.Id = A.SaleMasterRefId " +
                    "WHERE A.Id = :id AND A.CompanyRefId = :comid " +
                    "ORDER BY A.Id, B.Id";

            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("id", id)
                    .addValue("comid", comid);

            // Execute query and map results
            final List<BillsOrderMasterModel> result = new ArrayList<>();
            final java.util.Map<Integer, BillsOrderMasterModel> masterDictionary = new java.util.LinkedHashMap<>();

            namedParameterJdbcTemplate.query(query, params, (rs) -> {
                int masterId = rs.getInt("Id");
                BillsOrderMasterModel master;

                if (!masterDictionary.containsKey(masterId)) {
                    master = new BillsOrderMasterModel();
                    master.setId(rs.getInt("Id"));
                    master.setCompanyRefId(rs.getInt("CompanyRefId"));
                    master.setUserRefId(rs.getInt("UserRefId"));
                    master.setEmployeeRefId(rs.getInt("EmployeeRefId"));
                    master.setInvoiceNo(rs.getString("InvoiceNo"));
                    master.setInvoiceDate(rs.getTimestamp("InvoiceDate") != null ?
                            new java.util.Date(rs.getTimestamp("InvoiceDate").getTime()) : null);
                    master.setSInvoiceDate(rs.getString("InvoiceDate"));
                    master.setSupplierRefId(rs.getInt("SupplierRefId"));
                    master.setSaleDate(rs.getTimestamp("SaleDate") != null ?
                            new java.util.Date(rs.getTimestamp("SaleDate").getTime()) : null);
                    master.setSSaleDate(rs.getString("SaleDate"));
                    master.setSaleType(rs.getString("SaleType"));
                    master.setCNumberDisplay(rs.getString("CNumberDisplay"));
                    master.setCNumber(rs.getInt("CNumber"));
                    master.setCoinage(rs.getFloat("Coinage"));
                    master.setGrossAmount(rs.getFloat("GrossAmount"));
                    master.setTaxAmount(rs.getFloat("TaxAmount"));
                    master.setDiscountAmount(rs.getFloat("DiscountAmount"));
                    master.setPlusAmount(rs.getFloat("PlusAmount"));
                    master.setMinusAmount(rs.getFloat("MinusAmount"));
                    master.setAmount(rs.getFloat("Amount"));
                    master.setRemarks(rs.getString("Remarks"));
                    master.setActive(rs.getInt("Active"));
                    master.setBillStatus(rs.getString("BillStatus"));
                    master.setPayTo(rs.getString("PayTo"));
                    master.setOffVessal(rs.getString("OffVessal"));
                    master.setLodingVessal(rs.getString("LodingVessal"));
                    master.setCreated_Date(rs.getTimestamp("Created_Date") != null ?
                            new java.util.Date(rs.getTimestamp("Created_Date").getTime()) : null);
                    master.setCreated_By(rs.getString("Created_By"));
                    master.setModified_Date(rs.getTimestamp("Modified_Date") != null ?
                            new java.util.Date(rs.getTimestamp("Modified_Date").getTime()) : null);
                    master.setModified_By(rs.getString("Modified_By"));
                    master.setTruckRefid(rs.getInt("TruckRefid"));
                    master.setDriverRefid(rs.getInt("DriverRefid"));
                    master.setSaleMasterRefId(rs.getInt("SaleMasterRefId"));
                    master.setDescription(rs.getString("Description"));
                    master.setDueDate(rs.getTimestamp("DueDate") != null ?
                            new java.util.Date(rs.getTimestamp("DueDate").getTime()) : null);
                    master.setSDueDate(rs.getString("SDueDate"));
                    master.setPaymentTermsRefid(rs.getInt("PaymentTermsRefid"));
                    master.setJobNo(rs.getString("JobNo"));
                    master.setBillsOrderDetails(new ArrayList<>());
                    masterDictionary.put(masterId, master);
                } else {
                    master = masterDictionary.get(masterId);
                }

                // Add detail record
                BillsOrderDetailsModel detail = new BillsOrderDetailsModel();
                detail.setId(rs.getInt("SDId"));
                detail.setSDId(rs.getInt("SDId"));
                detail.setBillsOrderMasterRefId(masterId);
                detail.setAccountMasterRefId(rs.getInt("AccountMasterRefId"));
                detail.setProductRefId(rs.getInt("ProductRefId"));
                detail.setMRP(rs.getFloat("MRP"));
                detail.setQuoteValue(rs.getFloat("QuoteValue"));
                detail.setPurchaseRate(rs.getFloat("PurchaseRate"));
                detail.setItemQty(rs.getFloat("ItemQty"));
                detail.setDiscPer(rs.getFloat("DiscPer"));
                detail.setDiscAmount(rs.getFloat("DiscAmount"));
                detail.setLandingCost(rs.getFloat("LandingCost"));
                detail.setTaxPercent(rs.getFloat("TaxPercent"));
                detail.setTaxAmount(rs.getFloat("TaxAmount"));
                detail.setSalesRate(rs.getFloat("SalesRate"));
                detail.setNetSalesRate(rs.getFloat("NetSalesRate"));
                detail.setAmount(rs.getFloat("Amount"));
                detail.setRemarksD(rs.getString("RemarksD"));
                detail.setSerialNo(rs.getString("SerialNo"));
                detail.setProductCode(rs.getString("ProductCode"));
                detail.setProductName(rs.getString("ProductName"));

                // Add detail to master only if not already added
                if (master.getBillsOrderDetails().stream()
                        .noneMatch(d -> d.getSDId() == detail.getSDId())) {
                    master.getBillsOrderDetails().add(detail);
                }
            });

            // Add all masters to result
            result.addAll(masterDictionary.values());

            if (!result.isEmpty()) {
                ro.setSuccess(true);
                ro.setStatusCode(200);
                ro.setMessage("Bills Order retrieved successfully");
                ro.setData1(result);
                logger.info("Successfully retrieved {} bills order records", result.size());
            } else {
                ro.setSuccess(false);
                ro.setStatusCode(404);
                ro.setMessage("Invalid Bills Order Master No !!!.");
                ro.setData1("Api Details: BillsOrderMaster_EditBillsOrderMaster");
                logger.warn("No records found for ID: {} and CompanyID: {}", id, comid);
            }

        } catch (Exception ex) {
            logger.error("Error in editBillsOrderMaster for ID: {}", id, ex);
            ro.setSuccess(false);
            ro.setStatusCode(500);
            ro.setMessage(ex.getMessage() != null ? ex.getMessage() : "Error retrieving bills order");
            ro.setData1("Api Details: BillsOrderMaster_EditBillsOrderMaster");
        }

        return ro;
    }

    /**
     * Select Bills Order Master with Advanced Filtering
     * Equivalent to .NET SelectBillsOrderView method
     *
     * Supports complex filtering by:
     * - billId: 1 = has bill, 2 = no bill, 0 = all
     * - supplierId: Filter by supplier
     * - employeeId: Filter by employee
     * - truckId: Filter by truck
     * - productId: Filter by product
     * - billStatus: Filter by bill status
     * - offVesselName: Filter by off-vessel name
     * - search: Global search in Bill No, Invoice No, Serial No
     * - vesselNameSearch: Filter by vessel name
     * - fromDate / toDate: Date range filter
     * - remarks: 1 = use InvoiceDate, 0 = use SaleDate
     *
     * @param filterModel SelectBillsOrderMasterRequestDto with all filter criteria
     * @return BillsOrderF5ViewDto with master and detail records
     */
    public BillsOrderF5ViewDto selectBillsOrderView(SelectBillsOrderMasterRequestDto filterModel) {
        logger.info("Selecting bills order view with filters - comid: {}, billId: {}, search: {}",
                filterModel.getComid(), filterModel.getBillId(), filterModel.getSearch());

        BillsOrderF5ViewDto result = new BillsOrderF5ViewDto();
        result.setBillsOrderMaster(new ArrayList<>());
        result.setBillsOrderDetails(new ArrayList<>());

        try {
            // Build WHERE clause based on filter criteria
            // FILTER PRIORITY:
            // 1. Basic Filters (BillId, Supplier, Employee, Truck, Product, Status, OffVessel) - CUMULATIVE
            // 2. Global Search - CLEARS basic filters, applies search conditionally
            // 3. Date Range Filters - Only applied if NO global search is provided
            // 4. Vessel Name Search - HIGHEST PRIORITY, CLEARS ALL (including search & dates), applies only vessel search
            // NOTE: When VessalNameSearch is provided, dates are NOT used (matches .NET behavior)
            StringBuilder whereClause = new StringBuilder();

            // Filter by bill ID (has bill or no bill)
            if (filterModel.getBillId() != null && filterModel.getBillId() > 0) {
                if (filterModel.getBillId() == 1) {
                    whereClause.append(" AND A.Id IN (SELECT ISNULL(BM.BillsOrdermasterrefid, 0) ")
                            .append("FROM BillMaster BM WITH(NOLOCK) WHERE BM.Active != 2)");
                } else if (filterModel.getBillId() == 2) {
                    whereClause.append(" AND A.Id NOT IN (SELECT ISNULL(BM.BillsOrdermasterrefid, 0) ")
                            .append("FROM BillMaster BM WITH(NOLOCK) WHERE BM.Active != 2)");
                }
            }

            // Filter by supplier ID
            if (filterModel.getId() != null && filterModel.getId() != 0) {
                whereClause.append(" AND A.SupplierRefId = ").append(filterModel.getId());
            }

            // Filter by employee ID
            if (filterModel.getEmployeeid() != null && filterModel.getEmployeeid() != 0) {
                whereClause.append(" AND A.EmployeeRefId = ").append(filterModel.getEmployeeid());
            }

            // Filter by truck ID
            if (filterModel.getTId() != null && filterModel.getTId() != 0) {
                whereClause.append(" AND A.TruckRefid = ").append(filterModel.getTId());
            }

            // Filter by product ID
            if (filterModel.getDId() != null && filterModel.getDId() != 0) {
                whereClause.append(" AND B.ProductRefId = ").append(filterModel.getDId());
            }

            // Filter by bill status
            if (filterModel.getStatus() != null && !filterModel.getStatus().isEmpty()) {
                whereClause.append(" AND A.BillStatus = '").append(filterModel.getStatus().replace("'", "''")).append("'");
            }

            // Filter by off-vessel name
            if (filterModel.getOffvesselname() != null && !filterModel.getOffvesselname().isEmpty()) {
                whereClause.append(" AND A.Description = '").append(filterModel.getOffvesselname().replace("'", "''")).append("'");
            }

            // Global search filter (clears other filters if provided)
            if (filterModel.getSearch() != null && !filterModel.getSearch().isEmpty()) {
                String search = filterModel.getSearch().replace("'", "''");
                whereClause.setLength(0); // Clear where clause for search
                whereClause.append(" AND (A.CNumberDisplay = '").append(search)
                        .append("' OR A.InvoiceNo = '").append(search)
                        .append("' OR SA.CNumberDisplay = '").append(search)
                        .append("' OR B.SerialNo = '").append(search).append("')");
            } else {
                // Date range filter (only if no global search is provided)
                if (filterModel.getFromdate() != null && filterModel.getTodate() != null) {
                    if (filterModel.getRemarks() != null && filterModel.getRemarks() == 1) {
                        whereClause.append(" AND A.InvoiceDate BETWEEN '").append(filterModel.getFromdate())
                                .append("' AND '").append(filterModel.getTodate()).append("'");
                    } else {
                        whereClause.append(" AND A.SaleDate BETWEEN '").append(filterModel.getFromdate())
                                .append("' AND '").append(filterModel.getTodate()).append("'");
                    }
                }
            }

            // Filter by vessel name (HIGHEST PRIORITY - clears all other filters including search and dates)
            // NOTE: When VessalNameSearch is provided, date filters are NOT applied (matches .NET behavior)
            // Equivalent to .NET: where = " and (A.OffVessal = '" + objlist.VessalNameSearch + "' ..."
            if (filterModel.getVessalNameSearch() != null && !filterModel.getVessalNameSearch().isEmpty()) {
                String vessel = filterModel.getVessalNameSearch();
                whereClause.setLength(0); // Clear all previous filters (including dates) - vessel search takes absolute priority
                whereClause.append(" AND (A.OffVessal = '").append(vessel.replace("'", "''"))
                        .append("' OR A.LodingVessal = '").append(vessel.replace("'", "''"))
                        .append("' OR A.Remarks = '").append(vessel.replace("'", "''")).append("')");
            }

            // Build final SQL query for master records
            String masterSql = "SELECT DISTINCT A.Id, A.PStatus, ISNULL(E.EmployeeName, '') AS EmployeeName, " +
                    "FORMAT(ISNULL(A.SaleDate, '1900-01-01'), 'dd/MM/yyyy') AS BillDate, A.InvoiceNo, " +
                    "FORMAT(ISNULL(A.InvoiceDate, '1900-01-01'), 'dd/MM/yyyy') AS InvoiceDate, " +
                    "A.CNumberDisplay AS BillNoDisplay, " +
                    "FORMAT(ISNULL(A.Created_Date, '1900-01-01'), 'dd/MM/yyyy HH:mm:ss') AS BillTime, " +
                    "S.SupplierName, A.Amount AS NetAmt, A.SaleType, A.CNumber AS BillNo, " +
                    "ISNULL(J.TruckName, '') AS TruckName, ISNULL(K.DriverName, '') AS DriverName, " +
                    "ISNULL(SA.CNumberDisplay, '') AS BillNoDisplay1, A.BillStatus, A.PayTo, " +
                    "A.Description, A.Fileupload " +
                    "FROM BillsOrderMaster A WITH(NOLOCK) " +
                    "INNER JOIN Supplier S WITH(NOLOCK) ON A.SupplierRefId = S.Id " +
                    "LEFT JOIN EmployeeMaster E WITH(NOLOCK) ON E.Id = A.EmployeeRefId " +
                    "LEFT JOIN TruckMaster J WITH(NOLOCK) ON J.Id = A.TruckRefid " +
                    "LEFT JOIN DriverMaster K WITH(NOLOCK) ON K.Id = A.DriverRefid " +
                    "LEFT JOIN SaleOrderMaster SA WITH(NOLOCK) ON SA.Id = A.SaleMasterRefId " +
                    "LEFT JOIN BillsOrderDetails B WITH(NOLOCK) ON A.Id = B.BillsOrderMasterRefId " +
                    "WHERE A.CompanyRefId = " + filterModel.getComid() + " AND A.Active = 1 " + whereClause;

            // Build SQL query for detail records
            String detailSql = "SELECT B.DiscAmount AS DiscountAmt, B.DiscPer AS DiscountPercent, " +
                    "B.ItemQty, B.MRP, I.Description AS ProductName, B.SalesRate AS SaleRate, " +
                    "B.BillsOrderMasterRefId AS SaleRefId, A.TaxAmount AS TaxAmt, B.TaxPercent, " +
                    "I.GlAccountCode AS ProductCode, B.Amount AS SAmount, B.RemarksD, B.SerialNo, " +
                    "B.QuoteValue " +
                    "FROM BillsOrderDetails B WITH(NOLOCK) " +
                    "INNER JOIN BillsOrderMaster A WITH(NOLOCK) ON B.BillsOrderMasterRefId = A.Id " +
                    "LEFT JOIN SaleOrderMaster SA WITH(NOLOCK) ON SA.Id = A.SaleMasterRefId " +
                    "INNER JOIN GLAccounts I WITH(NOLOCK) ON B.AccountMasterRefId = I.RowIndex " +
                    "WHERE A.CompanyRefId = " + filterModel.getComid() + " AND A.Active = 1 " + whereClause;

            logger.debug("Executing master SQL query: {}", masterSql);
            List<Map<String, Object>> masterRows = namedParameterJdbcTemplate.queryForList(
                    masterSql, new MapSqlParameterSource());

            List<BillsOrderMasterViewDto> masterList = masterRows.stream().map(row ->
                    BillsOrderMasterViewDto.builder()
                            .id(row.get("Id") != null ? ((Number) row.get("Id")).intValue() : 0)
                            .pStatus(row.get("PStatus") != null ? ((Number) row.get("PStatus")).intValue() : 0)
                            .employeeName((String) row.get("EmployeeName"))
                            .billDate((String) row.get("BillDate"))
                            .invoiceNo((String) row.get("InvoiceNo"))
                            .invoiceDate((String) row.get("InvoiceDate"))
                            .billNoDisplay((String) row.get("BillNoDisplay"))
                            .billTime((String) row.get("BillTime"))
                            .supplierName((String) row.get("SupplierName"))
                            .netAmt(row.get("NetAmt") != null ? ((Number) row.get("NetAmt")).floatValue() : 0f)
                            .saleType((String) row.get("SaleType"))
                            .billNo(row.get("BillNo") != null ? ((Number) row.get("BillNo")).intValue() : 0)
                            .truckName((String) row.get("TruckName"))
                            .driverName((String) row.get("DriverName"))
                            .billNoDisplay1((String) row.get("BillNoDisplay1"))
                            .billStatus((String) row.get("BillStatus"))
                            .payTo((String) row.get("PayTo"))
                            .description((String) row.get("Description"))
                            .fileupload(row.get("Fileupload") != null ? ((Number) row.get("Fileupload")).intValue() : 0)
                            .build()
            ).collect(Collectors.toList());

            logger.debug("Executing detail SQL query: {}", detailSql);
            List<Map<String, Object>> detailRows = namedParameterJdbcTemplate.queryForList(
                    detailSql, new MapSqlParameterSource());

            List<BillsOrderDetailsViewDto> detailList = detailRows.stream().map(row ->
                    BillsOrderDetailsViewDto.builder()
                            .discountAmt(row.get("DiscountAmt") != null ? ((Number) row.get("DiscountAmt")).floatValue() : 0f)
                            .discountPercent(row.get("DiscountPercent") != null ? ((Number) row.get("DiscountPercent")).floatValue() : 0f)
                            .itemQty(row.get("ItemQty") != null ? ((Number) row.get("ItemQty")).floatValue() : 0f)
                            .mrp(row.get("MRP") != null ? ((Number) row.get("MRP")).floatValue() : 0f)
                            .productName((String) row.get("ProductName"))
                            .saleRate(row.get("SaleRate") != null ? ((Number) row.get("SaleRate")).floatValue() : 0f)
                            .saleRefId(row.get("SaleRefId") != null ? ((Number) row.get("SaleRefId")).intValue() : 0)
                            .taxAmt(row.get("TaxAmt") != null ? ((Number) row.get("TaxAmt")).floatValue() : 0f)
                            .taxPercent(row.get("TaxPercent") != null ? ((Number) row.get("TaxPercent")).floatValue() : 0f)
                            .productCode((String) row.get("ProductCode"))
                            .sAmount(row.get("SAmount") != null ? ((Number) row.get("SAmount")).floatValue() : 0f)
                            .remarksD((String) row.get("RemarksD"))
                            .serialNo((String) row.get("SerialNo"))
                            .quoteValue(row.get("QuoteValue") != null ? ((Number) row.get("QuoteValue")).floatValue() : 0f)
                            .build()
            ).collect(Collectors.toList());

            // Sort master records by PStatus (0 first), then by BillDate
            masterList.sort((a, b) -> {
                int statusCompare = Integer.compare(a.getPStatus(), b.getPStatus());
                if (statusCompare != 0) return statusCompare;
                return a.getBillDate().compareTo(b.getBillDate());
            });

            result.setBillsOrderMaster(masterList);
            result.setBillsOrderDetails(detailList);

            logger.info("SelectBillsOrderView completed: {} master records, {} detail records",
                    masterList.size(), detailList.size());

        } catch (Exception ex) {
            logger.error("Error in selectBillsOrderView: {}", ex.getMessage(), ex);
            throw new RuntimeException("Error retrieving bills order view: " + ex.getMessage());
        }

        return result;
    }
}



