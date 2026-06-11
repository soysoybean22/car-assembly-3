package service;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CompatibilityCheckerTest {

    private CompatibilityChecker checker;

    @BeforeEach
    void setUp() {
        checker = new CompatibilityChecker();
    }

    // FAIL 케이스 — 5가지 규칙 각각
    @Test
    void Sedan_Continental_불가() {
        ViolationResult r = checker.check(spec(CarType.SEDAN, Engine.GM, BrakeSystem.CONTINENTAL, SteeringSystem.BOSCH));
        assertFalse(r.isValid());
        assertEquals("Sedan에는 Continental 제동장치 사용 불가", r.getReason());
    }

    @Test
    void SUV_TOYOTA_불가() {
        ViolationResult r = checker.check(spec(CarType.SUV, Engine.TOYOTA, BrakeSystem.MANDO, SteeringSystem.MOBIS));
        assertFalse(r.isValid());
        assertEquals("SUV에는 TOYOTA 엔진 사용 불가", r.getReason());
    }

    @Test
    void Truck_WIA_불가() {
        ViolationResult r = checker.check(spec(CarType.TRUCK, Engine.WIA, BrakeSystem.CONTINENTAL, SteeringSystem.MOBIS));
        assertFalse(r.isValid());
        assertEquals("Truck에는 WIA 엔진 사용 불가", r.getReason());
    }

    @Test
    void Truck_MANDO_불가() {
        ViolationResult r = checker.check(spec(CarType.TRUCK, Engine.GM, BrakeSystem.MANDO, SteeringSystem.MOBIS));
        assertFalse(r.isValid());
        assertEquals("Truck에는 Mando 제동장치 사용 불가", r.getReason());
    }

    @Test
    void BOSCH제동_MOBIS조향_불가() {
        ViolationResult r = checker.check(spec(CarType.SEDAN, Engine.GM, BrakeSystem.BOSCH, SteeringSystem.MOBIS));
        assertFalse(r.isValid());
        assertEquals("Bosch 제동장치에는 Bosch 조향장치 이외 사용 불가", r.getReason());
    }

    // PASS 케이스 — 유효 조합 3가지 이상
    @Test
    void Sedan_GM_MANDO_BOSCH_통과() {
        assertTrue(checker.check(spec(CarType.SEDAN, Engine.GM, BrakeSystem.MANDO, SteeringSystem.BOSCH)).isValid());
    }

    @Test
    void SUV_GM_BOSCH제동_BOSCH조향_통과() {
        assertTrue(checker.check(spec(CarType.SUV, Engine.GM, BrakeSystem.BOSCH, SteeringSystem.BOSCH)).isValid());
    }

    @Test
    void Truck_GM_CONTINENTAL_MOBIS_통과() {
        assertTrue(checker.check(spec(CarType.TRUCK, Engine.GM, BrakeSystem.CONTINENTAL, SteeringSystem.MOBIS)).isValid());
    }

    @Test
    void PASS_결과_reason_null() {
        ViolationResult r = checker.check(spec(CarType.SEDAN, Engine.GM, BrakeSystem.MANDO, SteeringSystem.BOSCH));
        assertTrue(r.isValid());
        assertNull(r.getReason());
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
