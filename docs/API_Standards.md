# Maleva API Standards

## Table of Contents
1. [Spring Boot Application Standards](#spring-boot-application-standards)
2. [URL Pattern Standards](#url-pattern-standards)
3. [Project Structure](#project-structure)
4. [Naming Conventions](#naming-conventions)
5. [Response Format Standards](#response-format-standards)
6. [Error Handling](#error-handling)

---

## Spring Boot Application Standards

### 1. Configuration Management

#### Application Properties/YAML
- Store configuration in `application.yaml` (preferred over `.properties`)
- Use environment variables for sensitive data:
  ```yaml
  spring:
    datasource:
      url: ${SPRING_DATASOURCE_URL:jdbc:sqlserver://localhost:1433;databaseName=MalevaDB}
      username: ${SPRING_DATASOURCE_USERNAME:sa}
      password: ${SPRING_DATASOURCE_PASSWORD:password}
  ```

#### Port Configuration
- Default port: `8082` (as configured in `application.yaml`)
- Configure in `application.yaml`:
  ```yaml
  server:
    port: 8082
  ```

#### Multipart File Upload
```yaml
server:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 50MB
```

### 2. Dependency Management

#### Required Dependencies
- Spring Boot Starter Data JPA
- Spring Boot Starter Web MVC
- Spring Boot Starter Security
- Spring Boot Starter Validation
- Lombok (for reducing boilerplate)
- MapStruct (for DTO mapping)
- SQL Server JDBC Driver

### 3. Application Structure

#### Main Application Class
```java
@SpringBootApplication
public class MalevaApplication {
    public static void main(String[] args) {
        SpringApplication.run(MalevaApplication.class, args);
    }
}
```

#### Layer Architecture
```
src/main/java/my/maleva/api/
├── controller/          # REST Controllers
├── service/             # Business Logic (Service Layer)
├── service/impl/        # Service Implementations
├── repo/                # JPA Repositories
├── model/               # JPA Entities
├── dto/                 # Data Transfer Objects
│   ├── request/         # Request DTOs
│   └── response/        # Response DTOs
├── mapper/              # MapStruct Mappers
├── config/              # Configuration Classes
├── exception/           # Custom Exceptions
├── auth/                # Authentication/Authorization
├── util/                # Utility Classes
└── MalevaApplication.java
```

### 4. Security Configuration

#### Role-Based Access Control
- Use `@PreAuthorize` annotation on controllers:
  ```java
  @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
  ```

#### Authentication Providers
- JWT Token-based authentication
- Role mapping: `ROLE_ADMIN`, `ROLE_SUPERADMIN`, `ROLE_100`, etc.

---

## URL Pattern Standards

### 1. Base URL Structure
```
http://localhost:8082/api/{resource}/{action}
```

### 2. RESTful Endpoint Conventions

#### Collection Endpoints (Plural)
- **List All Resources**
  ```
  GET /api/{resources}
  Example: GET /api/customers
  ```

- **Create Resource**
  ```
  POST /api/{resources}
  Example: POST /api/customers
  Body: { ...resource data... }
  ```

#### Individual Resource Endpoints
- **Get Single Resource**
  ```
  GET /api/{resources}/{id}
  Example: GET /api/customers/123
  ```

- **Update Resource**
  ```
  PUT /api/{resources}/{id}
  Example: PUT /api/customers/123
  Body: { ...updated data... }
  ```

- **Delete Resource**
  ```
  DELETE /api/{resources}/{id}
  Example: DELETE /api/customers/123
  ```

### 3. Resource Naming Conventions

#### Naming Rules
- Use **plural nouns** for resource names
- Use **lowercase** with **hyphens** for multi-word resources
- Use **snake_case** or **camelCase** in query parameters

#### Examples
```
✓ /api/customers
✓ /api/payment-vouchers
✓ /api/item-masters
✓ /api/job-status-details
✗ /api/Customer (use lowercase)
✗ /api/payment_vouchers (use hyphens in URLs)
```

### 4. Query Parameters

#### Pagination
```
GET /api/customers?page=0&size=20&sort=id,desc
```

#### Filtering
```
GET /api/customers?companyId=1&active=1&city=Chennai
```

#### Search
```
GET /api/customers?search=John%20Doe
```

### 5. File Upload Endpoints

#### Single File Upload
```
POST /api/file-uploads
Content-Type: multipart/form-data
Parameter: file (required)
```

#### Multiple Files Upload
```
POST /api/file-uploads/multiple
Content-Type: multipart/form-data
Parameter: files (required, array)
```

#### Directory-Specific Upload
```
POST /api/file-uploads/directory/{directory}
Content-Type: multipart/form-data
Parameter: file (required)
```

#### File Deletion
```
DELETE /api/file-uploads/{fileName}
DELETE /api/file-uploads/directory/{directory}/{fileName}
```

### 6. Custom Action Endpoints

#### Special Operations (Non-CRUD)
```
POST /api/{resources}/bulk-upload
POST /api/{resources}/{id}/activate
POST /api/{resources}/{id}/deactivate
POST /api/{resources}/search
```

#### Examples
```
GET /api/customers/search?name=John
POST /api/bill-masters/123/print
POST /api/payments/bulk-upload
```

### 7. Versioning (Optional)

#### URL-based Versioning
```
GET /api/v1/customers
GET /api/v2/customers
```

---

## Project Structure

### Package Organization
```
my.maleva.api
├── model/               # JPA Entities (@Entity)
├── dto/                 # DTOs for API contracts
│   ├── request/        # Request DTOs
│   └── response/       # Response DTOs
├── repo/               # JPA Repositories (@Repository)
├── service/            # Service interfaces & implementations
├── mapper/             # MapStruct Mappers
├── controller/         # REST Controllers (@RestController)
├── config/             # Spring Configuration (@Configuration)
├── exception/          # Custom Exception Classes
├── auth/               # Authentication & Authorization
└── util/               # Utility & Helper Classes
```

---

## Naming Conventions

### Class Naming
| Type | Suffix | Example |
|------|--------|---------|
| Entity | (none) | `Customer`, `Payment`, `Invoice` |
| DTO | `Dto` | `CustomerDto`, `PaymentDto` |
| Request DTO | `RequestDto` | `CustomerRequestDto` |
| Response DTO | `ResponseDto` | `CustomerResponseDto` |
| Repository | `Repository` | `CustomerRepository` |
| Service | `Service` | `CustomerService` |
| Mapper | `Mapper` | `CustomerMapper` |
| Controller | `Controller` | `CustomerController` |
| Configuration | `Config` | `FileUploadConfig` |

### Method Naming
- **Get/Retrieve**: `get()`, `getById()`, `listAll()`, `findAll()`
- **Create**: `create()`, `save()`
- **Update**: `update()`
- **Delete**: `delete()`, `deleteById()`
- **Search**: `search()`, `findBy*()`

### Variable Naming
- Use **camelCase** for variables and parameters
- Use **UPPER_SNAKE_CASE** for constants
- Prefix boolean variables with `is` or `has`

---

## Response Format Standards

### 1. Successful Response (2xx)

#### Single Resource
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "createdDate": "2026-02-15T10:30:00",
  "modifiedDate": "2026-02-15T10:30:00"
}
```

#### List of Resources
```json
[
  {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com"
  },
  {
    "id": 2,
    "name": "Jane Smith",
    "email": "jane@example.com"
  }
]
```

#### Paginated Response
```json
{
  "content": [
    { "id": 1, "name": "John Doe" }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 100,
  "totalPages": 5
}
```

### 2. Error Response (4xx, 5xx)

#### Standard Error Format
```json
{
  "timestamp": "2026-02-15T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Customer not found with id: 123",
  "path": "/api/customers/123",
  "details": null
}
```

#### Validation Error
```json
{
  "timestamp": "2026-02-15T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/customers",
  "details": [
    "Name is required",
    "Email format is invalid"
  ]
}
```

### 3. HTTP Status Codes

| Code | Usage | Example |
|------|-------|---------|
| 200 | GET successful | `GET /api/customers/1` |
| 201 | POST successful | `POST /api/customers` |
| 204 | DELETE successful | `DELETE /api/customers/1` |
| 400 | Bad Request | Invalid input data |
| 401 | Unauthorized | Missing/invalid token |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Duplicate entry |
| 500 | Server Error | Unhandled exception |

---

## Error Handling

### 1. Custom Exception Classes

#### EntityNotFoundException
```java
throw new EntityNotFoundException("Customer not found: " + id);
```

#### InvalidRequestException
```java
throw new InvalidRequestException("File size exceeds maximum limit");
```

### 2. Global Exception Handler

#### Implementation
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(
        EntityNotFoundException ex, WebRequest request) {
        // Handle not found errors
    }
    
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiError> handleInvalidRequest(
        InvalidRequestException ex, WebRequest request) {
        // Handle invalid request errors
    }
}
```

### 3. Common Error Scenarios

| Scenario | Exception | Status Code |
|----------|-----------|-------------|
| Resource not found | `EntityNotFoundException` | 404 |
| Invalid input | `InvalidRequestException` | 400 |
| Duplicate entry | `InvalidRequestException` | 409 |
| Access denied | Spring Security | 403 |
| Validation failed | `MethodArgumentNotValidException` | 400 |
| Server error | `Exception` | 500 |

---

## API Documentation Standards

### 1. Swagger/OpenAPI Annotations

#### Controller Class
```java
@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customers", description = "Customer Management APIs")
public class CustomerController {
    
    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID")
    @ApiResponse(responseCode = "200", description = "Customer found")
    @ApiResponse(responseCode = "404", description = "Customer not found")
    public CustomerDto get(@PathVariable Integer id) {
        // Implementation
    }
}
```

### 2. JavaDoc Comments
```java
/**
 * Retrieves a customer by their ID.
 *
 * @param id the customer ID
 * @return the customer details
 * @throws EntityNotFoundException if customer not found
 */
public CustomerDto get(Integer id) {
    // Implementation
}
```

---

## Best Practices

1. **Always use DTOs** for request/response instead of entities
2. **Implement MapStruct mappers** for entity-DTO conversions
3. **Use @Transactional** on service methods that modify data
4. **Validate input** using jakarta.validation annotations
5. **Handle exceptions gracefully** with custom handlers
6. **Use appropriate HTTP status codes**
7. **Document APIs** with Swagger/OpenAPI annotations
8. **Follow RESTful principles** strictly
9. **Use pagination** for large datasets
10. **Secure endpoints** with @PreAuthorize

---

## Example Implementation

### Complete CRUD Endpoint Example

```
Resource: /api/customers

GET    /api/customers           → List all customers
POST   /api/customers           → Create new customer
GET    /api/customers/1         → Get customer by ID
PUT    /api/customers/1         → Update customer
DELETE /api/customers/1         → Delete customer
```

### Request/Response Flow

1. **Controller** receives request
2. **Controller** validates input using DTOs
3. **Service** processes business logic
4. **Repository** interacts with database
5. **Mapper** converts Entity to DTO
6. **Controller** returns response with appropriate HTTP status

