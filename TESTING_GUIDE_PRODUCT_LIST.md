# Testing Guide: Get Product List API

## 🎯 Endpoint Information

**HTTP Method:** GET  
**Path:** `/api/item-masters/company/{companyRefId}/products`  
**Full URL:** `http://{{host}}/api/item-masters/company/6/products`  
**Authentication:** Required (JWT Bearer Token)

---

## ✅ Step-by-Step Testing Instructions

### Step 1: Ensure Your Server is Running
```bash
# Navigate to project directory
cd C:\karthickworkspace\malevanew\malevabackend\Maleva

# Build and run (Windows)
mvn clean package
java -jar target/api-0.0.1-SNAPSHOT.war
```

Expected output:
```
Application started on port 8082
```

### Step 2: Get JWT Token
First, you need to authenticate and get a JWT token.

**Request:**
```http
POST http://localhost:8082/api/login
Content-Type: application/json

{
    "username": "admin",
    "password": "admin_password"
}
```

**Response (Example):**
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcwODYxODI3NCwiZXhwIjoxNzA4NjIxODc0fQ.xyz..."
}
```

**Copy the token value** - you'll use this in the next step.

### Step 3: Call the Get Product List API

**Request:**
```http
GET http://localhost:8082/api/item-masters/company/6/products
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcwODYxODI3NCwiZXhwIjoxNzA4NjIxODc0fQ.xyz...
Content-Type: application/json
```

**Success Response (200 OK):**
```json
[
    {
        "id": 1,
        "productName": "Product A",
        "saleRate": 1500.00,
        "purRate": 1000.00,
        "mrp": 1800.00,
        "productCode": "PA001"
    },
    {
        "id": 2,
        "productName": "Product B",
        "saleRate": 2500.00,
        "purRate": 1800.00,
        "mrp": 3000.00,
        "productCode": "PB002"
    },
    {
        "id": 3,
        "productName": "Product C",
        "saleRate": 500.00,
        "purRate": 300.00,
        "mrp": 600.00,
        "productCode": "PC003"
    }
]
```

---

## 🔧 Troubleshooting

### ❌ Error: "No static resource api/item-masters/company/6/products"
**Cause:** You have the original code with class-level `@PreAuthorize`  
**Solution:** The fix has been applied. Rebuild and redeploy:
```bash
mvn clean package
```

### ❌ Error: 401 Unauthorized
**Cause:** Missing or invalid JWT token  
**Solution:** 
1. Make sure you added the `Authorization` header
2. Get a fresh token using the login endpoint
3. Format: `Authorization: Bearer YOUR_TOKEN`

### ❌ Error: 403 Forbidden
**Cause:** Your user doesn't have the required role  
**Solution:** 
- Use a user with `ROLE_SUPERADMIN`, `ROLE_ADMIN`, or `ROLE_100`
- Check your user roles in the database

### ❌ Error: 404 Not Found - Company Not Found
**Cause:** Company ID 6 doesn't exist  
**Solution:** 
- Replace `6` with a valid company ID
- Check your companies table to find valid IDs

### ❌ Error: Empty Array Response
**Cause:** No active items for this company  
**Solution:** 
- This is normal - add some items to the company first
- Or use a different company ID that has items

---

## 🧪 Using curl (Command Line)

### Get Token:
```bash
curl -X POST "http://localhost:8082/api/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin\",\"password\":\"admin_password\"}"
```

### Get Product List:
```bash
curl -X GET "http://localhost:8082/api/item-masters/company/6/products" ^
  -H "Authorization: Bearer YOUR_TOKEN_HERE" ^
  -H "Content-Type: application/json"
```

---

## 📌 Using Postman

### Option A: Using Environment Variables
1. Set `{{host}}` = `localhost:8082`
2. Set `{{jwt_token}}` = your token from login

### Option B: Manual Testing
1. **Create new request**
   - Method: GET
   - URL: `http://localhost:8082/api/item-masters/company/6/products`

2. **Add Authorization Header**
   - Go to "Headers" tab
   - Add: `Authorization` = `Bearer YOUR_JWT_TOKEN`

3. **Send Request**
   - Click "Send"
   - Expected: 200 OK with product list

---

## ✨ What the API Returns

| Field | Type | Description |
|-------|------|-------------|
| id | Integer | Product ID |
| productName | String | Name of the product |
| saleRate | Decimal | Sale rate/price |
| purRate | Decimal | Purchase rate/cost |
| mrp | Decimal | Maximum Retail Price |
| productCode | String | Product code/SKU |

---

## 🔐 Security Requirements

- ✅ JWT Bearer Token required
- ✅ User must have one of these roles: `ROLE_SUPERADMIN`, `ROLE_ADMIN`, `ROLE_100`
- ✅ Token must be valid and not expired
- ✅ Token must be in format: `Authorization: Bearer <token>`

---

## 📝 Notes

- The endpoint returns **only active items** for the company
- Results are **sorted by product name**
- Empty array means no active items for that company
- Always include the JWT token in the Authorization header

---

**Last Updated:** February 23, 2026  
**Status:** ✅ Ready for Testing

