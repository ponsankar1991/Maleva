## Context

The Maleva project follows a modular structure where each module (accounting, billing, etc.) typically provides standard CRUD (Create, Read, Update, Delete) operations for its entities. Currently, these operations are implemented manually in every controller and service, leading to significant repetition of code that manages repository calls, DTO mapping, and basic validation. Furthermore, audit fields such as `createdDate` and `modifiedDate` are present in most entities but are updated manually in service methods, which is inconsistent and error-prone.

## Goals / Non-Goals

**Goals:**
- **Centralize CRUD Logic**: Implement generic base classes for controllers and services to handle standard operations.
- **Automate Auditing**: Leverage Spring Data JPA's auditing features to automatically manage timestamp and user audit fields.
- **Reduce Maintenance Burden**: Minimize the amount of code required to add or modify standard entity management.
- **Ensure Consistency**: Standardize REST API patterns and error handling across refactored modules.

**Non-Goals:**
- **Replace Complex Logic**: Specialized business logic or complex multi-entity transactions will remain in concrete service implementations.
- **Schema Migration**: This refactoring will not change the existing database schema, only how the fields are managed in Java.
- **Full Project Refactor**: While the core framework will be available to all, only selected modules (starting with `accounting`) will be refactored in this change.

## Decisions

### 1. JPA Auditing with `BaseAuditEntity`
- **Decision**: Create a `@MappedSuperclass` named `BaseAuditEntity` that contains common fields: `id`, `createdDate`, `modifiedDate`, and `modifiedBy`.
- **Rationale**: Centralizing these fields ensures consistency. Using `@EntityListeners(AuditingEntityListener.class)` allows Spring to automatically populate these fields.
- **Alternatives**: 
    - *Manual Updates*: Current state, rejected due to duplication.
    - *AOP Aspect*: Possible but more complex to maintain than native JPA features.

### 2. Generic `BaseService` Implementation
- **Decision**: Implement `public abstract class BaseService<E, D, ID>` where `E` is the Entity, `D` is the DTO, and `ID` is the ID type (e.g., `UUID`).
- **Rationale**: Standardizes the flow of: `ID -> Entity -> DTO` and `DTO -> Entity -> Save -> DTO`.
- **Implementation**: Will require a `JpaRepository<E, ID>` and a MapStruct mapper to be provided by concrete implementations.

### 3. Generic `BaseController` Implementation
- **Decision**: Implement `public abstract class BaseController<D, ID>` providing `@GetMapping`, `@PostMapping`, `@PutMapping`, and `@DeleteMapping` endpoints.
- **Rationale**: Ensures that all basic CRUD endpoints follow the same URL patterns and return consistent status codes (e.g., 201 Created for POST).

### 4. `AuditorAware` for User Tracking
- **Decision**: Implement `AuditorAware<String>` to fetch the current username from the `SecurityContextHolder`.
- **Rationale**: Automates the `modifiedBy` field population without requiring service-layer intervention.

## Risks / Trade-offs

- **[Risk] Generic Rigidity** → Standard CRUD might not fit all entities perfectly.
    - **Mitigation**: Base classes will provide `protected` methods that can be overridden by concrete classes for customization (e.g., adding specific search criteria).
- **[Risk] Mapper Injection in Generics** → Injecting generic mappers can sometimes be tricky with Spring/MapStruct.
    - **Mitigation**: Concrete services will explicitly pass the mapper to the `super` constructor, ensuring type safety and proper injection.
- **[Risk] Breakage of Existing Tests** → Refactoring core logic may break existing unit or integration tests.
    - **Mitigation**: Run existing tests (`EmployeeMasterControllerTest`, etc.) frequently during refactoring and update them as necessary to reflect the new structure.
