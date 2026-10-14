package reporting;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;


public record Scenario(
        String id,
        String area,
        String title,
        String expected,
        Priority priority,
        Automation automation,
        List<TestRef> tests,
        LocalDate lastVerified) {

    
    public Scenario {
        tests = List.copyOf(tests);
    }

    
    public enum Priority { P1, P2, P3 }

    
    public enum Automation { FULL, PARTIAL, NONE }

    
    public record TestRef(String className, String method, Integer invocation) {

        private static final Pattern INVOCATION_SUFFIX = Pattern.compile("^(\\w+)\\[(\\d+)]$");

        
        public static TestRef parse(String text) {
            var parts = text.trim().split("#", 2);
            if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
                throw new IllegalArgumentException(
                        "Test reference must look like ClassName#method or ClassName#method[2]: '" + text + "'");
            }
            var method = parts[1].trim();
            Integer invocation = null;
            var matcher = INVOCATION_SUFFIX.matcher(method);
            if (matcher.matches()) {
                method = matcher.group(1);
                invocation = Integer.valueOf(matcher.group(2));
            }
            return new TestRef(parts[0].trim(), method, invocation);
        }

        
        @Override
        public String toString() {
            return className + "#" + method + (invocation == null ? "" : "[" + invocation + "]");
        }
    }
}
