# Pre-Alert API - Postman Collection Guide

## Overview
This document provides a comprehensive guide to testing the Pre-Alert API endpoints using Postman. All endpoints follow the API standards defined in `API_Standards.md`.

## Base URL
```
http://localhost:8082/api
```

## Authentication
All endpoints require authentication with the following role-based access control:
- `ROLE_ADMIN`
- `ROLE_SUPRERADMIN`

Include Bearer token in the Authorization header:
```
Authorization: Bearer {token}
```

---

## PreAlertMaster Endpoints

### 1. Get All PreAlertMaster Records by Company
**GET** `/api/pre-alert-masters/company/{companyRefId}`

**Description:** Retrieve all PreAlertMaster records for a specific company.

**Path Parameters:**
- `companyRefId` (Integer, required): Company reference ID

**Example Request:**
```
GET /api/pre-alert-masters/company/1
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "companyRefId": 1,
    "customerMasterRefId": 101,
    "jobTypeMasterRefId": 5,
    "fromDate": "2026-02-01",
    "toDate": "2026-02-28",
    "port": "Chennai",
    "vessel": "MV Emerald",
    "oeta": "2026-02-15",
    "leta": "2026-02-16",
    "alleta": "2026-02-17",
    "none": "N/A",
    "chkPort": "Y",
    "chkVessel": "Y",
    "chkPickupDate": "N",
    "chkConsolidated": "Y",
    "chkDeliveryDone": "N",
    "active": 1,
    "createdDate": "2026-02-15T10:30:00",
    "modifiedDate": "2026-02-15T10:30:00",
    "cNumber": 1,
    "cNumberDisplay": "PA0001/2026",
    "entryDate": "2026-02-15",
    "saleOrderMasterRefId": 201
  }
]
```

---

### 2. Get Active PreAlertMaster Records
**GET** `/api/pre-alert-masters/company/{companyRefId}/active`

**Description:** Retrieve only active PreAlertMaster records.

**Path Parameters:**
- `companyRefId` (Integer, required): Company reference ID

**Example Request:**
```
GET /api/pre-alert-masters/company/1/active
Authorization: Bearer {token}
```

**Response (200 OK):** List of active PreAlertMaster records (same format as above)

---

### 3. Get PreAlertMaster by ID
**GET** `/api/pre-alert-masters/{id}`

**Description:** Retrieve a specific PreAlertMaster record by ID.

**Path Parameters:**
- `id` (Integer, required): PreAlertMaster ID

**Example Request:**
```
GET /api/pre-alert-masters/1
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "companyRefId": 1,
  "customerMasterRefId": 101,
  "jobTypeMasterRefId": 5,
  "port": "Chennai",
  "vessel": "MV Emerald",
  "cNumberDisplay": "PA0001/2026",
  "active": 1,
  "createdDate": "2026-02-15T10:30:00",
  "modifiedDate": "2026-02-15T10:30:00"
}
```

**Response (404 Not Found):**
```json
"PreAlertMaster not found with ID: 999"
```

---

### 4. Create New PreAlertMaster Record
**POST** `/api/pre-alert-masters`

**Description:** Create a new PreAlertMaster record.

**Request Body:**
```json
{
  "companyRefId": 1,
  "customerMasterRefId": 101,
  "jobTypeMasterRefId": 5,
  "fromDate": "2026-02-01",
  "toDate": "2026-02-28",
  "port": "Chennai",
  "vessel": "MV Emerald",
  "oeta": "2026-02-15",
  "leta": "2026-02-16",
  "alleta": "2026-02-17",
  "chkPort": "Y",
  "chkVessel": "Y",
  "chkPickupDate": "N",
  "chkConsolidated": "Y",
  "chkDeliveryDone": "N",
  "entryDate": "2026-02-15",
  "saleOrderMasterRefId": 201
}
```

**Example Request:**
```
POST /api/pre-alert-masters
Content-Type: application/json
Authorization: Bearer {token}

{
  "companyRefId": 1,
  "customerMasterRefId": 101,
  "jobTypeMasterRefId": 5,
  "port": "Chennai",
  "vessel": "MV Emerald",
  "entryDate": "2026-02-15"
}
```

**Response (201 Created):**
```json
{
  "id": 2,
  "companyRefId": 1,
  "customerMasterRefId": 101,
  "jobTypeMasterRefId": 5,
  "port": "Chennai",
  "vessel": "MV Emerald",
  "active": 1,
  "createdDate": "2026-02-27T14:30:00",
  "modifiedDate": "2026-02-27T14:30:00"
}
```

---

### 5. Update PreAlertMaster Record
**PUT** `/api/pre-alert-masters/{id}`

**Description:** Update an existing PreAlertMaster record.

**Path Parameters:**
- `id` (Integer, required): PreAlertMaster ID

**Request Body:** (Same structure as Create, but only include fields to be updated)

**Example Request:**
```
PUT /api/pre-alert-masters/1
Content-Type: application/json
Authorization: Bearer {token}

{
  "vessel": "MV Diamond",
  "oeta": "2026-02-16",
  "leta": "2026-02-17"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "companyRefId": 1,
  "customerMasterRefId": 101,
  "vessel": "MV Diamond",
  "oeta": "2026-02-16",
  "leta": "2026-02-17",
  "modifiedDate": "2026-02-27T14:35:00"
}
```

---

### 6. Delete PreAlertMaster Record
**DELETE** `/api/pre-alert-masters/{id}`

**Description:** Delete a PreAlertMaster record and its associated PreAlert details.

**Path Parameters:**
- `id` (Integer, required): PreAlertMaster ID

**Example Request:**
```
DELETE /api/pre-alert-masters/1
Authorization: Bearer {token}
```

**Response (204 No Content):** Empty response

**Response (404 Not Found):**
```json
"PreAlertMaster not found with ID: 999"
```

---

### 7. Get PreAlertMaster by Customer ID
**GET** `/api/pre-alert-masters/customer/{customerMasterRefId}`

**Description:** Retrieve all PreAlertMaster records for a specific customer.

**Path Parameters:**
- `customerMasterRefId` (Integer, required): Customer reference ID

**Example Request:**
```
GET /api/pre-alert-masters/customer/101
Authorization: Bearer {token}
```

**Response (200 OK):** List of PreAlertMaster records for the customer

---

### 8. Get PreAlertMaster by Job Type ID
**GET** `/api/pre-alert-masters/job-type/{jobTypeMasterRefId}`

**Description:** Retrieve all PreAlertMaster records for a specific job type.

**Path Parameters:**
- `jobTypeMasterRefId` (Integer, required): Job type reference ID

**Example Request:**
```
GET /api/pre-alert-masters/job-type/5
Authorization: Bearer {token}
```

**Response (200 OK):** List of PreAlertMaster records for the job type

---

### 9. Get PreAlertMaster by Port
**GET** `/api/pre-alert-masters/port/{port}`

**Description:** Retrieve all PreAlertMaster records for a specific port.

**Path Parameters:**
- `port` (String, required): Port name

**Example Request:**
```
GET /api/pre-alert-masters/port/Chennai
Authorization: Bearer {token}
```

**Response (200 OK):** List of PreAlertMaster records for the port

---

### 10. Get PreAlertMaster by Vessel
**GET** `/api/pre-alert-masters/vessel/{vessel}`

**Description:** Retrieve all PreAlertMaster records for a specific vessel.

**Path Parameters:**
- `vessel` (String, required): Vessel name

**Example Request:**
```
GET /api/pre-alert-masters/vessel/MV%20Emerald
Authorization: Bearer {token}
```

**Response (200 OK):** List of PreAlertMaster records for the vessel

---

### 11. Get PreAlertMaster by Date Range
**GET** `/api/pre-alert-masters/company/{companyId}/date-range?fromDate=yyyy-MM-dd&toDate=yyyy-MM-dd`

**Description:** Retrieve PreAlertMaster records within a date range.

**Path Parameters:**
- `companyId` (Integer, required): Company reference ID

**Query Parameters:**
- `fromDate` (String, required): Start date (format: yyyy-MM-dd)
- `toDate` (String, required): End date (format: yyyy-MM-dd)

**Example Request:**
```
GET /api/pre-alert-masters/company/1/date-range?fromDate=2026-02-01&toDate=2026-02-28
Authorization: Bearer {token}
```

**Response (200 OK):** List of PreAlertMaster records within the date range

---

### 12. Get PreAlertMaster by CNumber
**GET** `/api/pre-alert-masters/cnumber/{cNumber}?companyRefId={companyRefId}`

**Description:** Retrieve a PreAlertMaster record by sequence number.

**Path Parameters:**
- `cNumber` (Integer, required): Sequence number

**Query Parameters:**
- `companyRefId` (Integer, required): Company reference ID

**Example Request:**
```
GET /api/pre-alert-masters/cnumber/1?companyRefId=1
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "cNumber": 1,
  "cNumberDisplay": "PA0001/2026",
  "companyRefId": 1
}
```

---

### 13. Get PreAlertMaster by CNumberDisplay
**GET** `/api/pre-alert-masters/cnumber-display/{cNumberDisplay}`

**Description:** Retrieve a PreAlertMaster record by display number.

**Path Parameters:**
- `cNumberDisplay` (String, required): Display number (e.g., PA0001/2026)

**Example Request:**
```
GET /api/pre-alert-masters/cnumber-display/PA0001/2026
Authorization: Bearer {token}
```

**Response (200 OK):** PreAlertMaster record with the matching display number

---

### 14. Activate PreAlertMaster Record
**POST** `/api/pre-alert-masters/{id}/activate`

**Description:** Activate a PreAlertMaster record.

**Path Parameters:**
- `id` (Integer, required): PreAlertMaster ID

**Example Request:**
```
POST /api/pre-alert-masters/1/activate
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "companyRefId": 1,
  "active": 1,
  "modifiedDate": "2026-02-27T15:00:00"
}
```

---

### 15. Deactivate PreAlertMaster Record
**POST** `/api/pre-alert-masters/{id}/deactivate`

**Description:** Deactivate a PreAlertMaster record.

**Path Parameters:**
- `id` (Integer, required): PreAlertMaster ID

**Example Request:**
```
POST /api/pre-alert-masters/1/deactivate
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "companyRefId": 1,
  "active": 0,
  "modifiedDate": "2026-02-27T15:05:00"
}
```

---

### 16. Execute Bulk Import (SP_PreAlert)
**POST** `/api/pre-alert-masters/bulk-import?masterJson={json}&companyId={companyId}`

**Description:** Execute bulk import using SP_PreAlert stored procedure.

**Query Parameters:**
- `masterJson` (String, required): JSON containing PreAlertMaster and PreAlert details
- `companyId` (Integer, required): Company reference ID

**JSON Structure:**
```json
{
  "Id": 0,
  "CompanyRefId": 1,
  "CustomerMasterRefId": 101,
  "JobTypeMasterRefId": 5,
  "Port": "Chennai",
  "Vessel": "MV Emerald",
  "OETA": "2026-02-15",
  "LETA": "2026-02-16",
  "EntryDate": "2026-02-15",
  "Active": 1,
  "PreAlert": [
    {
      "CustomerMasterRefId": 101,
      "EmployeeMasterRefId": 10,
      "JobTypeMasterRefId": 5,
      "ShipName": "Ship A",
      "Vessel": "MV Emerald",
      "Port": "Chennai",
      "ETA": "2026-02-15",
      "Active": 1
    }
  ]
}
```

**Example Request:**
```
POST /api/pre-alert-masters/bulk-import?masterJson={"Id":0,"CompanyRefId":1,...}&companyId=1
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
"Bulk import executed successfully"
```

---

### 17. Count Active PreAlertMaster Records
**GET** `/api/pre-alert-masters/company/{companyRefId}/count-active`

**Description:** Get count of active PreAlertMaster records for a company.

**Path Parameters:**
- `companyRefId` (Integer, required): Company reference ID

**Example Request:**
```
GET /api/pre-alert-masters/company/1/count-active
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
5
```

---

## PreAlert Endpoints

### 1. Get All PreAlert Records by Company
**GET** `/api/pre-alerts/company/{companyRefId}`

**Description:** Retrieve all PreAlert detail records for a specific company.

**Path Parameters:**
- `companyRefId` (Integer, required): Company reference ID

**Example Request:**
```
GET /api/pre-alerts/company/1
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "companyRefId": 1,
    "customerMasterRefId": 101,
    "employeeMasterRefId": 10,
    "jobTypeMasterRefId": 5,
    "shipName": "Ship A",
    "vessel": "MV Emerald",
    "commodity": "Containers",
    "eta": "2026-02-15",
    "etb": "2026-02-15",
    "etd": "2026-02-16",
    "jobNo": "JOB001",
    "port": "Chennai",
    "weight": "500",
    "packageInfo": "Cartons",
    "awbNo": "AWB123456",
    "agentName": "Agent XYZ",
    "agentPhone": "9876543210",
    "remarks": "Standard shipment",
    "scn": "SCN001",
    "active": 1,
    "preAlertMasterRefId": 1,
    "createdDate": "2026-02-15T10:30:00",
    "modifiedDate": "2026-02-15T10:30:00"
  }
]
```

---

### 2. Get Active PreAlert Records
**GET** `/api/pre-alerts/company/{companyRefId}/active`

**Description:** Retrieve only active PreAlert records.

**Path Parameters:**
- `companyRefId` (Integer, required): Company reference ID

**Example Request:**
```
GET /api/pre-alerts/company/1/active
Authorization: Bearer {token}
```

**Response (200 OK):** List of active PreAlert records

---

### 3. Get PreAlert by ID
**GET** `/api/pre-alerts/{id}`

**Description:** Retrieve a specific PreAlert record by ID.

**Path Parameters:**
- `id` (Integer, required): PreAlert ID

**Example Request:**
```
GET /api/pre-alerts/1
Authorization: Bearer {token}
```

**Response (200 OK):** PreAlert record details

**Response (404 Not Found):**
```json
"PreAlert not found with ID: 999"
```

---

### 4. Create New PreAlert Record
**POST** `/api/pre-alerts`

**Description:** Create a new PreAlert record.

**Request Body:**
```json
{
  "companyRefId": 1,
  "customerMasterRefId": 101,
  "employeeMasterRefId": 10,
  "jobTypeMasterRefId": 5,
  "shipName": "Ship A",
  "vessel": "MV Emerald",
  "commodity": "Containers",
  "eta": "2026-02-15",
  "etb": "2026-02-15",
  "etd": "2026-02-16",
  "jobNo": "JOB001",
  "port": "Chennai",
  "weight": "500",
  "packageInfo": "Cartons",
  "awbNo": "AWB123456",
  "agentName": "Agent XYZ",
  "agentPhone": "9876543210",
  "remarks": "Standard shipment",
  "preAlertMasterRefId": 1
}
```

**Example Request:**
```
POST /api/pre-alerts
Content-Type: application/json
Authorization: Bearer {token}

{
  "companyRefId": 1,
  "customerMasterRefId": 101,
  "employeeMasterRefId": 10,
  "vessel": "MV Emerald",
  "port": "Chennai",
  "preAlertMasterRefId": 1
}
```

**Response (201 Created):** PreAlert record with assigned ID

---

### 5. Update PreAlert Record
**PUT** `/api/pre-alerts/{id}`

**Description:** Update an existing PreAlert record.

**Path Parameters:**
- `id` (Integer, required): PreAlert ID

**Request Body:** (Same structure as Create, but only include fields to be updated)

**Example Request:**
```
PUT /api/pre-alerts/1
Content-Type: application/json
Authorization: Bearer {token}

{
  "eta": "2026-02-16",
  "remarks": "Updated remarks"
}
```

**Response (200 OK):** Updated PreAlert record

---

### 6. Delete PreAlert Record
**DELETE** `/api/pre-alerts/{id}`

**Description:** Delete a PreAlert record.

**Path Parameters:**
- `id` (Integer, required): PreAlert ID

**Example Request:**
```
DELETE /api/pre-alerts/1
Authorization: Bearer {token}
```

**Response (204 No Content):** Empty response

---

### 7. Get PreAlert by PreAlertMaster ID
**GET** `/api/pre-alerts/master/{preAlertMasterRefId}`

**Description:** Retrieve all PreAlert records for a specific PreAlertMaster.

**Path Parameters:**
- `preAlertMasterRefId` (Integer, required): PreAlertMaster reference ID

**Example Request:**
```
GET /api/pre-alerts/master/1
Authorization: Bearer {token}
```

**Response (200 OK):** List of PreAlert records for the master

---

### 8. Get PreAlert by Customer ID
**GET** `/api/pre-alerts/customer/{customerMasterRefId}`

**Description:** Retrieve all PreAlert records for a specific customer.

**Path Parameters:**
- `customerMasterRefId` (Integer, required): Customer reference ID

**Example Request:**
```
GET /api/pre-alerts/customer/101
Authorization: Bearer {token}
```

**Response (200 OK):** List of PreAlert records for the customer

---

### 9. Get PreAlert by Employee ID
**GET** `/api/pre-alerts/employee/{employeeMasterRefId}`

**Description:** Retrieve all PreAlert records for a specific employee.

**Path Parameters:**
- `employeeMasterRefId` (Integer, required): Employee reference ID

**Example Request:**
```
GET /api/pre-alerts/employee/10
Authorization: Bearer {token}
```

**Response (200 OK):** List of PreAlert records for the employee

---

### 10. Get PreAlert by Job Type ID
**GET** `/api/pre-alerts/job-type/{jobTypeMasterRefId}`

**Description:** Retrieve all PreAlert records for a specific job type.

**Path Parameters:**
- `jobTypeMasterRefId` (Integer, required): Job type reference ID

**Example Request:**
```
GET /api/pre-alerts/job-type/5
Authorization: Bearer {token}
```

**Response (200 OK):** List of PreAlert records for the job type

---

### 11. Get PreAlert by Job Status ID
**GET** `/api/pre-alerts/job-status/{jobStatusMasterRefId}`

**Description:** Retrieve all PreAlert records for a specific job status.

**Path Parameters:**
- `jobStatusMasterRefId` (Integer, required): Job status reference ID

**Example Request:**
```
GET /api/pre-alerts/job-status/2
Authorization: Bearer {token}
```

**Response (200 OK):** List of PreAlert records for the job status

---

### 12. Get PreAlert by Boarding Officer ID
**GET** `/api/pre-alerts/boarding-officer/{boardingOfficerRefId}`

**Description:** Retrieve all PreAlert records for a specific boarding officer.

**Path Parameters:**
- `boardingOfficerRefId` (Integer, required): Boarding officer reference ID

**Example Request:**
```
GET /api/pre-alerts/boarding-officer/5
Authorization: Bearer {token}
```

**Response (200 OK):** List of PreAlert records for the boarding officer

---

### 13. Get PreAlert by Vessel
**GET** `/api/pre-alerts/vessel/{vessel}`

**Description:** Retrieve all PreAlert records for a specific vessel.

**Path Parameters:**
- `vessel` (String, required): Vessel name

**Example Request:**
```
GET /api/pre-alerts/vessel/MV%20Emerald
Authorization: Bearer {token}
```

**Response (200 OK):** List of PreAlert records for the vessel

---

### 14. Get PreAlert by Port
**GET** `/api/pre-alerts/port/{port}`

**Description:** Retrieve all PreAlert records for a specific port.

**Path Parameters:**
- `port` (String, required): Port name

**Example Request:**
```
GET /api/pre-alerts/port/Chennai
Authorization: Bearer {token}
```

**Response (200 OK):** List of PreAlert records for the port

---

### 15. Get PreAlert by Job Number
**GET** `/api/pre-alerts/job-no/{jobNo}`

**Description:** Retrieve all PreAlert records for a specific job number.

**Path Parameters:**
- `jobNo` (String, required): Job number

**Example Request:**
```
GET /api/pre-alerts/job-no/JOB001
Authorization: Bearer {token}
```

**Response (200 OK):** List of PreAlert records for the job number

---

### 16. Delete All PreAlert Records by Master ID
**DELETE** `/api/pre-alerts/master/{preAlertMasterRefId}/all`

**Description:** Delete all PreAlert records for a specific PreAlertMaster.

**Path Parameters:**
- `preAlertMasterRefId` (Integer, required): PreAlertMaster reference ID

**Example Request:**
```
DELETE /api/pre-alerts/master/1/all
Authorization: Bearer {token}
```

**Response (204 No Content):** Empty response

---

### 17. Count PreAlert Records by Master ID
**GET** `/api/pre-alerts/master/{preAlertMasterRefId}/count`

**Description:** Get count of PreAlert records for a specific PreAlertMaster.

**Path Parameters:**
- `preAlertMasterRefId` (Integer, required): PreAlertMaster reference ID

**Example Request:**
```
GET /api/pre-alerts/master/1/count
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
5
```

---

### 18. Activate PreAlert Record
**POST** `/api/pre-alerts/{id}/activate`

**Description:** Activate a PreAlert record.

**Path Parameters:**
- `id` (Integer, required): PreAlert ID

**Example Request:**
```
POST /api/pre-alerts/1/activate
Authorization: Bearer {token}
```

**Response (200 OK):** Activated PreAlert record with active=1

---

### 19. Deactivate PreAlert Record
**POST** `/api/pre-alerts/{id}/deactivate`

**Description:** Deactivate a PreAlert record.

**Path Parameters:**
- `id` (Integer, required): PreAlert ID

**Example Request:**
```
POST /api/pre-alerts/1/deactivate
Authorization: Bearer {token}
```

**Response (200 OK):** Deactivated PreAlert record with active=0

---

## Error Handling

### Common Error Responses

**400 - Bad Request (Validation Error):**
```json
{
  "timestamp": "2026-02-27T15:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/pre-alerts",
  "details": [
    "Company Reference ID is required",
    "Employee Master Reference ID is required"
  ]
}
```

**401 - Unauthorized:**
```json
{
  "timestamp": "2026-02-27T15:30:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or missing authentication token"
}
```

**403 - Forbidden:**
```json
{
  "timestamp": "2026-02-27T15:30:00Z",
  "status": 403,
  "error": "Forbidden",
  "message": "You do not have permission to access this resource"
}
```

**404 - Not Found:**
```json
{
  "timestamp": "2026-02-27T15:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "PreAlert not found with ID: 999"
}
```

**500 - Internal Server Error:**
```json
{
  "timestamp": "2026-02-27T15:30:00Z",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Error creating PreAlert: Database connection failed"
}
```

---

## Postman Environment Variables

Create a Postman environment with the following variables:

```
{
  "name": "Pre-Alert API Development",
  "values": [
    {
      "key": "base_url",
      "value": "http://localhost:8082/api",
      "enabled": true
    },
    {
      "key": "token",
      "value": "your_jwt_token_here",
      "enabled": true
    },
    {
      "key": "companyRefId",
      "value": "1",
      "enabled": true
    },
    {
      "key": "customerMasterRefId",
      "value": "101",
      "enabled": true
    },
    {
      "key": "employeeMasterRefId",
      "value": "10",
      "enabled": true
    }
  ]
}
```

### Usage in Requests

Use variables in your requests:
```
GET {{base_url}}/pre-alerts/company/{{companyRefId}}
Authorization: Bearer {{token}}
```

---

## Testing Steps

### 1. Setup
- Import this collection into Postman
- Set up the environment with your API base URL and authentication token
- Ensure the Spring Boot application is running on port 8082

### 2. Test PreAlertMaster Endpoints
1. Create a new PreAlertMaster record
2. Retrieve it by ID
3. Update the record with new values
4. Deactivate the record
5. Activate the record
6. Delete the record

### 3. Test PreAlert Endpoints
1. Create a new PreAlert record (linked to a PreAlertMaster)
2. Retrieve it by ID
3. Update the record
4. Retrieve all PreAlert records for a master
5. Count PreAlert records
6. Delete the record

### 4. Test Filtering & Search
1. Get records by company ID
2. Get records by customer ID
3. Get records by vessel name
4. Get records by port
5. Get records by date range

### 5. Test Bulk Import
1. Prepare JSON with master and detail records
2. Execute bulk import endpoint
3. Verify records are created in database

---

## Notes

- All timestamps are in ISO 8601 format (UTC)
- All IDs are integers
- Active status: 1 = active, 0 = inactive
- Default active status is 1 when creating new records
- Date format: yyyy-MM-dd
- URL encoding required for special characters (e.g., spaces as %20)

---

Generated: 2026-02-27
API Standard Version: 1.0

