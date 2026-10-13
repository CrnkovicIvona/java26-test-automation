package reporting;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

final class RunHistory {

    private static final int DISPLAY_LIMIT = 10;
    private static final Pattern RUN_ID = Pattern.compile("[A-Za-z0-9._-]+");

    private RunHistory() {}

    static List<Entry> read(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            return List.of();
        }
        if (!Files.isDirectory(directory)) {
            throw new IOException("Test history path is not a directory: " + directory);
        }

        List<Path> files;
        try (var stream = Files.list(directory)) {
            files = stream.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".csv"))
                    .sorted()
                    .toList();
        }

        var entries = new ArrayList<Entry>();
        var runIds = new HashSet<String>();
        for (var file : files) {
            var lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            if (lines.size() != 1) {
                throw new IOException("Expected exactly one test history record in " + file);
            }
            var entry = parse(lines.getFirst(), file);
            if (!runIds.add(entry.runId())) {
                throw new IOException("Duplicate run id in test history: " + entry.runId());
            }
            entries.add(entry);
        }
        return entries.stream().sorted(Comparator.comparing(Entry::recordedAt)).toList();
    }

    static Entry current(QualityReport report) {
        var runId = System.getenv("GITHUB_RUN_ID");
        if (runId == null || runId.isBlank()) {
            runId = "local-" + UUID.randomUUID();
        }
        return new Entry(runId, Instant.now(),
                report.tests(TestResult.Status.PASSED),
                report.tests(TestResult.Status.FAILED),
                report.tests(TestResult.Status.SKIPPED),
                report.durationSeconds(),
                report.criticalPassing(),
                report.highRiskGaps());
    }

    static List<Entry> recent(List<Entry> history, Entry current) {
        var entriesById = new java.util.LinkedHashMap<String, Entry>();
        history.forEach(entry -> entriesById.put(entry.runId(), entry));
        entriesById.put(current.runId(), current);
        var ordered = entriesById.values().stream()
                .sorted(Comparator.comparing(Entry::recordedAt))
                .toList();
        var firstRecentIndex = Math.max(0, ordered.size() - DISPLAY_LIMIT);
        return ordered.subList(firstRecentIndex, ordered.size());
    }

    static String csvLine(Entry entry) {
        return String.join(";",
                entry.runId(),
                entry.recordedAt().toString(),
                Long.toString(entry.passed()),
                Long.toString(entry.failed()),
                Long.toString(entry.skipped()),
                String.format(Locale.ROOT, "%.3f", entry.durationSeconds()),
                Long.toString(entry.criticalPassing()),
                Long.toString(entry.highRiskGaps()));
    }

    private static Entry parse(String line, Path file) throws IOException {
        var fields = line.split(";", -1);
        if (fields.length != 8 || !RUN_ID.matcher(fields[0]).matches()) {
            throw new IOException("Invalid test history record in " + file);
        }
        try {
            var entry = new Entry(fields[0], Instant.parse(fields[1]),
                    Long.parseLong(fields[2]),
                    Long.parseLong(fields[3]),
                    Long.parseLong(fields[4]),
                    Double.parseDouble(fields[5]),
                    Long.parseLong(fields[6]),
                    Long.parseLong(fields[7]));
            if (entry.passed() < 0 || entry.failed() < 0 || entry.skipped() < 0
                    || entry.durationSeconds() < 0 || !Double.isFinite(entry.durationSeconds())
                    || entry.criticalPassing() < 0 || entry.highRiskGaps() < 0) {
                throw new IOException("Negative or non-finite metric in test history file " + file);
            }
            return entry;
        } catch (DateTimeParseException | NumberFormatException e) {
            throw new IOException("Invalid test history metric in " + file + ": " + e.getMessage(), e);
        }
    }

    record Entry(
            String runId,
            Instant recordedAt,
            long passed,
            long failed,
            long skipped,
            double durationSeconds,
            long criticalPassing,
            long highRiskGaps) {

        String passRate() {
            var executed = passed + failed;
            return executed == 0 ? "n/a"
                    : String.format(Locale.ROOT, "%.1f%%", 100.0 * passed / executed);
        }
    }
}
