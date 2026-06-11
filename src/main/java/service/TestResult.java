package service;

public class TestResult {
    private final boolean passed;
    private final String reason;

    private TestResult(boolean passed, String reason) {
        this.passed = passed;
        this.reason = reason;
    }

    public static TestResult pass()              { return new TestResult(true, null); }
    public static TestResult fail(String reason) { return new TestResult(false, reason); }

    public boolean isPassed()  { return passed; }
    public String getReason()  { return reason; }
}
