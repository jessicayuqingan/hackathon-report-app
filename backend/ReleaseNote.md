# Backend Release Notes

## Purpose

Track backend changes for the `hackathon-report-app` project.

## Update Rule

Whenever backend files are changed, add a new dated entry to this file.

## Entries

### 2026-04-08

- Implemented safer report execution by validating saved report SQL, binding named parameters, and disabling arbitrary raw SQL execution through `/api/reports/run`.
- Added report-level authorization scope checks so users can only access runs and report definitions within their permitted report set.
- Extended report run workflow to persist execution parameters, allow rejected runs to be resubmitted, and expose notification summary data for queue visibility.
- Added filtered and paginated report run APIs for maker history, checker submitted queues, and checker decision history.
- Created `backend/ReleaseNote.md` to track backend changes.
