package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BrakeSystemTest {

    @Test
    void menuNumber_매핑() {
        assertEquals(1, BrakeSystem.MANDO.getMenuNumber());
        assertEquals(2, BrakeSystem.CONTINENTAL.getMenuNumber());
        assertEquals(3, BrakeSystem.BOSCH.getMenuNumber());
    }

    @Test
    void displayName_반환() {
        assertEquals("Mando",       BrakeSystem.MANDO.getDisplayName());
        assertEquals("Continental", BrakeSystem.CONTINENTAL.getDisplayName());
        assertEquals("Bosch",       BrakeSystem.BOSCH.getDisplayName());
    }

    @Test
    void fromMenuNumber_정상() {
        assertEquals(BrakeSystem.MANDO,       BrakeSystem.fromMenuNumber(1));
        assertEquals(BrakeSystem.CONTINENTAL, BrakeSystem.fromMenuNumber(2));
        assertEquals(BrakeSystem.BOSCH,       BrakeSystem.fromMenuNumber(3));
    }

    @Test
    void fromMenuNumber_범위초과_예외() {
        assertThrows(IllegalArgumentException.class, () -> BrakeSystem.fromMenuNumber(0));
        assertThrows(IllegalArgumentException.class, () -> BrakeSystem.fromMenuNumber(99));
    }
}
