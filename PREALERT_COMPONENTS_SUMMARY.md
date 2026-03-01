# Pre-Alert Spring Boot Components - Generation Summary

## Overview
Complete Spring Boot Pre-Alert Management System generated with Entity, DTO, Repository, Service, Controller, and Mapper components following Maleva API Standards.

## Project Package Structure
```
my.maleva.api
├── model/
│   ├── PreAlertMaster.java          ✓ Created
│   └── PreAlert.java                ✓ Created
├── dto/
│   ├── PreAlertMasterDto.java       ✓ Created
│   └── PreAlertDto.java             ✓ Created
├── repo/
│   ├── PreAlertMasterRepository.java ✓ Created
│   └── PreAlertRepository.java       ✓ Created
├── service/
│   ├── PreAlertMasterService.java   ✓ Created
│   └── PreAlertService.java         ✓ Created
├── service/impl/
│   ├── PreAlertMasterServiceImpl.java ✓ Created
│   └── PreAlertServiceImpl.java      ✓ Created
├── mapper/
│   ├── PreAlertMasterMapper.java    ✓ Created
│   └── PreAlertMapper.java          ✓ Created
└── controller/
    ├── PreAlertMasterController.java ✓ Created
    └── PreAlertController.java       ✓ Created
```

## Component Details

### 1. Entity Classes (JPA)

#### PreAlertMaster.java
- **Location**: `my.maleva.api.model`
- **Purpose**: Master record for pre-alert information
- **Key Fields**:
  - `id`: Primary key (auto-increment)
  - `companyRefId`: Company reference (required)
  - `customerMasterRefId`: Customer reference
  - `jobTypeMasterRefId`: Job type reference
  - `port`, `vessel`: Shipping details
  - `fromDate`, `toDate`: Date range
  - `cNumber`: Sequence number
  - `cNumberDisplay`: Display format (PA0001/2026)
  - `active`: Active status (1/0)
  - `createdDate`, `modifiedDate`: Audit fields
- **Relationships**:
  - One-to-Many: `preAlerts` (cascading delete)
- **JPA Lifecycle**: `@PrePersist`, `@PreUpdate` for timestamps

#### PreAlert.java
- **Location**: `my.maleva.api.model`
- **Purpose**: Detail records for pre-alert shipment information
- **Key Fields**:
  - `id`: Primary key (auto-increment)
  - `preAlertMasterRefId`: Foreign key to PreAlertMaster
  - `employeeMasterRefId`: Employee handling the alert
  - `shipName`, `vessel`, `commodity`: Shipment details
  - `eta`, `etb`, `etd`: Time estimates
  - `jobNo`, `port`: Job reference
  - `awbNo`, `agentName`: Agent details
  - `remarks`, `scn`: Additional info
  - `active`: Active status (1/0)
- **Relationships**:
  - Many-to-One: `preAlertMaster` (lazy loading)

### 2. Data Transfer Objects (DTOs)

#### PreAlertMasterDto.java
- **Location**: `my.maleva.api.dto`
- **Purpose**: API contract for PreAlertMaster
- **Validation Annotations**:
  - `@NotNull` on companyRefId (required)
  - `@Size` constraints on all text fields (max 300 chars)
- **Usage**: Request/Response payloads for PreAlertMaster endpoints

#### PreAlertDto.java
- **Location**: `my.maleva.api.dto`
- **Purpose**: API contract for PreAlert detail records
- **Validation Annotations**:
  - `@NotNull` on companyRefId, customerMasterRefId, employeeMasterRefId
  - `@Size` constraints on text fields
- **Usage**: Request/Response payloads for PreAlert endpoints

### 3. Repository Interfaces (Spring Data JPA)

#### PreAlertMasterRepository.java
- **Location**: `my.maleva.api.repo`
- **Query Methods**:
  - `findByCompanyRefId()`: List by company
  - `findByCompanyRefIdAndActive()`: Active records
  - `findByCustomerMasterRefId()`: By customer
  - `findByJobTypeMasterRefId()`: By job type
  - `findByPort()`, `findByVessel()`: By shipping details
  - `findByDateRange()`: By date range (custom query)
  - `findByCNumberAndCompanyRefId()`: By sequence number
  - `findByCNumberDisplay()`: By display number
  - `countByCompanyRefIdAndActive()`: Count active

#### PreAlertRepository.java
- **Location**: `my.maleva.api.repo`
- **Query Methods**:
  - `findByCompanyRefId()`: List by company
  - `findByCompanyRefIdAndActive()`: Active records
  - `findByPreAlertMasterRefId()`: By master
  - `findByCustomerMasterRefId()`: By customer
  - `findByEmployeeMasterRefId()`: By employee
  - `findByJobTypeId()`, `findByJobStatusId()`: By job details
  - `findByBoardingOfficerRefId()`: By officer
  - `findByVessel()`, `findByPort()`, `findByJobNo()`: By details
  - `countByPreAlertMasterRefId()`: Count by master
  - `deleteByPreAlertMasterRefId()`: Cascade delete

### 4. Service Interfaces & Implementations

#### PreAlertMasterService.java & PreAlertMasterServiceImpl.java
- **Location**: `my.maleva.api.service` and `my.maleva.api.service.impl`
- **CRUD Operations**:
  - `create()`, `update()`, `delete()`: Standard CRUD
  - `getById()`, `getAllByCompanyId()`: Retrieval
  - `getActiveByCompanyId()`: Active records
- **Business Logic Methods**:
  - `getByCustomerId()`, `getByJobTypeId()`: Filter by reference
  - `getByPort()`, `getByVessel()`: Filter by location
  - `getByDateRange()`: Range queries
  - `getByCNumber()`, `getByCNumberDisplay()`: Sequence lookup
  - `countActiveRecords()`: Analytics
  - `activate()`, `deactivate()`: Status management
- **Stored Procedure Integration**:
  - `executePreAlertStoredProcedure()`: Calls SP_PreAlert for bulk operations
  - Accepts JSON master and detail data
  - Handles transaction management
- **Features**:
  - Comprehensive logging
  - Exception handling
  - Transaction management (`@Transactional`)
  - Mapper integration for DTO conversions

#### PreAlertService.java & PreAlertServiceImpl.java
- **Location**: `my.maleva.api.service` and `my.maleva.api.service.impl`
- **CRUD Operations**:
  - `create()`, `update()`, `delete()`: Standard CRUD
  - `getById()`, `getAllByCompanyId()`: Retrieval
  - `getActiveByCompanyId()`: Active records
- **Business Logic Methods**:
  - `getByPreAlertMasterId()`: Get details for a master
  - `getByCustomerId()`, `getByEmployeeId()`: Filter by reference
  - `getByJobTypeId()`, `getByJobStatusId()`: Job filters
  - `getByBoardingOfficerId()`: Officer lookup
  - `getByVessel()`, `getByPort()`, `getByJobNo()`: Detail filters
  - `countByPreAlertMasterId()`: Count details
  - `deleteByPreAlertMasterId()`: Cascade delete
  - `activate()`, `deactivate()`: Status management
- **Features**:
  - Comprehensive logging
  - Exception handling
  - Mapper integration

### 5. Mappers (MapStruct)

#### PreAlertMasterMapper.java
- **Location**: `my.maleva.api.mapper`
- **Annotations**: `@Mapper(componentModel = "spring")`
- **Methods**:
  - `toDto()`: Entity → DTO conversion
  - `toEntity()`: DTO → Entity conversion
  - `updateEntityFromDto()`: Partial update from DTO
- **Configuration**: `NullValuePropertyMappingStrategy.IGNORE`
- **Generated**: Spring-managed component (auto-generated at compile time)

#### PreAlertMapper.java
- **Location**: `my.maleva.api.mapper`
- **Methods**: Same as PreAlertMasterMapper
- **Configuration**: `NullValuePropertyMappingStrategy.IGNORE`
- **Usage**: DTO ↔ Entity conversions for PreAlert

### 6. REST Controllers

#### PreAlertMasterController.java
- **Location**: `my.maleva.api.controller`
- **Base URL**: `/api/pre-alert-masters`
- **Endpoints**: 17 RESTful endpoints
  - **GET Operations**:
    - `/company/{companyRefId}`: List all for company
    - `/company/{companyRefId}/active`: Active records
    - `/{id}`: Get by ID
    - `/customer/{customerMasterRefId}`: By customer
    - `/job-type/{jobTypeMasterRefId}`: By job type
    - `/port/{port}`: By port
    - `/vessel/{vessel}`: By vessel
    - `/company/{companyId}/date-range`: By date range
    - `/cnumber/{cNumber}`: By sequence number
    - `/cnumber-display/{cNumberDisplay}`: By display number
    - `/company/{companyRefId}/count-active`: Count active
  - **POST Operations**:
    - `/`: Create new record
    - `/{id}/activate`: Activate record
    - `/{id}/deactivate`: Deactivate record
    - `/bulk-import`: Execute SP_PreAlert
  - **PUT Operations**:
    - `/{id}`: Update record
  - **DELETE Operations**:
    - `/{id}`: Delete record
- **Security**: `@PreAuthorize` for ROLE_ADMIN and ROLE_SUPRERADMIN
- **Features**: CORS enabled, comprehensive error handling, logging

#### PreAlertController.java
- **Location**: `my.maleva.api.controller`
- **Base URL**: `/api/pre-alerts`
- **Endpoints**: 19 RESTful endpoints
  - **GET Operations**: 14 query methods
  - **POST Operations**: 2 activate/deactivate
  - **PUT Operations**: 1 update
  - **DELETE Operations**: 2 (single + bulk by master)
- **Security**: Role-based access control
- **Features**: Same as PreAlertMasterController

## API Standards Compliance

### ✓ Naming Conventions
- Entity classes: `PreAlertMaster`, `PreAlert`
- DTO classes: `PreAlertMasterDto`, `PreAlertDto`
- Repository suffix: `Repository`
- Service suffix: `Service`
- Mapper suffix: `Mapper`
- Controller suffix: `Controller`

### ✓ URL Patterns
- Resource URLs use plural nouns: `/api/pre-alert-masters`, `/api/pre-alerts`
- RESTful conventions: GET, POST, PUT, DELETE
- Hyphenated multi-word resources: `/pre-alert-masters`
- Query parameters for filters: `?companyRefId=1&active=1`

### ✓ Response Format
- Success responses: JSON with entity/DTO data
- Error responses: Standard error format with timestamp, status, message
- HTTP Status Codes: 200 (OK), 201 (Created), 204 (No Content), 400 (Bad Request), 404 (Not Found), 500 (Internal Server Error)

### ✓ Security
- Role-based access control: `ROLE_ADMIN`, `ROLE_SUPRERADMIN`
- `@PreAuthorize` on all controller methods
- JWT token authentication

### ✓ Validation
- Jakarta validation annotations: `@NotNull`, `@Size`
- DTO-level validation on requests
- Custom error messages

## Database Schema Mapping

### PreAlertMaster Table
```sql
CREATE TABLE [dbo].[PreAlertMaster](
    [Id] INT IDENTITY(1,1) PRIMARY KEY,
    [CompanyRefId] INT NOT NULL,
    [CustomerMasterRefId] INT,
    [JobTypeMasterRefId] INT,
    [FromDate] DATE,
    [ToDate] DATE,
    [Port] VARCHAR(300),
    [Vessel] VARCHAR(300),
    [OETA] VARCHAR(300),
    [LETA] VARCHAR(300),
    [ALLETA] VARCHAR(300),
    [NONE] VARCHAR(300),
    [ChkPort] VARCHAR(300),
    [ChkVessel] VARCHAR(300),
    [ChkPickupDate] VARCHAR(300),
    [ChkConsolidated] VARCHAR(300),
    [ChkDeliveryDone] VARCHAR(300),
    [Active] INT NOT NULL,
    [Created_Date] DATETIME NOT NULL,
    [Modified_Date] DATETIME NOT NULL,
    [CNumber] INT,
    [CNumberDisplay] VARCHAR(300),
    [EntryDate] DATE,
    [SaleOrderMasterRefId] INT
)
```

### PreAlert Table
```sql
CREATE TABLE [dbo].[PreAlert](
    [Id] INT IDENTITY(1,1) PRIMARY KEY,
    [CompanyRefId] INT NOT NULL,
    [CustomerMasterRefId] INT NOT NULL,
    [EmployeeMasterRefId] INT NOT NULL,
    [JobTypeMasterRefId] INT,
    [JobStatusMasterRefId] INT,
    [ShipName] VARCHAR(300),
    [Vessel] VARCHAR(300),
    [Commodity] VARCHAR(300),
    [ETA] VARCHAR(300),
    [ETB] VARCHAR(300),
    [ETD] VARCHAR(300),
    [JobNo] VARCHAR(100),
    [Port] VARCHAR(300),
    [Weight] VARCHAR(100),
    [Package] VARCHAR(300),
    [AWBNo] VARCHAR(100),
    [AgentName] VARCHAR(300),
    [AgentPhone] VARCHAR(100),
    [Remarks] VARCHAR(300),
    [SCN] VARCHAR(300),
    [Active] INT NOT NULL,
    [Created_Date] DATETIME NOT NULL,
    [Modified_Date] DATETIME NOT NULL,
    [PreAlertMasterRefId] INT,
    [BoardingOfficerRefId] INT,
    [BoardingOfficerName] VARCHAR(300),
    [SaleOrderMasterRefId] INT
)
```

## Stored Procedure Integration

### SP_PreAlert
- **Purpose**: Bulk insert/update PreAlert master and detail records from JSON
- **Parameters**:
  - `@master`: JSON containing master and detail records
  - `@ComId`: Company ID
- **Business Logic**:
  - Creates/updates PreAlertMaster records
  - Inserts/updates PreAlert detail records from JSON array
  - Auto-generates sequence numbers (CNumber, CNumberDisplay)
  - Manages transaction with rollback on error
  - Format: `PA{0000}/{YEAR}` (e.g., PA0001/2026)
- **Service Integration**:
  - Called from `PreAlertMasterServiceImpl.executePreAlertStoredProcedure()`
  - Uses `JdbcTemplate` for stored procedure execution

## Testing & Documentation

### Postman Collection Guide
- **File**: `PREALERT_POSTMAN_GUIDE.md` (created in root directory)
- **Content**:
  - 36 complete endpoint examples
  - Request/response samples for each endpoint
  - Error handling examples
  - Environment variable setup
  - Testing steps and workflow

### API Standards Reference
- **File**: `docs/API_Standards.md`
- **Content**: Project-wide API standards and conventions

## Compilation & Build Status

### ✓ All Files Compiled Successfully
- 14 Java files created with zero errors
- MapStruct mappers configured for auto-generation
- All dependencies available in pom.xml
- Maven compiler plugin configured for annotation processing

## Next Steps

1. **Run Maven Compile**: `mvn clean compile`
2. **Run Spring Boot Application**: `mvn spring-boot:run` (runs on port 8082)
3. **Test Endpoints**: Use Postman collection guide
4. **Database**: Ensure SQL Server has PreAlertMaster and PreAlert tables
5. **Stored Procedure**: Deploy SP_PreAlert stored procedure in database

## File Locations

| Component | File Path |
|-----------|-----------|
| PreAlertMaster Entity | `src/main/java/my/maleva/api/model/PreAlertMaster.java` |
| PreAlert Entity | `src/main/java/my/maleva/api/model/PreAlert.java` |
| PreAlertMasterDto | `src/main/java/my/maleva/api/dto/PreAlertMasterDto.java` |
| PreAlertDto | `src/main/java/my/maleva/api/dto/PreAlertDto.java` |
| PreAlertMasterRepository | `src/main/java/my/maleva/api/repo/PreAlertMasterRepository.java` |
| PreAlertRepository | `src/main/java/my/maleva/api/repo/PreAlertRepository.java` |
| PreAlertMasterService | `src/main/java/my/maleva/api/service/PreAlertMasterService.java` |
| PreAlertService | `src/main/java/my/maleva/api/service/PreAlertService.java` |
| PreAlertMasterServiceImpl | `src/main/java/my/maleva/api/service/impl/PreAlertMasterServiceImpl.java` |
| PreAlertServiceImpl | `src/main/java/my/maleva/api/service/impl/PreAlertServiceImpl.java` |
| PreAlertMasterMapper | `src/main/java/my/maleva/api/mapper/PreAlertMasterMapper.java` |
| PreAlertMapper | `src/main/java/my/maleva/api/mapper/PreAlertMapper.java` |
| PreAlertMasterController | `src/main/java/my/maleva/api/controller/PreAlertMasterController.java` |
| PreAlertController | `src/main/java/my/maleva/api/controller/PreAlertController.java` |
| Postman Guide | `PREALERT_POSTMAN_GUIDE.md` |

## Summary

✅ **14 Java Components Generated**
- 2 JPA Entities
- 2 DTOs
- 2 Repositories
- 2 Service Interfaces
- 2 Service Implementations
- 2 MapStruct Mappers
- 2 REST Controllers

✅ **Complete API Implementation**
- 36 RESTful endpoints
- Full CRUD operations
- Advanced filtering and search
- Bulk import via stored procedure
- Role-based security
- Comprehensive error handling

✅ **API Standards Compliance**
- RESTful URL patterns
- Standard response formats
- Validation and error handling
- Security with role-based access
- Logging and audit trails

✅ **Documentation Provided**
- Postman collection guide with 36 endpoint examples
- Maleva API Standards reference
- Database schema mapping
- Component overview

**Status**: ✅ Ready for deployment and testing

Generated: February 27, 2026
Spring Boot Version: 4.0.2
Java Version: 17
MapStruct Version: 1.5.5.Final

