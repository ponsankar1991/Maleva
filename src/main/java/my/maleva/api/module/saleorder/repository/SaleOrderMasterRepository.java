package my.maleva.api.module.saleorder.repository;

import my.maleva.api.module.rti.dto.RTIJobLookupDto;
import my.maleva.api.module.saleorder.dto.JobNumberDto;
import my.maleva.api.module.saleorder.entity.SaleOrderMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * SaleOrderMasterRepository - Repository for SaleOrderMaster with dynamic query support
 * Extended with JpaSpecificationExecutor for complex filtering
 */
@Repository
public interface SaleOrderMasterRepository extends JpaRepository<SaleOrderMaster, Integer>,
        JpaSpecificationExecutor<SaleOrderMaster> {

    @Query("""
            select new my.maleva.api.module.rti.dto.RTIJobLookupDto(
                    s.id,
                    s.cNumberDisplay,
                    s.saleDate,
                    c.customerName
            )
            from SaleOrderMaster s
            join Customer c on c.id = s.customerRefId
            where s.companyRefId = :companyRefId
              and c.companyRefId = s.companyRefId
              and s.active <> 2
              and s.cNumberDisplay = :jobNo
            order by s.id desc
            """)
    List<RTIJobLookupDto> findRTIJobLookupByCompanyRefIdAndJobNo(@Param("companyRefId") Integer companyRefId, @Param("jobNo") String jobNo);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN TRUE ELSE FALSE END FROM SaleOrderMaster s WHERE s.companyRefId = :companyRefId AND s.cNumber = :cNumber")
    boolean existsByCompanyRefIdAndCNumber(@Param("companyRefId") Integer companyRefId, @Param("cNumber") Integer cNumber);

    /**
     * Loads an active sale order because update and delete flows should not
     * operate on already inactive records.
     *
     * @param id sale-order identifier
     * @param active active flag
     * @return matching active sale order when available
     */
    Optional<SaleOrderMaster> findByIdAndActive(Integer id, Integer active);

    Optional<SaleOrderMaster> findByCompanyRefIdAndCNumberAndActive(Integer companyRefId, Integer cNumber, Integer active);

    /**
     * Get customer job numbers for a given company and customer
     * 
     * Business Logic (from GetCustJobNo endpoint):
     * 1. Filter by company (multi-tenancy) - required
     * 2. Filter by customer (if custId != 0) - optional
     * 3. Exclude soft-deleted records (Active != 2)
     * 4. Filter by invoice number (if invoiceNo > 0, exact match; if = 0, not yet invoiced)
     * 
     * @param companyRefId Company ID (tenant identifier)
     * @param customerRefId Customer ID (0 means all customers)
     * @param invoiceNo Invoice number (0 means not yet invoiced, >0 means specific invoice)
     * @return List of job records with Id and billNoDisplay (CNumberDisplay)
     */
    @Query(value = 
        "SELECT new my.maleva.api.module.saleorder.dto.JobNumberDto(" +
        "  s.id, " +
        "  s.cNumberDisplay" +
        ") " +
        "FROM SaleOrderMaster s " +
        "WHERE s.companyRefId = :companyRefId " +
        "  AND s.active != 2 " +
        "  AND (s.customerRefId = :customerRefId) " +
        "  AND ( s.invoiceNo = :invoiceNo) " +
        "ORDER BY s.cNumberDisplay ASC")
    List<JobNumberDto> findCustJobNumbers(
        @Param("companyRefId") Integer companyRefId,
        @Param("customerRefId") Integer customerRefId,
        @Param("invoiceNo") Integer invoiceNo);

    /**
     * OPTIMIZED: Fetch SaleMaster data filtered by specific order IDs
     * This is the key performance optimization - only fetches matching records from DB
     * Instead of fetching 100K+ records and filtering in memory, fetches only what's needed
     * 
     * Column order (33 total) - same as findSaleMasterRawDataWithJoins:
     * 0=Id, 1=sportsaleorderid, 2=InvoiceId, 3=Remarks, 4=Destination, 5=FlighTime,
     * 6=Origin, 7=JobMasterRefId, 8=EmployeeName, 9=Offvesselname, 10=Sname,
     * 11=Loadingvesselname, 12=SPort, 13=OPort, 14=BillDate, 15=DETA (for sorting),
     * 16=ETA, 17=SETA, 18=SETB, 19=SOETA, 20=SOETB, 21=SPickupDate, 22=BillNoDisplay,
     * 23=BillTime, 24=CustomerName, 25=JobType, 26=NetAmt, 27=SaleType, 28=BillNo,
     * 29=JobStatus, 30=InvoiceNo, 31=QNECode, 32=QNEId
     * 
     * @param companyId the company ID
     * @param orderIds the specific order IDs to fetch
     * @return List of Object arrays for the matched orders only
     */
    @Query(value = "SELECT " +
            "A.Id, " +                                                                         // 0
            "A.sportsaleorderid, " +                                                           // 1
            "ISNULL(SM.InvoiceId, A.InvoiceNo) AS InvoiceId, " +                               // 2
            "A.Remarks, " +                                                                    // 3
            "A.Destination, " +                                                                // 4
            "A.FlighTime, " +                                                                  // 5
            "A.Origin, " +                                                                     // 6
            "A.JobMasterRefId, " +                                                             // 7
            "ISNULL(E.EmployeeName, '') AS EmployeeName, " +                                   // 8
            "A.Offvesselname, " +                                                              // 9
            "S.Sname, " +                                                                      // 10
            "A.Loadingvesselname, " +                                                          // 11
            "A.SPort, " +                                                                      // 12
            "A.OPort, " +                                                                      // 13
            "FORMAT(ISNULL(A.SaleDate, '1900-01-01'), 'dd/MM/yyyy') AS BillDate, " +          // 14
            "ISNULL(FORMAT(A.ETA, 'dd/MM/yyyy'), FORMAT(A.OETA, 'dd/MM/yyyy')) AS DETA, " +   // 15 - DETA for sorting
            "A.ETA, " +                                                                        // 16
            "ISNULL(FORMAT(A.ETA, 'dd/MM/yyyy HH:mm:ss'), '') AS SETA, " +                    // 17
            "ISNULL(FORMAT(A.ETB, 'dd/MM/yyyy HH:mm:ss'), '') AS SETB, " +                    // 18
            "ISNULL(FORMAT(A.OETA, 'dd/MM/yyyy HH:mm:ss'), '') AS SOETA, " +                  // 19
            "ISNULL(FORMAT(A.OETB, 'dd/MM/yyyy HH:mm:ss'), '') AS SOETB, " +                  // 20
            "ISNULL(CONVERT(VARCHAR(26), A.PickupDate, 20), '') AS SPickupDate, " +           // 21
            "A.CNumberDisplay AS BillNoDisplay, " +                                            // 22
            "FORMAT(ISNULL(A.Created_Date, '1900-01-01'), 'dd/MM/yyyy hh:mm:ss') AS BillTime, " + // 23
            "B.CustomerName, " +                                                               // 24
            "ISNULL(JT.Name, '') AS JobType, " +                                              // 25
            "A.Amount AS NetAmt, " +                                                           // 26
            "A.SaleType, " +                                                                   // 27
            "A.CNumber AS BillNo, " +                                                          // 28
            "ISNULL(J.Name, '') AS JobStatus, " +                                             // 29
            "ISNULL(SM.CNumberDisplay, '') AS InvoiceNo, " +                                  // 30
            "ISNULL(SM.QNECode, '') AS QNECode, " +                                           // 31
            "ISNULL(SM.QNEId, '') AS QNEId " +                                                // 32
            "FROM SaleOrderMaster A WITH(NOLOCK) " +
            "INNER JOIN Customer B WITH(NOLOCK) ON A.CustomerRefId = B.Id " +
            "LEFT JOIN EmployeeMaster E WITH(NOLOCK) ON E.Id = A.EmployeeRefId " +
            "LEFT JOIN JobStatusMaster J WITH(NOLOCK) ON J.Id = A.JStatus " +
            "LEFT JOIN JobTypeMaster JT WITH(NOLOCK) ON JT.Id = A.JobMasterRefId " +
            "OUTER APPLY ( " +
            "    SELECT TOP 1 " +
            "        SM1.Id AS InvoiceId, " +
            "        SM1.CNumberDisplay, " +
            "        SM1.QNECode, " +
            "        SM1.QNEId " +
            "    FROM SaleMaster SM1 WITH(NOLOCK) " +
            "    WHERE SM1.CompanyRefId = A.CompanyRefId " +
            "      AND SM1.Active = 1 " +
            "      AND ( " +
            "          (A.InvoiceNo IS NOT NULL AND A.InvoiceNo > 0 AND SM1.Id = A.InvoiceNo) " +
            "          OR SM1.SaleOrderMasterNo = A.Id " +
            "      ) " +
            "    ORDER BY " +
            "        CASE WHEN A.InvoiceNo IS NOT NULL AND A.InvoiceNo > 0 AND SM1.Id = A.InvoiceNo THEN 0 ELSE 1 END, " +
            "        SM1.Id DESC " +
            ") SM " +
            "INNER JOIN SymbolMaster S WITH(NOLOCK) ON B.SymbolRefid = S.Id " +
            "WHERE A.CompanyRefId = :companyId AND A.Active = 1 " +
            "AND A.Id IN (:orderIds) " +                          // ← KEY OPTIMIZATION: Filter at DB level
            "ORDER BY ISNULL(A.ETA, A.OETA) DESC, A.SaleDate DESC", nativeQuery = true)
    List<Object[]> findSaleMasterRawDataWithJoinsByOrderIds(
            @Param("companyId") Integer companyId,
            @Param("orderIds") List<Integer> orderIds);

    /**
     * OPTIMIZED: Fetch SaleDetails data filtered by specific order IDs
     * This is the key performance optimization - only fetches matching records from DB
     * Instead of fetching 200K+ records and filtering in memory, fetches only what's needed
     * 
     * Expected columns (14 total):
     * 0=DiscAmount, 1=DiscPer, 2=ItemQty, 3=MRP, 4=PName, 5=SDRemarks,
     * 6=SalesRate, 7=SaleOrderMasterRefId, 8=TaxAmount, 9=TaxPercent,
     * 10=Prod_Code, 11=Amount, 12=CurrencyValue, 13=ActualAmount
     * 
     * @param companyId the company ID
     * @param orderIds the specific order IDs to fetch details for
     * @return List of Object arrays for the matched order details only
     */
    @Query(value = "SELECT " +
            "B.DiscAmount, " +
            "B.DiscPer, " +
            "B.ItemQty, " +
            "B.MRP, " +
            "I.PName, " +
            "B.SDRemarks, " +
            "B.SalesRate, " +
            "B.SaleOrderMasterRefId, " +
            "A.TaxAmount, " +
            "B.TaxPercent, " +
            "I.Prod_Code, " +
            "B.Amount, " +
            "ISNULL(B.CurrencyValue, 0) AS CurrencyValue, " +
            "ISNULL(B.ActualAmount, 0) AS ActualAmount " +
            "FROM SaleOrderDetails B WITH(NOLOCK) " +
            "INNER JOIN SaleOrderMaster A WITH(NOLOCK) ON B.SaleOrderMasterRefId = A.Id " +
            "INNER JOIN ItemMaster I WITH(NOLOCK) ON B.ItemMasterRefId = I.Id " +
            "LEFT JOIN SaleMaster SM WITH(NOLOCK) ON SM.id = A.InvoiceNo " +
            "WHERE A.CompanyRefId = :companyId AND A.Active = 1 " +
            "AND B.SaleOrderMasterRefId IN (:orderIds) " +         // ← KEY OPTIMIZATION: Filter at DB level
            "ORDER BY B.Id", nativeQuery = true)
    List<Object[]> findSaleDetailsRawDataWithJoinsByOrderIds(
            @Param("companyId") Integer companyId,
            @Param("orderIds") List<Integer> orderIds);


    @Query(
            value = """
            SELECT COUNT(S.Id)
            FROM   SaleOrderMaster S WITH (NOLOCK)
            WHERE  S.CompanyRefId = :companyId
              AND  S.Id           = :jobId
              AND  (
                       (S.PortCPop       = 1 AND ISNULL(S.Notportchagre,  0) = 0)
                    OR (S.BoatCPop       = 1 AND ISNULL(S.NotBoatCPop,    0) = 0)
                    OR (S.BoatCPop1      = 1 AND ISNULL(S.NotBoatCPop1,   0) = 0)
                    OR (S.ForwardingCPop = 1 AND ISNULL(S.NotForwardingCPop, 0) = 0)
                    OR (S.PermitCPop     = 1 AND ISNULL(S.NotPermitCPop,  0) = 0)
                    OR (S.LiveCPop       = 1 AND ISNULL(S.NotLevyChares,  0) = 0)
                    OR (S.MMHECPop       = 1 AND ISNULL(S.NotMMHECPop,    0) = 0)
                    OR (S.AFpoCPop       = 1 AND ISNULL(S.NotAFpoCPop,    0) = 0)
                    OR (S.SFWpoCPop      = 1 AND ISNULL(S.NotSFWpoCPop,   0) = 0)
                    OR (S.SFEWpoCPop     = 1 AND ISNULL(S.NotSFEWpoCPop,  0) = 0)
                    OR (S.PFPPCPop1      = 1 AND ISNULL(S.NotPFPPCPop1,   0) = 0)
                   )
            """,
            nativeQuery = true
    )
    int countPendingPortCharges(
            @Param("companyId") int companyId,
            @Param("jobId")     int jobId
    );

}
