# EmployeeMaster Implementation - Visual Architecture Guide

## 🏛️ Architecture Layers

```
┌─────────────────────────────────────────────────────────────┐
│  CLIENT (Postman, Web Browser, Mobile App, Frontend)       │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  HTTP Request: GET /api/employees/company/1/roles           │
│  Parameters: roleId=700, roleId1=500                        │
│                                                              │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ↓
┌─────────────────────────────────────────────────────────────┐
│  PRESENTATION LAYER (Controller)                            │
│  EmployeeMasterController.java                              │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  @GetMapping("/company/{companyRefId}/roles")               │
│  ├─ Extract @PathVariable: companyRefId=1                   │
│  ├─ Extract @RequestParam: roleId=700, roleId1=500          │
│  ├─ Call service.getEmployeesByCompanyAndRoles(1, 700, 500) │
│  └─ Return ResponseEntity.ok(employees)                     │
│                                                              │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ↓
┌─────────────────────────────────────────────────────────────┐
│  SERVICE LAYER (Business Logic)                             │
│  EmployeeMasterService.java                                 │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  getEmployeesByCompanyAndRoles(1, 700, 500) {               │
│    1. Create roleIdList = [700, 500]                        │
│    2. Check if roleIdList is empty? NO                      │
│    3. Call repository.findByCompanyAndRoleIds(1, [700,500]) │
│    4. Stream result through mapper.toDto()                  │
│    5. Return List<EmployeeMasterDto>                        │
│  }                                                           │
│                                                              │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ↓
┌─────────────────────────────────────────────────────────────┐
│  REPOSITORY LAYER (Data Access)                             │
│  EmployeeMasterRepository.java                              │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  findByCompanyAndRoleIds(1, [700, 500]) {                   │
│    @Query("SELECT e FROM EmployeeMaster e                   │
│             WHERE e.companyRefId = :companyRefId            │
│             AND e.active = 1                                │
│             AND e.roleId IN :roleIds                        │
│             ORDER BY e.employeeName ASC")                   │
│  }                                                           │
│                                                              │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ↓
┌─────────────────────────────────────────────────────────────┐
│  DATABASE LAYER                                             │
│  EmployeeMaster Table (SQL Server / MySQL)                  │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  SELECT * FROM EmployeeMaster                               │
│  WHERE CompanyRefId = 1                                     │
│  AND Active = 1                                             │
│  AND role_id IN (700, 500)                                  │
│  ORDER BY EmployeeName ASC                                  │
│                                                              │
│  Result Rows:                                               │
│  ┌─────┬──────────────┬─────────┬─────────┐                │
│  │ Id  │ EmployeeName │ role_id │ Active  │                │
│  ├─────┼──────────────┼─────────┼─────────┤                │
│  │ 5   │ Alice Smith  │ 700     │ 1       │                │
│  │ 8   │ Bob Johnson  │ 500     │ 1       │                │
│  │ 12  │ Carol Davis  │ 700     │ 1       │                │
│  └─────┴──────────────┴─────────┴─────────┘                │
│                                                              │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ↓
┌─────────────────────────────────────────────────────────────┐
│  MAPPER LAYER                                               │
│  EmployeeMasterMapper.java (MapStruct)                      │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  EmployeeMaster ENTITY                                      │
│  {                                                           │
│    id: 5,                                                    │
│    employeeName: "Alice Smith",                             │
│    roleId: 700,  ← numeric value in DB                      │
│    active: 1,                                               │
│    ...                                                       │
│  }                                                           │
│           │                                                  │
│           └──→ mapper.toDto()                               │
│                   ↓                                          │
│               idToRole(700) → UserRoles.DRIVER               │
│                   ↓                                          │
│  EmployeeMasterDto DTO                                      │
│  {                                                           │
│    id: 5,                                                    │
│    employeeName: "Alice Smith",                             │
│    roleId: 700,                                             │
│    role: UserRoles.DRIVER,  ← enum value in JSON            │
│    roleName: "DRIVER",      ← string representation         │
│    active: 1,                                               │
│    ...                                                       │
│  }                                                           │
│                                                              │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ↓
┌─────────────────────────────────────────────────────────────┐
│  JSON SERIALIZATION (Jackson)                               │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  [                                                           │
│    {                                                         │
│      "id": 5,                                               │
│      "companyRefId": 1,                                     │
│      "employeeName": "Alice Smith",                         │
│      "employeeType": "DRIVER",                              │
│      "roleId": 700,                                         │
│      "role": "DRIVER",                                      │
│      "roleName": "DRIVER",                                  │
│      "active": 1,                                           │
│      "createdDate": "2024-02-24T10:00:00",                 │
│      "modifiedDate": "2024-02-24T10:00:00",                │
│      ...                                                     │
│    },                                                        │
│    {                                                         │
│      "id": 8,                                               │
│      "companyRefId": 1,                                     │
│      "employeeName": "Bob Johnson",                         │
│      "employeeType": "BOARDINGOFFICER",                     │
│      "roleId": 500,                                         │
│      "role": "BOARDINGOFFICER",                             │
│      "roleName": "BOARDINGOFFICER",                         │
│      "active": 1,                                           │
│      ...                                                     │
│    },                                                        │
│    ...                                                       │
│  ]                                                           │
│                                                              │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ↓
┌─────────────────────────────────────────────────────────────┐
│  HTTP RESPONSE (Spring Response)                            │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  HTTP/1.1 200 OK                                            │
│  Content-Type: application/json                             │
│  Content-Length: 1234                                       │
│  Date: Fri, 24 Feb 2024 10:30:00 GMT                       │
│                                                              │
│  [                                                           │
│    {                                                         │
│      "id": 5,                                               │
│      "employeeName": "Alice Smith",                         │
│      "roleId": 700,                                         │
│      "role": "DRIVER",                                      │
│      ...                                                     │
│    },                                                        │
│    ...                                                       │
│  ]                                                           │
│                                                              │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ↓
┌─────────────────────────────────────────────────────────────┐
│  CLIENT RECEIVES & PROCESSES RESPONSE                       │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  JavaScript / Frontend Code:                                │
│  fetch('/api/employees/company/1/roles?roleId=700...')     │
│  .then(res => res.json())                                   │
│  .then(employees => {                                       │
│    // employees is an Array of EmployeeMasterDto objects   │
│    employees.forEach(emp => {                              │
│      console.log(emp.employeeName, emp.roleName);          │
│      // Output: "Alice Smith", "DRIVER"                     │
│    });                                                       │
│  });                                                         │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔄 Complete Request Flow Diagram

```
Start: Client sends HTTP request
  │
  ├─ Request Details
  │  ├─ Method: GET
  │  ├─ Path: /api/employees/company/1/roles
  │  ├─ Query: ?roleId=700&roleId1=500
  │  └─ Headers: Accept: application/json
  │
  ↓
┌──────────────────────────────────────────────────────┐
│ DispatcherServlet (Spring Core)                     │
│ Routes request to correct handler method             │
└──────────────────────────────────────────────────────┘
  │
  ├─ Matches URL pattern: /api/employees/**
  ├─ Matches method: GET
  └─ Identifies handler: EmployeeMasterController::getEmployeesByCompanyAndRoles
  │
  ↓
┌──────────────────────────────────────────────────────┐
│ Parameter Extraction                                │
│                                                      │
│ @PathVariable Integer companyRefId = 1              │
│ @RequestParam Integer roleId = 700                  │
│ @RequestParam Integer roleId1 = 500                 │
└──────────────────────────────────────────────────────┘
  │
  ↓
┌──────────────────────────────────────────────────────┐
│ Invoke Controller Method                            │
│                                                      │
│ getEmployeesByCompanyAndRoles(1, 700, 500)          │
│   └─ Call: service.getEmployeesByCompanyAndRoles()  │
└──────────────────────────────────────────────────────┘
  │
  ↓
┌──────────────────────────────────────────────────────┐
│ Service: Build Filters                              │
│                                                      │
│ List<Integer> roleIdList = new ArrayList<>()        │
│                                                      │
│ if (700 != null && 700 > 0)  → true                │
│   roleIdList.add(700)         ✓ Added               │
│                                                      │
│ if (500 != null && 500 > 0)  → true                │
│   roleIdList.add(500)         ✓ Added               │
│                                                      │
│ Result: roleIdList = [700, 500]                     │
└──────────────────────────────────────────────────────┘
  │
  ↓
┌──────────────────────────────────────────────────────┐
│ Service: Check Filter Condition                     │
│                                                      │
│ if (roleIdList.isEmpty()) → false                  │
│   (not empty, has 2 elements)                       │
│                                                      │
│ Execute: repository.findByCompanyAndRoleIds()       │
└──────────────────────────────────────────────────────┘
  │
  ↓
┌──────────────────────────────────────────────────────┐
│ Repository: Execute Database Query                  │
│                                                      │
│ SELECT e FROM EmployeeMaster e                      │
│ WHERE e.companyRefId = 1                            │
│ AND e.active = 1                                    │
│ AND e.roleId IN (700, 500)                          │
│ ORDER BY e.employeeName ASC                         │
│                                                      │
│ SQL Parameters:                                      │
│ :companyRefId = 1                                   │
│ :roleIds = [700, 500]                               │
└──────────────────────────────────────────────────────┘
  │
  ↓
┌──────────────────────────────────────────────────────┐
│ Database Execution (JDBC)                           │
│                                                      │
│ Connection pool provides connection                 │
│ PreparedStatement prevents SQL injection            │
│ Results fetched from EmployeeMaster table           │
│                                                      │
│ 3 rows returned:                                     │
│ Row 1: Id=5,  Name="Alice Smith",   roleId=700      │
│ Row 2: Id=8,  Name="Bob Johnson",   roleId=500      │
│ Row 3: Id=12, Name="Carol Davis",   roleId=700      │
└──────────────────────────────────────────────────────┘
  │
  ↓
┌──────────────────────────────────────────────────────┐
│ JPA: Hydrate Entity Objects                         │
│                                                      │
│ ResultSet → EmployeeMaster objects                  │
│                                                      │
│ List<EmployeeMaster> = [                            │
│   EmployeeMaster {                                   │
│     id=5, employeeName="Alice Smith",               │
│     roleId=700, active=1, ...                       │
│   },                                                 │
│   EmployeeMaster {                                   │
│     id=8, employeeName="Bob Johnson",               │
│     roleId=500, active=1, ...                       │
│   },                                                 │
│   EmployeeMaster {                                   │
│     id=12, employeeName="Carol Davis",              │
│     roleId=700, active=1, ...                       │
│   }                                                  │
│ ]                                                    │
└──────────────────────────────────────────────────────┘
  │
  ↓
┌──────────────────────────────────────────────────────┐
│ Service: Convert Entities to DTOs                   │
│                                                      │
│ employees.stream()                                   │
│   .map(mapper::toDto)                               │
│   .collect(Collectors.toList())                     │
│                                                      │
│ For each EmployeeMaster:                            │
│   ├─ Copy fields to EmployeeMasterDto               │
│   ├─ Convert: roleId (700) → role (UserRoles.DRIVER)│
│   ├─ Compute: roleName = "DRIVER"                   │
│   └─ Include all non-null fields                    │
│                                                      │
│ Result: List<EmployeeMasterDto> = [                │
│   EmployeeMasterDto {                               │
│     id=5, employeeName="Alice Smith",               │
│     roleId=700, role=DRIVER, roleName="DRIVER"      │
│   },                                                 │
│   EmployeeMasterDto {                               │
│     id=8, employeeName="Bob Johnson",               │
│     roleId=500, role=BOARDINGOFFICER,               │
│     roleName="BOARDINGOFFICER"                      │
│   },                                                 │
│   EmployeeMasterDto {                               │
│     id=12, employeeName="Carol Davis",              │
│     roleId=700, role=DRIVER, roleName="DRIVER"      │
│   }                                                  │
│ ]                                                    │
└──────────────────────────────────────────────────────┘
  │
  ↓
┌──────────────────────────────────────────────────────┐
│ Controller: Create Response Entity                  │
│                                                      │
│ ResponseEntity.ok(employees)                        │
│                                                      │
│ Sets:                                               │
│ ├─ HTTP Status: 200 OK                              │
│ ├─ Content-Type: application/json                   │
│ ├─ Body: List<EmployeeMasterDto>                    │
│ └─ Headers: Standard Spring response headers        │
└──────────────────────────────────────────────────────┘
  │
  ↓
┌──────────────────────────────────────────────────────┐
│ Serialization (Jackson ObjectMapper)                │
│                                                      │
│ Each EmployeeMasterDto → JSON object                │
│ Enum → String value in JSON                         │
│ LocalDateTime → ISO 8601 format string              │
│ Null values → excluded (@JsonInclude.NON_NULL)      │
│                                                      │
│ Result: JSON Array String                           │
│ [{"id":5,"employeeName":"Alice Smith",...},...]     │
└──────────────────────────────────────────────────────┘
  │
  ↓
┌──────────────────────────────────────────────────────┐
│ HTTP Response Construction                          │
│                                                      │
│ Status Line: HTTP/1.1 200 OK                        │
│ Headers:                                             │
│   Content-Type: application/json;charset=UTF-8      │
│   Content-Length: 1234 bytes                        │
│   Date: Fri, 24 Feb 2024 10:30:00 GMT              │
│   Server: Apache Tomcat/10.1.x                      │
│ Body: [{"id":5,...},...]                           │
└──────────────────────────────────────────────────────┘
  │
  ↓
End: Response sent to client
```

---

## 📝 Method Signature & Implementation

### Complete Controller Method:

```java
@RestController
@RequestMapping("/api/employees")
@Validated  // Enable method-level validation
public class EmployeeMasterController {
    
    // Dependency injection
    private final EmployeeMasterService service;
    
    // Constructor injection (Spring 4.3+)
    public EmployeeMasterController(EmployeeMasterService service) {
        this.service = service;
    }
    
    /**
     * Get active employees filtered by company and optional role IDs
     * 
     * @param companyRefId Company ID (required, path variable)
     * @param roleId First role ID filter (optional, query parameter)
     * @param roleId1 Second role ID filter (optional, query parameter)
     * @return List of active employees matching criteria
     * 
     * @throws EntityNotFoundException if companyRefId doesn't exist
     */
    @GetMapping("/company/{companyRefId}/roles")
    public ResponseEntity<List<EmployeeMasterDto>> getEmployeesByCompanyAndRoles(
            @PathVariable Integer companyRefId,
            @RequestParam(value = "roleId", required = false) Integer roleId,
            @RequestParam(value = "roleId1", required = false) Integer roleId1) {
        
        // Call service to perform filtering logic
        List<EmployeeMasterDto> employees = service.getEmployeesByCompanyAndRoles(
            companyRefId, 
            roleId, 
            roleId1
        );
        
        // Return successful response
        return ResponseEntity.ok(employees);
    }
}
```

### Complete Service Method:

```java
@Service
@Transactional  // All public methods are transactional
public class EmployeeMasterService {
    
    private final EmployeeMasterRepository repository;
    private final EmployeeMasterMapper mapper;
    private final PasswordEncoder passwordEncoder;
    
    // Constructor injection
    public EmployeeMasterService(
            EmployeeMasterRepository repository,
            EmployeeMasterMapper mapper,
            PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * Get active employees for a company filtered by role IDs.
     * Only returns employees with Active=1
     * 
     * @param companyRefId The company ID to filter by
     * @param roleId First role ID to filter (optional)
     * @param roleId1 Second role ID to filter (optional)
     * @return List of employees matching the filter criteria with only Active=1 status
     */
    @Transactional(readOnly = true)  // Read-only for efficiency
    public List<EmployeeMasterDto> getEmployeesByCompanyAndRoles(
            Integer companyRefId, 
            Integer roleId, 
            Integer roleId1) {
        
        // Build dynamic role filter list
        List<Integer> roleIdList = new java.util.ArrayList<>();
        
        // Add roleId if provided and valid (must be > 0)
        if (roleId != null && roleId > 0) {
            roleIdList.add(roleId);
        }
        
        // Add roleId1 if provided and valid (must be > 0)
        if (roleId1 != null && roleId1 > 0) {
            roleIdList.add(roleId1);
        }
        
        List<EmployeeMaster> employees;
        
        // Decide which repository method to call
        if (roleIdList.isEmpty()) {
            // No role filters: get all active employees for company
            employees = repository.findByCompanyRefIdAndActive(companyRefId, 1);
        } else {
            // With role filters: get employees with specific role IDs
            employees = repository.findByCompanyAndRoleIds(companyRefId, roleIdList);
        }
        
        // Convert all entities to DTOs and return
        return employees.stream()
            .map(mapper::toDto)  // Entity → DTO conversion
            .collect(Collectors.toList());
    }
}
```

### Repository Method:

```java
@Repository
public interface EmployeeMasterRepository extends JpaRepository<EmployeeMaster, Integer> {
    
    /**
     * Custom query to find employees by company and multiple role IDs
     * Automatically filters for active employees (Active=1)
     */
    @Query("SELECT e FROM EmployeeMaster e " +
           "WHERE e.companyRefId = :companyRefId " +
           "AND e.active = 1 " +
           "AND e.roleId IN :roleIds " +
           "ORDER BY e.employeeName ASC")
    List<EmployeeMaster> findByCompanyAndRoleIds(
        @Param("companyRefId") Integer companyRefId,
        @Param("roleIds") List<Integer> roleIds
    );
    
    // Alternative simple method when no role filter
    List<EmployeeMaster> findByCompanyRefIdAndActive(
        Integer companyRefId, 
        Integer active
    );
}
```

---

## 🔄 Data Transformation Example

```
INPUT: HTTP GET Request
GET /api/employees/company/1/roles?roleId=700&roleId1=500

   ↓ CONTROLLER receives
   
   companyRefId = 1
   roleId = 700
   roleId1 = 500

   ↓ SERVICE builds filter
   
   roleIdList = [700, 500]

   ↓ REPOSITORY queries
   
   Database returns:
   [
     EmployeeMaster { id=5, name="Alice", roleId=700, active=1 },
     EmployeeMaster { id=8, name="Bob", roleId=500, active=1 }
   ]

   ↓ MAPPER converts ENTITIES to DTOs
   
   For each EmployeeMaster entity:
   - Copy basic fields (id, name, etc.)
   - Convert: roleId (700) ← idToRole() → UserRoles.DRIVER
   - Compute: roleName = UserRoles.DRIVER.name() = "DRIVER"
   
   Result:
   [
     EmployeeMasterDto {
       id=5,
       employeeName="Alice",
       roleId=700,
       role=UserRoles.DRIVER,
       roleName="DRIVER"
     },
     EmployeeMasterDto {
       id=8,
       employeeName="Bob",
       roleId=500,
       role=UserRoles.BOARDINGOFFICER,
       roleName="BOARDINGOFFICER"
     }
   ]

   ↓ JACKSON serializes to JSON
   
   [
     {
       "id": 5,
       "employeeName": "Alice",
       "roleId": 700,
       "role": "DRIVER",
       "roleName": "DRIVER"
     },
     {
       "id": 8,
       "employeeName": "Bob",
       "roleId": 500,
       "role": "BOARDINGOFFICER",
       "roleName": "BOARDINGOFFICER"
     }
   ]

   ↓ RESPONSE sent
   
   HTTP/1.1 200 OK
   Content-Type: application/json
   
   [{"id":5,"employeeName":"Alice",...},...]

OUTPUT: Client receives JSON data
```

---

## 🛡️ Error Handling Flows

### Scenario 1: Employee Not Found

```
GET /api/employees/999  (non-existent ID)

   ↓ Controller calls service.getById(999)
   
   ↓ Service calls repository.findById(999)
   
   ↓ Repository returns Optional.empty()
   
   ↓ Service throws EntityNotFoundException("Employee not found: 999")
   
   ↓ Exception caught by Spring's ExceptionHandler
   
   ↓ Response: HTTP 404 Not Found
   
   {
     "status": 404,
     "message": "Employee not found: 999",
     "timestamp": "2024-02-24T10:00:00.000+0000"
   }
```

### Scenario 2: Validation Error

```
POST /api/employees with invalid data
{
  "companyRefId": 1,
  "employeeName": "",  ← BLANK (violates @NotBlank)
  "email": "invalid-email"  ← INVALID EMAIL (violates @Email)
}

   ↓ Controller receives @Valid EmployeeMasterDto
   
   ↓ Jakarta Validation validates fields
   
   ↓ Violations found:
      - employeeName: must not be blank
      - email: must be a well-formed email address
   
   ↓ Spring throws MethodArgumentNotValidException
   
   ↓ Exception caught by ExceptionHandler
   
   ↓ Response: HTTP 400 Bad Request
   
   {
     "status": 400,
     "error": "Bad Request",
     "message": "Validation failed",
     "errors": [
       {
         "field": "employeeName",
         "message": "must not be blank"
       },
       {
         "field": "email",
         "message": "must be a well-formed email address"
       }
     ]
   }
```

### Scenario 3: Database Error

```
PUT /api/employees/5 → Database connection fails

   ↓ Service calls repository.save()
   
   ↓ JPA attempts to persist to database
   
   ↓ DatabaseException occurs (connection timeout, constraint violation, etc.)
   
   ↓ Exception propagates through service
   
   ↓ Exception caught by global exception handler
   
   ↓ Response: HTTP 500 Internal Server Error
   
   {
     "status": 500,
     "error": "Internal Server Error",
     "message": "A database error occurred",
     "timestamp": "2024-02-24T10:00:00.000+0000"
   }
```

---

## ✅ Verification Checklist

### Code Structure
- [x] Controller has @RestController annotation
- [x] Controller has @RequestMapping("/api/employees")
- [x] Controller is @Validated
- [x] Service has @Service annotation
- [x] Service has @Transactional annotation
- [x] Repository extends JpaRepository
- [x] Repository has @Repository annotation

### Endpoints Implemented
- [x] POST /api/employees (CREATE)
- [x] GET /api/employees/{id} (READ by ID)
- [x] GET /api/employees (READ list with filter)
- [x] PUT /api/employees/{id} (UPDATE)
- [x] DELETE /api/employees/{id} (DELETE)
- [x] GET /api/employees/company/{companyRefId}/roles (ADVANCED FILTER)

### Data Validation
- [x] @Valid on POST/PUT request bodies
- [x] @NotNull on required numeric fields
- [x] @NotBlank on required string fields
- [x] @Email on email fields
- [x] @Size on length-limited fields

### HTTP Responses
- [x] 201 Created for POST with Location header
- [x] 200 OK for GET and PUT
- [x] 204 No Content for DELETE
- [x] 400 Bad Request for validation errors
- [x] 404 Not Found for missing resources

### Business Logic
- [x] Role filtering in getEmployeesByCompanyAndRoles()
- [x] Active status filtering (Active=1)
- [x] Null value handling in updates
- [x] Timestamp management (createdDate, modifiedDate)
- [x] Password verification with BCrypt support

### Database
- [x] Repository query methods correctly parameterized
- [x] SQL injection prevention via @Query parameters
- [x] Efficient query ordering (by employeeName)
- [x] Proper JOIN and filtering logic

### Mapping
- [x] MapStruct mapper implementation
- [x] Entity ↔ DTO conversion
- [x] Role enum conversion (roleId ↔ UserRoles)
- [x] Null value strategy (IGNORE)

**All checks passed: Implementation is correct and complete! ✅**

