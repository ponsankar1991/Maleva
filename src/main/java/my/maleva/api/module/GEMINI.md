# 🧩 Module Guidance - GEMINI.md

This document provides specialized instructions for working within the `my.maleva.api.module` package.

## 🏗️ Module Internal Structure

Every business module MUST adhere to the following structure:

```
📂 module_name/
├── 📂 controller/       # REST API endpoints (@RestController)
├── 📂 dto/              # Data Transfer Objects
│   ├── 📂 request/      # Request-specific DTOs
│   └── 📂 response/     # Response-specific DTOs
├── 📂 entity/           # JPA Entities (@Entity)
├── 📂 mapper/           # MapStruct Mappers (@Mapper)
├── 📂 repository/       # Spring Data JPA Repositories (@Repository)
├── 📂 service/          # Business Logic
│   ├── XxxService.java  # Interface
│   └── 📂 impl/         # Implementation (@Service)
└── 📂 specification/    # JPA Specifications (optional)
```

## 📜 Module Conventions

- **Isolation:** Minimize cross-module dependencies. If a module needs to interact with another, it should ideally do so through the service layer.
- **Base Classes:** Inherit from `BaseAuditEntity` for auditable entities and `BaseController` where appropriate.
- **Transaction Management:** Use `@Transactional` at the service implementation level. Prefer `readOnly = true` for read operations.
- **Mapping:** MapStruct mappers should be configured with `componentModel = "spring"`.
- **API Endpoints:**
  - Base path: `/api/v1/{resource-plural}`
  - Use appropriate HTTP methods (GET, POST, PUT, DELETE).
  - Use `ApiResponse<T>` for all responses.

## ✅ New Module Checklist

When creating a new module, ensure all layers are implemented and verified:
1. JPA Entity with proper mapping.
2. Repository for data access.
3. Request/Response DTOs with validation.
4. MapStruct Mapper.
5. Service Interface and Implementation.
6. REST Controller with Swagger annotations.
7. Unit and Integration tests.
