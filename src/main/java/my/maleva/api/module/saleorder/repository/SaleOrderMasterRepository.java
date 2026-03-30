package my.maleva.api.module.saleorder.repository;

import my.maleva.api.module.saleorder.entity.SaleOrderMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * SaleOrderMasterRepository - Repository for SaleOrderMaster with dynamic query support
 * Extended with JpaSpecificationExecutor for complex filtering
 */
@Repository
public interface SaleOrderMasterRepository extends JpaRepository<SaleOrderMaster, Integer>,
        JpaSpecificationExecutor<SaleOrderMaster> {

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN TRUE ELSE FALSE END FROM SaleOrderMaster s WHERE s.companyRefId = :companyRefId AND s.cNumber = :cNumber")
    boolean existsByCompanyRefIdAndCNumber(@Param("companyRefId") Integer companyRefId, @Param("cNumber") Integer cNumber);

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
            "A.InvoiceNo AS InvoiceId, " +                                                     // 2
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
            "LEFT JOIN SaleMaster SM WITH(NOLOCK) ON SM.id = A.InvoiceNo " +
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
}





