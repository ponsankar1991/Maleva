# Maleva Project - Complete Structure Analysis

## 📋 Project Overview
**Maleva** is a Spring Boot 4.0.2 REST API application built with Java 17, designed for managing logistics, ports, transportation, and related business operations.

**Technology Stack:**
- Java 17
- Spring Boot 4.0.2
- Spring Data JPA
- MapStruct for entity-DTO mapping
- Jakarta Persistence (JPA)
- Lombok for code generation
- Spring Security (with JWT support)
- War packaging for deployment

---

## 📁 Project Structure

### Root Directory Structure
```
Maleva/
├── src/main/java/my/maleva/api/
│   ├── controller/        # REST API endpoints (70+ controllers)
│   ├── service/           # Business logic layer (70+ services)
│   ├── dto/              # Data Transfer Objects (70+ DTOs)
│   ├── model/            # JPA Entity classes (70+ entities)
│   ├── repo/             # Spring Data JPA repositories (70+ repos)
│   ├── mapper/           # MapStruct mappers (70+ mappers)
│   ├── exception/        # Custom exception classes
│   ├── auth/             # Authentication related classes
│   ├── config/           # Spring configuration classes
│   ├── util/             # Utility classes (UserRoles enum)
│   ├── MalevaApplication.java    # Spring Boot main class
│   └── ServletInitializer.java   # War deployment config
├── src/main/resources/
│   └── application.yaml          # Application configuration
├── db/
│   ├── sp/               # Stored procedures
│   └── table/            # Database table schemas
├── postman/              # Postman API test collections
├── docs/                 # API documentation
├── docker/               # Docker deployment configs
├── pom.xml              # Maven build configuration
└── README files         # Various implementation guides
```

---

## 🔄 Architecture - Three-Layer Design

### Layer 1: Controller Layer
**Location:** `src/main/java/my/maleva/api/controller/`

**Purpose:** HTTP endpoint mapping and request/response handling

**EmployeeMasterController Example:**
```
GET    /api/employees              → List all employees (with optional name filter)
POST   /api/employees              → Create new employee
GET    /api/employees/{id}         → Get single employee by ID
PUT    /api/employees/{id}         → Update employee
DELETE /api/employees/{id}         → Delete employee
GET    /api/employees/company/{companyRefId}/roles  → Get employees by company & roles
```

**Key Responsibilities:**
- URL routing and HTTP method mapping
- Request parameter validation (@Valid, @PathVariable, @RequestParam)
- Response entity creation with appropriate HTTP status codes
- Input/output serialization via DTOs

---

### Layer 2: Service Layer
**Location:** `src/main/java/my/maleva/api/service/`

**Purpose:** Core business logic implementation

**EmployeeMasterService Key Methods:**
```java
// CRUD Operations
create(EmployeeMasterDto dto)          // Create employee with default roleId=100
update(Integer id, EmployeeMasterDto)  // Update existing employee
getById(Integer id)                     // Retrieve by ID
findAll(String name)                    // List all with optional name filter
delete(Integer id)                      // Delete by ID

// Authentication
verifyCredentials(userName, password)   // Check password (BCrypt or plain)
findByUserName(String userName)         // Lookup for authentication

// Combo Lists (Filtered Lists)
getEmployeesByCompanyAndRoles(companyRefId, roleId, roleId1)
// Returns: Active employees (Active=1) for a company with optional role filtering
```

**Features:**
- @Transactional annotation for atomic operations
- @Transactional(readOnly=true) for query operations
- Exception handling (EntityNotFoundException)
- Password encoding support (BCrypt compatible)
- Role-based filtering

---

### Layer 3: Repository Layer
**Location:** `src/main/java/my/maleva/api/repo/`

**Purpose:** Database access abstraction

**EmployeeMasterRepository Methods:**
```java
// JPA Standard Methods (inherited)
findById(Integer id)
save(EmployeeMaster entity)
delete(EmployeeMaster entity)

// Custom Query Methods
findByEmployeeNameContainingIgnoreCase(name, pageable)
findByUserNameAndActive(userName, active)
findByCompanyRefIdAndActive(companyRefId, active)
findByCompanyRefIdAndActiveAndRoleId(companyRefId, active, roleId)
findByCompanyAndRoleIds(companyRefId, roleIds)  // Custom @Query
```

**Custom Query Implementation:**
```sql
SELECT e FROM EmployeeMaster e 
WHERE e.companyRefId = :companyRefId 
AND e.active = 1 
AND e.roleId IN :roleIds 
ORDER BY e.employeeName ASC
```

---

## 📊 Data Model - EmployeeMaster Example

### Database Table: EmployeeMaster
```
Column Name          | Type        | Nullable | Default
─────────────────────┼─────────────┼──────────┼─────────
Id                   | INT         | NO       | AUTO
CompanyRefId         | INT         | NO       | -
EmployeeName         | VARCHAR(500)| NO       | -
EmployeeType         | VARCHAR(100)| NO       | -
CNumberDisplay       | VARCHAR(300)| NO       | -
CNumber              | INT         | NO       | -
Address1-3           | VARCHAR(300)| YES      | NULL
City, State          | VARCHAR(100)| YES      | NULL
Zipcode              | VARCHAR(50) | YES      | NULL
Country              | VARCHAR(50) | YES      | NULL
GstNo                | VARCHAR(100)| YES      | NULL
Email                | VARCHAR(100)| YES      | NULL
MobileNo             | VARCHAR(50) | YES      | NULL
UserName             | VARCHAR(50) | YES      | NULL
Password             | VARCHAR(50) | YES      | NULL
Latitude, Longitude  | VARCHAR(50) | YES      | NULL
TokenId              | VARCHAR(500)| YES      | NULL
Active               | INT         | NO       | 1
CreatedDate          | DATETIME    | NO       | NOW
ModifiedDate         | DATETIME    | NO       | NOW
ModifiedBy           | VARCHAR(50) | NO       | -
PersonId             | VARCHAR(100)| YES      | NULL
AccountRefid         | INT         | NO       | -
role_id              | NUMERIC(4)  | NO       | 100
TinNo, SSTNo         | VARCHAR(100)| YES      | NULL
MsicCode             | VARCHAR(100)| YES      | NULL
ServiceTaxType       | VARCHAR(100)| YES      | NULL
BankName             | VARCHAR(100)| YES      | NULL
AccountNo            | VARCHAR(100)| YES      | NULL
RulesType            | VARCHAR(50) | YES      | NULL
QNECode, QNEId       | VARCHAR(50) | YES      | NULL
JoiningDate          | DATE        | YES      | NULL
LeavingDate          | DATE        | YES      | NULL
EmergencyNo          | VARCHAR(50) | YES      | NULL
AppPassword          | VARCHAR(100)| YES      | NULL
Employeecurrency     | VARCHAR(255)| YES      | NULL
```

---

## 🔧 DTO (Data Transfer Object) - EmployeeMasterDto

**Purpose:** Serialize/deserialize JSON data in API requests/responses

**Key Fields:**
- `id` - Auto-generated primary key
- `companyRefId` - Company association (@NotNull)
- `employeeName` - Full name (@NotBlank, max 500)
- `employeeType` - Role type (@NotBlank, max 100)
- `cNumberDisplay` - Contract number display (@NotBlank, max 300)
- `cNumber` - Contract number (@NotNull)
- `email` - Email address (@Email)
- `mobileNo` - Phone number (max 50)
- `userName` - Login username (max 50)
- `password` - Password (max 50)
- `active` - Status flag (1=active, 0=inactive) (@NotNull)
- `roleId` - Role ID (@NotNull) - links to UserRoles enum
- `role` - UserRoles enum representation (@JsonProperty)
- `roleName` - Computed role name (string representation)
- `createdDate` - Audit trail
- `modifiedDate` - Audit trail
- `modifiedBy` - Audit trail

**Validation:**
- Uses Jakarta Validation annotations (@NotNull, @NotBlank, @Size, @Email)
- Automatic validation via @Valid in controller
- Returns 400 Bad Request if validation fails

---

## 🔐 Role-Based Access Control (UserRoles)

### UserRoles Enum Definition
```java
public enum UserRoles {
    SUPERADMIN(100),
    ADMIN(200),
    CUSTOMERSERVICE(300),
    OPERATIONADMIN(400),
    BOARDINGOFFICER(500),
    WAREHOUSE(600),
    DRIVER(700),
    HR(800),
    ACCOUNTS(900),
    PAYABLE(1100),
    RECEIVABLE(1200),
    MAINTENANCE(1300);
}
```

**Features:**
- Each role has a numeric ID (stored in DB as `role_id`)
- Reverse lookup: `UserRoles.fromId(100)` → SUPERADMIN
- Used for role-based filtering in getEmployeesByCompanyAndRoles API
- Integrated with JWT authentication system

---

## 🎯 EmployeeMaster API - Complete Flow

### 1. CREATE Employee
```
HTTP Request:
POST /api/employees
Content-Type: application/json
{
  "companyRefId": 1,
  "employeeName": "John Doe",
  "employeeType": "SALES",
  "cNumberDisplay": "EMP-001",
  "cNumber": 1,
  "email": "john@example.com",
  "mobileNo": "1234567890",
  "userName": "johndoe",
  "password": "password123",
  "active": 1,
  "accountRefid": 5,
  "roleId": 700  // DRIVER role
}

Processing Flow:
1. Controller receives request
2. @Valid annotation triggers validation
3. Service.create() is called
4. Mapper.toEntity() converts DTO to Entity
5. Default roleId=100 (SUPERADMIN) applied if null
6. Timestamps set (createdDate, modifiedDate)
7. Entity saved to database
8. Entity converted back to DTO
9. DTO returned with HTTP 201 Created + Location header
```

### 2. LIST with Role Filtering
```
HTTP Request:
GET /api/employees/company/1/roles?roleId=700&roleId1=500

Processing Flow:
1. Controller receives companyRefId=1, roleId=700, roleId1=500
2. Service.getEmployeesByCompanyAndRoles() called
3. Creates roleIdList = [700, 500]
4. Repository.findByCompanyAndRoleIds() executes SQL:
   SELECT e FROM EmployeeMaster e 
   WHERE e.companyRefId = 1 
   AND e.active = 1 
   AND e.roleId IN (700, 500)
   ORDER BY e.employeeName ASC
5. All matching active employees returned as DTOs
6. DTOs include role enum representation
7. HTTP 200 with JSON array returned
```

### 3. AUTHENTICATION
```
HTTP Request:
POST /api/auth/login
{
  "userName": "johndoe",
  "password": "password123"
}

Processing Flow:
1. Repository.findByUserNameAndActive(userName, 1)
2. Service.verifyCredentials() checks password
3. If password is BCrypt: use passwordEncoder.matches()
4. If plain text: direct string comparison
5. On success: JWT token generated with roleId
6. Token returned to client for subsequent requests
```

---

## 📦 Mapper (MapStruct) - EmployeeMasterMapper

**Purpose:** Convert between entities and DTOs

```java
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface EmployeeMasterMapper {
    
    // Entity → DTO (converts roleId to role enum)
    @Mapping(target = "role", expression = "java(idToRole(entity.getRoleId()))")
    EmployeeMasterDto toDto(EmployeeMaster entity);
    
    // DTO → Entity (converts role enum back to roleId)
    @Mapping(target = "roleId", expression = "java(roleToId(dto.getRole()))")
    EmployeeMaster toEntity(EmployeeMasterDto dto);
    
    // Partial update (null values ignored)
    void updateFromDto(EmployeeMasterDto dto, @MappingTarget EmployeeMaster entity);
    
    // Helper: roleId (int) ↔ UserRoles (enum)
    default UserRoles idToRole(Integer id) { ... }
    default Integer roleToId(UserRoles role) { ... }
}
```

**Key Features:**
- Null value strategy: IGNORE (don't override with nulls)
- Custom role conversion using helper methods
- Automatic field mapping (snake_case ↔ camelCase)
- Null-safe conversions for optional fields

---

## 🔄 Request/Response Examples

### Example 1: Create Employee
```
REQUEST:
POST /api/employees
Content-Type: application/json

{
  "companyRefId": 1,
  "employeeName": "Alice Smith",
  "employeeType": "DRIVER",
  "cNumberDisplay": "EMP-002",
  "cNumber": 2,
  "email": "alice@example.com",
  "mobileNo": "9876543210",
  "userName": "alice",
  "password": "pass123",
  "active": 1,
  "accountRefid": 10,
  "roleId": 700
}

RESPONSE (201 Created):
Location: /api/employees/5

{
  "id": 5,
  "companyRefId": 1,
  "employeeName": "Alice Smith",
  "employeeType": "DRIVER",
  "cNumberDisplay": "EMP-002",
  "cNumber": 2,
  "email": "alice@example.com",
  "mobileNo": "9876543210",
  "userName": "alice",
  "password": "pass123",
  "active": 1,
  "accountRefid": 10,
  "roleId": 700,
  "role": "DRIVER",
  "roleName": "DRIVER",
  "createdDate": "2024-02-24T15:30:00",
  "modifiedDate": "2024-02-24T15:30:00",
  "modifiedBy": "system"
}
```

### Example 2: Get Employees by Company and Roles
```
REQUEST:
GET /api/employees/company/1/roles?roleId=700&roleId1=500

RESPONSE (200 OK):
[
  {
    "id": 5,
    "companyRefId": 1,
    "employeeName": "Alice Smith",
    "employeeType": "DRIVER",
    "roleId": 700,
    "role": "DRIVER",
    "roleName": "DRIVER",
    "active": 1,
    "createdDate": "2024-02-24T15:30:00",
    ...
  },
  {
    "id": 8,
    "companyRefId": 1,
    "employeeName": "Bob Johnson",
    "employeeType": "BOARDINGOFFICER",
    "roleId": 500,
    "role": "BOARDINGOFFICER",
    "roleName": "BOARDINGOFFICER",
    "active": 1,
    "createdDate": "2024-02-20T10:15:00",
    ...
  }
]
```

---

## 🏗️ Complete Request Flow

```
Client Request
    ↓
Spring DispatcherServlet routes to EmployeeMasterController
    ↓
Controller method validates @Valid EmployeeMasterDto
    ↓ (if validation fails: 400 Bad Request)
    ↓
Controller calls EmployeeMasterService method
    ↓
Service applies business logic:
  - Validates business rules
  - Uses Mapper to convert DTO ↔ Entity
  - Calls Repository for database operations
  - Handles exceptions
    ↓
Repository executes database query via JPA
    ↓ (if not found: EntityNotFoundException → 404)
    ↓
Database returns result set
    ↓
Service converts Entity back to DTO using Mapper
    ↓
Controller returns ResponseEntity with HTTP status + DTO
    ↓
Spring serializes DTO to JSON
    ↓
HTTP Response sent to client
```

---

## 🛠️ Configuration Files

### pom.xml (Maven Dependencies)
- Spring Boot 4.0.2 (parent)
- spring-boot-starter-data-jpa (Database)
- spring-boot-starter-web (REST API)
- spring-boot-starter-validation (Input validation)
- spring-boot-starter-security (Authentication)
- mapstruct (Entity-DTO mapping)
- lombok (Code generation)
- jakarta.persistence-api (JPA annotations)
- Database driver (MSSQL/MySQL/etc.)

### application.yaml
- Server port configuration
- Database connection settings
- JPA/Hibernate configuration
- Logging levels
- JWT secret key
- Connection pooling settings

---

## 📊 Database Schema Pattern

All entities follow this pattern:

```java
@Entity
@Table(name = "EntityName")
@Data  // Lombok: generates getters, setters, equals, hashCode, toString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntityName {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false)
    private Integer companyRefId;  // Multi-tenancy
    
    // Business fields...
    
    @Column(nullable = false)
    private LocalDateTime createdDate;  // Audit
    
    @Column(nullable = false)
    private LocalDateTime modifiedDate;  // Audit
    
    @Column(nullable = false)
    private String modifiedBy;  // Audit
    
    @Column(columnDefinition = "INT DEFAULT 1")
    private Integer active;  // Soft delete
}
```

---

## ✅ Implementation Status - EmployeeMaster

**All components correctly implemented:**

| Component | Status | Details |
|-----------|--------|---------|
| Model | ✅ | EmployeeMaster.java - Complete JPA entity |
| DTO | ✅ | EmployeeMasterDto.java - Full validation annotations |
| Repository | ✅ | EmployeeMasterRepository.java - 6 query methods |
| Mapper | ✅ | EmployeeMasterMapper.java - Role enum conversion |
| Service | ✅ | EmployeeMasterService.java - All business logic |
| Controller | ✅ | EmployeeMasterController.java - All endpoints |
| Auth | ✅ | Password verification with BCrypt support |
| Role Filter | ✅ | getEmployeesByCompanyAndRoles() fully functional |

---

## 🔗 Related Entities (70+ Implemented)

Similar patterns exist for:
- **Company** - Multi-tenant parent
- **CustomerMaster** - Customer data
- **PortMaster** - Port management
- **PlanningMaster** - Logistics planning
- **JobMaster** - Job scheduling
- **BillMaster** - Billing
- **PaymentMaster** - Payment processing
- **ItemMaster** - Inventory
- ... and 60+ more entities

---

## 📝 Summary

**The Maleva project is a well-architected, enterprise-grade Spring Boot REST API with:**

1. **Clean Architecture** - Clear separation: Controller → Service → Repository → Database
2. **Type Safety** - Strong typing with Java 17, Generic constraints
3. **Validation** - Input validation with Jakarta Validation API
4. **Mapping** - Smart object-relational mapping with MapStruct
5. **Security** - JWT-based authentication with role-based access
6. **Maintainability** - Consistent patterns across 70+ entities
7. **Scalability** - Multi-tenancy support (companyRefId)
8. **Audit Trail** - Automatic tracking of create/modify dates and users
9. **Error Handling** - Custom exceptions with appropriate HTTP status codes
10. **API Documentation** - Postman collections for testing

All implementations follow Spring Boot best practices and enterprise patterns.

