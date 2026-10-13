package reporting;

import reporting.EvaluatedScenario.Status;
import reporting.Scenario.Automation;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;


final class HtmlReport {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ROOT);

    private static final String CSS = """
            :root{--bg:#f5f6f8;--surface:#fff;--text:#1c2331;--muted:#586072;--line:#d8dce4;
              --ok:#17703f;--ok-bg:#e3f4ea;--bad:#b3261e;--bad-bg:#fbe7e5;
              --warn:#7a5a00;--warn-bg:#fdf3d4;--info:#2a4fb5;--info-bg:#e6ecfb}
            @media (prefers-color-scheme:dark){:root{--bg:#12151b;--surface:#1a1f29;--text:#e7eaf0;--muted:#a2abbb;
              --line:#323a49;--ok:#6fd49b;--ok-bg:#16301f;--bad:#ff8f87;--bad-bg:#3a1b19;
              --warn:#f0c860;--warn-bg:#3a2f10;--info:#8fb0ff;--info-bg:#1b2a4d}}
            *{box-sizing:border-box}
            body{margin:0;background:var(--bg);color:var(--text);
              font:16px/1.5 system-ui,-apple-system,"Segoe UI",Roboto,sans-serif}
            main{max-width:1100px;margin:0 auto;padding:32px 20px 64px}
            h1{font-size:1.75rem;margin:0 0 4px}
            h2{font-size:1.2rem;margin:40px 0 4px}
            p{margin:0 0 12px}
            .sub,.hint{color:var(--muted)}
            .hint{margin:0 0 12px;max-width:75ch}
            .verdict{display:inline-block;padding:6px 12px;border:1px solid;border-radius:4px;font-weight:600}
            .verdict.ok{color:var(--ok);background:var(--ok-bg)}
            .verdict.bad{color:var(--bad);background:var(--bad-bg)}
            .note{border-left:4px solid var(--warn);background:var(--warn-bg);padding:10px 14px;margin:16px 0}
            .kpis{display:grid;grid-template-columns:repeat(auto-fit,minmax(190px,1fr));gap:1px;margin:24px 0 0;
              padding:0;background:var(--line);border:1px solid var(--line)}
            .kpis div{background:var(--surface);padding:14px 16px}
            .kpis dt{color:var(--muted);font-size:.9rem}
            .kpis dd{margin:2px 0 0;font-size:1.6rem;font-weight:650}
            .kpis small{display:block;color:var(--muted);font-size:.85rem;font-weight:400;line-height:1.35}
            .table-wrap{overflow-x:auto;background:var(--surface);border:1px solid var(--line)}
            table{border-collapse:collapse;width:100%;min-width:680px}
            th,td{padding:8px 12px;text-align:left;vertical-align:top;border-bottom:1px solid var(--line)}
            tr:last-child>td{border-bottom:0}
            thead th,tr.area th{background:var(--bg);font-weight:600}
            .num{text-align:right;font-variant-numeric:tabular-nums}
            code{font:.85rem ui-monospace,SFMono-Regular,Consolas,monospace;overflow-wrap:anywhere}
            .badge{display:inline-block;padding:1px 8px;border:1px solid;border-radius:3px;font-size:.85rem;
              font-weight:600;white-space:nowrap}
            .s-passed{color:var(--ok);background:var(--ok-bg)}
            .s-failed{color:var(--bad);background:var(--bad-bg)}
            .s-skipped,.s-not_run{color:var(--warn);background:var(--warn-bg)}
            .s-not_automated{color:var(--info);background:var(--info-bg)}
            .detail{display:block;color:var(--muted);font-size:.85rem}
            :focus-visible{outline:3px solid var(--info);outline-offset:2px}
            """;

    
    private HtmlReport() {}

    
    static String render(QualityReport report, List<RunHistory.Entry> history) {
        var html = new StringBuilder(40_000);
        html.append("<!doctype html>\n<html lang=\"en\">\n<head>\n<meta charset=\"utf-8\">\n")
                .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n")
                .append("<title>Test quality report</title>\n<style>").append(CSS).append("</style>\n</head>\n<body>\n<main>\n");
        header(html, report);
        indicators(html, report);
        runHistory(html, history);
        failures(html, report);
        scenarios(html, report);
        gaps(html, report);
        reviewCandidates(html, report);
        unlinked(html, report);
        classes(html, report);
        about(html, report);
        html.append("</main>\n</body>\n</html>\n");
        return html.toString();
    }

    private static void runHistory(StringBuilder html, List<RunHistory.Entry> history) {
        html.append("<h2>Recent test runs</h2>\n");
        if (history.size() == 1) {
            html.append("<p class=\"hint\">This is the first recorded run.</p>\n");
        } else {
            html.append("<p class=\"hint\">Latest 10 recorded runs. Compare repeated runs to spot changes; ")
                    .append("a pass rate alone does not establish overall quality.</p>\n");
        }
        html.append("<div class=\"table-wrap\"><table>\n")
                .append("<thead><tr><th>Recorded (UTC)</th><th>Run ID</th><th>Pass rate</th>")
                .append("<th>Passed</th><th>Failed</th><th>Skipped</th><th>P1 passing</th>")
                .append("<th>High-risk gaps</th><th>Duration</th></tr></thead>\n<tbody>\n");
        for (var entry : history) {
            html.append("<tr><td>").append(esc(entry.recordedAt().toString()))
                    .append("</td><td><code>").append(esc(entry.runId()))
                    .append("</code></td><td>").append(esc(entry.passRate()))
                    .append("</td><td>").append(entry.passed())
                    .append("</td><td>").append(entry.failed())
                    .append("</td><td>").append(entry.skipped())
                    .append("</td><td>").append(entry.criticalPassing())
                    .append("</td><td>").append(entry.highRiskGaps())
                    .append("</td><td>").append(QualityReport.duration(entry.durationSeconds()))
                    .append("</td></tr>\n");
        }
        html.append("</tbody></table></div>\n");
    }

    private static void header(StringBuilder html, QualityReport report) {
        var failed = report.tests(TestResult.Status.FAILED);
        html.append("<h1>Test quality report</h1>\n<p class=\"sub\">SauceDemo UI automation. Generated on ")
                .append(report.generatedOn());
        if (!report.run().started().isEmpty()) {
            html.append(". Run started ").append(report.run().started().first().format(TIME));
        }
        html.append(".</p>\n");

        if (failed == 0) {
            html.append("<p class=\"verdict ok\">No failed tests in this run</p>\n");
        } else {
            html.append("<p class=\"verdict bad\">").append(failed).append(failed == 1 ? " test" : " tests")
                    .append(" failed in this run</p>\n");
        }
        if (report.run().mixedRuns()) {
            html.append("<p class=\"note\" role=\"note\">The result files come from ")
                    .append(report.run().started().size())
                    .append(" different Maven runs, so the numbers below mix them. ")
                    .append("Run <code>.\\mvnw.cmd clean test</code> and generate the report again for a single run.</p>\n");
        }
    }

    private static void indicators(StringBuilder html, QualityReport report) {
        var p1 = report.withPriority(Scenario.Priority.P1).size();
        html.append("<dl class=\"kpis\">\n");
        indicator(html, "Tests passed", report.tests(TestResult.Status.PASSED) + " of " + report.totalTests(),
                report.tests(TestResult.Status.FAILED) + " failed, " + report.tests(TestResult.Status.SKIPPED) + " skipped");
        indicator(html, "Pass rate", QualityReport.percent(report.passRate()), "This run only, skipped tests excluded");
        indicator(html, "Critical journeys (P1)", report.criticalFullyAutomated() + " of " + p1 + " automated",
                report.criticalPassing() + " passing in this run");
        indicator(html, "High-risk gaps", String.valueOf(report.highRiskGaps()),
                "P1 and P2 scenarios without full automation");
        indicator(html, "Suite duration", QualityReport.duration(report.durationSeconds()),
                "Sum of the individual test times");
        html.append("</dl>\n");
    }

    private static void indicator(StringBuilder html, String label, String value, String note) {
        html.append("<div><dt>").append(esc(label)).append("</dt><dd>").append(esc(value))
                .append("<small>").append(esc(note)).append("</small></dd></div>\n");
    }

    private static void failures(StringBuilder html, QualityReport report) {
        var failures = report.failures();
        if (failures.isEmpty()) {
            return;
        }
        html.append("<h2>Failed tests</h2>\n<p class=\"hint\">The XML result shows that a test failed, not why. ")
                .append("Whether the cause is the application, the test code or the environment is decided in triage. ")
                .append("Screenshots are in <code>target/screenshots</code>.</p>\n")
                .append("<div class=\"table-wrap\"><table>\n<thead><tr><th>Class</th><th>Test</th><th>Message</th></tr></thead>\n<tbody>\n");
        for (var failure : failures) {
            html.append("<tr><td>").append(esc(failure.className())).append("</td><td><code>")
                    .append(esc(failure.displayName())).append("</code></td><td>")
                    .append(esc(failure.message())).append("</td></tr>\n");
        }
        html.append("</tbody></table></div>\n");
    }

    private static void scenarios(StringBuilder html, QualityReport report) {
        html.append("<h2>Scenarios</h2>\n<p class=\"hint\">")
                .append(report.scenarios().size()).append(" scenarios: ")
                .append(report.scenarios(Automation.FULL)).append(" fully automated, ")
                .append(report.scenarios(Automation.PARTIAL)).append(" partly, ")
                .append(report.scenarios(Automation.NONE)).append(" not automated. ")
                .append("Partly automated means the linked tests check only part of the expected result. ")
                .append("Automation levels are manually assessed; the report does not inspect assertions to prove completeness.</p>\n")
                .append("<div class=\"table-wrap\"><table>\n<thead><tr><th>ID</th><th>Priority</th><th>Scenario</th>")
                .append("<th>Automation</th><th>Status in this run</th><th>Linked tests</th></tr></thead>\n<tbody>\n");

        String currentArea = null;
        for (var evaluated : report.scenarios()) {
            var scenario = evaluated.scenario();
            if (!scenario.area().equals(currentArea)) {
                currentArea = scenario.area();
                html.append("<tr class=\"area\"><th colspan=\"6\" scope=\"colgroup\">").append(esc(currentArea)).append("</th></tr>\n");
            }
            html.append("<tr><td>").append(esc(scenario.id())).append("</td><td>").append(scenario.priority())
                    .append("</td><td>").append(esc(scenario.title()))
                    .append("<span class=\"detail\">Expected: ").append(esc(scenario.expected())).append("</span></td><td>")
                    .append(automationLabel(scenario.automation())).append("</td><td>").append(statusBadge(evaluated.status()));
            if (evaluated.status() == Status.FAILED || evaluated.status() == Status.NOT_RUN || evaluated.status() == Status.SKIPPED) {
                html.append("<span class=\"detail\">").append(esc(evaluated.detail())).append("</span>");
            }
            html.append("</td><td>").append(testLinks(scenario)).append("</td></tr>\n");
        }
        html.append("</tbody></table></div>\n");
    }

    private static void gaps(StringBuilder html, QualityReport report) {
        var gaps = report.gaps();
        html.append("<h2>Coverage gaps</h2>\n");
        if (gaps.isEmpty()) {
            html.append("<p class=\"hint\">Every scenario in the registry is fully automated.</p>\n");
            return;
        }
        html.append("<p class=\"hint\">Scenarios without full automation, most important first. ")
                .append("This list is only as complete as the registry: behavior nobody wrote down does not appear here.</p>\n")
                .append("<div class=\"table-wrap\"><table>\n<thead><tr><th>ID</th><th>Priority</th><th>Scenario</th><th>Automation</th></tr></thead>\n<tbody>\n");
        for (var gap : gaps) {
            var scenario = gap.scenario();
            html.append("<tr><td>").append(esc(scenario.id())).append("</td><td>").append(scenario.priority())
                    .append("</td><td>").append(esc(scenario.title())).append("</td><td>")
                    .append(automationLabel(scenario.automation())).append("</td></tr>\n");
        }
        html.append("</tbody></table></div>\n");
    }

    private static void reviewCandidates(StringBuilder html, QualityReport report) {
        var candidates = report.reviewCandidates();
        if (candidates.isEmpty()) {
            return;
        }
        html.append("<h2>Review candidates</h2>\n<p class=\"hint\">Automated scenarios whose expectation was never confirmed or ")
                .append("was last confirmed more than ").append(QualityReport.REVIEW_AFTER_DAYS)
                .append(" days ago. They are not necessarily wrong; check that the test still matches the application ")
                .append("and update <code>last_verified</code> in the registry.</p>\n")
                .append("<div class=\"table-wrap\"><table>\n<thead><tr><th>ID</th><th>Scenario</th><th>Last verified</th></tr></thead>\n<tbody>\n");
        for (var candidate : candidates) {
            var scenario = candidate.scenario();
            html.append("<tr><td>").append(esc(scenario.id())).append("</td><td>").append(esc(scenario.title()))
                    .append("</td><td>").append(scenario.lastVerified() == null ? "never" : scenario.lastVerified().toString())
                    .append("</td></tr>\n");
        }
        html.append("</tbody></table></div>\n");
    }

    private static void unlinked(StringBuilder html, QualityReport report) {
        var unlinked = report.unlinkedTests();
        if (unlinked.isEmpty()) {
            return;
        }
        html.append("<h2>Tests without a scenario</h2>\n<p class=\"hint\">These tests ran, but no registry scenario links to them. ")
                .append("Either add a scenario or check whether the test is still needed.</p>\n<ul>\n");
        for (var test : unlinked) {
            html.append("<li><code>").append(esc(test.className() + "#" + test.method())).append("</code></li>\n");
        }
        html.append("</ul>\n");
    }

    private static void classes(StringBuilder html, QualityReport report) {
        html.append("<h2>Test classes</h2>\n<div class=\"table-wrap\"><table>\n<thead><tr><th>Class</th>")
                .append("<th class=\"num\">Passed</th><th class=\"num\">Failed</th><th class=\"num\">Skipped</th>")
                .append("<th class=\"num\">Duration</th></tr></thead>\n<tbody>\n");
        for (var summary : report.classSummaries()) {
            html.append("<tr><td>").append(esc(summary.className())).append("</td><td class=\"num\">").append(summary.passed())
                    .append("</td><td class=\"num\">").append(summary.failed()).append("</td><td class=\"num\">")
                    .append(summary.skipped()).append("</td><td class=\"num\">")
                    .append(QualityReport.duration(summary.seconds())).append("</td></tr>\n");
        }
        html.append("</tbody></table></div>\n");
    }

    private static void about(StringBuilder html, QualityReport report) {
        html.append("<h2>About these numbers</h2>\n");
        var environment = report.run().environment().entrySet().stream()
                .map(entry -> entry.getKey() + " = " + String.join(", ", entry.getValue()))
                .collect(Collectors.joining("; "));
        if (!environment.isEmpty()) {
            html.append("<p class=\"hint\">Environment: ").append(esc(environment)).append(".</p>\n");
        }
        html.append("<p class=\"hint\">A pass rate describes one run. It does not measure overall quality, and a high rate ")
                .append("with gaps in critical journeys still leaves risk. Flaky tests cannot be identified from a single run; ")
                .append("that needs repeated, comparable runs. CI run history is stored on the <code>qa-history</code> branch; ")
                .append("local runs are not persisted automatically. ")
                .append("Tests of the reporting tool itself are not counted.</p>\n");
    }

    private static String automationLabel(Automation automation) {
        return switch (automation) {
            case FULL -> "Full";
            case PARTIAL -> "Partial";
            case NONE -> "None";
        };
    }

    private static String statusBadge(Status status) {
        return "<span class=\"badge s-" + status.name().toLowerCase(Locale.ROOT) + "\">" + status.label() + "</span>";
    }

    private static String testLinks(Scenario scenario) {
        if (scenario.tests().isEmpty()) {
            return "";
        }
        return scenario.tests().stream()
                .map(ref -> "<code>" + esc(ref.toString()) + "</code>")
                .collect(Collectors.joining("<br>"));
    }

    
    static String esc(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
