# AGENTS.md: Maleva API Development Guide

> **Project:** Maleva - Enterprise Java Spring Boot API  
> **Stack:** Spring Boot 4.0.2, Java 17, SQL Server, Spring Data JPA, MapStruct  
> **Architecture:** Modular layered design with 38 business modules  

## 🏗️ Architecture Overview

Maleva uses a **strict modular layered architecture**. All business logic lives in `src/main/java/my/maleva/api/module/{module_name}/` with these required sub-layers:

```
module/{name}/
├── controller/        # @RestController endpoints
├── service/          # Business logic interfaces
│   └── impl/         # Implementation classes
├── repository/       # JpaRepository data access
├── entity/          # @Entity JPA models
├── dto/             # Data transfer objects (request/response)
├── mapper/          # MapStruct Entity↔DTO conversion
└── specification/   # JPA Specifications for complex queries (optional)
```

**Critical:** Every new feature MUST follow this structure exactly. No exceptions.

## 🔑 Key Patterns & Conventions

### 1. **DTO-Only API Delivery**
NEVER expose JPA entities to clients. All API responses must use MapStruct mappers:
```java
// ❌ WRONG
@GetMapping("/{id}")
public Customer getCustomer(@PathVariable Long id) { ... }

// ✅ CORRECT
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<CustomerDto>> getCustomer(@PathVariable Long id) {
    CustomerDto dto = service.findById(id);
    return ResponseEntity.ok(ApiResponse.success(dto));
}
```

### 2. **Naming Conventions (Strictly Enforced)**
- **Entity:** `Customer.java`
- **DTO:** `CustomerDto.java`
- **Request DTO:** `CustomerCreateRequest.java`
- **Response DTO:** `CustomerResponse.java`
- **Service Interface:** `CustomerService.java`
- **Service Implementation:** `CustomerServiceImpl.java`
- **Repository:** `CustomerRepository.java`
- **Mapper:** `CustomerMapper.java`
- **Controller:** `CustomerController.java`

### 3. **Response Wrapper (Always Used)**
Every response wrapped in `ApiResponse<T>` defined in `common/dto/ApiResponse.java`:
```java
return ResponseEntity.ok(ApiResponse.success(data, "Optional message"));
return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ApiResponse.error("Resource not found"));
```

### 4. **Service Pattern: Interface + Impl**
```java
// Interface in service/ folder
public interface CustomerService {
    CustomerDto findById(Long id);
}

// Implementation in service/impl/ folder
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerServiceImpl implements CustomerService { ... }
```

### 5. **MapStruct Mapper Configuration**
```java
@Mapper(componentModel = "spring")
public interface CustomerMapper {
    CustomerDto toDto(Customer entity);
    Customer toEntity(CustomerCreateRequest request);
    void updateEntity(@MappingTarget Customer entity, CustomerUpdateRequest request);
}
```

## 🗄️ Database & Configuration

### Connection Details
- **Database:** SQL Server (TCP/IP port 1433)
- **Driver:** `com.microsoft.sqlserver.jdbc.SQLServerDriver`
- **Default DB:** `DemoMaleva`
- **Configuration:** `src/main/resources/application.yaml`

### HikariCP Tuning (Critical for Stability)
The project has **optimized connection pooling** to handle heavy load:
```yaml
hikari:
  maximum-pool-size: 30      # Concurrent connections
  idle-timeout: 600000       # 10 minutes
  max-lifetime: 1800000      # 30 minutes
  connection-timeout: 60000  # 60 seconds to acquire
```
**DO NOT change these lightly** - they're tuned for production performance.

### JPA Configuration
- **DDL:** `ddl-auto: none` (manual schema management)
- **Naming:** `PhysicalNamingStrategyStandardImpl` (uses exact column names)
- **OpenInView:** Disabled (`false`) - prevents connection exhaustion
- **Batch Size:** 25 (optimized for SQL Server)

## 🚀 Developer Workflows

### Build & Dependencies
```bash
./mvnw clean install         # Full build with tests
./mvnw compile              # Just compile
mvn dependency:tree         # View dependency tree
```

**Windows: `clean` can fail while IntelliJ is open.** IntelliJ compiles this
module into the same `target/classes` Maven does, so its build worker holds
handles on `.class` files and `mvn clean` dies with:

```
Failed to clean project: Failed to delete ...\target\classes\my\maleva\api\module
```

It is a lock, not a code problem — retry, or close the IDE first. The durable
fix is to leave exactly one writer: in IntelliJ, **Settings → Build, Execution,
Deployment → Build Tools → Maven → Runner → "Delegate IDE build/run actions to
Maven"**, so the IDE calls Maven instead of compiling in parallel.

Two related traps:
- Deleting `target/` by hand while the IDE is open makes IntelliJ resync and
  wipe the output Maven just wrote, which shows up as
  `TypeNotPresentException` / "Mockito cannot mock this class" in tests that
  passed a moment earlier. Recompile and re-run; it settles.
- `clean` is rarely needed. Reach for it when classes are **renamed or
  deleted** (stale `.class` files linger otherwise); plain `./mvnw compile` is
  enough for ordinary edits.

### Running the Application
```bash
./mvnw spring-boot:run      # Development mode (port 8082)
# OR Windows:
run.bat                     # Same as above
```

### Verification Script
```bash
build-verify.bat            # Windows: Validates build, checks dependencies
```

### Docker Deployment
```bash
docker-compose up -d        # Start all services
docker-compose down         # Stop all services
# Windows: docker-compose.windows.yml for Windows-specific config
```

## 🧪 Testing and Validation

### Test Projects
- Unit tests: `src/test/java/my/maleva/api/`
- Test modules exist only for: `accounting/`, `payment/`
- **Coverage expectation:** New features MUST include unit tests

### Key Test Dependencies
- JUnit 5, Mockito, Spring Boot Test starters
- Test containers for integration tests
- H2 database available for in-memory testing

### Compilation Validation
- **Java 17 required** - Fails on older versions
- **Annotation processors active:** MapStruct + Lombok generate code during compile
- **Fork compilation:** Enabled for proper processor handling

## 📚 Critical Files Reference

| File | Purpose |
|------|---------|
| `docs/CODING_STANDARDS.md` | Full architectural and coding standards (923 lines) |
| `docs/API_Standards.md` | API response format, error handling, configuration |
| `GEMINI.md` | Core project instructions and development workflow |
| `src/main/resources/application.yaml` | Database, connection pooling, JPA configuration |
| `src/main/java/my/maleva/api/common/` | Shared configs, exceptions, DTOs, constants |
| `pom.xml` | Dependencies: MapStruct 1.5.5, Auth0 JWT, SpringDoc, PDFBox |

## 🔒 Security

- **Authentication:** JWT-based via Auth0 library (`java-jwt 4.4.0`)
- **Authorization:** Spring Security with `@PreAuthorize` annotations
- **Config:** `src/main/java/my/maleva/api/security/`
- **NEVER commit secrets** - use environment variables (see `application.yaml`)

## ⚠️ Common Pitfalls & Solutions

### ❌ Pitfall 1: Not Using DTOs
```java
// WRONG - Exposes entity structure
@GetMapping
public List<Customer> getAll() { return repository.findAll(); }

// CORRECT - Uses DTO
@GetMapping
public ResponseEntity<ApiResponse<List<CustomerDto>>> getAll() {
    return ResponseEntity.ok(ApiResponse.success(
        customerService.findAll()
    ));
}
```

### ❌ Pitfall 2: Forgetting Service Layer
Repository logic should NOT be in controller. Always use service:
```java
// WRONG
@Autowired CustomerRepository repo;
@GetMapping public Customer get(Long id) { return repo.findById(id).orElse(null); }

// CORRECT
@Autowired CustomerService service;
@GetMapping public ResponseEntity<...> get(Long id) { return service.findById(id); }
```

### ❌ Pitfall 3: Missing OpenInView Fix
Forgetting `open-in-view: false` causes connection pool exhaustion under load.

### ❌ Pitfall 4: Mixing Response Types
Don't mix raw objects with `ApiResponse<T>`:
```java
// WRONG
public Response<Data> method() { return new Response<>(data); }  // Custom wrapper

// CORRECT
public ResponseEntity<ApiResponse<Data>> method() { ... }        // Standard wrapper
```

## 🧩 Module Examples

### Customer Module (`src/main/java/my/maleva/api/module/customer/`)
- Controllers: 5 types (Customer, Quotation, Enquiry, etc.)
- Demonstrates multi-entity module with complex relationships

### Sales Module (`src/main/java/my/maleva/api/module/saleorder/`)
- Complex order processing with nested details
- Shows specification pattern for advanced filtering

### Accounting Module (`src/main/java/my/maleva/api/module/accounting/`)
- Expense tracking, GL accounts, claim vouchers
- Has comprehensive tests in `src/test/java/`

### Planning Module (`src/main/java/my/maleva/api/module/planning/`)
- Vessel planning, pre-alerts
- Integration with external systems (QNE)

## 🔗 External Integrations

### QNE Integration (`src/main/java/my/maleva/api/integration/qne/`)
- Custom REST client for external QNE system
- URL builder pattern for complex query construction

### File Handling (`src/main/java/my/maleva/api/module/filehandling/`)
- Image/file upload processing
- PDF support via PDFBox library

## 📊 Performance Optimizations in Place

1. **Connection Pooling:** HikariCP with 30 max connections
2. **Batch Operations:** Hibernate batch size = 25
3. **Lazy Loading:** Properly configured via `@Transactional(readOnly = true)`
4. **Query Optimization:** JPA Specifications for complex queries
5. **Caching:** Second-level cache disabled (not needed for this load)

## 🛠️ Quick Start for New Features

1. **Create module directory:** `src/main/java/my/maleva/api/module/{feature_name}/`
2. **Create subdirectories:** controller, service (with impl/), repository, entity, dto, mapper
3. **Write Entity:** `@Entity` with proper column mappings
4. **Write DTOs:** Request and Response variants with validation
5. **Write Mapper:** MapStruct interface with componentModel="spring"
6. **Write Repository:** Extend `JpaRepository<Entity, Long>`
7. **Write Service:** Interface + ServiceImpl with business logic
8. **Write Controller:** `@RestController` with `ApiResponse<T>` wrapping
9. **Add Tests:** In `src/test/java/my/maleva/api/module/{feature_name}/`
10. **Update Docs:** Reference in `docs/CODING_STANDARDS.md` module table

## 🎯 When Things Break

- **Compilation errors?** Check MapStruct annotation processors in pom.xml (lines 189-205)
- **Connection pool errors?** Verify SQL Server is running and credentials are correct
- **DTO mapping issues?** Ensure field names match or use `@Mapping` annotations in mapper
- **API response format wrong?** Verify using `ApiResponse<T>` not custom wrappers
- **Tests failing?** Check if using `@Transactional` properly in service layer

---

**Reference GEMINI.md for methodology. Reference CODING_STANDARDS.md (923 lines) for complete examples.**

