package reporting;


public record EvaluatedScenario(Scenario scenario, Status status, String detail) {

    
    public enum Status {
        PASSED, FAILED, SKIPPED, NOT_RUN, NOT_AUTOMATED;

        
        public String label() {
            return switch (this) {
                case PASSED -> "Passed";
                case FAILED -> "Failed";
                case SKIPPED -> "Skipped";
                case NOT_RUN -> "Not run";
                case NOT_AUTOMATED -> "Not automated";
            };
        }
    }
}
