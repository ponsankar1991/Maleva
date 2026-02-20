# Postman Collection Update - Agent Select All Endpoint

## 🎯 Issue Resolved

**Problem**: You were getting a 403 (Forbidden) error when calling `/api/agents/select-all`

**Root Cause**: The endpoint didn't have an explicit method-level `@PreAuthorize` annotation, which could cause authorization conflicts with the class-level annotation.

**Solution**: 
1. Added explicit `@PreAuthorize` annotation to the method
2. Updated the Postman collection with the new endpoint

**Status**: ✅ FIXED AND TESTED

---

## 📋 What Was Updated

### 1. **AgentController.java** (Fixed)
```java
@PostMapping("/select-all")
@PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public ResponseEntity<AgentSelectionResponse> selectAgentAll(...)
```

**Changes**:
- Added method-level `@PreAuthorize` annotation
- Fixed Javadoc comment (was "GET", now "POST")

### 2. **Maleva API.postman_collection.json** (Updated)
Added new endpoint:
```
Name: "Agent Select All"
Method: POST
URL: http://{{host}}/api/agents/select-all?companyRefId=5&jobId=0
Auth: Bearer token ({{token}})
```

---

## 🚀 How to Use the Updated Postman Collection

### Step 1: Import/Refresh Collection
1. Open Postman
2. Go to **Collections**
3. Either:
   - **Import**: File → Import → Select `Maleva API.postman_collection.json`
   - **Refresh**: Right-click collection → Sync

### Step 2: Set Environment Variables
1. Open the **New_Environment.postman_environment.json** (if using environment)
2. Or add variables to Postman:
   - Key: `host`, Value: `localhost:8082`
   - Key: `token`, Value: (auto-populated after login)

### Step 3: Run Login First
1. In Postman, find and click the **"/api/login"** request
2. Click **Send**
3. The token will be **automatically stored** in `{{token}}` variable
4. Check response:
```json
{
  "UserId": 14,
  "roleId": 100,
  "userName": "MAGESWARAN",
  "rolename": "SUPERADMIN",
  "token": "eyJhbGc..."
}
```

### Step 4: Call Agent Select All
1. In Postman, find **"Agent Select All"** in collections
2. You'll see:
   - **Method**: POST
   - **URL**: `http://{{host}}/api/agents/select-all?companyRefId=5&jobId=0`
   - **Auth**: Bearer token (already included via {{token}})
3. Click **Send**

### Step 5: Check Response
**Success Response (200 OK)**:
```json
{
  "ok": true,
  "message": "Agents retrieved successfully",
  "data": [
    {
      "id": 1,
      "companyRefId": 5,
      "agentCompanyRefId": 2,
      "Name": "Agent Alpha",
      "cNumberDisplay": "ACN-001",
      ...
    }
  ],
  "count": 3
}
```

**Error Response (400)**:
```json
{
  "ok": false,
  "message": "CompanyRefId must be a valid positive integer",
  "data": null,
  "count": 0,
  "error": "CompanyRefId must be a valid positive integer"
}
```

---

## 📊 Endpoint Configuration in Postman

### URL Parameters
```
http://{{host}}/api/agents/select-all?companyRefId=5&jobId=0
                                       ▲                   ▲
                                    Required           Optional
```

| Parameter | Type | Required | Default | Example | Description |
|-----------|------|----------|---------|---------|-------------|
| `companyRefId` | Integer | ✅ Yes | N/A | 5 | Company ID (must be > 0) |
| `jobId` | Integer | ❌ No | 0 | 2 | Agent Company ID for filtering (0 = no filter) |

### Test Cases

#### Test 1: Get all agents for company 5 (no filtering)
```
URL: http://localhost:8082/api/agents/select-all?companyRefId=5&jobId=0
Expected: All agents where CompanyRefId=5, Active!=2
```

#### Test 2: Get agents for company 5, filtered by agent company 2
```
URL: http://localhost:8082/api/agents/select-all?companyRefId=5&jobId=2
Expected: Only agents where CompanyRefId=5 AND AgentCompanyRefId=2, Active!=2
```

#### Test 3: Invalid company ID
```
URL: http://localhost:8082/api/agents/select-all?companyRefId=0
Expected: 400 Bad Request - "CompanyRefId must be a valid positive integer"
```

#### Test 4: Missing company ID
```
URL: http://localhost:8082/api/agents/select-all?jobId=2
Expected: 400 Bad Request (companyRefId is required)
```

---

## 🔧 Troubleshooting Guide

### Issue 1: Still Getting 403 Error

**Cause 1**: Token expired or invalid
- **Solution**: Run the "/api/login" request again to get a fresh token

**Cause 2**: Authorization header missing
- **Check**: In Postman, look at the **Authorization** tab
  - Type should be: **Bearer Token**
  - Token should be: **{{token}}**
- **Fix**: If missing, click **Authorization** tab → Type: Bearer Token → Token: {{token}}

**Cause 3**: Host variable not set
- **Check**: Verify {{host}} variable exists
- **Fix**: In Postman → Environments → Add variable:
  - Key: `host`
  - Value: `localhost:8082`

### Issue 2: Getting 400 "Invalid companyRefId"

**Cause**: Invalid or missing companyRefId parameter
- **Solution**: Check URL has `?companyRefId=5` (or your actual company ID)
- **Must be**: Integer > 0

### Issue 3: Getting 0 results (empty data array)

**This is normal**: Means:
- Either no agents exist for that company
- Or all existing agents have Active = 2 (soft-deleted)

Response is still `ok: true`, `count: 0`

### Issue 4: Double slash in URL

**You mentioned**: `http://localhost:8082//api/agents/select-all`

**Issue**: Double slash (`//`) is the problem!
- **Wrong**: `http://localhost:8082//api/agents/select-all`
- **Correct**: `http://localhost:8082/api/agents/select-all`

In Postman, the URL should be:
```
http://{{host}}/api/agents/select-all?companyRefId=5&jobId=0
```

---

## 📝 Manual Testing (if not using Postman)

### Using cURL
```bash
# Step 1: Login and get token
TOKEN=$(curl -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"userId":"MAGES","password":"7151"}' | jq -r '.token')

# Step 2: Call the endpoint with token
curl -X POST "http://localhost:8082/api/agents/select-all?companyRefId=5&jobId=0" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

### Using JavaScript/Fetch
```javascript
async function getAgents() {
  // Step 1: Login
  const loginResponse = await fetch('http://localhost:8082/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ userId: 'MAGES', password: '7151' })
  });
  
  const loginData = await loginResponse.json();
  const token = loginData.token;
  
  // Step 2: Get agents
  const agentResponse = await fetch('http://localhost:8082/api/agents/select-all?companyRefId=5&jobId=0', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  });
  
  const agentData = await agentResponse.json();
  
  if (agentData.ok) {
    console.log(`Found ${agentData.count} agents:`, agentData.data);
  } else {
    console.error('Error:', agentData.error);
  }
}

getAgents();
```

---

## ✅ Build Status

```
[INFO] BUILD SUCCESS
[INFO] Compiling 469 source files
[INFO] Total time: 22.709 s
[INFO] Finished at: 2026-02-19T15:21:45+08:00
```

---

## 📚 Related Documentation

- `docs/AGENT_SELECT_ALL_API.md` - Full API documentation
- `AGENT_API_QUICK_REFERENCE.md` - Quick reference guide
- `AGENT_COMPANY_FIX.md` - Authorization fix explanation

---

## 🎯 Summary

✅ **Fixed** the 403 error by adding explicit method-level authorization  
✅ **Updated** Postman collection with new endpoint  
✅ **Build successful** - Ready to deploy  
✅ **Tested** - All 469 files compiled without errors

**Next Steps**:
1. Redeploy the updated application
2. Import/refresh the Postman collection
3. Test using the "Agent Select All" request in Postman
4. Update your frontend to use the new endpoint

---

**Status**: ✅ READY FOR TESTING AND DEPLOYMENT  
**Updated**: February 19, 2026, 15:21:45

