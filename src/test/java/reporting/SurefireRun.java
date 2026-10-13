package reporting;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;


public record SurefireRun(
        List<TestResult> results,
        SortedSet<LocalDateTime> started,
        Map<String, SortedSet<String>> environment) {

    
    public boolean mixedRuns() {
        return started.size() > 1;
    }

    
    public SurefireRun excludingPackage(String packagePrefix) {
        var kept = results.stream()
                .filter(result -> !result.fullClassName().startsWith(packagePrefix))
                .toList();
        return new SurefireRun(kept, started, environment);
    }
}
