package ui;

import org.junit.jupiter.api.Test;
import service.CarAssemblyService;
import service.CompatibilityChecker;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import static org.junit.jupiter.api.Assertions.*;

class AssemblyFlowTest {

    // 전체 흐름: Sedan(1) GM(1) MANDO(1) BOSCH(1) → RUN(1) → exit
    @Test
    void 전체_흐름_RUN_성공() {
        List<String> output = run("1\n1\n1\n1\n1\nexit\n");
        assertTrue(output.stream().anyMatch(l -> l.contains("자동차가 동작됩니다")));
    }

    // 엔진 단계에서 뒤로가기 후 재선택
    @Test
    void 엔진_뒤로가기_후_재선택() {
        // CarType Sedan(1) → Engine 뒤로(0) → CarType Sedan(1) → Engine GM(1) → Brake MANDO(1) → Steering BOSCH(1) → RUN(1) → exit
        List<String> output = run("1\n0\n1\n1\n1\n1\n1\nexit\n");
        assertTrue(output.stream().anyMatch(l -> l.contains("자동차가 동작됩니다")));
    }

    // exit 입력 시 루프 종료
    @Test
    void exit_입력시_종료() {
        List<String> output = run("exit\n");
        assertTrue(output.stream().anyMatch(l -> l.contains("바이바이")));
    }

    // RunTest 단계에서 0 입력 → 처음으로 돌아가기
    @Test
    void RunTest단계에서_처음으로_돌아가기() {
        // 1차 조립 → RunTest에서 0 → 2차 조립 → RUN
        List<String> output = run("1\n1\n1\n1\n0\n1\n1\n1\n1\n1\nexit\n");
        assertTrue(output.stream().anyMatch(l -> l.contains("자동차가 동작됩니다")));
    }

    // 범위 외 입력 무시 후 유효 입력 처리
    @Test
    void 범위외_입력_무시() {
        // CarType 9(무시) → 1(Sedan) → GM(1) → MANDO(1) → BOSCH(1) → Test(2) → exit
        List<String> output = run("9\n1\n1\n1\n1\n2\nexit\n");
        assertTrue(output.stream().anyMatch(l -> l.contains("PASS") || l.contains("FAIL")));
    }

    // Test 결과 — 유효 조합 PASS
    @Test
    void test_유효조합_PASS_출력() {
        List<String> output = run("1\n1\n1\n1\n2\nexit\n");
        assertTrue(output.stream().anyMatch(l -> l.contains("PASS")));
    }

    // Test 결과 — 비호환 조합 FAIL
    @Test
    void test_비호환조합_FAIL_출력() {
        // Sedan(1) GM(1) CONTINENTAL(2) BOSCH(1) → Test(2) → exit
        List<String> output = run("1\n1\n2\n1\n2\nexit\n");
        assertTrue(output.stream().anyMatch(l -> l.contains("FAIL")));
    }

    private List<String> run(String input) {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(buf);

        InputHandler         handler = new InputHandler(new Scanner(input), ps);
        MenuPrinter          printer = new MenuPrinter(ps);
        CarAssemblyService   service = new CarAssemblyService(new CompatibilityChecker());
        AssemblyFlow         flow    = new AssemblyFlow(handler, printer, service);

        flow.run();

        return Arrays.asList(buf.toString().split("\n"));
    }
}
