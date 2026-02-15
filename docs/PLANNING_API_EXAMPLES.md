# Planning Management API - Request/Response Examples

## Base URL
```
http://localhost:8082
```

## Authentication Headers
```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

---

## 🟢 Planning Master Endpoints

### 1. Create Planning with Details

**Request:**
```http
POST /api/planning-masters HTTP/1.1
Content-Type: application/json

{
  "companyRefId": 1,
  "userRefId": 1,
  "employeeRefId": 2,
  "lastEmployeeRefId": 2,
  "saleDate": "2026-02-15T10:30:00",
  "fDate": "2026-02-15T00:00:00",
  "tDate": "2026-02-28T23:59:59",
  "cNumberDisplay": "PL000000001",
  "cNumber": 1,
  "remarks": "Weekly planning for February",
  "search": "transportation planning",
  "active": 1,
  "createdBy": "admin",
  "modifiedBy": "admin",
  "planningDetails": [
    {
      "saleOrderMasterRefId": 10,
      "truckRefId": 5,
      "remarks": "First delivery route",
      "originD": "Warehouse A, Chennai",
      "destinationD": "Store B, Bangalore",
      "pickupDateD": "2026-02-15T08:00:00",
      "deliveryDateD": "2026-02-15T18:00:00",
      "sortBy": 1,
      "truckNameD": "Truck-001",
      "driverNameD": "John Doe",
      "pickupTimeList": "[08:00, 09:00, 10:00]",
      "pickupQuantityList": "[10, 20, 15]",
      "deliveryQuantityList": "[45]",
      "deliveryTimeList": "[18:00]"
    },
    {
      "saleOrderMasterRefId": 11,
      "truckRefId": 6,
      "remarks": "Second delivery route",
      "originD": "Warehouse C, Delhi",
      "destinationD": "Store D, Mumbai",
      "pickupDateD": "2026-02-16T08:00:00",
      "deliveryDateD": "2026-02-16T18:00:00",
      "sortBy": 2,
      "truckNameD": "Truck-002",
      "driverNameD": "Jane Smith",
      "pickupTimeList": "[08:00, 09:30]",
      "pickupQuantityList": "[25, 30]",
      "deliveryQuantityList": "[55]",
      "deliveryTimeList": "[18:00]"
    }
  ]
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "companyRefId": 1,
  "userRefId": 1,
  "employeeRefId": 2,
  "lastEmployeeRefId": 2,
  "saleDate": "2026-02-15T10:30:00",
  "fDate": "2026-02-15T00:00:00",
  "tDate": "2026-02-28T23:59:59",
  "cNumberDisplay": "PL000000001",
  "cNumber": 1,
  "remarks": "Weekly planning for February",
  "search": "transportation planning",
  "active": 1,
  "createdDate": "2026-02-15T12:00:00",
  "createdBy": "admin",
  "modifiedDate": "2026-02-15T12:00:00",
  "modifiedBy": "admin",
  "planningDetails": [
    {
      "id": 1,
      "planningMasterRefId": 1,
      "saleOrderMasterRefId": 10,
      "truckRefId": 5,
      "remarks": "First delivery route",
      "createdDate": "2026-02-15T12:00:00",
      "modifiedDate": "2026-02-15T12:00:00",
      "originD": "Warehouse A, Chennai",
      "destinationD": "Store B, Bangalore",
      "pickupDateD": "2026-02-15T08:00:00",
      "deliveryDateD": "2026-02-15T18:00:00",
      "sortBy": 1,
      "truckNameD": "Truck-001",
      "driverNameD": "John Doe",
      "pickupTimeList": "[08:00, 09:00, 10:00]",
      "pickupQuantityList": "[10, 20, 15]",
      "deliveryQuantityList": "[45]",
      "deliveryTimeList": "[18:00]"
    },
    {
      "id": 2,
      "planningMasterRefId": 1,
      "saleOrderMasterRefId": 11,
      "truckRefId": 6,
      "remarks": "Second delivery route",
      "createdDate": "2026-02-15T12:00:00",
      "modifiedDate": "2026-02-15T12:00:00",
      "originD": "Warehouse C, Delhi",
      "destinationD": "Store D, Mumbai",
      "pickupDateD": "2026-02-16T08:00:00",
      "deliveryDateD": "2026-02-16T18:00:00",
      "sortBy": 2,
      "truckNameD": "Truck-002",
      "driverNameD": "Jane Smith",
      "pickupTimeList": "[08:00, 09:30]",
      "pickupQuantityList": "[25, 30]",
      "deliveryQuantityList": "[55]",
      "deliveryTimeList": "[18:00]"
    }
  ]
}
```

---

### 2. Get All Planning Records

**Request:**
```http
GET /api/planning-masters HTTP/1.1
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "companyRefId": 1,
    "cNumberDisplay": "PL000000001",
    "remarks": "Weekly planning for February",
    "active": 1,
    "createdDate": "2026-02-15T12:00:00"
  },
  {
    "id": 2,
    "companyRefId": 1,
    "cNumberDisplay": "PL000000002",
    "remarks": "Weekly planning 2",
    "active": 1,
    "createdDate": "2026-02-16T12:00:00"
  }
]
```

---

### 3. Get Planning by ID

**Request:**
```http
GET /api/planning-masters/1 HTTP/1.1
```

**Response (200 OK):**
```json
{
  "id": 1,
  "companyRefId": 1,
  "userRefId": 1,
  "employeeRefId": 2,
  "saleDate": "2026-02-15T10:30:00",
  "cNumberDisplay": "PL000000001",
  "cNumber": 1,
  "remarks": "Weekly planning for February",
  "active": 1,
  "createdDate": "2026-02-15T12:00:00",
  "modifiedDate": "2026-02-15T12:00:00",
  "planningDetails": [
    {
      "id": 1,
      "planningMasterRefId": 1,
      "saleOrderMasterRefId": 10,
      "sortBy": 1
    }
  ]
}
```

---

### 4. Update Planning

**Request:**
```http
PUT /api/planning-masters/1 HTTP/1.1
Content-Type: application/json

{
  "companyRefId": 1,
  "employeeRefId": 3,
  "fDate": "2026-02-16T00:00:00",
  "tDate": "2026-03-01T23:59:59",
  "remarks": "Updated weekly planning",
  "active": 1,
  "modifiedBy": "admin",
  "planningDetails": [
    {
      "saleOrderMasterRefId": 10,
      "truckRefId": 7,
      "sortBy": 1
    }
  ]
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "companyRefId": 1,
  "employeeRefId": 3,
  "fDate": "2026-02-16T00:00:00",
  "tDate": "2026-03-01T23:59:59",
  "remarks": "Updated weekly planning",
  "modifiedDate": "2026-02-15T12:30:00",
  "planningDetails": [
    {
      "id": 3,
      "planningMasterRefId": 1,
      "saleOrderMasterRefId": 10,
      "truckRefId": 7,
      "sortBy": 1
    }
  ]
}
```

---

### 5. Delete Planning

**Request:**
```http
DELETE /api/planning-masters/1 HTTP/1.1
```

**Response (204 No Content):**
```
[Empty body]
```

---

### 6. Search by Date Range

**Request:**
```http
GET /api/planning-masters/search/date-range?companyId=1&fromDate=2026-02-01T00:00:00&toDate=2026-02-28T23:59:59 HTTP/1.1
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "companyRefId": 1,
    "saleDate": "2026-02-15T10:30:00",
    "cNumberDisplay": "PL000000001",
    "remarks": "Weekly planning for February",
    "active": 1
  },
  {
    "id": 2,
    "companyRefId": 1,
    "saleDate": "2026-02-20T10:30:00",
    "cNumberDisplay": "PL000000002",
    "remarks": "Mid-month planning",
    "active": 1
  }
]
```

---

### 7. Search by Keyword

**Request:**
```http
GET /api/planning-masters/search?companyId=1&keyword=transportation HTTP/1.1
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "companyRefId": 1,
    "cNumberDisplay": "PL000000001",
    "remarks": "Weekly planning for transportation",
    "search": "transportation planning",
    "active": 1
  }
]
```

---

### 8. Get Planning by Employee

**Request:**
```http
GET /api/planning-masters/employee/2?companyId=1 HTTP/1.1
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "companyRefId": 1,
    "employeeRefId": 2,
    "cNumberDisplay": "PL000000001",
    "remarks": "Weekly planning",
    "active": 1
  }
]
```

---

## 🔵 Planning Details Endpoints

### 1. Create Planning Detail

**Request:**
```http
POST /api/planning-details HTTP/1.1
Content-Type: application/json

{
  "planningMasterRefId": 1,
  "saleOrderMasterRefId": 12,
  "truckRefId": 8,
  "remarks": "New delivery detail",
  "originD": "Warehouse E",
  "destinationD": "Store F",
  "pickupDateD": "2026-02-17T08:00:00",
  "deliveryDateD": "2026-02-17T18:00:00",
  "sortBy": 3,
  "truckNameD": "Truck-003",
  "driverNameD": "Mike Johnson"
}
```

**Response (201 Created):**
```json
{
  "id": 3,
  "planningMasterRefId": 1,
  "saleOrderMasterRefId": 12,
  "truckRefId": 8,
  "remarks": "New delivery detail",
  "createdDate": "2026-02-15T12:45:00",
  "modifiedDate": "2026-02-15T12:45:00",
  "originD": "Warehouse E",
  "destinationD": "Store F",
  "pickupDateD": "2026-02-17T08:00:00",
  "deliveryDateD": "2026-02-17T18:00:00",
  "sortBy": 3,
  "truckNameD": "Truck-003",
  "driverNameD": "Mike Johnson"
}
```

---

### 2. Get All Planning Details

**Request:**
```http
GET /api/planning-details HTTP/1.1
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "planningMasterRefId": 1,
    "saleOrderMasterRefId": 10,
    "truckNameD": "Truck-001",
    "sortBy": 1
  },
  {
    "id": 2,
    "planningMasterRefId": 1,
    "saleOrderMasterRefId": 11,
    "truckNameD": "Truck-002",
    "sortBy": 2
  }
]
```

---

### 3. Get Planning Detail by ID

**Request:**
```http
GET /api/planning-details/1 HTTP/1.1
```

**Response (200 OK):**
```json
{
  "id": 1,
  "planningMasterRefId": 1,
  "saleOrderMasterRefId": 10,
  "truckRefId": 5,
  "remarks": "First delivery route",
  "createdDate": "2026-02-15T12:00:00",
  "modifiedDate": "2026-02-15T12:00:00",
  "originD": "Warehouse A, Chennai",
  "destinationD": "Store B, Bangalore",
  "pickupDateD": "2026-02-15T08:00:00",
  "deliveryDateD": "2026-02-15T18:00:00",
  "sortBy": 1,
  "truckNameD": "Truck-001",
  "driverNameD": "John Doe"
}
```

---

### 4. Update Planning Detail

**Request:**
```http
PUT /api/planning-details/1 HTTP/1.1
Content-Type: application/json

{
  "planningMasterRefId": 1,
  "saleOrderMasterRefId": 10,
  "truckRefId": 9,
  "remarks": "Updated delivery route",
  "sortBy": 1
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "planningMasterRefId": 1,
  "saleOrderMasterRefId": 10,
  "truckRefId": 9,
  "remarks": "Updated delivery route",
  "modifiedDate": "2026-02-15T12:50:00",
  "sortBy": 1
}
```

---

### 5. Delete Planning Detail

**Request:**
```http
DELETE /api/planning-details/1 HTTP/1.1
```

**Response (204 No Content):**
```
[Empty body]
```

---

### 6. Get Details by Planning Master

**Request:**
```http
GET /api/planning-details/by-master/1 HTTP/1.1
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "planningMasterRefId": 1,
    "saleOrderMasterRefId": 10,
    "sortBy": 1
  },
  {
    "id": 2,
    "planningMasterRefId": 1,
    "saleOrderMasterRefId": 11,
    "sortBy": 2
  }
]
```

---

### 7. Get Details by Sale Order

**Request:**
```http
GET /api/planning-details/by-sale-order/10 HTTP/1.1
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "planningMasterRefId": 1,
    "saleOrderMasterRefId": 10,
    "truckNameD": "Truck-001"
  }
]
```

---

### 8. Get Details by Truck

**Request:**
```http
GET /api/planning-details/by-truck/5 HTTP/1.1
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "planningMasterRefId": 1,
    "truckRefId": 5,
    "truckNameD": "Truck-001"
  }
]
```

---

## ❌ Error Response Examples

### 404 Not Found

**Request:**
```http
GET /api/planning-masters/999 HTTP/1.1
```

**Response (404 Not Found):**
```json
{
  "timestamp": "2026-02-15T12:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Planning Master not found: 999",
  "path": "/api/planning-masters/999",
  "details": null
}
```

---

### 400 Bad Request (Validation Error)

**Request:**
```http
POST /api/planning-masters HTTP/1.1
Content-Type: application/json

{
  "userRefId": 1
  // Missing required fields: companyRefId, saleDate, fDate, tDate
}
```

**Response (400 Bad Request):**
```json
{
  "timestamp": "2026-02-15T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/planning-masters",
  "details": [
    "Company reference ID is required",
    "Sale date is required",
    "From date is required",
    "To date is required"
  ]
}
```

---

### 403 Forbidden (No Permission)

**Response (403 Forbidden):**
```json
{
  "timestamp": "2026-02-15T12:00:00Z",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied",
  "path": "/api/planning-masters",
  "details": null
}
```

---

## 📝 cURL Examples

### Create Planning
```bash
curl -X POST http://localhost:8082/api/planning-masters \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "companyRefId": 1,
    "employeeRefId": 2,
    "saleDate": "2026-02-15T10:30:00",
    "fDate": "2026-02-15T00:00:00",
    "tDate": "2026-02-28T23:59:59",
    "cNumberDisplay": "PL000000001",
    "cNumber": 1,
    "remarks": "Weekly planning",
    "active": 1
  }'
```

### Get Planning by ID
```bash
curl -X GET http://localhost:8082/api/planning-masters/1 \
  -H "Authorization: Bearer TOKEN"
```

### Update Planning
```bash
curl -X PUT http://localhost:8082/api/planning-masters/1 \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "companyRefId": 1,
    "remarks": "Updated planning",
    "active": 1
  }'
```

### Delete Planning
```bash
curl -X DELETE http://localhost:8082/api/planning-masters/1 \
  -H "Authorization: Bearer TOKEN"
```

### Search by Date Range
```bash
curl -X GET "http://localhost:8082/api/planning-masters/search/date-range?companyId=1&fromDate=2026-02-01T00:00:00&toDate=2026-02-28T23:59:59" \
  -H "Authorization: Bearer TOKEN"
```

---

## 📊 Response Summary

| Endpoint | Method | Status | Response Type |
|----------|--------|--------|---------------|
| /api/planning-masters | GET | 200 | Array of Planning |
| /api/planning-masters | POST | 201 | Single Planning |
| /api/planning-masters/{id} | GET | 200 | Single Planning |
| /api/planning-masters/{id} | PUT | 200 | Updated Planning |
| /api/planning-masters/{id} | DELETE | 204 | Empty |
| /api/planning-masters/search/* | GET | 200 | Array of Planning |
| /api/planning-details | GET | 200 | Array of Details |
| /api/planning-details | POST | 201 | Single Detail |
| /api/planning-details/{id} | GET | 200 | Single Detail |
| /api/planning-details/{id} | PUT | 200 | Updated Detail |
| /api/planning-details/{id} | DELETE | 204 | Empty |
| /api/planning-details/by-* | GET | 200 | Array of Details |


