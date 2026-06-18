---
name: run-tests
description: Run the full integration test suite with coverage report for the transaction-manager project. Use when asked to run tests, check coverage, verify a fix, or validate TDD compliance.
triggers:
  - /run-tests
  - run tests
  - check coverage
  - run integration tests
---

## Skill: run-tests

Run the full test suite and generate a JaCoCo coverage report.

### Steps

1. Run `./gradlew clean test jacocoTestReport` from the project root.
2. Check that all tests pass. If any fail, report the exact test names and failure messages.
3. Parse `build/reports/jacoco/test/html/index.html` or `build/reports/jacoco/test/jacocoTestReport.xml` for the overall line/branch coverage percentage.
4. Report:
   - Total tests run / passed / failed / skipped
   - Line coverage %
   - Branch coverage %
   - Path to the HTML report: `build/reports/jacoco/test/html/index.html`
5. If coverage drops below 80%, flag it as a violation and list the classes with the lowest coverage.
6. If tests fail, do NOT proceed — show the failure and ask the user what to fix.

### Commands

```bash
cd /Users/lean.intel/workspace/transaction-manager
./gradlew clean test jacocoTestReport
```

### Success criteria
- Exit code 0
- All tests green
- Line coverage ≥ 80%
