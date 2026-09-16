package my.maleva.api.module.saleorder.repository;

import my.maleva.api.module.rti.dto.RTIJobLookupDto;
import my.maleva.api.module.saleorder.dto.JobNumberDto;
import my.maleva.api.module.saleorder.dto.VesselScheduleDto;
import my.maleva.api.module.saleorder.dto.VesselActivityReportProjection;
import my.maleva.api.module.saleorder.entity.SaleOrderMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
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
        JpaSpecificationExecutor<SaleOrderMaster>, SaleOrderMasterRepositoryCustom {

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

    @Query(value = """
        SELECT g.*,
            (
                SELECT STRING_AGG(Emp.EmployeeName, ', ')
                FROM (
                    SELECT DISTINCT e.EmployeeName
                    FROM SaleOrderMaster s2
                    CROSS APPLY (
                        VALUES (s2.LBoardingOfficerRefid), (s2.LBoardingOfficer1Refid), (s2.LBoardingOfficer2Refid)
                    ) AS BO(EmpId)
                    JOIN EmployeeMaster e ON e.Id = BO.EmpId
                    WHERE 
                        s2.CompanyRefId = :companyRefId
                        AND s2.Loadingvesselname = g.vesselName
                        AND s2.SPort = g.portName
                        AND CAST(COALESCE(s2.ETA, s2.ETB) AS DATE) = g.activityDate
                        AND s2.Active != 2
                ) Emp
            ) AS boardingOfficers
        FROM (
            SELECT 
                CAST(COALESCE(ETA, ETB) AS DATE) AS activityDate,
                Loadingvesselname AS vesselName,
                'LOADING' AS activityType,
                COUNT(Id) AS jobCount,
                STRING_AGG(CNumberDisplay, ', ') AS cNumbers,
                SPort AS portName,
                MAX(ETA) AS eta,
                MAX(ETB) AS etb,
                CAST(NULL AS DATETIME) AS oeta,
                CAST(NULL AS DATETIME) AS oetb
            FROM 
                SaleOrderMaster
        WHERE 
            CompanyRefId = :companyRefId
            AND Loadingvesselname IS NOT NULL 
            AND Loadingvesselname != ''
            AND COALESCE(ETA, ETB) >= :fromDate 
            AND COALESCE(ETA, ETB) <= :toDate
            AND Active != 2
            AND (:portName IS NULL OR :portName = '' OR SPort = :portName)
            AND (ETA IS NOT NULL OR ETB IS NOT NULL)
        GROUP BY 
            CAST(COALESCE(ETA, ETB) AS DATE),
            Loadingvesselname,
            SPort
        ) g

        UNION ALL

        SELECT g.*,
            (
                SELECT STRING_AGG(Emp.EmployeeName, ', ')
                FROM (
                    SELECT DISTINCT e.EmployeeName
                    FROM SaleOrderMaster s2
                    CROSS APPLY (
                        VALUES (s2.OBoardingOfficerRefid), (s2.OBoardingOfficer1Refid), (s2.OBoardingOfficer2Refid)
                    ) AS BO(EmpId)
                    JOIN EmployeeMaster e ON e.Id = BO.EmpId
                    WHERE 
                        s2.CompanyRefId = :companyRefId
                        AND s2.Offvesselname = g.vesselName
                        AND s2.OPort = g.portName
                        AND CAST(COALESCE(s2.OETA, s2.OETB) AS DATE) = g.activityDate
                        AND s2.Active != 2
                ) Emp
            ) AS boardingOfficers
        FROM (
            SELECT 
                CAST(COALESCE(OETA, OETB) AS DATE) AS activityDate,
                Offvesselname AS vesselName,
                'OFFLOADING' AS activityType,
                COUNT(Id) AS jobCount,
                STRING_AGG(CNumberDisplay, ', ') AS cNumbers,
                OPort AS portName,
                CAST(NULL AS DATETIME) AS eta,
                CAST(NULL AS DATETIME) AS etb,
                MAX(OETA) AS oeta,
                MAX(OETB) AS oetb
            FROM 
                SaleOrderMaster
        WHERE 
            CompanyRefId = :companyRefId
            AND Offvesselname IS NOT NULL 
            AND Offvesselname != ''
            AND COALESCE(OETA, OETB) >= :fromDate 
            AND COALESCE(OETA, OETB) <= :toDate
            AND Active != 2
            AND (:portName IS NULL OR :portName = '' OR OPort = :portName)
            AND (OETA IS NOT NULL OR OETB IS NOT NULL)
        GROUP BY 
            CAST(COALESCE(OETA, OETB) AS DATE),
            Offvesselname,
            OPort
        ) g

        ORDER BY 
            activityDate ASC, 
            vesselName ASC,
            activityType ASC
        """, nativeQuery = true)
    List<VesselActivityReportProjection> getVesselActivityReport(@Param("companyRefId") Integer companyRefId, @Param("fromDate") String fromDate, @Param("toDate") String toDate, @Param("portName") String portName);

    Optional<SaleOrderMaster> findByIdAndCompanyRefId(Integer id, Integer companyRefId);

    /**
     * The Planning screen's Update window: sets only the columns that window edits (plus the
     * joined stop lists the grid and other screens read), in one targeted UPDATE. Saving the
     * whole entity instead writes every column the caller never loaded - that is how the
     * Planning update used to zero GrossAmount, TaxAmount, Amount and CurrencyValue.
     * The row count is not returned: SET NOCOUNT ON makes it -1 here, so callers check the
     * row exists first.
     */
    @Modifying(flushAutomatically = true)
    @Query("""
            update SaleOrderMaster s
               set s.pickupDate = :pickupDate,
                   s.deliveryDate = :deliveryDate,
                   s.wareHouseEnterDate = :wareHouseEnterDate,
                   s.wareHouseExitDate = :wareHouseExitDate,
                   s.wareHouseAddress = :wareHouseAddress,
                   s.origin = :origin,
                   s.destination = :destination,
                   s.originRefId = :originRefId,
                   s.destinationRefId = :destinationRefId,
                   s.quantity = :quantity,
                   s.totalWeight = :totalWeight,
                   s.pickupAddress = :pickupAddress,
                   s.deliveryAddress = :deliveryAddress,
                   s.pickupQuantitylist = :pickupQuantitylist,
                   s.deliveryQuantitylist = :deliveryQuantitylist,
                   s.quantityList = :quantityList,
                   s.lastEmployeeRefId = :lastEmployeeRefId,
                   s.modifiedDate = :modifiedDate
             where s.id = :id
               and s.companyRefId = :companyRefId
            """)
    void updatePlanningFields(@Param("id") Integer id,
                              @Param("companyRefId") Integer companyRefId,
                              @Param("pickupDate") java.time.LocalDateTime pickupDate,
                              @Param("deliveryDate") java.time.LocalDateTime deliveryDate,
                              @Param("wareHouseEnterDate") java.time.LocalDateTime wareHouseEnterDate,
                              @Param("wareHouseExitDate") java.time.LocalDateTime wareHouseExitDate,
                              @Param("wareHouseAddress") String wareHouseAddress,
                              @Param("origin") String origin,
                              @Param("destination") String destination,
                              @Param("originRefId") Integer originRefId,
                              @Param("destinationRefId") Integer destinationRefId,
                              @Param("quantity") String quantity,
                              @Param("totalWeight") String totalWeight,
                              @Param("pickupAddress") String pickupAddress,
                              @Param("deliveryAddress") String deliveryAddress,
                              @Param("pickupQuantitylist") String pickupQuantitylist,
                              @Param("deliveryQuantitylist") String deliveryQuantitylist,
                              @Param("quantityList") String quantityList,
                              @Param("lastEmployeeRefId") Integer lastEmployeeRefId,
                              @Param("modifiedDate") java.time.LocalDateTime modifiedDate);

    /**
     * The Vessel Planning screen's Update window: sets only the columns that window edits, in
     * one targeted UPDATE. It replaced a full PUT of the sale order built from a form that never
     * loaded the totals, which set GrossAmount, TaxAmount and Amount to 0.
     * The caller passes the stored value for anything the window leaves unchanged. The row count
     * is not returned (SET NOCOUNT ON makes it -1 here), so callers check the row exists first.
     * Clears the persistence context so a reload afterwards sees the new values.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update SaleOrderMaster s
               set s.jStatus = :jStatus,
                   s.cargo = :cargo,
                   s.ptw = :ptw,
                   s.eta = :eta,
                   s.etb = :etb,
                   s.etd = :etd,
                   s.oeta = :oeta,
                   s.oetb = :oetb,
                   s.oetd = :oetd,
                   s.lBoardingOfficerRefid = :loadingOfficer1,
                   s.lBoardingOfficer1Refid = :loadingOfficer2,
                   s.lBoardingOfficer2Refid = :loadingOfficer3,
                   s.lBoardingAmount = :loadingAmount1,
                   s.lBoardingAmount1 = :loadingAmount2,
                   s.lBoardingAmount2 = :loadingAmount3,
                   s.oBoardingOfficerRefid = :offOfficer1,
                   s.oBoardingOfficer1Refid = :offOfficer2,
                   s.oBoardingOfficer2Refid = :offOfficer3,
                   s.oBoardingAmount = :offAmount1,
                   s.oBoardingAmount1 = :offAmount2,
                   s.oBoardingAmount2 = :offAmount3,
                   s.modifiedDate = :modifiedDate
             where s.id = :id
               and s.companyRefId = :companyRefId
            """)
    void updateVesselPlanningFields(@Param("id") Integer id,
                                    @Param("companyRefId") Integer companyRefId,
                                    @Param("jStatus") Integer jStatus,
                                    @Param("cargo") String cargo,
                                    @Param("ptw") String ptw,
                                    @Param("eta") java.time.LocalDateTime eta,
                                    @Param("etb") java.time.LocalDateTime etb,
                                    @Param("etd") java.time.LocalDateTime etd,
                                    @Param("oeta") java.time.LocalDateTime oeta,
                                    @Param("oetb") java.time.LocalDateTime oetb,
                                    @Param("oetd") java.time.LocalDateTime oetd,
                                    @Param("loadingOfficer1") Integer loadingOfficer1,
                                    @Param("loadingOfficer2") Integer loadingOfficer2,
                                    @Param("loadingOfficer3") Integer loadingOfficer3,
                                    @Param("loadingAmount1") Double loadingAmount1,
                                    @Param("loadingAmount2") Double loadingAmount2,
                                    @Param("loadingAmount3") String loadingAmount3,
                                    @Param("offOfficer1") Integer offOfficer1,
                                    @Param("offOfficer2") Integer offOfficer2,
                                    @Param("offOfficer3") Integer offOfficer3,
                                    @Param("offAmount1") Double offAmount1,
                                    @Param("offAmount2") Double offAmount2,
                                    @Param("offAmount3") String offAmount3,
                                    @Param("modifiedDate") java.time.LocalDateTime modifiedDate);

    @Query("""
        SELECT s, c.customerName
        FROM SaleOrderMaster s
        LEFT JOIN my.maleva.api.module.customer.entity.Customer c ON s.customerRefId = c.id
        WHERE s.companyRefId = :companyId
          AND (
               :referenceStatus = 1 
               OR (:referenceStatus = 2 AND s.remarks IS NOT NULL AND TRIM(s.remarks) <> '') 
               OR (:referenceStatus = 0 AND (s.remarks IS NULL OR TRIM(s.remarks) = ''))
          )
          AND s.saleDate >= :fromDate
          AND s.saleDate < :toDate
          AND (:customerRefId IS NULL OR :customerRefId = 0 OR s.customerRefId = :customerRefId)
          AND s.active = 1
        ORDER BY s.saleDate ASC
    """)
    List<Object[]> findJobsWithMissingRemarks(
        @Param("companyId") Integer companyId, 
        @Param("fromDate") java.time.LocalDateTime fromDate, 
        @Param("toDate") java.time.LocalDateTime toDate,
        @Param("customerRefId") Integer customerRefId,
        @Param("referenceStatus") Integer referenceStatus
    );

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
     * Get customer job numbers for a given company and customercountPendingPortCharges
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

    /*
     * The SelectSaleOrder master/detail queries used to live here as native queries
     * returning List<Object[]>, read back by ordinal. They now live in
     * SaleOrderSearchQueries and are mapped by column label in
     * SaleOrderMasterRepositoryImpl, which also batches the id list.
     */


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
    @Query(value = """
            select s.SName as currencyName, s.CName as countryName, count(*) as jobCount 
            from SaleOrderMaster sm with(nolock) 
            inner join Customer j with(nolock) on sm.CustomerRefId=j.Id 
            inner join SymbolMaster s with(nolock) on j.SymbolRefid=s.Id 
            where sm.Active=1 
              and sm.SaleDate between :fromDate and :toDate 
              and sm.CompanyRefId = :comId 
              and (:empId = 0 OR sm.EmployeeRefId = :empId)
            group by s.SName, s.CName
            """, nativeQuery = true)
    List<SaleJobViewProjection> getJobViewByCurrency(
            @Param("comId") Integer comId,
            @Param("empId") Integer empId,
            @Param("fromDate") String fromDate,
            @Param("toDate") String toDate);

    @Query(value = """
            select j.EmployeeName as employeeName, count(*) as employeeCount 
            from SaleOrderMaster sm with(nolock) 
            left join EmployeeMaster j with(nolock) on sm.EmployeeRefId=j.Id 
            where sm.Active=1 
              and sm.SaleDate between :fromDate and :toDate 
              and sm.CompanyRefId = :comId 
              and (:empId = 0 OR sm.EmployeeRefId = :empId)
            group by j.EmployeeName
            """, nativeQuery = true)
    List<SaleJobViewProjection> getJobViewByEmployee(
            @Param("comId") Integer comId,
            @Param("empId") Integer empId,
            @Param("fromDate") String fromDate,
            @Param("toDate") String toDate);

    @Query(value = """
            select IsNull(j.Name, 'UnKnown') as jobType, count(*) as typeCount 
            from SaleOrderMaster sm with(nolock) 
            left join JobTypeMaster j with(nolock) on sm.JobMasterRefId=j.Id 
            where sm.Active=1 
              and sm.SaleDate between :fromDate and :toDate 
              and sm.CompanyRefId = :comId 
              and (:empId = 0 OR sm.EmployeeRefId = :empId)
            group by j.Name
            """, nativeQuery = true)
    List<SaleJobViewProjection> getJobViewByJobType(
            @Param("comId") Integer comId,
            @Param("empId") Integer empId,
            @Param("fromDate") String fromDate,
            @Param("toDate") String toDate);

    @Query(value = """
            select IsNull(j.Name, 'UnKnown') as jobStatus, count(*) as statusCount 
            from SaleOrderMaster sm with(nolock) 
            left join JobStatusMaster j with(nolock) on sm.JStatus=j.Id 
            where sm.Active=1 
              and sm.SaleDate between :fromDate and :toDate 
              and sm.CompanyRefId = :comId 
              and (:empId = 0 OR sm.EmployeeRefId = :empId)
            group by j.Name
            """, nativeQuery = true)
    List<SaleJobViewProjection> getJobViewByJobStatus(
            @Param("comId") Integer comId,
            @Param("empId") Integer empId,
            @Param("fromDate") String fromDate,
            @Param("toDate") String toDate);

    @Query(value = """
            SELECT j.CustomerName as customerName, s.SName AS currencyName, 
            SUM(CASE WHEN FORMAT(SI.SaleDate, 'MM-yyyy') = FORMAT(DATEADD(MONTH, -3, CONVERT(date, :fromDate)), 'MM-yyyy') THEN sm.ActualNetAmount ELSE 0 END) AS month1, 
            SUM(CASE WHEN FORMAT(SI.SaleDate, 'MM-yyyy') = FORMAT(DATEADD(MONTH, -2,  CONVERT(date, :fromDate)), 'MM-yyyy') THEN sm.ActualNetAmount ELSE 0 END) AS month2, 
            SUM(CASE WHEN FORMAT(SI.SaleDate, 'MM-yyyy') = FORMAT(DATEADD(MONTH, -1,  CONVERT(date, :fromDate)), 'MM-yyyy') THEN sm.ActualNetAmount ELSE 0 END) AS month3, 
            SUM(CASE WHEN FORMAT(SI.SaleDate, 'MM-yyyy') = FORMAT(GETDATE(), 'MM-yyyy') THEN sm.ActualNetAmount ELSE 0 END) AS currentMonth 
            FROM SaleOrderMaster sm WITH (NOLOCK) 
            INNER JOIN Customer j WITH (NOLOCK) ON sm.CustomerRefId = j.Id 
            INNER JOIN SymbolMaster s WITH (NOLOCK) ON j.SymbolRefid = s.Id 
            inner join salemaster SI WITH (NOLOCK) on sm.id = SI.saleordermasterno  
            WHERE sm.Active = 1 
            AND SI.SaleDate >= DATEADD(MONTH, -3, DATEFROMPARTS(YEAR(CONVERT(date, :fromDate)), MONTH(CONVERT(date, :fromDate)), 1)) 
            AND SI.SaleDate < DATEADD(MONTH, 1, DATEFROMPARTS(YEAR(CONVERT(date, :fromDate)), MONTH(CONVERT(date, :fromDate)), 1)) 
            AND sm.CompanyRefId = :comId 
            AND (:empId = 0 OR sm.EmployeeRefId = :empId) 
            AND (:tId = 0 OR s.Id = :tId) 
            GROUP BY j.CustomerName, s.SName 
            ORDER BY j.CustomerName
            """, nativeQuery = true)
    List<SaleJobViewProjection> getSaleCurrencyView(
            @Param("comId") Integer comId,
            @Param("empId") Integer empId,
            @Param("tId") Integer tId,
            @Param("fromDate") String fromDate);

    @Query(value = """
            SELECT j.CustomerName as customerName, s.CName AS countryName, 
            SUM(CASE WHEN FORMAT(sm.SaleDate, 'MM-yyyy') = FORMAT(DATEADD(MONTH, -3, GETDATE()), 'MM-yyyy') THEN sm.ActualNetAmount ELSE 0 END) AS month1, 
            SUM(CASE WHEN FORMAT(sm.SaleDate, 'MM-yyyy') = FORMAT(DATEADD(MONTH, -2, GETDATE()), 'MM-yyyy') THEN sm.ActualNetAmount ELSE 0 END) AS month2, 
            SUM(CASE WHEN FORMAT(sm.SaleDate, 'MM-yyyy') = FORMAT(DATEADD(MONTH, -1, GETDATE()), 'MM-yyyy') THEN sm.ActualNetAmount ELSE 0 END) AS month3, 
            SUM(CASE WHEN FORMAT(sm.SaleDate, 'MM-yyyy') = FORMAT(GETDATE(), 'MM-yyyy') THEN sm.ActualNetAmount ELSE 0 END) AS currentMonth 
            FROM SaleOrderMaster sm WITH (NOLOCK) 
            INNER JOIN Customer j WITH (NOLOCK) ON sm.CustomerRefId = j.Id 
            INNER JOIN SymbolMaster s WITH (NOLOCK) ON j.SymbolRefid = s.Id 
            WHERE sm.Active = 1 
            AND sm.SaleDate >= DATEADD(MONTH, -3, DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1)) 
            AND sm.SaleDate < DATEADD(MONTH, 1, DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1)) 
            AND sm.CompanyRefId = :comId 
            AND (:empId = 0 OR sm.EmployeeRefId = :empId) 
            AND (:tId = 0 OR j.Id = :tId) 
            GROUP BY j.CustomerName, s.CName 
            ORDER BY j.CustomerName
            """, nativeQuery = true)
    List<SaleJobViewProjection> getSaleEmployeeView(
            @Param("comId") Integer comId,
            @Param("empId") Integer empId,
            @Param("tId") Integer tId);

    @Query(value = """
            SELECT 
                ISNULL(NULLIF(
                    CASE 
                        WHEN sm.OPort = sm.SPort AND sm.OPort IS NOT NULL AND sm.OPort != '' THEN sm.OPort 
                        WHEN sm.OPort IS NOT NULL AND sm.OPort != '' THEN sm.OPort 
                        ELSE sm.SPort 
                    END, ''), 'Unknown') AS portName, 
                FORMAT(SaleDate, 'MMMM yyyy') AS saleMonth, 
                COUNT(*) AS jobCount, 
                SUM(ISNULL(sm.Amount, 0)) AS totalAmount 
            FROM SaleOrderMaster sm WITH(NOLOCK) 
            WHERE sm.Active = 1 
                AND sm.SaleDate BETWEEN :fromDate AND :toDate 
                AND sm.CompanyRefId = :comId 
                AND (:empId = 0 OR sm.EmployeeRefId = :empId) 
                AND (:portName IS NULL OR :portName = '' OR sm.OPort LIKE '%' + :portName + '%' OR sm.SPort LIKE '%' + :portName + '%')
            GROUP BY FORMAT(SaleDate, 'MMMM yyyy'), 
                ISNULL(NULLIF(
                    CASE 
                        WHEN sm.OPort = sm.SPort AND sm.OPort IS NOT NULL AND sm.OPort != '' THEN sm.OPort 
                        WHEN sm.OPort IS NOT NULL AND sm.OPort != '' THEN sm.OPort 
                        ELSE sm.SPort 
                    END, ''), 'Unknown') 
            ORDER BY portName, saleMonth
            """, nativeQuery = true)
    List<SalePortViewProjection> getSalePortViewRaw(
            @Param("comId") Integer comId,
            @Param("empId") Integer empId,
            @Param("portName") String portName,
            @Param("fromDate") String fromDate,
            @Param("toDate") String toDate);

    @Query(value = """
            SELECT j.CustomerName AS customerName,
                SUM(CASE WHEN FORMAT(sm.SaleDate, 'MM-yyyy') = FORMAT(DATEADD(MONTH, -3, GETDATE()), 'MM-yyyy') THEN 1 ELSE 0 END) AS month1,
                SUM(CASE WHEN FORMAT(sm.SaleDate, 'MM-yyyy') = FORMAT(DATEADD(MONTH, -2, GETDATE()), 'MM-yyyy') THEN 1 ELSE 0 END) AS month2,
                SUM(CASE WHEN FORMAT(sm.SaleDate, 'MM-yyyy') = FORMAT(DATEADD(MONTH, -1, GETDATE()), 'MM-yyyy') THEN 1 ELSE 0 END) AS month3,
                SUM(CASE WHEN FORMAT(sm.SaleDate, 'MM-yyyy') = FORMAT(GETDATE(), 'MM-yyyy') THEN 1 ELSE 0 END) AS currentMonth 
            FROM SaleOrderMaster sm WITH (NOLOCK) 
            INNER JOIN Customer j WITH (NOLOCK) ON sm.CustomerRefId = j.Id 
            WHERE sm.Active = 1   
                AND sm.SaleDate >= DATEADD(MONTH, -3, DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1))   
                AND sm.SaleDate < DATEADD(MONTH, 1, DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1))
                AND sm.CompanyRefId = :comId 
                AND (:empId = 0 OR sm.EmployeeRefId = :empId) 
                AND (:tId = 0 OR j.Id = :tId)
            GROUP BY j.CustomerName 
            ORDER BY j.CustomerName
            """, nativeQuery = true)
    List<SaleJobViewProjection> getSaleCustomerView(
            @Param("comId") Integer comId,
            @Param("empId") Integer empId,
            @Param("tId") Integer tId);




    @Query(value = """
            WITH LoadingUnpivoted AS (
                SELECT A.CNumberDisplay,
                    CAST(CASE WHEN A.ETA IS NOT NULL AND CAST(A.ETA AS DATE) <> '1900-01-01' THEN A.ETA ELSE A.OETA END AS DATE) AS etaDate,
                    A.Loadingvesselname AS vesselName, 'Loading Vessel' AS vesselType,
                    A.LBoardingOfficerRefid AS OfficerId, A.LBoardingAmount AS OfficerAmount
                FROM SaleOrderMaster A WITH (NOLOCK)
                WHERE A.CompanyRefId = :companyRefId AND A.Active = 1 AND A.JStatus <> 12
                    AND ISNULL(A.Loadingvesselname,'') <> '' AND A.ETA IS NOT NULL
                    AND CAST(A.ETA AS DATE) BETWEEN :fromDate AND :toDate
                    AND A.LBoardingOfficerRefid IS NOT NULL
                UNION ALL
                SELECT A.CNumberDisplay,
                    CAST(CASE WHEN A.ETA IS NOT NULL AND CAST(A.ETA AS DATE) <> '1900-01-01' THEN A.ETA ELSE A.OETA END AS DATE) AS etaDate,
                    A.Loadingvesselname AS vesselName, 'Loading Vessel' AS vesselType,
                    A.LBoardingOfficer1Refid AS OfficerId, A.LBoardingAmount1 AS OfficerAmount
                FROM SaleOrderMaster A WITH (NOLOCK)
                WHERE A.CompanyRefId = :companyRefId AND A.Active = 1 AND A.JStatus <> 12
                    AND ISNULL(A.Loadingvesselname,'') <> '' AND A.ETA IS NOT NULL
                    AND CAST(A.ETA AS DATE) BETWEEN :fromDate AND :toDate
                    AND A.LBoardingOfficer1Refid IS NOT NULL
            ),
            OffUnpivoted AS (
                SELECT A.CNumberDisplay,
                    CAST(CASE WHEN A.ETA IS NOT NULL AND CAST(A.ETA AS DATE) <> '1900-01-01' THEN A.ETA ELSE A.OETA END AS DATE) AS etaDate,
                    A.Offvesselname AS vesselName, 'Off Vessel' AS vesselType,
                    A.OBoardingOfficerRefid AS OfficerId, A.OBoardingAmount AS OfficerAmount
                FROM SaleOrderMaster A WITH (NOLOCK)
                WHERE A.CompanyRefId = :companyRefId AND A.Active = 1 AND A.JStatus <> 12
                    AND ISNULL(A.Offvesselname,'') <> '' AND A.OETA IS NOT NULL
                    AND CAST(A.OETA AS DATE) BETWEEN :fromDate AND :toDate
                    AND A.OBoardingOfficerRefid IS NOT NULL
                UNION ALL
                SELECT A.CNumberDisplay,
                    CAST(CASE WHEN A.ETA IS NOT NULL AND CAST(A.ETA AS DATE) <> '1900-01-01' THEN A.ETA ELSE A.OETA END AS DATE) AS etaDate,
                    A.Offvesselname AS vesselName, 'Off Vessel' AS vesselType,
                    A.OBoardingOfficer1Refid AS OfficerId, A.OBoardingAmount1 AS OfficerAmount
                FROM SaleOrderMaster A WITH (NOLOCK)
                WHERE A.CompanyRefId = :companyRefId AND A.Active = 1 AND A.JStatus <> 12
                    AND ISNULL(A.Offvesselname,'') <> '' AND A.OETA IS NOT NULL
                    AND CAST(A.OETA AS DATE) BETWEEN :fromDate AND :toDate
                    AND A.OBoardingOfficer1Refid IS NOT NULL
            ),
            AllUnpivoted AS (
                SELECT * FROM LoadingUnpivoted
                UNION ALL
                SELECT * FROM OffUnpivoted
            ),
            OfficerPayOnce AS (
                SELECT etaDate, vesselName, vesselType, OfficerId,
                       MAX(OfficerAmount) AS OfficerAmount
                FROM AllUnpivoted
                GROUP BY etaDate, vesselName, vesselType, OfficerId
            ),
            OfficerNumbered AS (
                SELECT
                    P.etaDate, P.vesselName, P.vesselType,
                    ISNULL(E.EmployeeName,'') AS OfficerName,
                    P.OfficerAmount,
                    ROW_NUMBER() OVER (
                        PARTITION BY P.etaDate, P.vesselName, P.vesselType
                        ORDER BY P.OfficerAmount DESC, E.EmployeeName
                    ) AS rn
                FROM OfficerPayOnce P
                LEFT JOIN EmployeeMaster E ON E.Id = P.OfficerId
            ),
            JobList AS (
                SELECT DISTINCT etaDate, vesselName, vesselType, CNumberDisplay
                FROM AllUnpivoted
            ),
            JobsAgg AS (
                SELECT etaDate, vesselName, vesselType,
                    COUNT(CNumberDisplay) AS totalJob,
                    STRING_AGG(CNumberDisplay, ', ') AS jobNumbers
                FROM JobList
                GROUP BY etaDate, vesselName, vesselType
            )
            SELECT
                J.etaDate,
                J.vesselName,
                J.vesselType,
                J.totalJob,
                J.jobNumbers,
                MAX(CASE WHEN O.rn = 1 THEN O.OfficerName END) AS officer1Name,
                MAX(CASE WHEN O.rn = 1 THEN O.OfficerAmount END) AS officer1Amount,
                MAX(CASE WHEN O.rn = 2 THEN O.OfficerName END) AS officer2Name,
                MAX(CASE WHEN O.rn = 2 THEN O.OfficerAmount END) AS officer2Amount,
                MAX(CASE WHEN O.rn = 3 THEN O.OfficerName END) AS officer3Name,
                MAX(CASE WHEN O.rn = 3 THEN O.OfficerAmount END) AS officer3Amount,
                MAX(CASE WHEN O.rn = 4 THEN O.OfficerName END) AS officer4Name,
                MAX(CASE WHEN O.rn = 4 THEN O.OfficerAmount END) AS officer4Amount,
                MAX(CASE WHEN O.rn = 5 THEN O.OfficerName END) AS officer5Name,
                MAX(CASE WHEN O.rn = 5 THEN O.OfficerAmount END) AS officer5Amount,
                SUM(O.OfficerAmount) AS totalAmount
            FROM JobsAgg J
            LEFT JOIN OfficerNumbered O
                ON O.etaDate = J.etaDate AND O.vesselName = J.vesselName AND O.vesselType = J.vesselType
            GROUP BY J.etaDate, J.vesselName, J.vesselType, J.totalJob, J.jobNumbers
            ORDER BY J.etaDate, J.vesselName
            """, nativeQuery = true)
    List<my.maleva.api.module.saleorder.dto.VesselScheduleDto> getVesselSchedules(
            @Param("companyRefId") Integer companyRefId,
            @Param("fromDate") String fromDate,
            @Param("toDate") String toDate);
}
