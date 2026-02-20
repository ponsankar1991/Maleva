# 🚨 QUICK FIX - 403 Error & IntelliJ Issues

## PROBLEM #1: 403 Error in Postman ❌

### Your URL:
```
POST http://localhost:8082//api/agents/select-all
                       ^^
                   DOUBLE SLASH!
```

### Fix - Change to:
```
POST http://localhost:8082/api/agents/select-all
                       ^
                   SINGLE SLASH!
```

**That's it!** The double slash is causing the 403 error.

---

## PROBLEM #2: IntelliJ ClassNotFoundException ❌

### Error:
```
Error: Could not find or load main class my.maleva.api.MalevaApplication
```

### Fix - Run this command:
```bash
mvn clean compile
```

Then start application with:
```bash
mvn spring-boot:run
```

Or in IntelliJ:
- File → Invalidate Caches → Restart
- Then run again

---

## VERIFY - Test These Now

### Test 1: Fix Postman URL
In Postman, the URL should be:
```
http://localhost:8082/api/agents/select-all?companyRefId=5&jobId=0
                     ^
              SINGLE SLASH
```

### Test 2: Run Login First
```
GET /api/auth/login
Body: {"userId":"MAGES","password":"7151"}
```

**Response** (should see):
```json
{
  "token": "eyJhbGc...",
  "roleId": 100
}
```

### Test 3: Run Agent Select All
```
POST /api/agents/select-all?companyRefId=5&jobId=0
Authorization: Bearer {token from login}
```

**Expected Response**:
```json
{
  "ok": true,
  "message": "Agents retrieved successfully",
  "count": 5
}
```

---

## 🎯 3-Step Fix

### Step 1: Fix URL
```
Remove the double slash from Postman URL
//api → /api
```

### Step 2: Clean Build
```bash
mvn clean compile
```

### Step 3: Run Application
```bash
mvn spring-boot:run
```

---

## ✅ Build Status
```
✅ Build: SUCCESS (mvn clean compile)
✅ Code: Fixed (@PreAuthorize added)
✅ Ready: YES
```

---

## That's All! 🎉

Just:
1. **Fix the URL** (remove double slash)
2. **Rebuild** (mvn clean compile)
3. **Restart app** (mvn spring-boot:run)
4. **Test in Postman**

You should get 200 OK now!

