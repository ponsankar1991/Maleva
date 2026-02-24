# Visual Explanation: The Fix

## ❌ BEFORE - Class-Level @PreAuthorize (BROKEN)

```
┌─────────────────────────────────────────────────────────────┐
│  Browser Request:                                           │
│  GET /api/item-masters/company/6/products                  │
└──────────────────────────┬──────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│  Spring Dispatcher Servlet                                 │
│  • Trying to find handler mapping                          │
└──────────────────────────┬──────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│  Spring Security Proxy (CLASS LEVEL @PreAuthorize)         │
│  ❌ PROBLEM: Intercepts BEFORE routing is resolved        │
│  • Can't determine which handler to use                     │
│  • Proxy confused about request routing                     │
└──────────────────────────┬──────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│  Handler Mapping Failed                                     │
│  ❌ Dispatcher can't find handler                           │
│  • Falls back to static resource lookup                     │
│  • Static resource "/api/item-masters/company/6/products"? │
│  • NOT FOUND!                                               │
└──────────────────────────┬──────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│  HTTP 500 Internal Server Error                            │
│  "No static resource api/item-masters/company/6/products"  │
└─────────────────────────────────────────────────────────────┘
```

---

## ✅ AFTER - Method-Level @PreAuthorize (FIXED)

```
┌─────────────────────────────────────────────────────────────┐
│  Browser Request:                                           │
│  GET /api/item-masters/company/6/products                  │
│  Authorization: Bearer YOUR_JWT_TOKEN                       │
└──────────────────────────┬──────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│  Spring Dispatcher Servlet                                 │
│  ✅ Finds handler mapping                                  │
│  • Maps to: ItemMasterController.getProductList()          │
└──────────────────────────┬──────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│  Handler Method Found                                       │
│  • Route: /api/item-masters/company/{companyRefId}/products│
│  • Parameters: companyRefId = 6                             │
└──────────────────────────┬──────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│  Spring Security (METHOD LEVEL @PreAuthorize)              │
│  ✅ Evaluates: hasAuthority('ROLE_SUPERADMIN') OR ...     │
│  • Extracts JWT token from Authorization header            │
│  • Validates token                                          │
│  • Checks user roles                                        │
└──────────────────────────┬──────────────────────────────────┘
                          │
            ┌─────────────┴──────────────┐
            ▼                            ▼
    ┌──────────────────┐        ┌──────────────────┐
    │ User Has Role?   │        │ Invalid Token?   │
    │ ✅ YES           │        │ OR No Role?      │
    └────────┬─────────┘        └────────┬─────────┘
             │                           │
             ▼                           ▼
    ┌──────────────────┐        ┌──────────────────┐
    │ Proceed          │        │ HTTP 403         │
    │ to Handler       │        │ Forbidden        │
    └────────┬─────────┘        │ (or 401)         │
             │                  └──────────────────┘
             ▼
┌─────────────────────────────────────────────────────────────┐
│  ItemMasterService.getProductList(6)                        │
│  • Query database for company products                      │
│  • Filter for active items only                             │
│  • Sort by product name                                     │
│  • Return ProductListDto list                               │
└──────────────────────────┬──────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│  HTTP 200 OK                                                │
│  Content-Type: application/json                             │
│  Body:                                                       │
│  [                                                           │
│    {                                                         │
│      "id": 1,                                                │
│      "productName": "Product A",                            │
│      "saleRate": 1500.00,                                    │
│      "purRate": 1000.00,                                     │
│      "mrp": 1800.00,                                         │
│      "productCode": "PA001"                                 │
│    },                                                        │
│    ...                                                       │
│  ]                                                           │
└─────────────────────────────────────────────────────────────┘
```

---

## Side-by-Side Code Comparison

### ❌ BROKEN CODE (Class-Level)
```java
@RestController
@RequestMapping("/api/item-masters")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class ItemMasterController {

    @GetMapping
    public List<ItemMasterDto> list() {           // ❌ No security check
        return service.listAll();
    }

    @GetMapping("/company/{companyRefId}/products")
    public ResponseEntity<List<ProductListDto>> getProductList(@PathVariable Integer companyRefId) {  // ❌ Security applied at class level
        return ResponseEntity.ok(service.getProductList(companyRefId));
    }
}
```

**Problems:**
- 🔴 Proxy created at class instantiation
- 🔴 Interferes with route mapping
- 🔴 Results in "No static resource" error
- 🔴 Unclear which methods are protected

---

### ✅ FIXED CODE (Method-Level)
```java
@RestController
@RequestMapping("/api/item-masters")
@Validated
public class ItemMasterController {

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
    public List<ItemMasterDto> list() {           // ✅ Clear security requirement
        return service.listAll();
    }

    @GetMapping("/company/{companyRefId}/products")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
    public ResponseEntity<List<ProductListDto>> getProductList(@PathVariable Integer companyRefId) {  // ✅ Explicit security per method
        return ResponseEntity.ok(service.getProductList(companyRefId));
    }
}
```

**Advantages:**
- ✅ Route mapping happens BEFORE security checks
- ✅ No proxy conflicts with dispatcher
- ✅ Crystal clear which methods are protected
- ✅ Standard Spring Security best practice

---

## Request Flow Comparison

### ❌ Class-Level (BROKEN)
```
Request → Spring Proxy (CLASS LEVEL) → ❌ Routing Conflict → 500 Error
```

### ✅ Method-Level (CORRECT)
```
Request → Route Mapping ✅ → Handler Found ✅ → Spring Proxy (METHOD LEVEL) ✅ → Service ✅ → Response 200
```

---

## Why Method-Level is Better

| Aspect | Class-Level | Method-Level |
|--------|------------|--------------|
| **Routing** | ❌ Interferes | ✅ Works correctly |
| **Clarity** | ❌ Ambiguous | ✅ Explicit |
| **Performance** | ❌ Proxy overhead | ✅ Minimal overhead |
| **Best Practice** | ❌ Not recommended | ✅ Recommended |
| **Error Handling** | ❌ Confusing errors | ✅ Clear errors |
| **Maintenance** | ❌ Hard to debug | ✅ Easy to understand |

---

## Summary

**The Fix:** Move `@PreAuthorize` from **class level** to **method level**

**Result:**
- ✅ Proper request routing
- ✅ Correct security enforcement
- ✅ No more 500 "No static resource" errors
- ✅ Clear and maintainable code

---

*Diagram created: February 23, 2026*

