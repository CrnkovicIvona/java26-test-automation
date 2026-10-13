package reporting;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("reporting")
class RunHistoryTest {

    @Test
    void readsAndSortsRunRecords(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("2.csv"),
                "2;2026-10-13T12:00:00Z;8;2;0;50.000;4;1");
        Files.writeString(dir.resolve("1.csv"),
                "1;2026-10-13T11:00:00Z;9;1;0;45.000;5;0");

        var entries = RunHistory.read(dir);

        assertEquals(List.of("1", "2"), entries.stream().map(RunHistory.Entry::runId).toList());
        assertEquals("90.0%", entries.getFirst().passRate());
    }

    @Test
    void rejectsMalformedHistoryRecords(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("broken.csv"), "not-a-valid-record");

        var error = assertThrows(IOException.class, () -> RunHistory.read(dir));

        assertTrue(error.getMessage().contains("Invalid test history record"));
    }

    @Test
    void recentHistoryContainsAtMostTenEntriesAndCurrentRun() {
        var previous = java.util.stream.IntStream.range(0, 12)
                .mapToObj(index -> new RunHistory.Entry(
                        Integer.toString(index), Instant.parse("2026-10-13T10:00:00Z").plusSeconds(index),
                        1, 0, 0, 1, 1, 0))
                .toList();
        var current = new RunHistory.Entry("current", Instant.parse("2026-10-13T12:00:00Z"),
                1, 0, 0, 1, 1, 0);

        var recent = RunHistory.recent(previous, current);

        assertEquals(10, recent.size());
        assertEquals("current", recent.getLast().runId());
        assertEquals("3", recent.getFirst().runId());
    }

    @Test
    void reportRendersPriorAndCurrentRunHistory() {
        var report = new QualityReport(LocalDate.of(2026, 10, 13),
                new SurefireRun(List.of(), new TreeSet<>(), Map.of()), List.of());
        var history = List.of(
                new RunHistory.Entry("prior-run", Instant.parse("2026-10-12T12:00:00Z"),
                        9, 1, 0, 50, 4, 1),
                new RunHistory.Entry("current-run", Instant.parse("2026-10-13T12:00:00Z"),
                        10, 0, 0, 45, 5, 0));

        var html = HtmlReport.render(report, history);
        var markdown = MarkdownSummary.render(report, history);

        assertTrue(html.contains("Recent test runs"));
        assertTrue(html.contains("prior-run"));
        assertTrue(markdown.contains("prior-run"));
        assertTrue(markdown.contains("current-run"));
    }
}
