package reporting;

import reporting.EvaluatedScenario.Status;
import reporting.Scenario.Automation;
import reporting.Scenario.TestRef;

import java.util.ArrayList;
import java.util.List;


final class ScenarioEvaluator {

    
    private ScenarioEvaluator() {}

    
    static EvaluatedScenario evaluate(Scenario scenario, List<TestResult> results) {
        if (scenario.automation() == Automation.NONE) {
            return new EvaluatedScenario(scenario, Status.NOT_AUTOMATED, "No automated test is linked.");
        }

        var failed = new ArrayList<String>();
        var skipped = new ArrayList<String>();
        var missing = new ArrayList<String>();
        var passed = 0;

        for (var ref : scenario.tests()) {
            var matches = results.stream().filter(result -> matches(ref, result)).toList();
            if (matches.isEmpty()) {
                missing.add(ref.toString());
                continue;
            }
            for (var result : matches) {
                switch (result.status()) {
                    case FAILED -> failed.add(result.message().isBlank()
                            ? result.displayName()
                            : result.displayName() + ": " + result.message());
                    case SKIPPED -> skipped.add(result.displayName());
                    case PASSED -> passed++;
                }
            }
        }

        if (!failed.isEmpty()) {
            return new EvaluatedScenario(scenario, Status.FAILED, String.join("; ", failed));
        }
        if (!missing.isEmpty()) {
            return new EvaluatedScenario(scenario, Status.NOT_RUN,
                    "Linked test not found in this run: " + String.join(", ", missing));
        }
        if (!skipped.isEmpty()) {
            return new EvaluatedScenario(scenario, Status.SKIPPED, "Skipped: " + String.join(", ", skipped));
        }
        return new EvaluatedScenario(scenario, Status.PASSED, passed + (passed == 1 ? " test passed." : " tests passed."));
    }

    
    static boolean matches(TestRef ref, TestResult result) {
        return ref.className().equals(result.className())
                && ref.method().equals(result.method())
                && (ref.invocation() == null || ref.invocation().equals(result.invocation()));
    }
}
