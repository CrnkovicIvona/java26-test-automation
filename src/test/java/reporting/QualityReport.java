package reporting;

import reporting.EvaluatedScenario.Status;
import reporting.Scenario.Automation;
import reporting.Scenario.Priority;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.OptionalDouble;
import java.util.TreeMap;


public record QualityReport(LocalDate generatedOn, SurefireRun run, List<EvaluatedScenario> scenarios) {

    
    public static final int REVIEW_AFTER_DAYS = 90;

    
    public static QualityReport build(List<Scenario> registry, SurefireRun run, LocalDate today) {
        var evaluated = registry.stream()
                .map(scenario -> ScenarioEvaluator.evaluate(scenario, run.results()))
                .toList();
        return new QualityReport(today, run, evaluated);
    }

    

    
    public long tests(TestResult.Status status) {
        return run.results().stream().filter(result -> result.status() == status).count();
    }

    
    public long totalTests() {
        return run.results().size();
    }

    
    public OptionalDouble passRate() {
        var passed = tests(TestResult.Status.PASSED);
        var failed = tests(TestResult.Status.FAILED);
        return passed + failed == 0 ? OptionalDouble.empty() : OptionalDouble.of(100.0 * passed / (passed + failed));
    }

    
    public double durationSeconds() {
        return run.results().stream().mapToDouble(TestResult::seconds).sum();
    }

    
    public List<TestResult> failures() {
        return run.results().stream().filter(result -> result.status() == TestResult.Status.FAILED).toList();
    }

    
    public List<ClassSummary> classSummaries() {
        var byClass = new TreeMap<String, List<TestResult>>();
        for (var result : run.results()) {
            byClass.computeIfAbsent(result.className(), key -> new java.util.ArrayList<>()).add(result);
        }
        return byClass.entrySet().stream().map(entry -> {
            var list = entry.getValue();
            return new ClassSummary(entry.getKey(),
                    count(list, TestResult.Status.PASSED),
                    count(list, TestResult.Status.FAILED),
                    count(list, TestResult.Status.SKIPPED),
                    list.stream().mapToDouble(TestResult::seconds).sum());
        }).toList();
    }

    
    public record ClassSummary(String className, long passed, long failed, long skipped, double seconds) {}

    private static long count(List<TestResult> list, TestResult.Status status) {
        return list.stream().filter(result -> result.status() == status).count();
    }

    

    
    public long scenarios(Automation automation) {
        return scenarios.stream().filter(e -> e.scenario().automation() == automation).count();
    }

    
    public long scenarios(Status status) {
        return scenarios.stream().filter(e -> e.status() == status).count();
    }

    
    public List<EvaluatedScenario> withPriority(Priority priority) {
        return scenarios.stream().filter(e -> e.scenario().priority() == priority).toList();
    }

    
    public long criticalFullyAutomated() {
        return withPriority(Priority.P1).stream()
                .filter(e -> e.scenario().automation() == Automation.FULL).count();
    }

    
    public long criticalPassing() {
        return withPriority(Priority.P1).stream()
                .filter(e -> e.scenario().automation() == Automation.FULL && e.status() == Status.PASSED)
                .count();
    }

    
    public List<EvaluatedScenario> gaps() {
        return scenarios.stream()
                .filter(e -> e.scenario().automation() != Automation.FULL)
                .sorted(Comparator.comparing((EvaluatedScenario e) -> e.scenario().priority())
                        .thenComparing(e -> e.scenario().id()))
                .toList();
    }

    
    public long highRiskGaps() {
        return gaps().stream().filter(e -> e.scenario().priority() != Priority.P3).count();
    }

    
    public List<EvaluatedScenario> reviewCandidates() {
        var cutoff = generatedOn.minusDays(REVIEW_AFTER_DAYS);
        return scenarios.stream()
                .filter(e -> e.scenario().automation() != Automation.NONE)
                .filter(e -> e.scenario().lastVerified() == null || e.scenario().lastVerified().isBefore(cutoff))
                .toList();
    }

    
    public List<TestResult> unlinkedTests() {
        var unlinked = new LinkedHashMap<String, TestResult>();
        for (var result : run.results()) {
            var linked = scenarios.stream().anyMatch(e -> e.scenario().tests().stream()
                    .anyMatch(ref -> ScenarioEvaluator.matches(ref, result)));
            if (!linked) {
                unlinked.putIfAbsent(result.className() + "#" + result.method(), result);
            }
        }
        return List.copyOf(unlinked.values());
    }

    

    
    public static String duration(double seconds) {
        if (seconds < 60) {
            return String.format(Locale.ROOT, "%.1f s", seconds);
        }
        var whole = Math.round(seconds);
        return String.format(Locale.ROOT, "%d m %02d s", whole / 60, whole % 60);
    }

    
    public static String percent(OptionalDouble value) {
        return value.isPresent() ? String.format(Locale.ROOT, "%.1f%%", value.getAsDouble()) : "n/a";
    }
}
