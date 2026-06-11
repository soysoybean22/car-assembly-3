package service;

public class ViolationResult {
    private final boolean valid;
    private final String reason;

    private ViolationResult(boolean valid, String reason) {
        this.valid  = valid;
        this.reason = reason;
    }

    public static ViolationResult pass() {
        return new ViolationResult(true, null);
    }

    public static ViolationResult fail(String reason) {
        return new ViolationResult(false, reason);
    }

    public boolean isValid()  { return valid; }
    public String getReason() { return reason; }
}
