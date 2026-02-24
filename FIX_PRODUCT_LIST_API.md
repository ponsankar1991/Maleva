# Fix for API Error: "No static resource api/item-masters/company/6/products"

## Problem Summary
When calling the endpoint:
```
GET http://{{host}}/api/item-masters/company/6/products
```

You were getting this error:
```json
{
    "timestamp": "2026-02-23T09:26:14.175028800Z",
    "status": 500,
    "error": "Internal Server Error",
    "message": "No static resource api/item-masters/company/6/products for request '/api/item-masters/company/6/products'.",
    "path": "/api/item-masters/company/6/products",
    "details": null
}
```

## Root Cause
The issue was caused by placing the `@PreAuthorize` annotation at the **class level**:

```java
@RestController
@RequestMapping("/api/item-masters")
@PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class ItemMasterController {
    // ...
}
```

When `@PreAuthorize` is applied at the class level, it creates a proxy that intercepts all method calls. In certain conditions, this can interfere with Spring's request routing, causing the dispatcher to treat the request as if no matching handler was found, and it then tries to locate it as a static resource (which fails).

## Solution
**Move the `@PreAuthorize` annotation from the class level to individual method level.**

### Before (❌ INCORRECT):
```java
@RestController
@RequestMapping("/api/item-masters")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class ItemMasterController {
    
    @GetMapping
    public List<ItemMasterDto> list() {
        return service.listAll();
    }
    
    @GetMapping("/company/{companyRefId}/products")
    public ResponseEntity<List<ProductListDto>> getProductList(@PathVariable Integer companyRefId) {
        List<ProductListDto> products = service.getProductList(companyRefId);
        return ResponseEntity.ok(products);
    }
}
```

### After (✅ CORRECT):
```java
@RestController
@RequestMapping("/api/item-masters")
@Validated
public class ItemMasterController {
    
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
    public List<ItemMasterDto> list() {
        return service.listAll();
    }
    
    @GetMapping("/company/{companyRefId}/products")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
    public ResponseEntity<List<ProductListDto>> getProductList(@PathVariable Integer companyRefId) {
        List<ProductListDto> products = service.getProductList(companyRefId);
        return ResponseEntity.ok(products);
    }
}
```

## What Was Changed
✅ Removed `@PreAuthorize` from the class-level declaration  
✅ Added `@PreAuthorize` annotation to each individual method that requires authorization  
✅ This allows Spring to properly route requests before applying security constraints

## How to Test the API

### 1. Get JWT Token
```bash
curl -X POST "http://localhost:8082/api/login" \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"your_username\",
    \"password\": \"your_password\"
  }"
```

### 2. Call the Product List Endpoint
```bash
curl -X GET "http://localhost:8082/api/item-masters/company/6/products" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 3. In Postman
**URL:**
```
GET http://{{host}}/api/item-masters/company/6/products
```

**Headers:**
```
Authorization: Bearer {{your_jwt_token}}
Content-Type: application/json
```

**Expected Success Response (200 OK):**
```json
[
    {
        "id": 1,
        "productName": "Product 1",
        "saleRate": 100.00,
        "purRate": 50.00,
        "mrp": 120.00,
        "productCode": "P001"
    },
    {
        "id": 2,
        "productName": "Product 2",
        "saleRate": 200.00,
        "purRate": 100.00,
        "mrp": 250.00,
        "productCode": "P002"
    }
]
```

## Key Points

1. **Class-level `@PreAuthorize` is problematic** for complex routing scenarios
2. **Method-level `@PreAuthorize` is the best practice** - it's clearer and avoids proxy conflicts
3. **All methods in this controller require authentication** as per `SecurityConfig.java`
4. **The endpoint requires JWT token** in the `Authorization` header

## Files Modified
- `src/main/java/my/maleva/api/controller/ItemMasterController.java`

## Build and Deploy
```bash
mvn clean package
# Then deploy the generated WAR file or run with:
java -jar target/api-0.0.1-SNAPSHOT.war
```

---
**Status:** ✅ FIXED  
**Date:** February 23, 2026

