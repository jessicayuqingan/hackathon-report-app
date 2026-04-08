---
description: Windsurf SDLC rule for planning, development, testing, and documentation
---

# Windsurf SDLC Rule

## 1. Planning & Contextual Awareness

- Analyze Before Acting: Before writing any code, Cascade must analyze the existing architecture and verify dependencies.
- Impact Assessment: For any change, identify which existing modules or data schemas will be affected.
- Environment Check: Since this project uses WSL2/Windows, always verify proxy settings (`HTTP_PROXY`) before attempting to install packages or pull remote data.

## 2. Development Standards

- Modular Code: Write small, reusable functions. Avoid "God objects" or files longer than 300 lines.
- Type Hinting: All Python code must include type hints (for example, `def process_data(df: pd.DataFrame) -> dict:`).
- Error Handling: Use explicit `try-except` blocks. Never use bare `except: pass`.
- Naming Conventions:
  - Variables/Functions: `snake_case`
  - Classes: `PascalCase`
  - Constants: `UPPER_SNAKE_CASE`

## 3. Data Analysis & Integration

- Schema Integrity: When modifying data pipelines (like for CNY/USD or Gold trackers), ensure the data schema is documented in a `SCHEMA.md` or as a comment at the top of the file.
- Idempotency: Data processing scripts should be run-safe. Running them twice should not result in duplicate data.

## 4. Testing & Quality Assurance

- Test-Driven Approach: For new features, Cascade should suggest a simple test case (using `pytest` or `unittest`) before finalizing the implementation.
- Validation: All data analysis outputs must include a sanity check (for example, checking for nulls or impossible values like negative gold prices).

## 5. Deployment & Documentation

- Docstrings: Use Google-style docstrings for all public functions.
- Change Logs: Automatically update the `README.md` or a `CHANGELOG.md` when a significant milestone is reached.
- Release Notes: Whenever files under `backend/` are changed, update `backend/ReleaseNote.md`. Whenever files under `frontend/` are changed, update `frontend/ReleaseNote.md` in the same task.
- Documentation Impact: When a code change affects architecture, business workflow, APIs, data flow, user behavior, or system scope, update the impacted documentation and diagrams in the same task. This includes `docs/architecture/report-app-architecture.md`, `docs/architecture/report-app-fsd.md`, `docs/architecture/report-app-presentation-summary.md`, diagram sources such as `docs/architecture/report-app-diagram.drawio` and `docs/architecture/report-app-diagrams.puml`, and any slide diagram or related presentation artifact that reflects the changed design.
- Git Hygiene: When suggesting a commit, use the Conventional Commits format (for example, `feat:`, `fix:`, `docs:`, `refactor:`).

## How to activate this skill

1. Create the directory: `mkdir -p .windsurf/rules`
2. Create the file: Save this content as `.windsurf/rules/sdlc-workflow.md`.
3. Trigger it: Tell Cascade, "I'm starting a new feature for the OpenClaw agent. Follow our SDLC rule for the implementation."
