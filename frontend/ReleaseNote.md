# Frontend Release Notes

## Purpose

Track frontend changes for the `hackathon-report-app` project.

## Update Rule

Whenever frontend files are changed, add a new dated entry to this file.

## Entries

### 2026-04-08

- Created `frontend/ReleaseNote.md` to track frontend changes.
- Added report parameter JSON input support to the report viewer and passed execution parameters to the backend execution API.
- Updated maker and checker work queues to consume filtered and paginated APIs and display stored execution parameters in the UI.
- Added notification summary widgets for pending approvals, rejected runs, approved runs, and recent workflow notifications.
- Added frontend support for rejected-run resubmission and improved queue refresh behavior after submit and decision actions.
