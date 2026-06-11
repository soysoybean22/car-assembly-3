# Phase 5 설계 — AssemblyFlow 분리 및 main() 정리

## 진행 방식

```
1. AssemblyFlowTest 작성 (AssemblyFlow 없음 → RED)
2. ./gradlew test → 컴파일 에러 실패 확인
3. AssemblyFlow 구현 + Assemble.java main() 정리
4. ./gradlew test → 전체 GREEN 확인
```

## 목표 요약

`Assemble.java`의 while 루프 전체를 `AssemblyFlow`로 이전한다.
`main()`은 의존성을 생성하고 `new AssemblyFlow(...).run()`을 호출하는 것만 남긴다.
`Assemble.java`에서 비즈니스 로직 메서드(`isValidCheck`, `runProducedCar`, `testProducedCar`, `selectXxx` 등)를 모두 제거한다.

---

## 파일 목록

```
src/
├── main/java/
│   ├── Assemble.java                     ← main()만 남기고 나머지 제거
│   ├── model/                            ← 변경 없음
│   ├── service/                          ← 변경 없음
│   └── ui/
│       ├── InputHandler.java             ← 변경 없음
│       ├── MenuPrinter.java              ← 변경 없음
│       └── AssemblyFlow.java             ← 신규
└── test/java/
    ├── model/                            ← 변경 없음
    ├── service/                          ← 변경 없음
    └── ui/
        ├── InputHandlerTest.java         ← 변경 없음
        └── AssemblyFlowTest.java         ← 신규
```

---

## 각 클래스 설계

### AssemblyFlow.java

```java
package ui;

import model.*;
import service.*;

public class AssemblyFlow {

    private final InputHandler    input;
    private final MenuPrinter     printer;
    private final CarAssemblyService service;

    private static final int CAR_TYPE_Q    = 0;
    private static final int ENGINE_Q      = 1;
    private static final int BRAKE_Q       = 2;
    private static final int STEERING_Q    = 3;
    private static final int RUN_TEST      = 4;

    public AssemblyFlow(InputHandler input, MenuPrinter printer, CarAssemblyService service) {
        this.input   = input;
        this.printer = printer;
        this.service = service;
    }

    public void run() {
        CarSpec spec = new CarSpec();
        int step = CAR_TYPE_Q;

        while (true) {
            printer.clearScreen();
            printMenu(step);

            int answer = input.readInt();
            if (answer == -1) { printer.printMessage("바이바이"); break; }

            if (!isValidRange(step, answer)) continue;

            if (answer == 0) {
                if (step == RUN_TEST) { spec.reset(); step = CAR_TYPE_Q; }
                else if (step > CAR_TYPE_Q) step--;
                continue;
            }

            step = applyAnswer(step, answer, spec);
        }
    }

    private void printMenu(int step) { ... }         // step별 printer 위임
    private boolean isValidRange(int step, int ans) { ... }  // 범위 검증 + 오류 메시지
    private int applyAnswer(int step, int answer, CarSpec spec) { ... }  // 선택 반영 + 다음 step 반환
}
```

**설계 근거:**
- `CarSpec spec`을 루프 내 지역 변수로 두고, "처음으로" 선택 시 `spec.reset()`을 호출한다 — `static` 전역 상태를 완전히 제거.
- `applyAnswer()`가 다음 `step`을 반환하도록 설계 — step 전환 규칙이 한 메서드에 집중된다.
- `isValidRange()`는 범위 오류 메시지 출력도 함께 담당 — `AssemblyFlow`가 흐름 문맥을 알고 있으므로 적합.

### Assemble.java (정리 후)

```java
import service.*;
import ui.*;
import java.util.Scanner;

public class Assemble {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        CompatibilityChecker checker = new CompatibilityChecker();
        CarAssemblyService   service = new CarAssemblyService(checker);
        InputHandler         input   = new InputHandler(sc);
        MenuPrinter          printer = new MenuPrinter();
        new AssemblyFlow(input, printer, service).run();
        sc.close();
    }
}
```

---

## 테스트 설계

`AssemblyFlow`는 Scanner를 통한 I/O에 의존하므로, `InputHandler`에 문자열 Scanner를 주입해 흐름 전체를 시뮬레이션한다.

### AssemblyFlowTest.java

```java
// 전체 흐름: Sedan(1) GM(1) MANDO(1) BOSCH(1) → RUN(1) → exit
@Test void 전체_흐름_RUN_성공() {
    String input = "1\n1\n1\n1\n1\nexit\n";
    List<String> output = run(input);
    assertTrue(output.stream().anyMatch(l -> l.contains("자동차가 동작됩니다")));
}

// 뒤로가기: CarType Sedan(1) → Engine에서 뒤로(0) → Engine GM(1) → ...
@Test void 엔진_뒤로가기_후_재선택() {
    String input = "1\n0\n1\n1\n1\n1\nexit\n";
    List<String> output = run(input);
    assertTrue(output.stream().anyMatch(l -> l.contains("자동차가 동작됩니다")));
}

// exit → 루프 종료
@Test void exit_입력시_종료() {
    List<String> output = run("exit\n");
    assertTrue(output.stream().anyMatch(l -> l.contains("바이바이")));
}

// 처음으로(0) 후 재조립
@Test void RunTest단계에서_처음으로_돌아가기() {
    // CarType(1) Engine(1) Brake(1) Steering(1) → RunTest에서 0(처음) → CarType(1) ...
    String input = "1\n1\n1\n1\n0\n1\n1\n1\n1\n1\nexit\n";
    List<String> output = run(input);
    assertTrue(output.stream().anyMatch(l -> l.contains("자동차가 동작됩니다")));
}

// 범위 외 입력 무시 후 유효 입력 처리
@Test void 범위외_입력_무시() {
    String input = "9\n1\n1\n1\n1\n2\nexit\n";
    List<String> output = run(input);
    assertTrue(output.stream().anyMatch(l -> l.contains("PASS") || l.contains("FAIL")));
}
```

---

## 설계 결정 사항

| 결정 | 이유 |
|------|------|
| `spec.reset()` 으로 처음 화면 복귀 | `static int[] stack` 제거 후 상태 초기화를 CarSpec이 담당 |
| `applyAnswer()`가 다음 step 반환 | step 전환 규칙의 단일 출처 확보 |
| 테스트에서 출력을 `ByteArrayOutputStream`으로 캡처 | `MenuPrinter`에 `PrintStream` 주입 가능하므로 출력 내용 검증 가능 |

---

## Phase 5 완료 기준

- `./gradlew test` 전체 GREEN (Phase 1~4 테스트 포함)
- `ui/AssemblyFlow.java` 추가
- `Assemble.java` — `main()`만 잔존, 비즈니스 로직 메서드 없음
- `src/test/java/ui/AssemblyFlowTest.java` 추가
