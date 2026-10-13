package reporting;

import reporting.Scenario.Automation;


final class MarkdownSummary {

    
    private MarkdownSummary() {}

    
    static String oneLine(QualityReport report) {
        return "Tests: %d passed, %d failed, %d skipped | pass rate %s | P1 automated %d/%d | high-risk gaps %d".formatted(
                report.tests(TestResult.Status.PASSED), report.tests(TestResult.Status.FAILED),
                report.tests(TestResult.Status.SKIPPED), QualityReport.percent(report.passRate()),
                report.criticalFullyAutomated(), report.withPriority(Scenario.Priority.P1).size(),
                report.highRiskGaps());
    }

    
    static String render(QualityReport report, java.util.List<RunHistory.Entry> history) {
        var md = new StringBuilder();
        md.append("## Test quality report\n\n");
        if (report.run().mixedRuns()) {
            md.append("> **Warning:** the result files come from ").append(report.run().started().size())
                    .append(" different Maven runs, so these numbers mix them.\n\n");
        }
        md.append("| Indicator | Value |\n|---|---|\n")
                .append("| Tests passed | ").append(report.tests(TestResult.Status.PASSED)).append(" of ").append(report.totalTests()).append(" |\n")
                .append("| Failed / skipped | ").append(report.tests(TestResult.Status.FAILED)).append(" / ")
                .append(report.tests(TestResult.Status.SKIPPED)).append(" |\n")
                .append("| Pass rate (this run, skipped excluded) | ").append(QualityReport.percent(report.passRate())).append(" |\n")
                .append("| Critical journeys (P1) automated | ").append(report.criticalFullyAutomated()).append(" of ")
                .append(report.withPriority(Scenario.Priority.P1).size()).append(", ").append(report.criticalPassing()).append(" passing |\n")
                .append("| Scenarios fully / partly / not automated | ").append(report.scenarios(Automation.FULL)).append(" / ")
                .append(report.scenarios(Automation.PARTIAL)).append(" / ").append(report.scenarios(Automation.NONE)).append(" |\n")
                .append("| High-risk gaps (P1 and P2) | ").append(report.highRiskGaps()).append(" |\n")
                .append("| Suite duration (sum of test times) | ").append(QualityReport.duration(report.durationSeconds())).append(" |\n");

        md.append("\n### Recent test runs\n\n");
        if (history.size() == 1) {
            md.append("This is the first recorded run.\n");
        } else {
            md.append("| Recorded (UTC) | Run ID | Pass rate | Passed | Failed | Skipped | P1 passing | High-risk gaps |\n")
                    .append("|---|---|---:|---:|---:|---:|---:|---:|\n");
            for (var entry : history) {
                md.append("| ").append(entry.recordedAt()).append(" | `").append(entry.runId())
                        .append("` | ").append(entry.passRate()).append(" | ").append(entry.passed())
                        .append(" | ").append(entry.failed()).append(" | ").append(entry.skipped())
                        .append(" | ").append(entry.criticalPassing()).append(" | ")
                        .append(entry.highRiskGaps()).append(" |\n");
            }
        }

        var failures = report.failures();
        if (!failures.isEmpty()) {
            md.append("\n### Failed tests\n\n");
            for (var failure : failures) {
                md.append("- `").append(failure.className()).append('#').append(failure.displayName()).append("`: ")
                        .append(cell(failure.message())).append('\n');
            }
        }

        var gaps = report.gaps().stream().filter(e -> e.scenario().priority() != Scenario.Priority.P3).toList();
        if (!gaps.isEmpty()) {
            md.append("\n### High-risk gaps\n\n");
            for (var gap : gaps) {
                md.append("- ").append(gap.scenario().id()).append(" (").append(gap.scenario().priority()).append(", ")
                        .append(gap.scenario().automation().name().toLowerCase()).append("): ")
                        .append(gap.scenario().title()).append('\n');
            }
        }
        md.append("\nAutomation levels are manually assessed registry labels; the report does not prove assertion completeness. ")
                .append("A pass rate describes this single run only and is not a measure of overall quality. ")
                .append("CI history is stored on the `qa-history` branch; local runs are not persisted automatically.\n");
        return md.toString();
    }

    
    private static String cell(String text) {
        return text.replace("|", "\\|").replace("\n", " ");
    }
}
