package my.maleva.api.module.saleorder.repository;

/**
 * SQL for the SelectSaleOrder (/api/sale-orders/search) screen.
 *
 * Every column carries an explicit alias and is read back by that alias, not by
 * ordinal. The previous version returned {@code Object[]} and the mapper indexed
 * into it (0..34) from a comment that had already drifted out of date — it said
 * "33 columns" while the query selected 35.
 */
final class SaleOrderSearchQueries {

    /**
     * How many ids go into one IN (...) list.
     *
     * SQL Server refuses a statement with more than 2,100 parameters, so a search
     * whose date range matched more orders than that failed outright rather than
     * returning slowly.
     */
    static final int ID_BATCH_SIZE = 1000;

    static final String MASTER_ROWS = """
            SELECT
                A.Id                                                              AS Id,
                A.sportsaleorderid                                                AS Sportsaleorderid,
                ISNULL(SM.InvoiceId, A.InvoiceNo)                                 AS InvoiceId,
                A.Remarks                                                         AS Remarks,
                A.Destination                                                     AS Destination,
                A.FlighTime                                                       AS FlighTime,
                A.Origin                                                          AS Origin,
                A.JobMasterRefId                                                  AS JobMasterRefId,
                ISNULL(E.EmployeeName, '')                                        AS EmployeeName,
                A.Offvesselname                                                   AS Offvesselname,
                S.Sname                                                           AS Sname,
                A.Loadingvesselname                                               AS Loadingvesselname,
                A.SPort                                                           AS SPort,
                A.OPort                                                           AS OPort,
                FORMAT(ISNULL(A.SaleDate, '1900-01-01'), 'dd/MM/yyyy')            AS BillDate,
                ISNULL(FORMAT(A.ETA, 'dd/MM/yyyy'), FORMAT(A.OETA, 'dd/MM/yyyy')) AS DETA,
                ISNULL(A.ETA, A.OETA)                                             AS DETASort,
                A.ETA                                                             AS ETA,
                ISNULL(FORMAT(A.ETA,  'dd/MM/yyyy HH:mm:ss'), '')                 AS SETA,
                ISNULL(FORMAT(A.ETB,  'dd/MM/yyyy HH:mm:ss'), '')                 AS SETB,
                ISNULL(FORMAT(A.OETA, 'dd/MM/yyyy HH:mm:ss'), '')                 AS SOETA,
                ISNULL(FORMAT(A.OETB, 'dd/MM/yyyy HH:mm:ss'), '')                 AS SOETB,
                ISNULL(CONVERT(VARCHAR(26), A.PickupDate, 20), '')                AS SPickupDate,
                A.CNumberDisplay                                                  AS BillNoDisplay,
                FORMAT(ISNULL(A.Created_Date, '1900-01-01'), 'dd/MM/yyyy HH:mm:ss') AS BillTime,
                B.CustomerName                                                    AS CustomerName,
                ISNULL(JT.Name, '')                                               AS JobType,
                A.Amount                                                          AS NetAmt,
                A.SaleType                                                        AS SaleType,
                A.CNumber                                                         AS BillNo,
                ISNULL(J.Name, '')                                                AS JobStatus,
                ISNULL(SM.CNumberDisplay, '')                                     AS InvoiceNo,
                ISNULL(SM.QNECode, '')                                            AS QNECode,
                ISNULL(SM.QNEId, '')                                              AS QNEId,
                ISNULL(A.Quantity, '')                                            AS Quantity,
                ISNULL(A.TotalWeight, '')                                         AS TotalWeight,
                ISNULL(A.Created_Date, A.SaleDate)                                AS BillTimeSort,
                A.SaleDate                                                        AS SaleDateSort
            FROM SaleOrderMaster A WITH(NOLOCK)
            INNER JOIN Customer B WITH(NOLOCK) ON A.CustomerRefId = B.Id
            LEFT  JOIN EmployeeMaster E WITH(NOLOCK) ON E.Id = A.EmployeeRefId
            LEFT  JOIN JobStatusMaster J WITH(NOLOCK) ON J.Id = A.JStatus
            LEFT  JOIN JobTypeMaster JT WITH(NOLOCK) ON JT.Id = A.JobMasterRefId
            OUTER APPLY (
                SELECT TOP 1
                    SM1.Id AS InvoiceId,
                    SM1.CNumberDisplay,
                    SM1.QNECode,
                    SM1.QNEId
                FROM SaleMaster SM1 WITH(NOLOCK)
                WHERE SM1.CompanyRefId = A.CompanyRefId
                  AND SM1.Active = 1
                  AND (
                      (A.InvoiceNo IS NOT NULL AND A.InvoiceNo > 0 AND SM1.Id = A.InvoiceNo)
                      OR SM1.SaleOrderMasterNo = A.Id
                  )
                ORDER BY
                    CASE WHEN A.InvoiceNo IS NOT NULL AND A.InvoiceNo > 0 AND SM1.Id = A.InvoiceNo THEN 0 ELSE 1 END,
                    SM1.Id DESC
            ) SM
            INNER JOIN SymbolMaster S WITH(NOLOCK) ON B.SymbolRefid = S.Id
            WHERE A.CompanyRefId = :companyId
              AND A.Active = 1
              AND A.Id IN (:orderIds)
            """;

    static final String DETAIL_ROWS = """
            SELECT
                B.Id                             AS DetailId,
                B.DiscAmount                     AS DiscAmount,
                B.DiscPer                        AS DiscPer,
                B.ItemQty                        AS ItemQty,
                B.MRP                            AS MRP,
                I.PName                          AS PName,
                B.SDRemarks                      AS SDRemarks,
                B.SalesRate                      AS SalesRate,
                B.SaleOrderMasterRefId           AS SaleOrderMasterRefId,
                A.TaxAmount                      AS TaxAmount,
                B.TaxPercent                     AS TaxPercent,
                I.Prod_Code                      AS Prod_Code,
                B.Amount                         AS Amount,
                ISNULL(B.CurrencyValue, 0)       AS CurrencyValue,
                ISNULL(B.ActualAmount, 0)        AS ActualAmount
            FROM SaleOrderDetails B WITH(NOLOCK)
            INNER JOIN SaleOrderMaster A WITH(NOLOCK) ON B.SaleOrderMasterRefId = A.Id
            INNER JOIN ItemMaster I WITH(NOLOCK) ON B.ItemMasterRefId = I.Id
            WHERE A.CompanyRefId = :companyId
              AND A.Active = 1
              AND B.SaleOrderMasterRefId IN (:orderIds)
            ORDER BY B.Id
            """;

    private SaleOrderSearchQueries() {
    }
}
