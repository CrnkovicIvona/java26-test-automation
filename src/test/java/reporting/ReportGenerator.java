package reporting;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;


public final class ReportGenerator {

    
    private static final String TOOLING_PACKAGE = "reporting.";

    
    private ReportGenerator() {}

    
    public static void main(String[] args) {
        var reportsDir = Path.of(argument(args, 0, "target/surefire-reports"));
        var registryFile = Path.of(argument(args, 1, "src/test/resources/scenario-registry.csv"));
        var outputDir = Path.of(argument(args, 2, "target/quality-report"));
        var historyDir = Path.of(argument(args, 3, "quality-history/runs"));

        try {
            var scenarios = ScenarioRegistry.load(registryFile);
            var run = SurefireReader.read(reportsDir).excludingPackage(TOOLING_PACKAGE);
            if (run.results().isEmpty()) {
                throw new IOException("The report directory has no application test results. Run the tests first.");
            }
            var report = QualityReport.build(scenarios, run, LocalDate.now());
            var currentRun = RunHistory.current(report);
            var history = RunHistory.recent(RunHistory.read(historyDir), currentRun);

            Files.createDirectories(outputDir);
            Files.writeString(outputDir.resolve("index.html"),
                    HtmlReport.render(report, history), StandardCharsets.UTF_8);
            Files.writeString(outputDir.resolve("summary.md"),
                    MarkdownSummary.render(report, history), StandardCharsets.UTF_8);
            Files.writeString(outputDir.resolve("current-run.csv"),
                    RunHistory.csvLine(currentRun) + System.lineSeparator(), StandardCharsets.UTF_8);

            System.out.println(MarkdownSummary.oneLine(report));
            if (run.mixedRuns()) {
                System.out.println("Warning: result files come from " + run.started().size()
                        + " different Maven runs. Use 'clean test' for a single-run report.");
            }
            System.out.println("Report written to " + outputDir.resolve("index.html").toAbsolutePath());
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Could not create the report: " + e.getMessage());
            System.exit(2);
        }
    }

    private static String argument(String[] args, int index, String fallback) {
        return args.length > index ? args[index] : fallback;
    }
}
