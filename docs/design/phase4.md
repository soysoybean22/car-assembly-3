# Phase 4 설계 — UI 레이어 분리

## 진행 방식

```
1. InputHandlerTest 작성 (ui 클래스 없음 → RED)
2. ./gradlew test → 컴파일 에러 실패 확인
3. InputHandler, MenuPrinter 구현
4. ./gradlew test → 전체 GREEN 확인
```

## 목표 요약

콘솔 출력 책임을 `MenuPrinter`로, 입력 파싱·검증 책임을 `InputHandler`로 분리한다.
`InputHandler`는 `Scanner`를 생성자로 주입받아 테스트 시 문자열로 대체 가능하다.
`MenuPrinter`는 순수 출력만 담당하므로 별도 테스트 없이 구현한다.
이 Phase에서 `Assemble.java`는 수정하지 않는다.

---

## 파일 목록

```
src/
├── main/java/
│   ├── Assemble.java                     ← 변경 없음
│   ├── model/                            ← 변경 없음
│   ├── service/                          ← 변경 없음
│   └── ui/
│       ├── InputHandler.java             ← 신규
│       └── MenuPrinter.java              ← 신규
└── test/java/
    ├── model/                            ← 변경 없음
    ├── service/                          ← 변경 없음
    └── ui/
        └── InputHandlerTest.java         ← 신규
```

---

## 각 클래스 설계

### InputHandler.java

```java
package ui;

import java.util.Scanner;

public class InputHandler {

    private final Scanner scanner;
    private final PrintStream out;  // 오류 메시지 출력 대상 (테스트 시 교체 가능)

    public InputHandler(Scanner scanner) {
        this(scanner, System.out);
    }

    public InputHandler(Scanner scanner, PrintStream out) {
        this.scanner = scanner;
        this.out     = out;
    }

    // 숫자가 입력될 때까지 반복 요청
    public int readInt() {
        while (true) {
            String line = scanner.nextLine().trim();
            if (isExit(line)) return -1;  // exit 신호를 -1로 전달
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
```

**설계 근거:**
- `readInt()`가 `exit`를 만나면 `-1`을 반환한다. 호출 측(`AssemblyFlow`)에서 `-1` 체크로 종료를 판단한다.
- `PrintStream out`을 생성자 인수로 받아 테스트에서 `new PrintStream(OutputStream.nullOutputStream())`으로 출력을 억제할 수 있다.
- 범위 검증은 `InputHandler`가 아닌 `AssemblyFlow`(Phase 5)에서 담당한다 — InputHandler는 "숫자인가"만 판별한다.

---

### MenuPrinter.java

```java
package ui;

import service.RunResult;
import service.TestResult;
import java.io.PrintStream;

public class MenuPrinter {

    private final PrintStream out;

    public MenuPrinter() { this(System.out); }
    public MenuPrinter(PrintStream out) { this.out = out; }

    public void clearScreen()          { out.print("\033[H\033[2J"); out.flush(); }
    public void printCarTypeMenu()     { /* 차량 타입 메뉴 출력 */ }
    public void printEngineMenu()      { /* 엔진 메뉴 출력 */ }
    public void printBrakeMenu()       { /* 제동장치 메뉴 출력 */ }
    public void printSteeringMenu()    { /* 조향장치 메뉴 출력 */ }
    public void printRunTestMenu()     { /* RUN/Test 메뉴 출력 */ }
    public void printRunResult(RunResult r)   { out.println(r.getMessage()); }
    public void printTestResult(TestResult r) {
        out.println("자동차 부품 조합 테스트 결과 : " + (r.isPassed() ? "PASS" : "FAIL"));
        if (!r.isPassed()) out.println(r.getReason());
    }
    public void printMessage(String msg) { out.println(msg); }
}
```

---

## 테스트 설계

### InputHandlerTest.java

```java
// 숫자 외 문자 → 재입력 요청 후 다음 숫자 반환
@Test void 비숫자_입력후_숫자_반환() {
    InputHandler h = handler("abc\n3\n");
    assertEquals(3, h.readInt());
}

// 정상 숫자 바로 반환
@Test void 숫자_즉시_반환() {
    assertEquals(2, handler("2\n").readInt());
}

// exit → -1 반환
@Test void exit_입력시_음수1_반환() {
    assertEquals(-1, handler("exit\n").readInt());
}

// EXIT 대소문자 무관
@Test void EXIT_대문자_음수1_반환() {
    assertEquals(-1, handler("EXIT\n").readInt());
}

// isExit
@Test void isExit_exit_true()  { assertTrue(new InputHandler(scanner("")).isExit("exit")); }
@Test void isExit_other_false(){ assertFalse(new InputHandler(scanner("")).isExit("1")); }

// 헬퍼
private InputHandler handler(String input) {
    return new InputHandler(new Scanner(input), new PrintStream(OutputStream.nullOutputStream()));
}
```

---

## 설계 결정 사항

| 결정 | 이유 |
|------|------|
| `readInt()`에서 exit → `-1` 반환 | 예외 대신 값으로 신호를 전달 — 호출 측 try-catch 없이 단순 비교로 처리 |
| 범위 검증은 `AssemblyFlow`에서 담당 | `InputHandler`는 파싱 책임만 가짐. 범위 규칙은 흐름을 아는 `AssemblyFlow`가 더 적합 |
| `MenuPrinter` 테스트 없음 | 순수 출력만 담당 — 로직이 없으므로 테스트 대상이 아님 |
| `PrintStream` 주입 | `System.out` 직접 참조를 끊어 테스트 시 출력 억제 가능 |

---

## Phase 4 완료 기준

- `./gradlew test` 전체 GREEN (Phase 1·2·3 테스트 포함)
- `ui/InputHandler.java`, `ui/MenuPrinter.java` 추가
- `src/test/java/ui/InputHandlerTest.java` 추가
- `Assemble.java` 변경 없음
