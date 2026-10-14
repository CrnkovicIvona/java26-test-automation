package reporting;

import reporting.Scenario.Automation;
import reporting.Scenario.Priority;
import reporting.Scenario.TestRef;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;


public final class ScenarioRegistry {

    
    static final String HEADER = "id;area;title;expected;priority;automation;tests;last_verified";

    private static final Pattern ID_FORMAT = Pattern.compile("[A-Z][A-Z0-9]*-\\d{3}");

    
    private ScenarioRegistry() {}

    
    public static List<Scenario> load(Path file) throws IOException {
        var lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        var scenarios = new ArrayList<Scenario>();
        var ids = new HashSet<String>();
        var headerSeen = false;

        for (var index = 0; index < lines.size(); index++) {
            var lineNumber = index + 1;
            var line = index == 0 ? lines.get(index).replace("\uFEFF", "") : lines.get(index);
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            if (!headerSeen) {
                if (!line.trim().equalsIgnoreCase(HEADER)) {
                    throw error(lineNumber, "expected the header '" + HEADER + "'");
                }
                headerSeen = true;
                continue;
            }
            var scenario = parse(line, lineNumber);
            if (!ids.add(scenario.id())) {
                throw error(lineNumber, "duplicate scenario id " + scenario.id());
            }
            scenarios.add(scenario);
        }
        if (!headerSeen) {
            throw new IllegalArgumentException("Registry " + file + " is empty; expected the header '" + HEADER + "'");
        }
        return List.copyOf(scenarios);
    }

    
    private static Scenario parse(String line, int lineNumber) {
        var fields = Arrays.stream(line.split(";", -1)).map(String::trim).toList();
        if (fields.size() != 8) {
            throw error(lineNumber, "expected 8 fields separated by ';' but found " + fields.size());
        }
        var id = fields.get(0);
        if (!ID_FORMAT.matcher(id).matches()) {
            throw error(lineNumber, "id '" + id + "' must look like LOGIN-001");
        }
        for (var required : List.of(1, 2, 3)) {
            if (fields.get(required).isBlank()) {
                throw error(lineNumber, "area, title and expected must not be empty");
            }
        }

        var priority = parseEnum(Priority.class, fields.get(4), lineNumber, "priority");
        var automation = parseEnum(Automation.class, fields.get(5), lineNumber, "automation");
        var tests = parseTests(fields.get(6), lineNumber);
        if (automation == Automation.NONE && !tests.isEmpty()) {
            throw error(lineNumber, id + " is marked NONE but links tests; use PARTIAL or FULL");
        }
        if (automation != Automation.NONE && tests.isEmpty()) {
            throw error(lineNumber, id + " is marked " + automation + " but links no tests");
        }

        return new Scenario(id, fields.get(1), fields.get(2), fields.get(3),
                priority, automation, tests, parseDate(fields.get(7), lineNumber));
    }

    
    private static List<TestRef> parseTests(String text, int lineNumber) {
        if (text.isBlank()) {
            return List.of();
        }
        try {
            return Arrays.stream(text.split(",")).map(TestRef::parse).toList();
        } catch (IllegalArgumentException e) {
            throw error(lineNumber, e.getMessage());
        }
    }

    
    private static LocalDate parseDate(String text, int lineNumber) {
        if (text.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw error(lineNumber, "last_verified '" + text + "' must be an ISO date such as 2026-10-13");
        }
    }

    
    private static <E extends Enum<E>> E parseEnum(Class<E> type, String text, int lineNumber, String column) {
        try {
            return Enum.valueOf(type, text.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw error(lineNumber, column + " '" + text + "' must be one of " + Arrays.toString(type.getEnumConstants()));
        }
    }

    
    private static IllegalArgumentException error(int lineNumber, String message) {
        return new IllegalArgumentException("Registry line " + lineNumber + ": " + message);
    }
}
