package reporting;


public record TestResult(
        String fullClassName,
        String method,
        String displayName,
        Integer invocation,
        Status status,
        double seconds,
        String message) {

    
    public enum Status { PASSED, FAILED, SKIPPED }

    
    public String className() {
        return fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
    }
}
