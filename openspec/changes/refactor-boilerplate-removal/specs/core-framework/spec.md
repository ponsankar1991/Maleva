## ADDED Requirements

### Requirement: Base Audit Entity
The system SHALL provide a `@MappedSuperclass` named `BaseAuditEntity` that defines common audit fields and primary key management for entities.

#### Scenario: Base Audit Entity contains required fields
- **WHEN** an entity inherits from `BaseAuditEntity`
- **THEN** it MUST include fields for `id` (UUID), `createdDate` (LocalDateTime), `modifiedDate` (LocalDateTime), and `modifiedBy` (String)

### Requirement: JPA Auditing Configuration
The system SHALL be configured to automatically populate `createdDate`, `modifiedDate`, and `modifiedBy` using Spring Data JPA Auditing.

#### Scenario: Automatic population of audit fields
- **WHEN** a new entity inheriting from `BaseAuditEntity` is saved
- **THEN** the `createdDate` and `modifiedDate` MUST be set to the current system time, and `modifiedBy` MUST be set to the current authenticated user

### Requirement: Generic Base Service
The system SHALL provide a generic `BaseService` class that implements standard CRUD operations for any entity-DTO pair.

#### Scenario: Standard CRUD operations in Base Service
- **WHEN** a concrete service extends `BaseService`
- **THEN** it MUST provide implementations for `listAll()`, `getById()`, `create()`, `update()`, and `delete()` without requiring manual repository calls for each

### Requirement: Generic Base Controller
The system SHALL provide a generic `BaseController` class that defines standard REST endpoints for CRUD operations.

#### Scenario: Standard REST endpoints in Base Controller
- **WHEN** a concrete controller extends `BaseController`
- **THEN** it MUST expose GET (all), GET (by ID), POST (create), PUT (update), and DELETE (by ID) endpoints following standard REST conventions
