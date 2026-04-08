# Report App Functional Specification Document

## 1. Document Purpose

This document defines the functional specification for the `hackathon-report-app` project.

It describes:

- the business purpose of the application
- the supported user roles
- the major functional modules
- the end-to-end business workflows
- the core data entities
- the functional and non-functional constraints visible in the current implementation
- the main gaps and future enhancement opportunities

This specification is based on the current implementation in:

- `frontend/`
- `backend/`
- `docs/architecture/report-app-architecture.md`
- `docs/architecture/report-app-presentation-summary.md`

## 2. System Overview

The Report App is a report generation and approval platform designed around a maker-checker control model.

The system allows authenticated users to:

- log in using username and password
- view available report definitions
- execute reports from predefined configurations
- persist a report execution as a report run snapshot
- submit generated runs for approval
- approve or reject submitted runs
- inspect the audit trail of a report run
- export the latest report output or a specific report run to Excel

The system is intended to provide not only reporting capability, but also controlled report lifecycle management with traceability and approval governance.

## 3. Business Objectives

The primary business objectives are:

- provide a centralized reporting interface for predefined reports
- enforce maker-checker separation of duties
- preserve execution history for traceability
- maintain an auditable event trail for each report run
- support operational distribution of results via Excel export

## 4. In-Scope Capabilities

The current system supports the following high-level capabilities:

- JWT-based authentication
- role-aware route access in the frontend
- report catalog browsing
- report execution for maker users
- automatic creation of `ReportRun` records upon execution
- maker submission of generated runs
- checker approval or rejection of submitted runs
- audit event capture for report lifecycle transitions
- viewing audit history for a run
- exporting report output to `.xlsx`

## 5. Out-of-Scope or Not Yet Implemented

The following capabilities are either out of scope for the current version or visibly incomplete in the implementation:

- report update and delete management
- advanced report parameterization
- durable production database persistence
- workflow configuration by administrators
- notification or alerting on submission/approval
- fine-grained permission administration beyond role checks
- safe business-query abstraction replacing direct report SQL execution
- standardized API response envelope across all endpoints

## 6. User Roles

### 6.1 Maker

A Maker is responsible for generating report output and submitting it for approval.

Maker permissions include:

- log in to the application
- browse report definitions
- execute a report
- create a new `ReportRun` through execution
- view personal report runs
- submit a generated run
- review audit events for a run
- export the current run or latest report output

### 6.2 Checker

A Checker is responsible for reviewing and deciding on submitted report runs.

Checker permissions include:

- log in to the application
- access checker-oriented pages
- view submitted runs awaiting review
- inspect audit trail of submitted runs
- approve a submitted run
- reject a submitted run with comment
- review checker decision history
- export a specific run

### 6.3 General Authenticated User

Any authenticated user can:

- access protected routes after login
- view report listings through the shared reports experience
- inspect data exposed to their authorized role context

## 7. Functional Modules

### 7.1 Authentication and Session Management

#### Description

The system authenticates users through a login endpoint and stores the returned JWT token and user profile in browser `localStorage`.

#### Functional behavior

- user enters username and password
- frontend calls `/api/auth/login`
- backend validates credentials and returns:
  - JWT token
  - user profile
- frontend stores:
  - `auth_token`
  - `auth_user`
- all subsequent API requests include the token through the HTTP interceptor
- protected routes require the user to be authenticated
- role-protected routes additionally validate the user role
- logout clears locally stored session data

#### Primary files

- `frontend/src/app/components/auth/login.component.ts`
- `frontend/src/app/services/auth.service.ts`
- `frontend/src/app/services/auth.guard.ts`
- `frontend/src/app/services/auth.interceptor.ts`
- `frontend/src/app/app.routes.ts`

### 7.2 Report Catalog

#### Description

The system provides a catalog of report definitions maintained in backend storage.

#### Functional behavior

- frontend loads available reports from `/api/reports`
- user can select a report from the list
- system displays report metadata such as name and description
- current frontend also maps English report names/descriptions to localized Chinese display text

#### Primary files

- `frontend/src/app/components/report/report-viewer.component.ts`
- `frontend/src/app/services/report.service.ts`
- `backend/src/main/java/com/legacy/report/controller/ReportController.java`
- `backend/src/main/java/com/legacy/report/service/ReportService.java`

### 7.3 Report Execution

#### Description

A Maker can execute a predefined report. The execution returns result rows to the UI and also creates a persistent run record for workflow control.

#### Functional behavior

- maker selects a report
- frontend calls `/api/reports/{id}/execute`
- backend verifies current user role is `MAKER`
- backend loads the report definition
- backend executes the stored report SQL
- backend creates a `ReportRun` record with:
  - report reference
  - report name
  - maker username
  - status `Generated`
  - generation timestamp
  - optional serialized result snapshot
- backend creates an audit event with type `Generated`
- frontend displays the returned result set
- frontend loads the latest run for the selected report

#### Inputs

- report identifier

#### Outputs

- tabular report result rows
- persistent report run record
- audit event entry

#### Business rules

- only users with Maker role may execute a report
- execution is tied to an existing saved report definition
- the generated run is the starting point of the approval workflow

### 7.4 Report Submission

#### Description

A Maker can submit a previously generated run for checker review.

#### Functional behavior

- maker chooses the current generated run
- frontend calls `/api/report-runs/{id}/submit`
- backend validates:
  - current user is `MAKER`
  - run exists
  - run status is `Generated`
  - current user is the same maker who generated the run
- backend updates the run status to `Submitted`
- backend records the submission timestamp
- backend creates an audit event with type `Submitted`
- frontend refreshes the current run state

#### Business rules

- only a `Generated` run can be submitted
- a maker can submit only their own generated runs

### 7.5 Checker Review and Decision

#### Description

A Checker reviews submitted runs and decides whether to approve or reject them.

#### Functional behavior

- checker loads submitted runs from `/api/report-runs/submitted`
- checker selects a run to review
- checker loads audit trail for context
- checker submits a decision through `/api/report-runs/{id}/decision`
- decision request includes:
  - `decision`
  - `comment`
- backend validates:
  - current user is `CHECKER`
  - run exists
  - run status is `Submitted`
- backend updates the run with:
  - checker username
  - decision timestamp
  - status `Approved` or `Rejected`
- backend creates an audit event with type `Approved` or `Rejected`
- frontend refreshes checker task list and feedback message

#### Business rules

- only a `Submitted` run can be decided
- only a Checker may decide a run
- rejection requires a non-empty comment

### 7.6 Audit Trail Viewing

#### Description

The system provides end-to-end audit visibility for each report run.

#### Functional behavior

- frontend requests `/api/report-runs/{id}/audit`
- backend returns ordered audit events for the run
- frontend displays the lifecycle history in a dedicated flow view and in embedded report views

#### Audit events currently include lifecycle transitions such as:

- `Generated`
- `Submitted`
- `Approved`
- `Rejected`

#### Business value

- supports traceability
- supports review context
- supports operational audit requirements

### 7.7 Excel Export

#### Description

The system supports export of report output as Excel files.

#### Functional behavior

Two export modes are available:

- export latest report by report definition
- export specific report run by run identifier

Frontend behavior:

- requests binary data from export endpoints
- creates a browser download action
- names the file based on report or run metadata

Backend behavior:

- renders Excel content through `ReportExcelExportService`
- for run export, prefers the stored result snapshot when available

#### Endpoints

- `/api/reports/{id}/export`
- `/api/report-runs/{id}/export`

## 8. End-to-End Business Workflows

### 8.1 Login Workflow

1. user opens login page
2. user enters credentials
3. system authenticates against backend
4. system stores JWT and user info locally
5. system redirects user based on intended route or role default

### 8.2 Maker Report Execution Workflow

1. maker logs in
2. maker loads report catalog
3. maker selects a report
4. maker executes the report
5. system returns the query result to UI
6. system persists a `ReportRun` in `Generated` state
7. system records a `Generated` audit event
8. maker may review the generated output and audit context

### 8.3 Maker Submission Workflow

1. maker identifies current generated run
2. maker submits run for approval
3. system validates ownership and state
4. system changes run state to `Submitted`
5. system records a `Submitted` audit event

### 8.4 Checker Decision Workflow

1. checker logs in
2. checker loads submitted runs
3. checker selects a run
4. checker reviews audit trail
5. checker approves or rejects the run
6. if rejected, checker must provide comment
7. system updates the run status
8. system records decision audit event

### 8.5 Export Workflow

1. user requests export
2. system generates `.xlsx` binary response
3. browser downloads the file
4. file can represent latest report output or a specific historical run

## 9. Core Data Entities

### 9.1 Report

Represents a saved report definition.

Key attributes include:

- `id`
- `name`
- `sql`
- `description`

### 9.2 ReportRun

Represents one execution instance of a report and its workflow state.

Key attributes include:

- `id`
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

### 9.3 ReportAuditEvent

Represents a traceable business event tied to a report run.

Key attributes include:

- `id`
- `reportRunId`
- `reportId`
- `actorUsername`
- `actorRole`
- `eventType`
- `eventTime`
- `comment`

### 9.4 User

Represents an application user.

Key attributes include:

- `id`
- `username`
- `password` or password hash
- `role`

## 10. Functional Rules and State Model

### 10.1 Report Run States

The run lifecycle currently uses the following states:

- `Generated`
- `Submitted`
- `Approved`
- `Rejected`

### 10.2 Allowed State Transitions

- `Generated -> Submitted`
- `Submitted -> Approved`
- `Submitted -> Rejected`

### 10.3 Transition Constraints

- only Maker can create `Generated`
- only the original Maker can submit a generated run
- only Checker can approve or reject
- rejection requires a comment
- invalid state transitions must be rejected by backend validation

## 11. Frontend Functional Requirements

### 11.1 Routing

The frontend provides the following main routes:

- `/login`
- `/reports`
- `/maker`
- `/checker`
- `/runs/:id/flow`

### 11.2 Access Control

- unauthenticated users are redirected to `/login`
- route guards validate authentication status
- role guards validate access to Maker and Checker specific routes

### 11.3 User Interface Requirements

The frontend shall support:

- login form
- report selection
- report execution
- tabular display of results
- maker run list
- submit-for-approval action
- checker pending queue
- checker decision action
- audit timeline view
- export action for report and run outputs

## 12. Backend Functional Requirements

### 12.1 API Responsibilities

The backend shall provide APIs for:

- authentication
- report listing and lookup
- report execution
- report run submission
- report decisioning
- audit retrieval
- export generation

### 12.2 Security Responsibilities

The backend shall:

- validate JWT for protected endpoints
- resolve current authenticated user
- enforce role checks for Maker and Checker actions

### 12.3 Persistence Responsibilities

The backend shall persist:

- report definitions
- report runs
- audit events
- user records

## 13. External Interfaces

### 13.1 Frontend to Backend APIs

Primary backend endpoints visible in the current implementation include:

- `POST /api/auth/login`
- `GET /api/auth/profile`
- `GET /api/reports`
- `GET /api/reports/{id}`
- `POST /api/reports/{id}/execute`
- `POST /api/reports`
- `GET /api/reports/{id}/export`
- `GET /api/report-runs/my-latest?reportId={id}`
- `GET /api/report-runs/my-runs`
- `GET /api/report-runs/submitted`
- `GET /api/report-runs/checker/history`
- `POST /api/report-runs/{id}/submit`
- `POST /api/report-runs/{id}/decision`
- `GET /api/report-runs/{id}/audit`
- `GET /api/report-runs/{id}/export`

## 14. Non-Functional Considerations

### 14.1 Security

Current implementation demonstrates role-protected actions, but the following risks remain visible:

- direct SQL execution through report definitions
- permissive CORS usage
- local token storage in browser `localStorage`

### 14.2 Auditability

The system has strong audit support through explicit event persistence tied to workflow transitions.

### 14.3 Performance and Scalability

The current implementation is suitable for demo or small-scale usage. It does not yet describe:

- workload limits
- pagination for long run histories
- asynchronous execution for heavy reports

### 14.4 Reliability

The current H2 in-memory database profile indicates non-durable runtime storage and is not suitable as a production persistence strategy.

## 15. Assumptions and Constraints

- report definitions are preconfigured in backend storage
- report execution is synchronous
- result snapshots may be serialized as JSON
- frontend and backend run locally in the current setup
- current business process is centered on report lifecycle control rather than ad hoc analytics

## 16. Known Gaps and Improvement Opportunities

- replace direct SQL execution with safer service-layer query strategies
- introduce standardized API response models
- externalize frontend API base URL configuration
- separate large frontend report workflow UI into smaller components
- add update/delete report management functions if needed
- add persistent production-grade database support
- add notifications for submission and decision events
- add stronger validation and error modeling

## 17. Acceptance Criteria Summary

The system is functionally acceptable for the current scope when:

- a valid user can log in and access authorized routes
- a maker can list and execute a predefined report
- execution creates a `ReportRun` in `Generated` state
- a maker can submit only their own generated run
- a checker can see submitted runs and decide them
- rejection cannot proceed without a comment
- audit events are recorded for lifecycle transitions
- users can view audit history for a run
- users can export report output to Excel

## 18. Related Documents

- `docs/architecture/report-app-architecture.md`
- `docs/architecture/report-app-presentation-summary.md`
- `docs/architecture/report-app-diagrams.puml`
- `docs/architecture/report-app-diagram.drawio`
