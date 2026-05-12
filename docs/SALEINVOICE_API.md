# Sales Invoice REST API Documentation

## Overview
The Sales Invoice API provides comprehensive CRUD operations and advanced search functionality for managing sales invoices in the Maleva system.

**Base URL:** `http://localhost:8082/api/v1/sale-invoices`  
**Port:** `8082`  
**Authentication:** JWT Bearer Token required for all endpoints

---

## Endpoints Summary

### 1. Get Next Invoice Number
**GET** `/api/v1/sale-invoices/next-number`

Generate the next sequential invoice number for a company.

#### Request Parameters
- `companyId` (required): Company ID

#### Response (200 OK)
```json
{
  "IsSuccess": true,
  "StatusCode": 200,
  "Message": "Next invoice number generated successfully",
  "Data1": "INV000000001"
}
```

#### Error Response (400 Bad Request)
```json
{
  "IsSuccess": false,
  "StatusCode": 400,
  "Message": "Invalid company ID"
}
```

---

### 2. Create Invoice
**POST** `/api/v1/sale-invoices`

Create a new sales invoice with line items.

#### Request Body
```json
{
  "companyRefId": 6,
  "customerRefId": 66,
  "jobMasterRefId": 1,
  "employeeRefId": 5,
  "saleDate": "2024-01-15T10:30:00Z",
  "saleType": "STANDARD",
  "billType": "INVOICE",
  "offVesselName": "MV VESSEL01",
  "loadingVesselName": "MV VESSEL02",
  "sPort": "Shanghai",
  "oPort": "Singapore",
  "remarks": "Sample invoice",
  "grossAmount": 10000,
  "taxAmount": 500,
  "discountAmount": 0,
  "amount": 10500
}
```

#### Response (201 Created)
```json
{
  "IsSuccess": true,
  "StatusCode": 201,
  "Message": "Invoice created successfully",
  "Data1": {
    "id": 123,
    "invoiceNumber": "INV000000001",
    "amount": 10500
  }
}
```

#### Error Responses
- **400 Bad Request:** Invalid input data
- **409 Conflict:** Business rule violation
- **500 Internal Server Error:** Server error

---

### 3. Search Invoices
**GET** `/api/v1/sale-invoices/search`

Advanced search and filtering of invoices with pagination support.

#### Query Parameters
- `companyId` (required): Company ID
- `customerId` (optional): Filter by customer
- `employeeId` (optional): Filter by employee
- `jobId` (optional): Filter by job
- `fromDate` (optional): Start date (ISO format)
- `toDate` (optional): End date (ISO format)
- `billType` (optional): Filter by bill type
- `saleType` (optional): Filter by sale type
- `search` (optional): Search by invoice number
- `page` (optional, default=0): Page number
- `size` (optional, default=20): Page size
- `sort` (optional, default="id,desc"): Sort field and direction

#### Response (200 OK)
```json
{
  "IsSuccess": true,
  "StatusCode": 200,
  "Message": "Search completed successfully",
  "Data1": {
    "invoices": [
      {
        "id": 123,
        "companyRefId": 6,
        "customerRefId": 66,
        "invoiceNumber": "INV000000001",
        "saleDate": "2024-01-15T10:30:00",
        "amount": 10500,
        "active": 1
      }
    ],
    "details": [
      [
        {
          "id": 1,
          "itemMasterRefId": 101,
          "itemQty": 5,
          "salesRate": 2000,
          "amount": 10000
        }
      ]
    ],
    "totalRecords": 50,
    "totalPages": 3,
    "currentPage": 0
  }
}
```

---

### 4. Get Invoice Details
**GET** `/api/v1/sale-invoices/{id}`

Retrieve complete invoice details including master and line items.

#### Path Parameters
- `id` (required): Invoice ID

#### Query Parameters
- `companyId` (required): Company ID for validation

#### Response (200 OK)
```json
{
  "IsSuccess": true,
  "StatusCode": 200,
  "Message": "Invoice retrieved successfully",
  "Data1": {
    "master": {
      "id": 123,
      "companyRefId": 6,
      "customerRefId": 66,
      "invoiceNumber": "INV000000001",
      "amount": 10500,
      "createdDate": "2024-01-15T10:30:00",
      "createdBy": "admin"
    },
    "details": [
      {
        "id": 1,
        "itemMasterRefId": 101,
        "itemQty": 5,
        "salesRate": 2000,
        "taxPercent": 5,
        "amount": 10000
      }
    ]
  }
}
```

#### Error Responses
- **404 Not Found:** Invoice not found
- **403 Forbidden:** Unauthorized company access

---

### 5. Update Invoice
**PUT** `/api/v1/sale-invoices/{id}`

Update an existing invoice.

#### Path Parameters
- `id` (required): Invoice ID

#### Query Parameters
- `companyId` (required): Company ID for validation

#### Request Body
```json
{
  "companyRefId": 6,
  "customerRefId": 67,
  "remarks": "Updated remarks",
  "amount": 11000
}
```

#### Response (200 OK)
```json
{
  "IsSuccess": true,
  "StatusCode": 200,
  "Message": "Invoice updated successfully",
  "Data1": {
    "id": 123,
    "companyRefId": 6,
    "amount": 11000,
    "modifiedDate": "2024-01-15T14:30:00",
    "modifiedBy": "admin"
  }
}
```

#### Error Responses
- **404 Not Found:** Invoice not found
- **409 Conflict:** Cannot update completed or QNE-pushed invoice
- **403 Forbidden:** Unauthorized company access

---

### 6. Delete Invoice (Soft Delete)
**DELETE** `/api/v1/sale-invoices/{id}`

Delete (soft delete - set Active=2) an invoice.

#### Path Parameters
- `id` (required): Invoice ID

#### Query Parameters
- `companyId` (required): Company ID for validation

#### Response (204 No Content)
Empty body

#### Error Responses
- **404 Not Found:** Invoice not found
- **409 Conflict:** Cannot delete completed or QNE-pushed invoice
- **403 Forbidden:** Unauthorized company access

---

### 7. Push to QNE
**POST** `/api/v1/sale-invoices/{id}/push-qne`

Push invoice to external QNE system.

#### Path Parameters
- `id` (required): Invoice ID

#### Query Parameters
- `companyId` (required): Company ID for validation

#### Response (200 OK)
```json
{
  "IsSuccess": true,
  "StatusCode": 200,
  "Message": "Invoice pushed to QNE successfully",
  "Data1": {
    "qneCode": "QNE1234567890",
    "qneId": "ID123",
    "fileUrl": "https://qne.system.com/files/123",
    "status": "success"
  }
}
```

#### Error Responses
- **404 Not Found:** Invoice not found
- **400 Bad Request:** Invoice already pushed to QNE
- **403 Forbidden:** Unauthorized company access
- **502 Bad Gateway:** QNE API error

---

### 8. Get Invoices by Company
**GET** `/api/v1/sale-invoices/company/{companyId}`

Retrieve all invoices for a company.

#### Path Parameters
- `companyId` (required): Company ID

#### Response (200 OK)
```json
{
  "IsSuccess": true,
  "StatusCode": 200,
  "Message": "Invoices retrieved successfully",
  "Data1": [
    {
      "id": 123,
      "invoiceNumber": "INV000000001",
      "customerRefId": 66,
      "amount": 10500
    }
  ]
}
```

---

### 9. Get Invoices by Company and Customer
**GET** `/api/v1/sale-invoices/company/{companyId}/customer/{customerId}`

Retrieve invoices for a specific customer.

#### Path Parameters
- `companyId` (required): Company ID
- `customerId` (required): Customer ID

#### Response (200 OK)
Same format as Get Invoices by Company

---

### 10. Get Unpushed Invoices
**GET** `/api/v1/sale-invoices/unpushed`

Retrieve invoices not yet pushed to QNE.

#### Query Parameters
- `companyId` (required): Company ID

#### Response (200 OK)
Same format as Get Invoices by Company

---

### 11. Get Invoice by C Number
**GET** `/api/v1/sale-invoices/by-cnumber`

Retrieve invoice by C Number (invoice number display).

#### Query Parameters
- `companyId` (required): Company ID
- `cNumber` (required): Invoice number display

#### Response (200 OK)
```json
{
  "IsSuccess": true,
  "StatusCode": 200,
  "Message": "Invoice retrieved successfully",
  "Data1": {
    "id": 123,
    "invoiceNumber": "INV000000001",
    "cNumberDisplay": "INV000000001",
    "amount": 10500
  }
}
```

---

## Error Handling

### HTTP Status Codes
- **200 OK:** Successful GET request
- **201 Created:** Successful POST request
- **204 No Content:** Successful DELETE request
- **400 Bad Request:** Invalid input parameters
- **403 Forbidden:** Unauthorized access (company mismatch)
- **404 Not Found:** Resource not found
- **409 Conflict:** Business rule violation
- **500 Internal Server Error:** Server error

### Error Response Format
```json
{
  "IsSuccess": false,
  "StatusCode": 400,
  "Message": "Error message",
  "ErrorDetails": "Detailed error information"
}
```

---

## Data Types

### Amount Fields
- Represented as `Double` in decimal format
- Examples: `10000.50`, `100.00`

### Date/Time Fields
- Represented in ISO 8601 format with timezone
- Examples: `2024-01-15T10:30:00Z`, `2024-01-15T14:45:30`

### Status Codes
- **Active:** `1` (Active), `2` (Deleted/Inactive)
- **Job Status:** Various integer values depending on business logic

---

## Authentication

All endpoints require JWT Bearer token in the Authorization header:

```
Authorization: Bearer {JWT_TOKEN}
```

### Required Roles
- `ROLE_ADMIN`
- `ROLE_SUPERADMIN`
- `ROLE_USER`

---

## Rate Limiting & Performance

- Default page size: 20 items
- Maximum page size: 100 items
- Response time target: < 500ms

---

## Examples

### Example 1: Create Invoice
```bash
curl -X POST http://localhost:8082/api/v1/sale-invoices \
  -H "Authorization: Bearer {TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "companyRefId": 6,
    "customerRefId": 66,
    "jobMasterRefId": 1,
    "saleDate": "2024-01-15T10:30:00Z",
    "saleType": "STANDARD",
    "billType": "INVOICE",
    "amount": 10500
  }'
```

### Example 2: Search Invoices
```bash
curl -X GET "http://localhost:8082/api/v1/sale-invoices/search?companyId=6&customerId=66&page=0&size=20" \
  -H "Authorization: Bearer {TOKEN}"
```

### Example 3: Get Invoice Details
```bash
curl -X GET "http://localhost:8082/api/v1/sale-invoices/123?companyId=6" \
  -H "Authorization: Bearer {TOKEN}"
```

---

## Configuration

### Application Properties (application.yaml)
```yaml
server:
  port: 8082
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 50MB

spring:
  jpa:
    show-sql: false
    hibernate:
      ddl-auto: none
```

---

## Deployment

The application runs on **port 8082** by default and is configured for SQL Server 2019+.

### Environment Variables
```
SPRING_DATASOURCE_URL=jdbc:sqlserver://server:port;databaseName=database
SPRING_DATASOURCE_USERNAME=username
SPRING_DATASOURCE_PASSWORD=password
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379
```

---

## Support

For issues or questions, refer to:
- Backend Migration Guide: Backend_Migration_Guide.md
- API Standards: docs/API_Standards.md

