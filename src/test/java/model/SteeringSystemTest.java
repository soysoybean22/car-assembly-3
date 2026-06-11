package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SteeringSystemTest {

    @Test
    void menuNumber_매핑() {
        assertEquals(1, SteeringSystem.BOSCH.getMenuNumber());
        assertEquals(2, SteeringSystem.MOBIS.getMenuNumber());
    }

    @Test
    void displayName_반환() {
        assertEquals("Bosch", SteeringSystem.BOSCH.getDisplayName());
        assertEquals("Mobis", SteeringSystem.MOBIS.getDisplayName());
    }

    @Test
    void fromMenuNumber_정상() {
        assertEquals(SteeringSystem.BOSCH, SteeringSystem.fromMenuNumber(1));
        assertEquals(SteeringSystem.MOBIS, SteeringSystem.fromMenuNumber(2));
    }

    @Test
    void fromMenuNumber_범위초과_예외() {
        assertThrows(IllegalArgumentException.class, () -> SteeringSystem.fromMenuNumber(0));
        assertThrows(IllegalArgumentException.class, () -> SteeringSystem.fromMenuNumber(99));
    }
}
