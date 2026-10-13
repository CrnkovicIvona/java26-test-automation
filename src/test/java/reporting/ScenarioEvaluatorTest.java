package reporting;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import reporting.Scenario.Automation;
import reporting.Scenario.Priority;
import reporting.Scenario.TestRef;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


@Tag("reporting")
class ScenarioEvaluatorTest {

    
    @Test
    void scenarioShouldPassWhenAllLinkedTestsPass() {
        var scenario = scenario(Automation.FULL, "LoginTest#a", "LoginTest#b");

        var result = ScenarioEvaluator.evaluate(scenario,
                List.of(result("LoginTest", "a", null, TestResult.Status.PASSED),
                        result("LoginTest", "b", null, TestResult.Status.PASSED)));

        assertEquals(EvaluatedScenario.Status.PASSED, result.status(), result.detail());
    }

    @Test
    void cucumberScenarioReferenceShouldMatchSurefireScenarioName() {
        var evaluated = ScenarioEvaluator.evaluate(
                scenario(Automation.FULL, "CucumberTest#Wrong password is rejected"),
                List.of(result("CucumberTest", "Wrong password is rejected", null,
                        TestResult.Status.PASSED)));

        assertEquals(EvaluatedScenario.Status.PASSED, evaluated.status(), evaluated.detail());
    }

    
    @Test
    void failingInvocationShouldAffectOnlyTheScenarioLinkedToIt() {
        var results = List.of(
                result("LoginTest", "invalid", 2, TestResult.Status.PASSED),
                result("LoginTest", "invalid", 3, TestResult.Status.FAILED));

        var second = ScenarioEvaluator.evaluate(scenario(Automation.FULL, "LoginTest#invalid[2]"), results);
        var third = ScenarioEvaluator.evaluate(scenario(Automation.FULL, "LoginTest#invalid[3]"), results);

        assertEquals(EvaluatedScenario.Status.PASSED, second.status());
        assertEquals(EvaluatedScenario.Status.FAILED, third.status());
    }

    
    @Test
    void linkWithoutInvocationShouldFailWhenAnyInvocationFails() {
        var results = List.of(
                result("LoginTest", "invalid", 1, TestResult.Status.PASSED),
                result("LoginTest", "invalid", 2, TestResult.Status.FAILED));

        var evaluated = ScenarioEvaluator.evaluate(scenario(Automation.FULL, "LoginTest#invalid"), results);

        assertEquals(EvaluatedScenario.Status.FAILED, evaluated.status());
    }

    
    @Test
    void scenarioShouldBeNotRunWhenLinkedTestIsMissing() {
        var evaluated = ScenarioEvaluator.evaluate(scenario(Automation.FULL, "LoginTest#missing"),
                List.of(result("LoginTest", "other", null, TestResult.Status.PASSED)));

        assertEquals(EvaluatedScenario.Status.NOT_RUN, evaluated.status());
    }

    
    @Test
    void failureShouldOutrankSkippedAndMissingTests() {
        var evaluated = ScenarioEvaluator.evaluate(scenario(Automation.FULL, "A#x", "A#y", "A#z"),
                List.of(result("A", "x", null, TestResult.Status.FAILED),
                        result("A", "y", null, TestResult.Status.SKIPPED)));

        assertEquals(EvaluatedScenario.Status.FAILED, evaluated.status());
    }

    
    @Test
    void scenarioWithoutAutomationShouldBeNotAutomated() {
        var evaluated = ScenarioEvaluator.evaluate(scenario(Automation.NONE),
                List.of(result("A", "x", null, TestResult.Status.PASSED)));

        assertEquals(EvaluatedScenario.Status.NOT_AUTOMATED, evaluated.status());
    }

    private static Scenario scenario(Automation automation, String... refs) {
        return new Scenario("T-001", "Area", "Title", "Expected", Priority.P2, automation,
                java.util.Arrays.stream(refs).map(TestRef::parse).toList(), null);
    }

    private static TestResult result(String className, String method, Integer invocation, TestResult.Status status) {
        return new TestResult("tests." + className, method, method, invocation, status, 1.0,
                status == TestResult.Status.FAILED ? "boom" : "");
    }
}
