# Planning Management API - Generated Components Documentation

## Overview

This document provides comprehensive documentation for the Planning Management API components generated from the SQL tables `PLANINGMaster` and `PLANINGDetails`.

## Generated Components

### 1. Entity Models

#### PlanningMaster Entity
- **Location**: `my.maleva.api.model.PlanningMaster`
- **Database Table**: `PLANINGMaster`
- **Description**: Main planning record entity that contains master planning information
- **Key Fields**:
  - `id`: Unique identifier (Auto-generated)
  - `companyRefId`: Reference to the company
  - `userRefId`: Reference to the user (optional)
  - `employeeRefId`: Reference to the employee
  - `saleDate`: Date of the planning
  - `fDate`: From date for planning period
  - `tDate`: To date for planning period
  - `cNumberDisplay`: Display reference number
  - `cNumber`: Numeric reference number
  - `remarks`: Planning remarks
  - `search`: Search field
  - `active`: Active status (0: inactive, 1: active, 2: deleted)
  - `createdDate`: Record creation timestamp
  - `createdBy`: User who created the record
  - `modifiedDate`: Last modification timestamp
  - `modifiedBy`: User who modified the record

#### PlanningDetails Entity
- **Location**: `my.maleva.api.model.PlanningDetails`
- **Database Table**: `PLANINGDetails`
- **Description**: Child details for planning master records
- **Key Fields**:
  - `id`: Unique identifier (Auto-generated)
  - `planningMasterRefId`: Reference to PlanningMaster (Foreign Key)
  - `saleOrderMasterRefId`: Reference to SaleOrderMaster
  - `truckRefId`: Reference to truck (optional)
  - `remarks`: Details remarks
  - `originD`: Origin location
  - `destinationD`: Destination location
  - `pickupDateD`: Pickup date
  - `deliveryDateD`: Delivery date
  - `sortBy`: Sort order
  - `truckNameD`: Truck name
  - `driverNameD`: Driver name
  - `pickupTimeList`: Pickup time list (JSON format)
  - `pickupQuantityList`: Pickup quantity list (JSON format)
  - `deliveryQuantityList`: Delivery quantity list (JSON format)
  - `deliveryTimeList`: Delivery time list (JSON format)

### 2. DTOs (Data Transfer Objects)

#### PlanningMasterDto
- **Location**: `my.maleva.api.dto.PlanningMasterDto`
- **Purpose**: API layer DTO for PlanningMaster
- **Contains**: All fields from PlanningMaster with validation annotations
- **Validation Rules**:
  - `companyRefId`: Required (@NotNull)
  - `saleDate`: Required (@NotNull)
  - `fDate`: Required (@NotNull)
  - `tDate`: Required (@NotNull)
  - `cNumberDisplay`: Required, max 300 characters
  - `cNumber`: Required
  - `active`: Required

#### PlanningDetailsDto
- **Location**: `my.maleva.api.dto.PlanningDetailsDto`
- **Purpose**: API layer DTO for PlanningDetails
- **Contains**: All fields from PlanningDetails with validation annotations
- **Validation Rules**:
  - `planningMasterRefId`: Required (@NotNull)
  - `saleOrderMasterRefId`: Required (@NotNull)
  - `sortBy`: Required (@NotNull)

### 3. Repositories

#### PlanningMasterRepository
- **Location**: `my.maleva.api.repo.PlanningMasterRepository`
- **Extends**: `JpaRepository<PlanningMaster, Integer>`
- **Key Methods**:
  - `findByCompanyRefIdAndActivNot(companyRefId, active)`: Get all active records for a company
  - `findByCompanyRefIdAndEmployeeRefIdAndActivNot(...)`: Filter by company and employee
  - `findByCompanyAndDateRange(...)`: Find records within date range
  - `findByCompanyRefIdAndCNumber(...)`: Find by sequence number
  - `findByCompanyRefIdAndCNumberDisplay(...)`: Find by display number
  - `searchByCompanyAndKeyword(...)`: Full-text search functionality

#### PlanningDetailsRepository
- **Location**: `my.maleva.api.repo.PlanningDetailsRepository`
- **Extends**: `JpaRepository<PlanningDetails, Integer>`
- **Key Methods**:
  - `findByPlanningMasterRefId(...)`: Get all details for a master
  - `findBySaleOrderMasterRefId(...)`: Get details by sale order
  - `findByTruckRefId(...)`: Get details by truck
  - `findByPlanningMasterRefIdSorted(...)`: Get sorted details for a master
  - `deleteByPlanningMasterRefId(...)`: Cascade delete operation

### 4. Mappers (MapStruct)

#### PlanningMasterMapper
- **Location**: `my.maleva.api.mapper.PlanningMasterMapper`
- **Interface**: MapStruct Mapper
- **Key Methods**:
  - `toDto(PlanningMaster)`: Convert entity to DTO
  - `toEntity(PlanningMasterDto)`: Convert DTO to entity
  - `updateFromDto(dto, entity)`: Update entity from DTO

#### PlanningDetailsMapper
- **Location**: `my.maleva.api.mapper.PlanningDetailsMapper`
- **Interface**: MapStruct Mapper
- **Key Methods**:
  - `toDto(PlanningDetails)`: Convert entity to DTO
  - `toEntity(PlanningDetailsDto)`: Convert DTO to entity
  - `updateFromDto(dto, entity)`: Update entity from DTO

### 5. Services

#### PlanningMasterService
- **Location**: `my.maleva.api.service.PlanningMasterService`
- **Description**: Business logic for PlanningMaster operations
- **Key Methods**:
  - `listAll()`: Get all active planning records
  - `getById(id)`: Get planning record by ID with related details
  - `create(dto)`: Create new planning with details (SP_PLANINGMaster logic)
  - `update(id, dto)`: Update planning and re-create details
  - `delete(id)`: Delete planning and cascade delete details
  - `getByCompanyAndDateRange(...)`: Get records within date range
  - `search(companyId, keyword)`: Search planning records
  - `getByCompanyAndEmployee(...)`: Get records by company and employee

**Stored Procedure Logic Incorporated**:
- Master record creation/update with validation
- Detail records management (insert/update/delete)
- Active status handling (values: 0=inactive, 1=active, 2=deleted)
- Automatic timestamp management
- Cascade operations on master record modification

#### PlanningDetailsService
- **Location**: `my.maleva.api.service.PlanningDetailsService`
- **Description**: Business logic for PlanningDetails operations
- **Key Methods**:
  - `listAll()`: Get all planning details
  - `getById(id)`: Get detail by ID
  - `create(dto)`: Create new detail record
  - `update(id, dto)`: Update detail record
  - `delete(id)`: Delete detail record
  - `getByPlanningMasterId(...)`: Get all details for a master
  - `getBySaleOrderMasterId(...)`: Get details by sale order
  - `getByTruckId(...)`: Get details by truck
  - `deleteByPlanningMasterId(...)`: Cascade delete

### 6. Controllers (REST API)

#### PlanningMasterController
- **Location**: `my.maleva.api.controller.PlanningMasterController`
- **Base URL**: `/api/planning-masters`
- **Security**: `@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")`

**Endpoints**:

| HTTP Method | Endpoint | Description |
|------------|----------|-------------|
| GET | `/api/planning-masters` | Get all planning records |
| GET | `/api/planning-masters/{id}` | Get planning by ID |
| POST | `/api/planning-masters` | Create new planning |
| PUT | `/api/planning-masters/{id}` | Update planning |
| DELETE | `/api/planning-masters/{id}` | Delete planning |
| GET | `/api/planning-masters/search/date-range` | Search by date range |
| GET | `/api/planning-masters/search` | Search by keyword |
| GET | `/api/planning-masters/employee/{employeeId}` | Get by employee |

#### PlanningDetailsController
- **Location**: `my.maleva.api.controller.PlanningDetailsController`
- **Base URL**: `/api/planning-details`
- **Security**: Same as PlanningMasterController

**Endpoints**:

| HTTP Method | Endpoint | Description |
|------------|----------|-------------|
| GET | `/api/planning-details` | Get all details |
| GET | `/api/planning-details/{id}` | Get detail by ID |
| POST | `/api/planning-details` | Create new detail |
| PUT | `/api/planning-details/{id}` | Update detail |
| DELETE | `/api/planning-details/{id}` | Delete detail |
| GET | `/api/planning-details/by-master/{masterRefId}` | Get by master ID |
| GET | `/api/planning-details/by-sale-order/{saleOrderMasterId}` | Get by sale order |
| GET | `/api/planning-details/by-truck/{truckRefId}` | Get by truck |
| DELETE | `/api/planning-details/by-master/{masterRefId}` | Cascade delete |

## API Usage Examples

### 1. Create Planning with Details

```bash
POST /api/planning-masters
Content-Type: application/json

{
  "companyRefId": 1,
  "userRefId": 1,
  "employeeRefId": 2,
  "saleDate": "2026-02-15T10:00:00",
  "fDate": "2026-02-15T00:00:00",
  "tDate": "2026-02-28T23:59:59",
  "cNumberDisplay": "PL000000001",
  "cNumber": 1,
  "remarks": "Weekly planning",
  "search": "transportation",
  "active": 1,
  "createdBy": "admin",
  "modifiedBy": "admin",
  "planningDetails": [
    {
      "saleOrderMasterRefId": 10,
      "truckRefId": 5,
      "remarks": "First delivery",
      "originD": "Warehouse A",
      "destinationD": "Store B",
      "pickupDateD": "2026-02-15T08:00:00",
      "deliveryDateD": "2026-02-15T18:00:00",
      "sortBy": 1,
      "truckNameD": "Truck-001",
      "driverNameD": "John Doe"
    }
  ]
}
```

### 2. Update Planning

```bash
PUT /api/planning-masters/1
Content-Type: application/json

{
  "companyRefId": 1,
  "fDate": "2026-02-16T00:00:00",
  "tDate": "2026-02-28T23:59:59",
  "remarks": "Updated planning",
  "active": 1,
  "modifiedBy": "admin",
  "planningDetails": [
    {
      "saleOrderMasterRefId": 10,
      "truckRefId": 6,
      "sortBy": 1
    }
  ]
}
```

### 3. Search Planning by Date Range

```bash
GET /api/planning-masters/search/date-range?companyId=1&fromDate=2026-02-01T00:00:00&toDate=2026-02-28T23:59:59
```

### 4. Search Planning by Keyword

```bash
GET /api/planning-masters/search?companyId=1&keyword=transportation
```

## Transaction Management

All service methods that modify data are marked with `@Transactional` annotation to ensure:
- ACID compliance
- Automatic rollback on errors
- Proper cascade operations
- Data consistency

## Error Handling

- **EntityNotFoundException**: Thrown when record not found (HTTP 404)
- **InvalidRequestException**: Thrown for invalid input (HTTP 400)
- Errors are handled by global exception handler returning structured error responses

## Project Structure

```
my.maleva.api/
├── model/
│   ├── PlanningMaster.java
│   └── PlanningDetails.java
├── dto/
│   ├── PlanningMasterDto.java
│   └── PlanningDetailsDto.java
├── repo/
│   ├── PlanningMasterRepository.java
│   └── PlanningDetailsRepository.java
├── mapper/
│   ├── PlanningMasterMapper.java
│   └── PlanningDetailsMapper.java
├── service/
│   ├── PlanningMasterService.java
│   └── PlanningDetailsService.java
└── controller/
    ├── PlanningMasterController.java
    └── PlanningDetailsController.java
```

## Dependencies

- Spring Data JPA
- Spring Web MVC
- Spring Security
- Lombok
- MapStruct
- Jakarta Persistence API
- Jakarta Validation API

## Standards Compliance

All generated components follow the API standards defined in `/docs/API_Standards.md`:

- ✅ RESTful endpoint conventions (plural resource names, lowercase)
- ✅ Proper HTTP status codes (201 for creation, 204 for deletion, etc.)
- ✅ DTO-based request/response handling
- ✅ MapStruct for entity-DTO mapping
- ✅ Service layer business logic
- ✅ JPA repositories for data access
- ✅ Proper error handling with custom exceptions
- ✅ Role-based access control with @PreAuthorize
- ✅ Transaction management with @Transactional

## Database Schema Relationships

```
PLANINGMaster (1)
    |
    └─── (N) PLANINGDetails
    
Foreign Keys:
- PLANINGDetails.PLANINGMasterRefId → PLANINGMaster.Id (CASCADE)
- PLANINGDetails.SaleOrderMasterRefId → SaleOrderMaster.Id
- PLANINGDetails.TruckRefid → TruckMaster.Id
```

## Testing Recommendations

1. **Unit Tests**: Test service layer methods with mocked repositories
2. **Integration Tests**: Test controller endpoints with actual database
3. **API Tests**: Use Postman collection for manual testing
4. **Business Logic Tests**: Verify cascade operations and data consistency

## Future Enhancements

1. Add pagination support for list endpoints
2. Implement filtering capabilities
3. Add batch operations endpoint
4. Add export functionality (CSV, PDF)
5. Implement audit logging
6. Add API documentation with Swagger/OpenAPI


