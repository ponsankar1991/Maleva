# Maleva API Postman Collection - Quick Guide

## ✅ All Employee APIs Now in Main Collection

The Employee API endpoints have been merged into the main **Maleva API.postman_collection.json** file.

---

## 🎯 What's New in the Collection

### Employees Section (6 New Requests)
All employee-related API calls are grouped under "Employees -" naming:

```
📦 Maleva API (Collection)
   │
   ├─ Authentication
   │  ├─ /api/login
   │  └─ logout
   │
   ├─ Customers
   │  ├─ Get customer
   │  ├─ Create customer
   │  ├─ update customer
   │  └─ Get customer select
   │
   ├─ Users
   │  └─ user register
   │
   ├─ Agents
   │  └─ Agent Select All
   │
   └─ Employees (NEW! 6 requests)
      ├─ ✨ Employees - Get All Active Employees for Company
      ├─ ✨ Employees - Get SALES Employees (Auto-includes TRANSPORTATION)
      ├─ ✨ Employees - Get MANAGER Employees
      ├─ ✨ Employees - Get ADMIN Employees
      ├─ ✨ Employees - Get Multiple Role Types (SALES + MANAGER)
      └─ ✨ Employees - Get SALES and ADMIN Employees
```

---

## 🚀 How to Import and Use

### Step 1: Import the Collection
```
Postman → File → Import
→ Select: Maleva API.postman_collection.json
→ Click Import
```

### Step 2: Set Up Variables
```
Collection → Variables (gear icon)
Set: host = your_server (e.g., localhost:8080)
Set: token = (leave empty, get it from login)
```

### Step 3: Login to Get Token
```
1. Find: "/api/login" request
2. Click Send
3. Token automatically saved
```

### Step 4: Test Employee API
```
1. Scroll to "Employees -" requests
2. Click any request
3. Modify company ID in URL if needed
4. Click Send
5. View response
```

---

## 📊 Employee Requests Reference

### Request 1: All Employees
```
GET /api/employees/company/1/roles

No query parameters needed
Returns: All active employees for company 1
```

### Request 2: SALES Employees
```
GET /api/employees/company/1/roles?type=SALES

Query: type=SALES
Returns: SALES + TRANSPORTATION employees
Note: TRANSPORTATION auto-included
```

### Request 3: MANAGER Employees
```
GET /api/employees/company/1/roles?type=MANAGER

Query: type=MANAGER
Returns: MANAGER employees only
```

### Request 4: ADMIN Employees
```
GET /api/employees/company/1/roles?type=ADMIN

Query: type=ADMIN
Returns: ADMIN employees only
```

### Request 5: Multiple Roles
```
GET /api/employees/company/1/roles?type=SALES&type1=MANAGER

Queries: type=SALES, type1=MANAGER
Returns: SALES + TRANSPORTATION + MANAGER employees
```

### Request 6: SALES & ADMIN
```
GET /api/employees/company/1/roles?type=SALES&type1=ADMIN

Queries: type=SALES, type1=ADMIN
Returns: SALES + TRANSPORTATION + ADMIN employees
```

---

## ⚙️ Collection Variables

| Variable | Current Value | Purpose |
|----------|---------------|---------|
| `token` | (empty) | Auth token (auto-filled by login) |
| (host) | - | Server address (optional, see URLs) |

---

## 🔐 Authentication

All Employee API requests use **Bearer Token** authentication:
```json
{
  "auth": {
    "type": "bearer",
    "bearer": [
      {
        "key": "token",
        "value": "{{token}}",
        "type": "string"
      }
    ]
  }
}
```

**To get token:**
1. Send the login request: `/api/login`
2. Token is automatically extracted and stored
3. All subsequent requests use this token

---

## 📝 Example Response

### Request:
```
GET /api/employees/company/1/roles?type=SALES
Authorization: Bearer {{token}}
```

### Response (HTTP 200):
```json
[
  {
    "id": 1,
    "companyRefId": 1,
    "employeeName": "John Doe",
    "employeeType": "SALES",
    "userName": "johndoe",
    "password": "$2b$10$...",
    "active": 1,
    "email": "john@company.com",
    "mobileNo": "1234567890",
    "address1": "...",
    "createdDate": "2024-01-15T10:30:00",
    "modifiedDate": "2024-01-15T10:30:00"
  },
  {
    "id": 5,
    "companyRefId": 1,
    "employeeName": "Jane Smith",
    "employeeType": "TRANSPORTATION",
    "userName": "janesmith",
    ...
  }
]
```

---

## ✨ Key Features

✅ **Easy Testing** - Pre-configured requests ready to send
✅ **Clear Naming** - Request names clearly describe what they do
✅ **Auto-Auth** - Token automatically handled after login
✅ **Documented** - Each request has detailed description
✅ **Consistent** - Follows same pattern as other Maleva API requests
✅ **Flexible** - Easy to modify parameters

---

## 🎓 Collection Statistics

| Metric | Count |
|--------|-------|
| Total Requests | 14 |
| Employee API Requests | 6 |
| Authentication Methods | Bearer Token |
| Query Parameters | Optional |
| Path Parameters | Required (Company ID) |

---

## 🔍 Finding the Requests

In Postman, search for:
```
"Employees -"
```

This will highlight all 6 new employee API requests.

---

## 💡 Tips & Tricks

### Tip 1: Change Company ID
Edit the URL in any request:
```
Before: /api/employees/company/1/roles
After:  /api/employees/company/5/roles
```

### Tip 2: Customize Parameters
Click "Params" tab and edit query parameters:
```
type: SALES
type1: MANAGER
```

### Tip 3: Save Responses
Use Postman's "Save Response" feature to save results for analysis.

### Tip 4: Run Multiple Requests
Use Postman Runner to execute multiple requests in sequence.

### Tip 5: Set Environment Variables
Create an environment with different host values for dev/staging/prod.

---

## ✅ Verification Checklist

- [x] All 6 employee requests added to collection
- [x] Requests follow Postman v2.1.0 format
- [x] Bearer token authentication configured
- [x] URLs properly formatted with variables
- [x] Query parameters documented
- [x] Descriptions added for each request
- [x] JSON file is valid
- [x] Ready to import and use

---

## 📞 Support

| Question | Answer |
|----------|--------|
| "How do I import?" | File → Import → Select the JSON file |
| "How do I set variables?" | Collection → Variables (gear icon) |
| "How do I get token?" | Send /api/login request first |
| "How do I modify parameters?" | Click "Params" tab in the request |
| "Where are employee requests?" | Search for "Employees -" or scroll down |

---

## 🎯 Next Steps

1. ✅ Import the Maleva API collection into Postman
2. ✅ Set the {{host}} variable to your server
3. ✅ Send the login request to get a token
4. ✅ Find and send any "Employees -" request
5. ✅ View the response with active employees
6. ✅ Integrate into your frontend application

---

**Status:** ✅ Ready to Use
**All Endpoints:** Included in Maleva API collection
**No Separate Files:** Everything in one collection
**Production Ready:** Yes

---

Import now and start testing! 🚀

