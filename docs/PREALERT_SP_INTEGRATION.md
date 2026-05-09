# PreAlert Stored Procedure Integration Guide

## 📋 Overview

This document outlines the integration between the Java backend and the SQL Server `SP_PreAlert` stored procedure. The implementation handles INSERT and UPDATE operations for PreAlert master and detail records.

---

## 🔄 Stored Procedure: `SP_PreAlert`

**Location:** `dbo.SP_PreAlert`  
**Database:** `DemoMaleva`

### Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `@master` | NVARCHAR(MAX) | JSON array of PreAlertMaster records with nested PreAlert details |
| `@ComId` | INT | Company ID |

### Logic Flow

1. **Parse JSON Input**: Converts `@master` JSON to temporary table `#temp1`
2. **Loop Through Records**: Processes each record in the temporary table
3. **Handle INSERT/UPDATE**:
   - **If `Id = 0`**: INSERT new PreAlertMaster record
   - **If `Id > 0`**: UPDATE existing PreAlertMaster record
4. **Handle Detail Rows**:
   - **New Rows (`Id = 0`)**: INSERT into PreAlert table
   - **Existing Rows (`Id > 0`)**: UPDATE matching records
   - **No DELETE**: Old data is preserved
5. **Generate Sequence Number**: For new records only
   - Increments SequenceNo from SequenceNoMaster
   - Generates formatted CNumberDisplay (e.g., `PA0001/2026`)

### Return Values

```sql
SELECT 
    @Result AS Result,        -- 1 = Success, 0 = Error
    @msg AS Msg,             -- Success or error message
    @SaleNoDisplay AS BillNo, -- Generated/formatted CNumberDisplay
    GETDATE() AS SaleTime,   -- Current timestamp
    @prealertid AS Id        -- PreAlertMaster Id
```

---

## 🔧 Java Service Implementation

### 1. **insertPreAlert()** - Create New PreAlert Records

**Method Location**: `PreAlertReportServiceImpl.insertPreAlert()`

#### Functionality
- Creates new PreAlertMaster and PreAlert detail records
- Automatically generates sequence numbers
- Supports batch processing of multiple masters

#### JSON Structure Required

```json
[
  {
    "Id": 0,
    "CompanyRefId": 1,
    "CustomerMasterRefId": 10,
    "JobTypeMasterRefId": 5,
    "EmployeeRefId": 3,
    "CNumber": 0,
    "Port": "Singapore",
    "Vessel": "MV Example",
    "OETA": "2026-05-15",
    "LETA": "2026-05-20",
    "ALLETA": "All ETA",
    "NONE": "None",
    "ChkPort": "SGP",
    "ChkVessel": "EXP",
    "ChkPickupDate": "2026-05-10",
    "CNumberDisplay": "",
    "ChkConsolidated": "Y",
    "ChkDeliveryDone": "N",
    "FromDate": "2026-05-01",
    "EntryDate": "2026-05-05",
    "ToDate": "2026-05-31",
    "Active": 1,
    "PreAlert": "[{\"Id\": 0, \"CustomerMasterRefId\": 10, \"EmployeeMasterRefId\": 3, \"JobTypeMasterRefId\": 5, \"SaleOrderMasterRefId\": 1, \"JobStatusMasterRefId\": 1, \"BoardingOfficerRefId\": 2, \"BoardingOfficerName\": \"Officer Name\", \"ShipName\": \"Ship\", \"Vessel\": \"Vessel\", \"Commodity\": \"Commodity\", \"ETA\": \"2026-05-15\", \"ETB\": \"2026-05-16\", \"ETD\": \"2026-05-17\", \"JobNo\": \"JOB001\", \"Port\": \"SGP\", \"Weight\": \"100\", \"Package\": \"Pallet\", \"AWBNo\": \"AWB001\", \"AgentName\": \"Agent\", \"AgentPhone\": \"123456\", \"Remarks\": \"Remarks\", \"SCN\": \"SCN001\", \"Active\": 1}]"
  }
]
```

#### Usage

```java
// Create PreAlertMasterDto with detail rows
PreAlertMasterDto master = new PreAlertMasterDto();
master.setId(0); // 0 for INSERT
master.setCompanyRefId(1);
master.setPort("Singapore");
// ... set other fields ...

// Create detail rows
List<PreAlertDto> details = new ArrayList<>();
PreAlertDto detail = new PreAlertDto();
detail.setId(0);
detail.setJobNo("JOB001");
// ... set other fields ...
details.add(detail);

master.setPreAlertRows(details);

// Call service
List<PreAlertMasterDto> masters = Arrays.asList(master);
Object result = preAlertReportService.insertPreAlert(masters, companyId);
```

#### Response

```json
{
  "ok": true,
  "message": "InsertPreAlert CreateSuccess",
  "Name": "PA0001/2026",
  "Id": 123,
  "Data1": "PA0001/2026",
  "Data2": 123
}
```

---

### 2. **updatePreAlert()** - Update Existing PreAlert Records

**Method Location**: `PreAlertReportServiceImpl.updatePreAlert()`

#### Functionality
- Updates existing PreAlertMaster record
- Updates existing PreAlert detail rows by Id
- Inserts new PreAlert detail rows (Id = 0)
- Never deletes detail rows (preserves old data)

#### Prerequisites
- Master record must have `Id > 0`
- Detail rows to update must have `Id > 0`
- New detail rows must have `Id = 0`

#### Usage

```java
// Load existing master record
PreAlertMasterDto master = preAlertMasterService.getById(123);

// Modify master fields
master.setPort("New Port");

// Get existing detail rows
List<PreAlertDto> details = preAlertService.getByPreAlertMasterId(123);

// Update existing detail
details.get(0).setJobNo("UPDATED_JOB");

// Add new detail row
PreAlertDto newDetail = new PreAlertDto();
newDetail.setId(0); // 0 = INSERT
newDetail.setJobNo("NEW_JOB");
details.add(newDetail);

master.setPreAlertRows(details);

// Call service
Object result = preAlertReportService.updatePreAlert(master, companyId);
```

#### Response

```json
{
  "ok": true,
  "message": "PreAlert updated successfully",
  "Id": 123,
  "Data1": "PA0001/2026",
  "Data2": 123
}
```

---

## 🌐 REST API Endpoints

### 1. Insert PreAlert (Create)
```
POST /api/transaction/pre-alert/insert
Header: Comid: 1
Body: [PreAlertMasterDto]
```

### 2. Update PreAlert
```
PUT /api/transaction/pre-alert/{id}
Query: comId=1
Body: PreAlertMasterDto
```

### 3. Get PreAlert for Edit (Read)
```
GET /api/transaction/pre-alert/{id}/edit
Query: preAlertNo=1&comId=1
```

### 4. Get PreAlert Report
```
POST /api/transaction/pre-alert/report
Body: PreAlertSearchModel
```

---

## 📊 Database Tables

### PreAlertMaster
- **Purpose**: Header/Master records
- **Key Columns**: Id, CompanyRefId, CNumber, CNumberDisplay, Port, Vessel, OETA, LETA, Active

### PreAlert
- **Purpose**: Detail/Line item records
- **Key Columns**: Id, PreAlertMasterRefId, SaleOrderMasterRefId, JobNo, Commodity, ETA, Port, Active
- **Relationship**: Many PreAlert records belong to one PreAlertMaster

### SequenceNoMaster
- **Purpose**: Auto-increment sequence numbers
- **Key Columns**: CompanyRefId, SequenceName, SequenceNo
- **Usage**: Generates next CNumber for PreAlerts

---

## 🔑 Important Design Decisions

### 1. **No DELETE Operations**
- The stored procedure intentionally does NOT delete old detail rows
- Old data is preserved for historical tracking
- Only new (Id = 0) or modified (Id > 0) rows are processed

### 2. **JSON-Based Input**
- Uses OPENJSON for SQL Server JSON parsing
- Supports NVARCHAR(MAX) for flexible data handling
- PreAlert details are nested as JSON string within master JSON

### 3. **Automatic Sequence Generation**
- Only triggered for NEW master records (Id = 0)
- Prevents duplicate CNumber values
- Uses SequenceNoMaster for company-specific numbering

### 4. **Transactional Processing**
- BEGIN/COMMIT/ROLLBACK ensures data consistency
- All-or-nothing operation: if any error occurs, entire transaction rolls back

### 5. **NULL Handling**
- Empty strings treated as NULL
- ISNULL() defaults used for Active status (defaults to 1)

---

## ⚠️ Common Issues and Solutions

### Issue 1: Null Reference Exception in Java
**Cause**: PreAlert detail rows list is null

**Solution**:
```java
if (master.getPreAlertRows() == null) {
    master.setPreAlertRows(new ArrayList<>());
}
```

### Issue 2: "No result returned from SP_PreAlert"
**Cause**: Stored procedure not executing or returning empty result set

**Solution**:
1. Verify stored procedure exists in database
2. Check @ComId parameter is valid
3. Review database transaction logs for errors

### Issue 3: JSON Format Error
**Cause**: Invalid JSON structure or special characters

**Solution**:
```java
// Use ObjectMapper for proper JSON conversion
String json = objectMapper.writeValueAsString(objects);
```

### Issue 4: Sequence Number Not Generating
**Cause**: SequenceNoMaster record doesn't exist for company

**Solution**:
```sql
INSERT INTO SequenceNoMaster (CompanyRefId, SequenceName, SequenceNo)
VALUES (1, 'PreAlert', 0);
```

---

## 🧪 Testing

### Insert Test
```bash
curl -X POST http://localhost:8080/api/transaction/pre-alert/insert \
  -H "Content-Type: application/json" \
  -H "Comid: 1" \
  -d '[{"companyRefId": 1, "port": "SIN", "vessel": "MV Test", "preAlertRows": []}]'
```

### Update Test
```bash
curl -X PUT http://localhost:8080/api/transaction/pre-alert/123 \
  -H "Content-Type: application/json" \
  -d '{
    "id": 123,
    "companyRefId": 1,
    "port": "Updated Port",
    "preAlertRows": []
  }' \
  -d "comId=1"
```

---

## 📝 Code Changes Summary

| File | Change | Purpose |
|------|--------|---------|
| `PreAlertReportServiceImpl.java` | Updated `insertPreAlert()` | Proper JSON structure with nested PreAlert details |
| `PreAlertReportServiceImpl.java` | Added `updatePreAlert()` | Support for UPDATE operations via SP |
| `PreAlertReportService.java` | Added `updatePreAlert()` interface method | Service contract |
| `PreAlertController.java` | Added `updatePreAlert()` endpoint | REST API for updates |
| `PreAlertController.java` | Added `getPreAlertForEdit()` endpoint | REST API for fetching edit data |

---

## 📚 References

- **Stored Procedure**: `/db/sp/spprealert.sql`
- **Related DTOs**: `PreAlertMasterDto`, `PreAlertDto`
- **Service Interface**: `PreAlertReportService`
- **API Controller**: `PreAlertController`

---

**Last Updated**: May 5, 2026  
**Version**: 1.0

