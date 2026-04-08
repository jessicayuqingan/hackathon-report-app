# Report App Presentation Summary

## Executive Summary

This application is a report generation and approval platform built around a maker-checker control model.

It allows users to:

- authenticate with JWT
- browse report definitions
- execute report queries
- preserve execution snapshots
- submit runs for approval
- approve or reject submitted runs
- review audit history
- export reports to Excel

## One-Slide System Description

### What it is

A two-tier business reporting system with:

- Angular frontend
- Spring Boot backend
- H2 runtime database
- JWT security
- Excel export templates

### Why it matters

The core value is not only report generation, but controlled report lifecycle management:

- execution traceability
- maker-checker approval
- auditability
- exportable historical outputs

## Architecture at a Glance

### Frontend

- login and token storage
- report browsing and execution
- submission and approval UI
- audit timeline visualization
- Excel download handling

### Backend

- authentication and authorization
- report catalog APIs
- report execution workflow
- run status transitions
- audit event persistence
- Excel rendering

### Database

- `users`
- `report_config`
- `report_run`
- `report_audit_event`

## Key Business Workflow

### 1. Authenticate

- user logs in
- backend returns JWT and profile
- frontend stores token and reuses it for API calls

### 2. Execute report

- maker chooses a report
- backend executes saved SQL
- result is returned to UI
- snapshot is persisted as a `ReportRun`
- `Generated` audit event is recorded

### 3. Submit for approval

- maker submits generated run
- status becomes `Submitted`
- audit event is recorded

### 4. Approve or reject

- checker reviews submitted run
- status becomes `Approved` or `Rejected`
- decision metadata and comment are stored
- audit event is recorded

### 5. Export and audit

- users export latest report or a specific run
- users inspect end-to-end audit trail for each run

## Core Entities

### `Report`

Stores report definition metadata and SQL.

### `ReportRun`

Stores execution instance data, lifecycle state, and optional output snapshot.

### `ReportAuditEvent`

Stores chronological workflow events for traceability.

### `User`

Stores username, password hash, and role set.

## Architecture Strengths

- clear business workflow
- explicit maker-checker separation
- persistent audit trail
- run snapshot preservation
- dedicated export service

## Main Risks

### Raw SQL execution

The current implementation directly executes report SQL, which creates:

- security risk
- maintainability risk
- limited validation capability

### Mixed persistence model

The app mixes JDBC DAO and JPA repository styles.

### Demo-only storage profile

The H2 in-memory setup is useful for demos, but not durable.

## Recommended Slide Talking Points

- The system is a report workflow platform, not just a reporting UI
- `ReportRun` is the key business record
- `ReportAuditEvent` provides end-to-end traceability
- The largest refactor opportunity is replacing raw SQL execution with safer backend business logic

## Related Artifacts

- `docs/architecture/report-app-architecture.md`
- `docs/architecture/report-app-diagrams.puml`
- `docs/architecture/report-app-diagram.drawio`
- `docs/architecture/report-app-slide-diagram.puml`
