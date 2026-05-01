# Sales Invoice API - Quick Reference

**Quick Links:**
- 📍 Base URL: `http://localhost:8082/api/v1/sale-invoices`
- 🔐 Authentication: JWT Bearer Token
- 📚 Full Documentation: docs/SALEINVOICE_API.md
- 📋 Implementation Details: docs/SALEINVOICE_IMPLEMENTATION.md

---

## 🚀 Endpoints at a Glance

### Get Next Invoice Number
```bash
GET /next-number?companyId=6

# Response
{
  "Data1": "INV000000001"
}
```

### Create Invoice
```bash
POST /

# Request
{
  "companyRefId": 6,
  "customerRefId": 66,
  "jobMasterRefId": 1,
  "saleDate": "2024-01-15T10:30:00Z",
  "amount": 10500
}
```

### Search Invoices
```bash
GET /search?companyId=6&customerId=66&page=0&size=20

# Optional filters
&fromDate=2024-01-01T00:00:00Z
&toDate=2024-01-31T23:59:59Z
&billType=INVOICE
&saleType=STANDARD
&search=INV000000001
```

### Get Invoice
```bash
GET /{id}?companyId=6
```

### Update Invoice
```bash
PUT /{id}?companyId=6

# Request
{
  "remarks": "Updated remarks"
}
```

### Delete Invoice
```bash
DELETE /{id}?companyId=6
```

### Push to QNE
```bash
POST /{id}/push-qne?companyId=6
```

### Get All Company Invoices
```bash
GET /company/{companyId}
```

### Get Customer Invoices
```bash
GET /company/{companyId}/customer/{customerId}
```

### Get Unpushed Invoices
```bash
GET /unpushed?companyId=6
```

### Get by Invoice Number
```bash
GET /by-cnumber?companyId=6&cNumber=INV000000001
```

---

## 🔑 Required Headers

```
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json (for POST/PUT)
```

---

## 📊 Common Status Codes

| Code | Meaning |
|------|---------|
| 200 | Success (GET, PUT) |
| 201 | Created (POST) |
| 204 | No Content (DELETE) |
| 400 | Bad Request |
| 403 | Forbidden (unauthorized) |
| 404 | Not Found |
| 409 | Conflict (business rule) |
| 500 | Server Error |

---

## 🧪 Test with cURL

### Get Next Number
```bash
curl -X GET "http://localhost:8082/api/v1/sale-invoices/next-number?companyId=6" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Create Invoice
```bash
curl -X POST "http://localhost:8082/api/v1/sale-invoices" \
  -H "Authorization: Bearer YOUR_TOKEN" \
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

### Search
```bash
curl -X GET "http://localhost:8082/api/v1/sale-invoices/search?companyId=6&page=0&size=20" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Details
```bash
curl -X GET "http://localhost:8082/api/v1/sale-invoices/123?companyId=6" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Update
```bash
curl -X PUT "http://localhost:8082/api/v1/sale-invoices/123?companyId=6" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "remarks": "Updated"
  }'
```

### Delete
```bash
curl -X DELETE "http://localhost:8082/api/v1/sale-invoices/123?companyId=6" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 💻 Using with Postman

1. **Import Collection:** Use the Postman collection in `postman/collections/`
2. **Set Environment:** Select appropriate environment (Local/Dev/Prod)
3. **Authentication:** Configure JWT token in collection variables
4. **Test Requests:** Use pre-configured requests in the collection

---

## 📝 Common Scenarios

### Scenario 1: Create Complete Invoice
```bash
# Step 1: Get next invoice number
GET /next-number?companyId=6

# Step 2: Create invoice with details
POST /
with full invoice object

# Step 3: Get created invoice
GET /123?companyId=6
```

### Scenario 2: Search and Update
```bash
# Step 1: Search invoices
GET /search?companyId=6&customerId=66

# Step 2: Get invoice details
GET /123?companyId=6

# Step 3: Update invoice
PUT /123?companyId=6
```

### Scenario 3: Push to QNE
```bash
# Step 1: Get unpushed invoices
GET /unpushed?companyId=6

# Step 2: Push to QNE
POST /123/push-qne?companyId=6

# Step 3: Verify push
GET /123?companyId=6
```

---

## ⚠️ Error Handling

### Business Rule Violations
```json
{
  "IsSuccess": false,
  "StatusCode": 409,
  "Message": "Cannot update completed invoice",
  "ErrorDetails": "Invoice jStatus = 8"
}
```

### Validation Errors
```json
{
  "IsSuccess": false,
  "StatusCode": 400,
  "Message": "Invalid company ID",
  "ErrorDetails": "Company ID must be positive"
}
```

### Not Found
```json
{
  "IsSuccess": false,
  "StatusCode": 404,
  "Message": "Invoice not found",
  "ErrorDetails": "No invoice with ID: 999"
}
```

---

## 🔍 Filter Combinations

### By Customer and Date Range
```bash
GET /search?companyId=6&customerId=66&fromDate=2024-01-01T00:00:00Z&toDate=2024-01-31T23:59:59Z
```

### By Bill Type and Sale Type
```bash
GET /search?companyId=6&billType=INVOICE&saleType=STANDARD
```

### By Search Term
```bash
GET /search?companyId=6&search=INV000000001
```

### Unpushed with Pagination
```bash
GET /unpushed?companyId=6&page=0&size=50
```

---

## 📌 Important Rules

### Cannot Update Invoices If:
- ❌ Job Status = 8 (Completed)
- ❌ QNE Code is not null (Already pushed)

### Cannot Delete Invoices If:
- ❌ Job Status = 8 (Completed)
- ❌ QNE Code is not null (Already pushed)

### For Creating Invoices:
- ✅ companyRefId is required
- ✅ customerRefId is required
- ✅ jobMasterRefId is required
- ✅ saleDate is required
- ✅ amount is required

---

## 🎯 Response Pagination

**Default:** 20 items per page, starting from page 0

```bash
# Get page 1 with 50 items per page
GET /search?companyId=6&page=0&size=50

# Response includes
"totalRecords": 150,
"totalPages": 3,
"currentPage": 0
```

---

## 🔄 Invoice Number Format

- Format: `INV{9-digit-number}`
- Example: `INV000000001`, `INV000000123`, `INV000010000`
- Auto-incrementing per company
- Unique per company

---

## 📚 Related Classes

| Class | Purpose |
|-------|---------|
| `SaleInvoiceController.java` | REST endpoints |
| `SaleMasterService.java` | Business logic interface |
| `SaleMasterServiceImpl.java` | Implementation |
| `SaleMasterRepository.java` | Database queries |
| `SaleMasterDto.java` | Data transfer object |
| `SaleDetailsDto.java` | Line item DTO |
| `ApiResponse<T>` | Response wrapper |

---

## 🚀 Running the Application

```bash
# Build
mvn clean compile

# Run (port 8082)
mvn spring-boot:run

# Access Swagger UI
http://localhost:8082/swagger-ui.html
```

---

## 💡 Tips & Tricks

1. **Always include companyId** for authorization
2. **Use pagination** for large result sets
3. **Check for QNE code** before trying to push
4. **Validate dates** are in ISO format
5. **Use search filters** for better performance
6. **Handle 409 Conflict** for business rule errors

---

## 📞 Debugging

### Enable Debug Logging
```yaml
logging:
  level:
    my.maleva.api.module.invoice: DEBUG
```

### Check Application Logs
```bash
tail -f logs/application.log
```

### Verify Port
```bash
netstat -an | grep 8082
```

### Test Database Connection
```bash
sqlcmd -S server -U sa -P password
```

---

## 📖 Related Documentation

- **Full API Docs:** docs/SALEINVOICE_API.md
- **Implementation Guide:** docs/SALEINVOICE_IMPLEMENTATION.md
- **Backend Migration Guide:** docs/Backend_Migration_Guide.md
- **Coding Standards:** docs/CODING_STANDARDS.md

---

**Last Updated:** April 29, 2026  
**Status:** ✅ Ready for Use

