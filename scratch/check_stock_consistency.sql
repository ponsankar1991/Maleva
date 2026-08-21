/* ===========================================================================
   STOCK CONSISTENCY CHECK
   ---------------------------------------------------------------------------
   The same fact is held in two places, on purpose:

     ProductMasterCStock.CStock   the number the rest of Maleva reads
     InventoryAsset               one row per physical unit (Recond / Tool only)

   The application always writes both together in one transaction, so they
   cannot drift. Hand-written SQL can drift them - inserting an InventoryAsset
   row without adding 1 to CStock leaves a unit that exists but is not counted.

   PART A finds drift. PART B fixes it. PART C is for quantity items.
   Run PART A any time; it only reads.
   =========================================================================== */

USE MalevanewDemo;
GO

/* ---------------------------------------------------------------------------
   PART A  -  Find serial-tracked items whose balance disagrees with the units.

   You want ZERO rows. Any row here means CStock and the unit count have
   drifted apart for that item.
   --------------------------------------------------------------------------- */
DECLARE @CompanyRefId INT = 1;          -- <<< your company

SELECT  p.Prod_Code,
        p.PName,
        i.ItemType,
        COUNT(CASE WHEN a.Status = 'AVAILABLE'    THEN 1 END) AS AvailableUnits,
        COUNT(CASE WHEN a.Status = 'INSTALLED'    THEN 1 END) AS Installed,
        COUNT(CASE WHEN a.Status = 'UNDER_REPAIR' THEN 1 END) AS UnderRepair,
        MAX(s.CStock)                                         AS BalanceSays,
        COUNT(CASE WHEN a.Status = 'AVAILABLE' THEN 1 END)
            - MAX(s.CStock)                                   AS Difference
FROM    InventoryItem i
JOIN    ProductMaster p ON p.Id = i.ProductRefId
LEFT JOIN InventoryAsset a ON a.CompanyRefId = i.CompanyRefId
                          AND a.ProductRefId = i.ProductRefId
LEFT JOIN ProductMasterCStock s ON s.CompanyRefId = i.CompanyRefId
                               AND s.ProductRefId = i.ProductRefId
WHERE   i.CompanyRefId = @CompanyRefId
  AND   i.ItemType IN ('ASSET','TOOL')
  AND   i.Active = 1
GROUP BY p.Prod_Code, p.PName, i.ItemType
HAVING  COUNT(CASE WHEN a.Status = 'AVAILABLE' THEN 1 END) <> ISNULL(MAX(s.CStock), 0)
ORDER BY p.Prod_Code;
GO


/* ---------------------------------------------------------------------------
   PART B  -  Fix the drift.

   The unit rows are the truth for serial-tracked items - each row is a real
   turbo you can walk up to and touch. CStock is the copy, so CStock is what
   gets corrected.

   Commented out on purpose. Read PART A first, then uncomment and run.
   --------------------------------------------------------------------------- */
/*
DECLARE @CompanyRefId INT = 1;
DECLARE @By VARCHAR(50) = 'RECONCILE';

UPDATE  s
SET     s.CStock        = x.AvailableUnits,
        s.Modified_Date = GETDATE(),
        s.Modified_By   = @By
FROM    ProductMasterCStock s
JOIN    (
    SELECT  i.CompanyRefId,
            i.ProductRefId,
            COUNT(CASE WHEN a.Status = 'AVAILABLE' THEN 1 END) AS AvailableUnits
    FROM    InventoryItem i
    LEFT JOIN InventoryAsset a ON a.CompanyRefId = i.CompanyRefId
                              AND a.ProductRefId = i.ProductRefId
    WHERE   i.CompanyRefId = @CompanyRefId
      AND   i.ItemType IN ('ASSET','TOOL')
      AND   i.Active = 1
    GROUP BY i.CompanyRefId, i.ProductRefId
) x ON x.CompanyRefId = s.CompanyRefId AND x.ProductRefId = s.ProductRefId
WHERE   s.CStock <> x.AvailableUnits;

PRINT CONCAT('Balances corrected: ', @@ROWCOUNT);
*/
GO


/* ---------------------------------------------------------------------------
   PART C  -  Quantity items: does the balance match its own history?

   For oil and parts there are no unit rows, so the check is different - the
   balance must equal every IN minus every OUT. You want ZERO rows.
   --------------------------------------------------------------------------- */
DECLARE @CompanyRefId INT = 1;          -- <<< same company

SELECT  p.Prod_Code,
        p.PName,
        i.BaseUom,
        s.CStock                       AS BalanceSays,
        ISNULL(t.MovementTotal, 0)     AS HistorySays,
        s.CStock - ISNULL(t.MovementTotal, 0) AS Difference
FROM    InventoryItem i
JOIN    ProductMaster p ON p.Id = i.ProductRefId
JOIN    ProductMasterCStock s ON s.CompanyRefId = i.CompanyRefId
                             AND s.ProductRefId = i.ProductRefId
OUTER APPLY (
    SELECT SUM(CASE WHEN t2.TransactionType = 'IN' THEN t2.Quantity
                    ELSE -t2.Quantity END) AS MovementTotal
    FROM   InventoryTransaction t2
    WHERE  t2.CompanyRefId = i.CompanyRefId
      AND  t2.ProductRefId = i.ProductRefId
) t
WHERE   i.CompanyRefId = @CompanyRefId
  AND   i.ItemType IN ('CONSUMABLE','PART')
  AND   i.Active = 1
  AND   s.CStock <> ISNULL(t.MovementTotal, 0)
ORDER BY p.Prod_Code;
GO

/* If PART C returns rows, stock was changed without recording a movement -
   usually a direct UPDATE on ProductMasterCStock. Correct it with a Stock
   Adjustment on the screen so the change is explained, rather than editing
   the balance again by hand. */
