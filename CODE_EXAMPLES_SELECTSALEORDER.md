# SelectSaleOrder Implementation - Code Examples

## Complete Example Flows

### Example 1: Filter with Employee and Date Range

```java
// 1. Create Filter DTO
SaleOrderFilterDTO filter = SaleOrderFilterDTO.builder()
    .comid(6)
    .employeeid(14)
    .fromdate(LocalDate.of(2026, 3, 1))
    .todate(LocalDate.of(2026, 3, 31))
    .eta(true)
    .etaType(2)  // Filter by ETA only
    .build();

// 2. Call Service
SaleF5View response = saleOrderMasterService.selectSaleOrder(filter);

// 3. Process Response
if (response.getSalemaster() != null) {
    response.getSalemaster().forEach(master -> {
        System.out.println("Bill: " + master.getBillNoDisplay());
        System.out.println("Customer: " + master.getCustomerName());
        System.out.println("Amount: " + master.getNetAmt());
    });
}
```

---

### Example 2: Filter by Customer and Status

```java
// Filter all orders for customer ID=5 with status=2 or 3
SaleOrderFilterDTO filter = SaleOrderFilterDTO.builder()
    .comid(6)
    .id(5)                        // Customer filter
    .statusList("2,3")            // Multiple statuses
    .fromdate(LocalDate.now())
    .todate(LocalDate.now())
    .build();

SaleF5View response = saleOrderMasterService.selectSaleOrder(filter);
```

---

### Example 3: Filter with Manager Dashboard (Sub-employees)

```java
// When manager views their team's orders (dashboardStatus=2)
SaleOrderFilterDTO filter = SaleOrderFilterDTO.builder()
    .comid(6)
    .employeeid(100)              // Manager's ID
    .dashboardStatus(2)           // Include sub-employees
    .fromdate(LocalDate.now())
    .todate(LocalDate.now())
    .build();

SaleF5View response = saleOrderMasterService.selectSaleOrder(filter);
// This will include orders from employee 100 AND all their subordinates
```

---

### Example 4: Search by Invoice Number

```java
// Search specific invoice
SaleOrderFilterDTO filter = SaleOrderFilterDTO.builder()
    .comid(6)
    .search("INV-2026-00123")      // Search value
    .invoice(true)                // Search in invoice field
    .fromdate(LocalDate.now())
    .todate(LocalDate.now())
    .build();

SaleF5View response = saleOrderMasterService.selectSaleOrder(filter);
```

---

## Using Helper Classes

### DateTimeUtil Usage

```java
// Formatting dates
LocalDate date = LocalDate.of(2026, 3, 15);
String formatted = DateTimeUtil.formatDate(date);  // "15/03/2026"

LocalDateTime dateTime = LocalDateTime.of(2026, 3, 15, 14, 30, 0);
String formatted = DateTimeUtil.formatDateTime(dateTime);  // "15/03/2026 14:30:00"

// Parsing dates
LocalDate parsed = DateTimeUtil.parseDate("15/03/2026");
LocalDateTime parsedDT = DateTimeUtil.parseDateTime("15/03/2026 14:30:00");

// Date range operations
LocalDate startDate = LocalDate.of(2026, 3, 1);
LocalDate endDate = LocalDate.of(2026, 3, 31);
LocalDate checkDate = LocalDate.of(2026, 3, 15);

boolean inRange = DateTimeUtil.isBetween(checkDate, startDate, endDate);  // true

// Day boundaries
LocalDateTime startOfDay = DateTimeUtil.toStartOfDay(date);  // 2026-03-15T00:00:00
LocalDateTime endOfDay = DateTimeUtil.toEndOfDay(date);      // 2026-03-15T23:59:59
```

---

### SaleOrderFilterHelper Usage

```java
@Autowired
private SaleOrderFilterHelper filterHelper;

// Validate filter
filterHelper.validateFilter(filter);
// Sets defaults for nulls, validates company ID

// Check filter activation
if (filterHelper.isCustomerFilterActive(filter)) {
    // Customer filter is being used
}

if (filterHelper.isEmployeeFilterActive(filter)) {
    // Employee filter is being used
}

// Parse status list
Integer[] statusIds = filterHelper.parseStatusList("1,2,3");
// Result: [1, 2, 3]

// Get date boundaries
LocalDateTime rangeStart = filterHelper.getDateRangeStart(LocalDate.now());
LocalDateTime rangeEnd = filterHelper.getDateRangeEnd(LocalDate.now());

// Log details
filterHelper.logFilterDetails(filter);
// DEBUG level logging of all active filters

// Check sub-employee inclusion
boolean includeSubEmployees = filterHelper.shouldIncludeSubEmployees(filter);
// true if filter.getDashboardStatus() == 2

// Get ETA type description
String etaDesc = filterHelper.getEtaTypeDescription(1);  // "OETA (Outbound ETA)"
```

---

### QueryResultMapper Usage

```java
@Autowired
private QueryResultMapper queryResultMapper;

// Map single SaleMaster row (33 columns from database)
Object[] row = queryResult.get(0);
SaleMasterViewModel vm = queryResultMapper.mapSaleMasterRow(row);

// Map list of rows
List<Object[]> rawData = repository.findSaleMasterRawDataWithJoins(companyId);
List<SaleMasterViewModel> mapped = queryResultMapper.mapSaleMasterRows(rawData);

// Map single SaleDetails row (14 columns from database)
Object[] detailRow = queryResult.get(0);
SaleDetailsViewModel detailVm = queryResultMapper.mapSaleDetailsRow(detailRow);

// Map list of detail rows
List<Object[]> rawDetailsData = repository.findSaleDetailsRawDataWithJoins(companyId);
List<SaleDetailsViewModel> mappedDetails = queryResultMapper.mapSaleDetailsRows(rawDetailsData);
```

---

## Controller Usage

```java
@PostMapping("/search")
public ResponseEntity<?> selectSaleOrder(
        @Valid @RequestBody SaleOrderFilterDTO filter,
        HttpServletRequest request) {
    
    logger.info("SelectSaleOrder endpoint called");
    logger.info("Request IP: {}", request.getRemoteAddr());
    
    try {
        // Validate filter
        if (filter.getComid() == null || filter.getComid() == 0) {
            return ResponseEntity.badRequest()
                .body(new ResponseViewModel(
                    false, 
                    400, 
                    "Company ID is required",
                    null, 
                    null
                ));
        }
        
        // Call service
        SaleF5View response = saleOrderMasterService.selectSaleOrder(filter);
        
        // Return success response
        ResponseViewModel result = new ResponseViewModel();
        result.setIsSuccess(true);
        result.setStatusCode(200);
        result.setMessage("Success");
        result.setData1(response);
        result.setData3(com.fasterxml.jackson.databind.ObjectMapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(response));
        
        return ResponseEntity.ok(result);
        
    } catch (Exception ex) {
        logger.error("Error in selectSaleOrder", ex);
        
        return ResponseEntity.status(500)
            .body(new ResponseViewModel(
                false,
                500,
                "Error: " + ex.getMessage(),
                null,
                ex.getMessage()
            ));
    }
}
```

---

## Logging Output Examples

### Normal Execution

```
2026-03-12 10:15:30 INFO  SelectSaleOrderMasterServiceImpl - ========== SelectSaleOrder API Started ==========
2026-03-12 10:15:30 DEBUG SelectSaleOrderMasterServiceImpl - Step 1: Validating filter parameters
2026-03-12 10:15:30 DEBUG SaleOrderFilterHelper - Filter validation completed - Company: 6, FromDate: 2026-03-12, ToDate: 2026-03-12
2026-03-12 10:15:30 DEBUG SaleOrderFilterHelper - SaleOrderFilter Details: Company=6, Employee=14, FromDate=2026-03-12, ToDate=2026-03-12
2026-03-12 10:15:30 DEBUG SelectSaleOrderMasterServiceImpl - Step 2: Building dynamic specification from filter
2026-03-12 10:15:30 DEBUG SelectSaleOrderMasterServiceImpl - Step 3: Fetching filtered order IDs
2026-03-12 10:15:31 INFO  SelectSaleOrderMasterServiceImpl - Found 45 SaleOrderMaster records matching filter criteria
2026-03-12 10:15:31 DEBUG SelectSaleOrderMasterServiceImpl - Step 4: Fetching sale master data with joins
2026-03-12 10:15:31 DEBUG QueryResultMapper - Mapping 45 raw SaleMaster rows to ViewModels
2026-03-12 10:15:32 INFO  QueryResultMapper - Successfully mapped 45 SaleMaster rows
2026-03-12 10:15:32 DEBUG SelectSaleOrderMasterServiceImpl - Step 5: Fetching sale details data with joins
2026-03-12 10:15:32 DEBUG QueryResultMapper - Mapping 120 raw SaleDetails rows to ViewModels
2026-03-12 10:15:32 INFO  QueryResultMapper - Successfully mapped 120 SaleDetails rows
2026-03-12 10:15:32 DEBUG SelectSaleOrderMasterServiceImpl - Step 6: Building final SaleF5View response
2026-03-12 10:15:32 INFO  SelectSaleOrderMasterServiceImpl - SelectSaleOrder completed successfully in 2345 ms
2026-03-12 10:15:32 INFO  SelectSaleOrderMasterServiceImpl - ========== SelectSaleOrder API Completed ==========
```

### Error Execution

```
2026-03-12 10:20:30 INFO  SelectSaleOrderMasterServiceImpl - ========== SelectSaleOrder API Started ==========
2026-03-12 10:20:30 DEBUG SelectSaleOrderMasterServiceImpl - Step 1: Validating filter parameters
2026-03-12 10:20:30 WARN  SaleOrderFilterHelper - Company ID is null or invalid, using default: 6
2026-03-12 10:20:30 DEBUG SelectSaleOrderMasterServiceImpl - Step 2: Building dynamic specification from filter
2026-03-12 10:20:30 ERROR SelectSaleOrderMasterServiceImpl - ERROR in SelectSaleOrder after 123 ms - Company: 6, Error: Invalid date format
2026-03-12 10:20:30 ERROR SelectSaleOrderMasterServiceImpl - ========== SelectSaleOrder API FAILED ==========
```

---

## Configuration Required

### pom.xml Dependencies

```xml
<!-- Lombok for DTOs -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>

<!-- MapStruct for mapping -->
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.2.Final</version>
</dependency>

<!-- Spring Data JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- SQL Server Driver -->
<dependency>
    <groupId>com.microsoft.sqlserver</groupId>
    <artifactId>mssql-jdbc</artifactId>
    <version>12.2.0.jre11</version>
</dependency>

<!-- Jackson for JSON -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>

<!-- Validation -->
<dependency>
    <groupId>jakarta.validation</groupId>
    <artifactId>jakarta.validation-api</artifactId>
</dependency>
```

---

## Testing Guide

### Unit Test Example

```java
@SpringBootTest
@RunWith(SpringRunner.class)
public class SelectSaleOrderTest {

    @Autowired
    private SaleOrderMasterService service;
    
    @Autowired
    private SaleOrderFilterHelper filterHelper;

    @Test
    public void testSelectSaleOrderWithValidFilter() {
        // Arrange
        SaleOrderFilterDTO filter = SaleOrderFilterDTO.builder()
            .comid(6)
            .fromdate(LocalDate.now())
            .todate(LocalDate.now())
            .build();

        // Act
        SaleF5View response = service.selectSaleOrder(filter);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getSalemaster());
        assertNotNull(response.getSaledetails());
    }

    @Test
    public void testFilterHelperValidation() {
        // Arrange
        SaleOrderFilterDTO filter = new SaleOrderFilterDTO();

        // Act
        filterHelper.validateFilter(filter);

        // Assert
        assertNotNull(filter.getComid());
        assertNotNull(filter.getFromdate());
        assertNotNull(filter.getTodate());
    }

    @Test
    public void testDateTimeUtilFormatting() {
        // Arrange
        LocalDate date = LocalDate.of(2026, 3, 15);

        // Act
        String formatted = DateTimeUtil.formatDate(date);

        // Assert
        assertEquals("15/03/2026", formatted);
    }
}
```

---

## Performance Optimization Tips

1. **Add Database Indexes**
   ```sql
   CREATE INDEX idx_saleorder_company_active 
       ON SaleOrderMaster(CompanyRefId, Active)
   
   CREATE INDEX idx_saleorder_employee 
       ON SaleOrderMaster(EmployeeRefId)
   
   CREATE INDEX idx_saleorder_status 
       ON SaleOrderMaster(JStatus)
   
   CREATE INDEX idx_saleorder_saledate 
       ON SaleOrderMaster(SaleDate)
   ```

2. **Add Pagination for Large Result Sets**
   ```java
   Pageable pageable = PageRequest.of(0, 100);
   Page<SaleOrderMaster> page = repository.findAll(spec, pageable);
   ```

3. **Cache Frequently Accessed Data**
   ```java
   @Cacheable(value = "saleorders")
   public SaleF5View selectSaleOrder(SaleOrderFilterDTO filter) {
       // Implementation
   }
   ```

4. **Monitor Query Execution**
   ```yaml
   # application.yaml
   spring:
     jpa:
       properties:
         hibernate:
           generate_statistics: true
   ```

---

## References

- **Jackson Documentation**: https://github.com/FasterXML/jackson
- **MapStruct Documentation**: https://mapstruct.org/
- **Spring Data JPA**: https://spring.io/projects/spring-data-jpa
- **Jakarta Persistence**: https://jakarta.ee/specifications/persistence/

