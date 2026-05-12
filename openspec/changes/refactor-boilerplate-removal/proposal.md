## Why

The Maleva project contains significant code duplication across its modules, particularly in standard CRUD (Create, Read, Update, Delete) operations. Currently, each entity has its own controller and service that manually implements identical logic for listing, fetching, creating, updating, and deleting records. This "boilerplate" code increases the codebase size, makes maintenance difficult, and slows down the development of new features. Furthermore, audit field management (tracking when and by whom a record was created or modified) is handled manually in every service implementation, which is repetitive and prone to inconsistency.

## What Changes

- **Infrastructure: JPA Auditing**: Enable Spring Data JPA auditing to automatically manage `createdDate`, `modifiedDate`, and `modifiedBy` fields.
- **Infrastructure: Base Classes**: 
    - `BaseAuditEntity`: A mapped superclass for entities to inherit common audit fields.
    - `BaseService<E, D, ID>`: A generic service class providing standard CRUD logic, reducing implementation effort in concrete services.
    - `BaseController<D, ID>`: A generic controller class providing standard REST endpoints, ensuring consistent API behavior.
- **Refactoring: Module Cleanup**: Transition existing modules (starting with `accounting`) to utilize these base components, removing redundant code from their respective controllers and services.
- **Cleanup: Exception Handling**: Refine `GlobalExceptionHandler` to better handle common errors consistently across the new generic components.

## Capabilities

### New Capabilities
- `core-framework`: Implementation of base entities, generic services, and base controllers along with JPA auditing configuration.
- `accounting-refactor`: Refactoring of the accounting module to adopt the new core framework patterns.

### Modified Capabilities
<!-- No existing capabilities found in openspec/specs -->

## Impact

- **Core Infrastructure**: New base classes in `my.maleva.api.common`.
- **Accounting Module**: Controllers, services, and entities in `my.maleva.api.module.accounting` will be refactored.
- **Database Access**: Standardized handling of audit fields across all refactored entities.
- **API Consistency**: More consistent response structures and error handling across all refactored endpoints.
