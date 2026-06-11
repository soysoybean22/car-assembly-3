package ui;

import service.RunResult;
import service.TestResult;
import java.io.PrintStream;

public class MenuPrinter {

    private final PrintStream out;

    public MenuPrinter() { this(System.out); }
    public MenuPrinter(PrintStream out) { this.out = out; }

    public void clearScreen() { out.print("\033[H\033[2J"); out.flush(); }

    public void printCarTypeMenu() {
        out.println("        ______________");
        out.println("       /|            |");
        out.println("  ____/_|_____________|____");
        out.println(" |                      O  |");
        out.println(" '-(@)----------------(@)--'");
        out.println("===============================");
        out.println("어떤 차량 타입을 선택할까요?");
        out.println("1. Sedan");
        out.println("2. SUV");
        out.println("3. Truck");
        out.println("===============================");
    }

    public void printEngineMenu() {
        out.println("어떤 엔진을 탑재할까요?");
        out.println("0. 뒤로가기");
        out.println("1. GM");
        out.println("2. TOYOTA");
        out.println("3. WIA");
        out.println("4. 고장난 엔진");
        out.println("===============================");
    }

    public void printBrakeMenu() {
        out.println("어떤 제동장치를 선택할까요?");
        out.println("0. 뒤로가기");
        out.println("1. MANDO");
        out.println("2. CONTINENTAL");
        out.println("3. BOSCH");
        out.println("===============================");
    }

    public void printSteeringMenu() {
        out.println("어떤 조향장치를 선택할까요?");
        out.println("0. 뒤로가기");
        out.println("1. BOSCH");
        out.println("2. MOBIS");
        out.println("===============================");
    }

    public void printRunTestMenu() {
        out.println("멋진 차량이 완성되었습니다.");
        out.println("어떤 동작을 할까요?");
        out.println("0. 처음 화면으로 돌아가기");
        out.println("1. RUN");
        out.println("2. Test");
        out.println("===============================");
    }

    public void printRunResult(RunResult r) {
        out.println(r.getMessage());
    }

    public void printTestResult(TestResult r) {
        out.println("자동차 부품 조합 테스트 결과 : " + (r.isPassed() ? "PASS" : "FAIL"));
        if (!r.isPassed()) out.println(r.getReason());
    }

    public void printMessage(String msg) {
        out.println(msg);
    }
}
