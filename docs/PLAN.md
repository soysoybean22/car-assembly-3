# PLAN.md

## 진행 방식 — TDD

모든 Phase는 아래 순서를 반드시 따른다.

```
1. 테스트 작성   → 구현이 없으므로 컴파일·테스트 실패 (RED)
2. RED 확인      → ./gradlew test 실패 확인
3. 구현          → 테스트를 통과시키는 최소한의 코드 작성
4. GREEN 확인    → ./gradlew test 전체 통과 확인
5. 커밋          → RED 커밋 + GREEN 커밋 분리
```

각 Phase는 이전 Phase의 GREEN이 확인된 상태에서만 시작한다.

---

## Phase 1 — enum 도입 및 도메인 모델 정의

**목표:** 정수 상수 기반 상태를 타입 안전한 enum과 VO로 교체한다.

### 작업 범위
- `model/CarType.java` — `SEDAN`, `SUV`, `TRUCK` (displayName 포함)
- `model/Engine.java` — `GM`, `TOYOTA`, `WIA`, `BROKEN`
- `model/BrakeSystem.java` — `MANDO`, `CONTINENTAL`, `BOSCH`
- `model/SteeringSystem.java` — `BOSCH`, `MOBIS`
- `model/CarSpec.java` — 4가지 부품을 담는 Value Object (getter/setter)

### 완료 조건 (테스트)
```
CarTypeTest       : 각 enum 값의 displayName이 올바른지 검증
CarSpecTest       : CarSpec 생성 및 getter/setter 동작 검증
```
- `./gradlew test` 전체 통과

---

## Phase 2 — 호환성 규칙 단일화

**목표:** 중복된 5가지 규칙을 `CompatibilityChecker` 한 곳에서 관리한다.

### 작업 범위
- `service/CompatibilityChecker.java` 신규 작성
  - `ViolationResult check(CarSpec spec)` — 유효 여부 + 실패 원인 반환
- 기존 `Assemble.java`의 `isValidCheck()`, `testProducedCar()` 내 규칙 코드를 `CompatibilityChecker`로 이전 (원본 메서드는 아직 유지)

### 완료 조건 (테스트)
```
CompatibilityCheckerTest:
  - Sedan + CONTINENTAL → FAIL, 메시지 "Sedan에는 Continental 제동장치 사용 불가"
  - SUV + TOYOTA → FAIL
  - Truck + WIA → FAIL
  - Truck + MANDO → FAIL
  - BOSCH 제동장치 + MOBIS 조향장치 → FAIL
  - 유효한 조합 3가지 이상 → PASS
```
- `./gradlew test` 전체 통과

---

## Phase 3 — CarAssemblyService 분리

**목표:** 비즈니스 로직(run/test)을 콘솔 출력 없이 순수 Java 메서드로 분리한다.

### 작업 범위
- `service/CarAssemblyService.java` 신규 작성
  - `RunResult run(CarSpec spec)` — 동작 가능 여부, 부품 요약을 담은 결과 객체 반환
  - `TestResult test(CarSpec spec)` — PASS/FAIL 및 실패 원인 반환
  - 내부적으로 `CompatibilityChecker` 사용
- 기존 `Assemble.java`의 `runProducedCar()`, `testProducedCar()` 로직을 위 서비스로 이전

### 완료 조건 (테스트)
```
CarAssemblyServiceTest:
  - 고장난 엔진(BROKEN) → RunResult.success == false, "엔진이 고장" 메시지
  - 비호환 조합 → RunResult.success == false
  - 유효 조합 → RunResult.success == true, 부품 요약 포함
  - test() PASS 케이스 / FAIL 케이스 각 1건 이상
```
- `./gradlew test` 전체 통과

---

## Phase 4 — UI 레이어 분리

**목표:** 콘솔 출력 책임을 `MenuPrinter`로, 입력 처리를 `InputHandler`로 분리한다.

### 작업 범위
- `ui/MenuPrinter.java` 신규 작성
  - 각 단계별 메뉴 출력 메서드 (`printCarTypeMenu()` 등)
  - `RunResult` / `TestResult`를 받아 결과 출력하는 메서드
- `ui/InputHandler.java` 신규 작성
  - 생성자에서 `Scanner` 주입
  - `readInt(String prompt)` — 파싱 실패 시 재입력 유도
  - `isExit(String input)` — "exit" 여부 판별

### 완료 조건 (테스트)
```
InputHandlerTest:
  - 숫자 외 문자 입력 시 재입력 요청 (Scanner를 "abc\n1\n"으로 주입)
  - "exit" 입력 → isExit() == true
  - 정상 숫자 입력 → 파싱된 int 반환
```
- `./gradlew test` 전체 통과

---

## Phase 5 — AssemblyFlow 분리 및 main() 정리

**목표:** `main()`의 while 루프 흐름을 `AssemblyFlow`로 이전하고, `main()`은 의존성 조립만 담당하게 한다.

### 작업 범위
- `ui/AssemblyFlow.java` 신규 작성
  - 생성자: `InputHandler`, `MenuPrinter`, `CarAssemblyService` 주입
  - `run()`: 단계 전환, 뒤로가기, 종료 흐름 관리
- `Assemble.java` — `main()`에서 의존성 생성 후 `new AssemblyFlow(...).run()` 호출만 남기기
- 기존 `Assemble.java`의 중복·이전된 메서드 전부 제거

### 완료 조건 (테스트)
```
AssemblyFlowTest:
  - 전체 흐름 시뮬레이션: "1\n1\n1\n1\n1\n" 입력 → RUN 성공 결과 확인
  - 뒤로가기 흐름: "1\n0\n2\n1\n1\n1\n" → Engine 단계로 복귀 후 재선택 확인
  - "exit" 입력 → 루프 종료 확인
```
- `./gradlew test` 전체 통과
- `Assemble.java` 내 비즈니스 로직 메서드 잔존 없음

---

## 체크리스트

| Phase | 목표 | 테스트 | 완료 |
|-------|------|--------|------|
| 1 | enum + CarSpec 도입 | CarTypeTest, EngineTest, BrakeSystemTest, SteeringSystemTest, CarSpecTest | ☑ |
| 2 | 호환성 규칙 단일화 | CompatibilityCheckerTest | ☑ |
| 3 | 서비스 레이어 분리 | CarAssemblyServiceTest | ☐ |
| 4 | UI 레이어 분리 | InputHandlerTest | ☐ |
| 5 | 흐름 분리 + main() 정리 | AssemblyFlowTest | ☐ |
