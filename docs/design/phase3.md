# Phase 3 설계 — CarAssemblyService 분리

## 진행 방식

```
1. RunResult, TestResult, CarAssemblyServiceTest 작성 (service 클래스 없음 → RED)
2. ./gradlew test → 컴파일 에러 실패 확인
3. CarAssemblyService 구현
4. ./gradlew test → 전체 GREEN 확인
```

## 목표 요약

`Assemble.java`의 `runProducedCar()`, `testProducedCar()` 로직을 콘솔 출력 없이 순수 Java 메서드로 분리한다.
`CompatibilityChecker`를 내부에서 사용하며, 결과를 객체로 반환하여 테스트 가능하게 만든다.
이 Phase에서 `Assemble.java`는 수정하지 않는다.

---

## 파일 목록

```
src/
├── main/java/
│   ├── Assemble.java                        ← 변경 없음
│   ├── model/                               ← 변경 없음
│   └── service/
│       ├── ViolationResult.java             ← 변경 없음
│       ├── CompatibilityChecker.java        ← 변경 없음
│       ├── RunResult.java                   ← 신규
│       ├── TestResult.java                  ← 신규
│       └── CarAssemblyService.java          ← 신규
└── test/java/
    ├── model/                               ← 변경 없음
    ├── service/
    │   ├── CompatibilityCheckerTest.java    ← 변경 없음
    │   └── CarAssemblyServiceTest.java      ← 신규
```

---

## 각 클래스 설계

### RunResult.java

```java
package service;

public class RunResult {
    private final boolean success;
    private final String message;

    private RunResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static RunResult success(String message) {
        return new RunResult(true, message);
    }

    public static RunResult failure(String message) {
        return new RunResult(false, message);
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}
```

### TestResult.java

```java
package service;

public class TestResult {
    private final boolean passed;
    private final String reason;  // FAIL 시 원인, PASS 시 null

    private TestResult(boolean passed, String reason) { ... }

    public static TestResult pass()              { return new TestResult(true, null); }
    public static TestResult fail(String reason) { return new TestResult(false, reason); }

    public boolean isPassed()  { return passed; }
    public String getReason()  { return reason; }
}
```

### CarAssemblyService.java

```java
package service;

import model.*;

public class CarAssemblyService {

    private final CompatibilityChecker checker;

    public CarAssemblyService(CompatibilityChecker checker) {
        this.checker = checker;
    }

    public RunResult run(CarSpec spec) {
        // 1. 호환성 검사
        ViolationResult v = checker.check(spec);
        if (!v.isValid())
            return RunResult.failure("자동차가 동작되지 않습니다\n" + v.getReason());

        // 2. 고장난 엔진 검사
        if (spec.getEngine().isBroken())
            return RunResult.failure("엔진이 고장나있습니다.\n자동차가 움직이지 않습니다.");

        // 3. 정상 동작 — 부품 요약 반환
        String summary = String.format(
            "Car Type : %s\nEngine   : %s\nBrake    : %s\nSteering : %s\n자동차가 동작됩니다.",
            spec.getCarType().getDisplayName(),
            spec.getEngine().getDisplayName(),
            spec.getBrakeSystem().getDisplayName(),
            spec.getSteeringSystem().getDisplayName()
        );
        return RunResult.success(summary);
    }

    public TestResult test(CarSpec spec) {
        ViolationResult v = checker.check(spec);
        if (!v.isValid())
            return TestResult.fail(v.getReason());
        return TestResult.pass();
    }
}
```

**설계 근거:**
- `CompatibilityChecker`를 생성자로 주입받는다 — 테스트에서 교체 가능하고 Phase 5에서 `AssemblyFlow`가 동일 패턴으로 조립한다.
- `run()`에서 호환성 검사를 고장 엔진보다 먼저 수행한다 — 현재 `Assemble.java`의 검사 순서와 동일하게 유지.
- `RunResult.message`에 부품 요약 전체를 담는다 — UI(Phase 4)가 그대로 출력만 하면 된다.

---

## 테스트 설계

### CarAssemblyServiceTest.java

```java
// run() — 고장난 엔진
@Test void run_고장난엔진_실패() {
    RunResult r = service.run(spec(SEDAN, BROKEN, MANDO, BOSCH));
    assertFalse(r.isSuccess());
    assertTrue(r.getMessage().contains("엔진이 고장"));
}

// run() — 비호환 조합
@Test void run_비호환조합_실패() {
    RunResult r = service.run(spec(SEDAN, GM, CONTINENTAL, BOSCH));
    assertFalse(r.isSuccess());
}

// run() — 유효 조합, 부품 요약 포함
@Test void run_유효조합_성공_요약포함() {
    RunResult r = service.run(spec(SEDAN, GM, MANDO, BOSCH));
    assertTrue(r.isSuccess());
    assertTrue(r.getMessage().contains("Sedan"));
    assertTrue(r.getMessage().contains("GM"));
    assertTrue(r.getMessage().contains("자동차가 동작됩니다"));
}

// test() — PASS
@Test void test_유효조합_PASS() {
    TestResult r = service.test(spec(TRUCK, GM, CONTINENTAL, MOBIS));
    assertTrue(r.isPassed());
    assertNull(r.getReason());
}

// test() — FAIL
@Test void test_비호환조합_FAIL() {
    TestResult r = service.test(spec(SUV, TOYOTA, MANDO, MOBIS));
    assertFalse(r.isPassed());
    assertNotNull(r.getReason());
}

// 호환성 검사가 고장 엔진보다 먼저 수행됨
@Test void run_비호환조합이면_고장엔진보다_호환규칙_먼저() {
    // SEDAN + CONTINENTAL 위반이면서 엔진도 BROKEN
    RunResult r = service.run(spec(SEDAN, BROKEN, CONTINENTAL, BOSCH));
    assertFalse(r.isSuccess());
    assertTrue(r.getMessage().contains("Continental"));
}
```

---

## 설계 결정 사항

| 결정 | 이유 |
|------|------|
| `RunResult`와 `TestResult`를 별개 클래스로 | run과 test의 성공 의미가 다름 — run은 "차가 움직임", test는 "조합이 유효함". 하나로 합치면 호출 측에서 의미가 모호해진다 |
| `run()`에서 호환성 → 고장 엔진 순으로 검사 | 기존 `Assemble.java` 동작 유지 |
| `Assemble.java` 미수정 | Phase 5에서 일괄 교체 |

---

## Phase 3 완료 기준

- `./gradlew test` 전체 GREEN (Phase 1·2 테스트 포함)
- `service/RunResult.java`, `service/TestResult.java`, `service/CarAssemblyService.java` 추가
- `src/test/java/service/CarAssemblyServiceTest.java` 추가
- `Assemble.java` 변경 없음
