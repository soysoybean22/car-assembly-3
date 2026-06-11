# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 기술 스택

- **언어:** Java (순수 Java, 프레임워크 없음)
- **빌드 시스템:** Gradle 9.3.0 (Kotlin DSL, `build.gradle.kts`)
- **테스트 프레임워크:** JUnit 5 (Jupiter) — `junit-bom:6.0.0`

## 빌드 및 실행 명령어

```bash
# 컴파일
./gradlew compileJava          # Unix
gradlew.bat compileJava        # Windows

# 빌드 (컴파일 + 패키징)
./gradlew build

# 실행 (컴파일 후 직접 실행)
java -cp build/classes/java/main Assemble

# 테스트 실행
./gradlew test

# 빌드 결과물 초기화
./gradlew clean
```

## 프로젝트 구조

```
src/main/java/Assemble.java   # 유일한 소스 파일 (main 클래스)
src/test/java/                # 테스트 클래스 위치 (현재 없음)
build.gradle.kts              # 의존성 및 빌드 설정
```

## 아키텍처

`Assemble.java` 단일 파일로 구성된 CLI 자동차 조립 시뮬레이터:

- **진행 흐름:** `step` 변수로 현재 단계를 추적하는 순차적 상태 머신
  - `CarType_Q(0)` → `Engine_Q(1)` → `BrakeSystem_Q(2)` → `SteeringSystem_Q(3)` → `Run_Test(4)`
- **선택 저장:** `static int[] stack[5]` — 각 인덱스가 단계 번호에 대응
- **뒤로가기:** 입력 `0`으로 이전 단계로 복귀 (`step--`)
- **부품 호환성 규칙** (`isValidCheck()`):
  - Sedan + Continental 제동장치 → 불가
  - SUV + TOYOTA 엔진 → 불가
  - Truck + WIA 엔진 → 불가
  - Truck + MANDO 제동장치 → 불가
  - BOSCH 제동장치는 반드시 BOSCH 조향장치와 조합

## 테스트 방법

현재 JUnit 테스트 파일이 없으며, `src/test/java/` 아래에 테스트 클래스를 추가하면 `./gradlew test`로 실행된다.

`testProducedCar()` 메서드는 프로그램 내부 테스트 기능으로, 선택한 부품 조합의 유효성을 검증하고 `PASS` / `FAIL` 결과를 출력한다.
