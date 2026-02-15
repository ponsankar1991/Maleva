# Port Master API - Request/Response Examples

## Base Configuration

**Base URL**: `http://localhost:8082`
**Port**: 8082 (configured in application.yaml)
**Security**: Requires JWT token with ROLE_ADMIN, ROLE_SUPRERADMIN, or ROLE_100

---

## 1. Create Port Record

### Request
```bash
curl -X POST http://localhost:8082/api/port-masters \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "companyRefId": 1,
    "portName": "Port of Shanghai",
    "active": 1,
    "modifiedBy": "admin"
  }'
```

### Response (201 Created)
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

---

## 2. Get All Ports

### Request
```bash
curl -X GET http://localhost:8082/api/port-masters \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Response (200 OK)
```json
[
  {
    "id": 1,
    "companyRefId": 1,
    "portName": "Port of Shanghai",
    "active": 1,
    "createdDate": "2026-02-15T12:00:00",
    "modifiedDate": "2026-02-15T12:00:00",
    "modifiedBy": "admin"
  },
  {
    "id": 2,
    "companyRefId": 1,
    "portName": "Port of Singapore",
    "active": 1,
    "createdDate": "2026-02-15T12:05:00",
    "modifiedDate": "2026-02-15T12:05:00",
    "modifiedBy": "admin"
  }
]
```

---

## 3. Get Port by ID

### Request
```bash
curl -X GET http://localhost:8082/api/port-masters/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Response (200 OK)
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

---

## 4. Update Port

### Request
```bash
curl -X PUT http://localhost:8082/api/port-masters/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "portName": "Port of Shanghai - Updated",
    "active": 1,
    "modifiedBy": "admin"
  }'
```

### Response (200 OK)
```json
{
  "id": 1,
  "companyRefId": 1,
  "portName": "Port of Shanghai - Updated",
  "active": 1,
  "createdDate": "2026-02-15T12:00:00",
  "modifiedDate": "2026-02-15T12:30:00",
  "modifiedBy": "admin"
}
```

---

## 5. Batch Create/Update (SP_PortMaster Logic)

### Request
```bash
curl -X POST "http://localhost:8082/api/port-masters/batch?companyId=1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '[
    {
      "id": 0,
      "portName": "Port of Mumbai",
      "active": 1,
      "modifiedBy": "admin"
    },
    {
      "id": 0,
      "portName": "Port of Dubai",
      "active": 1,
      "modifiedBy": "admin"
    },
    {
      "id": 1,
      "portName": "Port of Shanghai - Batch Updated",
      "active": 1,
      "modifiedBy": "admin"
    }
  ]'
```

### Response (201 Created)
```json
[
  {
    "id": 3,
    "companyRefId": 1,
    "portName": "Port of Mumbai",
    "active": 1,
    "createdDate": "2026-02-15T12:45:00",
    "modifiedDate": "2026-02-15T12:45:00",
    "modifiedBy": "admin"
  },
  {
    "id": 4,
    "companyRefId": 1,
    "portName": "Port of Dubai",
    "active": 1,
    "createdDate": "2026-02-15T12:45:00",
    "modifiedDate": "2026-02-15T12:45:00",
    "modifiedBy": "admin"
  },
  {
    "id": 1,
    "companyRefId": 1,
    "portName": "Port of Shanghai - Batch Updated",
    "active": 1,
    "createdDate": "2026-02-15T12:00:00",
    "modifiedDate": "2026-02-15T12:45:00",
    "modifiedBy": "admin"
  }
]
```

---

## 6. Delete Port

### Request
```bash
curl -X DELETE http://localhost:8082/api/port-masters/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Response (204 No Content)
```
[Empty body]
```

---

## 7. Get Ports by Company

### Request
```bash
curl -X GET http://localhost:8082/api/port-masters/company/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Response (200 OK)
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

---

## 8. Get Active Ports by Company

### Request
```bash
curl -X GET http://localhost:8082/api/port-masters/company/1/active \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Response (200 OK)
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

---

## 9. Search Port by Name

### Request
```bash
curl -X GET "http://localhost:8082/api/port-masters/search?companyId=1&portName=shanghai" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Response (200 OK)
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

---

## 10. Soft Delete (Set Active=2)

### Request
```bash
curl -X DELETE http://localhost:8082/api/port-masters/1/soft \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Response (204 No Content)
```
[Empty body]
```

---

## 11. Activate Port

### Request
```bash
curl -X POST http://localhost:8082/api/port-masters/1/activate \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Response (200 OK)
```json
{
  "id": 1,
  "companyRefId": 1,
  "portName": "Port of Shanghai",
  "active": 1,
  "createdDate": "2026-02-15T12:00:00",
  "modifiedDate": "2026-02-15T13:00:00",
  "modifiedBy": "SYSTEM"
}
```

---

## 12. Deactivate Port

### Request
```bash
curl -X POST http://localhost:8082/api/port-masters/1/deactivate \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Response (200 OK)
```json
{
  "id": 1,
  "companyRefId": 1,
  "portName": "Port of Shanghai",
  "active": 0,
  "createdDate": "2026-02-15T12:00:00",
  "modifiedDate": "2026-02-15T13:15:00",
  "modifiedBy": "SYSTEM"
}
```

---

## Error Responses

### 400 Bad Request (Missing Required Field)

**Request**:
```bash
curl -X POST http://localhost:8082/api/port-masters \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "active": 1
  }'
```

**Response**:
```json
{
  "timestamp": "2026-02-15T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/port-masters",
  "details": [
    "Company reference ID is required",
    "Port name is required"
  ]
}
```

### 400 Bad Request (Duplicate Port Name)

**Response**:
```json
{
  "timestamp": "2026-02-15T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Port name 'Port of Shanghai' already exists for this company",
  "path": "/api/port-masters",
  "details": null
}
```

### 404 Not Found

**Request**:
```bash
curl -X GET http://localhost:8082/api/port-masters/999 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response**:
```json
{
  "timestamp": "2026-02-15T12:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Port Master not found: 999",
  "path": "/api/port-masters/999",
  "details": null
}
```

### 403 Forbidden (Insufficient Permissions)

**Response**:
```json
{
  "timestamp": "2026-02-15T12:00:00Z",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied",
  "path": "/api/port-masters",
  "details": null
}
```

---

## Postman Collection

### Import URL
Use these endpoints in Postman with the JWT token in Authorization header.

### Variables
```
baseUrl = http://localhost:8082
token = YOUR_JWT_TOKEN
companyId = 1
portId = 1
```

### Requests

| Name | Method | URL | Body |
|------|--------|-----|------|
| List All Ports | GET | {{baseUrl}}/api/port-masters | - |
| Get Port by ID | GET | {{baseUrl}}/api/port-masters/{{portId}} | - |
| Create Port | POST | {{baseUrl}}/api/port-masters | ✓ |
| Update Port | PUT | {{baseUrl}}/api/port-masters/{{portId}} | ✓ |
| Delete Port | DELETE | {{baseUrl}}/api/port-masters/{{portId}} | - |
| Batch Create | POST | {{baseUrl}}/api/port-masters/batch?companyId={{companyId}} | ✓ |
| Get by Company | GET | {{baseUrl}}/api/port-masters/company/{{companyId}} | - |
| Get Active Only | GET | {{baseUrl}}/api/port-masters/company/{{companyId}}/active | - |
| Search | GET | {{baseUrl}}/api/port-masters/search?companyId={{companyId}}&portName=shanghai | - |
| Soft Delete | DELETE | {{baseUrl}}/api/port-masters/{{portId}}/soft | - |
| Activate | POST | {{baseUrl}}/api/port-masters/{{portId}}/activate | - |
| Deactivate | POST | {{baseUrl}}/api/port-masters/{{portId}}/deactivate | - |

---

## Response Summary

| Endpoint | Method | Status | Response Type |
|----------|--------|--------|---------------|
| /api/port-masters | GET | 200 | Array |
| /api/port-masters | POST | 201 | Object |
| /api/port-masters/{id} | GET | 200 | Object |
| /api/port-masters/{id} | PUT | 200 | Object |
| /api/port-masters/{id} | DELETE | 204 | Empty |
| /api/port-masters/batch | POST | 201 | Array |
| /api/port-masters/company/{id} | GET | 200 | Array |
| /api/port-masters/company/{id}/active | GET | 200 | Array |
| /api/port-masters/search | GET | 200 | Array |
| /api/port-masters/{id}/soft | DELETE | 204 | Empty |
| /api/port-masters/{id}/activate | POST | 200 | Object |
| /api/port-masters/{id}/deactivate | POST | 200 | Object |


