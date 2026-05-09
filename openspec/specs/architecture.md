# Project Architecture Specification: Maleva Full-Stack

## Overview
Maleva is a comprehensive business management system. It follows a modular architecture where the backend provides a RESTful API and the frontend provides a modern user interface.

## System Components

### 1. Backend (Java Spring Boot)
- **Location**: `C:\karthickworkspace\malevanew\fulstack\Maleva`
- **Structure**: Organized by business modules under `src/main/java/my/maleva/api/module/`.
- **Core Modules**:
    - `accounting`, `billing`, `purchase`, `saleorder`, `inventory` (stockin), `customer`, `supplier`, etc.
- **Responsibilities**: Data persistence, business logic validation, security, and API exposure.

### 2. Frontend (React + TypeScript)
- **Location**: `C:\karthickworkspace\malevanew\fulstack\maleva-front-end`
- **Structure**: Feature-based organization in `src/features/`.
- **Responsibilities**: User experience, state management, API consumption, and data visualization.

## Integration Layer
- **Communication**: JSON over HTTP (REST).
- **Authentication**: Handled by the backend security module.
- **Synchronization**: The `openspec` directory serves as the source of truth for API contracts and feature requirements.

## Development Workflow (OpenSpec Flow)
1. **Proposal**: New features or changes are proposed as documents in `openspec/changes/`.
2. **Specification**: Once approved, the proposal is converted into a formal specification in `openspec/specs/`.
3. **Implementation**: Code is written in both backend and frontend to satisfy the specification.
4. **Verification**: Changes are verified against the specification before being merged.
