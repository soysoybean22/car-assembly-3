package service;

public class RunResult {
    private final boolean success;
    private final String message;

    private RunResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static RunResult success(String message) { return new RunResult(true, message); }
    public static RunResult failure(String message) { return new RunResult(false, message); }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}
