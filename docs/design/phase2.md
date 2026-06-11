# Phase 2 설계 — 호환성 규칙 단일화

## 진행 방식

```
1. CompatibilityCheckerTest 작성 (service 클래스 없음 → RED)
2. ./gradlew test → 컴파일 에러 실패 확인
3. CompatibilityChecker 구현
4. ./gradlew test → 전체 GREEN 확인
```

## 목표 요약

`Assemble.java`의 `isValidCheck()`와 `testProducedCar()`에 중복된 5가지 호환성 규칙을 `CompatibilityChecker` 하나로 통합한다.
이 Phase에서 `Assemble.java`는 수정하지 않는다. 다음 Phase에서 `CompatibilityChecker`를 호출하도록 교체한다.

---

## 파일 목록

```
src/
├── main/java/
│   ├── Assemble.java                        ← 변경 없음
│   ├── model/                               ← 변경 없음
│   └── service/
│       ├── CompatibilityChecker.java        ← 신규
│       └── ViolationResult.java             ← 신규
└── test/java/
    ├── model/                               ← 변경 없음
    └── service/
        └── CompatibilityCheckerTest.java    ← 신규
```

---

## 각 클래스 설계

### ViolationResult.java

```java
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

    public boolean isValid()   { return valid; }
    public String getReason()  { return reason; }
}
```

**설계 근거:**
- 생성자를 `private`으로 막고 `pass()` / `fail(String)` 팩토리 메서드만 노출한다. 호출 측에서 `new ViolationResult(true, null)` 같은 실수가 생기지 않는다.
- `reason`은 FAIL일 때만 의미가 있다. PASS일 경우 `null`이며, 호출 측은 `isValid()` 확인 후에만 `getReason()`을 사용한다.

---

### CompatibilityChecker.java

```java
package service;

import model.*;

public class CompatibilityChecker {

    public ViolationResult check(CarSpec spec) {
        if (spec.getCarType() == CarType.SEDAN
                && spec.getBrakeSystem() == BrakeSystem.CONTINENTAL)
            return ViolationResult.fail("Sedan에는 Continental 제동장치 사용 불가");

        if (spec.getCarType() == CarType.SUV
                && spec.getEngine() == Engine.TOYOTA)
            return ViolationResult.fail("SUV에는 TOYOTA 엔진 사용 불가");

        if (spec.getCarType() == CarType.TRUCK
                && spec.getEngine() == Engine.WIA)
            return ViolationResult.fail("Truck에는 WIA 엔진 사용 불가");

        if (spec.getCarType() == CarType.TRUCK
                && spec.getBrakeSystem() == BrakeSystem.MANDO)
            return ViolationResult.fail("Truck에는 Mando 제동장치 사용 불가");

        if (spec.getBrakeSystem() == BrakeSystem.BOSCH
                && spec.getSteeringSystem() != SteeringSystem.BOSCH)
            return ViolationResult.fail("Bosch 제동장치에는 Bosch 조향장치 이외 사용 불가");

        return ViolationResult.pass();
    }
}
```

**설계 근거:**
- 인스턴스 메서드로 설계한다. 상태가 없어 static으로도 가능하지만, Phase 3에서 `CarAssemblyService`가 생성자 주입으로 받을 수 있도록 인스턴스 방식을 선택한다.
- 규칙 순서는 현재 `Assemble.java`의 `isValidCheck()` 검사 순서와 동일하게 유지한다.

---

## 테스트 설계

### CompatibilityCheckerTest.java

```java
// FAIL 케이스 — 5가지 규칙 각각
@Test void Sedan_Continental_불가() {
    CarSpec spec = spec(SEDAN, GM, CONTINENTAL, BOSCH);
    ViolationResult r = checker.check(spec);
    assertFalse(r.isValid());
    assertEquals("Sedan에는 Continental 제동장치 사용 불가", r.getReason());
}
@Test void SUV_TOYOTA_불가() { ... }
@Test void Truck_WIA_불가()   { ... }
@Test void Truck_MANDO_불가() { ... }
@Test void BOSCH제동_MOBIS조향_불가() { ... }

// PASS 케이스 — 유효 조합 3가지 이상
@Test void Sedan_GM_MANDO_BOSCH_통과()         { assertTrue(checker.check(...).isValid()); }
@Test void SUV_GM_BOSCH_BOSCH_통과()           { assertTrue(checker.check(...).isValid()); }
@Test void Truck_GM_CONTINENTAL_MOBIS_통과()   { assertTrue(checker.check(...).isValid()); }

// 헬퍼
private CarSpec spec(CarType ct, Engine e, BrakeSystem b, SteeringSystem s) {
    CarSpec spec = new CarSpec();
    spec.setCarType(ct); spec.setEngine(e);
    spec.setBrakeSystem(b); spec.setSteeringSystem(s);
    return spec;
}
```

---

## 설계 결정 사항

| 결정 | 이유 |
|------|------|
| `ViolationResult`를 별도 클래스로 분리 | `boolean` + `String` 쌍을 메서드 반환값으로 깔끔하게 표현. Phase 3의 `CarAssemblyService`도 동일 타입을 재사용 |
| `Assemble.java` 미수정 | 이 Phase는 규칙 통합만 목표. 기존 동작 보존 후 Phase 3에서 교체 |
| 인스턴스 메서드 (static 아님) | Phase 3 `CarAssemblyService` 생성자 주입 대비 |

---

## Phase 2 완료 기준

- `./gradlew test` 전체 GREEN (Phase 1 테스트 포함)
- `service/CompatibilityChecker.java`, `service/ViolationResult.java` 추가
- `src/test/java/service/CompatibilityCheckerTest.java` 추가 (FAIL 5건 + PASS 3건 이상)
- `Assemble.java` 변경 없음
