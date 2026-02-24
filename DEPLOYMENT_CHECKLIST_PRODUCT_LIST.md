# Deployment Checklist: Product List API Fix

## ✅ Pre-Deployment Verification

- [ ] **Code Fix Applied**
  - [ ] `ItemMasterController.java` has been updated
  - [ ] `@PreAuthorize` moved from class to method level
  - [ ] All 6 methods have individual `@PreAuthorize` annotations

- [ ] **No Syntax Errors**
  - [ ] No red underlines in IDE
  - [ ] Project compiles without errors
  - [ ] All imports are correct

- [ ] **Documentation Created**
  - [ ] `FIX_PRODUCT_LIST_API.md` ✅ Created
  - [ ] `TESTING_GUIDE_PRODUCT_LIST.md` ✅ Created
  - [ ] `API_FIX_SUMMARY.md` ✅ Created
  - [ ] `VISUAL_FIX_EXPLANATION.md` ✅ Created

---

## 🏗️ Build Phase

### Windows PowerShell
```powershell
# Navigate to project directory
cd "C:\karthickworkspace\malevanew\malevabackend\Maleva"

# Clean and package
mvn clean package
```

- [ ] Build completes successfully (look for "BUILD SUCCESS")
- [ ] WAR file created: `target/api-0.0.1-SNAPSHOT.war`
- [ ] No compilation errors or warnings
- [ ] All tests pass (if applicable)

---

## 🚀 Deployment Phase

### Option 1: Run Locally (Development)
```bash
# Start server
java -jar target/api-0.0.1-SNAPSHOT.war
```

- [ ] Server starts on port 8082
- [ ] No errors in console
- [ ] Application name: "Maleva"
- [ ] Database connection successful

### Option 2: Deploy to Application Server (Production)
```bash
# Copy WAR to application server
copy target/api-0.0.1-SNAPSHOT.war C:\path\to\tomcat\webapps\api.war
```

- [ ] WAR file copied to deployment location
- [ ] Application server restarted
- [ ] WAR extracted and deployed
- [ ] Logs show successful deployment

---

## 🧪 Testing Phase

### Step 1: Authentication Test
```bash
curl -X POST "http://localhost:8082/api/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"your_password\"}"
```

- [ ] Response status: **200 OK**
- [ ] Response contains: `"token": "eyJ..."`
- [ ] Token is not empty
- [ ] Save token for next tests

### Step 2: Test All Endpoints

#### GET /api/item-masters (List All)
```bash
curl -X GET "http://localhost:8082/api/item-masters" \
  -H "Authorization: Bearer YOUR_TOKEN"
```
- [ ] Response status: **200 OK**
- [ ] Response is a JSON array
- [ ] Contains ItemMasterDto objects

#### GET /api/item-masters/{id} (Get By ID)
```bash
curl -X GET "http://localhost:8082/api/item-masters/1" \
  -H "Authorization: Bearer YOUR_TOKEN"
```
- [ ] Response status: **200 OK**
- [ ] Response contains single ItemMasterDto
- [ ] ID matches request parameter

#### POST /api/item-masters (Create)
```bash
curl -X POST "http://localhost:8082/api/item-masters" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{...item data...}"
```
- [ ] Response status: **201 Created**
- [ ] Response contains created ItemMasterDto
- [ ] Location header present

#### PUT /api/item-masters/{id} (Update)
```bash
curl -X PUT "http://localhost:8082/api/item-masters/1" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{...updated data...}"
```
- [ ] Response status: **200 OK**
- [ ] Response contains updated ItemMasterDto

#### DELETE /api/item-masters/{id} (Delete)
```bash
curl -X DELETE "http://localhost:8082/api/item-masters/1" \
  -H "Authorization: Bearer YOUR_TOKEN"
```
- [ ] Response status: **204 No Content**
- [ ] No response body

#### **GET /api/item-masters/company/{companyRefId}/products (MAIN FIX)**
```bash
curl -X GET "http://localhost:8082/api/item-masters/company/6/products" \
  -H "Authorization: Bearer YOUR_TOKEN"
```
- [ ] Response status: **200 OK** (NOT 500!)
- [ ] Response is a JSON array
- [ ] Contains ProductListDto objects
- [ ] Each object has: id, productName, saleRate, purRate, mrp, productCode

### Step 3: Security Tests

#### Missing Token
```bash
curl -X GET "http://localhost:8082/api/item-masters/company/6/products"
```
- [ ] Response status: **401 Unauthorized**

#### Invalid Token
```bash
curl -X GET "http://localhost:8082/api/item-masters/company/6/products" \
  -H "Authorization: Bearer invalid_token"
```
- [ ] Response status: **401 Unauthorized**

#### Insufficient Role (if applicable)
```bash
curl -X GET "http://localhost:8082/api/item-masters/company/6/products" \
  -H "Authorization: Bearer TOKEN_WITH_WRONG_ROLE"
```
- [ ] Response status: **403 Forbidden**

---

## 📊 Performance Tests

- [ ] Response time < 1 second
- [ ] No memory leaks (monitor heap usage)
- [ ] No connection pool exhaustion
- [ ] Handles concurrent requests (test with load testing tool)

---

## 📝 Final Verification

- [ ] No HTTP 500 "No static resource" errors
- [ ] No routing issues
- [ ] All endpoints work as expected
- [ ] Security checks working correctly
- [ ] Database queries optimized
- [ ] Logging configured properly
- [ ] No security warnings in logs

---

## 🔒 Security Checklist

- [ ] JWT token validation working
- [ ] Role-based access control enforced
- [ ] CORS configured correctly
- [ ] CSRF protection disabled (for API)
- [ ] SQL injection prevention verified
- [ ] No sensitive data in logs
- [ ] Password encoding enabled (if configured)

---

## 📚 Documentation Checklist

- [ ] API documentation up-to-date
- [ ] Postman collection updated
- [ ] README includes new endpoint
- [ ] Deployment instructions clear
- [ ] Testing guide provided
- [ ] Troubleshooting guide included

---

## 🚨 Rollback Plan (If Needed)

If issues arise after deployment:

1. Stop the application
2. Revert to previous WAR file
3. Check logs for errors
4. Verify database is intact
5. Restart application

---

## ✨ Sign-Off

- [ ] Developer: Code reviewed and tested
- [ ] QA: All tests passed
- [ ] Operations: Deployment successful
- [ ] Business: Feature verified working

---

## 📞 Support Information

If issues occur, check:

1. **Logs:** `/logs/application.log`
2. **Database:** Ensure tables exist and data is present
3. **Network:** Check if port 8082 is accessible
4. **Authentication:** Verify JWT configuration
5. **Documentation:** Reference `TESTING_GUIDE_PRODUCT_LIST.md`

---

**Deployment Date:** February 23, 2026  
**Version:** 0.0.1-SNAPSHOT  
**Status:** ✅ Ready for Deployment

