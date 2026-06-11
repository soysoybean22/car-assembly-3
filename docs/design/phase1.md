# Phase 1 설계 — enum 도입 및 도메인 모델 정의

## 목표 요약

`Assemble.java`의 정수 상수(`SEDAN=1`, `stack[0]` 등)를 타입 안전한 `enum`과 Value Object로 교체한다.
이 Phase에서는 `Assemble.java`를 수정하지 않는다. 새 파일만 추가하고, 테스트로 모델이 올바르게 동작함을 검증한다.

---

## 파일 목록

```
src/
├── main/java/
│   ├── Assemble.java              ← 변경 없음
│   └── model/
│       ├── CarType.java           ← 신규
│       ├── Engine.java            ← 신규
│       ├── BrakeSystem.java       ← 신규
│       ├── SteeringSystem.java    ← 신규
│       └── CarSpec.java           ← 신규
└── test/java/
    └── model/
        ├── CarTypeTest.java       ← 신규
        ├── EngineTest.java        ← 신규
        ├── BrakeSystemTest.java   ← 신규
        ├── SteeringSystemTest.java← 신규
        └── CarSpecTest.java       ← 신규
```

---

## 각 클래스 설계

### CarType.java

```java
package model;

public enum CarType {
    SEDAN(1, "Sedan"),
    SUV  (2, "SUV"),
    TRUCK(3, "Truck");

    private final int menuNumber;
    private final String displayName;

    CarType(int menuNumber, String displayName) {
        this.menuNumber  = menuNumber;
        this.displayName = displayName;
    }

    public int getMenuNumber()    { return menuNumber; }
    public String getDisplayName(){ return displayName; }

    public static CarType fromMenuNumber(int n) {
        for (CarType t : values()) {
            if (t.menuNumber == n) return t;
        }
        throw new IllegalArgumentException("유효하지 않은 차량 타입 번호: " + n);
    }
}
```

**설계 근거:**
- `menuNumber`는 현재 `Assemble.java`의 메뉴 입력값(1/2/3)과 1:1 대응. 추후 `InputHandler`가 `fromMenuNumber()`를 호출하여 enum으로 변환한다.
- `displayName`은 콘솔 출력에 사용. `MenuPrinter`(Phase 4)가 `getDisplayName()`으로 읽는다.

---

### Engine.java

```java
package model;

public enum Engine {
    GM    (1, "GM"),
    TOYOTA(2, "TOYOTA"),
    WIA   (3, "WIA"),
    BROKEN(4, "고장난 엔진");

    private final int menuNumber;
    private final String displayName;

    Engine(int menuNumber, String displayName) { ... }

    public int getMenuNumber()    { return menuNumber; }
    public String getDisplayName(){ return displayName; }
    public boolean isBroken()     { return this == BROKEN; }

    public static Engine fromMenuNumber(int n) { ... }
}
```

**설계 근거:**
- `isBroken()`을 별도 메서드로 둔다. `stack[Engine_Q] == 4` 같은 매직 넘버 비교를 없애기 위함.

---

### BrakeSystem.java

```java
package model;

public enum BrakeSystem {
    MANDO      (1, "Mando"),
    CONTINENTAL(2, "Continental"),
    BOSCH      (3, "Bosch");

    private final int menuNumber;
    private final String displayName;

    BrakeSystem(int menuNumber, String displayName) { ... }

    public int getMenuNumber()    { return menuNumber; }
    public String getDisplayName(){ return displayName; }

    public static BrakeSystem fromMenuNumber(int n) { ... }
}
```

---

### SteeringSystem.java

```java
package model;

public enum SteeringSystem {
    BOSCH(1, "Bosch"),
    MOBIS(2, "Mobis");

    private final int menuNumber;
    private final String displayName;

    SteeringSystem(int menuNumber, String displayName) { ... }

    public int getMenuNumber()    { return menuNumber; }
    public String getDisplayName(){ return displayName; }

    public static SteeringSystem fromMenuNumber(int n) { ... }
}
```

---

### CarSpec.java

```java
package model;

public class CarSpec {
    private CarType       carType;
    private Engine        engine;
    private BrakeSystem   brakeSystem;
    private SteeringSystem steeringSystem;

    public CarSpec() {}

    // getter / setter 4쌍

    public boolean isComplete() {
        return carType != null
            && engine != null
            && brakeSystem != null
            && steeringSystem != null;
    }
}
```

**설계 근거:**
- 불변 객체(생성자 주입)가 아닌 단계별 세터 방식을 채택한다. 사용자가 한 단계씩 선택을 완성하는 흐름이므로, 선택 중간 상태를 자연스럽게 표현할 수 있다.
- `isComplete()`는 `AssemblyFlow`(Phase 5)에서 최종 단계 진입 조건 확인에 사용한다.
- `static int[] stack`을 이 클래스가 대체한다.

---

## 설계 결정 사항

| 결정 | 이유 |
|------|------|
| `fromMenuNumber()` 예외를 checked가 아닌 `IllegalArgumentException`으로 | 유효 범위 검사는 `InputHandler`(Phase 4)가 먼저 담당하므로 여기서는 프로그래밍 오류로 간주 |
| `BrakeSystem.BOSCH`와 `SteeringSystem.BOSCH`를 별개 enum으로 | 제동장치와 조향장치는 서로 다른 타입 — 같은 이름이라도 혼용되면 컴파일 오류로 잡힌다 |
| `CarSpec`을 Record가 아닌 일반 클래스로 | Java 16 미만 호환성 유지 및 단계별 부분 세팅 지원 |

---

## 테스트 설계

### CarTypeTest.java

```java
@Test void menuNumber_매핑() {
    assertEquals(1, CarType.SEDAN.getMenuNumber());
    assertEquals(2, CarType.SUV.getMenuNumber());
    assertEquals(3, CarType.TRUCK.getMenuNumber());
}
@Test void displayName_반환() {
    assertEquals("Sedan", CarType.SEDAN.getDisplayName());
}
@Test void fromMenuNumber_정상() {
    assertEquals(CarType.SUV, CarType.fromMenuNumber(2));
}
@Test void fromMenuNumber_범위초과_예외() {
    assertThrows(IllegalArgumentException.class, () -> CarType.fromMenuNumber(99));
}
```

### EngineTest.java

```java
@Test void isBroken_고장엔진() {
    assertTrue(Engine.BROKEN.isBroken());
}
@Test void isBroken_정상엔진() {
    assertFalse(Engine.GM.isBroken());
    assertFalse(Engine.TOYOTA.isBroken());
    assertFalse(Engine.WIA.isBroken());
}
@Test void fromMenuNumber_정상() {
    assertEquals(Engine.WIA, Engine.fromMenuNumber(3));
}
```

### BrakeSystemTest.java / SteeringSystemTest.java

`CarTypeTest`와 동일한 패턴 — `menuNumber` 매핑, `displayName`, `fromMenuNumber` 정상/예외.

### CarSpecTest.java

```java
@Test void 초기상태_isComplete_false() {
    assertFalse(new CarSpec().isComplete());
}
@Test void 전체_설정_후_isComplete_true() {
    CarSpec spec = new CarSpec();
    spec.setCarType(CarType.SEDAN);
    spec.setEngine(Engine.GM);
    spec.setBrakeSystem(BrakeSystem.MANDO);
    spec.setSteeringSystem(SteeringSystem.BOSCH);
    assertTrue(spec.isComplete());
}
@Test void 부분_설정_isComplete_false() {
    CarSpec spec = new CarSpec();
    spec.setCarType(CarType.TRUCK);
    assertFalse(spec.isComplete());
}
@Test void getter_setter_일치() {
    CarSpec spec = new CarSpec();
    spec.setEngine(Engine.TOYOTA);
    assertEquals(Engine.TOYOTA, spec.getEngine());
}
```

---

## Phase 1 완료 기준

- `./gradlew test` 실행 시 위 테스트 전부 GREEN
- `Assemble.java` 변경 없음 (기존 동작 유지)
- `model/` 패키지 5개 파일 추가
- `src/test/java/model/` 5개 테스트 파일 추가
