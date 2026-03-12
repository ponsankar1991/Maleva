# SelectSaleOrder API - Enterprise Implementation Guide

## Overview

This document describes the refactored **SelectSaleOrder** API implementation that was migrated from .NET Framework (using Dapper) to Spring Boot with JDK 17.

The implementation follows enterprise Java best practices with:
- Clean, readable, well-documented code
- Layered architecture (Controller → Service → Repository)
- Utility classes for common operations
- Comprehensive logging at each step
- Proper error handling and validation
- SQL injection prevention through parameterized queries

---

## Architecture

### Layer Structure

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                       │
│  - SaleOrderController.selectSaleOrder()                    │
│  - @PostMapping("/search")                                  │
│  - Input validation via @Valid                              │
└──────────────────────┬──────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────┐
│                      SERVICE LAYER                          │
│  - SaleOrderMasterServiceImpl.selectSaleOrder()             │
│  - Business logic orchestration                            │
│  - Filter validation and logging                           │
│  - Data transformation and sorting                         │
└──────────────────────┬──────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────┐
│                  HELPER & UTILITY LAYERS                    │
│  - SaleOrderFilterHelper: Filter validation and parsing     │
│  - DateTimeUtil: Date formatting and parsing               │
│  - QueryResultMapper: Object array to DTO mapping          │
│  - SaleOrderSpecification: JPA specification building       │
└──────────────────────┬──────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────┐
│                  PERSISTENCE LAYER                          │
│  - SaleOrderMasterRepository                               │
│  - Native query execution with joins                       │
│  - JPA Specification support                               │
└─────────────────────────────────────────────────────────────┘
```

---

## Key Classes

### 1. **SaleOrderMasterServiceImpl**
**File**: `src/main/java/my/maleva/api/service/impl/SaleOrderMasterServiceImpl.java`

Main service implementation with `selectSaleOrder()` method that:
- Validates filter parameters
- Builds dynamic JPA specification
- Fetches and maps sale master data
- Fetches and maps sale details data
- Returns combined response

**Key Methods**:
- `selectSaleOrder(SaleOrderFilterDTO)` - Main API method
- `buildFilterSpecification()` - Creates JPA specification from filter
- `fetchAndMapSaleMasterData()` - Fetches and maps master records
- `fetchAndMapSaleDetailsData()` - Fetches and maps detail records
- `buildSaleF5ViewResponse()` - Constructs final response

**Example**:
```java
SaleF5View response = saleOrderMasterService.selectSaleOrder(filterDTO);
```

---

### 2. **SaleOrderFilterHelper**
**File**: `src/main/java/my/maleva/api/service/helper/SaleOrderFilterHelper.java`

Utility component for filter parameter handling:
- Validates filter parameters
- Provides filter activation check methods
- Parses status lists
- Handles date conversions
- Formats log messages

**Key Methods**:
```java
// Validation
filterHelper.validateFilter(filter);

// Filter activation checks
filterHelper.isCustomerFilterActive(filter);
filterHelper.isEmployeeFilterActive(filter);
filterHelper.isSearchFilterActive(filter);

// Parsing utilities
Integer[] statusArray = filterHelper.parseStatusList("1,2,3");

// Date utilities
LocalDateTime start = filterHelper.getDateRangeStart(localDate);
LocalDateTime end = filterHelper.getDateRangeEnd(localDate);

// Logging
filterHelper.logFilterDetails(filter);
```

---

### 3. **DateTimeUtil**
**File**: `src/main/java/my/maleva/api/util/DateTimeUtil.java`

Centralized date/time operations:
- Consistent date formatting (dd/MM/yyyy)
- DateTime formatting (dd/MM/yyyy HH:mm:ss)
- Safe parsing with error handling
- Date range checking
- Day boundary conversions

**Key Methods**:
```java
// Formatting
String formatted = DateTimeUtil.formatDate(localDate);
String formatted = DateTimeUtil.formatDateTime(localDateTime);

// Parsing
LocalDate date = DateTimeUtil.parseDate("15/03/2026");
LocalDateTime datetime = DateTimeUtil.parseDateTime("15/03/2026 14:30:00");

// Range checking
boolean inRange = DateTimeUtil.isBetween(date, startDate, endDate);

// Day boundaries
LocalDateTime startOfDay = DateTimeUtil.toStartOfDay(date);
LocalDateTime endOfDay = DateTimeUtil.toEndOfDay(date);
```

---

### 4. **QueryResultMapper**
**File**: `src/main/java/my/maleva/api/mapper/QueryResultMapper.java`

Maps raw database Object[] arrays to typed ViewModels:
- Type-safe conversions
- Null-safety with sensible defaults
- Error handling and logging
- Consistent with SQL ISNULL behavior

**Key Methods**:
```java
// Map single row
SaleMasterViewModel vm = queryResultMapper.mapSaleMasterRow(row);

// Map list of rows
List<SaleMasterViewModel> list = queryResultMapper.mapSaleMasterRows(rawData);

// Safe type conversions (used internally)
Integer intVal = castToInteger(obj);          // null-safe
Double doubleVal = castToDouble(obj);         // returns 0.0 if null
String strVal = castToString(obj);            // returns "" if null
LocalDateTime dt = castToLocalDateTime(obj);  // null-safe
```

---

### 5. **SaleOrderSpecification**
**File**: `src/main/java/my/maleva/api/specification/SaleOrderSpecification.java`

JPA Specification for dynamic WHERE clause building:
- Replaces string concatenation (SQL injection safe)
- Supports complex filter logic
- Builds parameter-based queries

**Key Logic**:
```java
// Customer filter
if (customerId != null && customerId != 0)
  // Add: criteria.where(cb.equal(root.get("customerRefId"), customerId))

// Employee filter with sub-employee lookup
if (employeeId != 0) {
  if (dashboardStatus == 2) {
    // Add sub-query for RulesTypeMaster
  } else {
    // Simple employee filter
  }
}

// Status list filter
if (statusList != null) {
  // Parse comma-separated list
  // Add: criteria.where(root.get("jStatus").in(statusArray))

// Date range filters (ETA, Pickup, SaleDate)
// Add: criteria.where(cb.between(date, fromDate, toDate))
```

---

## Filter Parameters

### SaleOrderFilterDTO Properties

| Parameter | Type | Description | Example |
|-----------|------|-------------|---------|
| comid | Integer | Company ID (required) | 6 |
| id | Integer | Customer ID (optional) | 0 (means no filter) |
| jId | Integer | Job Master ID (optional) | 0 |
| employeeid | Integer | Employee ID (optional) | 14 |
| dashboardStatus | Integer | 2 = include sub-employees | 0 or 2 |
| statusList | String | Comma-separated status IDs | "1,2,3" |
| statusid | Integer | Single status ID | 0 |
| completestatusnotshow | Boolean | Exclude complete status (8) | false |
| remarks | Integer | 1=has remarks, 2=empty remarks | 3 |
| offvesselname | String | Offshore vessel name search | "" |
| loadingvesselname | String | Loading vessel name search | "" |
| search | String | Bill number or invoice search | "" |
| invoice | Boolean | Search in invoice or bill | false |
| eta | Boolean | Filter by ETA dates | false |
| etaType | Integer | 1=OETA, 2=ETA, else=Both | 0 |
| fromdate | LocalDate | Range start date | "2026/03/11" |
| todate | LocalDate | Range end date | "2026/03/11" |
| pickup | Boolean | Filter by pickup date | false |
| invoicecheck | Boolean | Filter by invoice status | false |

---

## Execution Flow

### Step-by-Step Process

```
1. REQUEST RECEIVED
   └─> @PostMapping("/search") receives SaleOrderFilterDTO

2. FILTER VALIDATION (SaleOrderFilterHelper)
   └─> Validate company ID
   └─> Set default dates if missing
   └─> Log filter details

3. BUILD SPECIFICATION (SaleOrderSpecification)
   └─> Parse all filter parameters
   └─> Build JPA Criteria predicates
   └─> Combine with AND logic

4. FETCH FILTERED ORDER IDS
   └─> Execute JPA Specification query
   └─> Collect list of matching IDs
   └─> Log count

5. FETCH SALE MASTER DATA (Native Query)
   └─> Execute complex SQL with 7 JOINs:
       - SaleOrderMaster (A)
       - Customer (B)
       - EmployeeMaster (E)
       - JobStatusMaster (J)
       - JobTypeMaster (JT)
       - SaleMaster (SM)
       - SymbolMaster (S)
   └─> Get 33 columns with formatting
   └─> Map Object[] to SaleMasterViewModel

6. FETCH SALE DETAILS DATA (Native Query)
   └─> Execute complex SQL with 3 JOINs:
       - SaleOrderDetails (B)
       - SaleOrderMaster (A)
       - ItemMaster (I)
   └─> Get 14 columns
   └─> Map Object[] to SaleDetailsViewModel

7. FILTER & SORT
   └─> Filter by matched order IDs
   └─> Sort by DETA (formatted date) then BillDate
   └─> Return sorted lists

8. BUILD RESPONSE
   └─> Combine master + details using MapStruct
   └─> Return SaleF5View object

9. RESPONSE SENT
   └─> HTTP 200 with data
   └─> Logging includes execution time
```

---

## Code Quality Features

### 1. **Readability**
- Clear variable names
- Organized into logical methods
- Step numbers in comments for clarity
- Descriptive JavaDoc comments

### 2. **Logging**
- Detailed logging at each step
- INFO level for major operations
- DEBUG level for detailed steps
- ERROR level with stack traces
- Execution time tracking

```java
logger.info("========== SelectSaleOrder API Started ==========");
logger.debug("Step 1: Validating filter parameters");
logger.info("Found {} SaleOrderMaster records matching filter criteria", count);
logger.error("ERROR in SelectSaleOrder after {} ms", executionTime);
```

### 3. **Error Handling**
- Try-catch blocks around risky operations
- Meaningful error messages
- Exception chaining with cause
- Validation before processing

### 4. **Null Safety**
- Safe type conversions
- Default values for nulls (consistent with SQL)
- Empty collection checks
- Null-safe comparisons

### 5. **Performance**
- Filter on database before fetching all data
- Lazy evaluation with streams
- Efficient mapping logic
- Execution time monitoring

---

## Postman Integration

### API Endpoint

```
POST http://localhost:8082/api/sale-orders/search
```

### Request Headers

```
Content-Type: application/json
```

### Sample Request

```json
{
  "Comid": "6",
  "Fromdate": "2026/03/11",
  "Todate": "2026/03/11",
  "Id": 0,
  "Employeeid": "14",
  "Statusid": 0,
  "completestatusnotshow": false,
  "Search": "",
  "Remarks": 3,
  "ETA": false,
  "ETAType": 0,
  "Pickup": false,
  "Offvesselname": "",
  "Loadingvesselname": "",
  "JId": 0,
  "Invoice": false,
  "statusList": null,
  "DashboardStatus": 0
}
```

### Sample Response

```json
{
  "IsSuccess": true,
  "StatusCode": 200,
  "Message": "Success",
  "Data1": [
    {
      "salemaster": [
        {
          "Id": 1,
          "BillNo": 1001,
          "BillDate": "15/03/2026",
          "CustomerName": "ABC Company",
          "NetAmt": 50000.00,
          ...
        }
      ],
      "saledetails": [
        {
          "SAmount": 10000.00,
          "ProductName": "Item 1",
          ...
        }
      ]
    }
  ],
  "Data3": "[{...}]",
  "ErrorDetails": null
}
```

---

## Common Issues & Solutions

### Issue 1: Date Parsing Error
**Error**: `Text '2026/03/11' could not be parsed`

**Solution**: Use `@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")` in DTO:
```java
@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
private LocalDate fromdate;
```

---

### Issue 2: Empty Filter Results
**Possible Causes**:
- Company ID mismatch
- Date range doesn't match any records
- Status filters are too restrictive

**Solution**: Check filter values in Postman:
```java
logger.debug("Filter: {}", filterHelper.formatFilterForLogging(filter));
```

---

### Issue 3: Null Reference in Mapper
**Error**: `NullPointerException in mapSaleMasterRow()`

**Solution**: All conversion methods are null-safe:
```java
// Returns "" for null instead of NPE
String value = castToString(row[3]);

// Returns 0.0 for null (SQL ISNULL behavior)
Double amount = castToDouble(row[10]);
```

---

## Best Practices Used

✅ **DRY (Don't Repeat Yourself)**
- Utility classes for common operations
- Helper methods for repeated logic

✅ **SRP (Single Responsibility Principle)**
- Each class has one reason to change
- Service focuses on orchestration
- Helper focuses on filtering
- Mapper focuses on data conversion

✅ **Clean Code**
- Meaningful names
- Small methods
- No deep nesting
- Proper documentation

✅ **Error Handling**
- Validation before processing
- Meaningful error messages
- Proper exception chaining

✅ **Logging**
- Comprehensive logging
- Different log levels
- Performance tracking
- Easy debugging

✅ **SQL Injection Prevention**
- No string concatenation for SQL
- Parameter binding through JPA
- Specification-based queries

---

## Performance Considerations

1. **Database Queries**: Uses 2 native queries (master + details)
2. **Filtering**: Applied at database level, not in memory
3. **Mapping**: Single pass through data
4. **Sorting**: Applied in memory after filtering (small dataset)
5. **Execution Time**: Logged for monitoring

---

## Future Improvements

- [ ] Add caching for repeated queries
- [ ] Implement pagination for large result sets
- [ ] Add response compression
- [ ] Implement query optimization indexes
- [ ] Add metrics and monitoring

---

## Contact & Support

For issues or questions, refer to:
- Code comments in each class
- JavaDoc documentation
- Log output for troubleshooting
- Team documentation

