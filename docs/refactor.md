# 리팩토링 계획서

## 현재 코드의 문제점

### 1. 단일 파일에 모든 책임 집중 (SRP 위반)
`Assemble.java` 하나가 UI 출력, 입력 처리, 상태 관리, 유효성 검사, 실행 로직을 모두 담당한다.
변경 이유가 하나여야 할 클래스가 여러 이유로 수정되어야 한다.

### 2. 정수 상수 기반 상태 관리
```java
private static int[] stack = new int[5];
// stack[0] = 차량타입, stack[1] = 엔진, ...
```
배열 인덱스와 값이 모두 정수라 의미를 파악하기 어렵고, 타입 안전성이 없다.
`stack[Engine_Q] == 4`가 "고장난 엔진"을 뜻한다는 것을 코드만으로 알 수 없다.

### 3. 호환성 규칙 중복 구현
`isValidCheck()`와 `testProducedCar()` 두 메서드에 동일한 5가지 규칙이 반복된다.
규칙이 추가되거나 변경되면 두 곳을 모두 수정해야 한다.

### 4. 비즈니스 로직과 UI 코드 혼재
`selectCarType()` 같은 메서드가 상태 저장과 콘솔 출력을 동시에 수행한다.
`runProducedCar()`는 유효성 검사, 상태 읽기, 출력을 한 메서드에서 처리한다.

### 5. `main()`의 거대한 while 루프
단계 전환, 입력 파싱, 뒤로가기, 분기 처리가 모두 `main()`의 단일 루프 안에 있다.
새 단계 추가 시 `switch`문 여러 곳을 동시에 수정해야 한다.

### 6. 테스트 불가능한 구조
모든 메서드가 `static`이고 콘솔 I/O에 직접 의존하여 단위 테스트를 작성하기 어렵다.
`Scanner`가 `main()`에 고정되어 있어 입력을 주입할 수 없다.

---

## 리팩토링 목표

- 각 클래스가 단일 책임을 갖도록 분리
- 정수 상수를 `enum`으로 교체하여 타입 안전성 확보
- 중복 규칙 제거 및 단일 출처(Single Source of Truth) 확보
- 비즈니스 로직을 순수 Java 메서드로 분리하여 JUnit 테스트 가능하게 만들기

---

## 제안 클래스 구조

```
src/main/java/
├── Assemble.java              # main() 진입점만 유지
├── model/
│   ├── CarType.java           # enum: SEDAN, SUV, TRUCK
│   ├── Engine.java            # enum: GM, TOYOTA, WIA, BROKEN
│   ├── BrakeSystem.java       # enum: MANDO, CONTINENTAL, BOSCH
│   ├── SteeringSystem.java    # enum: BOSCH, MOBIS
│   └── CarSpec.java           # 선택된 4가지 부품을 담는 VO
├── service/
│   ├── CompatibilityChecker.java  # 호환성 규칙 단일 관리
│   └── CarAssemblyService.java    # run/test 비즈니스 로직
└── ui/
    ├── MenuPrinter.java       # 각 단계별 메뉴 출력
    ├── InputHandler.java      # Scanner 래핑, 입력 파싱/검증
    └── AssemblyFlow.java      # 단계 전환 흐름 제어 (현재 main의 while 루프)
```

---

## 핵심 리팩토링 항목

### 항목 1: 정수 상수 → enum 전환

**현재:**
```java
private static final int SEDAN = 1, SUV = 2, TRUCK = 3;
private static int[] stack = new int[5];
stack[CarType_Q] = answer;
```

**개선:**
```java
public enum CarType {
    SEDAN("Sedan"), SUV("SUV"), TRUCK("Truck");
    private final String displayName;
    CarType(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}

// CarSpec.java
public class CarSpec {
    private CarType carType;
    private Engine engine;
    private BrakeSystem brakeSystem;
    private SteeringSystem steeringSystem;
    // getter/setter
}
```

### 항목 2: 호환성 규칙 단일화

**현재:** `isValidCheck()`와 `testProducedCar()`에 동일 규칙 중복

**개선:**
```java
// CompatibilityChecker.java
public class CompatibilityChecker {

    public record ViolationResult(boolean valid, String reason) {}

    public ViolationResult check(CarSpec spec) {
        if (spec.getCarType() == SEDAN && spec.getBrakeSystem() == CONTINENTAL)
            return new ViolationResult(false, "Sedan에는 Continental 제동장치 사용 불가");
        if (spec.getCarType() == SUV && spec.getEngine() == TOYOTA)
            return new ViolationResult(false, "SUV에는 TOYOTA 엔진 사용 불가");
        // ... 나머지 규칙
        return new ViolationResult(true, null);
    }
}
```
`runProducedCar()`와 `testProducedCar()` 모두 이 메서드 하나를 호출한다.

### 항목 3: UI 출력과 비즈니스 로직 분리

**현재:** `selectCarType()`이 상태 저장 + 콘솔 출력을 동시 수행

**개선:**
- `CarAssemblyService.apply(CarSpec, CarType)` → 상태만 변경, 반환값 없음
- `MenuPrinter.printSelectionConfirm(CarType)` → 출력만 담당
- 호출 측(`AssemblyFlow`)에서 둘을 순서대로 호출

### 항목 4: 입력 처리 분리

**현재:** `main()`의 루프 내에서 직접 `sc.nextLine()` 및 파싱

**개선:**
```java
// InputHandler.java
public class InputHandler {
    private final Scanner scanner;
    public InputHandler(Scanner scanner) { this.scanner = scanner; }

    public int readInt(String prompt) { ... }   // 파싱 및 NumberFormatException 처리
    public boolean isExit(String input) { ... }
}
```
`Scanner`를 생성자로 주입하므로 테스트 시 `new Scanner("1\n2\n3\n")`으로 대체 가능.

### 항목 5: 단계 흐름을 별도 클래스로 분리

**현재:** `main()`의 while + switch가 흐름 전체를 담당

**개선:**
```java
// AssemblyFlow.java
public class AssemblyFlow {
    private final InputHandler input;
    private final MenuPrinter printer;
    private final CarAssemblyService service;
    private final CompatibilityChecker checker;

    public void run() {
        // step 관리, 뒤로가기, 단계 전환 로직
    }
}
```
`main()`은 의존성을 생성하고 `new AssemblyFlow(...).run()`만 호출.

---

## 테스트 작성 계획

리팩토링 후 `src/test/java/`에 아래 테스트 추가:

| 테스트 클래스 | 검증 내용 |
|---------------|-----------|
| `CompatibilityCheckerTest` | 5가지 규칙 각각 FAIL 케이스 + 유효 조합 PASS 케이스 |
| `CarAssemblyServiceTest` | 고장 엔진 RUN 시 동작하지 않음 확인 |
| `InputHandlerTest` | 숫자 외 입력, 범위 외 입력 처리 확인 |

---

## 우선순위

| 순위 | 항목 | 이유 |
|------|------|------|
| 1 | 호환성 규칙 단일화 (항목 2) | 버그 위험이 가장 높은 중복 코드 제거 |
| 2 | enum 전환 (항목 1) | 이후 모든 리팩토링의 기반 |
| 3 | UI/비즈니스 로직 분리 (항목 3, 4) | 테스트 가능성 확보 |
| 4 | 흐름 분리 (항목 5) | 가독성 및 확장성 개선 |
| 5 | 테스트 작성 | 리팩토링 안정성 검증 |
