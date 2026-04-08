# Report App Architecture

## Overview

This project is a report generation and approval platform with a maker-checker workflow.

It consists of:

- An Angular frontend in `frontend/`
- A Spring Boot backend in `backend/`
- An H2 in-memory database for runtime storage
- JWT-based authentication and authorization
- Excel template-based report export

The core business process is:

1. User logs in
2. User loads available report definitions
3. Maker executes a report
4. System stores a report run snapshot
5. Maker submits the run for approval
6. Checker approves or rejects the run
7. Users review audit history and export results

## System Context

### Frontend

The Angular frontend is responsible for:

- authenticating the user
- storing JWT and user metadata in `localStorage`
- attaching the JWT to backend API requests
- loading reports
- executing reports
- submitting runs for approval
- approving or rejecting runs
- rendering audit timelines
- downloading Excel exports

Relevant files:

- `frontend/src/app/services/auth.service.ts`
- `frontend/src/app/services/auth.interceptor.ts`
- `frontend/src/app/services/report.service.ts`
- `frontend/src/app/components/report/report-viewer.component.ts`
- `frontend/src/app/components/report/report-run-flow.component.ts`
- `frontend/src/app/app.routes.ts`

### Backend

The Spring Boot backend is responsible for:

- JWT authentication
- role-based access control
- loading report definitions
- executing report SQL
- persisting report run records
- persisting audit events
- exporting report results to Excel

Relevant files:

- `backend/src/main/java/com/legacy/report/controller/AuthController.java`
- `backend/src/main/java/com/legacy/report/controller/ReportController.java`
- `backend/src/main/java/com/legacy/report/controller/ReportRunController.java`
- `backend/src/main/java/com/legacy/report/service/AuthService.java`
- `backend/src/main/java/com/legacy/report/service/CurrentUserService.java`
- `backend/src/main/java/com/legacy/report/service/ReportService.java`
- `backend/src/main/java/com/legacy/report/service/ReportRunService.java`
- `backend/src/main/java/com/legacy/report/service/AuditService.java`
- `backend/src/main/java/com/legacy/report/service/ReportExcelExportService.java`

### Storage

The application uses H2 in-memory storage, configured in:

- `backend/src/main/resources/application.yml`

Primary tables/entities:

- `users`
- `report_config`
- `report_run`
- `report_audit_event`

## Main Components and Responsibilities

### Authentication

- `AuthController` exposes `/api/auth/login` and `/api/auth/profile`
- `AuthService` validates credentials and issues JWTs
- `SecurityConfig` protects all API endpoints except login/logout/test endpoints
- `CurrentUserService` resolves the current authenticated user and validates roles

### Report Catalog

- `ReportController` exposes report catalog endpoints
- `ReportService` contains report-related business operations
- `ReportDao` loads report definitions from `report_config`

### Report Execution Workflow

- `ReportRunService.executeReportWithRun()` is the core workflow entry point
- It verifies the current user has `MAKER` role
- It loads the report definition
- It executes the SQL through `ReportService`
- It stores a `ReportRun`
- It writes a `ReportAuditEvent`
- It returns the raw report rows back to the frontend

### Approval Workflow

- `submitRun()` transitions a run from `Generated` to `Submitted`
- `decideRun()` transitions a run from `Submitted` to `Approved` or `Rejected`
- Approval actions are restricted to `CHECKER`
- All state changes are captured in `report_audit_event`

### Export Workflow

- `ReportExcelExportService` renders `.xlsx` files from templates
- Latest report export can re-execute current SQL
- Run export prefers `resultSnapshot` stored in `ReportRun`
- Export actions also create audit events

## Primary User Roles

### Maker

A maker can:

- log in
- browse reports
- execute a report
- view their own report runs
- submit a run for approval
- export report output

### Checker

A checker can:

- log in
- view submitted runs
- approve or reject a run
- provide rejection comments
- review audit history
- export a run

## End-to-End Data Flow

### 1. Login

1. Frontend sends `POST /api/auth/login`
2. Backend verifies username/password using `UserRepository`
3. Backend returns JWT and user profile
4. Frontend stores token and user data in `localStorage`
5. Angular interceptor attaches the token to future API requests

### 2. Load Report Definitions

1. Frontend calls `GET /api/reports`
2. Backend routes request to `ReportController`
3. `ReportService` delegates to `ReportDao`
4. `ReportDao` reads active rows from `report_config`
5. Frontend displays available reports

### 3. Execute Report

1. Maker selects a report
2. Frontend calls `POST /api/reports/{id}/execute`
3. Backend validates JWT and maker role
4. `ReportRunService` loads report metadata
5. `ReportService.runReport()` executes the saved SQL
6. `ReportRunService` stores the output snapshot in `report_run.resultSnapshot`
7. `AuditService` stores a `Generated` event in `report_audit_event`
8. Backend returns result rows to frontend for display

### 4. Submit for Approval

1. Maker submits a generated run
2. Frontend calls `POST /api/report-runs/{id}/submit`
3. Backend verifies maker ownership and current state
4. `report_run.status` changes to `Submitted`
5. `submitted_at` is stored
6. `AuditService` stores a `Submitted` event

### 5. Approve or Reject

1. Checker loads submitted runs via `GET /api/report-runs/submitted`
2. Checker submits a decision via `POST /api/report-runs/{id}/decision`
3. Backend verifies checker role and current state
4. `report_run.status` changes to `Approved` or `Rejected`
5. `checker_username` and `decided_at` are stored
6. `AuditService` stores an `Approved` or `Rejected` event

### 6. Review Audit Trail

1. Frontend loads `GET /api/report-runs/{id}/audit`
2. Backend queries `report_audit_event`
3. Frontend renders a chronological timeline

### 7. Export Report

1. Frontend calls either report export or run export endpoint
2. Backend loads source data
3. Backend loads Excel template from `report-templates`
4. `ReportExcelExportService` renders workbook bytes
5. Backend returns blob response to frontend
6. Frontend triggers browser download

## Data Model

### `User`

Represents an authenticated application user.

Fields:

- `id`
- `username`
- `password`
- `role`

### `Report`

Represents a report definition loaded from `report_config`.

Fields:

- `id`
- `name`
- `sql`
- `description`

### `ReportRun`

Represents one executed instance of a report.

Fields:

- `id`
- `version`
- `reportId`
- `reportName`
- `status`
- `makerUsername`
- `checkerUsername`
- `generatedAt`
- `submittedAt`
- `decidedAt`
- `parametersJson`
- `resultSnapshot`

### `ReportAuditEvent`

Represents a traceable lifecycle event for a report run.

Fields:

- `id`
- `reportRunId`
- `reportId`
- `actorUsername`
- `actorRole`
- `eventType`
- `eventTime`
- `comment`

## Logical Relationships

- One `Report` can produce many `ReportRun` records
- One `ReportRun` can produce many `ReportAuditEvent` records
- One `User` can act as maker for many report runs
- One `User` can act as checker for many report runs
- One `User` can generate many audit events

## API Surface Relevant to Data Flow

### Auth APIs

- `POST /api/auth/login`
- `GET /api/auth/profile`
- `POST /api/auth/logout`

### Report APIs

- `GET /api/reports`
- `GET /api/reports/{id}`
- `POST /api/reports/{id}/execute`
- `GET /api/reports/{id}/export`
- `POST /api/reports`
- `POST /api/reports/generate`
- `POST /api/reports/run`

### Report Run APIs

- `POST /api/report-runs/{id}/submit`
- `POST /api/report-runs/{id}/decision`
- `GET /api/report-runs/my-latest`
- `GET /api/report-runs/my-runs`
- `GET /api/report-runs/submitted`
- `GET /api/report-runs/checker/history`
- `GET /api/report-runs/{id}/audit`
- `GET /api/report-runs/{id}/export`

## Technical Strengths

- Clear maker-checker workflow concept
- Audit history is persisted explicitly
- Result snapshots support historical export and traceability
- JWT authentication is integrated across frontend and backend
- Export pipeline is separated into a dedicated service

## Key Risks and Improvement Areas

### Direct SQL execution

The highest architectural risk is direct execution of report SQL.

- `ReportService.runReport()` executes raw SQL
- `POST /api/reports/run` accepts arbitrary SQL from request body
- `generateReport()` appends parameters directly into SQL text

This creates significant security and maintainability risks.

### Mixed persistence patterns

The system uses:

- JDBC DAO for report definitions
- JPA repositories for workflow entities

This is workable, but inconsistent and harder to evolve.

### In-memory database

`jdbc:h2:mem:reportdb` means runtime data is ephemeral.

This is suitable for demo or hackathon use, but not for durable environments.

### Hardcoded frontend backend URL

Frontend services point directly to `http://localhost:8080/api`, which should be externalized for deployment.

## Recommended Next Steps

- Replace raw SQL execution with a safer query abstraction or controlled parameterization layer
- Move report business logic out of SQL into Java services where appropriate
- Standardize data access patterns
- Externalize API base URL through Angular environment configuration
- Add a persistent database profile for non-demo environments
- Add API-level validation and structured error responses

## Artifact Index

This document is accompanied by:

- `docs/architecture/report-app-diagrams.puml`
- `docs/architecture/report-app-diagram.drawio`
