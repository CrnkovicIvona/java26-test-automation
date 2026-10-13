## Scenario registry and quality report

`src/test/resources/scenario-registry.csv` lists the user-facing behaviors of SauceDemo, including the ones that are not automated yet. Each automated scenario links to the JUnit tests and/or named Cucumber scenarios that cover it. After a test run, `reporting.ReportGenerator` combines the registry with the Surefire results and writes a report.

### Create the report

```powershell
.\mvnw.cmd clean test
java -cp target/test-classes reporting.ReportGenerator
```

Open `target/quality-report/index.html`. A Markdown summary for the GitHub Actions job summary is written to `target/quality-report/summary.md`. Use `clean test`: Surefire keeps old result files, and the report warns when the folder mixes more than one run.

The Cucumber runner also writes HTML and JSON reports to `target/cucumber-report.html` and `target/cucumber-report.json`.

Optional arguments: Surefire folder, registry file, output folder, history folder (in that order). The history folder contains one `*.csv` metrics record per run; omit it when no history is available.

### What the report shows

- tests passed, failed and skipped, the pass rate of this run (skipped tests excluded) and the suite duration
- critical (P1) journeys: how many are automated and passing
- every scenario with its priority, automation level (full, partial, none) and status in this run
- coverage gaps, most important first; high-risk gaps are P1 and P2 scenarios without full automation
- review candidates: automated scenarios not confirmed against the application for more than 90 days
- tests that no scenario links to

A pass rate describes one run only. It is not a measure of overall quality, and flaky tests cannot be found from one run. The regression workflow stores a metrics record for each run on the `qa-history` branch; the report displays the latest ten records. Local runs are displayed but are not persisted automatically.

Automation levels are manually assessed registry labels; the report does not inspect assertions to prove completeness. Scenario statuses describe the current run; the history section compares run-level metrics only.

### Maintain the registry

- Add a line when you find or automate a behavior. IDs look like `LOGIN-013` and never change.
- `automation` is `FULL` only when the linked tests check the whole expected result; otherwise `PARTIAL`. Use `NONE` and leave `tests` empty for a gap.
- Link JUnit tests as `ClassName#method`; link one invocation of a parameterized test with `ClassName#method[2]`.
- Link Cucumber scenarios with `CucumberTest#Scenario name`. The name must match a `Scenario:` in the feature file.
- Set `last_verified` only after confirming the expectation against the live application.
- `ScenarioRegistryTest` fails when a link points at a test that was renamed or deleted, or when a line is malformed. Run it with `.\mvnw.cmd -Dgroups=reporting test`.
- Use the `characterization` tag for observed behavior that is not yet an approved product requirement; do not present it as a regression expectation.

The CI workflow reads previous records from the `qa-history` branch. A separate job with write permission records each scheduled run and each push to `main` or `master`; pull request runs do not write to the branch.

### GitHub Actions

The `CI - Full test suite` workflow runs a clean, headless JUnit and Cucumber suite on pull requests targeting `main` or `master`, pushes to those branches, and on its scheduled/manual triggers. It publishes the quality report, Surefire XML, the Maven log, Cucumber HTML/JSON, and failure screenshots as artifacts even when the test command fails. Its job summary classifies failures as test, configuration, or infrastructure failures.

For GitHub to block merging after a failed run, require the `Full test suite` check in branch protection or a repository ruleset.
