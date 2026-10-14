package reporting;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import reporting.Scenario.TestRef;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


@Tag("reporting")
class ScenarioRegistryTest {

    
    private static final Path REGISTRY = Path.of("src/test/resources/scenario-registry.csv");

    private static final Path CUCUMBER_FEATURE =
            Path.of("src/test/resources/features/loginAndPurchase.feature");

    private static final Pattern CUCUMBER_SCENARIO =
            Pattern.compile("^\\s*Scenario(?: Outline)?:\\s*(.+?)\\s*$");

    
    @Test
    void registryShouldLoadAndContainScenarios() throws IOException {
        assertFalse(ScenarioRegistry.load(REGISTRY).isEmpty(), "The registry should contain scenarios");
    }

    
    @Test
    void everyLinkedTestShouldExistInTheTestSources() throws IOException {
        var problems = new ArrayList<String>();
        var cucumberScenarios = cucumberScenarioNames();
        for (var scenario : ScenarioRegistry.load(REGISTRY)) {
            for (var ref : scenario.tests()) {
                problems.addAll(checkLink(scenario.id(), ref, cucumberScenarios));
            }
        }
        assertTrue(problems.isEmpty(), () -> "Broken test links in the registry:\n" + String.join("\n", problems));
    }

    
    @Test
    void loaderShouldRejectUnknownPriority(@TempDir Path dir) throws IOException {
        var file = write(dir, "X-001;Area;Title;Expected;P9;NONE;;");

        var error = assertThrows(IllegalArgumentException.class, () -> ScenarioRegistry.load(file));

        assertTrue(error.getMessage().contains("line 2"), "The message should name the line: " + error.getMessage());
        assertTrue(error.getMessage().contains("priority"), error.getMessage());
    }

    
    @Test
    void loaderShouldRejectDuplicateIds(@TempDir Path dir) throws IOException {
        var file = write(dir,
                "X-001;Area;Title;Expected;P1;NONE;;",
                "X-001;Area;Other;Expected;P2;NONE;;");

        var error = assertThrows(IllegalArgumentException.class, () -> ScenarioRegistry.load(file));

        assertTrue(error.getMessage().contains("duplicate"), error.getMessage());
    }

    
    @Test
    void loaderShouldRejectAutomatedScenarioWithoutTests(@TempDir Path dir) throws IOException {
        var file = write(dir, "X-001;Area;Title;Expected;P1;FULL;;");

        var error = assertThrows(IllegalArgumentException.class, () -> ScenarioRegistry.load(file));

        assertTrue(error.getMessage().contains("links no tests"), error.getMessage());
    }

    private static Path write(Path dir, String... rows) throws IOException {
        var lines = new ArrayList<String>();
        lines.add(ScenarioRegistry.HEADER);
        lines.addAll(List.of(rows));
        return Files.write(dir.resolve("registry.csv"), lines);
    }

    private static List<String> cucumberScenarioNames() throws IOException {
        var names = new ArrayList<String>();
        for (var line : Files.readAllLines(CUCUMBER_FEATURE)) {
            var matcher = CUCUMBER_SCENARIO.matcher(line);
            if (matcher.matches()) {
                names.add(matcher.group(1));
            }
        }
        return List.copyOf(names);
    }

    private static List<String> checkLink(String scenarioId, TestRef ref, List<String> cucumberScenarios) {
        if (ref.className().equals("CucumberTest")) {
            if (ref.invocation() != null) {
                return List.of(scenarioId + ": invocation suffix is not valid for Cucumber scenarios: " + ref);
            }
            return cucumberScenarios.contains(ref.method())
                    ? List.of()
                    : List.of(scenarioId + ": Cucumber scenario not found: " + ref.method());
        }

        Class<?> type;
        try {
            type = Class.forName("tests." + ref.className());
        } catch (ClassNotFoundException e) {
            return List.of(scenarioId + ": class not found: tests." + ref.className());
        }
        var method = Arrays.stream(type.getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals(ref.method()))
                .findFirst();
        if (method.isEmpty()) {
            return List.of(scenarioId + ": method not found: " + ref);
        }
        var parameterized = method.get().isAnnotationPresent(ParameterizedTest.class);
        if (!parameterized && !method.get().isAnnotationPresent(Test.class)) {
            return List.of(scenarioId + ": not a test method: " + ref);
        }
        if (ref.invocation() != null && !parameterized) {
            return List.of(scenarioId + ": [n] is only valid for parameterized tests: " + ref);
        }
        return List.of();
    }
}
