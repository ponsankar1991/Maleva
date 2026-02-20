# AgentCompanyMaster Controller - Authorization Fix

## Issue
You were getting an "Access Denied" error (HTTP 500) when trying to access the AgentCompanyMaster API endpoints, even though you were logged in as **SUPERADMIN** with a valid JWT token:

```json
{
  "timestamp": "2026-02-19T06:13:30.847506Z",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Access Denied",
  "path": "/api/agent-companies/6"
}
```

User Details:
- **UserId**: 14
- **RoleId**: 100 (SUPERADMIN)
- **UserName**: MAGESWARAN
- **RoleName**: SUPERADMIN

## Root Cause
The `AgentCompanyMasterController.java` file had a **typo in all @PreAuthorize annotations**:

**Incorrect (before fix):**
```java
@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPRERADMIN')")
```

**Correct (after fix):**
```java
@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
```

The missing 'E' in "SUPER" prevented Spring Security from recognizing the SUPERADMIN role (roleId=100).

## What Was Fixed

### File Modified
- `src/main/java/my/maleva/api/agentcompany/controller/AgentCompanyMasterController.java`

### Methods Fixed (7 total)
1. **listAll()** - GET /api/agent-companies
2. **getById()** - GET /api/agent-companies/{id}
3. **getByCompanyRefId()** - GET /api/agent-companies/company/{companyRefId}
4. **create()** - POST /api/agent-companies
5. **update()** - PUT /api/agent-companies/{id}
6. **delete()** - DELETE /api/agent-companies/{id}
7. **upsert()** - POST /api/agent-companies/upsert
8. **search()** - POST /api/agent-companies/search

### Change Summary
- **Changed**: `hasAuthority('ROLE_SUPRERADMIN')` (8 occurrences)
- **To**: `hasAuthority('ROLE_SUPERADMIN')`

## Verification

### Build Status
✅ **Build Successful** - The project compiles without errors after the fix

```
[INFO] BUILD SUCCESS
[INFO] Total time: 21.242 s
[INFO] Finished at: 2026-02-19T14:17:41+08:00
```

## How to Test

After redeploying the application, test with your SUPERADMIN credentials:

### Test 1: Get all agent companies
```bash
curl -X GET http://localhost:8082/api/agent-companies \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJNQUdFUyIsImlhdCI6MTc3MTQ4MTQzNywiZXhwIjoxNzcxNDg1MDM3LCJyb2xlSWQiOjEwMH0.jsZiLm7utYrd076XU6gyMRtzPR5tJUeRmJLUT_jRLnY"
```

### Test 2: Get specific agent company (ID 6)
```bash
curl -X GET http://localhost:8082/api/agent-companies/6 \
  -H "Authorization: Bearer {your_jwt_token}"
```

## Expected Behavior After Fix

✅ All endpoints should now return proper responses:
- **200 OK** - Successful request
- **201 Created** - Successful POST/upsert
- **204 No Content** - Soft delete or empty results
- **400 Bad Request** - Invalid input
- **404 Not Found** - Resource not found
- **500 Internal Server Error** - Server error (not authorization-related)

❌ You should **NO LONGER** get "Access Denied" (403) or 500 errors for valid SUPERADMIN requests

## Additional Notes

This is a common typo issue that affects Spring Security's authority matching. The correct role names should always match the authorities configured in your JWT token or user details service:

- **ROLE_SUPERADMIN** (roleId = 100)
- **ROLE_ADMIN** (roleId = 200)

## Files Deployed
1. Recompiled JAR/WAR with the fixed controller
2. Deploy to your application server and restart

---
**Fixed Date**: February 19, 2026  
**Status**: ✅ RESOLVED

