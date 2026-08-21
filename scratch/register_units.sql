/* ===========================================================================
   REGISTER SERIAL-TRACKED UNITS BY SQL
   ---------------------------------------------------------------------------
   Only for bulk-loading units you already own. For one or two units, use the
   REGISTER UNIT button on the item page instead - it is safer and faster.

   Registering a unit is THREE writes, not one:
       InventoryAsset        the unit itself
       ProductMasterCStock   balance + 1
       InventoryTransaction  history row so the balance is explained

   Inserting into InventoryAsset alone leaves the screen showing
   "0 of 1 available" - a unit that exists but is not counted.
   =========================================================================== */

USE MalevanewDemo;
GO

/* ---------------------------------------------------------------------------
   PART A  -  Find the ProductRefId to register units against.
   Only Recond (ASSET) and Tool items can have units.
   --------------------------------------------------------------------------- */
DECLARE @CompanyRefId INT = 1;          -- <<< your company

SELECT  i.ProductRefId, p.Prod_Code, p.PName, i.ItemType,
        (SELECT COUNT(*) FROM InventoryAsset a
         WHERE a.CompanyRefId = i.CompanyRefId AND a.ProductRefId = i.ProductRefId) AS UnitsNow
FROM    InventoryItem i
JOIN    ProductMaster p ON p.Id = i.ProductRefId
WHERE   i.CompanyRefId = @CompanyRefId
  AND   i.ItemType IN ('ASSET','TOOL')
  AND   i.Active = 1
ORDER BY p.Prod_Code;
GO


/* ---------------------------------------------------------------------------
   PART B  -  Register the units.

   List every serial in @Units. Re-running is safe: serials that already exist
   are skipped, so nothing is double counted.
   --------------------------------------------------------------------------- */
DECLARE @CompanyRefId INT = 1;          -- <<< same company
DECLARE @ProductRefId INT = 9;          -- <<< from PART A
DECLARE @By           VARCHAR(50) = 'SQL-LOAD';

DECLARE @Units TABLE (SerialNo VARCHAR(100) PRIMARY KEY);
INSERT INTO @Units (SerialNo) VALUES
    ('TURBO-021'),
    ('TURBO-022'),
    ('TURBO-023');       -- <<< one line per physical unit

BEGIN TRY
    BEGIN TRAN;

    /* The product must be set up as a serial-tracked item, or the units would
       never show anywhere. */
    IF NOT EXISTS (SELECT 1 FROM InventoryItem
                   WHERE CompanyRefId = @CompanyRefId
                     AND ProductRefId = @ProductRefId
                     AND ItemType IN ('ASSET','TOOL'))
        THROW 50001, 'That product is not set up as Recond or Tool. Check PART A.', 1;

    /* A balance row must exist before it can be incremented. */
    IF NOT EXISTS (SELECT 1 FROM ProductMasterCStock
                   WHERE CompanyRefId = @CompanyRefId AND ProductRefId = @ProductRefId)
        INSERT INTO ProductMasterCStock
            (CompanyRefId, ProductRefId, CStock, Created_Date, Modified_Date, Modified_By)
        VALUES (@CompanyRefId, @ProductRefId, 0, GETDATE(), GETDATE(), @By);

    /* Skip serials already registered, so the script can be re-run. */
    DECLARE @New TABLE (SerialNo VARCHAR(100));
    INSERT INTO @New (SerialNo)
    SELECT u.SerialNo
    FROM   @Units u
    WHERE  NOT EXISTS (SELECT 1 FROM InventoryAsset a
                       WHERE a.CompanyRefId = @CompanyRefId
                         AND a.ProductRefId = @ProductRefId
                         AND a.SerialNo     = u.SerialNo);

    DECLARE @Count INT = (SELECT COUNT(*) FROM @New);

    IF @Count = 0
    BEGIN
        PRINT 'Every serial is already registered - nothing to do.';
        COMMIT;
        RETURN;
    END

    -- 1) the units
    INSERT INTO InventoryAsset
        (CompanyRefId, ProductRefId, SerialNo, Status, CurrentTruckRefId, Modified_By)
    SELECT @CompanyRefId, @ProductRefId, SerialNo, 'AVAILABLE', NULL, @By
    FROM   @New;

    -- 2) the balance
    UPDATE ProductMasterCStock
    SET    CStock        = CStock + @Count,
           Modified_Date = GETDATE(),
           Modified_By   = @By
    WHERE  CompanyRefId = @CompanyRefId AND ProductRefId = @ProductRefId;

    -- 3) the history, one row per unit, each carrying the balance after it
    DECLARE @Balance DECIMAL(18,2) =
        (SELECT CStock FROM ProductMasterCStock
         WHERE CompanyRefId = @CompanyRefId AND ProductRefId = @ProductRefId);

    INSERT INTO InventoryTransaction
        (CompanyRefId, ProductRefId, TransactionType, Quantity, BalanceAfter,
         ReferenceType, AssetSerialNo, Remarks, Created_By, Created_Date)
    SELECT  @CompanyRefId, @ProductRefId, 'IN', 1,
            @Balance - @Count + ROW_NUMBER() OVER (ORDER BY SerialNo),
            'ASSET_REGISTER', SerialNo, 'Registered by SQL load', @By, GETDATE()
    FROM    @New;

    COMMIT;
    PRINT CONCAT('Units registered: ', @Count, '. New balance: ', @Balance);
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK;
    PRINT CONCAT('FAILED - nothing was saved. ', ERROR_MESSAGE());
END CATCH
GO


/* ---------------------------------------------------------------------------
   PART C  -  Check the result.
   Available units and the stock balance must be the same number.
   --------------------------------------------------------------------------- */
DECLARE @CompanyRefId INT = 1;          -- <<< same company

SELECT  p.Prod_Code, p.PName,
        SUM(CASE WHEN a.Status = 'AVAILABLE'    THEN 1 ELSE 0 END) AS Available,
        SUM(CASE WHEN a.Status = 'INSTALLED'    THEN 1 ELSE 0 END) AS Installed,
        SUM(CASE WHEN a.Status = 'UNDER_REPAIR' THEN 1 ELSE 0 END) AS UnderRepair,
        COUNT(*)          AS TotalUnits,
        MAX(s.CStock)     AS BalanceSays
FROM    InventoryAsset a
JOIN    ProductMaster p ON p.Id = a.ProductRefId
LEFT JOIN ProductMasterCStock s ON s.CompanyRefId = a.CompanyRefId
                               AND s.ProductRefId = a.ProductRefId
WHERE   a.CompanyRefId = @CompanyRefId
GROUP BY p.Prod_Code, p.PName
ORDER BY p.Prod_Code;
GO

/* Available and BalanceSays should match. If they do not, something was
   inserted into InventoryAsset without updating the balance. */
