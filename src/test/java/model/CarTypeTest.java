package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CarTypeTest {

    @Test
    void menuNumber_매핑() {
        assertEquals(1, CarType.SEDAN.getMenuNumber());
        assertEquals(2, CarType.SUV.getMenuNumber());
        assertEquals(3, CarType.TRUCK.getMenuNumber());
    }

    @Test
    void displayName_반환() {
        assertEquals("Sedan", CarType.SEDAN.getDisplayName());
        assertEquals("SUV",   CarType.SUV.getDisplayName());
        assertEquals("Truck", CarType.TRUCK.getDisplayName());
    }

    @Test
    void fromMenuNumber_정상() {
        assertEquals(CarType.SEDAN, CarType.fromMenuNumber(1));
        assertEquals(CarType.SUV,   CarType.fromMenuNumber(2));
        assertEquals(CarType.TRUCK, CarType.fromMenuNumber(3));
    }

    @Test
    void fromMenuNumber_범위초과_예외() {
        assertThrows(IllegalArgumentException.class, () -> CarType.fromMenuNumber(0));
        assertThrows(IllegalArgumentException.class, () -> CarType.fromMenuNumber(99));
    }
}
