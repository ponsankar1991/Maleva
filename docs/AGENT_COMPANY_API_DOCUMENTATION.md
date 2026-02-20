# AgentCompanyMaster API Documentation

## Overview
The AgentCompanyMaster API provides REST endpoints for managing agent company master data. It implements the business logic from the `SP_AgentCompany` stored procedure with additional CRUD operations and search functionality.

## Base URL
```
http://localhost:8082/api/agent-companies
```

## Table of Contents
1. [API Endpoints](#api-endpoints)
2. [Data Models](#data-models)
3. [Response Format](#response-format)
4. [Error Handling](#error-handling)
5. [Authentication & Authorization](#authentication--authorization)
6. [Examples](#examples)

---

## API Endpoints

### 1. Get All Agent Companies
**Endpoint:** `GET /api/agent-companies`

**Description:** Retrieve all active agent companies (Active != 2)

**Authentication:** Required (ROLE_ADMIN or ROLE_SUPRERADMIN)

**Request:**
```
GET /api/agent-companies HTTP/1.1
Host: localhost:8082
Authorization: Bearer {jwt_token}
```

**Response (200 OK):**
```json
{
  "success": true,
  "statusCode": 200,
  "message": "Agent companies retrieved successfully",
  "data": [
    {
      "id": 1,
      "companyRefId": 5,
      "name": "Agent Company A",
      "dFlag": 0,
      "createdDate": "2026-02-15T10:30:00",
      "modifiedDate": "2026-02-15T10:30:00",
      "modifiedBy": "ADMIN",
      "active": 1
    },
    {
      "id": 2,
      "companyRefId": 5,
      "name": "Agent Company B",
      "dFlag": 0,
      "createdDate": "2026-02-15T11:00:00",
      "modifiedDate": "2026-02-15T11:00:00",
      "modifiedBy": "ADMIN",
      "active": 1
    }
  ]
}
```

**Response (204 No Content):**
```json
{
  "success": false,
  "statusCode": 204,
  "message": "No agent companies found",
  "data": null
}
```

---

### 2. Get Agent Company by ID
**Endpoint:** `GET /api/agent-companies/{id}`

**Description:** Retrieve a specific agent company by ID

**Authentication:** Required (ROLE_ADMIN or ROLE_SUPRERADMIN)

**Parameters:**
- `id` (path) - Agent company ID (Long)

**Request:**
```
GET /api/agent-companies/1 HTTP/1.1
Host: localhost:8082
Authorization: Bearer {jwt_token}
```

**Response (200 OK):**
```json
{
  "success": true,
  "statusCode": 200,
  "message": "Agent company retrieved successfully",
  "data": {
    "id": 1,
    "companyRefId": 5,
    "name": "Agent Company A",
    "dFlag": 0,
    "createdDate": "2026-02-15T10:30:00",
    "modifiedDate": "2026-02-15T10:30:00",
    "modifiedBy": "ADMIN",
    "active": 1
  }
}
```

**Response (404 Not Found):**
```json
{
  "success": false,
  "statusCode": 404,
  "message": "Agent Company not found: 999",
  "data": null
}
```

---

### 3. Get Agent Companies by CompanyRefId
**Endpoint:** `GET /api/agent-companies/company/{companyRefId}`

**Description:** Retrieve all agent companies for a specific company reference ID (Active != 2)

**Authentication:** Required (ROLE_ADMIN or ROLE_SUPRERADMIN)

**Parameters:**
- `companyRefId` (path) - Company reference ID (Integer)

**Request:**
```
GET /api/agent-companies/company/5 HTTP/1.1
Host: localhost:8082
Authorization: Bearer {jwt_token}
```

**Response (200 OK):**
```json
{
  "success": true,
  "statusCode": 200,
  "message": "Agent companies for company retrieved successfully",
  "data": [
    {
      "id": 1,
      "companyRefId": 5,
      "name": "Agent Company A",
      "dFlag": 0,
      "createdDate": "2026-02-15T10:30:00",
      "modifiedDate": "2026-02-15T10:30:00",
      "modifiedBy": "ADMIN",
      "active": 1
    }
  ]
}
```

---

### 4. Create Agent Company
**Endpoint:** `POST /api/agent-companies`

**Description:** Create a new agent company

**Authentication:** Required (ROLE_ADMIN or ROLE_SUPRERADMIN)

**Request Body:**
```json
{
  "companyRefId": 5,
  "name": "New Agent Company",
  "dFlag": 0,
  "active": 1
}
```

**Request:**
```
POST /api/agent-companies HTTP/1.1
Host: localhost:8082
Content-Type: application/json
Authorization: Bearer {jwt_token}

{
  "companyRefId": 5,
  "name": "New Agent Company",
  "dFlag": 0,
  "active": 1
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "statusCode": 201,
  "message": "Agent company created successfully",
  "data": {
    "id": 3,
    "companyRefId": 5,
    "name": "New Agent Company",
    "dFlag": 0,
    "createdDate": "2026-02-19T14:00:00",
    "modifiedDate": "2026-02-19T14:00:00",
    "modifiedBy": "ADMIN",
    "active": 1
  }
}
```

**Response (400 Bad Request):**
```json
{
  "success": false,
  "statusCode": 400,
  "message": "CompanyRefId must be a valid positive integer",
  "data": null
}
```

---

### 5. Update Agent Company
**Endpoint:** `PUT /api/agent-companies/{id}`

**Description:** Update an existing agent company

**Authentication:** Required (ROLE_ADMIN or ROLE_SUPRERADMIN)

**Parameters:**
- `id` (path) - Agent company ID (Long)

**Request Body:**
```json
{
  "companyRefId": 5,
  "name": "Updated Agent Company",
  "dFlag": 0,
  "active": 1
}
```

**Request:**
```
PUT /api/agent-companies/1 HTTP/1.1
Host: localhost:8082
Content-Type: application/json
Authorization: Bearer {jwt_token}

{
  "companyRefId": 5,
  "name": "Updated Agent Company",
  "dFlag": 0,
  "active": 1
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "statusCode": 200,
  "message": "Agent company updated successfully",
  "data": {
    "id": 1,
    "companyRefId": 5,
    "name": "Updated Agent Company",
    "dFlag": 0,
    "createdDate": "2026-02-15T10:30:00",
    "modifiedDate": "2026-02-19T14:15:00",
    "modifiedBy": "ADMIN",
    "active": 1
  }
}
```

---

### 6. Delete Agent Company
**Endpoint:** `DELETE /api/agent-companies/{id}`

**Description:** Delete (soft delete) an agent company by setting Active = 2

**Authentication:** Required (ROLE_ADMIN or ROLE_SUPRERADMIN)

**Parameters:**
- `id` (path) - Agent company ID (Long)

**Request:**
```
DELETE /api/agent-companies/1 HTTP/1.1
Host: localhost:8082
Authorization: Bearer {jwt_token}
```

**Response (204 No Content):**
```
HTTP/1.1 204 No Content
```

**Response (404 Not Found):**
```json
{
  "success": false,
  "statusCode": 404,
  "message": "Agent Company not found: 999",
  "data": null
}
```

---

### 7. Bulk Upsert Agent Companies
**Endpoint:** `POST /api/agent-companies/upsert`

**Description:** Bulk upsert agent companies (implements SP_AgentCompany logic)

For each record:
- If exists with CompanyRefId + Name + Active=1: UPDATE the record
- Otherwise: INSERT new record

**Authentication:** Required (ROLE_ADMIN or ROLE_SUPRERADMIN)

**Query Parameters:**
- `companyRefId` (required) - Company reference ID (Integer, must be > 0)

**Request Body:**
```json
[
  {
    "name": "Agent Company 1",
    "dFlag": 0,
    "active": 1
  },
  {
    "name": "Agent Company 2",
    "dFlag": 0,
    "active": 1
  }
]
```

**Request:**
```
POST /api/agent-companies/upsert?companyRefId=5 HTTP/1.1
Host: localhost:8082
Content-Type: application/json
Authorization: Bearer {jwt_token}

[
  {
    "name": "Agent Company 1",
    "dFlag": 0,
    "active": 1
  },
  {
    "name": "Agent Company 2",
    "dFlag": 0,
    "active": 1
  }
]
```

**Response (201 Created):**
```json
{
  "success": true,
  "statusCode": 201,
  "message": "Agent companies upserted successfully",
  "data": [
    {
      "id": 4,
      "companyRefId": 5,
      "name": "Agent Company 1",
      "dFlag": 0,
      "createdDate": "2026-02-19T14:20:00",
      "modifiedDate": "2026-02-19T14:20:00",
      "modifiedBy": "SYSTEM",
      "active": 1
    },
    {
      "id": 5,
      "companyRefId": 5,
      "name": "Agent Company 2",
      "dFlag": 0,
      "createdDate": "2026-02-19T14:20:00",
      "modifiedDate": "2026-02-19T14:20:00",
      "modifiedBy": "SYSTEM",
      "active": 1
    }
  ]
}
```

**Response (400 Bad Request):**
```json
{
  "success": false,
  "statusCode": 400,
  "message": "CompanyRefId must be provided and must be a valid positive integer",
  "data": null
}
```

---

### 8. Search Agent Companies
**Endpoint:** `POST /api/agent-companies/search`

**Description:** Search agent companies by company reference ID

**Authentication:** Required (ROLE_ADMIN or ROLE_SUPRERADMIN)

**Query Parameters:**
- `companyRefId` (required) - Company reference ID (Integer, must be > 0)

**Request:**
```
POST /api/agent-companies/search?companyRefId=5 HTTP/1.1
Host: localhost:8082
Authorization: Bearer {jwt_token}
```

**Response (200 OK):**
```json
{
  "success": true,
  "statusCode": 200,
  "message": "Agent companies search completed successfully",
  "data": [
    {
      "id": 1,
      "companyRefId": 5,
      "name": "Agent Company A",
      "dFlag": 0,
      "createdDate": "2026-02-15T10:30:00",
      "modifiedDate": "2026-02-15T10:30:00",
      "modifiedBy": "ADMIN",
      "active": 1
    }
  ]
}
```

---

## Data Models

### AgentCompanyMasterDTO
```json
{
  "id": 1,
  "companyRefId": 5,
  "name": "Agent Company A",
  "dFlag": 0,
  "createdDate": "2026-02-15T10:30:00",
  "modifiedDate": "2026-02-15T10:30:00",
  "modifiedBy": "ADMIN",
  "active": 1
}
```

**Fields:**
- `id` (Long) - Primary key, auto-generated
- `companyRefId` (Integer) - Foreign key reference to Company table (required)
- `name` (String, max 100) - Agent company name (required)
- `dFlag` (Integer) - Deletion flag (default: 0)
- `createdDate` (LocalDateTime) - Record creation timestamp (auto-managed)
- `modifiedDate` (LocalDateTime) - Record modification timestamp (auto-managed)
- `modifiedBy` (String, max 50) - User who last modified the record (auto-managed)
- `active` (Integer) - Status flag: 1=active, 2=deleted/inactive (default: 1)

### AgentCompanyRequestDTO
```json
{
  "companyRefId": 5,
  "name": "Agent Company A",
  "dFlag": 0,
  "active": 1
}
```

Used for CREATE and UPDATE operations. Excludes auto-managed fields.

### AgentCompanyResponseDTO
```json
{
  "id": 1,
  "companyRefId": 5,
  "name": "Agent Company A",
  "dFlag": 0,
  "createdDate": "2026-02-15T10:30:00",
  "modifiedDate": "2026-02-15T10:30:00",
  "modifiedBy": "ADMIN",
  "active": 1
}
```

Used for response payloads. Includes all fields.

---

## Response Format

### Success Response
```json
{
  "success": true,
  "statusCode": 200,
  "message": "Operation successful",
  "data": { /* actual data */ },
  "meta": null
}
```

### Error Response
```json
{
  "success": false,
  "statusCode": 400,
  "message": "Error message",
  "data": null,
  "error": null
}
```

**Response Fields:**
- `success` (Boolean) - True if successful, false otherwise
- `statusCode` (Integer) - HTTP status code
- `message` (String) - Human-readable message
- `data` (Object) - Response data (null on error)
- `meta` (Object) - Metadata (pagination, etc.) - optional
- `error` (Object) - Error details - optional

---

## HTTP Status Codes

| Code | Meaning | Usage |
|------|---------|-------|
| 200 | OK | Successful GET, PUT |
| 201 | Created | Successful POST |
| 204 | No Content | Successful DELETE or empty list |
| 400 | Bad Request | Invalid input data |
| 401 | Unauthorized | Missing/invalid authentication |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource doesn't exist |
| 500 | Server Error | Unhandled exception |

---

## Error Handling

### Validation Errors
```json
{
  "success": false,
  "statusCode": 400,
  "message": "Name is required",
  "data": null
}
```

### Not Found Error
```json
{
  "success": false,
  "statusCode": 404,
  "message": "Agent Company not found: 999",
  "data": null
}
```

### Authorization Error
```json
{
  "success": false,
  "statusCode": 403,
  "message": "Access Denied",
  "data": null
}
```

### Server Error
```json
{
  "success": false,
  "statusCode": 500,
  "message": "An unexpected error occurred",
  "data": null
}
```

---

## Authentication & Authorization

All endpoints require JWT authentication with one of the following roles:
- `ROLE_ADMIN`
- `ROLE_SUPRERADMIN`

**Include the token in the Authorization header:**
```
Authorization: Bearer {jwt_token}
```

---

## Business Logic (from SP_AgentCompany)

### Upsert Logic
The stored procedure `SP_AgentCompany` implements the following logic:

1. For each agent company in the input:
2. Check if a record exists with: `CompanyRefId = @Comid` AND `Name = @Name` AND `Active = 1`
3. If exists: UPDATE the existing record (preserving ID)
4. If not exists: INSERT new record (get new ID from SCOPE_IDENTITY)
5. Return: Id, Name (for new inserts), Result status, and Message

**Key Points:**
- Duplicate checking is based on CompanyRefId + Name + Active status
- The CompanyRefId is provided from the frontend as a query parameter
- Active flag filters out soft-deleted records (Active != 2)
- ModifiedDate and ModifiedBy are automatically set on insert/update

---

## Examples

### Example 1: Create a New Agent Company
```bash
curl -X POST http://localhost:8082/api/agent-companies \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {jwt_token}" \
  -d '{
    "companyRefId": 5,
    "name": "ABC Logistics",
    "dFlag": 0,
    "active": 1
  }'
```

### Example 2: Get Agent Companies for a Specific Company
```bash
curl -X GET http://localhost:8082/api/agent-companies/company/5 \
  -H "Authorization: Bearer {jwt_token}"
```

### Example 3: Bulk Upsert Agent Companies
```bash
curl -X POST "http://localhost:8082/api/agent-companies/upsert?companyRefId=5" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {jwt_token}" \
  -d '[
    {
      "name": "Company A",
      "dFlag": 0,
      "active": 1
    },
    {
      "name": "Company B",
      "dFlag": 0,
      "active": 1
    }
  ]'
```

### Example 4: Update an Agent Company
```bash
curl -X PUT http://localhost:8082/api/agent-companies/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {jwt_token}" \
  -d '{
    "companyRefId": 5,
    "name": "Updated Company Name",
    "dFlag": 0,
    "active": 1
  }'
```

### Example 5: Delete (Soft Delete) an Agent Company
```bash
curl -X DELETE http://localhost:8082/api/agent-companies/1 \
  -H "Authorization: Bearer {jwt_token}"
```

---

## Implementation Details

### Component Structure
```
my.maleva.api.agentcompany/
├── entity/
│   └── AgentCompanyMaster.java          # JPA Entity with Lombok annotations
├── dto/
│   ├── AgentCompanyMasterDTO.java       # Internal DTO
│   ├── AgentCompanyRequestDTO.java      # Request DTO (excludes audit fields)
│   └── AgentCompanyResponseDTO.java     # Response DTO (includes all fields)
├── repository/
│   └── AgentCompanyMasterRepository.java # Spring Data JPA with custom queries
├── service/
│   └── AgentCompanyMasterService.java   # Business logic (includes SP logic)
├── mapper/
│   └── AgentCompanyMasterMapper.java    # MapStruct mapper
└── controller/
    └── AgentCompanyMasterController.java # REST endpoints
```

### Key Technologies
- **Spring Boot 4.0.2** - Framework
- **Spring Data JPA** - Database access
- **MapStruct 1.5.5** - DTO mapping
- **Lombok 1.18.26** - Boilerplate reduction
- **JWT** - Authentication
- **Spring Security** - Authorization

### Database Requirements
- SQL Server 2019+ or compatible
- Table: `AgentCompanyMaster`
- Foreign Key: `CompanyRefId` → `Company.Id`

---

## Version History
- **v1.0.0** (2026-02-19) - Initial release with full CRUD and SP_AgentCompany integration

