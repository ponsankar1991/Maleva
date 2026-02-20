# Postman Configuration - CORRECT SETTINGS

## Agent Select All Endpoint - CORRECT CONFIGURATION

### URL Configuration
```
Method: POST
URL: http://localhost:8082/api/agents/select-all
                         ^
                    SINGLE SLASH!
```

### Query Parameters
| Key | Value | Description |
|-----|-------|-------------|
| companyRefId | 5 | Company ID (required, > 0) |
| jobId | 0 | Agent Company filter (optional, 0 = no filter) |

### Full URL
```
http://localhost:8082/api/agents/select-all?companyRefId=5&jobId=0
```

---

## Authentication Setup

### Authorization Tab
```
Type: Bearer Token
Token: {{token}}
```

After running `/api/login`, the {{token}} variable is automatically populated.

---

## Headers
```
Content-Type: application/json
```

---

## Complete Postman Request Configuration

### Tab: Params
| Key | Value |
|-----|-------|
| companyRefId | 5 |
| jobId | 0 |

### Tab: Authorization
| Field | Value |
|-------|-------|
| Type | Bearer Token |
| Token | {{token}} |

### Tab: Headers
| Key | Value |
|-----|-------|
| Content-Type | application/json |

### Tab: Body
Leave **EMPTY** (no body needed for this request)

---

## Comparison: WRONG vs RIGHT

### ❌ WRONG (Getting 403)
```
POST http://localhost:8082//api/agents/select-all?companyRefId=5&jobId=0
                       ^^
                  DOUBLE SLASH
```

**Result**: 403 Forbidden

---

### ✅ RIGHT (Will get 200)
```
POST http://localhost:8082/api/agents/select-all?companyRefId=5&jobId=0
                       ^
                  SINGLE SLASH
```

**Result**: 200 OK with data

---

## Step-by-Step in Postman

### Step 1: Click on "Agent Select All" in Collections
It should show:
```
Name: Agent Select All
Method: POST
URL: http://{{host}}/api/agents/select-all?companyRefId=5&jobId=0
```

### Step 2: Check URL
Make sure it shows:
```
http://localhost:8082/api/agents/select-all?companyRefId=5&jobId=0
                     ^
            Only ONE slash!
```

### Step 3: Check Authorization
- Click **Authorization** tab
- Should show: **Type: Bearer Token**
- Should show: **Token: {{token}}**

### Step 4: Click **Send**

### Step 5: Check Response
Should see status: **200 OK**

Response should look like:
```json
{
  "ok": true,
  "message": "Agents retrieved successfully",
  "data": [...],
  "count": 5
}
```

---

## Test Cases

### Test Case 1: Get all agents for company 5
```
URL: http://localhost:8082/api/agents/select-all?companyRefId=5&jobId=0
Expected: 200 OK with all agents
```

### Test Case 2: Get agents with agent company filter
```
URL: http://localhost:8082/api/agents/select-all?companyRefId=5&jobId=2
Expected: 200 OK with filtered agents
```

### Test Case 3: Invalid company ID
```
URL: http://localhost:8082/api/agents/select-all?companyRefId=0&jobId=0
Expected: 400 Bad Request - "CompanyRefId must be valid"
```

### Test Case 4: Missing token
```
URL: http://localhost:8082/api/agents/select-all?companyRefId=5
Headers: No Authorization header
Expected: 401 Unauthorized
```

---

## Common Mistakes to Avoid

### ❌ Mistake 1: Double Slash
```
WRONG: http://localhost:8082//api/agents/select-all
RIGHT: http://localhost:8082/api/agents/select-all
```

### ❌ Mistake 2: Forgot to run login first
```
WRONG: Send Agent Select All without token
RIGHT: Run /api/login first, then Agent Select All
```

### ❌ Mistake 3: Wrong HTTP Method
```
WRONG: GET /api/agents/select-all
RIGHT: POST /api/agents/select-all
```

### ❌ Mistake 4: Missing query parameters
```
WRONG: POST /api/agents/select-all
RIGHT: POST /api/agents/select-all?companyRefId=5&jobId=0
```

### ❌ Mistake 5: Bearer token malformed
```
WRONG: Authorization: Bearer{{token}}  (no space)
RIGHT: Authorization: Bearer {{token}} (with space)
```

---

## Postman Collection Update

The Postman collection has been updated with the correct endpoint:

**File**: `postman/collections/Maleva API.postman_collection.json`

**Endpoint name**: "Agent Select All"

**Configuration**: 
- Method: POST
- URL: http://{{host}}/api/agents/select-all?companyRefId=5&jobId=0
- Auth: Bearer {{token}}

---

## How to Update If Using Old Collection

If your collection still has wrong settings:

1. Delete the old "Agent Select All" endpoint
2. Create new one with:
   - **Name**: Agent Select All
   - **Method**: POST
   - **URL**: http://{{host}}/api/agents/select-all?companyRefId=5&jobId=0
   - **Authorization**: Bearer Token → {{token}}

Or import the updated collection from: `postman/collections/Maleva API.postman_collection.json`

---

## Quick Copy-Paste URL

### For localhost:
```
http://localhost:8082/api/agents/select-all?companyRefId=5&jobId=0
```

### For your actual server (replace SERVER_IP):
```
http://SERVER_IP:8082/api/agents/select-all?companyRefId=5&jobId=0
```

---

## Verification Checklist

Before sending, verify:

- ✅ Method is **POST** (not GET)
- ✅ URL has **single slash** (`/api/` not `//api/`)
- ✅ URL includes query params (`?companyRefId=5&jobId=0`)
- ✅ Authorization type is **Bearer Token**
- ✅ Token is **{{token}}** (not hardcoded)
- ✅ Token obtained from `/api/login` first
- ✅ Content-Type header is `application/json`
- ✅ Body is empty

---

## Success Indicators

You'll know it worked when:

1. ✅ Status shows **200 OK** (green)
2. ✅ Response shows `"ok": true`
3. ✅ You see agent data in response
4. ✅ Count is > 0

---

**This is the CORRECT configuration. Follow this exactly!**

