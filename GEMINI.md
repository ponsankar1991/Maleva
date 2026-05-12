# 🧞 Maleva API - Project Instructions

This document provides foundational guidance for interacting with the Maleva API codebase. Adherence to these instructions is mandatory to maintain architectural integrity and consistency.

## 🏗️ Project Architecture

Maleva is a Spring Boot 4.x application following a modular, layered architecture.

- **Primary Stack:** Java 17, Spring Boot, Spring Data JPA, Hibernate, MapStruct, Lombok, SQL Server.
- **Security:** JWT-based authentication with Spring Security.
- **API Documentation:** SpringDoc OpenAPI (Swagger).
- **Communication:** RESTful APIs with `ApiResponse<T>` wrapper.

## 📁 Key Directories

- `src/main/java/my/maleva/api/`: Root package.
- `common/`: Shared configurations, constants, utilities, and base classes.
- `module/`: Business modules (e.g., `sale`, `purchase`, `customer`). Each module follows a strictly defined internal structure.
- `security/`: Authentication and JWT logic.
- `integration/`: External service clients (e.g., QNE integration).
- `docs/`: Extensive project documentation and standards.
- `openspec/`: Experimental change management system.

## 📜 Coding Standards & Conventions

Refer to `docs/CODING_STANDARDS.md` and `docs/API_Standards.md` for detailed rules.

- **Naming:** Follow established patterns: `XxxController`, `XxxService`, `XxxServiceImpl`, `XxxRepository`, `XxxMapper`, `XxxDto`, `XxxRequest`, `XxxResponse`.
- **Modularity:** Business logic MUST be encapsulated within the appropriate `module/xxx` package.
- **DTOs:** ALWAYS use DTOs for API requests and responses. NEVER expose JPA entities directly.
- **Mappers:** Use MapStruct for Entity <-> DTO conversions.
- **Validation:** Use Jakarta Validation annotations on DTOs.
- **Exception Handling:** Use `GlobalExceptionHandler` and custom exceptions.
- **API Response:** Wrap all responses in `ApiResponse<T>`.

## 🔄 Development Workflow

1. **Research:** Map the codebase and validate assumptions. For bug fixes, EMPIRICALLY reproduce the failure first.
2. **Strategy:** Formulate a plan and share a concise summary.
3. **Execution (Plan -> Act -> Validate):**
   - **Plan:** Define implementation and testing strategy.
   - **Act:** Apply surgical, idiomatic changes.
   - **Validate:** Run tests and verify against project standards.

### 🧪 Testing Requirement

A change is incomplete without verification logic. ALWAYS search for and update related tests. Add new test cases to verify your changes.

### 🚀 OpenSpec Workflow

For complex features or architectural changes, utilize the `openspec` skills:
- `openspec-propose`: Generate a full proposal (design, specs, tasks).
- `openspec-apply-change`: Implement tasks from a change.
- `openspec-archive-change`: Finalize and archive a completed change.

## 🛠️ Tooling & Commands

- **Build/Test:** `./mvnw clean install`
- **Run:** `./mvnw spring-boot:run`
- **Docker:** `docker-compose up -d`
- **Verify:** `build-verify.bat` (Windows)

## ⚠️ Security Mandates

- NEVER log or commit secrets, API keys, or credentials.
- Protect `.env` and configuration files.
- Adhere to the established RBAC using `@PreAuthorize`.
