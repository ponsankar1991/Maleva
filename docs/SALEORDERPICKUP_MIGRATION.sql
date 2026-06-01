-- ============================================================================
-- SQL Server Migration Script: SaleOrderMaster → SaleOrderPickup
-- ============================================================================
-- Purpose: Extract pickup address and quantity list (separated by "{@}" delimiter)
--          from SaleOrderMaster table and insert into new SaleOrderPickup table
--
-- Prerequisites:
--   - SaleOrderMaster table exists with pickupaddress and quantitylist columns
--   - SaleOrderPickup table created (new table)
--   - Backup of SaleOrderMaster taken before running this script
--
-- Database: DemoMaleva (SQL Server)
-- Created Date: 2026-05-31
-- ============================================================================

USE DemoMaleva;
GO

-- ============================================================================
-- Step 1: Create temporary staging table to parse delimited values
-- ============================================================================
IF OBJECT_ID('tempdb..#SaleOrderPickupStaging', 'U') IS NOT NULL
    DROP TABLE #SaleOrderPickupStaging;

-- ============================================================================
-- Step 1: Create temporary staging table to parse delimited values
-- ============================================================================
IF OBJECT_ID('tempdb..#SaleOrderPickupStaging', 'U') IS NOT NULL
    DROP TABLE #SaleOrderPickupStaging;

CREATE TABLE #SaleOrderPickupStaging (
    SaleOrderMasterRefId INT,
    PickupAddressItem NVARCHAR(2000),
    PickupQuantityItem NVARCHAR(100),
    PickupTimeItem NVARCHAR(100),
    ItemSequence INT
);

GO

-- ============================================================================
-- Step 2: Parse and populate staging table (SQL Server 2016+ Compatible)
-- Convert "{@}" delimiter to "|" (single char) for STRING_SPLIT compatibility
-- ============================================================================
;WITH AddressRows AS (
    -- Split addresses by converting {@ to | and using STRING_SPLIT
    SELECT
        som.Id,
        LTRIM(RTRIM(addr.value)) AS Address,
        ROW_NUMBER() OVER (PARTITION BY som.Id ORDER BY (SELECT NULL)) AS ItemSeq
    FROM SaleOrderMaster som
    CROSS APPLY STRING_SPLIT(
        REPLACE(
            CASE WHEN som.pickupaddress IS NULL OR LTRIM(som.pickupaddress) = '' THEN ''
                 ELSE RTRIM(LTRIM(som.pickupaddress))
            END,
            '{@}',
            '|'
        ),
        '|'
    ) AS addr
    WHERE som.pickupaddress IS NOT NULL
        AND LTRIM(som.pickupaddress) <> ''
        AND LTRIM(RTRIM(addr.value)) <> ''
),
QuantityRows AS (
    -- Split quantities by converting {@ to | and using STRING_SPLIT
    SELECT
        som.Id,
        LTRIM(RTRIM(qty.value)) AS Quantity,
        ROW_NUMBER() OVER (PARTITION BY som.Id ORDER BY (SELECT NULL)) AS ItemSeq
    FROM SaleOrderMaster som
    CROSS APPLY STRING_SPLIT(
        REPLACE(
            CASE WHEN som.pickupQuantitylist IS NULL OR LTRIM(som.pickupQuantitylist) = '' THEN ''
                 ELSE RTRIM(LTRIM(som.pickupQuantitylist))
            END,
            '{@}',
            '|'
        ),
        '|'
    ) AS qty
    WHERE som.pickupQuantitylist IS NOT NULL
        AND LTRIM(som.pickupQuantitylist) <> ''
        AND LTRIM(RTRIM(qty.value)) <> ''
),
     TimeRows AS (
 -- Split quantities by converting {@ to | and using STRING_SPLIT
 SELECT
     som.Id,
     LTRIM(RTRIM(qty.value)) AS Quantity,
     ROW_NUMBER() OVER (PARTITION BY som.Id ORDER BY (SELECT NULL)) AS ItemSeq
 FROM SaleOrderMaster som
     CROSS APPLY STRING_SPLIT(
     REPLACE(
     CASE WHEN som.pickuptimelist IS NULL OR LTRIM(som.pickuptimelist) = '' THEN ''
     ELSE RTRIM(LTRIM(som.pickuptimelist))
     END,
     '{@}',
     '|'
     ),
     '|'
     ) AS qty
 WHERE som.pickuptimelist IS NOT NULL
   AND LTRIM(som.pickuptimelist) <> ''
   AND LTRIM(RTRIM(qty.value)) <> ''
     )


INSERT INTO #SaleOrderPickupStaging (
    SaleOrderMasterRefId,
    PickupAddressItem,
    PickupQuantityItem,
        PickupTimeItem,
    ItemSequence
)
SELECT
    a.Id AS SaleOrderMasterRefId,
    a.Address AS PickupAddressItem,
    ISNULL(q.Quantity, '') AS PickupQuantityItem,
    ISNULL(t.Quantity, '') AS PickupTimeItem,
    a.ItemSeq AS ItemSequence
FROM AddressRows a
LEFT JOIN QuantityRows q
    ON a.Id = q.Id
    AND a.ItemSeq = q.ItemSeq
left join TimeRows t
    ON a.Id = t.Id
    AND a.ItemSeq = t.ItemSeq
;

GO

GO

-- ============================================================================
-- Step 3: Insert parsed data into SaleOrderPickup table
-- ============================================================================
INSERT INTO SaleOrderPickup (
    SaleOrderMasterRefId,
    PickupAddress,
    PickupTime
    PickupQuantity,
    CreatedDate
)
SELECT
    SaleOrderMasterRefId,
    CASE
        WHEN PickupAddressItem IS NULL OR LTRIM(PickupAddressItem) = '' THEN NULL
        ELSE PickupAddressItem
    END AS PickupAddress,
    CASE
        WHEN PickupTimeItem IS NULL OR LTRIM(PickupTimeItem) = '' THEN NULL
        ELSE PickupTimeItem
        END AS PickupTime,
    CASE
        WHEN PickupQuantityItem IS NULL OR LTRIM(PickupQuantityItem) = '' THEN NULL
        ELSE PickupQuantityItem
    END AS PickupQuantity,
    GETDATE() AS CreatedDate
FROM
    #SaleOrderPickupStaging
WHERE
    PickupAddressItem IS NOT NULL
    AND LTRIM(PickupAddressItem) <> '';

GO

-- ============================================================================
-- Step 4: Verification Queries
-- ============================================================================

-- Count records migrated
SELECT
    'SaleOrderPickup Records Inserted' AS CheckPoint,
    COUNT(*) AS RecordCount
FROM
    SaleOrderPickup;

GO

-- Sample verification - show a few migrated records
SELECT TOP 10
    sp.Id,
    sp.SaleOrderMasterRefId,
    sp.PickupAddress,
    sp.PickupQuantity,
    sp.CreatedDate,
    som.CNumberDisplay
FROM
    SaleOrderPickup sp
INNER JOIN
    SaleOrderMaster som ON sp.SaleOrderMasterRefId = som.Id
ORDER BY
    sp.SaleOrderMasterRefId, sp.Id;

GO

-- Show migration statistics by SaleOrderMaster
SELECT
    som.Id,
    som.CNumberDisplay,
    COUNT(sp.Id) AS PickupRecordCount,
    som.pickupaddress AS OriginalPickupAddressDelimited,
    som.quantitylist AS OriginalQuantityListDelimited
FROM
    SaleOrderMaster som
LEFT JOIN
    SaleOrderPickup sp ON sp.SaleOrderMasterRefId = som.Id
WHERE
    (som.pickupaddress IS NOT NULL AND LTRIM(som.pickupaddress) <> '')
    OR (som.quantitylist IS NOT NULL AND LTRIM(som.quantitylist) <> '')
GROUP BY
    som.Id, som.CNumberDisplay, som.pickupaddress, som.quantitylist
ORDER BY
    som.Id;

GO

-- ============================================================================
-- Step 5: Cleanup - Drop staging table
-- ============================================================================
DROP TABLE #SaleOrderPickupStaging;

GO

-- ============================================================================
-- Optional: Verify no data loss
-- Count original delimited records vs new table records
-- ============================================================================
DECLARE @TotalOriginalRecords INT;
DECLARE @TotalNewRecords INT;

SELECT @TotalOriginalRecords = COUNT(*)
FROM SaleOrderMaster
WHERE (pickupaddress IS NOT NULL AND LTRIM(pickupaddress) <> '')
   OR (quantitylist IS NOT NULL AND LTRIM(quantitylist) <> '');

SELECT @TotalNewRecords = COUNT(*) FROM SaleOrderPickup;

SELECT
    'Migration Summary' AS Summary,
    @TotalOriginalRecords AS SaleOrderMasterRecordsProcessed,
    @TotalNewRecords AS SaleOrderPickupRecordsCreated,
    CASE
        WHEN @TotalNewRecords >= @TotalOriginalRecords THEN 'SUCCESS: All records migrated'
        ELSE 'WARNING: Some records may not have been migrated'
    END AS MigrationStatus;

GO

-- ============================================================================
-- IMPORTANT: After verification, consider these next steps:
-- ============================================================================
-- 1. Backup the migrated data
-- 2. Test that application code works with new SaleOrderPickup table
-- 3. Create database backup before archiving
-- 4. Document any schema changes
-- 5. If successful, consider deprecating pickupaddress and quantitylist columns
--    in future versions (do NOT delete immediately)
-- ============================================================================

