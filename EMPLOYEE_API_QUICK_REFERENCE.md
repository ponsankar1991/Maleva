# Employee API - Quick Reference Card

## Endpoint
```
GET /api/employees/company/{companyRefId}/roles?type={type}&type1={type1}
```

## Parameters
| Param | Type | Required | Description |
|-------|------|----------|-------------|
| `companyRefId` | Integer | ✅ Yes | Company ID |
| `type` | String | ❌ No | Role type filter |
| `type1` | String | ❌ No | Second role filter |

## Quick Examples

### Get all employees for company 1
```
GET /api/employees/company/1/roles
```

### Get SALES employees
```
GET /api/employees/company/1/roles?type=SALES
```

### Get MANAGER employees
```
GET /api/employees/company/1/roles?type=MANAGER
```

### Get SALES + MANAGER employees
```
GET /api/employees/company/1/roles?type=SALES&type1=MANAGER
```

## Response Format
```json
[
  {
    "id": 1,
    "companyRefId": 1,
    "employeeName": "John Doe",
    "employeeType": "SALES",
    "userName": "johndoe",
    "password": "encrypted_password",
    "active": 1,
    "email": "john@company.com",
    "mobileNo": "123456789"
  }
]
```

## Special Behavior
- ⚠️ When `type=SALES`, **TRANSPORTATION employees are automatically included**
- ⚠️ When `type1=SALES`, **TRANSPORTATION employees are automatically included**
- ✅ Only **Active=1** employees are returned
- ✅ Results are ordered by **employeeName** (A-Z)

## Implementation Files
- **Service:** `src/main/java/my/maleva/api/service/EmployeeMasterService.java`
- **Controller:** `src/main/java/my/maleva/api/controller/EmployeeMasterController.java`
- **Repository:** `src/main/java/my/maleva/api/repo/EmployeeMasterRepository.java`

## Method Signature
```java
public List<EmployeeMasterDto> getEmployeesByCompanyAndRoles(
    Integer companyRefId, 
    String type, 
    String type1)
```

## HTTP Response Codes
- `200 OK` - Success (even if empty list)
- `400 Bad Request` - Invalid parameters
- `500 Internal Server Error` - Server error

## Key Features
✅ SQL Injection Safe  
✅ Only Active=1 data  
✅ Multiple role filtering  
✅ Auto-include TRANSPORTATION with SALES  
✅ Clean DTOs (not raw entities)  
✅ Proper error handling  

## Testing
Import Postman collection: `Employee_Filter_API.postman_collection.json`

