package ui;

import java.io.PrintStream;
import java.util.Scanner;

public class InputHandler {

    private final Scanner scanner;
    private final PrintStream out;

    public InputHandler(Scanner scanner) {
        this(scanner, System.out);
    }

    public InputHandler(Scanner scanner, PrintStream out) {
        this.scanner = scanner;
        this.out     = out;
    }

    public int readInt() {
        while (true) {
            String line = scanner.nextLine().trim();
            if (isExit(line)) return -1;
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                out.println("ERROR :: 숫자만 입력 가능");
            }
        }
    }

    public boolean isExit(String input) {
        return "exit".equalsIgnoreCase(input.trim());
    }
}
