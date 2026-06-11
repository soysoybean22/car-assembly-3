package service;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CarAssemblyServiceTest {

    private CarAssemblyService service;

    @BeforeEach
    void setUp() {
        service = new CarAssemblyService(new CompatibilityChecker());
    }

    // run() — 고장난 엔진
    @Test
    void run_고장난엔진_실패() {
        RunResult r = service.run(spec(CarType.SEDAN, Engine.BROKEN, BrakeSystem.MANDO, SteeringSystem.BOSCH));
        assertFalse(r.isSuccess());
        assertTrue(r.getMessage().contains("엔진이 고장"));
    }

    // run() — 비호환 조합
    @Test
    void run_비호환조합_실패() {
        RunResult r = service.run(spec(CarType.SEDAN, Engine.GM, BrakeSystem.CONTINENTAL, SteeringSystem.BOSCH));
        assertFalse(r.isSuccess());
    }

    // run() — 유효 조합, 부품 요약 포함
    @Test
    void run_유효조합_성공_요약포함() {
        RunResult r = service.run(spec(CarType.SEDAN, Engine.GM, BrakeSystem.MANDO, SteeringSystem.BOSCH));
        assertTrue(r.isSuccess());
        assertTrue(r.getMessage().contains("Sedan"));
        assertTrue(r.getMessage().contains("GM"));
        assertTrue(r.getMessage().contains("자동차가 동작됩니다"));
    }

    // run() — 유효 조합, 4가지 부품 모두 요약에 포함
    @Test
    void run_유효조합_요약에_모든부품_포함() {
        RunResult r = service.run(spec(CarType.TRUCK, Engine.GM, BrakeSystem.CONTINENTAL, SteeringSystem.MOBIS));
        assertTrue(r.isSuccess());
        assertTrue(r.getMessage().contains("Truck"));
        assertTrue(r.getMessage().contains("GM"));
        assertTrue(r.getMessage().contains("Continental"));
        assertTrue(r.getMessage().contains("Mobis"));
    }

    // test() — PASS
    @Test
    void test_유효조합_PASS() {
        TestResult r = service.test(spec(CarType.TRUCK, Engine.GM, BrakeSystem.CONTINENTAL, SteeringSystem.MOBIS));
        assertTrue(r.isPassed());
        assertNull(r.getReason());
    }

    // test() — FAIL
    @Test
    void test_비호환조합_FAIL_원인포함() {
        TestResult r = service.test(spec(CarType.SUV, Engine.TOYOTA, BrakeSystem.MANDO, SteeringSystem.MOBIS));
        assertFalse(r.isPassed());
        assertNotNull(r.getReason());
    }

    // 호환성 규칙 위반이 고장 엔진보다 먼저 검사됨
    @Test
    void run_호환규칙위반이_고장엔진보다_우선() {
        RunResult r = service.run(spec(CarType.SEDAN, Engine.BROKEN, BrakeSystem.CONTINENTAL, SteeringSystem.BOSCH));
        assertFalse(r.isSuccess());
        assertTrue(r.getMessage().contains("Continental"));
    }

    private CarSpec spec(CarType ct, Engine e, BrakeSystem b, SteeringSystem s) {
        CarSpec spec = new CarSpec();
        spec.setCarType(ct);
        spec.setEngine(e);
        spec.setBrakeSystem(b);
        spec.setSteeringSystem(s);
        return spec;
    }
}
