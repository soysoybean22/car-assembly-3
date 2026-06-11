package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EngineTest {

    @Test
    void menuNumber_매핑() {
        assertEquals(1, Engine.GM.getMenuNumber());
        assertEquals(2, Engine.TOYOTA.getMenuNumber());
        assertEquals(3, Engine.WIA.getMenuNumber());
        assertEquals(4, Engine.BROKEN.getMenuNumber());
    }

    @Test
    void displayName_반환() {
        assertEquals("GM",       Engine.GM.getDisplayName());
        assertEquals("TOYOTA",   Engine.TOYOTA.getDisplayName());
        assertEquals("WIA",      Engine.WIA.getDisplayName());
        assertEquals("고장난 엔진", Engine.BROKEN.getDisplayName());
    }

    @Test
    void isBroken_고장엔진() {
        assertTrue(Engine.BROKEN.isBroken());
    }

    @Test
    void isBroken_정상엔진() {
        assertFalse(Engine.GM.isBroken());
        assertFalse(Engine.TOYOTA.isBroken());
        assertFalse(Engine.WIA.isBroken());
    }

    @Test
    void fromMenuNumber_정상() {
        assertEquals(Engine.GM,     Engine.fromMenuNumber(1));
        assertEquals(Engine.TOYOTA, Engine.fromMenuNumber(2));
        assertEquals(Engine.WIA,    Engine.fromMenuNumber(3));
        assertEquals(Engine.BROKEN, Engine.fromMenuNumber(4));
    }

    @Test
    void fromMenuNumber_범위초과_예외() {
        assertThrows(IllegalArgumentException.class, () -> Engine.fromMenuNumber(0));
        assertThrows(IllegalArgumentException.class, () -> Engine.fromMenuNumber(99));
    }
}
