package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CarSpecTest {

    @Test
    void 초기상태_isComplete_false() {
        assertFalse(new CarSpec().isComplete());
    }

    @Test
    void 전체_설정_후_isComplete_true() {
        CarSpec spec = new CarSpec();
        spec.setCarType(CarType.SEDAN);
        spec.setEngine(Engine.GM);
        spec.setBrakeSystem(BrakeSystem.MANDO);
        spec.setSteeringSystem(SteeringSystem.BOSCH);
        assertTrue(spec.isComplete());
    }

    @Test
    void 부분_설정_isComplete_false() {
        CarSpec spec = new CarSpec();
        spec.setCarType(CarType.TRUCK);
        assertFalse(spec.isComplete());
    }

    @Test
    void getter_setter_일치() {
        CarSpec spec = new CarSpec();
        spec.setCarType(CarType.SUV);
        spec.setEngine(Engine.TOYOTA);
        spec.setBrakeSystem(BrakeSystem.CONTINENTAL);
        spec.setSteeringSystem(SteeringSystem.MOBIS);

        assertEquals(CarType.SUV,              spec.getCarType());
        assertEquals(Engine.TOYOTA,            spec.getEngine());
        assertEquals(BrakeSystem.CONTINENTAL,  spec.getBrakeSystem());
        assertEquals(SteeringSystem.MOBIS,     spec.getSteeringSystem());
    }

    @Test
    void reset_후_isComplete_false() {
        CarSpec spec = new CarSpec();
        spec.setCarType(CarType.SEDAN);
        spec.setEngine(Engine.GM);
        spec.setBrakeSystem(BrakeSystem.MANDO);
        spec.setSteeringSystem(SteeringSystem.BOSCH);

        spec.reset();

        assertFalse(spec.isComplete());
        assertNull(spec.getCarType());
        assertNull(spec.getEngine());
        assertNull(spec.getBrakeSystem());
        assertNull(spec.getSteeringSystem());
    }
}
