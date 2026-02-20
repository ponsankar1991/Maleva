# AgentCompanyMaster API - Quick Reference Guide

## Base URL
```
http://localhost:8082/api/agent-companies
```

## Authentication
All endpoints require JWT token in Authorization header:
```
Authorization: Bearer {jwt_token}
```

Required Roles: `ROLE_ADMIN` or `ROLE_SUPRERADMIN`

---

## Endpoint Summary

### 1. LIST ALL ACTIVE AGENT COMPANIES
```
GET /api/agent-companies
```
**Returns:** List of all agent companies where Active != 2

**Example Response:**
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
    }
  ]
}
```

---

### 2. GET AGENT COMPANY BY ID
```
GET /api/agent-companies/{id}
```
**Path Parameters:**
- `id` - Agent Company ID

**Example:**
```
GET /api/agent-companies/1
```

**Returns:** Single agent company details or 404

---

### 3. GET BY COMPANY REFERENCE ID
```
GET /api/agent-companies/company/{companyRefId}
```
**Path Parameters:**
- `companyRefId` - Company Reference ID

**Example:**
```
GET /api/agent-companies/company/5
```

**Returns:** All agent companies for a specific company (Active != 2)

---

### 4. CREATE NEW AGENT COMPANY
```
POST /api/agent-companies
```
**Request Body:**
```json
{
  "companyRefId": 5,
  "name": "New Agent Company",
  "dFlag": 0,
  "active": 1
}
```

**Returns:** 201 Created with created record details

**Status Codes:**
- 201 Created
- 400 Bad Request (invalid input)

---

### 5. UPDATE AGENT COMPANY
```
PUT /api/agent-companies/{id}
```
**Path Parameters:**
- `id` - Agent Company ID

**Request Body:**
```json
{
  "companyRefId": 5,
  "name": "Updated Name",
  "dFlag": 0,
  "active": 1
}
```

**Returns:** 200 OK with updated record or 404 Not Found

---

### 6. DELETE (SOFT DELETE) AGENT COMPANY
```
DELETE /api/agent-companies/{id}
```
**Path Parameters:**
- `id` - Agent Company ID

**Returns:** 204 No Content (soft delete sets Active = 2)

---

### 7. BULK UPSERT AGENT COMPANIES ⭐ (SP_AgentCompany Logic)
```
POST /api/agent-companies/upsert?companyRefId={companyRefId}
```
**Query Parameters:**
- `companyRefId` - Company Reference ID (required, must be > 0)

**Request Body:** Array of agent companies
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

**Logic:**
- For each record in the array:
  - Check if exists with: CompanyRefId + Name + Active=1
  - If exists: UPDATE the record (preserve ID)
  - If not exists: INSERT new record (get new ID)
- All records processed in single transaction

**Example Request:**
```bash
curl -X POST "http://localhost:8082/api/agent-companies/upsert?companyRefId=5" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '[
    {"name": "Company A", "dFlag": 0, "active": 1},
    {"name": "Company B", "dFlag": 0, "active": 1}
  ]'
```

**Returns:** 201 Created with list of created/updated records

---

### 8. SEARCH AGENT COMPANIES
```
POST /api/agent-companies/search?companyRefId={companyRefId}
```
**Query Parameters:**
- `companyRefId` - Company Reference ID (required, must be > 0)

**Returns:** List of agent companies for the specified company (Active != 2)

---

## HTTP Status Codes Reference

| Code | Meaning | When Used |
|------|---------|-----------|
| 200 | OK | GET, PUT successful |
| 201 | Created | POST successful (create/upsert) |
| 204 | No Content | DELETE successful or empty list |
| 400 | Bad Request | Invalid input, validation error |
| 401 | Unauthorized | Missing/invalid JWT token |
| 403 | Forbidden | Insufficient role permissions |
| 404 | Not Found | Resource doesn't exist |
| 500 | Server Error | Unexpected exception |

---

## Request/Response DTO Structure

### Request DTO (for POST/PUT)
```json
{
  "companyRefId": 5,           // Required: Foreign key
  "name": "Agent Company",      // Required: Max 100 chars
  "dFlag": 0,                   // Optional: Deletion flag
  "active": 1                   // Optional: 1=active, 2=deleted
}
```

### Response DTO (from GET/POST/PUT)
```json
{
  "id": 1,                                          // Auto-generated
  "companyRefId": 5,                                // From request
  "name": "Agent Company",                          // From request
  "dFlag": 0,                                       // From request
  "createdDate": "2026-02-15T10:30:00",            // Auto-managed
  "modifiedDate": "2026-02-19T14:00:00",           // Auto-managed
  "modifiedBy": "ADMIN",                            // Auto-managed
  "active": 1                                       // From request
}
```

---

## CURL Examples

### Example 1: Get all agent companies
```bash
curl -X GET "http://localhost:8082/api/agent-companies" \
  -H "Authorization: Bearer {jwt_token}"
```

### Example 2: Create new agent company
```bash
curl -X POST "http://localhost:8082/api/agent-companies" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {jwt_token}" \
  -d '{
    "companyRefId": 5,
    "name": "ABC Logistics",
    "dFlag": 0,
    "active": 1
  }'
```

### Example 3: Get agent companies for company ID 5
```bash
curl -X GET "http://localhost:8082/api/agent-companies/company/5" \
  -H "Authorization: Bearer {jwt_token}"
```

### Example 4: Update agent company
```bash
curl -X PUT "http://localhost:8082/api/agent-companies/1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {jwt_token}" \
  -d '{
    "companyRefId": 5,
    "name": "Updated Company Name",
    "dFlag": 0,
    "active": 1
  }'
```

### Example 5: Delete agent company
```bash
curl -X DELETE "http://localhost:8082/api/agent-companies/1" \
  -H "Authorization: Bearer {jwt_token}"
```

### Example 6: Bulk upsert agent companies (SP_AgentCompany logic)
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

### Example 7: Search agent companies
```bash
curl -X POST "http://localhost:8082/api/agent-companies/search?companyRefId=5" \
  -H "Authorization: Bearer {jwt_token}"
```

---

## Common Errors and Solutions

### Error: "CompanyRefId must be a valid positive integer"
**Cause:** CompanyRefId is null, 0, or negative
**Solution:** Provide valid CompanyRefId > 0

### Error: "Name is required"
**Cause:** Name field is null or empty string
**Solution:** Provide non-empty name (max 100 characters)

### Error: "Agent Company not found: {id}"
**Cause:** Record with given ID doesn't exist
**Solution:** Verify ID is correct, check if record was deleted

### Error: "Access Denied" (403)
**Cause:** User doesn't have required role
**Solution:** Ensure user has ROLE_ADMIN or ROLE_SUPRERADMIN

### Error: "Unauthorized" (401)
**Cause:** JWT token missing or invalid
**Solution:** Provide valid JWT token in Authorization header

---

## Key Features

✅ **Full CRUD Operations** - Create, Read, Update, Delete
✅ **Bulk Upsert** - Process multiple records with SP_AgentCompany logic
✅ **Smart Duplicate Handling** - Check CompanyRefId + Name + Active=1
✅ **Soft Deletes** - Records marked as deleted (Active=2) not removed
✅ **Audit Trail** - CreatedDate, ModifiedDate, ModifiedBy auto-managed
✅ **Security** - JWT authentication + Role-based authorization
✅ **Validation** - Input validation on all endpoints
✅ **Error Handling** - Proper error responses with meaningful messages

---

## Database Table Structure

```sql
CREATE TABLE [dbo].[AgentCompanyMaster](
    [Id] INT IDENTITY(1,1) PRIMARY KEY,
    [CompanyRefId] INT NOT NULL,
    [Name] NVARCHAR(100) NOT NULL,
    [DFlag] INT NOT NULL DEFAULT 0,
    [Created_Date] DATETIME NOT NULL DEFAULT GETDATE(),
    [Modified_Date] DATETIME NOT NULL DEFAULT GETDATE(),
    [Modified_By] VARCHAR(50) NOT NULL DEFAULT SUSER_NAME(),
    [Active] INT NOT NULL DEFAULT 1,
    FOREIGN KEY ([CompanyRefId]) REFERENCES [Company]([Id])
)
```

---

## Component Architecture

```
Controller (REST Endpoints)
    ↓
Service (Business Logic + SP_AgentCompany Logic)
    ↓
Repository (Spring Data JPA)
    ↓
Entity (JPA Mapping)
    ↓
Database (SQL Server)

Mapper (MapStruct) ↔ DTO Conversions
```

---

## Notes

1. **CompanyRefId from Frontend:** The CompanyRefId is passed as a query parameter in the upsert endpoint, not in the request body
2. **Active Flag:** Active != 2 filters out soft-deleted records in all list/search operations
3. **Soft Delete:** Deleting doesn't remove the record, just sets Active = 2
4. **Transaction Safety:** Upsert operations are atomic (all-or-nothing)
5. **Duplicate Detection:** Based on CompanyRefId + Name + Active=1 combination

---

## Related Documentation

- **Full API Documentation:** `docs/AGENT_COMPANY_API_DOCUMENTATION.md`
- **Implementation Summary:** `docs/AGENT_COMPANY_IMPLEMENTATION_SUMMARY.md`
- **API Standards:** `docs/API_Standards.md`

---

**Last Updated:** February 19, 2026
**Version:** 1.0.0
**Status:** Production Ready ✅

