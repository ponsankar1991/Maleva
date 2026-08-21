/* ===========================================================================
   MALEVA WORKSHOP INVENTORY - DATABASE SETUP
   ---------------------------------------------------------------------------
   Run this on:  MalevanewDemo   (103.215.139.8)   <-- NOT DemoMaleva
   Run it as:    one whole script, top to bottom.

   Safe to run more than once: every object is created only if it is missing,
   so re-running will not duplicate or drop anything.

   Order:  STEP 1 check  ->  STEP 2 fix if needed  ->  STEP 3 create
           ->  STEP 4 protect  ->  STEP 5 verify
   =========================================================================== */

USE MalevanewDemo;
GO

PRINT '--- Connected to: ' + DB_NAME() + ' ---';
GO


/* ===========================================================================
   STEP 1  -  CHECK FIRST (reads only, changes nothing)

   STEP 4 adds two rules to the existing ProductMasterCStock table:
     - only one stock row per company + product
     - stock can never be negative
   If your current data breaks either rule, STEP 4 will fail. Run this first
   and look at the two result grids.
   =========================================================================== */

PRINT '';
PRINT '=== STEP 1: checking existing ProductMasterCStock data ===';
GO

-- 1a. Duplicate stock rows. You want ZERO rows here.
SELECT  CompanyRefId, ProductRefId, COUNT(*) AS DuplicateRows
FROM    ProductMasterCStock
GROUP BY CompanyRefId, ProductRefId
HAVING  COUNT(*) > 1;
GO

-- 1b. Negative stock. You want ZERO rows here.
SELECT  Id, CompanyRefId, ProductRefId, CStock
FROM    ProductMasterCStock
WHERE   CStock < 0;
GO


/* ===========================================================================
   STEP 2  -  ONLY IF STEP 1 FOUND ROWS

   Both fixes are commented out on purpose. Read them, then uncomment and run
   only the one you need. Take a backup before running either.
   =========================================================================== */

-- Fix for 1a: keep the newest stock row per company+product, delete the rest.
--   Check the numbers first - deleting the wrong row loses a stock balance.
/*
WITH Ranked AS (
    SELECT Id,
           ROW_NUMBER() OVER (PARTITION BY CompanyRefId, ProductRefId
                              ORDER BY Modified_Date DESC, Id DESC) AS rn
    FROM   ProductMasterCStock
)
DELETE FROM ProductMasterCStock
WHERE Id IN (SELECT Id FROM Ranked WHERE rn > 1);
*/

-- Fix for 1b: pull negative balances up to zero.
--   A negative balance means stock went out that was never booked in, so
--   note these down before you clear them - they are a counting problem.
/*
UPDATE ProductMasterCStock SET CStock = 0 WHERE CStock < 0;
*/


/* ===========================================================================
   STEP 3  -  CREATE THE THREE NEW TABLES

   Nothing here touches ProductMaster or any table you already use.

     InventoryItem         workshop settings for a product
                           (type, category, reorder level, location, cost)
     InventoryTransaction  every stock IN and OUT - the history
     InventoryAsset        one row per physical serial-tracked unit
                           (each turbo, each tool)
   =========================================================================== */

PRINT '';
PRINT '=== STEP 3: creating tables ===';
GO

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'InventoryItem')
BEGIN
    CREATE TABLE InventoryItem (
        Id                   INT IDENTITY(1,1) PRIMARY KEY,
        CompanyRefId         INT NOT NULL,
        ProductRefId         INT NOT NULL,
        ItemType             VARCHAR(20) NOT NULL
                             CHECK (ItemType IN ('CONSUMABLE','PART','ASSET','TOOL')),
        Category             VARCHAR(100) NULL,
        Brand                VARCHAR(100) NULL,
        FitsModel            VARCHAR(200) NULL,
        BaseUom              VARCHAR(20)  NOT NULL,
        MinQty               DECIMAL(18,2) NULL,   -- reorder level (NULL for serial items)
        ReorderQty           DECIMAL(18,2) NULL,
        UnitCost             DECIMAL(18,2) NULL,
        StorageLocation      VARCHAR(100) NULL,
        BinCode              VARCHAR(50)  NULL,
        DefaultSupplierRefId INT NULL,
        Remarks              VARCHAR(200) NULL,
        Active               INT NOT NULL DEFAULT 1,
        Created_Date         DATETIME NOT NULL DEFAULT GETDATE(),
        Modified_Date        DATETIME NOT NULL DEFAULT GETDATE(),
        Modified_By          VARCHAR(50) NOT NULL,
        CONSTRAINT FK_InventoryItem_ProductMaster
            FOREIGN KEY (ProductRefId) REFERENCES ProductMaster(Id),
        CONSTRAINT UQ_InventoryItem_Company_Product
            UNIQUE (CompanyRefId, ProductRefId)
    );
    PRINT 'InventoryItem created.';
END
ELSE PRINT 'InventoryItem already exists - skipped.';
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_InventoryItem_Company_Type')
    CREATE INDEX IX_InventoryItem_Company_Type
        ON InventoryItem (CompanyRefId, ItemType, Active);
GO


IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'InventoryTransaction')
BEGIN
    CREATE TABLE InventoryTransaction (
        Id              INT IDENTITY(1,1) PRIMARY KEY,
        CompanyRefId    INT NOT NULL,
        ProductRefId    INT NOT NULL,
        TransactionType VARCHAR(3) NOT NULL CHECK (TransactionType IN ('IN','OUT')),
        Quantity        DECIMAL(18,2) NOT NULL CHECK (Quantity > 0),
        BalanceAfter    DECIMAL(18,2) NOT NULL,
        ReferenceType   VARCHAR(50)  NULL,  -- OPENING, PURCHASE, JOB_ORDER, ADJUSTMENT,
                                            -- MANUAL_ISSUE, ASSET_REGISTER, REPAIR_COMPLETE
        ReferenceId     INT NULL,           -- PurchaseMaster.Id / JobOrderMaster.Id
        TruckRefId      INT NULL,           -- which truck it went to
        AssetSerialNo   VARCHAR(100) NULL,  -- which physical unit (serial items only)
        Remarks         VARCHAR(200) NULL,
        Created_By      VARCHAR(50) NOT NULL,
        Created_Date    DATETIME NOT NULL DEFAULT GETDATE(),
        CONSTRAINT FK_InventoryTransaction_ProductMaster
            FOREIGN KEY (ProductRefId) REFERENCES ProductMaster(Id)
    );
    PRINT 'InventoryTransaction created.';
END
ELSE PRINT 'InventoryTransaction already exists - skipped.';
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_InventoryTransaction_Company_Product_Date')
    CREATE INDEX IX_InventoryTransaction_Company_Product_Date
        ON InventoryTransaction (CompanyRefId, ProductRefId, Created_Date);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_InventoryTransaction_AssetSerial')
    CREATE INDEX IX_InventoryTransaction_AssetSerial
        ON InventoryTransaction (CompanyRefId, AssetSerialNo);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_InventoryTransaction_Truck')
    CREATE INDEX IX_InventoryTransaction_Truck
        ON InventoryTransaction (CompanyRefId, ProductRefId, TruckRefId);
GO


IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'InventoryAsset')
BEGIN
    CREATE TABLE InventoryAsset (
        Id                INT IDENTITY(1,1) PRIMARY KEY,
        CompanyRefId      INT NOT NULL,
        ProductRefId      INT NOT NULL,
        SerialNo          VARCHAR(100) NOT NULL,
        Status            VARCHAR(20) NOT NULL
                          CHECK (Status IN ('AVAILABLE','INSTALLED','UNDER_REPAIR')),
        CurrentTruckRefId INT NULL,          -- filled only while Status = INSTALLED
        Created_Date      DATETIME NOT NULL DEFAULT GETDATE(),
        Modified_Date     DATETIME NOT NULL DEFAULT GETDATE(),
        Modified_By       VARCHAR(50) NOT NULL,
        CONSTRAINT FK_InventoryAsset_ProductMaster
            FOREIGN KEY (ProductRefId) REFERENCES ProductMaster(Id),
        CONSTRAINT UQ_InventoryAsset_Company_Product_Serial
            UNIQUE (CompanyRefId, ProductRefId, SerialNo)
    );
    PRINT 'InventoryAsset created.';
END
ELSE PRINT 'InventoryAsset already exists - skipped.';
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_InventoryAsset_Company_Status')
    CREATE INDEX IX_InventoryAsset_Company_Status
        ON InventoryAsset (CompanyRefId, Status);
GO


/* ===========================================================================
   STEP 4  -  PROTECT THE EXISTING STOCK TABLE

   Right now nothing stops two stock rows for the same product, or a stock
   balance going below zero. These two rules stop both, in the database itself,
   so a bug in any application can never corrupt the balance.

   If either line fails, go back to STEP 1 and STEP 2.
   =========================================================================== */

PRINT '';
PRINT '=== STEP 4: protecting ProductMasterCStock ===';
GO

IF NOT EXISTS (SELECT 1 FROM sys.key_constraints
               WHERE name = 'UQ_ProductMasterCStock_Company_Product')
BEGIN
    ALTER TABLE ProductMasterCStock
        ADD CONSTRAINT UQ_ProductMasterCStock_Company_Product
        UNIQUE (CompanyRefId, ProductRefId);
    PRINT 'Unique rule added.';
END
ELSE PRINT 'Unique rule already there - skipped.';
GO

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints
               WHERE name = 'CK_ProductMasterCStock_NonNegative')
BEGIN
    ALTER TABLE ProductMasterCStock
        ADD CONSTRAINT CK_ProductMasterCStock_NonNegative CHECK (CStock >= 0);
    PRINT 'Non-negative rule added.';
END
ELSE PRINT 'Non-negative rule already there - skipped.';
GO




/* ===========================================================================
   STEP 5  -  VERIFY

   Expect: 3 rows in the first grid (the three new tables),
           2 rows in the second grid (the two new rules).
   =========================================================================== */

PRINT '';
PRINT '=== STEP 5: verifying ===';
GO

SELECT name AS TableCreated, create_date
FROM   sys.tables
WHERE  name IN ('InventoryItem','InventoryTransaction','InventoryAsset')
ORDER BY name;
GO

SELECT name AS RuleAdded FROM sys.key_constraints
WHERE  name = 'UQ_ProductMasterCStock_Company_Product'
UNION ALL
SELECT name FROM sys.check_constraints
WHERE  name = 'CK_ProductMasterCStock_NonNegative';
GO

PRINT '';
PRINT '=== DONE. Next: run inventory_backfill.sql if you want your existing ===';
PRINT '=== products to show up in the new inventory screens.                ===';
GO
