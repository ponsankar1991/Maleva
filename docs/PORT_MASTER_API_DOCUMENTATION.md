# Port Master API - Generated Components Documentation

## Overview

This document provides comprehensive documentation for the Port Master API components generated from the SQL table `PortMaster` and stored procedure `SP_PortMaster`.

## Table of Contents
1. [Entity Model](#entity-model)
2. [DTO Class](#dto-class)
3. [Repository Interface](#repository-interface)
4. [Mapper](#mapper)
5. [Service Layer](#service-layer)
6. [Controller](#controller)
7. [API Endpoints](#api-endpoints)
8. [Usage Examples](#usage-examples)

---

## Entity Model

### PortMaster Entity
**Location**: `my.maleva.api.model.PortMaster`

**Database Table**: `PortMaster`

**Description**: JPA entity representing a port master record

**Fields**:
- `id` (Integer): Unique identifier (Auto-generated, Primary Key)
- `companyRefId` (Integer): Reference to company (Foreign Key, NOT NULL)
- `portName` (String): Name of the port (VARCHAR 50, NOT NULL)
- `createdDate` (LocalDateTime): Record creation timestamp (NOT NULL)
- `modifiedDate` (LocalDateTime): Last modification timestamp (NOT NULL)
- `modifiedBy` (String): User who modified the record (VARCHAR 50, NOT NULL)
- `active` (Integer): Active status (1=active, 0=inactive, 2=deleted, NOT NULL)

**Annotations**:
- `@Entity`: JPA entity annotation
- `@Table(name = "PortMaster")`: Map to database table
- `@Data`: Lombok annotation for getter/setter/toString/equals/hashCode
- `@Builder`: Lombok builder pattern support
- `@NoArgsConstructor`: Lombok no-argument constructor
- `@AllArgsConstructor`: Lombok all-arguments constructor

---

## DTO Class

### PortMasterDto
**Location**: `my.maleva.api.dto.PortMasterDto`

**Purpose**: Data Transfer Object for API request/response handling

**Fields with Validations**:
```java
private Integer id;                              // Response only
private Integer companyRefId;                    // @NotNull required
private String portName;                         // @NotBlank, @Size(max=50)
private Integer active;                          // @NotNull required
private LocalDateTime createdDate;               // Response only
private LocalDateTime modifiedDate;              // Response only
private String modifiedBy;                       // @Size(max=50)
```

**Validation Rules**:
- `companyRefId`: Required (@NotNull)
- `portName`: Required (@NotBlank), max 50 characters
- `active`: Required (@NotNull)
- `modifiedBy`: Optional, max 50 characters

---

## Repository Interface

### PortMasterRepository
**Location**: `my.maleva.api.repo.PortMasterRepository`

**Extends**: `JpaRepository<PortMaster, Integer>`

**Query Methods**:

1. `findByCompanyRefIdAndActivNot(companyRefId, active)`
   - Get all non-deleted ports for a company
   - Excludes records with active=2

2. `findByCompanyRefIdAndPortName(companyRefId, portName)`
   - Find port by company and exact port name
   - Returns Optional

3. `findByCompanyRefId(companyRefId)`
   - Get all ports for a company
   - Includes all active statuses

4. `searchByCompanyAndPortName(companyId, portName)`
   - Case-insensitive partial name search
   - JPQL query with LOWER and CONCAT

5. `findByCompanyRefIdAndActive(companyRefId, active)`
   - Get ports by company and active status
   - Useful for filtering

6. `existsByCompanyRefIdAndPortName(companyRefId, portName)`
   - Check if port name exists for company
   - Returns boolean

---

## Mapper

### PortMasterMapper
**Location**: `my.maleva.api.mapper.PortMasterMapper`

**Framework**: MapStruct

**Mappings**:

1. `toDto(PortMaster entity)`: PortMasterDto
   - Convert entity to DTO for API response

2. `toEntity(PortMasterDto dto)`: PortMaster
   - Convert DTO to entity for database persistence

3. `updateFromDto(PortMasterDto dto, PortMaster entity)`: void
   - Update existing entity from DTO
   - Null values are ignored

**Configuration**:
- `componentModel = "spring"`: Spring component
- `nullValuePropertyMappingStrategy = IGNORE`: Don't map null values
- `unmappedTargetPolicy = IGNORE`: Ignore unmapped properties

---

## Service Layer

### PortMasterService
**Location**: `my.maleva.api.service.PortMasterService`

**Implements Business Logic from SP_PortMaster**

#### Methods:

1. **listAll()**: List<PortMasterDto>
   - Get all active port records

2. **getById(id)**: PortMasterDto
   - Get specific port by ID
   - Throws EntityNotFoundException if not found

3. **getByCompany(companyId)**: List<PortMasterDto>
   - Get all ports for a company

4. **getActiveByCompany(companyId)**: List<PortMasterDto>
   - Get only active ports for a company

5. **create(dto)**: PortMasterDto
   - Create new port record
   - **SP_PortMaster Logic Implemented**:
     - Insert into PortMaster table
     - Set company reference
     - Auto-timestamp creation
     - Validate duplicate port name
   - Validates: companyRefId, portName
   - Returns created port with ID

6. **createBatch(companyId, dtos)**: List<PortMasterDto>
   - **Bulk SP_PortMaster Logic**:
     - Process multiple records in one transaction
     - Create new or update existing based on ID
     - Insert new records if ID=0
     - Update existing records if ID>0
   - Validates all records
   - Checks for duplicates
   - Returns all created/updated ports

7. **update(id, dto)**: PortMasterDto
   - **SP_PortMaster Update Logic Implemented**:
     - Update portName and active status
     - Auto-timestamp modification
     - Check for duplicate names
   - Throws EntityNotFoundException if not found

8. **delete(id)**: void
   - Hard delete port record
   - Throws EntityNotFoundException if not found

9. **search(companyId, portName)**: List<PortMasterDto>
   - Search ports by name
   - Case-insensitive
   - Returns all active if name is empty

10. **softDelete(id)**: void
    - Set active=2 instead of deleting

11. **activate(id)**: PortMasterDto
    - Set active=1

12. **deactivate(id)**: PortMasterDto
    - Set active=0

**Transaction Management**:
- All modifying operations use `@Transactional`
- Automatic rollback on exceptions
- ACID compliance

---

## Controller

### PortMasterController
**Location**: `my.maleva.api.controller.PortMasterController`

**Base URL**: `/api/port-masters`

**Security**: 
```java
@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
```

---

## API Endpoints

### CRUD Endpoints

| Method | Endpoint | Description | Status |
|--------|----------|-------------|--------|
| GET | `/api/port-masters` | List all ports | 200 |
| GET | `/api/port-masters/{id}` | Get port by ID | 200 |
| POST | `/api/port-masters` | Create new port | 201 |
| PUT | `/api/port-masters/{id}` | Update port | 200 |
| DELETE | `/api/port-masters/{id}` | Delete port | 204 |

### Batch Operations

| Method | Endpoint | Description | Status |
|--------|----------|-------------|--------|
| POST | `/api/port-masters/batch` | Batch create/update | 201 |

### Query Endpoints

| Method | Endpoint | Description | Status |
|--------|----------|-------------|--------|
| GET | `/api/port-masters/company/{companyId}` | Get by company | 200 |
| GET | `/api/port-masters/company/{companyId}/active` | Get active only | 200 |
| GET | `/api/port-masters/search` | Search by name | 200 |

### Status Endpoints

| Method | Endpoint | Description | Status |
|--------|----------|-------------|--------|
| DELETE | `/api/port-masters/{id}/soft` | Soft delete | 204 |
| POST | `/api/port-masters/{id}/activate` | Activate | 200 |
| POST | `/api/port-masters/{id}/deactivate` | Deactivate | 200 |

**Total Endpoints**: 12

---

## Usage Examples

### 1. Create Port Record

**Request:**
```bash
POST /api/port-masters
Content-Type: application/json

{
  "companyRefId": 1,
  "portName": "Port of Shanghai",
  "active": 1,
  "modifiedBy": "admin"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "companyRefId": 1,
  "portName": "Port of Shanghai",
  "active": 1,
  "createdDate": "2026-02-15T12:00:00",
  "modifiedDate": "2026-02-15T12:00:00",
  "modifiedBy": "admin"
}
```

### 2. Get All Ports

**Request:**
```bash
GET /api/port-masters
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "companyRefId": 1,
    "portName": "Port of Shanghai",
    "active": 1
  },
  {
    "id": 2,
    "companyRefId": 1,
    "portName": "Port of Singapore",
    "active": 1
  }
]
```

### 3. Update Port

**Request:**
```bash
PUT /api/port-masters/1
Content-Type: application/json

{
  "portName": "Port of Shanghai - Updated",
  "active": 1
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "companyRefId": 1,
  "portName": "Port of Shanghai - Updated",
  "active": 1,
  "modifiedDate": "2026-02-15T12:30:00"
}
```

### 4. Batch Create/Update

**Request:**
```bash
POST /api/port-masters/batch?companyId=1
Content-Type: application/json

[
  {
    "id": 0,
    "portName": "New Port 1",
    "active": 1
  },
  {
    "id": 1,
    "portName": "Updated Port 1",
    "active": 1
  }
]
```

**Response (201 Created):**
```json
[
  {
    "id": 3,
    "companyRefId": 1,
    "portName": "New Port 1",
    "active": 1
  },
  {
    "id": 1,
    "companyRefId": 1,
    "portName": "Updated Port 1",
    "active": 1
  }
]
```

### 5. Search Ports

**Request:**
```bash
GET /api/port-masters/search?companyId=1&portName=shanghai
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "companyRefId": 1,
    "portName": "Port of Shanghai",
    "active": 1
  }
]
```

### 6. Get Active Ports

**Request:**
```bash
GET /api/port-masters/company/1/active
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "companyRefId": 1,
    "portName": "Port of Shanghai",
    "active": 1
  }
]
```

### 7. Activate Port

**Request:**
```bash
POST /api/port-masters/1/activate
```

**Response (200 OK):**
```json
{
  "id": 1,
  "active": 1,
  "modifiedDate": "2026-02-15T13:00:00"
}
```

### 8. Delete Port

**Request:**
```bash
DELETE /api/port-masters/1
```

**Response (204 No Content):**
```
[Empty body]
```

---

## Stored Procedure Logic Integration

### SP_PortMaster Features Implemented in Service:

1. **Batch Processing**
   - Process multiple records in single transaction
   - JSON parsing and row iteration (simulated in createBatch)

2. **Insert Logic**
   - Create new record when ID=0
   - Auto-populate company reference
   - Auto-timestamp creation

3. **Update Logic**
   - Update existing record when ID>0
   - Preserve creation data
   - Update modification timestamp

4. **Transaction Management**
   - All operations in transaction
   - Rollback on error
   - ACID compliance

5. **Validation**
   - Company reference validation
   - Duplicate port name check
   - Active status validation

6. **Error Handling**
   - Clear error messages
   - Proper exception types
   - Transaction rollback

---

## File Structure

```
src/main/java/my/maleva/api/
├── model/
│   └── PortMaster.java                  [Entity]
├── dto/
│   └── PortMasterDto.java               [DTO]
├── repo/
│   └── PortMasterRepository.java        [Repository]
├── mapper/
│   └── PortMasterMapper.java            [Mapper]
├── service/
│   └── PortMasterService.java           [Service - Business Logic]
└── controller/
    └── PortMasterController.java        [Controller - REST API]
```

---

## Security

All endpoints require one of the following roles:
- `ROLE_SUPRERADMIN`
- `ROLE_ADMIN`
- `ROLE_100`

---

## Response Codes

| Code | Meaning | Use Case |
|------|---------|----------|
| 200 | OK | GET, PUT successful |
| 201 | Created | POST successful |
| 204 | No Content | DELETE successful |
| 400 | Bad Request | Validation error |
| 404 | Not Found | Resource not found |
| 500 | Server Error | Unhandled exception |

---

## Standards Compliance

✅ RESTful design principles
✅ Proper HTTP methods
✅ Consistent naming conventions
✅ DTO-based request/response
✅ MapStruct automatic mapping
✅ Service layer separation
✅ JPA repositories abstraction
✅ Transaction management
✅ Security with @PreAuthorize
✅ Input validation
✅ Error handling

---

## Database Schema

```sql
CREATE TABLE [dbo].[PortMaster](
    [Id] [int] IDENTITY(1,1) PRIMARY KEY,
    [CompanyRefId] [int] NOT NULL FOREIGN KEY,
    [PortName] [varchar](50) NOT NULL,
    [Created_Date] [datetime] NOT NULL DEFAULT GETDATE(),
    [Modified_Date] [datetime] NOT NULL DEFAULT GETDATE(),
    [Modified_By] [varchar](50) NOT NULL DEFAULT SUSER_NAME(),
    [Active] [int] NOT NULL DEFAULT 1
)
```


