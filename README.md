# SauceDemo UI Test Automation

A Java-based UI automation project for [SauceDemo](https://www.saucedemo.com/). It demonstrates maintainable Selenium WebDriver tests for essential e-commerce journeys, with JUnit 5, Page Objects, a focused Gherkin layer, and GitHub Actions CI.

## Technology stack

- Java 26
- Selenium WebDriver 4.51
- JUnit Jupiter 5
- Cucumber 7 with the JUnit Platform engine
- Maven Wrapper
- Chrome in headed local runs and headless CI runs

## Coverage

The suite covers:

- Successful, invalid, and locked-out login; logout and access protection
- Product catalog content, sorting, and product details
- Cart contents, item counts, adding and removing products
- Checkout field validation, order overview, totals, and confirmation
- Selected SauceDemo special-user scenarios, kept separate from the standard-user smoke suite
- Gherkin scenarios for login, cart, checkout, and logout

The special-user tests are a starting point for investigating SauceDemo's intentionally unusual accounts. They do not claim to validate every account-specific anomaly.

## Project structure

```text
src/test/
├── java/
│   ├── config/       # Runtime configuration
│   ├── data/         # Test users and checkout data
│   ├── pages/        # Selenium Page Objects
│   ├── reporting/    # Scenario registry and quality-report generation
│   ├── runner/       # Cucumber JUnit Platform suite
│   ├── steps/        # Gherkin step definitions
│   ├── support/      # WebDriver setup and failure screenshots
│   └── tests/        # JUnit UI tests
└── resources/
    ├── features/     # Gherkin feature files
    └── scenario-registry.csv
```

Page Objects contain page-level interactions; JUnit tests and Cucumber step definitions use them to exercise user journeys. The scenario registry connects user-facing behaviors to JUnit tests and named Gherkin scenarios for the quality report. `FULL` and `PARTIAL` coverage labels are manually assessed; the report does not prove assertion completeness. CI stores run metrics on the `qa-history` branch, and the report shows up to ten recent runs; local runs are not persisted automatically.

## Design principles and test standards

The project uses the Page Object Model (POM) to keep page-specific locators and interactions in page classes, separate from test intent and assertions. JUnit tests and Gherkin step definitions should reuse those page objects rather than duplicating browser interactions.

Tests should follow these principles:

- **Readable intent:** use descriptive names and keep each test focused on one main behavior. A reviewer should be able to understand the user journey and expected result without tracing a long chain of helpers.
- **Arrange, Act, Assert:** establish the preconditions, perform the user action, and verify the business outcome with explicit assertions.
- **Independence and repeatability:** each test manages its own browser and state; tests must not depend on execution order or leftover data.
- **Reliable interactions:** prefer stable test attributes and page-level locators; wait for an expected state rather than using fixed sleeps.
- **Maintainability:** keep test data and configuration centralized, reuse helpers when they reduce duplication, and avoid abstractions that hide the flow.
- **Appropriate coverage:** prioritize important user journeys and verify meaningful results, including negative paths where they matter. Keep smoke tests focused and run broader regression scenarios separately.

## Prerequisites

- JDK 26
- Google Chrome or Chromium for local UI runs
- Internet access to SauceDemo

The Maven Wrapper downloads the configured Maven version. Selenium Manager resolves the matching browser driver.

## Run tests locally

On Windows, use `.\mvnw.cmd`; on macOS or Linux, use `./mvnw`.

Run the complete regression suite from a clean state. This runs all JUnit and Cucumber tests and matches the test scope of the Regression tests and quality report workflow:

```powershell
.\mvnw.cmd clean -Dheadless=true test
```

Run only the faster smoke subset:

```powershell
.\mvnw.cmd -Dheadless=true -Dgroups=smoke test
```

The regression workflow runs the complete suite, not only tests tagged `regression`. To run only JUnit methods explicitly tagged `regression` locally:

```powershell
.\mvnw.cmd -Dheadless=true -Dgroups=regression test
```

Tag-filtered group commands select JUnit tests and skip the Cucumber suite. Run Gherkin separately with the command below; an unfiltered full-suite run includes both JUnit and Cucumber tests.

Run the Cucumber feature suite:

```powershell
.\mvnw.cmd -Dtest=CucumberTest test
```

Run selected test groups:

```powershell
.\mvnw.cmd -Dheadless=true -Dgroups="product-details,cart,checkout-step-one,checkout-step-two,checkout-complete" test
.\mvnw.cmd -Dheadless=true -Dgroups=special-user test
```

Omit `-Dheadless=true` to run Chrome with a visible window. Use `clean` before a full run when generating the quality report so old Surefire results are not mixed with the current run.

## Test and quality reports

Maven writes JUnit/Surefire results to `target/surefire-reports/`. Screenshots captured after failures are written to `target/screenshots/`.

The HTML quality report is generated separately after the tests:

```powershell
.\mvnw.cmd clean test
java -cp target/test-classes reporting.ReportGenerator
```

Open `target/quality-report/index.html` in a browser. The generator also writes `target/quality-report/summary.md`. See [src/REPORTING.md](src/REPORTING.md) for report contents and scenario-registry guidance.

Tests tagged `characterization` record observed behavior that is not yet an approved product requirement and are not included in the regression-tagged subset.

## Continuous integration

GitHub Actions runs Java 26 and headless Chrome:

- **CI - Full test suite** runs `mvn --batch-mode --no-transfer-progress clean -Dheadless=true test` for pull requests targeting `main` or `master`, pushes to those branches, every Monday at 05:00 UTC, and on manual request. A failed test command fails the CI check after reports and diagnostics are collected.
- The workflow summary classifies failures as `TEST_FAILURE`, `CONFIGURATION_FAILURE`, or `INFRASTRUCTURE_FAILURE` and points to the Maven log and generated reports. Surefire XML and Maven logs, Cucumber HTML/JSON, failure screenshots, and the custom quality report are uploaded as separate artifacts, even when tests fail. The quality report artifact is retained for 30 days; test diagnostics are retained for 14 days.
- Run metrics are saved to the `qa-history` branch for scheduled runs and pushes to `main` or `master`. Pull request runs do not write to that branch.
- **Smoke tests** remain available as a manually started workflow; they are not run in parallel with the full CI suite.

To make GitHub block merging when CI fails, configure branch protection or a repository ruleset for `main` and `master` and require the `Full test suite` status check. Download the `quality-report` artifact and open `index.html` in a browser to inspect the custom report.
