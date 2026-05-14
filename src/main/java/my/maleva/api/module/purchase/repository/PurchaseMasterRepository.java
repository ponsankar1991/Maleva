package my.maleva.api.module.purchase.repository;

import my.maleva.api.module.purchase.entity.PurchaseMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

@Repository
public interface PurchaseMasterRepository extends JpaRepository<PurchaseMaster, Integer> {

    /**
     * Find all PurchaseMaster records by company ID
     */
    List<PurchaseMaster> findByCompanyRefId(Integer companyRefId);

    /**
     * Find active PurchaseMaster records by company ID
     */
    List<PurchaseMaster> findByCompanyRefIdAndActive(Integer companyRefId, Integer active);

    /**
     * Find PurchaseMaster by company and supplier
     */
    List<PurchaseMaster> findByCompanyRefIdAndSupplierRefId(Integer companyRefId, Integer supplierRefId);

    /**
     * Find PurchaseMaster by sale type
     */
    List<PurchaseMaster> findByCompanyRefIdAndSaleType(Integer companyRefId, String saleType);

    /**
     * Find PurchaseMaster by invoice number
     */
    Optional<PurchaseMaster> findByCompanyRefIdAndInvoiceNo(Integer companyRefId, String invoiceNo);

    /**
     * Find PurchaseMaster by CNumber
     */
    Optional<PurchaseMaster> findByCompanyRefIdAndCNumber(Integer companyRefId, Integer cNumber);

    /**
     * Find PurchaseMaster by date range
     */
    List<PurchaseMaster> findByCompanyRefIdAndSaleDateBetween(Integer companyRefId, LocalDate startDate, LocalDate endDate);

    /**
     * Find PurchaseMaster by employee
     */
    List<PurchaseMaster> findByCompanyRefIdAndEmployeeRefId(Integer companyRefId, Integer employeeRefId);

    /**
     * Check if CNumber exists
     */
    boolean existsByCompanyRefIdAndCNumber(Integer companyRefId, Integer cNumber);

    /**
     * Find PurchaseMaster by purchase order reference
     */
    Optional<PurchaseMaster> findByPurchaseOrderMasterRefId(Integer purchaseOrderMasterRefId);

    /**
     * Count purchases by company
     */
    long countByCompanyRefId(Integer companyRefId);

    /**
     * Count active purchases by company
     */
    long countByCompanyRefIdAndActive(Integer companyRefId, Integer active);

    /**
     * Calculate total payment amount for a purchase order
     * Sums PaymentAmount from PaymentDetails where PurchaseMasterRefId matches
     * and PurchaseMaster has correct company and active status
     */
    @Query(value = """
        SELECT COALESCE(SUM(pd.paymentAmount), 0)
        FROM PaymentDetails pd
        INNER JOIN PurchaseMaster pm ON pd.purchaseMasterRefId = pm.id
        WHERE pm.id = :purchaseId
        AND pm.companyRefId = :companyId
        AND pm.active != 2
        """, nativeQuery = true)
    BigDecimal checkEditAmount(@Param("purchaseId") Integer purchaseId, @Param("companyId") Integer companyId);

    /**
     * Get distinct descriptions from PurchaseMaster for a company
     * Equivalent to .NET SelectDescription method
     * Filters out empty/null descriptions and inactive records
     */
    @Query("SELECT DISTINCT TRIM(p.description) FROM PurchaseMaster p " +
           "WHERE p.companyRefId = :companyId " +
           "AND p.active != 2 " +
           "AND p.description IS NOT NULL " +
           "AND TRIM(p.description) != ''")
    List<String> findDistinctDescriptionsByCompanyId(@Param("companyId") Integer companyId);

    /**
     * Insert PurchaseMaster using stored procedure SP_PurchaseMaster
     * Equivalent to .NET InsertPurchaseMaster method
     * ✅ FIXED: Using raw EXEC without subquery to avoid Hibernate type inference issues
     * @param jsonDetails JSON string of PurchaseMasterModel list
     * @param companyId Company identifier
     * @return ResultModel containing execution result
     */
    @Query(value = "EXEC [SP_PurchaseMaster] :jsonDetails, :companyId", nativeQuery = true)
    List<Object[]> executeInsertPurchaseMaster(@Param("jsonDetails") String jsonDetails, @Param("companyId") Integer companyId);

    /**
     * Get spare parts report view with multiple filters
     * Equivalent to .NET SelectSparePartsView method
     * Performs complex JOIN with Supplier, Employee, Truck, Driver, ProductMaster
     * Supports filtering by supplier, employee, driver, truck, product, and date/invoice search
     * 
     * @param companyId Company identifier
     * @param supplierId Optional supplier filter (0 = no filter)
     * @param employeeId Optional employee filter (0 = no filter)
     * @param driverId Optional driver filter (0 = no filter)
     * @param truckId Optional truck filter (0 = no filter)
     * @param productId Optional product filter (0 = no filter)
     * @param search Optional search by CNumberDisplay or InvoiceNo
     * @param invoiceCheck 1 = filter by InvoiceDate, 0 = filter by SaleDate
     * @param fromDate Start date for date range filter
     * @param toDate End date for date range filter
     * @return List of SparePartsReportView records matching the criteria
     */
    @Query(value = """
        SELECT DISTINCT 
            A.Id,
            ISNULL(E.EmployeeName, '') as EmployeeName,
            FORMAT(ISNULL(A.SaleDate, '1900-01-01'), 'dd/MM/yyyy') as BillDate,
            A.InvoiceNo,
            FORMAT(ISNULL(A.InvoiceDate, '1900-01-01'), 'dd/MM/yyyy') as InvoiceDate,
            A.CNumberDisplay as BillNoDisplay,
            FORMAT(ISNULL(A.Created_Date, '1900-01-01'), 'dd/MM/yyyy hh:mm:ss') as BillTime,
            B.SupplierName as SupplierName,
            A.Amount as NetAmt,
            A.SaleType as SaleType,
            A.CNumber as BillNo,
            ISNULL(J.TruckName, '') as TruckName,
            ISNULL(K.DriverName, '') as DriverName,
            PM.ItemQty,
            PM.SalesRate,
            PM.Amount,
            PM.RemarksD,
            ISNULL(PMS.Prod_Code, PM.ProductCode) AS ProductCode,
            ISNULL(PMS.PName, PM.ProductName) AS ProductName,
            A.SerialNo
        FROM PurchaseMaster A WITH(NOLOCK)
            LEFT JOIN PurchaseDetails PM WITH(NOLOCK) ON A.Id = PM.PurchaseMasterRefId
            INNER JOIN Supplier B WITH(NOLOCK) ON A.SupplierRefId = B.Id
            LEFT JOIN EmployeeMaster E WITH(NOLOCK) ON E.Id = A.EmployeeRefId
            LEFT JOIN TruckMaster J WITH(NOLOCK) ON J.Id = A.TruckRefid
            LEFT JOIN ProductMaster PMS WITH(NOLOCK) ON PMS.Id = PM.ProductMasterRefId
            LEFT JOIN DriverMaster K WITH(NOLOCK) ON K.Id = A.DriverRefid
        WHERE A.CompanyRefId = :companyId
            AND A.Active = 1
            AND (:supplierId = 0 OR A.SupplierRefId = :supplierId)
            AND (:employeeId = 0 OR A.EmployeeRefId = :employeeId)
            AND (:driverId = 0 OR A.DriverRefid = :driverId)
            AND (:truckId = 0 OR A.TruckRefid = :truckId)
            AND (:productId = 0 OR PM.ProductMasterRefId = :productId)
            AND (
                :search = '' OR :search IS NULL 
                OR A.CNumberDisplay = :search 
                OR A.InvoiceNo = :search
            )
            AND (
                :search != '' AND :search IS NOT NULL
                OR (
                    :invoiceCheck = 1 
                    AND CAST(A.InvoiceDate AS DATE) BETWEEN CAST(:fromDate AS DATE) AND CAST(:toDate AS DATE)
                )
                OR (
                    :invoiceCheck = 0 
                    AND CAST(A.SaleDate AS DATE) BETWEEN CAST(:fromDate AS DATE) AND CAST(:toDate AS DATE)
                )
            )
        ORDER BY A.Id DESC
        """, nativeQuery = true)
    List<Object[]> selectSparePartsView(
        @Param("companyId") Integer companyId,
        @Param("supplierId") Integer supplierId,
        @Param("employeeId") Integer employeeId,
        @Param("driverId") Integer driverId,
        @Param("truckId") Integer truckId,
        @Param("productId") Integer productId,
        @Param("search") String search,
        @Param("invoiceCheck") Integer invoiceCheck,
        @Param("fromDate") String fromDate,
        @Param("toDate") String toDate
    );

    /**
     * Get purchase master records with multiple filters
     * Equivalent to .NET SelectPurchaseMaster method
     * Performs complex JOIN with Supplier, Employee, Truck, Driver, ProductMaster
     * Supports filtering by supplier, employee, driver, truck, product, and date/invoice search
     * 
     * @param companyId Company identifier
     * @param supplierId Optional supplier filter (0 = no filter)
     * @param employeeId Optional employee filter (0 = no filter)
     * @param driverId Optional driver filter (0 = no filter)
     * @param truckId Optional truck filter (0 = no filter)
     * @param productId Optional product filter (0 = no filter)
     * @param search Optional search by CNumberDisplay or InvoiceNo
     * @param invoiceCheck 1 = filter by InvoiceDate, 0 = filter by SaleDate
     * @param fromDate Start date for date range filter
     * @param toDate End date for date range filter
     * @return List of PurchaseMaster records and associated details matching criteria
     */
     @Query(value = """
         SELECT 
             A.Id,
             ISNULL(E.EmployeeName, '') as EmployeeName,
             FORMAT(ISNULL(A.SaleDate, '1900-01-01'), 'dd/MM/yyyy') as BillDate,
             A.InvoiceNo,
             FORMAT(ISNULL(A.InvoiceDate, '1900-01-01'), 'dd/MM/yyyy') as InvoiceDate,
             A.CNumberDisplay as BillNoDisplay,
             FORMAT(ISNULL(A.Created_Date, '1900-01-01'), 'dd/MM/yyyy hh:mm:ss') as BillTime,
             B.SupplierName as SupplierName,
             A.Amount as NetAmt,
             A.SaleType as SaleType,
             A.CNumber as BillNo,
             ISNULL(J.TruckName, '') as TruckName,
             ISNULL(K.DriverName, '') as DriverName
         FROM PurchaseMaster A WITH(NOLOCK)
             LEFT JOIN PurchaseDetails PM WITH(NOLOCK) ON A.Id = PM.PurchaseMasterRefId
             INNER JOIN Supplier B WITH(NOLOCK) ON A.SupplierRefId = B.Id
             LEFT JOIN EmployeeMaster E WITH(NOLOCK) ON E.Id = A.EmployeeRefId
             LEFT JOIN TruckMaster J WITH(NOLOCK) ON J.Id = A.TruckRefid
             LEFT JOIN DriverMaster K WITH(NOLOCK) ON K.Id = A.DriverRefid
         WHERE A.CompanyRefId = :companyId
             AND A.Active = 1
             AND (:supplierId = 0 OR A.SupplierRefId = :supplierId)
             AND (:employeeId = 0 OR A.EmployeeRefId = :employeeId)
             AND (:driverId = 0 OR A.DriverRefid = :driverId)
             AND (:truckId = 0 OR A.TruckRefid = :truckId)
             AND (:productId = 0 OR PM.ProductMasterRefId = :productId)
             AND (
                 :search = '' OR :search IS NULL 
                 OR A.CNumberDisplay = :search 
                 OR A.InvoiceNo = :search
             )
             AND (
                 :search != '' AND :search IS NOT NULL
                 OR (
                     :invoiceCheck = 1 
                     AND CAST(A.InvoiceDate AS DATE) BETWEEN CAST(:fromDate AS DATE) AND CAST(:toDate AS DATE)
                 )
                 OR (
                     :invoiceCheck = 0 
                     AND CAST(A.SaleDate AS DATE) BETWEEN CAST(:fromDate AS DATE) AND CAST(:toDate AS DATE)
                 )
             )
         GROUP BY A.Id, E.EmployeeName, A.SaleDate, A.InvoiceNo, A.InvoiceDate, 
                  A.CNumberDisplay, A.Created_Date, B.SupplierName, A.Amount, A.SaleType,
                  A.CNumber, J.TruckName, K.DriverName
         ORDER BY A.SaleDate DESC, A.CNumber DESC
         """, nativeQuery = true)
    List<Object[]> selectPurchaseMaster(
        @Param("companyId") Integer companyId,
        @Param("supplierId") Integer supplierId,
        @Param("employeeId") Integer employeeId,
        @Param("driverId") Integer driverId,
        @Param("truckId") Integer truckId,
        @Param("productId") Integer productId,
        @Param("search") String search,
        @Param("invoiceCheck") Integer invoiceCheck,
        @Param("fromDate") String fromDate,
        @Param("toDate") String toDate
    );

    /**
     * Get purchase details records with multiple filters
     * Supporting query for SelectPurchaseMaster operation
     * Equivalent to the details portion of .NET SelectPurchaseMaster method
     * 
     * @param companyId Company identifier
     * @param supplierId Optional supplier filter (0 = no filter)
     * @param productId Optional product filter (0 = no filter)
     * @return List of PurchaseDetails records matching criteria
     */
    @Query(value = """
        SELECT 
            PM.DiscAmount as DiscountAmt,
            PM.DiscPer as DiscountPercent,
            PM.ItemQty,
            PM.MRP,
            ISNULL(NULLIF(I.PName, ''), PM.ProductName) as ProductName,
            PM.SalesRate as SaleRate,
            PM.PurchaseMasterRefId as SaleRefId,
            PM.TaxAmount as TaxAmt,
            PM.TaxPercent,
            ISNULL(NULLIF(I.Prod_Code, ''), PM.ProductCode) as ProductCode,
            PM.Amount as SAmount,
            PM.RemarksD
        FROM PurchaseDetails PM WITH(NOLOCK)
            INNER JOIN PurchaseMaster A WITH(NOLOCK) ON PM.PurchaseMasterRefId = A.Id
            LEFT JOIN ProductMaster I WITH(NOLOCK) ON PM.ProductMasterRefId = I.Id
        WHERE A.CompanyRefId = :companyId
            AND A.Active = 1
            AND (:supplierId = 0 OR A.SupplierRefId = :supplierId)
            AND (:productId = 0 OR PM.ProductMasterRefId = :productId)
        """, nativeQuery = true)
    List<Object[]> selectPurchaseDetails(
        @Param("companyId") Integer companyId,
        @Param("supplierId") Integer supplierId,
        @Param("productId") Integer productId
    );

    /**
     * Find ID of PurchaseMaster by CNumber and Company
     * Used for lookup when CNumber is provided instead of ID
     * Equivalent to: select id from PurchaseMaster where companyrefid=? and CNumber=?
     */
    @Query(value = """
        SELECT ISNULL(A.Id, 0)
        FROM PurchaseMaster A WITH(NOLOCK)
        WHERE A.CompanyRefId = :companyId
            AND A.CNumber = :cNumber
        """, nativeQuery = true)
    Integer findIdByCNumberAndCompany(
        @Param("companyId") Integer companyId,
        @Param("cNumber") Integer cNumber
    );

    /**
     * Get full PurchaseMaster record with all details for editing
     * Equivalent to .NET EditPurchaseMaster method - retrieves master + all details with joins
     *
     * Performs complex JOIN with:
     *  - PurchaseDetails (join on master.Id = detail.PurchaseMasterRefId)
     *  - ProductMaster (join on detail.ProductMasterRefId = product.Id)
     *  - UOM (join on product.UOM_Code = uom.Id)
     *
     * Returns flattened result set with combined master and detail fields
     *
     * @param id Purchase Master ID
     * @param companyId Company ID for validation
     * @return List of Object[] containing all master and detail data
     */
    @Query(value = """
        SELECT 
            A.Id,
            A.CompanyRefId,
            A.UserRefId,
            A.EmployeeRefId,
            A.InvoiceNo,
            A.InvoiceDate,
            A.InvoiceDate as SInvoiceDate,
            A.SupplierRefId,
            A.SaleDate,
            A.SaleDate as SSaleDate,
            A.SaleType,
            A.CNumberDisplay,
            A.CNumber,
            A.Coinage,
            A.GrossAmount,
            A.TaxAmount,
            A.DiscountAmount,
            A.PlusAmount,
            A.MinusAmount,
            A.Amount,
            A.Remarks,
            A.Active,
            A.Created_Date,
            A.Created_By,
            A.Modified_Date,
            A.Modified_By,
            A.Description,
            A.PaymentTermsRefid,
            A.SerialNo,
            A.TruckRefid,
            A.DriverRefid,
            B.Id as SDId,
            B.ProductMasterRefId,
            B.MRP,
            B.PurchaseRate,
            B.ItemQty,
            ISNULL(B.DiscPer, 0) as DiscPer,
            ISNULL(B.DiscAmount, 0) as DiscAmount,
            B.LandingCost,
            B.TaxPercent,
            B.TaxAmount,
            B.SalesRate,
            ISNULL(B.NetSalesRate, 0) as NetSalesRate,
            B.Amount,
            B.RemarksD,
            ISNULL(NULLIF(I.Prod_Code, ''), B.ProductCode) as ProductCode,
            ISNULL(NULLIF(I.PName, ''), B.ProductName) as ProductName,
            ISNULL(UM.Description, '') as UOM
        FROM PurchaseMaster A WITH(NOLOCK)
            INNER JOIN PurchaseDetails B WITH(NOLOCK) ON A.Id = B.PurchaseMasterRefId
            LEFT JOIN ProductMaster I WITH(NOLOCK) ON I.Id = B.ProductMasterRefId
            LEFT JOIN UOM UM WITH(NOLOCK) ON UM.Id = I.UOM_Code
        WHERE A.Id = :id
            AND A.CompanyRefId = :companyId
        ORDER BY B.Id
        """, nativeQuery = true)
    List<Object[]> findEditPurchaseMaster(
        @Param("id") Integer id,
        @Param("companyId") Integer companyId
    );
}
