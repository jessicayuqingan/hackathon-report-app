# Report App User Manual

## 1. Purpose

This user manual explains how to use the Report App from an end-user perspective.

It covers:

- how to access the system
- how to log in
- how Maker users execute and submit reports
- how Checker users review and decide submitted runs
- how to view audit history
- how to export report results
- sample business cases
- common issues and resolutions

## 1.1 How to Use This Manual

This manual is designed in a training-friendly format.

For each major task, you will typically see:

- objective
- who should perform the task
- step-by-step instructions
- expected result
- screenshot placeholder for training material preparation

Recommended usage:

- trainers can use this document as a walkthrough guide during demos
- end users can use it as a step-by-step operating reference
- documentation owners can replace screenshot placeholders with real application screenshots

## 2. System Overview

The Report App is a report generation and approval platform based on a maker-checker workflow.

Main capabilities include:

- secure login with username and password
- viewing available predefined reports
- executing reports
- saving report runs for traceability
- submitting report runs for approval
- approving or rejecting submitted runs
- viewing the audit trail of a report run
- exporting report output to Excel

## 3. Access Information

### 3.1 Application URLs

- Frontend: `http://localhost:4200`
- Backend API: `http://localhost:8080/api`

### 3.2 Main Pages

- Login page: `/login`
- Reports page: `/reports`
- Maker page: `/maker`
- Checker page: `/checker`
- Report run flow page: `/runs/:id/flow`

## 4. User Roles

### 4.1 Maker

A Maker can:

- log in to the system
- view allowed reports
- execute a report
- view their latest run and run history
- submit a generated run for approval
- view audit information for their runs
- export report output and run output

### 4.2 Checker

A Checker can:

- log in to the system
- view submitted runs pending approval
- inspect audit history for a selected run
- approve a run
- reject a run with a comment
- review checker history
- export run output

### 4.3 Admin

The default `admin` user has both `MAKER` and `CHECKER` roles and can access both workflow perspectives.

## 5. Default Login Accounts

The system initializes the following default users:

- `admin / 123456`
- `maker1 / 123456`
- `checker1 / 123456`

Recommended usage:

- use `maker1` for Maker-only testing
- use `checker1` for Checker-only testing
- use `admin` when you want to test both views with one account

## 6. Login Instructions

### 6.1 How to Log In

Objective:

- access the system successfully with a valid user account

1. Open `http://localhost:4200`
2. If you are not already on the login page, go to `/login`
3. Enter your username
4. Enter your password
5. Click the login button

Expected result:

- the system authenticates the user
- the user is redirected to the appropriate page based on role

Screenshot placeholder:

- `[Insert Screenshot - Login page with username/password fields]`
- `[Insert Screenshot - Successful login landing page]`

### 6.2 What Happens After Login

The system redirects you based on role:

- users with `CHECKER` role are redirected to `/checker`
- users with `MAKER` role are redirected to `/maker`
- authenticated users can also access `/reports`

### 6.3 Logout

To log out:

1. Click the logout action in the report viewer
2. The system clears local session information
3. You are redirected back to the login page

## 7. Maker User Guide

### 7.1 Open the Maker Page

Objective:

- access the Maker workspace for report execution and submission

1. Log in with a Maker-capable account such as `maker1`
2. After login, the system should open `/maker`

Expected result:

- the Maker page loads successfully
- report execution functions are visible

Screenshot placeholder:

- `[Insert Screenshot - Maker landing page]`

### 7.2 View Available Reports

Objective:

- review the reports currently available to the logged-in Maker

1. On the Maker page, wait for the report list to load
2. Review the available report names
3. Select the report you want to run

Notes:

- only reports within your access scope are expected to appear
- the UI may show localized Chinese names and descriptions for reports

Screenshot placeholder:

- `[Insert Screenshot - Report list and selected report]`

### 7.3 Execute a Report

Objective:

- run a predefined report and review the output

1. Select a report from the list
2. Click the action that executes the report
3. Wait for the result set to load
4. Review the table of returned rows

System behavior:

- the report result is displayed in the UI
- a `ReportRun` record is created automatically
- the run starts in `Generated` status
- an audit event is recorded

Screenshot placeholder:

- `[Insert Screenshot - Report execution button]`
- `[Insert Screenshot - Report result table after execution]`

### 7.4 View Current Run Status

Objective:

- review the state of the most recent report run

After execution, the current run information becomes available.

You can review:

- current run status
- audit trail for the run
- submission eligibility
- export options for the run

Screenshot placeholder:

- `[Insert Screenshot - Current run status panel]`

### 7.5 Submit a Run for Approval

Objective:

- send a generated run to Checker for review

1. Execute a report successfully
2. Confirm the current run status is `Generated`
3. Click the submit-for-approval action
4. Wait for the confirmation message

Expected result:

- status changes to `Submitted`
- a submission audit event is recorded

Important rule:

- you can only submit your own generated run

Screenshot placeholder:

- `[Insert Screenshot - Submit for approval action]`
- `[Insert Screenshot - Submission success message or updated Submitted status]`

### 7.6 View Maker Run History

Objective:

- review previously created report runs and their statuses

A Maker can also review previously created runs.

Typical details include:

- run ID
- report name
- current status
- generation time
- approval outcome when available

Screenshot placeholder:

- `[Insert Screenshot - Maker run history list]`

### 7.7 Export Report Output

Objective:

- download report output to Excel for offline review or sharing

A Maker can export in two ways:

#### Export latest report output

1. Select a report
2. Use the export action for the selected report
3. Save the downloaded `.xlsx` file

#### Export current run output

1. Execute a report
2. Use the export action for the current run
3. Save the downloaded `.xlsx` file

Screenshot placeholder:

- `[Insert Screenshot - Report export button]`
- `[Insert Screenshot - Browser download result]`

## 8. Checker User Guide

### 8.1 Open the Checker Page

Objective:

- access the Checker workspace for pending approvals and decision history

1. Log in with a Checker-capable account such as `checker1`
2. After login, the system should open `/checker`

Expected result:

- the Checker page loads successfully
- pending approval functions are visible

Screenshot placeholder:

- `[Insert Screenshot - Checker landing page]`

### 8.2 View Submitted Runs

Objective:

- identify report runs waiting for Checker action

1. Open the Checker page
2. Wait for the submitted run list to load
3. Select a run from the pending list

The submitted list is intended to show runs waiting for approval.

Screenshot placeholder:

- `[Insert Screenshot - Checker pending submitted runs list]`

### 8.3 Review Audit History Before Decision

Objective:

- inspect lifecycle details before approving or rejecting a run

1. Select a submitted run
2. Review the audit trail shown in the Checker view
3. Confirm the run history and context before making a decision

Screenshot placeholder:

- `[Insert Screenshot - Checker audit trail panel]`

### 8.4 Approve a Run

Objective:

- complete a positive approval decision for a submitted run

1. Select a submitted run
2. Choose decision `APPROVED`
3. Submit the decision
4. Wait for confirmation

Expected result:

- run status changes to `Approved`
- the decision is recorded in audit history

Screenshot placeholder:

- `[Insert Screenshot - Approval decision controls]`
- `[Insert Screenshot - Approval success state]`

### 8.5 Reject a Run

Objective:

- reject a submitted run and record the reason

1. Select a submitted run
2. Choose decision `REJECTED`
3. Enter a comment explaining the rejection reason
4. Submit the decision

Expected result:

- run status changes to `Rejected`
- rejection comment is stored with the audit event

Important rule:

- rejection requires a comment

Screenshot placeholder:

- `[Insert Screenshot - Rejection form with comment]`
- `[Insert Screenshot - Rejection recorded in audit trail]`

### 8.6 View Checker History

Objective:

- review runs already decided by the Checker

Checker users can also review historical decisions.

This is useful for:

- verifying previous approvals
- verifying previous rejections
- exporting historical runs when needed

Screenshot placeholder:

- `[Insert Screenshot - Checker history list]`

### 8.7 Export a Run

Objective:

- download the output of a reviewed or historical run

1. Open an eligible run from current or history views
2. Use the export action
3. Save the generated `.xlsx` file

Screenshot placeholder:

- `[Insert Screenshot - Checker export action]`

## 9. Audit Trail Guide

The audit trail provides the lifecycle history of each report run.

Typical event types include:

- `Generated`
- `Submitted`
- `Approved`
- `Rejected`
- `Resubmitted` when a previously rejected run is submitted again

### 9.1 How to View the Full Approval Flow

1. Find the desired run
2. Open the action to view the complete approval flow
3. The system navigates to `/runs/{id}/flow`
4. Review the timeline events displayed for that run

Screenshot placeholder:

- `[Insert Screenshot - Full approval flow page]`

## 10. Sample Cases

### Case 1: Maker executes and submits a report successfully

#### Objective

Demonstrate a normal Maker workflow from execution to submission.

#### User

`maker1 / 123456`

#### Steps

1. Log in as `maker1`
2. Open the Maker page
3. Select a report from the available report list
4. Execute the report
5. Review the result table
6. Confirm a current run is created with status `Generated`
7. Click submit for approval

#### Expected outcome

- report results are displayed
- run status changes from `Generated` to `Submitted`
- audit trail shows `Generated` and `Submitted`

Training note:

- use this case as the first end-to-end demo for new Maker users

### Case 2: Checker approves a submitted run

#### Objective

Demonstrate a successful approval flow.

#### User

`checker1 / 123456`

#### Steps

1. Ensure there is already a submitted run from Case 1
2. Log in as `checker1`
3. Open the Checker page
4. Select the submitted run
5. Review the audit trail
6. Choose `APPROVED`
7. Submit the decision

#### Expected outcome

- run status becomes `Approved`
- audit trail shows `Approved`
- the run is removed from the pending list and becomes part of decision history

Training note:

- use this case to explain approval accountability and state transitions

### Case 3: Checker rejects a submitted run with comment

#### Objective

Demonstrate a rejection flow with mandatory comment.

#### User

`checker1 / 123456`

#### Steps

1. Ensure there is a submitted run available
2. Log in as `checker1`
3. Open the Checker page
4. Select the submitted run
5. Choose `REJECTED`
6. Enter a rejection comment such as `Data needs verification`
7. Submit the decision

#### Expected outcome

- run status becomes `Rejected`
- rejection comment is saved in audit history
- the run is no longer pending approval

Training note:

- use this case to emphasize that rejection comments are mandatory

### Case 4: Maker resubmits a rejected run

#### Objective

Demonstrate the rework-and-resubmission scenario.

#### User

`maker1 / 123456`

#### Steps

1. Start from a run that was rejected by a Checker
2. Log in as the same Maker who created the run
3. Open the Maker page
4. Find the rejected run or current run context
5. Submit it again for approval if the UI presents the action

#### Expected outcome

- run status becomes `Submitted`
- audit trail records `Resubmitted`

Note:

- backend logic allows submission from `Generated` or `Rejected` status for the original Maker

Training note:

- use this case to explain the feedback-and-resubmission loop

### Case 5: View full run audit trail

#### Objective

Demonstrate traceability for one report run.

#### User

Any authenticated user with access to the run

#### Steps

1. Locate a run from Maker or Checker views
2. Open the complete approval flow page
3. Review each lifecycle event in order

#### Expected outcome

- event list is displayed in chronological order
- actor, event type, time, and comment are visible when available

Training note:

- use this case to explain traceability and audit readiness

### Case 6: Export report output to Excel

#### Objective

Demonstrate file export.

#### User

Maker or Checker depending on the screen context

#### Steps

1. Open a report or a specific run
2. Click the export action
3. Wait for browser download
4. Save the `.xlsx` file locally

#### Expected outcome

- an Excel file is downloaded successfully
- filename is based on report or run metadata

Training note:

- use this case to demonstrate how users take report outputs offline

## 11. Business Rules Summary

- users must log in before accessing protected pages
- route access is role-based for Maker and Checker pages
- report execution creates a run automatically
- only Maker users can execute and submit runs
- only the original Maker can submit their generated or rejected run
- only Checker users can approve or reject submitted runs
- rejection requires a comment
- audit events are recorded for workflow transitions

## 12. Common Issues and Troubleshooting

### 12.1 Login fails

Possible causes:

- incorrect username or password
- backend is not running
- frontend cannot reach backend

What to check:

- confirm backend is running on `http://localhost:8080`
- confirm frontend is running on `http://localhost:4200`
- retry with `admin / 123456`, `maker1 / 123456`, or `checker1 / 123456`

### 12.2 Report list does not load

Possible causes:

- session expired or token missing
- backend unavailable
- user has limited access scope

What to check:

- log out and log in again
- refresh the page
- confirm backend is healthy

### 12.3 Submit for approval does not work

Possible causes:

- the run is not in `Generated` or `Rejected` status
- the current user is not the original Maker

What to check:

- confirm you are logged in as the same Maker who created the run
- confirm the run status before attempting submission

### 12.4 Rejection cannot be submitted

Possible cause:

- no rejection comment was entered

What to check:

- enter a non-empty comment and try again

### 12.5 Export fails

Possible causes:

- backend export service is unavailable
- selected run or report cannot be resolved
- browser download blocked

What to check:

- retry the export
- confirm the run or report still exists
- verify browser download permissions

## 13. Quick Reference

### Login accounts

- `admin / 123456`
- `maker1 / 123456`
- `checker1 / 123456`

### Key statuses

- `Generated`
- `Submitted`
- `Approved`
- `Rejected`

### Key pages

- `/login`
- `/reports`
- `/maker`
- `/checker`
- `/runs/:id/flow`

## 14. Related Documents

- `Agent.md`
- `docs/architecture/report-app-architecture.md`
- `docs/architecture/report-app-presentation-summary.md`
- `docs/architecture/report-app-fsd.md`
