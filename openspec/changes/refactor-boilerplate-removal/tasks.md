## 1. Core Framework Setup

- [x] 1.1 Enable JPA Auditing by adding `@EnableJpaAuditing` to `JpaConfig` or a new config class
- [x] 1.2 Create `BaseAuditEntity` in `my.maleva.api.common.entity` as a `@MappedSuperclass`
- [x] 1.3 Implement `AuditorAware<String>` to provide the current username from `SecurityContextHolder`
- [x] 1.4 Create generic `BaseService<E, D, ID>` in `my.maleva.api.common.service` to handle CRUD operations
- [x] 1.5 Create generic `BaseController<D, ID>` in `my.maleva.api.common.controller` to handle standard REST endpoints

## 2. Accounting Module Refactoring

- [x] 2.1 Update `Account` entity to extend `BaseAuditEntity` and remove redundant fields
- [x] 2.2 Update `AccountService` to extend `BaseService` and remove boilerplate CRUD logic
- [x] 2.3 Update `AccountController` to extend `BaseController` and remove boilerplate endpoint logic
- [x] 2.4 Update `GLAccounts` entity to extend `BaseAuditEntity` and remove redundant fields
- [x] 2.5 Update `GLAccountsService` to extend `BaseService` and remove boilerplate CRUD logic
- [x] 2.6 Update `GLAccountsController` to extend `BaseController` and remove boilerplate endpoint logic

## 3. Verification & Testing

- [x] 3.1 Run existing module tests to ensure no regressions in standard functionality
- [x] 3.2 Verify that `createdDate` and `modifiedDate` are automatically populated upon entity creation/update
- [x] 3.3 Perform manual integration testing of refactored REST endpoints using Postman or curl
