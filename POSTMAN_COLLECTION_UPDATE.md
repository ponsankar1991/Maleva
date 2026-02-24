# Employee API - Postman Collection Update Summary

## ✅ Update Complete

All Employee API calls have been **merged into the existing Maleva API.postman_collection.json** file. The separate Employee_Filter_API.postman_collection.json file has been removed.

---

## 📋 What Was Added to Maleva API Collection

The following 6 Employee API endpoints are now included in the main Postman collection:

### 1. **Employees - Get All Active Employees for Company**
- **ID:** `emp-001-get-all`
- **Method:** GET
- **Endpoint:** `/api/employees/company/{companyRefId}/roles`
- **Parameters:** Company ID (in URL path)
- **Description:** Get all active employees for a specific company

### 2. **Employees - Get SALES Employees (Auto-includes TRANSPORTATION)**
- **ID:** `emp-002-get-sales`
- **Method:** GET
- **Endpoint:** `/api/employees/company/{companyRefId}/roles?type=SALES`
- **Parameters:** Company ID (path) + type=SALES (query)
- **Description:** Get SALES employees (automatically includes TRANSPORTATION)

### 3. **Employees - Get MANAGER Employees**
- **ID:** `emp-003-get-manager`
- **Method:** GET
- **Endpoint:** `/api/employees/company/{companyRefId}/roles?type=MANAGER`
- **Parameters:** Company ID (path) + type=MANAGER (query)
- **Description:** Get only MANAGER employees

### 4. **Employees - Get ADMIN Employees**
- **ID:** `emp-004-get-admin`
- **Method:** GET
- **Endpoint:** `/api/employees/company/{companyRefId}/roles?type=ADMIN`
- **Parameters:** Company ID (path) + type=ADMIN (query)
- **Description:** Get only ADMIN employees

### 5. **Employees - Get Multiple Role Types (SALES + MANAGER)**
- **ID:** `emp-005-get-multi`
- **Method:** GET
- **Endpoint:** `/api/employees/company/{companyRefId}/roles?type=SALES&type1=MANAGER`
- **Parameters:** Company ID (path) + type=SALES + type1=MANAGER (queries)
- **Description:** Get multiple role types (SALES + auto-included TRANSPORTATION + MANAGER)

### 6. **Employees - Get SALES and ADMIN Employees**
- **ID:** `emp-006-get-sales-admin`
- **Method:** GET
- **Endpoint:** `/api/employees/company/{companyRefId}/roles?type=SALES&type1=ADMIN`
- **Parameters:** Company ID (path) + type=SALES + type1=ADMIN (queries)
- **Description:** Get SALES (with auto-included TRANSPORTATION) and ADMIN employees

---

## 🔧 How to Use

### In Postman

1. **Open Postman**
2. **Import Collection:** 
   - File → Import
   - Select: `Maleva API.postman_collection.json`
3. **Set Environment Variables:**
   - Edit collection variables
   - Set `host` = your server (e.g., `localhost:8080`)
   - Set `token` = your authentication token (optional, or login first)
4. **Find Employee Endpoints:**
   - Look for requests starting with "Employees -" in the collection
   - All 6 new requests are grouped together
5. **Send Requests:**
   - Click on any Employee API request
   - Click Send
   - View the response

---

## 📊 Collection Structure

```
Maleva API (Collection)
├── /api/login
├── logout
├── Get customer
├── Create customer
├── update customer
├── user register
├── employees
├── Get customer select
├── Agent Select All
├── Employees - Get All Active Employees for Company
├── Employees - Get SALES Employees (Auto-includes TRANSPORTATION)
├── Employees - Get MANAGER Employees
├── Employees - Get ADMIN Employees
├── Employees - Get Multiple Role Types (SALES + MANAGER)
└── Employees - Get SALES and ADMIN Employees
```

---

## 🎯 Key Features

✅ **All in One File** - No separate collection files needed
✅ **Consistent Format** - Matches existing Maleva API structure
✅ **Clear Naming** - Each endpoint clearly labeled with what it does
✅ **Pre-configured** - URLs with {{host}} variable already set
✅ **Bearer Token** - All requests use existing token authentication
✅ **Well Documented** - Each request has description of parameters
✅ **Easy to Test** - Just update company ID and send

---

## 🚀 Quick Start

```
1. Import "Maleva API.postman_collection.json" into Postman
2. Set {{host}} variable (e.g., localhost:8080)
3. Run login request to get {{token}}
4. Scroll down to find "Employees -" requests
5. Send any request to test
6. See results in response body
```

---

## 📝 Testing the Employees API

### Test 1: Get All Employees
```
Request: Employees - Get All Active Employees for Company
Expected: Array of all active employees from company 1
```

### Test 2: Get SALES Employees
```
Request: Employees - Get SALES Employees (Auto-includes TRANSPORTATION)
Expected: Array of SALES + TRANSPORTATION employees
```

### Test 3: Get Specific Role
```
Request: Employees - Get MANAGER Employees
Expected: Array of MANAGER employees only
```

### Test 4: Get Multiple Roles
```
Request: Employees - Get Multiple Role Types (SALES + MANAGER)
Expected: Array of SALES + TRANSPORTATION + MANAGER employees
```

---

## 📋 Environment Variables Used

| Variable | Purpose | Example |
|----------|---------|---------|
| `{{host}}` | Server address | localhost:8080 |
| `{{token}}` | Authentication token | JWT token from login |

---

## ✨ What Changed

### Before
- ❌ Separate Postman collection file for Employee API
- ❌ Two different collection files to manage
- ❌ Inconsistent structure

### After
- ✅ All API endpoints in one collection
- ✅ Single Postman collection file to maintain
- ✅ Consistent structure across all endpoints
- ✅ Easy to navigate and manage
- ✅ Matches project workflow

---

## 🎓 Collection File Details

| Property | Value |
|----------|-------|
| File Name | Maleva API.postman_collection.json |
| Location | postman/collections/ |
| Format | Postman Collection v2.1.0 |
| Total Requests | 14 (8 existing + 6 new Employee requests) |
| Authentication | Bearer Token |

---

## ✅ Verification

- [x] All 6 Employee API requests added
- [x] Requests follow existing Postman format
- [x] Bearer token authentication configured
- [x] URLs use {{host}} variable
- [x] Descriptions added for each request
- [x] Query parameters documented
- [x] JSON file is valid and properly formatted
- [x] Separate collection file deleted
- [x] Ready to import and use

---

## 📞 Usage Notes

1. **Update Company ID** - Change the "1" in URLs to your desired company ID
2. **Auto-Include Logic** - TRANSPORTATION is automatically added when SALES is selected
3. **Active Filter** - Only Active=1 employees are returned
4. **Sorting** - Results are sorted by employee name (A-Z)
5. **Token Required** - Must be logged in (use the login endpoint first)

---

## 🔗 File Locations

- **Updated Collection:** `postman/collections/Maleva API.postman_collection.json`
- **Documentation:** See `EMPLOYEE_API_GUIDE.md` and related docs
- **Implementation:** See Java files in `src/main/java/my/maleva/api/`

---

## ✨ Summary

✅ **Status:** Postman collection updated successfully
✅ **All endpoints:** Ready to test
✅ **Documentation:** Comprehensive guides included
✅ **Implementation:** Java backend ready
✅ **Ready for:** Immediate testing and integration

---

**Date:** February 23, 2026
**Status:** ✅ COMPLETE

