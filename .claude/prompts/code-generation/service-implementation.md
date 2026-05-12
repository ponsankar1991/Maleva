# Service Implementation Prompt Template

## 📝 Description
Use this prompt to generate Service layer classes with business logic implementation.

## 🎯 How to Use
1. Copy the entire content below
2. Replace all `[PLACEHOLDER]` values
3. Paste the complete prompt to GitHub Copilot

---

## 📋 Service Implementation Prompt

I need to create Service layer classes for the Maleva Java Spring Boot application. Please follow these requirements:

### Service Information
- **Entity Class:** `my.maleva.api.model.[ENTITY_CLASS_NAME]`
- **DTO Class:** `my.maleva.api.dto.response.[ENTITY_CLASS_NAME]Dto`
- **Request DTO:** `my.maleva.api.dto.request.[ENTITY_CLASS_NAME]RequestDto`
- **Repository Class:** `my.maleva.api.repository.[ENTITY_CLASS_NAME]Repository`
- **Mapper Class:** `my.maleva.api.mapper.[ENTITY_CLASS_NAME]Mapper`
- **Service Package:** `my.maleva.api.service`
- **Implementation Package:** `my.maleva.api.service.impl`

### Service Interface Methods

#### 1. **Create Method**
```java
[ENTITY_CLASS_NAME]Dto createItem([ENTITY_CLASS_NAME]RequestDto requestDto, Integer companyId, String loggedInUser);
```

#### 2. **Get All Method (Paginated)**
```java
Page<[ENTITY_CLASS_NAME]Dto> getAll(Pageable pageable, Integer companyId);
```

#### 3. **Get by ID Method**
```java
[ENTITY_CLASS_NAME]Dto getById(Integer id, Integer companyId);
```

#### 4. **Update Method**
```java
[ENTITY_CLASS_NAME]Dto update(Integer id, [ENTITY_CLASS_NAME]RequestDto requestDto, Integer companyId, String loggedInUser);
```

#### 5. **Delete Method**
```java
void delete(Integer id, Integer companyId);
```

#### 6. **Get by Company Method**
```java
List<[ENTITY_CLASS_NAME]Dto> getByCompanyId(Integer companyId, Pageable pageable);
```

#### 7. **Search Method**
```java
List<[ENTITY_CLASS_NAME]Dto> search([ENTITY_CLASS_NAME]SearchFilterDto filters);
```

### Implementation Requirements

1. **Service Interface:**
   - Create interface in `my.maleva.api.service`
   - Define all abstract methods
   - Add Javadoc for each method

2. **Service Implementation Class:**
   - Implement interface with `@Service` annotation
   - Inject Repository using `@Autowired`
   - Inject Mapper using `@Autowired`
   - Add logging with `@Slf4j`
   - Use `@Transactional` for write operations

3. **Business Logic:**
   - Validate required fields before create/update
   - Check company ID access control
   - Implement soft delete (set active/deleted flag)
   - Track created/modified timestamps
   - Throw appropriate exceptions (ResourceNotFoundException, ValidationException)
   - Log all significant operations (INFO, WARN, ERROR)

4. **Error Handling:**
   - Check if resource exists (404 if not)
   - Check authorization (company ID match)
   - Validate business rules
   - Return meaningful error messages

5. **Data Mapping:**
   - Use MapStruct mapper for Entity ↔ DTO conversion
   - Handle null values properly
   - Convert timestamps correctly

### Business Rules to Check
[ADD_YOUR_BUSINESS_RULES]

Example:
- Active status must be 0 or 1
- Created date should be current timestamp
- Modified date should be updated on changes
- Company ID cannot be null

### Exception Types Used
- `ResourceNotFoundException` - Resource not found
- `ValidationException` - Validation failed
- `AccessDeniedException` - Company ID doesn't match
- `DataIntegrityViolationException` - Database constraint violation

### Required Annotations
- `@Service`
- `@Transactional`
- `@Autowired`
- `@Slf4j`
- `@Override`

### Output Locations
- Interface: `src/main/java/my/maleva/api/service/[ENTITY_CLASS_NAME]Service.java`
- Implementation: `src/main/java/my/maleva/api/service/impl/[ENTITY_CLASS_NAME]ServiceImpl.java`

---

## ✅ Expected Output

Service layer should:
- ✅ Service interface with all methods
- ✅ Implementation class with business logic
- ✅ Proper error handling and validation
- ✅ Pagination support
- ✅ Company data isolation
- ✅ Comprehensive logging
- ✅ Transactional operations
- ✅ Mapper integration

---

## 🔗 Next Steps

1. Create Repository using `repository-queries.md`
2. Create MapStruct Mapper using `mapstruct-mapper.md`
3. Create Controller using `crud-endpoints.md`
4. Write Test Cases using `../testing/test-cases.md`

