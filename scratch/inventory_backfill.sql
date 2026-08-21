/* ===========================================================================
   MALEVA WORKSHOP INVENTORY - BACKFILL EXISTING PRODUCTS
   ---------------------------------------------------------------------------
   Run this AFTER inventory_in_out_schema.sql.
   Run it on:  MalevanewDemo   (103.215.139.8)

   WHY YOU NEED THIS
   The new inventory screens read from InventoryItem. Your existing products
   live in ProductMaster and have no InventoryItem row yet, so without this
   script the new screens will look empty even though you have products.

   This script gives every existing product a workshop setting row.

   HOW TO RUN
   Do one company at a time. Set @CompanyRefId below, then run each part in
   order and READ the preview before running the INSERT under it.
   =========================================================================== */

USE MalevanewDemo;
GO

DECLARE @CompanyRefId INT = 1;          -- <<< CHANGE THIS to your company id
DECLARE @RunBy        VARCHAR(50) = 'BACKFILL';

/* ---------------------------------------------------------------------------
   PART A  -  PREVIEW: which products will be brought in?
   Read this list. If the count looks wrong, stop and check @CompanyRefId.
   --------------------------------------------------------------------------- */
SELECT  p.Id AS ProductRefId, p.Prod_Code, p.PName, p.PurchaseRate
FROM    ProductMaster p
WHERE   p.CompanyRefId = @CompanyRefId
  AND   ISNULL(p.Activestatus, 1) = 1
  AND   NOT EXISTS (SELECT 1 FROM InventoryItem i
                    WHERE i.CompanyRefId = p.CompanyRefId
                      AND i.ProductRefId = p.Id)
ORDER BY p.Prod_Code;
GO


/* ---------------------------------------------------------------------------
   PART B  -  INSERT the workshop setting rows.

   Everything comes in as PART / 'Pcs' to start with. That is deliberate -
   the script does not guess what is oil and what is a filter. You fix the
   few that are different in PART D, which is quick and safe.
   --------------------------------------------------------------------------- */
DECLARE @CompanyRefId INT = 1;          -- <<< SAME id as above
DECLARE @RunBy        VARCHAR(50) = 'BACKFILL';

INSERT INTO InventoryItem
    (CompanyRefId, ProductRefId, ItemType, BaseUom, MinQty, ReorderQty,
     UnitCost, Active, Modified_By)
SELECT  p.CompanyRefId,
        p.Id,
        'PART',
        'Pcs',
        0,
        0,
        ISNULL(p.PurchaseRate, 0),
        1,
        @RunBy
FROM    ProductMaster p
WHERE   p.CompanyRefId = @CompanyRefId
  AND   ISNULL(p.Activestatus, 1) = 1
  AND   NOT EXISTS (SELECT 1 FROM InventoryItem i
                    WHERE i.CompanyRefId = p.CompanyRefId
                      AND i.ProductRefId = p.Id);

PRINT CONCAT('Workshop setting rows created: ', @@ROWCOUNT);
GO


/* ---------------------------------------------------------------------------
   PART C  -  Make sure every product has a stock balance row.
   Products with no ProductMasterCStock row get one at zero.
   --------------------------------------------------------------------------- */
DECLARE @CompanyRefId INT = 1;          -- <<< SAME id
DECLARE @RunBy        VARCHAR(50) = 'BACKFILL';

INSERT INTO ProductMasterCStock
    (CompanyRefId, ProductRefId, CStock, Created_Date, Modified_Date, Modified_By)
SELECT  i.CompanyRefId, i.ProductRefId, 0, GETDATE(), GETDATE(), @RunBy
FROM    InventoryItem i
WHERE   i.CompanyRefId = @CompanyRefId
  AND   NOT EXISTS (SELECT 1 FROM ProductMasterCStock s
                    WHERE s.CompanyRefId = i.CompanyRefId
                      AND s.ProductRefId = i.ProductRefId);

PRINT CONCAT('Missing stock rows created: ', @@ROWCOUNT);
GO


/* ---------------------------------------------------------------------------
   PART D  -  Fix the item types and units.

   Only PART and CONSUMABLE matter for now. Serial-tracked items (ASSET, TOOL)
   are better added through the screen, because each one needs its own serial
   number - see the note at the bottom.

   The examples below match on the product name. CHECK each SELECT before you
   run the UPDATE under it, so you do not reclassify the wrong rows.
   --------------------------------------------------------------------------- */
DECLARE @CompanyRefId INT = 1;          -- <<< SAME id

-- D1. PREVIEW what would become oil / fluid (measured in litres)
SELECT  p.Prod_Code, p.PName
FROM    InventoryItem i
JOIN    ProductMaster p ON p.Id = i.ProductRefId
WHERE   i.CompanyRefId = @CompanyRefId
  AND  (p.PName LIKE '%OIL%'     OR p.PName LIKE '%COOLANT%'
     OR p.PName LIKE '%FLUID%'   OR p.PName LIKE '%GREASE%'
     OR p.PName LIKE '%MINYAK%');
GO

-- D2. If that list looks right, run this to mark them as oil / fluid in litres.
/*
DECLARE @CompanyRefId INT = 1;
UPDATE  i
SET     i.ItemType = 'CONSUMABLE', i.BaseUom = 'L', i.Modified_Date = GETDATE()
FROM    InventoryItem i
JOIN    ProductMaster p ON p.Id = i.ProductRefId
WHERE   i.CompanyRefId = @CompanyRefId
  AND  (p.PName LIKE '%OIL%'     OR p.PName LIKE '%COOLANT%'
     OR p.PName LIKE '%FLUID%'   OR p.PName LIKE '%GREASE%'
     OR p.PName LIKE '%MINYAK%');
PRINT CONCAT('Marked as oil / fluid: ', @@ROWCOUNT);
*/
GO

-- D3. Set reorder levels. Everything starts at 0, which means nothing is ever
--     flagged as low stock. Set a real level per item, for example:
/*
DECLARE @CompanyRefId INT = 1;
UPDATE  i SET i.MinQty = 6, i.Modified_Date = GETDATE()
FROM    InventoryItem i
JOIN    ProductMaster p ON p.Id = i.ProductRefId
WHERE   i.CompanyRefId = @CompanyRefId AND p.Prod_Code = 'FLT-OIL-01';
*/
GO


/* ---------------------------------------------------------------------------
   PART E  -  Explain the balances you already have.

   If a product already shows 180 litres but there are no movement rows, the
   history cannot explain where that 180 came from. This writes one OPENING
   row per product that has stock, so every balance has a reason behind it.

   Run this ONCE only. Running it twice would double-count the opening rows.
   PART F below tells you whether it has already been run.
   --------------------------------------------------------------------------- */
DECLARE @CompanyRefId INT = 1;          -- <<< SAME id
DECLARE @RunBy        VARCHAR(50) = 'BACKFILL';

INSERT INTO InventoryTransaction
    (CompanyRefId, ProductRefId, TransactionType, Quantity, BalanceAfter,
     ReferenceType, Remarks, Created_By, Created_Date)
SELECT  s.CompanyRefId, s.ProductRefId, 'IN', s.CStock, s.CStock,
        'OPENING', 'Opening balance carried in from existing stock',
        @RunBy, GETDATE()
FROM    ProductMasterCStock s
JOIN    InventoryItem i ON i.CompanyRefId = s.CompanyRefId
                       AND i.ProductRefId = s.ProductRefId
WHERE   s.CompanyRefId = @CompanyRefId
  AND   s.CStock > 0
  AND   NOT EXISTS (SELECT 1 FROM InventoryTransaction t
                    WHERE t.CompanyRefId = s.CompanyRefId
                      AND t.ProductRefId = s.ProductRefId);

PRINT CONCAT('Opening movement rows written: ', @@ROWCOUNT);
GO


/* ---------------------------------------------------------------------------
   PART F  -  Check the result.
   --------------------------------------------------------------------------- */
DECLARE @CompanyRefId INT = 1;          -- <<< SAME id

SELECT  i.ItemType,
        COUNT(*)            AS Items,
        SUM(ISNULL(s.CStock,0)) AS TotalOnHand
FROM    InventoryItem i
LEFT JOIN ProductMasterCStock s ON s.CompanyRefId = i.CompanyRefId
                               AND s.ProductRefId = i.ProductRefId
WHERE   i.CompanyRefId = @CompanyRefId AND i.Active = 1
GROUP BY i.ItemType
ORDER BY i.ItemType;

-- Any product whose balance is not explained by its movements.
-- You want ZERO rows here.
SELECT  p.Prod_Code, p.PName, s.CStock AS BalanceNow,
        ISNULL(t.MovementTotal, 0) AS ExplainedByMovements
FROM    ProductMasterCStock s
JOIN    ProductMaster p ON p.Id = s.ProductRefId
JOIN    InventoryItem i ON i.CompanyRefId = s.CompanyRefId
                       AND i.ProductRefId = s.ProductRefId
OUTER APPLY (
    SELECT SUM(CASE WHEN t2.TransactionType = 'IN' THEN t2.Quantity
                    ELSE -t2.Quantity END) AS MovementTotal
    FROM   InventoryTransaction t2
    WHERE  t2.CompanyRefId = s.CompanyRefId
      AND  t2.ProductRefId = s.ProductRefId
) t
WHERE   s.CompanyRefId = @CompanyRefId
  AND   s.CStock <> ISNULL(t.MovementTotal, 0);
GO


/* ===========================================================================
   ABOUT SERIAL-TRACKED ITEMS (turbos, starters, tools)

   This script does not create them, on purpose. Each physical unit needs its
   own serial number, and the database has no way to guess those - if you have
   three turbos, they are three separate units with three separate histories.

   Add them from the screen, or through the API:

     POST /api/inventory/items            <- the kind ("Turbocharger DC13"),
                                             with itemType ASSET or TOOL and
                                             the first serial number
     POST /api/inventory/assets/register  <- each additional unit after that

   Once a unit exists you can Issue it to a truck, Return it for repair, and
   Mark it repaired, and the system keeps the full history of which truck it
   was on each time.
   =========================================================================== */
