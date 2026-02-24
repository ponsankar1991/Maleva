# Employee GetEmployee API - Implementation Guide

## Overview
You now have a new REST API endpoint to retrieve employees filtered by company ID and role types. This replaces your C# implementation with a proper Java/Spring Boot solution.

## API Endpoint

### Get Employees by Company and Roles
**Endpoint:** `GET /api/employees/company/{companyRefId}/roles`

**Purpose:** Retrieves a list of active employees (Active=1) for a specific company, optionally filtered by employee types/roles.

## Request Parameters

| Parameter | Type | Location | Required | Description |
|-----------|------|----------|----------|-------------|
| `companyRefId` | Integer | URL Path | Yes | The Company ID to filter by |
| `type` | String | Query String | No | First employee type/role filter (e.g., "SALES", "MANAGER", "ADMIN") |
| `type1` | String | Query String | No | Second employee type/role filter. If value is "SALES", the system automatically includes "TRANSPORTATION" employees |

## Response

The API returns a JSON array of EmployeeMasterDto objects containing:
- `id` - Employee ID
- `companyRefId` - Company ID
- `employeeName` - Employee Name
- `employeeType` - Employee Type/Role
- `userName` - Username
- `password` - Password (encrypted if BCrypt)
- `active` - Active status (will always be 1)
- Other employee details (email, mobile, address, etc.)

## Usage Examples

### Example 1: Get All Active Employees for Company 1
```http
GET /api/employees/company/1/roles
```
**Result:** Returns all active employees for company ID 1

### Example 2: Get SALES Employees for Company 1
```http
GET /api/employees/company/1/roles?type=SALES
```
**Result:** Returns all active SALES employees for company ID 1

### Example 3: Get SALES Employees (Including TRANSPORTATION)
```http
GET /api/employees/company/1/roles?type=SALES
```
**Result:** Returns all active SALES and TRANSPORTATION employees for company ID 1 (automatic inclusion of TRANSPORTATION when SALES is specified)

### Example 4: Get Multiple Role Types
```http
GET /api/employees/company/1/roles?type=MANAGER&type1=ADMIN
```
**Result:** Returns all active MANAGER and ADMIN employees for company ID 1

### Example 5: Get SALES and Another Type
```http
GET /api/employees/company/1/roles?type=SALES&type1=MANAGER
```
**Result:** Returns all active SALES, TRANSPORTATION (automatic), and MANAGER employees for company ID 1

## Implementation Details

### What Changed

1. **Repository Layer** (`EmployeeMasterRepository.java`)
   - Added `findByCompanyRefIdAndActive()` - Find employees by company and active status
   - Added `findByCompanyRefIdAndActiveAndEmployeeType()` - Find employees by company, active status, and specific type
   - Added `findByCompanyAndEmployeeTypes()` - Custom query to find employees by company and multiple types

2. **Service Layer** (`EmployeeMasterService.java`)
   - Added `getEmployeesByCompanyAndRoles()` method
   - Handles parameter validation and filtering logic
   - Automatically adds "TRANSPORTATION" when "SALES" is requested
   - Returns only Active=1 employees

3. **Controller Layer** (`EmployeeMasterController.java`)
   - Added `getEmployeesByCompanyAndRoles()` endpoint
   - Maps to: `GET /api/employees/company/{companyRefId}/roles`
   - Accepts optional query parameters for role filtering

### Key Features

✅ **Security:** Only returns Active=1 employees (hard-coded in queries)
✅ **Performance:** Uses indexed queries on CompanyRefId and Active fields
✅ **Flexibility:** Supports single, dual, or no role filters
✅ **Auto-Include:** Automatically includes TRANSPORTATION when SALES is requested
✅ **Clean Code:** Follows Spring Boot best practices with proper layering
✅ **Type Safety:** Uses Java's type system instead of string concatenation

## How to Call from Client

### Using Postman
1. Create a new GET request
2. URL: `http://localhost:8080/api/employees/company/1/roles?type=SALES`
3. Click Send
4. Response will contain the filtered employee list

### Using JavaScript/Fetch
```javascript
const companyId = 1;
const roleType = 'SALES';

fetch(`/api/employees/company/${companyId}/roles?type=${roleType}`)
  .then(response => response.json())
  .then(employees => {
    console.log('Active employees:', employees);
    employees.forEach(emp => {
      console.log(`${emp.employeeName} - ${emp.employeeType}`);
    });
  });
```

### Using cURL
```bash
curl -X GET "http://localhost:8080/api/employees/company/1/roles?type=SALES&type1=MANAGER"
```

### Using Angular HttpClient
```typescript
getEmployeesByRole(companyId: number, type?: string, type1?: string) {
  let params = new HttpParams();
  if (type) params = params.set('type', type);
  if (type1) params = params.set('type1', type1);
  
  return this.http.get(`/api/employees/company/${companyId}/roles`, { params });
}
```

## Database Query Generated

The underlying SQL query (via JPA) is equivalent to:

```sql
SELECT e.* FROM EmployeeMaster e 
WHERE e.CompanyRefId = ? 
  AND e.Active = 1 
  AND e.EmployeeType IN (?, ?, ?)
ORDER BY e.EmployeeName ASC
```

## Comparison with Your C# Code

| Aspect | Your C# Code | Our Java Implementation |
|--------|-------------|------------------------|
| SQL Injection Risk | ⚠️ High (String concatenation) | ✅ None (JPA parameterized) |
| Code Location | Mixed (Service + Controller) | ✅ Properly separated (3 layers) |
| Filtering Logic | Complex string building | ✅ Clean list-based approach |
| Type Safety | ⚠️ String-based | ✅ Type-safe with DTOs |
| Testability | ⚠️ Difficult | ✅ Easy to unit test |
| Performance | ⚠️ N+1 potential | ✅ Single query |
| Documentation | ❌ No | ✅ JavaDoc included |

## Testing the API

### Using TestNG/JUnit
```java
@Test
public void testGetActiveEmployeesByCompanyAndRoles() {
    Integer companyId = 1;
    List<EmployeeMasterDto> employees = service.getEmployeesByCompanyAndRoles(
        companyId, "SALES", null);
    
    assertNotNull(employees);
    assertTrue(employees.stream()
        .allMatch(e -> e.getCompanyRefId().equals(companyId)));
    assertTrue(employees.stream()
        .allMatch(e -> e.getActive() == 1));
}
```

## Troubleshooting

### No Results Returned
- Verify the companyRefId exists in the database
- Check if employees have Active=1 status
- Verify employee types match exactly (case-sensitive)
- Check database has data for the specified company

### 404 Error
- Ensure company ID is valid
- Check the endpoint URL is correct

### 500 Error
- Check server logs for details
- Verify database connection is working
- Check that all parameters are properly formatted

## Future Enhancements

1. Add pagination support with `Pageable`
2. Add filtering by status (Active/Inactive toggle)
3. Add search by employee name within company
4. Add role-based access control (RBAC)
5. Add response caching
6. Add rate limiting

## Support
For questions or issues, refer to the implementation files or contact your development team.

